package com.mojobsystem.review.model;

public record DashboardMetrics(
        int managedJobs,
        int totalApplications,
        int pendingReviews,
        int approvedCount,
        int rejectedCount
) {}
