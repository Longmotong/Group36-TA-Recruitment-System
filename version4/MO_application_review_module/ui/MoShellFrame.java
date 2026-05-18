package MO_system.ui;

import MO_system.DataRoot;
import MO_system.MoContext;
import MO_system.model.job.Job;
import MO_system.repository.JobRepository;
import MO_system.service.ApplicationReviewDataService;
import MO_system.ui.job.CreateJobPanel;
import MO_system.ui.job.JobApplicantsIntegrationPlaceholderPanel;
import MO_system.ui.job.JobDetailPanel;
import MO_system.ui.job.MyJobsPanel;
import MO_system.ui.job.TaAllocationPanel;
import MO_system.ui.review.ApplicationReviewPanelHost;

import profile_module.ui.TaTopNavigationPanel;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Single MO desktop window: all screens swap in place via {@link CardLayout} (no extra JFrames).
 */
public final class MoShellFrame extends JFrame implements MoShellHost {

    private static final String CARD_DASH = "dashboard";
    private static final String CARD_JOBS = "jobs";
    private static final String CARD_DETAIL = "detail";
    private static final String CARD_CREATE = "create";
    private static final String CARD_ALLOC = "alloc";
    private static final String CARD_REVIEW = "review";
    private static final String CARD_JOB_APPLICANTS = "jobApplicants";

    private final JobRepository jobRepository = new JobRepository();
    private final ApplicationReviewDataService applicationReviewDataService = new ApplicationReviewDataService(DataRoot.resolve());
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardRoot = new JPanel(cardLayout);
    private final TaTopNavigationPanel topNav;

    private final MoDashboardPanel dashboardPanel;
    private final MyJobsPanel jobsPanel;
    private final CreateJobPanel createJobPanel;
    private final ApplicationReviewPanelHost reviewPanel;

    private JobDetailPanel jobDetailPanel;
    private TaAllocationPanel allocationPanel;
    private JobApplicantsIntegrationPlaceholderPanel jobApplicantsPlaceholderPanel;
    private final Runnable logoutHandler;

    public MoShellFrame() {
        this(null);
    }

    public MoShellFrame(Runnable logoutHandler) {
        this.logoutHandler = logoutHandler;
        setTitle("MO System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MoFrameGeometry.apply(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(MoUiTheme.PAGE_BG);

        TaTopNavigationPanel.Actions moNavActions = new TaTopNavigationPanel.Actions() {
            @Override
            public void onHome() {
                MoShellFrame.this.showDashboard();
            }

            @Override
            public void onProfileModule() {
                MoShellFrame.this.showJobList();
            }

            @Override
            public void onJobApplicationModule() {
                MoShellFrame.this.showApplicationReview(null);
            }

            @Override
            public void onLogout() {
                MoShellFrame.this.performLogout();
            }
        };
        topNav = new TaTopNavigationPanel(
                moNavActions,
                this::moPortalUserLine,
                TaTopNavigationPanel.Active.MO_HOME,
                TaTopNavigationPanel.NavStyle.PORTAL_PURPLE_GRADIENT,
                TaTopNavigationPanel.PortalChromeVariant.MO_THREE);
        getContentPane().add(topNav, BorderLayout.NORTH);

        cardRoot.setOpaque(false);
        dashboardPanel = new MoDashboardPanel(this);
        cardRoot.add(dashboardPanel, CARD_DASH);

        jobsPanel = new MyJobsPanel(this);
        cardRoot.add(jobsPanel, CARD_JOBS);

        createJobPanel = new CreateJobPanel(this, jobRepository);
        cardRoot.add(createJobPanel, CARD_CREATE);

        reviewPanel = new ApplicationReviewPanelHost(this, applicationReviewDataService, null);
        cardRoot.add(reviewPanel, CARD_REVIEW);

        JPanel pageHost = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint bg = new GradientPaint(
                        0, 0, new Color(253, 252, 255),
                        0, h, new Color(248, 246, 255));
                g2.setPaint(bg);
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(109, 77, 235, 18));
                int startX = Math.max(0, w - 240);
                for (int x = startX; x < w - 18; x += 10) {
                    for (int y = 0; y < 150; y += 10) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        pageHost.setOpaque(false);
        pageHost.add(cardRoot, BorderLayout.CENTER);
        getContentPane().add(pageHost, BorderLayout.CENTER);
        showDashboard();
        MoFrameGeometry.finishTopLevelFrame(this);
    }

    private String moPortalUserLine() {
        String disp = NavigationPanel.formatMoDisplayId(MoContext.getCurrentMoUserId());
        return "Logged in: " + disp;
    }

    private void updateNavHighlight(NavigationPanel.Tab tab) {
        TaTopNavigationPanel.Active active = switch (tab) {
            case HOME -> TaTopNavigationPanel.Active.MO_HOME;
            case JOB_MANAGEMENT -> TaTopNavigationPanel.Active.MO_JOBS;
            case APPLICATION_REVIEW -> TaTopNavigationPanel.Active.MO_REVIEW;
        };
        topNav.setActive(active);
        topNav.refreshUserLabel();
    }

    /**
     * Ends the MO session without quitting the app: confirm, reset to default demo account, home screen.
     */
    private void performLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Sign out and return to the home screen?\n\n"
                        + "The application will stay open. Your session will reset to the default MO account.",
                "Sign out",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        if (logoutHandler != null) {
            logoutHandler.run();
            return;
        }
        MoContext.setCurrentMoUserId(MoContext.U_MO_001);
        jobDataChanged();
        showDashboard();
    }

