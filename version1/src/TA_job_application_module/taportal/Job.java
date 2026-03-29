package taportal;

import java.util.List;

/**
 * 职位数据模型
 */
public class Job {
    private String jobId;
    private String title;
    private Course course;
    private String department;
    private Instructor instructor;
    private Employment employment;
    private Dates dates;
    private Content content;
    private Publication publication;
    private Lifecycle lifecycle;
    private Stats stats;
    private Meta meta;

    // Simplified summary fields for index
    private String courseCode;
    private String weeklyHours;
    private String deadline;
    private String locationMode;
    private String employmentType;
    private String status;

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Instructor getInstructor() { return instructor; }
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    public Employment getEmployment() { return employment; }
    public void setEmployment(Employment employment) { this.employment = employment; }

    public Dates getDates() { return dates; }
    public void setDates(Dates dates) { this.dates = dates; }

    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }

    public Publication getPublication() { return publication; }
    public void setPublication(Publication publication) { this.publication = publication; }

    public Lifecycle getLifecycle() { return lifecycle; }
    public void setLifecycle(Lifecycle lifecycle) { this.lifecycle = lifecycle; }

    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }

    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }

    // Simplified getters for convenience
    public String getCourseCode() {
        if (course != null) return course.getCourseCode();
        return courseCode;
    }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getWeeklyHoursDisplay() {
        if (employment != null) return employment.getWeeklyHours() + " hours/week";
        return weeklyHours != null ? weeklyHours : "N/A";
    }

    public String getDeadlineDisplay() {
        if (dates != null && dates.getDeadline() != null) {
            return dates.getDeadline().substring(0, 10);
        }
        return deadline != null ? deadline : "N/A";
    }

    public String getLocationMode() {
        if (employment != null) return employment.getLocationMode();
        return locationMode;
    }

    public String getEmploymentType() {
        if (employment != null) return employment.getEmploymentType();
        return employmentType;
    }

    public String getStatus() {
        if (lifecycle != null) return lifecycle.getStatus();
        return status;
    }

    public String getInstructorName() {
        if (instructor != null) return instructor.getName();
        return "";
    }

    public String getInstructorEmail() {
        if (instructor != null) return instructor.getEmail();
        return "";
    }

    public List<String> getResponsibilities() {
        if (content != null && content.getResponsibilities() != null) {
            return content.getResponsibilities();
        }
        return List.of();
    }

    public List<String> getRequirements() {
        if (content != null && content.getRequirements() != null) {
            return content.getRequirements();
        }
        return List.of();
    }

    public List<String> getPreferredSkills() {
        if (content != null && content.getPreferredSkills() != null) {
            return content.getPreferredSkills();
        }
        return List.of();
    }

    public String getDescription() {
        if (content != null) return content.getDescription();
        return "";
    }

    public String getSummary() {
        if (content != null) return content.getSummary();
        return "";
    }

    // Inner classes
    public static class Course {
        private String courseCode;
        private String courseName;
        private String term;
        private int year;

        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public String getCourseNameWithCode() {
            return courseCode + " - " + courseName;
        }
    }

    public static class Instructor {
        private String name;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class Employment {
        private String jobType;
        private String employmentType;
        private int weeklyHours;
        private String locationMode;
        private String locationDetail;

        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }
        public String getEmploymentType() { return employmentType; }
        public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
        public int getWeeklyHours() { return weeklyHours; }
        public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }
        public String getLocationMode() { return locationMode; }
        public void setLocationMode(String locationMode) { this.locationMode = locationMode; }
        public String getLocationDetail() { return locationDetail; }
        public void setLocationDetail(String locationDetail) { this.locationDetail = locationDetail; }
    }

    public static class Dates {
        private String postedAt;
        private String deadline;
        private String startDate;
        private String endDate;

        public String getPostedAt() { return postedAt; }
        public void setPostedAt(String postedAt) { this.postedAt = postedAt; }
        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }

    public static class Content {
        private String summary;
        private String description;
        private List<String> responsibilities;
        private List<String> requirements;
        private List<String> preferredSkills;

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getResponsibilities() { return responsibilities; }
        public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities; }
        public List<String> getRequirements() { return requirements; }
        public void setRequirements(List<String> requirements) { this.requirements = requirements; }
        public List<String> getPreferredSkills() { return preferredSkills; }
        public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }
    }

    public static class Publication {
        private String status;
        private String publishedAt;
        private String publishedBy;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPublishedAt() { return publishedAt; }
        public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
        public String getPublishedBy() { return publishedBy; }
        public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }
    }

    public static class Lifecycle {
        private String status;
        private boolean isDeleted;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isIsDeleted() { return isDeleted; }
        public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    }

    public static class Stats {
        private int applicationCount;
        private int pendingCount;
        private int acceptedCount;
        private int rejectedCount;

        public int getApplicationCount() { return applicationCount; }
        public void setApplicationCount(int applicationCount) { this.applicationCount = applicationCount; }
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        public int getAcceptedCount() { return acceptedCount; }
        public void setAcceptedCount(int acceptedCount) { this.acceptedCount = acceptedCount; }
        public int getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }
    }

    public static class Meta {
        private String createdAt;
        private String updatedAt;

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
}
