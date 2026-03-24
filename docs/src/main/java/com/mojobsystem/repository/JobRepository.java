package com.mojobsystem.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojobsystem.model.Job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class JobRepository {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path JOBS_FILE = DATA_DIR.resolve("jobs.json");
    private static final Path JOBS_DIR = DATA_DIR.resolve("jobs");
    private static final Path INDEXES_DIR = DATA_DIR.resolve("indexes");
    private static final Path JOBS_INDEX_FILE = INDEXES_DIR.resolve("jobs_index.json");
    private static final Path MO_JOBS_INDEX_FILE = INDEXES_DIR.resolve("mo_jobs_index.json");
    private static final String DEFAULT_MO_USER = "u_mo_001";

    private final ObjectMapper objectMapper;

    public JobRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<Job> loadAllJobs() {
        try {
            ensureStorageReady();

            if (Files.size(JOBS_FILE) == 0) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(JOBS_FILE.toFile(), new TypeReference<List<Job>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load jobs from " + JOBS_FILE, e);
        }
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
                ObjectNode rich = JobRichFileWriter.toJsonNode(j, objectMapper, DEFAULT_MO_USER);
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
            e.put("status", indexStatus(j.getStatus()));
            arr.add(e);
        }
        root.set("jobs", arr);
        objectMapper.writeValue(JOBS_INDEX_FILE.toFile(), root);
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
