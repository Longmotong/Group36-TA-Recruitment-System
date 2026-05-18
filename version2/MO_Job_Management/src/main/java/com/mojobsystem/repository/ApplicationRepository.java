package com.mojobsystem.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final Path APPLICATIONS_DIR = Path.of("data", "applications");

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                String appId = text(root, "applicationId");
                String name = text(app, "fullName");
                String sid = text(app, "studentId");
                String email = text(app, "email");
                String phone = text(app, "phoneNumber");
                String major = text(app, "programMajor");
                String year = text(app, "year");
                String gpa = gpaText(app);
                JsonNode jobSnap = root.path("jobSnapshot");
                int weekly = jobSnap.path("weeklyHours").asInt(0);
                String statusLabel = text(st, "label");
                if (statusLabel.isBlank()) {
                    statusLabel = "Accepted";
                }
                JsonNode formNode = root.path("applicationForm");
                String availability = text(formNode, "availability");
                List<String> skills = new ArrayList<>();
                JsonNode form = formNode.path("relevantSkills");
                if (form.isArray()) {
                    for (JsonNode n : form) {
                        skills.add(n.asText());
                    }
                }
                out.add(new AllocatedTaRecord(
                        appId, name, sid, phone, email, major, year, gpa, weekly, statusLabel, availability, skills));
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

    /**
     * All applications for this job (any assignee).
     */
    public int countApplicationsForJob(String jobId) {
        return countApplicationsForJob(jobId, null);
    }

    /**
     * Applications for this job; when {@code moUserId} is non-blank, only those with
     * {@code workflow.assignedMO} equal to it (same scope as {@link #listApplicationsForJob}).
     */
    public int countApplicationsForJob(String jobId, String moUserId) {
        if (jobId == null || jobId.isBlank()) {
            return 0;
        }
        int n = 0;
        for (Path p : listApplicationFiles()) {
            try {
                JsonNode root = objectMapper.readTree(p.toFile());
                if (!jobId.equals(text(root, "jobId"))) {
                    continue;
                }
                if (moUserId != null && !moUserId.isBlank()) {
                    String assigned = text(root.path("workflow"), "assignedMO");
                    if (!moUserId.equals(assigned)) {
                        continue;
                    }
                }
                n++;
            } catch (IOException ignored) {
            }
        }
        return n;
    }

    /**
     * Lists applications for one job. When {@code moUserId} is non-blank, only rows where
     * {@code workflow.assignedMO} matches (MO scope).
     */
    public List<ApplicationSummary> listApplicationsForJob(String jobId, String moUserId) {
        if (jobId == null || jobId.isBlank()) {
            return List.of();
        }
        List<ApplicationSummary> out = new ArrayList<>();
        for (Path p : listApplicationFiles()) {
            try {
                JsonNode root = objectMapper.readTree(p.toFile());
                if (!jobId.equals(text(root, "jobId"))) {
                    continue;
                }
                if (moUserId != null && !moUserId.isBlank()) {
                    String assigned = text(root.path("workflow"), "assignedMO");
                    if (!moUserId.equals(assigned)) {
                        continue;
                    }
                }
                out.add(toSummary(root));
            } catch (IOException ignored) {
            }
        }
        return out;
    }

    /**
     * Lists applications for the given MO across the given job ids (typically {@code mo_jobs_index} for that MO).
     */
    public List<ApplicationSummary> listApplicationsForMoJobs(String moUserId, Set<String> jobIds) {
        if (moUserId == null || moUserId.isBlank() || jobIds == null || jobIds.isEmpty()) {
            return List.of();
        }
        List<ApplicationSummary> out = new ArrayList<>();
        for (Path p : listApplicationFiles()) {
            try {
                JsonNode root = objectMapper.readTree(p.toFile());
                String jid = text(root, "jobId");
                if (!jobIds.contains(jid)) {
                    continue;
                }
                String assigned = text(root.path("workflow"), "assignedMO");
                if (!moUserId.equals(assigned)) {
                    continue;
                }
                out.add(toSummary(root));
            } catch (IOException ignored) {
            }
        }
        return out;
    }

    private ApplicationSummary toSummary(JsonNode root) {
        String appId = text(root, "applicationId");
        String jobId = text(root, "jobId");
        JsonNode app = root.path("applicantSnapshot");
        String sid = text(app, "studentId");
        String name = text(app, "fullName");
        String email = text(app, "email");
        JsonNode st = root.path("status");
        String current = text(st, "current");
        String label = text(st, "label");
        if (label.isBlank()) {
            label = current;
        }
        return new ApplicationSummary(appId, jobId, sid, name, email, current, label);
    }

    public record ApplicationSummary(
            String applicationId,
            String jobId,
            String studentId,
            String fullName,
            String email,
            String status,
            String statusLabel
    ) {
    }

    private static String text(JsonNode parent, String field) {
        if (parent == null || parent.isMissingNode()) {
            return "";
        }
        JsonNode n = parent.get(field);
        return n == null || n.isNull() ? "" : n.asText("");
    }

    private List<Path> listApplicationFiles() {
        if (!Files.isDirectory(APPLICATIONS_DIR)) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(APPLICATIONS_DIR)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".json")).toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public record AllocatedTaRecord(
            String applicationId,
            String fullName,
            String studentId,
            String phoneNumber,
            String email,
            String programMajor,
            String year,
            String gpa,
            int weeklyHours,
            String statusLabel,
            String availability,
            List<String> skills
    ) {
        public AllocatedTaRecord {
            skills = skills == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(skills));
        }
    }

    private static String gpaText(JsonNode applicantSnapshot) {
        if (applicantSnapshot == null || applicantSnapshot.isMissingNode()) {
            return "";
        }
        JsonNode n = applicantSnapshot.get("gpa");
        if (n == null || n.isNull() || n.isMissingNode()) {
            return "";
        }
        if (n.isNumber()) {
            double d = n.doubleValue();
            return (Math.abs(d - Math.rint(d)) < 1e-9) ? String.valueOf((long) d) : String.valueOf(d);
        }
        return n.asText("");
    }
}
