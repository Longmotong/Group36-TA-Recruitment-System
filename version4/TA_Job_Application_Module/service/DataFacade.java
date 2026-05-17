package TA_Job_Application_Module.service;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.model.TAUser;
import TA_Job_Application_Module.service.repositories.ApplicationRepository;
import TA_Job_Application_Module.service.repositories.JobRepository;
import TA_Job_Application_Module.service.repositories.UserRepository;
import TA_Job_Application_Module.service.workflow.ApplicationWorkflowService;
import TA_Job_Application_Module.service.workflow.DraftService;

import java.util.List;

public class DataFacade {
    private final DataService dataService;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationWorkflowService workflowService;
    private final DraftService draftService;

    public DataFacade(DataService dataService) {
        this.dataService = dataService;
        this.userRepository = new UserRepository(dataService);
        this.jobRepository = new JobRepository(dataService);
        this.applicationRepository = new ApplicationRepository(dataService);
        this.workflowService = new ApplicationWorkflowService(dataService);
        this.draftService = new DraftService(dataService);
    }

    public DataService raw() {
        return dataService;
    }

    public TAUser currentUser() {
        return userRepository.getCurrentUser();
    }

    public List<Job> openJobs() {
        return jobRepository.getOpenJobs();
    }

    public Job findJob(String jobId) {
        return jobRepository.getJobById(jobId);
    }

    public List<Application> userApplications() {
        return applicationRepository.getUserApplications();
    }

    public List<Application> drafts() {
        return draftService.getDrafts();
    }

    public void submitApplication(Application app) {
        applicationRepository.addApplication(app);
    }

    public String saveDraft(Application draft, Job job) {
        return draftService.saveDraft(draft, job);
    }

    public void deleteDraft(String draftId) {
        draftService.deleteDraft(draftId);
    }

    public void cancelApplication(String appId) {
        workflowService.cancelApplication(appId);
    }

    public void acceptOffer(String appId) {
        workflowService.acceptOffer(appId);
    }

    public void declineOffer(String appId) {
        workflowService.declineOffer(appId);
    }
}
