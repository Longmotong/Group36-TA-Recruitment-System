package com.mojobsystem.ui;

import com.mojobsystem.model.job.Job;

import javax.swing.JFrame;

/**
 * In-window navigation for MO workspace (single {@link MoShellFrame}, no extra JFrames).
 */
public interface MoShellHost {
    void showDashboard();

    void showJobList();

    void showJobDetail(Job job);

    void showCreateJob();

    void showEditJob(Job job);

    void showTaAllocation(Job job);

    /**
     * From Job Detail → View applicants. Placeholder card for team integration (not the nav
     * Application Review screen).
     */
    void showJobApplicantsPlaceholder(Job job);

    /** {@code fromJobId} may be null when opened from the dashboard. */
    void showApplicationReview(String fromJobId);

    JFrame getShellFrame();

    /** Call after repository writes so job lists / metrics refresh. */
    void jobDataChanged();
}
