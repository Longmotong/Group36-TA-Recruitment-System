package TA_Job_Application_Module.service.repositories;

import TA_Job_Application_Module.model.TAUser;
import TA_Job_Application_Module.service.DataService;

public class UserRepository {
    private final DataService dataService;

    public UserRepository(DataService dataService) {
        this.dataService = dataService;
    }

    public TAUser getCurrentUser() {
        return dataService.getCurrentUser();
    }

    public TAUser loadUserData(String username, String role) {
        return dataService.loadUserData(username, role);
    }

    public void saveCurrentUser() {
        dataService.saveCurrentUserToFile();
    }
}
