package Admin_Module.com.taapp.data;

import Admin_Module.com.taapp.model.Application;
import Admin_Module.com.taapp.model.AssignedPosition;
import Admin_Module.com.taapp.model.Position;
import Admin_Module.com.taapp.model.Statistics;
import Admin_Module.com.taapp.model.TA;
import Admin_Module.com.taapp.util.SimpleJson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonDataRepository {
    private final Path dataRoot;

    public JsonDataRepository() { this(DataRoot.resolve()); }
    public JsonDataRepository(Path dataRoot) { this.dataRoot = dataRoot == null ? DataRoot.resolve() : dataRoot; }

    public List<Application> loadApplications() {
        List<Map<String, Object>> indexed = readArrayObject(dataRoot.resolve("indexes").resolve("applications_index.json"), "applications");
        Map<String, Map<String, Object>> indexById = new HashMap<>();
        for (Map<String, Object> idx : indexed) {
            String appId = text(idx, "applicationId", "");
            if (!appId.isBlank()) {
                indexById.put(appId, idx);
            }
        }

        List<Application> apps = new ArrayList<>();
        List<Path> appFiles = listJsonFiles(dataRoot.resolve("applications"));
        for (Path appFile : appFiles) {
            Map<String, Object> root = readObject(appFile);
            if (root == null) continue;
            String appId = text(root, "applicationId", appFile.getFileName().toString().replaceFirst("\\.json$", ""));
            apps.add(toApplication(root, indexById.getOrDefault(appId, Map.of()), appId));
        }

        // Backward compatibility: if application files are missing, retain index-driven loading.
        if (apps.isEmpty()) {
            for (Map<String, Object> idx : indexed) {
                String appId = text(idx, "applicationId", "");
                Map<String, Object> root = readObject(dataRoot.resolve("applications").resolve(appId + ".json"));
                if (root == null) continue;
                apps.add(toApplication(root, idx, appId));
            }
        }
        return List.copyOf(apps);
    }

    private Application toApplication(Map<String, Object> root, Map<String, Object> idx, String fallbackAppId) {
        String appId = text(root, "applicationId", fallbackAppId);
        Map<String, Object> job = loadJobById(text(root, "jobId", text(idx, "jobId", "")));
        Map<String, Object> applicant = map(root.get("applicantSnapshot"));
        Map<String, Object> status = map(root.get("status"));
        Map<String, Object> meta = map(root.get("meta"));
        return new Application(
                appId,
                text(root, "userId", text(root, "studentId", text(applicant, "studentId", text(idx, "studentId", "")))),
                text(applicant, "fullName", text(idx, "studentName", "")),
                text(root, "jobId", text(idx, "jobId", "")),
                text(job, "title", text(idx, "jobTitle", text(root, "jobSnapshot.title", ""))),
                text(job, "moduleName", text(job, "course.courseName", text(root, "jobSnapshot.courseName", text(idx, "jobTitle", "")))),
                text(job, "department", text(applicant, "department", text(root, "jobSnapshot.department", text(idx, "department", "")))),
                text(meta, "submittedAt", text(idx, "appliedDate", text(root, "meta.submittedAt", ""))),
                text(status, "current", text(idx, "status", text(root, "status", "")))
        );
    }

    public List<Position> loadPositions() {
        List<Map<String, Object>> jobs = loadMergedJobs();
        Map<String, List<Application>> appsByJob = loadApplications().stream()
                .collect(Collectors.groupingBy(Application::getPositionId));
        List<Position> positions = new ArrayList<>();
        for (Map<String, Object> job : jobs) {
            Map<String, Object> course = map(job.get("course"));
            Map<String, Object> employment = map(job.get("employment"));
            Map<String, Object> lifecycle = map(job.get("lifecycle"));
            String jobId = text(job, "id", text(job, "jobId", ""));
            if (jobId.isBlank()) continue;
            List<Application> jobApps = appsByJob.getOrDefault(jobId, List.of());
            int applicationCount = jobApps.size();
            int acceptedCount = (int) jobApps.stream().filter(a -> isStatus(a.getStatus(), "accepted") || isStatus(a.getStatus(), "approved")).count();
            positions.add(new Position(
                    jobId,
                    text(job, "title", ""),
                    text(course, "courseName", text(job, "moduleName", text(job, "title", ""))),
                    text(job, "department", ""),
                    intValue(employment, "weeklyHours", intValue(job, "weeklyHours", 0)),
                    intValue(job, "quota", 0),
                    acceptedCount,
                    text(lifecycle, "status", text(job, "status", "")),
                    applicationCount
            ));
        }
        return List.copyOf(positions);
    }

    public List<TA> loadTAs() {
        List<Application> apps = loadApplications();
        Map<String, List<Application>> byUser = apps.stream().collect(Collectors.groupingBy(this::resolveUserKey));
        List<TA> tas = new ArrayList<>();
        Map<String, Map<String, Object>> mergedJobsById = loadMergedJobs().stream().collect(Collectors.toMap(
                j -> text(j, "id", text(j, "jobId", "")),
                j -> j,
                this::mergeJobs
        ));
        for (Path p : listJsonFiles(dataRoot.resolve("users").resolve("ta"))) {
            Map<String, Object> root = readObject(p);
            if (root == null) continue;
            Map<String, Object> account = map(root.get("account"));
            Map<String, Object> profile = map(root.get("profile"));
            String userId = text(root, "userId", p.getFileName().toString().replaceFirst("\\.json$", ""));
            List<AssignedPosition> assigned = new ArrayList<>();
            int totalWorkload = 0;
            for (Application app : byUser.getOrDefault(userId, List.of())) {
                if (!isStatus(app.getStatus(), "accepted") && !isStatus(app.getStatus(), "approved")) continue;
                Map<String, Object> job = mergedJobsById.getOrDefault(app.getPositionId(), loadJobById(app.getPositionId()));
                Map<String, Object> course = map(job.get("course"));
                Map<String, Object> dates = map(job.get("dates"));
                int hours = resolveWeeklyHours(job, app);
                totalWorkload += hours;
                assigned.add(new AssignedPosition(
                        app.getPositionId(),
                        text(job, "title", app.getPositionTitle()),
                        text(course, "courseName", app.getCourse()),
                        text(job, "department", app.getDepartment()),
                        hours,
                        text(dates, "startDate", ""),
                        text(dates, "endDate", ""),
                        "active"
                ));
            }
            String status = totalWorkload > 0 ? "active" : "inactive";
            tas.add(new TA(
                    userId,
                    text(profile, "fullName", text(root, "loginId", userId)),
                    text(profile, "studentId", text(account, "username", text(root, "loginId", ""))),
                    text(account, "email", ""),
                    text(profile, "programMajor", text(profile, "department", "")),
                    text(profile, "year", ""),
                    assigned.size(),
                    totalWorkload,
                    status,
                    assigned
            ));
        }
        return List.copyOf(tas);
    }

    public Statistics loadStatistics() {
        List<Application> apps = loadApplications();
        List<Position> positions = loadPositions();
        List<TA> tas = loadTAs();

        int totalApplications = apps.size();
        int approvedApplications = (int) apps.stream().filter(a -> isStatus(a.getStatus(), "approved") || isStatus(a.getStatus(), "accepted")).count();
        int pendingApplications = (int) apps.stream().filter(a -> isStatus(a.getStatus(), "pending") || isStatus(a.getStatus(), "under_review")).count();
        int rejectedApplications = (int) apps.stream().filter(a -> isStatus(a.getStatus(), "rejected")).count();
        int approvalRate = totalApplications > 0 ? (int) Math.round((approvedApplications * 100.0) / totalApplications) : 0;

        int totalPositions = positions.size();
        int openPositions = (int) positions.stream().filter(p -> isStatus(p.getStatus(), "open")).count();
        int filledPositions = (int) positions.stream().filter(p -> isStatus(p.getStatus(), "closed") || isStatus(p.getStatus(), "filled")).count();
        int fillRate = totalPositions > 0 ? (int) Math.round((filledPositions * 100.0) / totalPositions) : 0;

        int totalTAs = tas.size();
        int activeTAs = (int) tas.stream().filter(ta -> isStatus(ta.getStatus(), "active")).count();
        int totalWorkload = tas.stream().mapToInt(TA::getTotalWorkload).sum();
        int avgWorkload = totalTAs > 0 ? (int) Math.round(totalWorkload * 1.0 / totalTAs) : 0;

        Map<String, MutableDept> acc = new HashMap<>();
        for (Position p : positions) {
            MutableDept d = acc.computeIfAbsent(p.getDepartment(), k -> new MutableDept());
            d.total += 1;
            if (isStatus(p.getStatus(), "closed") || isStatus(p.getStatus(), "filled")) d.filled += 1;
            d.applications += p.getApplicationCount();
        }
        Map<String, Statistics.DepartmentStats> departmentStats = acc.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new Statistics.DepartmentStats(e.getValue().total, e.getValue().filled, e.getValue().applications)
        ));
        return new Statistics(totalApplications, approvedApplications, pendingApplications, rejectedApplications, approvalRate,
                totalPositions, openPositions, filledPositions, fillRate, totalTAs, activeTAs, totalWorkload, avgWorkload, departmentStats);
    }

    private String resolveUserKey(Application app) {
        String userId = app.getTaId();
        if (userId != null && !userId.isBlank()) {
            return userId.startsWith("u_") ? userId : "u_ta_" + userId;
        }
        return app.getId();
    }

    private List<Map<String, Object>> loadMergedJobs() {
        Map<String, Map<String, Object>> merged = new HashMap<>();

        for (Map<String, Object> job : readArray(dataRoot.resolve("jobs.json"))) {
            String jobId = text(job, "id", text(job, "jobId", ""));
            if (jobId.isBlank()) continue;
            merged.merge(jobId, job, this::mergeJobs);
        }

        for (Path p : listJsonFiles(dataRoot.resolve("jobs"))) {
            Map<String, Object> job = readObject(p);
            if (job == null || job.isEmpty()) continue;
            String jobId = text(job, "id", text(job, "jobId", p.getFileName().toString().replaceFirst("\\.json$", "")));
            if (jobId.isBlank()) continue;
            merged.merge(jobId, job, this::mergeJobs);
        }

        for (Application app : loadApplications()) {
            String jobId = app.getPositionId();
            if (jobId == null || jobId.isBlank() || merged.containsKey(jobId)) continue;
            Map<String, Object> appRoot = readObject(dataRoot.resolve("applications").resolve(app.getId() + ".json"));
            Map<String, Object> snapshot = map(map(appRoot).get("jobSnapshot"));
            Map<String, Object> shadow = new HashMap<>();
            shadow.put("id", jobId);
            shadow.put("jobId", jobId);
            shadow.put("title", text(snapshot, "title", app.getPositionTitle()));
            shadow.put("moduleName", text(snapshot, "courseName", app.getCourse()));
            shadow.put("department", text(snapshot, "department", app.getDepartment()));
            shadow.put("weeklyHours", intValue(snapshot, "weeklyHours", 0));
            shadow.put("quota", 0);
            shadow.put("status", "open");
            merged.put(jobId, shadow);
        }

        List<Map<String, Object>> out = new ArrayList<>(merged.values());
        out.sort(Comparator.comparing(j -> text(j, "id", text(j, "jobId", ""))));
        return out;
    }

    private Map<String, Object> mergeJobs(Map<String, Object> primary, Map<String, Object> secondary) {
        Map<String, Object> out = new HashMap<>();
        if (secondary != null) out.putAll(secondary);
        if (primary != null) out.putAll(primary);
        return out;
    }

    private int resolveWeeklyHours(Map<String, Object> job, Application app) {
        int fromJob = intValue(map(job.get("employment")), "weeklyHours", -1);
        if (fromJob >= 0) {
            return fromJob;
        }

        Map<String, Object> appRoot = readObject(dataRoot.resolve("applications").resolve(app.getId() + ".json"));
        int fromSnapshot = intValue(map(map(appRoot).get("jobSnapshot")), "weeklyHours", -1);
        if (fromSnapshot >= 0) {
            return fromSnapshot;
        }

        return 0;
    }

    private Map<String, Object> loadJobById(String jobId) {
        if (jobId == null || jobId.isBlank()) return Map.of();
        Map<String, Object> single = readObject(dataRoot.resolve("jobs").resolve(jobId + ".json"));
        if (single != null && !single.isEmpty()) return single;
        for (Map<String, Object> job : readArray(dataRoot.resolve("jobs.json"))) {
            if (jobId.equals(text(job, "id", text(job, "jobId", "")))) return job;
        }
        return Map.of();
    }

    private List<Map<String, Object>> readArrayObject(Path path, String field) {
        Map<String, Object> root = readObject(path);
        if (root == null) return List.of();
        Object v = root.get(field);
        if (!(v instanceof List<?> list)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = map(item);
            if (!m.isEmpty()) out.add(m);
        }
        return out;
    }

    private List<Map<String, Object>> readArray(Path path) {
        Object parsed = readAny(path);
        if (!(parsed instanceof List<?> list)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = map(item);
            if (!m.isEmpty()) out.add(m);
        }
        return out;
    }

    private List<Path> listJsonFiles(Path dir) {
        if (!Files.isDirectory(dir)) return List.of();
        List<Path> out = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path p : stream) out.add(p);
        } catch (IOException e) {
            return List.of();
        }
        out.sort(Comparator.comparing(p -> p.getFileName().toString()));
        return out;
    }

    private Object readAny(Path p) {
        try { return SimpleJson.parse(Files.readString(p, StandardCharsets.UTF_8)); } catch (Exception e) { return null; }
    }

    private Map<String, Object> readObject(Path p) {
        Object any = readAny(p);
        return any instanceof Map ? map(any) : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object v) { return v instanceof Map ? (Map<String, Object>) v : Map.of(); }

    private static String text(Map<String, Object> obj, String key, String def) {
        Object v = resolve(obj, key);
        return v == null ? def : String.valueOf(v);
    }

    private static Object resolve(Map<String, Object> obj, String key) {
        if (obj == null || key == null || key.isBlank()) return null;
        Object current = obj;
        for (String part : key.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(part);
            if (current == null) return null;
        }
        return current;
    }

    private static int intValue(Map<String, Object> obj, String key, int def) {
        Object v = resolve(obj, key);
        if (v instanceof Number n) return n.intValue();
        try { return v == null ? def : Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    private static boolean isStatus(String actual, String expected) { return actual != null && actual.equalsIgnoreCase(expected); }

    private static final class MutableDept { int total; int filled; int applications; }
}
