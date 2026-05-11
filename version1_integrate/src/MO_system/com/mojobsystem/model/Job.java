package com.mojobsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Job model representing a TA recruitment position.
 * Merged from version1.1 and version1.3.26.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {
    private String id;
    private String title;
    private String moduleCode;
    private String moduleName;
    private int quota;
    private int weeklyHours;
    private String status;
    private String description;
    private String additionalRequirements;
    private List<String> requiredSkills;
    private int applicantsCount;

    private String department;
    private String instructorName;
    private String instructorEmail;
    private String deadline;
    private String locationMode;
    private String employmentType;
    private String courseTerm;
    private int courseYear;

    public Job() {
        this.id = UUID.randomUUID().toString();
        this.status = "Open";
        this.additionalRequirements = "";
        this.requiredSkills = new ArrayList<>();
        this.applicantsCount = 0;
        this.department = "";
        this.instructorName = "";
        this.instructorEmail = "mo@university.edu";
        this.deadline = "";
        this.locationMode = "Hybrid";
        this.employmentType = "Part-time TA";
        this.courseTerm = "Spring";
        this.courseYear = 0;
    }

    public Job(String id, String title, String moduleCode, String moduleName,
               int quota, int weeklyHours, String status, String description,
               String additionalRequirements, List<String> requiredSkills,
               int applicantsCount) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.title = title;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.quota = quota;
        this.weeklyHours = weeklyHours;
        this.status = (status == null || status.isBlank()) ? "Open" : status;
        this.description = description;
        this.additionalRequirements = additionalRequirements == null ? "" : additionalRequirements;
        this.requiredSkills = (requiredSkills == null) ? new ArrayList<>() : requiredSkills;
        this.applicantsCount = applicantsCount;
        this.department = "";
        this.instructorName = "";
        this.instructorEmail = "mo@university.edu";
        this.deadline = "";
        this.locationMode = "Hybrid";
        this.employmentType = "Part-time TA";
        this.courseTerm = "Spring";
        this.courseYear = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public int getQuota() { return quota; }
    public void setQuota(int quota) { this.quota = quota; }

    public int getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdditionalRequirements() {
        return additionalRequirements == null ? "" : additionalRequirements;
    }
    public void setAdditionalRequirements(String additionalRequirements) {
        this.additionalRequirements = additionalRequirements;
    }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public int getApplicantsCount() { return applicantsCount; }
    public void setApplicantsCount(int applicantsCount) { this.applicantsCount = applicantsCount; }

    public String getDepartment() { return department == null ? "" : department; }
    public void setDepartment(String department) { this.department = department; }

    public String getInstructorName() { return instructorName == null ? "" : instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getInstructorEmail() { return instructorEmail == null ? "" : instructorEmail; }
    public void setInstructorEmail(String instructorEmail) { this.instructorEmail = instructorEmail; }

    public String getDeadline() { return deadline == null ? "" : deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getLocationMode() { return locationMode == null ? "" : locationMode; }
    public void setLocationMode(String locationMode) { this.locationMode = locationMode; }

    public String getEmploymentType() { return employmentType == null ? "" : employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getCourseTerm() { return courseTerm == null || courseTerm.isBlank() ? "Spring" : courseTerm; }
    public void setCourseTerm(String courseTerm) { this.courseTerm = courseTerm; }

    public int getCourseYear() { return courseYear; }
    public void setCourseYear(int courseYear) { this.courseYear = courseYear; }

    public String getDisplayStatus() {
        if (status == null || status.isBlank()) return "Open";
        String lower = status.trim().toLowerCase();
        return switch (lower) {
            case "closed" -> "Closed";
            case "draft" -> "Draft";
            default -> "Open";
        };
    }

    public boolean isOpen() {
        return !"Closed".equalsIgnoreCase(status) && !"Draft".equalsIgnoreCase(status);
    }
}
