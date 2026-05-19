package TA_Job_Application_Module;
import java.util.List;


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
    private Dashboard dashboard;
    private Permissions permissions;
    private Meta meta;
    private int profileCompletion;
    /** When false, TA portal shows first-login onboarding; missing in JSON is treated as true (existing users). */
    private boolean onboardingCompleted = true;

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

    public Dashboard getDashboard() { return dashboard; }
    public void setDashboard(Dashboard dashboard) { this.dashboard = dashboard; }

    public Permissions getPermissions() { return permissions; }
    public void setPermissions(Permissions permissions) { this.permissions = permissions; }
    
    public int getProfileCompletion() { return profileCompletion; }
    public void setProfileCompletion(int profileCompletion) { this.profileCompletion = profileCompletion; }

    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }
    
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
        private List<CompletedCourse> completedCourses;

        public double getGpa() { return gpa; }
        public void setGpa(double gpa) { this.gpa = gpa; }
        public List<CompletedCourse> getCompletedCourses() { return completedCourses; }
        public void setCompletedCourses(List<CompletedCourse> completedCourses) { this.completedCourses = completedCourses; }
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
        private List<String> proficiencyLevels;
        private TaSkillPool taSkillPool;

        public List<String> getProficiencyLevels() { return proficiencyLevels; }
        public void setProficiencyLevels(List<String> proficiencyLevels) { this.proficiencyLevels = proficiencyLevels; }
        public TaSkillPool getTaSkillPool() { return taSkillPool; }
        public void setTaSkillPool(TaSkillPool taSkillPool) { this.taSkillPool = taSkillPool; }

        // Legacy setters for backward compatibility with old format
        public void setProgramming(List<Skill> programming) { /* ignore in new format */ }
        public void setTeaching(List<Skill> teaching) { /* ignore in new format */ }
        public void setCommunication(List<Skill> communication) { /* ignore in new format */ }
        public void setOther(List<Skill> other) { /* ignore in new format */ }

        // Helper method to get selected skills as a list of skill names (for backward compatibility)
        public List<Skill> getSelectedSkills() {
            List<Skill> selected = new java.util.ArrayList<>();
            if (taSkillPool != null) {
                if (taSkillPool.getTechnicalSkills() != null) {
                    TechnicalSkillPool tech = taSkillPool.getTechnicalSkills();
                    addSelectedSkills(tech.getProgrammingAndSoftwareFundamentals(), selected);
                    addSelectedSkills(tech.getHardwareAndLogicDesign(), selected);
                    addSelectedSkills(tech.getEmbeddedSystemsAndLowLevelDevelopment(), selected);
                }
                if (taSkillPool.getEngineeringAndTools() != null && taSkillPool.getEngineeringAndTools().getProfessionalDevelopmentAndSimulationTools() != null) {
                    addSelectedSkills(taSkillPool.getEngineeringAndTools().getProfessionalDevelopmentAndSimulationTools(), selected);
                }
                if (taSkillPool.getLanguageAndCommunication() != null && taSkillPool.getLanguageAndCommunication().getCrossCulturalCommunication() != null) {
                    addSelectedSkills(taSkillPool.getLanguageAndCommunication().getCrossCulturalCommunication(), selected);
                }
            }
            return selected;
        }

        private void addSelectedSkills(List<SkillItem> items, List<Skill> result) {
            if (items != null) {
                for (SkillItem item : items) {
                    if (item.isSelected()) {
                        Skill s = new Skill();
                        s.setName(item.getName());
                        s.setProficiency(item.getProficiency());
                        result.add(s);
                    }
                }
            }
        }

        // Legacy methods for backward compatibility
        public List<Skill> getProgramming() {
            List<Skill> list = new java.util.ArrayList<>();
            if (taSkillPool != null && taSkillPool.getTechnicalSkills() != null) {
                addSelectedSkills(taSkillPool.getTechnicalSkills().getProgrammingAndSoftwareFundamentals(), list);
            }
            return list;
        }

        public List<Skill> getTeaching() { return new java.util.ArrayList<>(); }
        public List<Skill> getCommunication() {
            List<Skill> list = new java.util.ArrayList<>();
            if (taSkillPool != null && taSkillPool.getLanguageAndCommunication() != null) {
                addSelectedSkills(taSkillPool.getLanguageAndCommunication().getCrossCulturalCommunication(), list);
            }
            return list;
        }
        public List<Skill> getOther() {
            List<Skill> list = new java.util.ArrayList<>();
            if (taSkillPool != null && taSkillPool.getEngineeringAndTools() != null) {
                addSelectedSkills(taSkillPool.getEngineeringAndTools().getProfessionalDevelopmentAndSimulationTools(), list);
            }
            return list;
        }
    }

    public static class TaSkillPool {
        private TechnicalSkillPool technicalSkills;
        private EngineeringToolPool engineeringAndTools;
        private LanguagePool languageAndCommunication;

        public TechnicalSkillPool getTechnicalSkills() { return technicalSkills; }
        public void setTechnicalSkills(TechnicalSkillPool technicalSkills) { this.technicalSkills = technicalSkills; }
        public EngineeringToolPool getEngineeringAndTools() { return engineeringAndTools; }
        public void setEngineeringAndTools(EngineeringToolPool engineeringAndTools) { this.engineeringAndTools = engineeringAndTools; }
        public LanguagePool getLanguageAndCommunication() { return languageAndCommunication; }
        public void setLanguageAndCommunication(LanguagePool languageAndCommunication) { this.languageAndCommunication = languageAndCommunication; }
    }

    public static class TechnicalSkillPool {
        private List<SkillItem> programmingAndSoftwareFundamentals;
        private List<SkillItem> hardwareAndLogicDesign;
        private List<SkillItem> embeddedSystemsAndLowLevelDevelopment;

        public List<SkillItem> getProgrammingAndSoftwareFundamentals() { return programmingAndSoftwareFundamentals; }
        public void setProgrammingAndSoftwareFundamentals(List<SkillItem> programmingAndSoftwareFundamentals) { this.programmingAndSoftwareFundamentals = programmingAndSoftwareFundamentals; }
        public List<SkillItem> getHardwareAndLogicDesign() { return hardwareAndLogicDesign; }
        public void setHardwareAndLogicDesign(List<SkillItem> hardwareAndLogicDesign) { this.hardwareAndLogicDesign = hardwareAndLogicDesign; }
        public List<SkillItem> getEmbeddedSystemsAndLowLevelDevelopment() { return embeddedSystemsAndLowLevelDevelopment; }
        public void setEmbeddedSystemsAndLowLevelDevelopment(List<SkillItem> embeddedSystemsAndLowLevelDevelopment) { this.embeddedSystemsAndLowLevelDevelopment = embeddedSystemsAndLowLevelDevelopment; }
    }

    public static class EngineeringToolPool {
        private List<SkillItem> professionalDevelopmentAndSimulationTools;

        public List<SkillItem> getProfessionalDevelopmentAndSimulationTools() { return professionalDevelopmentAndSimulationTools; }
        public void setProfessionalDevelopmentAndSimulationTools(List<SkillItem> professionalDevelopmentAndSimulationTools) { this.professionalDevelopmentAndSimulationTools = professionalDevelopmentAndSimulationTools; }
    }

    public static class LanguagePool {
        private List<SkillItem> crossCulturalCommunication;

        public List<SkillItem> getCrossCulturalCommunication() { return crossCulturalCommunication; }
        public void setCrossCulturalCommunication(List<SkillItem> crossCulturalCommunication) { this.crossCulturalCommunication = crossCulturalCommunication; }
    }

    public static class SkillItem {
        private String name;
        private boolean selected;
        private String proficiency;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
        public String getProficiency() { return proficiency; }
        public void setProficiency(String proficiency) { this.proficiency = proficiency; }
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
        private String storedFileName;
        private String filePath;
        private String fileType;
        private int fileSizeKB;
        private String uploadedAt;

        public boolean isUploaded() { return uploaded; }
        public void setUploaded(boolean uploaded) { this.uploaded = uploaded; }
        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
        public String getStoredFileName() { return storedFileName; }
        public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public int getFileSizeKB() { return fileSizeKB; }
        public void setFileSizeKB(int fileSizeKB) { this.fileSizeKB = fileSizeKB; }
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

    public static class Dashboard {
        private int profileCompletion;

        public int getProfileCompletion() { return profileCompletion; }
        public void setProfileCompletion(int profileCompletion) { this.profileCompletion = profileCompletion; }
    }

    public static class Permissions {
        private boolean canEditOwnProfile;
        private boolean canUploadCV;
        private boolean canBrowseJobs;
        private boolean canApplyJob;
        private boolean canViewOwnApplications;
        private boolean canReviewApplication;
        private boolean canManageJob;
        private boolean canManageUsers;

        public boolean isCanEditOwnProfile() { return canEditOwnProfile; }
        public void setCanEditOwnProfile(boolean canEditOwnProfile) { this.canEditOwnProfile = canEditOwnProfile; }
        public boolean isCanUploadCV() { return canUploadCV; }
        public void setCanUploadCV(boolean canUploadCV) { this.canUploadCV = canUploadCV; }
        public boolean isCanBrowseJobs() { return canBrowseJobs; }
        public void setCanBrowseJobs(boolean canBrowseJobs) { this.canBrowseJobs = canBrowseJobs; }
        public boolean isCanApplyJob() { return canApplyJob; }
        public void setCanApplyJob(boolean canApplyJob) { this.canApplyJob = canApplyJob; }
        public boolean isCanViewOwnApplications() { return canViewOwnApplications; }
        public void setCanViewOwnApplications(boolean canViewOwnApplications) { this.canViewOwnApplications = canViewOwnApplications; }
        public boolean isCanReviewApplication() { return canReviewApplication; }
        public void setCanReviewApplication(boolean canReviewApplication) { this.canReviewApplication = canReviewApplication; }
        public boolean isCanManageJob() { return canManageJob; }
        public void setCanManageJob(boolean canManageJob) { this.canManageJob = canManageJob; }
        public boolean isCanManageUsers() { return canManageUsers; }
        public void setCanManageUsers(boolean canManageUsers) { this.canManageUsers = canManageUsers; }
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
