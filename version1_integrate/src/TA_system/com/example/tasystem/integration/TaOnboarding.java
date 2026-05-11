package com.example.tasystem.integration;

import taportal.TAUser;

/**
 * Whether the TA must complete the first-time profile wizard before the main dashboard.
 */
public final class TaOnboarding {
    private TaOnboarding() {
    }

    public static boolean needsProfileSetup(TAUser u) {
        if (u == null || u.getProfile() == null) {
            return true;
        }
        TAUser.Profile p = u.getProfile();
        if (blank(p.getFullName()) || blank(p.getStudentId()) || blank(p.getYear())
                || blank(p.getProgramMajor()) || blank(p.getPhoneNumber())) {
            return true;
        }
        if (u.getAccount() == null || blank(u.getAccount().getEmail())) {
            return true;
        }
        return false;
    }

    private static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
