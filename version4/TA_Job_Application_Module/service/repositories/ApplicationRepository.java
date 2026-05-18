package TA_Job_Application_Module.service.repositories;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.service.DataService;

import java.util.List;

public class ApplicationRepository {
    private final DataService dataService;

    public ApplicationRepository(DataService dataService) {
        this.dataService = dataService;
    }

    public List<Application> getUserApplications() {
        return dataService.getUserApplications();
    }

    public List<Application> getDrafts() {
        return dataService.getDrafts();
    }

    public void addApplication(Application application) {
        dataService.addApplication(application);
    }

    public String saveDraft(Application draftApp, Job job) {
        return dataService.saveDraft(draftApp, job);
    }

    public void deleteDraft(String draftId) {
        dataService.deleteDraft(draftId);
    }

    public boolean hasAppliedToJob(String jobId) {
        return dataService.hasAppliedToJob(jobId);
    }
}
