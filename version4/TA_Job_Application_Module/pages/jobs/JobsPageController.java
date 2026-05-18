package TA_Job_Application_Module.pages.jobs;

import javax.swing.JPanel;

public class JobsPageController {
    private final JobsTablePanel tablePanel;
    private final JobsAiOrchestrator aiOrchestrator;

    public JobsPageController(Page_Jobs jobsPage) {
        this.tablePanel = new JobsTablePanel(jobsPage);
        this.aiOrchestrator = new JobsAiOrchestrator(jobsPage);
    }

    public JPanel view() {
        return tablePanel.view();
    }

    public void refreshJobs() {
        tablePanel.refreshJobs();
    }

    public void restoreAIState() {
        aiOrchestrator.restoreAnalysisState();
    }

    public void startBackgroundAnalysis(String apiKey) {
        aiOrchestrator.startBackgroundAnalysis(apiKey);
    }
}
