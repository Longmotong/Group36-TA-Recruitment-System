/**
 * TA用户数据模型
 */
public class TAUser {
    private String userId;
    private String loginId;
    private String role;
    private Account account;
    private Profile profile;
    private Academic academic;
    private Skills skills;
    private CV cv;
    private ApplicationSummary applicationSummary;
    private int profileCompletion;
    private Meta meta;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }
    
    public Academic getAcademic() { return academic; }
    public void setAcademic(Academic academic) { this.academic = academic; }
    
    public Skills getSkills() { return skills; }
    public void setSkills(Skills skills) { this.skills = skills; }
    
    public CV getCv() { return cv; }
    public void setCv(CV cv) { this.cv = cv; }
    
    public ApplicationSummary getApplicationSummary() { return applicationSummary; }
    public void setApplicationSummary(ApplicationSummary applicationSummary) { this.applicationSummary = applicationSummary; }
    
    public int getProfileCompletion() { return profileCompletion; }
    public void setProfileCompletion(int profileCompletion) { this.profileCompletion = profileCompletion; }
    
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }

    // Inner classes
    public static class Account {
        private String username;
        private String email;
        private String status;
        private String lastLoginAt;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLastLoginAt() { return lastLoginAt; }
        public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    }

    public static class Profile {
        private String fullName;
        private String studentId;
        private String year;
        private String programMajor;
        private String department;
        private String phoneNumber;
        private String address;
        private String shortBio;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
        public String getProgramMajor() { return programMajor; }
        public void setProgramMajor(String programMajor) { this.programMajor = programMajor; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getShortBio() { return shortBio; }
        public void setShortBio(String shortBio) { this.shortBio = shortBio; }
    }

    public static class Academic {
        private double gpa;
        private java.util.List<CompletedCourse> completedCourses;

        public double getGpa() { return gpa; }
        public void setGpa(double gpa) { this.gpa = gpa; }
        public java.util.List<CompletedCourse> getCompletedCourses() { return completedCourses; }
        public void setCompletedCourses(java.util.List<CompletedCourse> completedCourses) { this.completedCourses = completedCourses; }
    }

    public static class CompletedCourse {
        private String courseCode;
        private String courseName;
        private String grade;

        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
    }

    public static class Skills {
        private java.util.List<Skill> programming;
        private java.util.List<Skill> teaching;
        private java.util.List<Skill> communication;
        private java.util.List<Skill> other;

        public java.util.List<Skill> getProgramming() { return programming; }
        public void setProgramming(java.util.List<Skill> programming) { this.programming = programming; }
        public java.util.List<Skill> getTeaching() { return teaching; }
        public void setTeaching(java.util.List<Skill> teaching) { this.teaching = teaching; }
        public java.util.List<Skill> getCommunication() { return communication; }
        public void setCommunication(java.util.List<Skill> communication) { this.communication = communication; }
        public java.util.List<Skill> getOther() { return other; }
        public void setOther(java.util.List<Skill> other) { this.other = other; }
    }

    public static class Skill {
        private String skillId;
        private String name;
        private String proficiency;

        public String getSkillId() { return skillId; }
        public void setSkillId(String skillId) { this.skillId = skillId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getProficiency() { return proficiency; }
        public void setProficiency(String proficiency) { this.proficiency = proficiency; }
    }

    public static class CV {
        private boolean uploaded;
        private String originalFileName;
        private String filePath;
        private String uploadedAt;

        public boolean isUploaded() { return uploaded; }
        public void setUploaded(boolean uploaded) { this.uploaded = uploaded; }
        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    public static class ApplicationSummary {
        private int totalApplications;
        private int pending;
        private int underReview;
        private int accepted;
        private int rejected;

        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        public int getUnderReview() { return underReview; }
        public void setUnderReview(int underReview) { this.underReview = underReview; }
        public int getAccepted() { return accepted; }
        public void setAccepted(int accepted) { this.accepted = accepted; }
        public int getRejected() { return rejected; }
        public void setRejected(int rejected) { this.rejected = rejected; }
    }

    public static class Meta {
        private String createdAt;
        private String updatedAt;
        private boolean isDeleted;
        private boolean isActive;

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public boolean isIsDeleted() { return isDeleted; }
        public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
        public boolean isIsActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
    }
}