    @Override
    public void showDashboard() {
        updateNavHighlight(NavigationPanel.Tab.HOME);
        dashboardPanel.refreshOnShow();
        cardLayout.show(cardRoot, CARD_DASH);
        setTitle("MO System - Dashboard");
    }

    @Override
    public void showJobList() {
        updateNavHighlight(NavigationPanel.Tab.JOB_MANAGEMENT);
        jobDataChanged();
        cardLayout.show(cardRoot, CARD_JOBS);
        setTitle("MO System - My Jobs");
    }

    @Override
    public void showJobDetail(Job job) {
        Job j = resolveJob(job);
        if (jobDetailPanel == null) {
            jobDetailPanel = new JobDetailPanel(this, jobRepository, j, this::jobDataChanged);
            cardRoot.add(jobDetailPanel, CARD_DETAIL);
        } else {
            jobDetailPanel.setJob(j);
        }
        updateNavHighlight(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_DETAIL);
        setTitle("MO System - Job Detail");
    }

    @Override
    public void showCreateJob() {
        createJobPanel.openForCreate();
        updateNavHighlight(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_CREATE);
        setTitle("MO System - Create New Job");
    }

    @Override
    public void showEditJob(Job job) {
        Job j = resolveJob(job);
        createJobPanel.openForEdit(j);
        updateNavHighlight(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_CREATE);
        setTitle("MO System - Edit Job");
    }

    @Override
    public void showJobApplicantsPlaceholder(Job job) {
        Job j = resolveJob(job);
        if (jobApplicantsPlaceholderPanel == null) {
            jobApplicantsPlaceholderPanel = new JobApplicantsIntegrationPlaceholderPanel(this, j);
            cardRoot.add(jobApplicantsPlaceholderPanel, CARD_JOB_APPLICANTS);
        } else {
            jobApplicantsPlaceholderPanel.setJob(j);
        }
        updateNavHighlight(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_JOB_APPLICANTS);
        setTitle("MO System - Applicants");
    }

    @Override
    public void showTaAllocation(Job job) {
        Job j = resolveJob(job);
        if (allocationPanel == null) {
            allocationPanel = new TaAllocationPanel(this, jobRepository, j);
            cardRoot.add(allocationPanel, CARD_ALLOC);
        } else {
            allocationPanel.setJob(jobRepository, j);
        }
        updateNavHighlight(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_ALLOC);
        setTitle("MO System - TA Allocation Results");
    }

    @Override
    public void showApplicationReview(String fromJobId) {
        reviewPanel.setFromJobId(fromJobId);
        updateNavHighlight(NavigationPanel.Tab.APPLICATION_REVIEW);
        cardLayout.show(cardRoot, CARD_REVIEW);
        setTitle("MO System - Application Review");
    }

    @Override
    public JFrame getShellFrame() {
        return this;
    }

    @Override
    public void jobDataChanged() {
        jobsPanel.reloadJobsFromRepository();
        dashboardPanel.refreshOnShow();
    }

    private Job resolveJob(Job job) {
        if (job == null || job.getId() == null || job.getId().isBlank()) {
            return job;
        }
        List<Job> all = jobRepository.loadAllJobs();
        for (Job j : all) {
            if (job.getId().equals(j.getId())) {
                return j;
            }
        }
        return job;
    }
}
