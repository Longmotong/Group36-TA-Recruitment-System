package com.taapp.model;

import java.util.Map;

public class Statistics {
    public static class DepartmentStats {
        private final int total;
        private final int filled;
        private final int applications;

        public DepartmentStats(int total, int filled, int applications) {
            this.total = total;
            this.filled = filled;
            this.applications = applications;
        }

        public int getTotal() { return total; }
        public int getFilled() { return filled; }
        public int getApplications() { return applications; }
    }

    private final int totalApplications;
    private final int approvedApplications;
    private final int pendingApplications;
    private final int rejectedApplications;
    private final int approvalRate;
    private final int totalPositions;
    private final int openPositions;
    private final int filledPositions;
    private final int fillRate;
    private final int totalTAs;
    private final int activeTAs;
    private final int totalWorkload;
    private final int avgWorkload;
    private final Map<String, DepartmentStats> departmentStats;

    public Statistics(
            int totalApplications,
            int approvedApplications,
            int pendingApplications,
            int rejectedApplications,
            int approvalRate,
            int totalPositions,
            int openPositions,
            int filledPositions,
            int fillRate,
            int totalTAs,
            int activeTAs,
            int totalWorkload,
            int avgWorkload,
            Map<String, DepartmentStats> departmentStats) {
        this.totalApplications = totalApplications;
        this.approvedApplications = approvedApplications;
        this.pendingApplications = pendingApplications;
        this.rejectedApplications = rejectedApplications;
        this.approvalRate = approvalRate;
        this.totalPositions = totalPositions;
        this.openPositions = openPositions;
        this.filledPositions = filledPositions;
        this.fillRate = fillRate;
        this.totalTAs = totalTAs;
        this.activeTAs = activeTAs;
        this.totalWorkload = totalWorkload;
        this.avgWorkload = avgWorkload;
        this.departmentStats = departmentStats;
    }

    public int getTotalApplications() { return totalApplications; }
    public int getApprovedApplications() { return approvedApplications; }
    public int getPendingApplications() { return pendingApplications; }
    public int getRejectedApplications() { return rejectedApplications; }
    public int getApprovalRate() { return approvalRate; }
    public int getTotalPositions() { return totalPositions; }
    public int getOpenPositions() { return openPositions; }
    public int getFilledPositions() { return filledPositions; }
    public int getFillRate() { return fillRate; }
    public int getTotalTAs() { return totalTAs; }
    public int getActiveTAs() { return activeTAs; }
    public int getTotalWorkload() { return totalWorkload; }
    public int getAvgWorkload() { return avgWorkload; }
    public Map<String, DepartmentStats> getDepartmentStats() { return departmentStats; }
}

