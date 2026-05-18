package MO_system.service;

import MO_system.model.job.Job;
import MO_system.model.job.JobStatusUtil;
import MO_system.repository.ApplicationRepository;
import MO_system.repository.JobRepository;

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
