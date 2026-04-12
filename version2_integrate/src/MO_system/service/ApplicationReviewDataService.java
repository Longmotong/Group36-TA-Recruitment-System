package com.mojobsystem.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojobsystem.model.applicationreview.ApplicationItem;
import com.mojobsystem.model.applicationreview.ReviewDashboardMetrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Application Review data: MO-scoped list, filters, metrics. Persists via {@link ApplicationReviewPersistence}.
 */
public class ApplicationReviewDataService {

    private final ObjectMapper objectMapper;
    private final Path dataRoot;

    public ApplicationReviewDataService(Path dataRoot) {
        this.dataRoot = dataRoot;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Path getDataRoot() {
        return dataRoot;
    }

    public List<ApplicationItem> loadApplicationsForMo(String moUserId, Set<String> moJobIds) {
        return loadApplicationsForMo(moUserId, moJobIds, null);
    }

    /**
     * Loads applications for jobs in {@code moJobIds} with {@code workflow.assignedMO == moUserId}.
     * When {@code restrictJobId} is non-blank, only that job id.
     */
    public List<ApplicationItem> loadApplicationsForMo(String moUserId, Set<String> moJobIds, String restrictJobId) {
        if (moUserId == null || moUserId.isBlank() || moJobIds == null || moJobIds.isEmpty()) {
            return List.of();
        }
        Path appDir = dataRoot.resolve("applications");
        if (!Files.isDirectory(appDir)) {
            return List.of();
        }
        List<ApplicationItem> result = new ArrayList<>();
        try (Stream<Path> stream = Files.list(appDir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            ApplicationItem item = objectMapper.readValue(path.toFile(), ApplicationItem.class);
                            if (item == null || item.getJobId() == null) {
                                return;
                            }
                            if (!moJobIds.contains(item.getJobId())) {
                                return;
                            }
                            // 过滤掉已取消的申请
                            String currentStatus = item.getStatus() == null ? "" : safe(item.getStatus().getCurrent()).toLowerCase(Locale.ROOT);
                            if ("cancelled".equals(currentStatus)) {
                                return;
                            }
                            if (restrictJobId != null && !restrictJobId.isBlank()
                                    && !restrictJobId.equals(item.getJobId())) {
                                return;
                            }
                            // Check workflow assignment
                            String assigned = item.getWorkflow() == null ? "" : item.getWorkflow().getAssignedMO();
                            boolean hasAssignment = !assigned.isBlank();
                            boolean jobMatches = moJobIds.contains(item.getJobId());
                            
                            if (hasAssignment) {
                                // If workflow is set, must match current MO
                                if (!moUserId.equals(assigned)) {
                                    return;
                                }
                            } else {
                                // If no workflow, job must be in MO's list
                                if (!jobMatches) {
                                    return;
                                }
                            }
                            result.add(item);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
            return List.of();
        }
        return result;
    }

    public ReviewDashboardMetrics buildDashboardMetrics(int managedJobCount, List<ApplicationItem> applications) {
        int total = applications.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;
        for (ApplicationItem item : applications) {
            String s = normalizeStatusForMetrics(item);
            if ("pending".equals(s)) {
                pending++;
            } else if ("approved".equals(s)) {
                approved++;
            } else if ("rejected".equals(s)) {
                rejected++;
            }
        }
        return new ReviewDashboardMetrics(managedJobCount, total, pending, approved, rejected);
    }

    /**
     * UI-normalized status: pending | approved | rejected (maps {@code accepted} → approved).
     */
    public static String normalizeStatusForMetrics(ApplicationItem item) {
        String status = item.getStatus() == null ? "" : safe(item.getStatus().getCurrent()).toLowerCase(Locale.ROOT);
        if ("under_review".equals(status) || "pending".equals(status)) {
            return "pending";
        }
        if ("accepted".equals(status)) {
            return "approved";
        }
        if ("rejected".equals(status)) {
            return "rejected";
        }
        if (status.isBlank() && item.getReview() != null) {
            String decision = safe(item.getReview().getDecision()).toLowerCase(Locale.ROOT);
            if ("accepted".equals(decision) || "approved".equals(decision)) {
                return "approved";
            }
            if ("rejected".equals(decision)) {
                return "rejected";
            }
            return "pending";
        }
        return status.isBlank() ? "pending" : status;
    }

    public List<ApplicationItem> filterApplications(List<ApplicationItem> all,
                                                      String keyword,
                                                      String courseCode,
                                                      String statusFilter) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String cc = courseCode == null ? "" : courseCode.trim().toLowerCase(Locale.ROOT);
        String st = statusFilter == null ? "" : statusFilter.trim().toLowerCase(Locale.ROOT);

        List<ApplicationItem> filtered = new ArrayList<>();
        for (ApplicationItem item : all) {
            String name = item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getFullName()).toLowerCase(Locale.ROOT);
            String sid = safe(item.getStudentId()).toLowerCase(Locale.ROOT);
            String c = item.getJobSnapshot() == null ? "" : safe(item.getJobSnapshot().getCourseCode()).toLowerCase(Locale.ROOT);
            String s = normalizeStatusForMetrics(item);

            boolean keywordMatch = kw.isBlank() || name.contains(kw) || sid.contains(kw);
            boolean courseMatch = cc.isBlank() || c.equals(cc);
            boolean statusMatch = st.isBlank() || s.equals(st);

            if (keywordMatch && courseMatch && statusMatch) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public void submitReview(ApplicationItem item, String decision, String notes, String moUserId) throws IOException {
        if (item == null || item.getApplicationId() == null) {
            return;
        }
        ApplicationReviewPersistence.submitReview(dataRoot, item.getApplicationId(), decision, notes, moUserId);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
