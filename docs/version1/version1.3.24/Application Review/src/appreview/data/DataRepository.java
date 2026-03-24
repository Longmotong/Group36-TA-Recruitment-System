package appreview.data;

import appreview.model.ApplicationRecord;
import appreview.model.JobRecord;
import appreview.model.MoProfile;
import appreview.model.ReviewRecord;
import appreview.model.TaProfile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository for file-based data loading and writing.
 */
public class DataRepository {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final Path dataDir;

    /**
     * Build repository with data folder path.
     *
     * @param dataDirPath data directory path
     */
    public DataRepository(String dataDirPath) {
        this.dataDir = Paths.get(dataDirPath);
    }

    /**
     * Load all TA applications.
     *
     * @return list
     * @throws IOException io failure
     */
    public List<ApplicationRecord> loadApplications() throws IOException {
        List<ApplicationRecord> result = new ArrayList<ApplicationRecord>();
        Path dir = dataDir.resolve("applications");
        if (!Files.exists(dir)) {
            return result;
        }
        Files.list(dir).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
            try {
                Map<String, Object> json = asMap(SimpleJson.parse(readString(path)));
                ApplicationRecord a = new ApplicationRecord();
                a.raw = json;
                a.applicationId = str(json.get("applicationId"));
                a.userId = str(json.get("userId"));
                a.studentId = str(json.get("studentId"));
                a.jobId = str(json.get("jobId"));
                Map<String, Object> jobSnapshot = asMap(json.get("jobSnapshot"));
                a.courseCode = str(jobSnapshot.get("courseCode"));
                a.courseName = str(jobSnapshot.get("courseName"));
                a.weeklyHours = intVal(jobSnapshot.get("weeklyHours"));
                Map<String, Object> applicant = asMap(json.get("applicantSnapshot"));
                a.taName = str(applicant.get("fullName"));
                a.taEmail = str(applicant.get("email"));
                a.taPhone = str(applicant.get("phoneNumber"));
                a.major = str(applicant.get("programMajor"));
                a.year = str(applicant.get("year"));
                a.gpa = doubleVal(applicant.get("gpa"));
                Map<String, Object> form = asMap(json.get("applicationForm"));
                a.experience = str(form.get("relevantExperience"));
                a.relevantSkills = toStringList(form.get("relevantSkills"));
                Map<String, Object> attach = asMap(json.get("attachments"));
                a.cvPath = str(asMap(attach.get("cv")).get("filePath"));
                Map<String, Object> status = asMap(json.get("status"));
                a.statusCurrent = normalizeStatus(str(status.get("current")));
                a.statusLabel = statusLabel(a.statusCurrent);
                a.applicationDate = str(asMap(json.get("meta")).get("submittedAt"));
                Map<String, Object> review = asMap(json.get("review"));
                a.reviewDecision = normalizeStatus(str(review.get("decision")));
                a.reviewNotes = str(review.get("decisionReason"));
                a.reviewedBy = str(review.get("reviewedBy"));
                a.reviewedAt = str(review.get("reviewedAt"));
                result.add(a);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load application file: " + path + ", " + ex.getMessage(), ex);
            }
        });
        return result;
    }

    /**
     * Load all jobs.
     *
     * @return jobs
     * @throws IOException io failure
     */
    public List<JobRecord> loadJobs() throws IOException {
        List<JobRecord> jobs = new ArrayList<JobRecord>();
        Path dir = dataDir.resolve("jobs");
        if (!Files.exists(dir)) {
            return jobs;
        }
        Files.list(dir).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
            try {
                Map<String, Object> json = asMap(SimpleJson.parse(readString(path)));
                JobRecord j = new JobRecord();
                j.raw = json;
                j.jobId = str(json.get("jobId"));
                j.title = str(json.get("title"));
                Map<String, Object> course = asMap(json.get("course"));
                j.courseCode = str(course.get("courseCode"));
                j.courseName = str(course.get("courseName"));
                j.department = str(json.get("department"));
                Map<String, Object> employment = asMap(json.get("employment"));
                j.weeklyHours = intVal(employment.get("weeklyHours"));
                j.lifecycleStatus = str(asMap(json.get("lifecycle")).get("status"));
                j.publicationStatus = str(asMap(json.get("publication")).get("status"));
                Map<String, Object> content = asMap(json.get("content"));
                j.preferredSkills = toStringList(content.get("preferredSkills"));
                j.responsibilities = toStringList(content.get("responsibilities"));
                j.requirements = toStringList(content.get("requirements"));
                jobs.add(j);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load job file: " + path + ", " + ex.getMessage(), ex);
            }
        });
        return jobs;
    }

    /**
     * Load TA users.
     *
     * @return ta profile list
     * @throws IOException io failure
     */
    public List<TaProfile> loadTaProfiles() throws IOException {
        List<TaProfile> users = new ArrayList<TaProfile>();
        Path dir = dataDir.resolve("users").resolve("ta");
        if (!Files.exists(dir)) {
            return users;
        }
        Files.list(dir).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
            try {
                Map<String, Object> json = asMap(SimpleJson.parse(readString(path)));
                TaProfile t = new TaProfile();
                t.raw = json;
                t.userId = str(json.get("userId"));
                Map<String, Object> profile = asMap(json.get("profile"));
                t.studentId = str(profile.get("studentId"));
                t.fullName = str(profile.get("fullName"));
                t.major = str(profile.get("programMajor"));
                t.year = str(profile.get("year"));
                t.department = str(profile.get("department"));
                t.phone = str(profile.get("phoneNumber"));
                t.email = str(asMap(json.get("account")).get("email"));
                t.gpa = doubleVal(asMap(json.get("academic")).get("gpa"));
                t.cvPath = str(asMap(json.get("cv")).get("filePath"));
                t.skills = readSkillNames(asMap(json.get("skills")));
                t.experienceSummary = str(profile.get("shortBio"));
                users.add(t);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load user file: " + path + ", " + ex.getMessage(), ex);
            }
        });
        return users;
    }

    /**
     * Load MO users.
     *
     * @return mo profile list
     * @throws IOException io failure
     */
    public List<MoProfile> loadMoProfiles() throws IOException {
        List<MoProfile> users = new ArrayList<MoProfile>();
        Path dir = dataDir.resolve("users").resolve("mo");
        if (!Files.exists(dir)) {
            return users;
        }
        Files.list(dir).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
            try {
                Map<String, Object> json = asMap(SimpleJson.parse(readString(path)));
                MoProfile mo = new MoProfile();
                mo.userId = str(json.get("userId"));
                mo.loginId = str(json.get("loginId"));
                Map<String, Object> profile = asMap(json.get("profile"));
                mo.fullName = str(profile.get("fullName"));
                mo.staffId = str(profile.get("staffId"));
                mo.department = str(profile.get("department"));
                mo.phone = str(profile.get("phoneNumber"));
                mo.email = str(asMap(json.get("account")).get("email"));
                users.add(mo);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load MO file: " + path + ", " + ex.getMessage(), ex);
            }
        });
        return users;
    }

    /**
     * Update review decision to application file and review history file.
     *
     * @param application application object
     * @param decision    approved/rejected
     * @param notes       review notes
     * @throws IOException io failure
     */
    public void updateApplicationReview(ApplicationRecord application, String decision, String notes,
                                        String reviewerUserId, String reviewerName) throws IOException {
        String normalized = normalizeStatus(decision);
        Map<String, Object> root = application.raw;
        Map<String, Object> status = asMap(root.get("status"));
        status.put("current", normalized);
        status.put("label", statusLabel(normalized));
        status.put("lastUpdated", now());
        status.put("updatedBy", reviewerUserId);

        Map<String, Object> review = asMap(root.get("review"));
        review.put("decision", normalized);
        review.put("decisionReason", notes == null ? "" : notes);
        review.put("reviewerNotes", notes == null ? "" : notes);
        review.put("reviewedBy", reviewerUserId);
        review.put("reviewedAt", now());

        Map<String, Object> meta = asMap(root.get("meta"));
        meta.put("updatedAt", now());

        writeString(dataDir.resolve("applications").resolve(application.applicationId + ".json"), SimpleJson.stringify(root));
        appendReviewRecord(application, normalized, notes, reviewerName);
    }

    /**
     * Load review records from data/logs/review_records.json.
     *
     * @return records
     * @throws IOException io failure
     */
    public List<ReviewRecord> loadReviewRecords() throws IOException {
        Path file = dataDir.resolve("logs").resolve("review_records.json");
        List<ReviewRecord> records = new ArrayList<ReviewRecord>();
        if (!Files.exists(file)) {
            return records;
        }
        Map<String, Object> root = asMap(SimpleJson.parse(readString(file)));
        List<Object> list = asList(root.get("records"));
        for (Object obj : list) {
            Map<String, Object> m = asMap(obj);
            ReviewRecord r = new ReviewRecord();
            r.applicationId = str(m.get("applicationId"));
            r.courseName = str(m.get("courseName"));
            r.courseCode = str(m.get("courseCode"));
            r.taName = str(m.get("taName"));
            r.studentId = str(m.get("studentId"));
            r.reviewDate = str(m.get("reviewDate"));
            r.result = statusLabel(normalizeStatus(str(m.get("result"))));
            r.reviewer = str(m.get("reviewer"));
            r.notes = str(m.get("notes"));
            records.add(r);
        }
        return records;
    }

    private void appendReviewRecord(ApplicationRecord a, String decision, String notes, String reviewerName) throws IOException {
        Path file = dataDir.resolve("logs").resolve("review_records.json");
        if (!Files.exists(file)) {
            Map<String, Object> init = new LinkedHashMap<String, Object>();
            init.put("records", new ArrayList<Object>());
            writeString(file, SimpleJson.stringify(init));
        }
        Map<String, Object> root = asMap(SimpleJson.parse(readString(file)));
        List<Object> list = asList(root.get("records"));
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("applicationId", a.applicationId);
        row.put("courseName", a.courseName);
        row.put("courseCode", a.courseCode);
        row.put("taName", a.taName);
        row.put("studentId", a.studentId);
        row.put("reviewDate", now());
        row.put("result", decision);
        row.put("reviewer", reviewerName);
        row.put("notes", notes == null ? "" : notes);
        list.add(row);
        root.put("records", list);
        writeString(file, SimpleJson.stringify(root));
    }

    private static String normalizeStatus(String s) {
        String v = s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
        if ("accepted".equals(v) || "approved".equals(v)) {
            return "approved";
        }
        if ("rejected".equals(v)) {
            return "rejected";
        }
        return "pending";
    }

    private static String statusLabel(String status) {
        if ("approved".equals(status)) {
            return "Approved";
        }
        if ("rejected".equals(status)) {
            return "Rejected";
        }
        return "Pending Review";
    }

    private static String now() {
        return LocalDateTime.now().format(TS);
    }

    private static List<String> readSkillNames(Map<String, Object> skillsObj) {
        List<String> skills = new ArrayList<String>();
        for (Object listObj : skillsObj.values()) {
            for (Object row : asList(listObj)) {
                String name = str(asMap(row).get("name"));
                if (!name.isEmpty()) {
                    skills.add(name);
                }
            }
        }
        return skills;
    }

    private static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static void writeString(Path path, String text) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object obj) {
        if (obj instanceof List) {
            return (List<Object>) obj;
        }
        return new ArrayList<Object>();
    }

    private static List<String> toStringList(Object obj) {
        List<String> values = new ArrayList<String>();
        for (Object item : asList(obj)) {
            values.add(str(item));
        }
        return values;
    }

    private static String str(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private static int intVal(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(str(obj));
        } catch (Exception ex) {
            return 0;
        }
    }

    private static double doubleVal(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(str(obj));
        } catch (Exception ex) {
            return 0D;
        }
    }
}
