package TA_Job_Application_Module.service.workflow;

import TA_Job_Application_Module.service.DataService;

public class ApplicationWorkflowService {
    private final DataService dataService;

    public ApplicationWorkflowService(DataService dataService) {
        this.dataService = dataService;
    }

    public void cancelApplication(String applicationId) {
        dataService.cancelApplication(applicationId);
    }

    public void acceptOffer(String applicationId) {
        dataService.acceptOffer(applicationId);
    }

    public void declineOffer(String applicationId) {
        dataService.declineOffer(applicationId);
    }
}
