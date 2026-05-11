package com.mojobsystem;

import login.SessionManager;
import login.User;

/**
 * Application context constants.
 */
public class MoContext {
    public static String CURRENT_MO_ID = "u_mo_001";
    public static final String CURRENT_MO_NAME = "Course Coordinator";
    public static final String CURRENT_MO_EMAIL = "mo@university.edu";

    private MoContext() {
    }

    public static void init() {
        CURRENT_MO_ID = "u_mo_001";
    }

    public static void initFromSession() {
        if (SessionManager.isLoggedIn()) {
            User user = SessionManager.getCurrentUser();
            if (user != null) {
                String id = user.getMoId();
                if (id != null && !id.isBlank()) {
                    CURRENT_MO_ID = id;
                } else if (user.getUserId() != null && !user.getUserId().isBlank()) {
                    CURRENT_MO_ID = user.getUserId();
                }
            }
        }
    }

    public static void initWithUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            CURRENT_MO_ID = userId;
        }
    }
}
