package com.mojobsystem.service;

import com.mojobsystem.model.job.Job;
import com.mojobsystem.model.job.JobStatusUtil;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;

import java.util.List;
import java.util.Set;

public final class MoDashboardService {
    public record DashboardMetrics(int totalJobPostings, int openJobPostings, int pendingReviews) {
    }

    private MoDashboardService() {
    }

    public static DashboardMetrics compute(JobRepository jobRepository,
                                           ApplicationRepository applicationRepository,
                                           String moUserId) {
        List<Job> moJobs = jobRepository.loadJobsForMo(moUserId);
        Set<String> jobIds = jobRepository.loadMoJobIds(moUserId);
        int totalPostings = moJobs.size();

        long openPostings = moJobs.stream()
                .filter(j -> JobStatusUtil.isOpen(j.getStatus()))
                .count();

        int pending = applicationRepository.countPendingReviewsForMo(moUserId, jobIds);
        return new DashboardMetrics(totalPostings, (int) openPostings, pending);
    }

}
