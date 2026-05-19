package com.mojobsystem.ui.review;

import com.mojobsystem.service.ApplicationReviewDataService;
import com.mojobsystem.ui.MoShellHost;
import com.mojobsystem.ui.MoUiTheme;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Application Review card host: full list / filter / detail / review / records ({@link MoApplicationReviewPanel}).
 */
public class ApplicationReviewPanelHost extends JPanel {

    private final MoShellHost host;
    private final ApplicationReviewDataService applicationReviewDataService;

    public ApplicationReviewPanelHost(MoShellHost host, ApplicationReviewDataService applicationReviewDataService, String fromJobId) {
        this.host = host;
        this.applicationReviewDataService = applicationReviewDataService;
        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());
        rebuildContent(fromJobId);
    }

    public void setFromJobId(String fromJobId) {
        removeAll();
        setLayout(new BorderLayout());
        rebuildContent(fromJobId);
        revalidate();
        repaint();
    }

    private void rebuildContent(String fromJobId) {
        add(new MoApplicationReviewPanel(host, applicationReviewDataService, fromJobId), BorderLayout.CENTER);
    }
}
