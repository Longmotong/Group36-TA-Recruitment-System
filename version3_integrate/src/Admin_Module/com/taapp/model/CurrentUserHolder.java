package com.taapp.model;


public class CurrentUserHolder {
    private static Authentication_Module.model.User currentUser;

    public static void setCurrentUser(Authentication_Module.model.User user) {
        currentUser = user;
    }

    public static Authentication_Module.model.User getCurrentUser() {
        return currentUser;
    }

    public static CurrentUser toAdminCurrentUser() {
        if (currentUser == null) {
            return new CurrentUser("unknown", "unknown", "unknown", "Unknown User", "unknown@example.edu");
        }
        return new CurrentUser(
            currentUser.getUsername(),      
            currentUser.getUsername(),      
            currentUser.getRole(),          
            currentUser.getUsername(),      
            currentUser.getUsername() + "@example.edu"  
        );
    }
}
