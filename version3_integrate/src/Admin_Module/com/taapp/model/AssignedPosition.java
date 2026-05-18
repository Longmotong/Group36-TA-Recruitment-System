package com.taapp.model;

public class AssignedPosition {
    private final String id;
    private final String positionTitle;
    private final String course;
    private final String department;
    private final int hours;
    private final String startDate;
    private final String endDate;
    private final String status;

    public AssignedPosition(
            String id,
            String positionTitle,
            String course,
            String department,
            int hours,
            String startDate,
            String endDate,
            String status) {
        this.id = id;
        this.positionTitle = positionTitle;
        this.course = course;
        this.department = department;
        this.hours = hours;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public String getId() { return id; }
    public String getPositionTitle() { return positionTitle; }
    public String getCourse() { return course; }
    public String getDepartment() { return department; }
    public int getHours() { return hours; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
}

