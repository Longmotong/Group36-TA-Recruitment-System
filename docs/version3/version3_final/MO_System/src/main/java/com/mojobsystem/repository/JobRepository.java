package com.mojobsystem.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojobsystem.DataRoot;
import com.mojobsystem.MoContext;
import com.mojobsystem.model.job.Job;
import com.mojobsystem.model.job.JobStatusUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JobRepository {
    private static final Path DATA_DIR = DataRoot.resolve();
    private static final Path JOBS_FILE = DATA_DIR.resolve("jobs.json");
    private static final Path JOBS_DIR = DATA_DIR.resolve("jobs");
    private static final Path INDEXES_DIR = DATA_DIR.resolve("indexes");
    private static final Path JOBS_INDEX_FILE = INDEXES_DIR.resolve("jobs_index.json");
    private static final Path MO_JOBS_INDEX_FILE = INDEXES_DIR.resolve("mo_jobs_index.json");
    private final ObjectMapper objectMapper;

    public JobRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<Job> loadAllJobs() {
        try {
            List<Job> jobs = loadAllJobsFromFile();
            for (Job j : jobs) {
                enrichFromRichJobFile(j);
            }
            return jobs;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load jobs from " + JOBS_FILE, e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to load jobs from " + JOBS_FILE, e);
        }
    }

    /**
     * Jobs linked to an MO in {@code data/indexes/mo_jobs_index.json}.
     * <p>
     * Only enriches from {@code data/jobs/{id}.json} for jobs in this MO (unlike iterating
     * {@link #loadAllJobs()} and filtering, which used to enrich every job in {@code jobs.json}
     * and caused noticeable EDT stalls when opening Job Management).
     */
    public List<Job> loadJobsForMo(String moUserId) {
        Set<String> allowed = loadMoJobIds(moUserId);
        if (allowed.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<Job> jobs = loadAllJobsFromFile();
            List<Job> out = new ArrayList<>();
            for (Job j : jobs) {
                if (allowed.contains(j.getId())) {
                    enrichFromRichJobFile(j);
                    out.add(j);
                }
            }
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load jobs for MO " + moUserId, e);
        }
    }

    public Set<String> loadMoJobIds(String moUserId) {
        try {
            if (Files.notExists(MO_JOBS_INDEX_FILE)) {
                return Set.of();
            }
            JsonNode root = objectMapper.readTree(MO_JOBS_INDEX_FILE.toFile());
            JsonNode moJobs = root.path("moJobs").path(moUserId);
            if (!moJobs.isArray()) {
                return Set.of();
            }
            Set<String> ids = new LinkedHashSet<>();
            for (JsonNode n : moJobs) {
                ids.add(n.asText());
            }
            return ids;
        } catch (IOException e) {
            return Set.of();
        }
    }

    /**
     * When present, merges application counters from rich {@code data/jobs/{id}.json} into the flat model.
     */
    public void enrichFromRichJobFile(Job job) {
        if (job == null || job.getId() == null) {
            return;
        }
        Path rich = JOBS_DIR.resolve(job.getId() + ".json");
        if (Files.notExists(rich)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(rich.toFile());
            JsonNode stats = root.path("stats");
            int appCount = stats.path("applicationCount").asInt(-1);
            if (appCount >= 0) {
                job.setApplicantsCount(appCount);
            }
            JsonNode course = root.path("course");
            String cc = text(course, "courseCode");
            String cn = text(course, "courseName");
            if (!cc.isBlank()) {
                job.setModuleCode(cc.replace(" ", "").trim());
            }
            if (!cn.isBlank()) {
                job.setModuleName(cn);
            }
            String dept = text(root, "department");
            if (!dept.isBlank()) {
                job.setDepartment(dept);
            }
            JsonNode ins = root.path("instructor");
            String inName = text(ins, "name");
            String inMail = text(ins, "email");
            if (!inName.isBlank()) {
                job.setInstructorName(inName);
            }
            if (!inMail.isBlank()) {
                job.setInstructorEmail(inMail);
            }
            JsonNode emp = root.path("employment");
            int wh = emp.path("weeklyHours").asInt(-1);
            if (wh > 0) {
                job.setWeeklyHours(wh);
            }
            String loc = text(emp, "locationMode");
            if (!loc.isBlank()) {
                job.setLocationMode(loc);
            }
            String et = text(emp, "employmentType");
            if (!et.isBlank()) {
                job.setEmploymentType(et);
            }
            JsonNode dates = root.path("dates");
            String dl = text(dates, "deadline");
            if (!dl.isBlank() && dl.length() >= 10) {
                job.setDeadline(dl.substring(0, 10));
            }
        } catch (IOException ignored) {
        }
    }

    public RichJobStats readRichJobStats(String jobId) {
        if (jobId == null) {
            return null;
        }
        Path rich = JOBS_DIR.resolve(jobId + ".json");
        if (Files.notExists(rich)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(rich.toFile());
            JsonNode stats = root.path("stats");
            return new RichJobStats(
                    stats.path("applicationCount").asInt(0),
                    stats.path("pendingCount").asInt(0),
                    stats.path("acceptedCount").asInt(0),
                    stats.path("rejectedCount").asInt(0)
            );
        } catch (IOException e) {
            return null;
        }
    }

    private static String text(JsonNode parent, String field) {
        if (parent == null || parent.isMissingNode()) {
            return "";
        }
        JsonNode n = parent.get(field);
        return n == null || n.isNull() ? "" : n.asText("");
    }

    public record RichJobStats(int applicationCount, int pendingCount, int acceptedCount, int rejectedCount) {
    }

    /**
     * Replace this MO's jobs in the global list while keeping jobs owned by other MOs.
     */
    public void saveJobsForMo(String moUserId, List<Job> moJobsView) {
        try {
            ensureStorageReady();
            Set<String> moIds = new HashSet<>(loadMoJobIds(moUserId));
            List<Job> all = loadAllJobsFromFile();
            Map<String, Job> byId = new LinkedHashMap<>();
            for (Job j : all) {
                byId.put(j.getId(), j);
            }
            Set<String> nextMo = new HashSet<>();
            if (moJobsView != null) {
                for (Job j : moJobsView) {
                    nextMo.add(j.getId());
                    byId.put(j.getId(), j);
                }
            }
            for (String id : moIds) {
                if (!nextMo.contains(id)) {
                    byId.remove(id);
                }
            }
            saveAllJobs(new ArrayList<>(byId.values()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save jobs for MO", e);
        }
    }

    private List<Job> loadAllJobsFromFile() throws IOException {
        ensureStorageReady();
        if (Files.size(JOBS_FILE) == 0) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(JOBS_FILE.toFile(), new TypeReference<List<Job>>() {});
    }

    public void saveAllJobs(List<Job> jobs) {
        try {
            ensureStorageReady();
            List<Job> safeJobs = (jobs == null) ? new ArrayList<>() : new ArrayList<>(jobs);
            objectMapper.writeValue(JOBS_FILE.toFile(), safeJobs);

            Files.createDirectories(JOBS_DIR);
            Set<String> ids = new HashSet<>();
            for (Job j : safeJobs) {
                ids.add(j.getId());
                Path jobFile = JOBS_DIR.resolve(j.getId() + ".json");
                ObjectNode rich = JobRichFileWriter.toJsonNode(j, objectMapper, MoContext.getCurrentMoUserId());
                objectMapper.writeValue(jobFile.toFile(), rich);
            }

            try (Stream<Path> stream = Files.list(JOBS_DIR)) {
                stream.filter(p -> {
                    String n = p.getFileName().toString();
                    return n.endsWith(".json") && n.startsWith("job_");
                }).forEach(p -> {
                    String name = p.getFileName().toString();
                    String id = name.substring(0, name.length() - 5);
                    if (!ids.contains(id)) {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete stale job file " + p, e);
                        }
                    }
                });
            }

            writeJobsIndex(safeJobs);
            syncMoJobsIndex(safeJobs);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save jobs", e);
        }
    }

    private void writeJobsIndex(List<Job> jobs) throws IOException {
        Files.createDirectories(INDEXES_DIR);
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode arr = objectMapper.createArrayNode();
        for (Job j : jobs) {
            ObjectNode e = objectMapper.createObjectNode();
            e.put("jobId", j.getId());
            e.put("title", nz(j.getTitle()));
            e.put("courseCode", nz(j.getModuleCode()));
            e.put("department", nz(j.getDepartment()));
            e.put("instructorName", nz(j.getInstructorName()));
            e.put("weeklyHours", j.getWeeklyHours());
            String dl = j.getDeadline();
            if (dl == null || dl.isBlank()) {
                dl = LocalDate.now().plusWeeks(2).toString();
            }
            e.put("deadline", dl);
            e.put("locationMode", nz(j.getLocationMode()));
            e.put("employmentType", nz(j.getEmploymentType()));
            e.put("status", JobStatusUtil.canonical(j.getStatus()));
            arr.add(e);
        }
        root.set("jobs", arr);
        objectMapper.writeValue(JOBS_INDEX_FILE.toFile(), root);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private void syncMoJobsIndex(List<Job> jobs) throws IOException {
        Files.createDirectories(INDEXES_DIR);
        Set<String> valid = new HashSet<>();
        for (Job j : jobs) {
            valid.add(j.getId());
        }

        ObjectNode root;
        if (Files.exists(MO_JOBS_INDEX_FILE)) {
            root = (ObjectNode) objectMapper.readTree(MO_JOBS_INDEX_FILE.toFile());
        } else {
            root = objectMapper.createObjectNode();
            root.set("moJobs", objectMapper.createObjectNode());
        }
        ObjectNode moJobs = (ObjectNode) root.get("moJobs");
        if (moJobs == null) {
            moJobs = objectMapper.createObjectNode();
            root.set("moJobs", moJobs);
        }

        List<String> moIds = new ArrayList<>();
        moJobs.fieldNames().forEachRemaining(moIds::add);
        for (String moId : moIds) {
            JsonNode node = moJobs.get(moId);
            if (!node.isArray()) {
                continue;
            }
            ArrayNode arr = (ArrayNode) node;
            ArrayNode keep = objectMapper.createArrayNode();
            for (JsonNode idNode : arr) {
                String jid = idNode.asText();
                if (valid.contains(jid)) {
                    keep.add(jid);
                }
            }
            moJobs.set(moId, keep);
        }

        Set<String> inAny = new HashSet<>();
        moIds.clear();
        moJobs.fieldNames().forEachRemaining(moIds::add);
        for (String moId : moIds) {
            JsonNode node = moJobs.get(moId);
            if (node != null && node.isArray()) {
                for (JsonNode n : node) {
                    inAny.add(n.asText());
                }
            }
        }

        String currentMo = MoContext.getCurrentMoUserId();
        ArrayNode currentMoArr = (ArrayNode) moJobs.get(currentMo);
        if (currentMoArr == null) {
            currentMoArr = objectMapper.createArrayNode();
            moJobs.set(currentMo, currentMoArr);
        }
        for (String vid : valid) {
            if (!inAny.contains(vid)) {
                currentMoArr.add(vid);
                inAny.add(vid);
            }
        }

        objectMapper.writeValue(MO_JOBS_INDEX_FILE.toFile(), root);
    }

    private void ensureStorageReady() throws IOException {
        if (Files.notExists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
        if (Files.notExists(JOBS_FILE)) {
            Files.writeString(JOBS_FILE, "[]");
        }
    }
}
