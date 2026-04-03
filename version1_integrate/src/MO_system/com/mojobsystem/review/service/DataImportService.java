package com.mojobsystem.review.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojobsystem.review.model.ApplicationItem;
import com.mojobsystem.review.model.DashboardMetrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class DataImportService {
    private final ObjectMapper objectMapper;
    private final Path dataRoot;

    public DataImportService(Path dataRoot) {
        this.dataRoot = dataRoot;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<ApplicationItem> loadApplications() {
        Path appDir = dataRoot.resolve("applications");
        if (!Files.exists(appDir)) {
            return List.of();
        }

        List<ApplicationItem> result = new ArrayList<>();
        try (Stream<Path> stream = Files.list(appDir)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            ApplicationItem item = objectMapper.readValue(path.toFile(), ApplicationItem.class);
                            if (item != null) {
                                result.add(item);
                            }
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
            return List.of();
        }

        return result;
    }

    public int countManagedJobsForMO(String moUserId) {
        Path indexFile = dataRoot.resolve("indexes").resolve("mo_jobs_index.json");
        if (!Files.exists(indexFile)) {
            return 0;
        }

        try {
            JsonNode root = objectMapper.readTree(indexFile.toFile());
            JsonNode rows = root.get("rows");
            if (rows == null || !rows.isArray()) {
                return 0;
            }
            int count = 0;
            for (JsonNode row : rows) {
                String userId = asText(row, "userId");
                if (moUserId.equals(userId)) {
                    count++;
                }
            }
            return count;
        } catch (IOException e) {
            return 0;
        }
    }

    public DashboardMetrics buildDashboardMetrics(String moUserId, List<ApplicationItem> applications) {
        int managedJobs = countManagedJobsForMO(moUserId);
        int total = applications.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;

        for (ApplicationItem item : applications) {
            String status = normalizeStatus(item);
            if ("pending".equals(status) || "under_review".equals(status)) {
                pending++;
            }
            if ("approved".equals(status)) {
                approved++;
            }
            if ("rejected".equals(status)) {
                rejected++;
            }
        }
        return new DashboardMetrics(managedJobs, total, pending, approved, rejected);
    }

    public List<ApplicationItem> filterApplications(List<ApplicationItem> all,
                                                    String keyword,
                                                    String courseCode,
                                                    String status) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String cc = courseCode == null ? "" : courseCode.trim().toLowerCase(Locale.ROOT);
        String st = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);

        List<ApplicationItem> filtered = new ArrayList<>();
        for (ApplicationItem item : all) {
            String name = item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getFullName()).toLowerCase(Locale.ROOT);
            String sid = safe(item.getStudentId()).toLowerCase(Locale.ROOT);
            String c = item.getJobSnapshot() == null ? "" : safe(item.getJobSnapshot().getCourseCode()).toLowerCase(Locale.ROOT);
            String s = normalizeStatus(item);

            boolean keywordMatch = kw.isBlank() || name.contains(kw) || sid.contains(kw);
            boolean courseMatch = cc.isBlank() || c.equals(cc);
            boolean statusMatch = st.isBlank() || s.equals(st);

            if (keywordMatch && courseMatch && statusMatch) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public void submitReview(ApplicationItem item, String decision, String notes) {
        if (item == null) {
            return;
        }

        if (item.getReview() == null) {
            item.setReview(new ApplicationItem.Review());
        }
        if (item.getStatus() == null) {
            item.setStatus(new ApplicationItem.Status());
        }

        item.getReview().setDecision(decision);
        item.getReview().setReviewerNotes(notes == null ? "" : notes.trim());
        item.getReview().setReviewedBy("u_mo_001");
        item.getReview().setReviewedAt(java.time.LocalDateTime.now().toString());

        if ("approved".equals(decision)) {
            item.getStatus().setCurrent("approved");
            item.getStatus().setLabel("Approved");
        } else {
            item.getStatus().setCurrent("rejected");
            item.getStatus().setLabel("Rejected");
        }

        persistApplication(item);
    }

    private void persistApplication(ApplicationItem item) {
        Path file = dataRoot.resolve("applications").resolve(item.getApplicationId() + ".json");
        if (!Files.exists(file)) {
            return;
        }

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), item);
        } catch (IOException ignored) {
        }
    }

    private String normalizeStatus(ApplicationItem item) {
        String raw = item.getStatus() == null ? "" : safe(item.getStatus().getCurrent()).toLowerCase(Locale.ROOT);
        if (raw.isBlank()) {
            String decision = item.getReview() == null ? "" : safe(item.getReview().getDecision()).toLowerCase(Locale.ROOT);
            if ("approved".equals(decision)) {
                return "approved";
            }
            if ("rejected".equals(decision)) {
                return "rejected";
            }
            return "pending";
        }
        if ("under_review".equals(raw)) {
            return "pending";
        }
        return raw;
    }

    private String asText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null ? "" : v.asText("");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
