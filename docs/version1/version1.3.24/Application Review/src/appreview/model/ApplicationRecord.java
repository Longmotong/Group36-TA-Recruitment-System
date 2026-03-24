package appreview.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application aggregate model for review workflow.
 */
public class ApplicationRecord {
    public String applicationId;
    public String userId;
    public String studentId;
    public String jobId;
    public String taName;
    public String taEmail;
    public String taPhone;
    public String major;
    public String year;
    public double gpa;
    public String courseCode;
    public String courseName;
    public String applicationDate;
    public int weeklyHours;
    public List<String> relevantSkills = new ArrayList<String>();
    public String experience;
    public String cvPath;
    public String statusCurrent;
    public String statusLabel;
    public String reviewDecision;
    public String reviewNotes;
    public String reviewedBy;
    public String reviewedAt;
    public Map<String, Object> raw;
}
