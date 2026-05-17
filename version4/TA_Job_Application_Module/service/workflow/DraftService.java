package TA_Job_Application_Module.service.workflow;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.service.DataService;

import java.util.List;

public class DraftService {
    private final DataService dataService;

    public DraftService(DataService dataService) {
        this.dataService = dataService;
    }

    public List<Application> getDrafts() {
        return dataService.getDrafts();
    }

    public String saveDraft(Application draftApp, Job job) {
        return dataService.saveDraft(draftApp, job);
    }

    public void deleteDraft(String draftId) {
        dataService.deleteDraft(draftId);
    }
}
