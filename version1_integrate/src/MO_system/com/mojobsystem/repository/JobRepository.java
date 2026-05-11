package com.mojobsystem.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojobsystem.MoContext;
import com.mojobsystem.model.Job;
import login.AppDataRoot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JobRepository {
    private static final String DEFAULT_MO_USER = "u_mo_001";

    private final Path dataRoot;
    private final Path jobsFilePath;
    private final Path jobsDirPath;
    private final Path indexesDirPath;
    private final Path jobsIndexFilePath;
    private final Path moJobsIndexFilePath;

    private final ObjectMapper objectMapper;

    public JobRepository() {
        this.dataRoot = resolveDataRoot();
        this.jobsFilePath = dataRoot.resolve("jobs.json");
        this.jobsDirPath = dataRoot.resolve("jobs");
        this.indexesDirPath = dataRoot.resolve("indexes");
        this.jobsIndexFilePath = indexesDirPath.resolve("jobs_index.json");
        this.moJobsIndexFilePath = indexesDirPath.resolve("mo_jobs_index.json");

        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 与 {@link com.mojobsystem.integration.IntegrationDataService} 一致：通过 {@link login.AppDataRoot} 解析共享 {@code data} 根目录。
     */
    private static Path resolveDataRoot() {
        return AppDataRoot.asPath();
    }

    public List<Job> loadAllJobs() {
        try {
            return loadAllJobsFromDir();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load jobs from " + jobsDirPath, e);
        }
    }

    /**
     * 从 data/jobs/ 文件夹读取所有 Job
     */
    private List<Job> loadAllJobsFromDir() throws IOException {
        List<Job> jobs = new ArrayList<>();
        if (Files.notExists(jobsDirPath)) {
            Files.createDirectories(jobsDirPath);
            return jobs;
        }

        try (Stream<Path> stream = Files.list(jobsDirPath)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                  .filter(p -> !Files.isDirectory(p))
                  .forEach(p -> {
                      try {
                          Job job = loadJobFromFile(p);
                          if (job != null) {
                              jobs.add(job);
                          }
                      } catch (Exception e) {
                          System.err.println("Failed to load job from: " + p);
                      }
                  });
        }
        return jobs;
    }

    /**
     * 从单个 Job 文件加载并映射到 Job 模型
     */
    private Job loadJobFromFile(Path file) throws IOException {
        JsonNode root = objectMapper.readTree(file.toFile());

        Job job = new Job();
        job.setId(nz(root.path("jobId").asText()));
        job.setTitle(nz(root.path("title").asText()));

        JsonNode course = root.path("course");
        job.setModuleCode(nz(course.path("courseCode").asText()).replace(" ", ""));
        job.setModuleName(nz(course.path("courseName").asText()));
        job.setCourseTerm(nz(course.path("term").asText()));
        job.setCourseYear(course.path("year").asInt(0));

        job.setDepartment(nz(root.path("department").asText()));

        JsonNode instructor = root.path("instructor");
        job.setInstructorName(nz(instructor.path("name").asText()));
        job.setInstructorEmail(nz(instructor.path("email").asText()));

        JsonNode employment = root.path("employment");
        job.setWeeklyHours(employment.path("weeklyHours").asInt(0));
        job.setLocationMode(nz(employment.path("locationMode").asText()));
        job.setEmploymentType(nz(employment.path("employmentType").asText()));

        JsonNode dates = root.path("dates");
        String dl = dates.path("deadline").asText("");
        if (!dl.isBlank() && dl.length() >= 10) {
            job.setDeadline(dl.substring(0, 10));
        }

        JsonNode content = root.path("content");
        job.setDescription(nz(content.path("description").asText()));

        JsonNode reqs = content.path("requirements");
        List<String> requirements = new ArrayList<>();
        if (reqs.isArray()) {
            reqs.forEach(n -> requirements.add(n.asText()));
        }
        job.setAdditionalRequirements(String.join("\n", requirements));

        JsonNode skills = content.path("preferredSkills");
        List<String> skillList = new ArrayList<>();
        if (skills.isArray()) {
            skills.forEach(n -> skillList.add(n.asText()));
        }
        job.setRequiredSkills(skillList);

        JsonNode lifecycle = root.path("lifecycle");
        job.setStatus(nz(lifecycle.path("status").asText("open")));

        JsonNode stats = root.path("stats");
        job.setApplicantsCount(stats.path("applicationCount").asInt(0));

        return job;
    }

    /**
     * 从 data/jobs/ 文件夹加载该 MO 管理的 Jobs
     * 直接从 Job 文件的 ownership.managedBy 字段判断
     */
    public List<Job> loadJobsForMo(String moUserId) {
        if (moUserId == null || moUserId.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<Job> allJobs = loadAllJobsFromDir();
            List<Job> moJobs = new ArrayList<>();
            for (Job job : allJobs) {
                if (isManagedBy(job.getId(), moUserId)) {
                    moJobs.add(job);
                }
            }
            return moJobs;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load jobs for MO " + moUserId, e);
        }
    }

    /**
     * 检查 Job 是否由指定 MO 管理
     */
    private boolean isManagedBy(String jobId, String moUserId) {
        Path jobFile = jobsDirPath.resolve(jobId + ".json");
        if (Files.notExists(jobFile)) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(jobFile.toFile());
            JsonNode ownership = root.path("ownership");
            JsonNode managedBy = ownership.path("managedBy");
            if (managedBy.isArray() && !managedBy.isEmpty()) {
                for (JsonNode m : managedBy) {
                    if (moUserId.equals(m.asText())) {
                        return true;
                    }
                }
                return false;
            }
            String createdBy = ownership.path("createdBy").asText("");
            return moUserId.equals(createdBy);
        } catch (IOException e) {
            return false;
        }
    }

    public Set<String> loadMoJobIds(String moUserId) {
        try {
            if (Files.notExists(moJobsIndexFilePath)) {
                return Set.of();
            }
            JsonNode root = objectMapper.readTree(moJobsIndexFilePath.toFile());
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
        Path rich = jobsDirPath.resolve(job.getId() + ".json");
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
        Path rich = jobsDirPath.resolve(jobId + ".json");
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
            saveAllJobs(new ArrayList<>(byId.values()), moUserId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save jobs for MO", e);
        }
    }

    private List<Job> loadAllJobsFromFile() throws IOException {
        ensureStorageReady();
        if (Files.size(jobsFilePath) == 0) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(jobsFilePath.toFile(), new TypeReference<List<Job>>() {});
    }

    /**
     * @param actingMoUserId MO performing the save; written into {@code ownership.managedBy} for new job files
     *                       so {@link #loadJobsForMo(String)} matches {@link MoContext#CURRENT_MO_ID}.
     */
    public void saveAllJobs(List<Job> jobs, String actingMoUserId) {
        try {
            ensureStorageReady();
            List<Job> safeJobs = (jobs == null) ? new ArrayList<>() : new ArrayList<>(jobs);
            objectMapper.writeValue(jobsFilePath.toFile(), safeJobs);

            Files.createDirectories(jobsDirPath);
            String moForOwnership = (actingMoUserId != null && !actingMoUserId.isBlank())
                    ? actingMoUserId.trim()
                    : (MoContext.CURRENT_MO_ID != null && !MoContext.CURRENT_MO_ID.isBlank()
                            ? MoContext.CURRENT_MO_ID
                            : DEFAULT_MO_USER);
            Set<String> ids = new HashSet<>();
            for (Job j : safeJobs) {
                ids.add(j.getId());
                Path jobFile = jobsDirPath.resolve(j.getId() + ".json");
                JsonNode existingRich = Files.exists(jobFile) ? objectMapper.readTree(jobFile.toFile()) : null;
                ObjectNode rich = JobRichFileWriter.toJsonNode(j, objectMapper, existingRich, moForOwnership);
                objectMapper.writeValue(jobFile.toFile(), rich);
            }

            try (Stream<Path> stream = Files.list(jobsDirPath)) {
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
        Files.createDirectories(indexesDirPath);
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
            e.put("status", indexStatus(j.getStatus()));
            arr.add(e);
        }
        root.set("jobs", arr);
        objectMapper.writeValue(jobsIndexFilePath.toFile(), root);
    }

    private static String indexStatus(String status) {
        if (status == null) {
            return "open";
        }
        String s = status.trim().toLowerCase(Locale.ENGLISH);
        if ("closed".equals(s)) {
            return "closed";
        }
        if ("draft".equals(s)) {
            return "draft";
        }
        return "open";
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private void syncMoJobsIndex(List<Job> jobs) throws IOException {
        Files.createDirectories(indexesDirPath);
        Set<String> valid = new HashSet<>();
        for (Job j : jobs) {
            valid.add(j.getId());
        }

        ObjectNode root;
        if (Files.exists(moJobsIndexFilePath)) {
            root = (ObjectNode) objectMapper.readTree(moJobsIndexFilePath.toFile());
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

        ArrayNode u1 = (ArrayNode) moJobs.get(DEFAULT_MO_USER);
        if (u1 == null) {
            u1 = objectMapper.createArrayNode();
            moJobs.set(DEFAULT_MO_USER, u1);
        }
        for (String vid : valid) {
            if (!inAny.contains(vid)) {
                u1.add(vid);
                inAny.add(vid);
            }
        }

        objectMapper.writeValue(moJobsIndexFilePath.toFile(), root);
    }

    private void ensureStorageReady() throws IOException {
        if (Files.notExists(dataRoot)) {
            Files.createDirectories(dataRoot);
        }
        if (Files.notExists(jobsFilePath)) {
            Files.writeString(jobsFilePath, "[]");
        }
    }
}
