package appreview.service;

import appreview.data.DataRepository;
import appreview.model.ApplicationRecord;
import appreview.model.JobRecord;
import appreview.model.MoProfile;
import appreview.model.ReviewRecord;
import appreview.model.TaProfile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core application review business service.
 */
public class ApplicationReviewService {
    private final DataRepository repository;
    private List<ApplicationRecord> applications;
    private List<JobRecord> jobs;
    private List<TaProfile> taProfiles;
    private List<MoProfile> moProfiles;
    private List<ReviewRecord> reviewRecords;
    private String currentMoId = "u_mo_001";
    private String currentMoName = "MO";

    /**
     * Constructor.
     *
     * @param repository data repository
     */
    public ApplicationReviewService(DataRepository repository) {
        this.repository = repository;
    }

    /**
     * Load all data from files.
     *
     * @throws IOException io exception
     */
    public void initialize() throws IOException {
        this.applications = repository.loadApplications();
        this.jobs = repository.loadJobs();
        this.taProfiles = repository.loadTaProfiles();
        this.moProfiles = repository.loadMoProfiles();
        this.reviewRecords = repository.loadReviewRecords();
        this.applications.sort(Comparator.comparing(a -> a.applicationId));
        resolveCurrentMo();
    }

    public List<ApplicationRecord> getApplications() {
        return applications;
    }

    public List<JobRecord> getJobs() {
        return jobs;
    }

    public List<ReviewRecord> getReviewRecords() {
        return reviewRecords;
    }

    public String getCurrentMoId() {
        return currentMoId;
    }

    public String getCurrentMoName() {
        return currentMoName;
    }

    public int getActiveCourses() {
        return (int) jobs.stream().filter(j -> "open".equalsIgnoreCase(j.lifecycleStatus))
                .map(j -> j.courseCode).distinct().count();
    }

    public long getOpenJobs() {
        return jobs.stream().filter(j -> "open".equalsIgnoreCase(j.lifecycleStatus)
                && "published".equalsIgnoreCase(j.publicationStatus)).count();
    }

    public long getPendingReviews() {
        return applications.stream().filter(a -> "pending".equalsIgnoreCase(a.statusCurrent)).count();
    }

    public Map<String, Long> getApplicationStats() {
        Map<String, Long> m = new LinkedHashMap<String, Long>();
        m.put("total", (long) applications.size());
        m.put("pending", applications.stream().filter(a -> "pending".equals(a.statusCurrent)).count());
        m.put("approved", applications.stream().filter(a -> "approved".equals(a.statusCurrent)).count());
        m.put("rejected", applications.stream().filter(a -> "rejected".equals(a.statusCurrent)).count());
        return m;
    }

