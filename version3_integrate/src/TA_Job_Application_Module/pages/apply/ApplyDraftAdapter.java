package TA_Job_Application_Module.pages.apply;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;

public class ApplyDraftAdapter {
    private final ApplyFormPanel formPanel;

    public ApplyDraftAdapter(ApplyFormPanel formPanel) {
        this.formPanel = formPanel;
    }

    public void open(Job job, Application draft) {
        formPanel.openForDraft(job, draft);
    }
}
