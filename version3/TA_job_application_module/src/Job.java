package TA_Job_Application_Module;
import java.util.List;
import java.util.Map;


public class Job {
    private String jobId;
    private String title;
    private Course course;
    private String department;
    private Instructor instructor;
    private Employment employment;
    private Dates dates;
    private Content content;
    private Ownership ownership;
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

    public Ownership getOwnership() { return ownership; }
    public void setOwnership(Ownership ownership) { this.ownership = ownership; }

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

    public TaSkillRequirement getTaSkillRequirement() {
        if (content != null) return content.getTaSkillRequirement();
        return null;
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
        private TaSkillRequirement taSkillRequirement;

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
        public TaSkillRequirement getTaSkillRequirement() { return taSkillRequirement; }
        public void setTaSkillRequirement(TaSkillRequirement taSkillRequirement) { this.taSkillRequirement = taSkillRequirement; }
    }

    public static class Ownership {
        private String createdBy;
        private List<String> managedBy;
        private String lastEditedBy;

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public List<String> getManagedBy() { return managedBy; }
        public void setManagedBy(List<String> managedBy) { this.managedBy = managedBy; }
        public String getLastEditedBy() { return lastEditedBy; }
        public void setLastEditedBy(String lastEditedBy) { this.lastEditedBy = lastEditedBy; }
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
        private String deletedAt;
        private String deletedBy;
        private String closeReason;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isIsDeleted() { return isDeleted; }
        public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
        public String getDeletedAt() { return deletedAt; }
        public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
        public String getDeletedBy() { return deletedBy; }
        public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
        public String getCloseReason() { return closeReason; }
        public void setCloseReason(String closeReason) { this.closeReason = closeReason; }
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

    // TA Skill Requirement Classes
    public static class TaSkillRequirement {
        private List<String> proficiencyLevels;
        private SkillCatalog skillCatalog;
        private List<RequiredSkill> requiredSkills;
        private List<String> customRequiredSkills;
        private List<MoSelectedSkill> moSelectedSkills;
        private MoSkillSelectionPool moSkillSelectionPool;

        public List<String> getProficiencyLevels() { return proficiencyLevels; }
        public void setProficiencyLevels(List<String> proficiencyLevels) { this.proficiencyLevels = proficiencyLevels; }
        public SkillCatalog getSkillCatalog() { return skillCatalog; }
        public void setSkillCatalog(SkillCatalog skillCatalog) { this.skillCatalog = skillCatalog; }
        public List<RequiredSkill> getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(List<RequiredSkill> requiredSkills) { this.requiredSkills = requiredSkills; }
        public List<String> getCustomRequiredSkills() { return customRequiredSkills; }
        public void setCustomRequiredSkills(List<String> customRequiredSkills) { this.customRequiredSkills = customRequiredSkills; }
        public List<MoSelectedSkill> getMoSelectedSkills() { return moSelectedSkills; }
        public void setMoSelectedSkills(List<MoSelectedSkill> moSelectedSkills) { this.moSelectedSkills = moSelectedSkills; }
        public MoSkillSelectionPool getMoSkillSelectionPool() { return moSkillSelectionPool; }
        public void setMoSkillSelectionPool(MoSkillSelectionPool moSkillSelectionPool) { this.moSkillSelectionPool = moSkillSelectionPool; }
    }

    public static class RequiredSkill {
        private String name;
        private String minimumProficiency;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMinimumProficiency() { return minimumProficiency; }
        public void setMinimumProficiency(String minimumProficiency) { this.minimumProficiency = minimumProficiency; }
    }

    public static class MoSelectedSkill {
        private String category;
        private String subcategory;
        private String name;
        private String minimumProficiency;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSubcategory() { return subcategory; }
        public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMinimumProficiency() { return minimumProficiency; }
        public void setMinimumProficiency(String minimumProficiency) { this.minimumProficiency = minimumProficiency; }
    }

    public static class SkillCatalog {
        private TechnicalSkills technicalSkills;
        private EngineeringAndTools engineeringAndTools;
        private LanguageAndCommunication languageAndCommunication;

        public TechnicalSkills getTechnicalSkills() { return technicalSkills; }
        public void setTechnicalSkills(TechnicalSkills technicalSkills) { this.technicalSkills = technicalSkills; }
        public EngineeringAndTools getEngineeringAndTools() { return engineeringAndTools; }
        public void setEngineeringAndTools(EngineeringAndTools engineeringAndTools) { this.engineeringAndTools = engineeringAndTools; }
        public LanguageAndCommunication getLanguageAndCommunication() { return languageAndCommunication; }
        public void setLanguageAndCommunication(LanguageAndCommunication languageAndCommunication) { this.languageAndCommunication = languageAndCommunication; }
    }

    public static class TechnicalSkills {
        private List<String> programmingAndSoftwareFundamentals;
        private List<String> hardwareAndLogicDesign;
        private List<String> embeddedSystemsAndLowLevelDevelopment;

        public List<String> getProgrammingAndSoftwareFundamentals() { return programmingAndSoftwareFundamentals; }
        public void setProgrammingAndSoftwareFundamentals(List<String> programmingAndSoftwareFundamentals) { this.programmingAndSoftwareFundamentals = programmingAndSoftwareFundamentals; }
        public List<String> getHardwareAndLogicDesign() { return hardwareAndLogicDesign; }
        public void setHardwareAndLogicDesign(List<String> hardwareAndLogicDesign) { this.hardwareAndLogicDesign = hardwareAndLogicDesign; }
        public List<String> getEmbeddedSystemsAndLowLevelDevelopment() { return embeddedSystemsAndLowLevelDevelopment; }
        public void setEmbeddedSystemsAndLowLevelDevelopment(List<String> embeddedSystemsAndLowLevelDevelopment) { this.embeddedSystemsAndLowLevelDevelopment = embeddedSystemsAndLowLevelDevelopment; }
    }

    public static class EngineeringAndTools {
        private List<String> professionalDevelopmentAndSimulationTools;

        public List<String> getProfessionalDevelopmentAndSimulationTools() { return professionalDevelopmentAndSimulationTools; }
        public void setProfessionalDevelopmentAndSimulationTools(List<String> professionalDevelopmentAndSimulationTools) { this.professionalDevelopmentAndSimulationTools = professionalDevelopmentAndSimulationTools; }
    }

    public static class LanguageAndCommunication {
        private List<String> crossCulturalCommunication;

        public List<String> getCrossCulturalCommunication() { return crossCulturalCommunication; }
        public void setCrossCulturalCommunication(List<String> crossCulturalCommunication) { this.crossCulturalCommunication = crossCulturalCommunication; }
    }

    public static class MoSkillSelectionPool {
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
        private String minimumProficiency;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
        public String getMinimumProficiency() { return minimumProficiency; }
        public void setMinimumProficiency(String minimumProficiency) { this.minimumProficiency = minimumProficiency; }
    }
}
