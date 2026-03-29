package com.mojobsystem.model;

/**
 * Dashboard metrics for MO overview.
 */
public class DashboardMetrics {
    private final int managedJobs;
    private final int totalApplications;
    private final int pendingReviews;
    private final int approvedCount;
    private final int rejectedCount;

    public DashboardMetrics(int managedJobs, int totalApplications, int pendingReviews,
                          int approvedCount, int rejectedCount) {
        this.managedJobs = managedJobs;
        this.totalApplications = totalApplications;
        this.pendingReviews = pendingReviews;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
    }

    public int managedJobs() { return managedJobs; }
    public int totalApplications() { return totalApplications; }
    public int pendingReviews() { return pendingReviews; }
    public int approvedCount() { return approvedCount; }
    public int rejectedCount() { return rejectedCount; }
}
