package edu.ebu6304.standalone.model;

import java.util.List;

public class ApplicationItem {
    private String applicationId;
    private String userId;
    private String studentId;
    private String jobId;
    private JobSnapshot jobSnapshot;
    private ApplicantSnapshot applicantSnapshot;
    private ApplicationForm applicationForm;
    private Status status;
    private Review review;
    private Meta meta;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobSnapshot getJobSnapshot() {
        return jobSnapshot;
    }

    public void setJobSnapshot(JobSnapshot jobSnapshot) {
        this.jobSnapshot = jobSnapshot;
    }

    public ApplicantSnapshot getApplicantSnapshot() {
        return applicantSnapshot;
    }

    public void setApplicantSnapshot(ApplicantSnapshot applicantSnapshot) {
        this.applicantSnapshot = applicantSnapshot;
    }

    public ApplicationForm getApplicationForm() {
        return applicationForm;
    }

    public void setApplicationForm(ApplicationForm applicationForm) {
        this.applicationForm = applicationForm;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public static class JobSnapshot {
        private String title;
        private String courseCode;
        private String courseName;
        private String department;
        private String instructorName;
        private String instructorEmail;
        private Integer weeklyHours;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getInstructorName() {
            return instructorName;
        }

        public void setInstructorName(String instructorName) {
            this.instructorName = instructorName;
        }

        public String getInstructorEmail() {
            return instructorEmail;
        }

        public void setInstructorEmail(String instructorEmail) {
            this.instructorEmail = instructorEmail;
        }

        public Integer getWeeklyHours() {
            return weeklyHours;
        }

        public void setWeeklyHours(Integer weeklyHours) {
            this.weeklyHours = weeklyHours;
        }
    }

    public static class ApplicantSnapshot {
        private String fullName;
        private String studentId;
        private String email;
        private String phoneNumber;
        private String programMajor;
        private String year;
        private String department;
        private Double gpa;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getProgramMajor() {
            return programMajor;
        }

        public void setProgramMajor(String programMajor) {
            this.programMajor = programMajor;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public Double getGpa() {
            return gpa;
        }

        public void setGpa(Double gpa) {
            this.gpa = gpa;
        }
    }

    public static class ApplicationForm {
        private List<String> relevantSkills;
        private String relevantExperience;
        private String availability;
        private String motivationCoverLetter;

        public List<String> getRelevantSkills() {
            return relevantSkills;
        }

        public void setRelevantSkills(List<String> relevantSkills) {
            this.relevantSkills = relevantSkills;
        }

        public String getRelevantExperience() {
            return relevantExperience;
        }

        public void setRelevantExperience(String relevantExperience) {
            this.relevantExperience = relevantExperience;
        }

        public String getAvailability() {
            return availability;
        }

        public void setAvailability(String availability) {
            this.availability = availability;
        }

        public String getMotivationCoverLetter() {
            return motivationCoverLetter;
        }

        public void setMotivationCoverLetter(String motivationCoverLetter) {
            this.motivationCoverLetter = motivationCoverLetter;
        }
    }

    public static class Status {
        private String current;
        private String label;

        public String getCurrent() {
            return current;
        }

        public void setCurrent(String current) {
            this.current = current;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public static class Review {
        private String reviewerNotes;
        private String decision;
        private String decisionReason;
        private String reviewedBy;
        private String reviewedAt;

        public String getReviewerNotes() {
            return reviewerNotes;
        }

        public void setReviewerNotes(String reviewerNotes) {
            this.reviewerNotes = reviewerNotes;
        }

        public String getDecision() {
            return decision;
        }

        public void setDecision(String decision) {
            this.decision = decision;
        }

        public String getDecisionReason() {
            return decisionReason;
        }

        public void setDecisionReason(String decisionReason) {
            this.decisionReason = decisionReason;
        }

        public String getReviewedBy() {
            return reviewedBy;
        }

        public void setReviewedBy(String reviewedBy) {
            this.reviewedBy = reviewedBy;
        }

        public String getReviewedAt() {
            return reviewedAt;
        }

        public void setReviewedAt(String reviewedAt) {
            this.reviewedAt = reviewedAt;
        }
    }

    public static class Meta {
        private String submittedAt;
        private String updatedAt;

        public String getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(String submittedAt) {
            this.submittedAt = submittedAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
