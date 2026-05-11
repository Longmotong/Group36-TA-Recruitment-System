package com.mojobsystem.service;

import com.mojobsystem.model.Job;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class MoDashboardService {
    public record DashboardMetrics(int activeCourses, int openJobPostings, int pendingReviews) {
    }

    private MoDashboardService() {
    }

    public static DashboardMetrics compute(JobRepository jobRepository,
                                           ApplicationRepository applicationRepository,
                                           String moUserId) {
        List<Job> moJobs = jobRepository.loadJobsForMo(moUserId);
        Set<String> jobIds = jobRepository.loadMoJobIds(moUserId);

        Set<String> openModules = moJobs.stream()
                .filter(j -> "open".equals(normalizedStatus(j.getStatus())))
                .map(Job::getModuleCode)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toSet());

        long openPostings = moJobs.stream()
                .filter(j -> "open".equals(normalizedStatus(j.getStatus())))
                .count();

        int pending = applicationRepository.countPendingReviewsForMo(moUserId, jobIds);
        return new DashboardMetrics(openModules.size(), (int) openPostings, pending);
    }

    private static String normalizedStatus(String status) {
        if (status == null || status.isBlank()) {
            return "open";
        }
        String s = status.trim().toLowerCase(Locale.ENGLISH);
        if ("closed".equals(s)) {
            return "closed";
        }
        if ("draft".equals(s)) {
            return "draft";
        }
        return "open";
    }
}
