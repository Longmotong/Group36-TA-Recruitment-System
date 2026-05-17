package TA_Job_Application_Module.portal.callbacks;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;

public interface PortalFlowCallbacks {
    interface JobsFlow {
        void openJobDetail(Job job);
        void openApplications();
        void returnHome();
    }

    interface JobDetailFlow {
        void backToJobs();
        void openApply(Job job);
    }

    interface ApplyFlow {
        void backToJobDetail(Job job);
        void submitSuccess();
        void draftSaved();
    }

    interface ApplicationsFlow {
        void openStatus(Application application);
        void backToHome();
        void browseJobs();
        void cancelApplication(Application application);
        void editDraft(Application draft);
    }
}