    public List<ApplicationRecord> filterApplications(String keyword, String courseFilter, String statusFilter) {
        String kw = low(keyword);
        String cf = low(courseFilter);
        String sf = low(statusFilter);
        List<ApplicationRecord> result = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord a : applications) {
            boolean okKw = kw.isEmpty() || low(a.taName).contains(kw) || low(a.studentId).contains(kw);
            boolean okCourse = cf.isEmpty() || "all".equals(cf)
                    || low(a.courseCode).contains(cf) || low(a.courseName).contains(cf);
            boolean okStatus = sf.isEmpty() || "all".equals(sf) || low(a.statusCurrent).equals(sf);
            if (okKw && okCourse && okStatus) {
                result.add(a);
            }
        }
        return result;
    }

    public ApplicationRecord findApplicationById(String applicationId) {
        for (ApplicationRecord a : applications) {
            if (a.applicationId.equalsIgnoreCase(applicationId)) {
                return a;
            }
        }
        return null;
    }

    public JobRecord findJob(String jobId) {
        for (JobRecord j : jobs) {
            if (j.jobId.equalsIgnoreCase(jobId)) {
                return j;
            }
        }
        return null;
    }

    public TaProfile findTaByUserId(String userId) {
        for (TaProfile t : taProfiles) {
            if (t.userId.equalsIgnoreCase(userId)) {
                return t;
            }
        }
        return null;
    }

    public MoProfile findMoByUserId(String userId) {
        for (MoProfile m : moProfiles) {
            if (m.userId.equalsIgnoreCase(userId)) {
                return m;
            }
        }
        return null;
    }

    public String getAssignedMoId(ApplicationRecord app) {
        if (app == null || app.raw == null) {
            return "";
        }
        Object workflowObj = app.raw.get("workflow");
        if (!(workflowObj instanceof Map)) {
            return "";
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> workflow = (Map<String, Object>) workflowObj;
        Object assigned = workflow.get("assignedMO");
        return assigned == null ? "" : String.valueOf(assigned);
    }

    public String getMoDisplayName(String moId) {
        MoProfile mo = findMoByUserId(moId);
        return mo == null ? moId : mo.fullName;
    }

    public int[] scoreAndMissings(ApplicationRecord app) {
        JobRecord job = findJob(app.jobId);
        TaProfile ta = findTaByUserId(app.userId);
        if (job == null || ta == null) {
            return new int[]{0, 0, 0};
        }
        int required = Math.max(job.preferredSkills.size(), 1);
        int matched = 0;
        for (String skill : job.preferredSkills) {
            for (String taSkill : ta.skills) {
                if (taSkill.equalsIgnoreCase(skill)) {
                    matched++;
                    break;
                }
            }
        }
        int missing = Math.max(required - matched, 0);
        int score = (int) Math.round((matched * 100.0) / required);
        return new int[]{score, matched, missing};
    }

    public int getCurrentWorkloadHours(String studentId) {
        int total = 0;
        for (ApplicationRecord a : applications) {
            if (studentId.equals(a.studentId) && "approved".equals(a.statusCurrent)) {
                JobRecord job = findJob(a.jobId);
                total += job == null ? a.weeklyHours : job.weeklyHours;
            }
        }
        return total;
    }

    public List<ApplicationRecord> getCurrentAssignments(String studentId) {
        List<ApplicationRecord> result = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord a : applications) {
            if (studentId.equals(a.studentId) && "approved".equals(a.statusCurrent)) {
                result.add(a);
            }
        }
        return result;
    }

    public void quickDecision(ApplicationRecord app, boolean approve, String notes) throws IOException {
        repository.updateApplicationReview(app, approve ? "approved" : "rejected", notes, currentMoId, currentMoName);
        initialize();
    }

    public Map<String, Long> reviewStats() {
        Map<String, Long> m = new LinkedHashMap<String, Long>();
        m.put("total", (long) reviewRecords.size());
        m.put("approved", reviewRecords.stream().filter(r -> "Approved".equalsIgnoreCase(r.result)).count());
        m.put("rejected", reviewRecords.stream().filter(r -> "Rejected".equalsIgnoreCase(r.result)).count());
        return m;
    }

    public List<ReviewRecord> filterReviews(String keyword, String resultFilter, int dayRange) {
        List<ReviewRecord> rows = new ArrayList<ReviewRecord>();
        String kw = low(keyword);
        String rf = low(resultFilter);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        for (ReviewRecord r : reviewRecords) {
            boolean okKw = kw.isEmpty() || low(r.courseCode + " " + r.courseName + " " + r.taName + " " + r.studentId).contains(kw);
            boolean okResult = rf.isEmpty() || "all".equals(rf) || low(r.result).contains(rf);
            boolean okTime = true;
            if (dayRange > 0) {
                try {
                    okTime = LocalDateTime.parse(r.reviewDate, dt).isAfter(now.minusDays(dayRange));
                } catch (Exception ex) {
                    okTime = true;
                }
            }
            if (okKw && okResult && okTime) {
                rows.add(r);
            }
        }
        return rows;
    }

    private static String low(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private void resolveCurrentMo() {
        MoProfile selected = findMoByUserId("u_mo_001");
        if (selected == null && !moProfiles.isEmpty()) {
            selected = moProfiles.get(0);
        }
        if (selected != null) {
            currentMoId = selected.userId;
            currentMoName = selected.fullName;
        }
    }
}
