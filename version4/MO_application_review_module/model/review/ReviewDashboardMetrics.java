package MO_system.model.review;

public record ReviewDashboardMetrics(
        int managedJobs,
        int totalApplications,
        int pendingReviews,
        int approvedCount,
        int rejectedCount
) {
}
