package TA_Job_Application_Module.pages.jobs;

import javax.swing.JPanel;

public class JobsTablePanel {
    private final Page_Jobs jobsPage;

    public JobsTablePanel(Page_Jobs jobsPage) {
        this.jobsPage = jobsPage;
    }

    public JPanel view() {
        return jobsPage.getPanel();
    }

    public void refreshJobs() {
        jobsPage.refreshJobs();
    }
}
