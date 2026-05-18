package TA_Job_Application_Module.service.repositories;

import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.service.DataService;

import java.util.List;

public class JobRepository {
    private final DataService dataService;

    public JobRepository(DataService dataService) {
        this.dataService = dataService;
    }

    public List<Job> getOpenJobs() {
        return dataService.getOpenJobs();
    }

    public List<Job> getAllJobs() {
        return dataService.getJobs();
    }

    public Job getJobById(String jobId) {
        return dataService.getJobById(jobId);
    }

    public void reloadJobs() {
        dataService.reloadJobs();
    }
}
