package edu.ebu6304.standalone.model;

public record DashboardMetrics(
        int managedJobs,
        int totalApplications,
        int pendingReviews,
        int approvedCount,
        int rejectedCount
) {
}
