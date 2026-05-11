package com.taapp.model;

public class Application {
    private final String id;
    private final String taId;
    private final String taName;
    private final String positionId;
    private final String positionTitle;
    private final String course;
    private final String department;
    private final String appliedDate;
    private final String status;

    public Application(
            String id,
            String taId,
            String taName,
            String positionId,
            String positionTitle,
            String course,
            String department,
            String appliedDate,
            String status) {
        this.id = id;
        this.taId = taId;
        this.taName = taName;
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.course = course;
        this.department = department;
        this.appliedDate = appliedDate;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTaId() { return taId; }
    public String getTaName() { return taName; }
    public String getPositionId() { return positionId; }
    public String getPositionTitle() { return positionTitle; }
    public String getCourse() { return course; }
    public String getDepartment() { return department; }
    public String getAppliedDate() { return appliedDate; }
    public String getStatus() { return status; }
}

