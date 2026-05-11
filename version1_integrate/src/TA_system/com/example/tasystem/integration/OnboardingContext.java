package com.example.tasystem.integration;

import com.example.tasystem.data.ProfileData;

import java.io.File;
import java.io.IOException;

/**
 * Profile onboarding wizard (from {@code profile_module}) backed by {@link taportal.TAUser} via {@link TaUserProfileMapper}.
 */
public interface OnboardingContext {

    ProfileData profile();

    void updateProfile(ProfileData next);

    /** Persist and hide onboarding; show main TA portal. */
    void completeOnboarding();

    /**
     * Copy CV into {@code data/uploads/profile_cv/{studentId}/} when student ID is set (same as job module).
     */
    default void syncCvFromPendingFile(File source) throws IOException {
        if (source == null) {
            return;
        }
    }
}
