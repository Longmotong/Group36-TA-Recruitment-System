package com.taapp.model;

public class Position {
    private final String id;
    private final String title;
    private final String course;
    private final String department;
    private final int requiredHours;
    private final int maxTAs;
    private final int assignedTAs;
    private final String status;
    private final int applicationCount;

    public Position(
            String id,
            String title,
            String course,
            String department,
            int requiredHours,
            int maxTAs,
            int assignedTAs,
            String status,
            int applicationCount) {
        this.id = id;
        this.title = title;
        this.course = course;
        this.department = department;
        this.requiredHours = requiredHours;
        this.maxTAs = maxTAs;
        this.assignedTAs = assignedTAs;
        this.status = status;
        this.applicationCount = applicationCount;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCourse() { return course; }
    public String getDepartment() { return department; }
    public int getRequiredHours() { return requiredHours; }
    public int getMaxTAs() { return maxTAs; }
    public int getAssignedTAs() { return assignedTAs; }
    public String getStatus() { return status; }
    public int getApplicationCount() { return applicationCount; }
}

