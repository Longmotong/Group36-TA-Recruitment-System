package com.taapp.data;

import com.taapp.model.Application;
import com.taapp.model.Position;
import com.taapp.model.Statistics;
import com.taapp.model.TA;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class MockData {
    private static final JsonDataRepository REPO = new JsonDataRepository();

    private MockData() {}

    public static List<TA> getMockTAs() {
        return REPO.loadTAs();
    }

    public static List<Application> getMockApplications() {
        return REPO.loadApplications();
    }

    public static List<Position> getMockPositions() {
        return REPO.loadPositions();
    }

    public static Statistics getStatistics() {
        return REPO.loadStatistics();
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

    private static java.time.LocalDate appliedDateSafe(Application a) {
        try {
            return java.time.LocalDate.parse(a.getAppliedDate());
        } catch (Exception e) {
            return java.time.LocalDate.MIN;
        }
    }
}
