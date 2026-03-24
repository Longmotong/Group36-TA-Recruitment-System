package com.taapp.data;

import com.taapp.model.Application;
import com.taapp.model.AssignedPosition;
import com.taapp.model.Position;
import com.taapp.model.Statistics;
import com.taapp.model.TA;
import com.taapp.util.SimpleJson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DataStore {
    private final Path dataRoot;

    private List<TA> cachedTAs;
    private List<Position> cachedPositions;
    private List<Application> cachedApplications;
    private Statistics cachedStatistics;

    public DataStore(Path dataRoot) {
        this.dataRoot = Objects.requireNonNull(dataRoot);
    }

    public static DataStore defaultStore() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path rootInCurrent = cwd.resolve("data（1）").resolve("data").normalize();
        if (Files.exists(rootInCurrent)) {
            return new DataStore(rootInCurrent);
        }
        Path rootInParent = cwd.resolve("..").resolve("data（1）").resolve("data").normalize();
        return new DataStore(rootInParent);
    }

    public List<TA> getTAs() {
        ensureLoaded();
        return cachedTAs;
    }

    public List<Position> getPositions() {
        ensureLoaded();
        return cachedPositions;
    }

    public List<Application> getApplications() {
        ensureLoaded();
        return cachedApplications;
    }

    public Statistics getStatistics() {
        ensureLoaded();
        return cachedStatistics;
    }

    private void ensureLoaded() {
        if (cachedTAs != null) return;
        try {
            Map<String, String> studentIdToName = new HashMap<>();
            cachedPositions = Collections.unmodifiableList(loadPositions());
            List<TA> rawTAs = loadTAs(studentIdToName);
            cachedApplications = Collections.unmodifiableList(loadApplications(studentIdToName));
            cachedTAs = Collections.unmodifiableList(enrichTAs(rawTAs, cachedPositions, cachedApplications));
            cachedStatistics = computeStatistics(cachedTAs, cachedPositions, cachedApplications);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + dataRoot + ": " + e.getMessage(), e);
        }
    }

    private List<TA> loadTAs(Map<String, String> studentIdToName) throws IOException {
        List<TA> tas = new ArrayList<>();

        Path usersTaDir = dataRoot.resolve("users").resolve("ta");
        if (!Files.exists(usersTaDir)) return tas;

        try (var stream = Files.list(usersTaDir)) {
            for (Path p : stream.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList())) {
                String json = Files.readString(p, StandardCharsets.UTF_8);
                Map<String, Object> obj = SimpleJson.parseObject(json);

                Map<String, Object> profile = asObj(obj.get("profile"));
                Map<String, Object> account = asObj(obj.get("account"));

                String id = str(obj.get("userId"));
                String name = str(profile.get("fullName"));
                String studentId = str(profile.get("studentId"));
                String email = str(account.get("email"));
                String program = str(profile.get("programMajor"));
                String year = str(profile.get("year"));
                String status = str(account.get("status"));

                studentIdToName.put(studentId, name);

                // Workload/assignments are not represented directly in this dataset.
                tas.add(new TA(
                        id,
                        name,
                        studentId,
                        email,
                        program,
                        year,
                        0,
                        0,
                        status,
                        List.of()
                ));
            }
        }

        return tas;
    }

    private List<Position> loadPositions() throws IOException {
        List<Position> positions = new ArrayList<>();

        Path jobsDir = dataRoot.resolve("jobs");
        if (!Files.exists(jobsDir)) return positions;

        try (var stream = Files.list(jobsDir)) {
            for (Path p : stream.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList())) {
                String json = Files.readString(p, StandardCharsets.UTF_8);
                Map<String, Object> obj = SimpleJson.parseObject(json);

                String id = str(obj.get("jobId"));
                String title = str(obj.get("title"));
                String department = str(obj.get("department"));

                Map<String, Object> course = asObj(obj.get("course"));
                String courseCode = str(course.get("courseCode"));
                String courseName = str(course.get("courseName"));
                String courseLabel = joinNonEmpty(" - ", courseCode, courseName);

                Map<String, Object> employment = asObj(obj.get("employment"));
                int weeklyHours = intVal(employment.get("weeklyHours"));

                Map<String, Object> lifecycle = asObj(obj.get("lifecycle"));
                String status = str(lifecycle.get("status"));

                Map<String, Object> stats = asObj(obj.get("stats"));
                int applicationCount = intVal(stats.get("applicationCount"));

                // Swing UI expects: requiredHours/maxTAs/assignedTAs. We map weeklyHours -> requiredHours for now.
                positions.add(new Position(
                        id,
                        title,
                        courseLabel,
                        department,
                        weeklyHours,
                        1,
                        0,
                        status,
                        applicationCount
                ));
            }
        }

        return positions;
    }

    private List<Application> loadApplications(Map<String, String> studentIdToName) throws IOException {
        List<Application> apps = new ArrayList<>();

        Path appsDir = dataRoot.resolve("applications");
        if (!Files.exists(appsDir)) return apps;

        try (var stream = Files.list(appsDir)) {
            for (Path p : stream.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList())) {
                String json = Files.readString(p, StandardCharsets.UTF_8);
                Map<String, Object> obj = SimpleJson.parseObject(json);

                String id = str(obj.get("applicationId"));
                String studentId = str(obj.get("studentId"));
                String taName = studentIdToName.getOrDefault(studentId, "");
                String jobId = str(obj.get("jobId"));

                Map<String, Object> jobSnap = asObj(obj.get("jobSnapshot"));
                String positionTitle = str(jobSnap.get("title"));
                String dept = str(jobSnap.get("department"));
                String courseCode = str(jobSnap.get("courseCode"));
                String courseName = str(jobSnap.get("courseName"));
                String course = joinNonEmpty(" - ", courseCode, courseName);

                Map<String, Object> statusObj = asObj(obj.get("status"));
                String status = str(statusObj.get("current"));

                String appliedDate = extractDate(str(asObj(obj.get("meta")).get("submittedAt")));

                apps.add(new Application(
                        id,
                        studentId,
                        taName,
                        jobId,
                        positionTitle,
                        course,
                        dept,
                        appliedDate,
                        status
                ));
            }
        }

        return apps;
    }

    private static List<TA> enrichTAs(List<TA> rawTAs, List<Position> positions, List<Application> apps) {
        Map<String, Position> positionById = positions.stream()
                .collect(Collectors.toMap(Position::getId, p -> p, (a, b) -> a));

        Map<String, List<Application>> acceptedByStudentId = apps.stream()
                .filter(a -> "accepted".equalsIgnoreCase(a.getStatus()) || "approved".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(Application::getTaId));

        List<TA> enriched = new ArrayList<>();
        for (TA ta : rawTAs) {
            List<Application> accepted = acceptedByStudentId.getOrDefault(ta.getStudentId(), List.of());
            List<AssignedPosition> assigned = new ArrayList<>();
            int totalHours = 0;
            for (Application a : accepted) {
                Position p = positionById.get(a.getPositionId());
                int weeklyHours = p != null ? p.getRequiredHours() : 0;
                int termHours = weeklyHours * 8; // approximate one teaching period
                totalHours += termHours;
                assigned.add(new AssignedPosition(
                        a.getPositionId(),
                        a.getPositionTitle(),
                        a.getCourse(),
                        a.getDepartment(),
                        termHours,
                        "",
                        "",
                        "active"
                ));
            }
            enriched.add(new TA(
                    ta.getId(),
                    ta.getName(),
                    ta.getStudentId(),
                    ta.getEmail(),
                    ta.getProgram(),
                    ta.getYear(),
                    assigned.size(),
                    totalHours,
                    ta.getStatus(),
                    Collections.unmodifiableList(assigned)
            ));
        }
        return enriched;
    }

    private static Statistics computeStatistics(List<TA> tas, List<Position> positions, List<Application> apps) {
        int totalApplications = apps.size();
        int approved = (int) apps.stream().filter(a -> "accepted".equalsIgnoreCase(a.getStatus()) || "approved".equalsIgnoreCase(a.getStatus())).count();
        int pending = (int) apps.stream().filter(a -> "pending".equalsIgnoreCase(a.getStatus()) || "under_review".equalsIgnoreCase(a.getStatus())).count();
        int rejected = (int) apps.stream().filter(a -> "rejected".equalsIgnoreCase(a.getStatus())).count();
        int approvalRate = totalApplications > 0 ? (int) Math.round((approved * 100.0) / totalApplications) : 0;

        int totalPositions = positions.size();
        int openPositions = (int) positions.stream().filter(p -> "open".equalsIgnoreCase(p.getStatus())).count();
        int filledPositions = (int) positions.stream().filter(p -> "filled".equalsIgnoreCase(p.getStatus())).count();
        int fillRate = totalPositions > 0 ? (int) Math.round((filledPositions * 100.0) / totalPositions) : 0;

        int totalTAs = tas.size();
        int activeTAs = (int) tas.stream().filter(ta -> "active".equalsIgnoreCase(ta.getStatus())).count();
        int totalWorkload = tas.stream().mapToInt(TA::getTotalWorkload).sum();
        int avgWorkload = totalTAs > 0 ? (int) Math.round(totalWorkload * 1.0 / totalTAs) : 0;

        Map<String, MutableDept> acc = new HashMap<>();
        for (Position p : positions) {
            MutableDept d = acc.computeIfAbsent(p.getDepartment(), k -> new MutableDept());
            d.total++;
            if ("filled".equalsIgnoreCase(p.getStatus())) d.filled++;
            d.applications += p.getApplicationCount();
        }
        Map<String, Statistics.DepartmentStats> deptStats = acc.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new Statistics.DepartmentStats(e.getValue().total, e.getValue().filled, e.getValue().applications)
                ));

        return new Statistics(
                totalApplications,
                approved,
                pending,
                rejected,
                approvalRate,
                totalPositions,
                openPositions,
                filledPositions,
                fillRate,
                totalTAs,
                activeTAs,
                totalWorkload,
                avgWorkload,
                deptStats
        );
    }

    private static String extractDate(String iso) {
        if (iso == null) return "";
        try {
            return LocalDate.parse(iso.substring(0, 10)).toString();
        } catch (Exception e) {
            return iso;
        }
    }

    private static Map<String, Object> asObj(Object v) {
        if (v == null) return Map.of();
        if (!(v instanceof Map)) return Map.of();
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) v;
        return m;
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private static int intVal(Object v) {
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Long) return (int) Math.min(Integer.MAX_VALUE, (Long) v);
        if (v instanceof Double) return (int) Math.round((Double) v);
        if (v == null) return 0;
        try {
            return (int) Math.round(Double.parseDouble(String.valueOf(v)));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String joinNonEmpty(String sep, String a, String b) {
        boolean ha = a != null && !a.isBlank();
        boolean hb = b != null && !b.isBlank();
        if (ha && hb) return a + sep + b;
        if (ha) return a;
        if (hb) return b;
        return "";
    }

    private static final class MutableDept {
        int total = 0;
        int filled = 0;
        int applications = 0;
    }
}

