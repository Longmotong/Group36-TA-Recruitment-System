package profile_module.ui;

/**
 * Interface for job-related navigation callbacks.
 */
public interface JobNavigator {
    void navigateToJobs();
    void navigateToJobDetail(String jobId);
    void navigateToMyApplications();
    void navigateToApply(String jobId);
    void navigateBackToHome();
}
