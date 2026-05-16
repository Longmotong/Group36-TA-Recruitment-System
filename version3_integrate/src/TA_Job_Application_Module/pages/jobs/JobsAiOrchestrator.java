package TA_Job_Application_Module.pages.jobs;

public class JobsAiOrchestrator {
    private final Page_Jobs jobsPage;

    public JobsAiOrchestrator(Page_Jobs jobsPage) {
        this.jobsPage = jobsPage;
    }

    public void restoreAnalysisState() {
        jobsPage.restoreAIState();
    }

    public void startBackgroundAnalysis(String apiKey) {
        jobsPage.startBackgroundAnalysis(apiKey);
    }
}
