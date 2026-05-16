package com.mojobsystem.model.applicationreview;

public record ReviewDashboardMetrics(
        int managedJobs,
        int totalApplications,
        int pendingReviews,
        int approvedCount,
        int rejectedCount
) {
}
