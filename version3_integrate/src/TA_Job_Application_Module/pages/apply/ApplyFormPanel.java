package TA_Job_Application_Module.pages.apply;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;

import javax.swing.JPanel;

public class ApplyFormPanel {
    private final Page_Apply applyPage;

    public ApplyFormPanel(Page_Apply applyPage) {
        this.applyPage = applyPage;
    }

    public JPanel view() {
        return applyPage.getPanel();
    }

    public void openForJob(Job job) {
        applyPage.showJob(job);
    }

    public void openForDraft(Job job, Application draft) {
        applyPage.showJobForDraft(job, draft);
    }
}
