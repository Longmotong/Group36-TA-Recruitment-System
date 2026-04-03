package com.mojobsystem.ui;

import com.mojobsystem.review.ApplicationReviewApp;
import javafx.application.Platform;
import javafx.stage.Stage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Coordinator that launches the JavaFX {@link ApplicationReviewApp} once
 * and toggles its Stage visibility on subsequent calls.
 */
public class ApplicationReviewPlaceholderFrame {

    private volatile JFrame parentFrame;
    private static volatile boolean launched = false;

    public static ApplicationReviewPlaceholderFrame getInstance() {
        return new ApplicationReviewPlaceholderFrame();
    }

    public static ApplicationReviewPlaceholderFrame getInstance(JFrame parent) {
        return new ApplicationReviewPlaceholderFrame(parent, null);
    }

    public ApplicationReviewPlaceholderFrame() {
        this.parentFrame = null;
    }

    public ApplicationReviewPlaceholderFrame(String fromJobId) {
        this.parentFrame = null;
    }

    public ApplicationReviewPlaceholderFrame(JFrame parentFrame, String fromJobId) {
        this.parentFrame = parentFrame;
    }

    private void ensureLaunched() {
        if (!launched) {
            launched = true;
            if (ApplicationReviewApp.getHomeCallback() == null) {
                ApplicationReviewApp.setHomeCallback(this::onHomeRequested);
                ApplicationReviewApp.setJobCallback(this::onJobRequested);
            }
            ApplicationReviewApp.launchIfNeeded();
        }
    }

    private void onHomeRequested() {
        Stage stage = ApplicationReviewApp.getSharedStage();
        if (stage != null) {
            Platform.runLater(() -> stage.hide());
        }
        final JFrame frame = parentFrame;
        if (frame == null) {
            Platform.runLater(() -> {
                JFrame active = IntegratedDashboardFrame.getActiveInstance();
                if (active != null) {
                    SwingUtilities.invokeLater(() -> active.setVisible(true));
                }
            });
        } else {
            SwingUtilities.invokeLater(() -> frame.setVisible(true));
        }
    }

    private void onJobRequested() {
        Stage stage = ApplicationReviewApp.getSharedStage();
        if (stage != null) {
            Platform.runLater(() -> stage.hide());
        }
        Platform.runLater(() -> {
            SwingUtilities.invokeLater(() -> new MyJobsFrame().setVisible(true));
        });
    }

    public void setVisible(boolean show) {
        if (show) {
            boolean isFirstLaunch = ApplicationReviewApp.getSharedStage() == null;
            ensureLaunched();
            if (parentFrame != null) {
                SwingUtilities.invokeLater(() -> parentFrame.setVisible(false));
            }
            if (isFirstLaunch) {
                return;
            }
            Platform.runLater(() -> {
                if (ApplicationReviewApp.isPendingMyReviewRecords()) {
                    ApplicationReviewApp.showMyReviewRecordsView();
                    ApplicationReviewApp.clearPendingMyReviewRecords();
                }
                Stage stage = ApplicationReviewApp.getSharedStage();
                if (stage != null) {
                    stage.show();
                    stage.toFront();
                }
            });
        } else {
            Platform.runLater(() -> {
                ApplicationReviewApp.showApplicationReviewView();
                Stage stage = ApplicationReviewApp.getSharedStage();
                if (stage != null) {
                    stage.hide();
                }
            });
        }
    }
}
