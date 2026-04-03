package com.mojobsystem.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import login.AppDataRoot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Reads {@code data/applications/*.json} for dashboard metrics and TA allocation rows.
 */
public class ApplicationRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Path applicationsDir() {
        return AppDataRoot.asPath().resolve("applications");
    }

    public List<AllocatedTaRecord> listAcceptedForJob(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return List.of();
        }
        List<AllocatedTaRecord> out = new ArrayList<>();
        for (Path p : listApplicationFiles()) {
            try {
                JsonNode root = objectMapper.readTree(p.toFile());
                if (!jobId.equals(text(root, "jobId"))) {
                    continue;
                }
                JsonNode st = root.path("status");
                String current = text(st, "current");
                if (!"accepted".equalsIgnoreCase(current)) {
                    continue;
                }
                JsonNode app = root.path("applicantSnapshot");
                String name = text(app, "fullName");
                String sid = text(app, "studentId");
                String email = text(app, "email");
                JsonNode jobSnap = root.path("jobSnapshot");
                int weekly = jobSnap.path("weeklyHours").asInt(0);
                List<String> skills = new ArrayList<>();
                JsonNode form = root.path("applicationForm").path("relevantSkills");
                if (form.isArray()) {
                    for (JsonNode n : form) {
                        skills.add(n.asText());
                    }
                }
                out.add(new AllocatedTaRecord(name, sid, email, weekly, skills));
            } catch (IOException ignored) {
                // skip bad file
            }
        }
        return out;
    }

    /**
     * Applications assigned to this MO that need attention (pending / under review) for given jobs.
     */
    public int countPendingReviewsForMo(String moUserId, Set<String> moJobIds) {
        if (moUserId == null || moJobIds == null || moJobIds.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (Path p : listApplicationFiles()) {
            try {
                JsonNode root = objectMapper.readTree(p.toFile());
                String jobId = text(root, "jobId");
                if (!moJobIds.contains(jobId)) {
                    continue;
                }
                String assigned = text(root.path("workflow"), "assignedMO");
                if (!moUserId.equals(assigned)) {
                    continue;
                }
                String current = text(root.path("status"), "current").toLowerCase(Locale.ENGLISH);
                if ("pending".equals(current) || "under_review".equals(current)) {
                    n++;
                }
            } catch (IOException ignored) {
            }
        }
        return n;
    }

    public int countApplicationsForJob(String jobId) {
        if (jobId == null) {
            return 0;
        }
        int n = 0;
        for (Path p : listApplicationFiles()) {
            try {
                JsonNode root = objectMapper.readTree(p.toFile());
                if (jobId.equals(text(root, "jobId"))) {
                    n++;
                }
            } catch (IOException ignored) {
            }
        }
        return n;
    }

    private static String text(JsonNode parent, String field) {
        if (parent == null || parent.isMissingNode()) {
            return "";
        }
        JsonNode n = parent.get(field);
        return n == null || n.isNull() ? "" : n.asText("");
    }

    private List<Path> listApplicationFiles() {
        Path dir = applicationsDir();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".json")).toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public record AllocatedTaRecord(
            String fullName,
            String studentId,
            String email,
            int weeklyHours,
            List<String> skills
    ) {
        public AllocatedTaRecord {
            skills = skills == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(skills));
        }
    }
}
