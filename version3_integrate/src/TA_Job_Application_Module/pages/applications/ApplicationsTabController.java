package TA_Job_Application_Module.pages.applications;

import javax.swing.JPanel;

public class ApplicationsTabController {
    private final Page_MyApplications applicationsPage;

    public ApplicationsTabController(Page_MyApplications applicationsPage) {
        this.applicationsPage = applicationsPage;
    }

    public JPanel view() {
        return applicationsPage.getPanel();
    }

    public void openApplicationsTab() {
        applicationsPage.selectApplicationsTab();
    }

    public void openDraftsTab() {
        applicationsPage.selectDraftsTab();
    }

    public void refreshAll() {
        applicationsPage.refreshTable();
    }
}
