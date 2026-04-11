import java.util.List;

/**
 * 申请数据模型
 */
public class Application {
    private String applicationId;
    private String studentId;
    private String userId;
    private String jobId;
    private JobSnapshot jobSnapshot;
    private ApplicantSnapshot applicantSnapshot;
    private ApplicationForm applicationForm;
    private Attachments attachments;
    private Status status;
    private List<TimelineEvent> timeline;
    private Review review;
    private Meta meta;
    private boolean isDraft;

    // Getters and Setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public JobSnapshot getJobSnapshot() { return jobSnapshot; }
    public void setJobSnapshot(JobSnapshot jobSnapshot) { this.jobSnapshot = jobSnapshot; }

    public ApplicantSnapshot getApplicantSnapshot() { return applicantSnapshot; }
    public void setApplicantSnapshot(ApplicantSnapshot applicantSnapshot) { this.applicantSnapshot = applicantSnapshot; }

    public ApplicationForm getApplicationForm() { return applicationForm; }
    public void setApplicationForm(ApplicationForm applicationForm) { this.applicationForm = applicationForm; }

    public Attachments getAttachments() { return attachments; }
    public void setAttachments(Attachments attachments) { this.attachments = attachments; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<TimelineEvent> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineEvent> timeline) { this.timeline = timeline; }

    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }

    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }

    public boolean isDraft() { return isDraft; }
    public void setDraft(boolean draft) { isDraft = draft; }

    // Inner classes
    public static class JobSnapshot {
        private String title;
        private String courseCode;
        private String courseName;
        private String department;
        private String instructorName;
        private String instructorEmail;
        private String deadline;
        private String employmentType;
        private int weeklyHours;
        private String locationMode;
        private String locationDetail;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getInstructorName() { return instructorName; }
        public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
        public String getInstructorEmail() { return instructorEmail; }
        public void setInstructorEmail(String instructorEmail) { this.instructorEmail = instructorEmail; }
        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
        public String getEmploymentType() { return employmentType; }
        public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
        public int getWeeklyHours() { return weeklyHours; }
        public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }
        public String getLocationMode() { return locationMode; }
        public void setLocationMode(String locationMode) { this.locationMode = locationMode; }
        public String getLocationDetail() { return locationDetail; }
        public void setLocationDetail(String locationDetail) { this.locationDetail = locationDetail; }

        public String getCourseNameWithCode() {
            return courseCode + " - " + courseName;
        }
    }

    public static class ApplicantSnapshot {
        private String fullName;
        private String studentId;
        private String email;
        private String phoneNumber;
        private String programMajor;
        private String year;
        private double gpa;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getProgramMajor() { return programMajor; }
        public void setProgramMajor(String programMajor) { this.programMajor = programMajor; }
        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
        public double getGpa() { return gpa; }
        public void setGpa(double gpa) { this.gpa = gpa; }
    }

    public static class ApplicationForm {
        private List<String> relevantSkills;
        private String relevantExperience;
        private String availability;
        private String motivationCoverLetter;

        public List<String> getRelevantSkills() { return relevantSkills; }
        public void setRelevantSkills(List<String> relevantSkills) { this.relevantSkills = relevantSkills; }
        public String getRelevantExperience() { return relevantExperience; }
        public void setRelevantExperience(String relevantExperience) { this.relevantExperience = relevantExperience; }
        public String getAvailability() { return availability; }
        public void setAvailability(String availability) { this.availability = availability; }
        public String getMotivationCoverLetter() { return motivationCoverLetter; }
        public void setMotivationCoverLetter(String motivationCoverLetter) { this.motivationCoverLetter = motivationCoverLetter; }
    }

    public static class Attachments {
        private CVInfo cv;
        private List<Document> supportingDocuments;

        public CVInfo getCv() { return cv; }
        public void setCv(CVInfo cv) { this.cv = cv; }
        public List<Document> getSupportingDocuments() { return supportingDocuments; }
        public void setSupportingDocuments(List<Document> supportingDocuments) { this.supportingDocuments = supportingDocuments; }
    }

    public static class CVInfo {
        private String fileName;
        private String filePath;
        private String fileType;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
    }

    public static class Document {
        private String documentId;
        private String fileName;
        private String filePath;
        private String fileType;
        private String uploadedAt;

        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    public static class Status {
        private String current;
        private String label;
        private String color;
        private String lastUpdated;

        public String getCurrent() { return current; }
        public void setCurrent(String current) { this.current = current; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class TimelineEvent {
        private String timelineId;
        private String stepKey;
        private String stepLabel;
        private String status;
        private String timestamp;
        private String note;

        public String getTimelineId() { return timelineId; }
        public void setTimelineId(String timelineId) { this.timelineId = timelineId; }
        public String getStepKey() { return stepKey; }
        public void setStepKey(String stepKey) { this.stepKey = stepKey; }
        public String getStepLabel() { return stepLabel; }
        public void setStepLabel(String stepLabel) { this.stepLabel = stepLabel; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class Review {
        private String reviewerNotes;
        private String statusMessage;
        private String nextSteps;
        private String reviewedBy;
        private String reviewedAt;

        public String getReviewerNotes() { return reviewerNotes; }
        public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }
        public String getStatusMessage() { return statusMessage; }
        public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
        public String getNextSteps() { return nextSteps; }
        public void setNextSteps(String nextSteps) { this.nextSteps = nextSteps; }
        public String getReviewedBy() { return reviewedBy; }
        public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
        public String getReviewedAt() { return reviewedAt; }
        public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    }

    public static class Meta {
        private String submittedAt;
        private String updatedAt;
        private boolean isDeleted;

        public String getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public boolean isIsDeleted() { return isDeleted; }
        public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    }
}
