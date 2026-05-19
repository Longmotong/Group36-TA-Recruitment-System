package com.taapp.data;

import com.taapp.model.Application;
import com.taapp.model.AssignedPosition;
import com.taapp.model.Position;
import com.taapp.model.Statistics;
import com.taapp.model.TA;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MockData {
    private MockData() {}

    public static List<TA> getMockTAs() {
        List<TA> tas = new ArrayList<>();

        tas.add(new TA(
                "ta1",
                "Zhang Wei",
                "TA2024001",
                "zhang.wei@university.edu",
                "Computer Science",
                "Year 3",
                2,
                160,
                "active",
                List.of(
                        new AssignedPosition(
                                "pos1",
                                "Teaching Assistant",
                                "CS101 - Introduction to Programming",
                                "Computer Science",
                                80,
                                "2024-09-01",
                                "2024-12-20",
                                "active"),
                        new AssignedPosition(
                                "pos2",
                                "Lab Assistant",
                                "CS201 - Data Structures",
                                "Computer Science",
                                80,
                                "2024-09-01",
                                "2024-12-20",
                                "active")
                )));

        tas.add(new TA(
                "ta2",
                "Liu Xiao Ming",
                "TA2024002",
                "liu.xiaoming@university.edu",
                "Information Systems",
                "Year 4",
                1,
                100,
                "active",
                List.of(
                        new AssignedPosition(
                                "pos3",
                                "Grading Assistant",
                                "IS301 - Database Systems",
                                "Information Systems",
                                100,
                                "2024-09-01",
                                "2024-12-20",
                                "active")
                )));

        tas.add(new TA(
                "ta3",
                "Wang Mei Ling",
                "TA2024003",
                "wang.meiling@university.edu",
                "Business Administration",
                "Year 2",
                3,
                240,
                "active",
                List.of(
                        new AssignedPosition(
                                "pos4",
                                "Teaching Assistant",
                                "BUS101 - Business Fundamentals",
                                "Business",
                                80,
                                "2024-09-01",
                                "2024-12-20",
                                "active"),
                        new AssignedPosition(
                                "pos5",
                                "Tutorial Assistant",
                                "BUS201 - Marketing",
                                "Business",
                                80,
                                "2024-09-01",
                                "2024-12-20",
                                "active"),
                        new AssignedPosition(
                                "pos6",
                                "Lab Assistant",
                                "BUS301 - Finance",
                                "Business",
                                80,
                                "2024-09-01",
                                "2024-12-20",
                                "active")
                )));

        tas.add(new TA(
                "ta4",
                "Chen Hao Ran",
                "TA2024004",
                "chen.haoran@university.edu",
                "Mathematics",
                "Year 3",
                1,
                120,
                "active",
                List.of(
                        new AssignedPosition(
                                "pos7",
                                "Teaching Assistant",
                                "MATH201 - Calculus II",
                                "Mathematics",
                                120,
                                "2024-09-01",
                                "2024-12-20",
                                "active")
                )));

        tas.add(new TA(
                "ta5",
                "Zhao Li Na",
                "TA2024005",
                "zhao.lina@university.edu",
                "Computer Science",
                "Year 4",
                0,
                0,
                "inactive",
                List.of()
        ));

        return Collections.unmodifiableList(tas);
    }

    public static List<Application> getMockApplications() {
        List<Application> apps = new ArrayList<>();

        apps.add(new Application(
                "app1",
                "ta1",
                "Zhang Wei",
                "pos1",
                "Teaching Assistant",
                "CS101 - Introduction to Programming",
                "Computer Science",
                "2024-08-15",
                "approved"
        ));

        apps.add(new Application(
                "app2",
                "ta1",
                "Zhang Wei",
                "pos2",
                "Lab Assistant",
                "CS201 - Data Structures",
                "Computer Science",
                "2024-08-16",
                "approved"
        ));

        apps.add(new Application(
                "app3",
                "ta2",
                "Liu Xiao Ming",
                "pos3",
                "Grading Assistant",
                "IS301 - Database Systems",
                "Information Systems",
                "2024-08-17",
                "approved"
        ));

        apps.add(new Application(
                "app4",
                "ta5",
                "Zhao Li Na",
                "pos8",
                "Research Assistant",
                "CS401 - AI Research",
                "Computer Science",
                "2024-08-20",
                "pending"
        ));

        apps.add(new Application(
                "app5",
                "ta3",
                "Wang Mei Ling",
                "pos9",
                "Office Assistant",
                "BUS101 - Business Fundamentals",
                "Business",
                "2024-08-19",
                "rejected"
        ));

        return Collections.unmodifiableList(apps);
    }

    public static List<Position> getMockPositions() {
        List<Position> positions = new ArrayList<>();

        positions.add(new Position(
                "pos1",
                "Teaching Assistant",
                "CS101 - Introduction to Programming",
                "Computer Science",
                80,
                2,
                2,
                "filled",
                5
        ));

        positions.add(new Position(
                "pos2",
                "Lab Assistant",
                "CS201 - Data Structures",
                "Computer Science",
                80,
                3,
                2,
                "open",
                8
        ));

        positions.add(new Position(
                "pos3",
                "Grading Assistant",
                "IS301 - Database Systems",
                "Information Systems",
                100,
                2,
                1,
                "open",
                3
        ));

        positions.add(new Position(
                "pos4",
                "Teaching Assistant",
                "BUS101 - Business Fundamentals",
                "Business",
                80,
                2,
                2,
                "filled",
                6
        ));

        positions.add(new Position(
                "pos5",
                "Tutorial Assistant",
                "MATH201 - Calculus II",
                "Mathematics",
                120,
                1,
                1,
                "filled",
                4
        ));

        return Collections.unmodifiableList(positions);
    }

    public static Statistics getStatistics() {
        List<Application> apps = getMockApplications();
        List<Position> positions = getMockPositions();
        List<TA> tas = getMockTAs();

        int totalApplications = apps.size();
        int approvedApplications = (int) apps.stream().filter(a -> "approved".equalsIgnoreCase(a.getStatus())).count();
        int pendingApplications = (int) apps.stream().filter(a -> "pending".equalsIgnoreCase(a.getStatus())).count();
        int rejectedApplications = (int) apps.stream().filter(a -> "rejected".equalsIgnoreCase(a.getStatus())).count();
        int approvalRate = totalApplications > 0 ? (int) Math.round((approvedApplications * 100.0) / totalApplications) : 0;

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
            d.total += 1;
            if ("filled".equalsIgnoreCase(p.getStatus())) d.filled += 1;
            d.applications += p.getApplicationCount();
        }
        Map<String, Statistics.DepartmentStats> departmentStats = acc.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new Statistics.DepartmentStats(e.getValue().total, e.getValue().filled, e.getValue().applications)
                ));

        return new Statistics(
                totalApplications,
                approvedApplications,
                pendingApplications,
                rejectedApplications,
                approvalRate,
                totalPositions,
                openPositions,
                filledPositions,
                fillRate,
                totalTAs,
                activeTAs,
                totalWorkload,
                avgWorkload,
                departmentStats
        );
    }

    public static List<TA> getTAsByWorkloadDesc() {
        return getMockTAs().stream()
                .sorted(Comparator.comparingInt(TA::getTotalWorkload).reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Application> getRecentApplications(int limit) {
        return getMockApplications().stream()
                .sorted(Comparator.comparing(MockData::appliedDateSafe).reversed())
                .limit(Math.max(0, limit))
                .collect(Collectors.toUnmodifiableList());
    }

    private static LocalDate appliedDateSafe(Application a) {
        try {
            return LocalDate.parse(a.getAppliedDate());
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

    private static final class MutableDept {
        int total = 0;
        int filled = 0;
        int applications = 0;
    }
}

