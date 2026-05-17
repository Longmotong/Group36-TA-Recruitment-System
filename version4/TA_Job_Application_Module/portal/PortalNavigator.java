package TA_Job_Application_Module.portal;

import TA_Job_Application_Module.pages.applications.Page_MyApplications;
import TA_Job_Application_Module.pages.jobs.Page_Jobs;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;

public class PortalNavigator {
    private final CardLayout cardLayout;
    private final JPanel mainContentPanel;
    private final Page_Jobs jobsPage;
    private final Page_MyApplications myApplicationsPage;
    private final JScrollPane jobDetailScroll;
    private final JScrollPane applicationStatusScroll;
    private final JScrollPane applyScroll;
    private final Runnable onPageChanged;

    public PortalNavigator(CardLayout cardLayout,
                           JPanel mainContentPanel,
                           Page_Jobs jobsPage,
                           Page_MyApplications myApplicationsPage,
                           JScrollPane jobDetailScroll,
                           JScrollPane applicationStatusScroll,
                           JScrollPane applyScroll,
                           Runnable onPageChanged) {
        this.cardLayout = cardLayout;
        this.mainContentPanel = mainContentPanel;
        this.jobsPage = jobsPage;
        this.myApplicationsPage = myApplicationsPage;
        this.jobDetailScroll = jobDetailScroll;
        this.applicationStatusScroll = applicationStatusScroll;
        this.applyScroll = applyScroll;
        this.onPageChanged = onPageChanged;
    }

    public void showPage(String pageName) {
        if (PortalRoutes.APPLICATIONS.equals(pageName)) {
            myApplicationsPage.refreshTable();
        } else if (PortalRoutes.JOBS.equals(pageName)) {
            jobsPage.refreshJobs();
            jobsPage.restoreAIState();
        }
        cardLayout.show(mainContentPanel, pageName);
        if (PortalRoutes.JOB_DETAIL.equals(pageName) && jobDetailScroll != null) {
            SwingUtilities.invokeLater(() -> jobDetailScroll.getVerticalScrollBar().setValue(0));
        }
        if (PortalRoutes.STATUS.equals(pageName) && applicationStatusScroll != null) {
            SwingUtilities.invokeLater(() -> applicationStatusScroll.getVerticalScrollBar().setValue(0));
        }
        if (PortalRoutes.APPLY.equals(pageName) && applyScroll != null) {
            SwingUtilities.invokeLater(() -> applyScroll.getVerticalScrollBar().setValue(0));
        }
        if (onPageChanged != null) {
            onPageChanged.run();
        }
    }
}
