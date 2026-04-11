package com.mojobsystem.ui;

import com.mojobsystem.MoContext;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.JobRepository;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardRoot = new JPanel(cardLayout);
    private JPanel navPanel;

    private String currentCard = CARD_DASH;

    private final MoDashboardPanel dashboardPanel;
    private final MyJobsPanel jobsPanel;
    private final CreateJobPanel createJobPanel;
    private final ApplicationReviewPlaceholderPanel reviewPanel;

    private JobDetailPanel jobDetailPanel;
    private TaAllocationPanel allocationPanel;
    private JobApplicantsIntegrationPlaceholderPanel jobApplicantsPlaceholderPanel;

    public MoShellFrame() {
        setTitle("MO System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MoFrameGeometry.apply(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(MoUiTheme.PAGE_BG);

        cardRoot.setOpaque(false);
        dashboardPanel = new MoDashboardPanel(this);
        cardRoot.add(dashboardPanel, CARD_DASH);

        jobsPanel = new MyJobsPanel(this);
        cardRoot.add(jobsPanel, CARD_JOBS);

        createJobPanel = new CreateJobPanel(this, jobRepository);
        cardRoot.add(createJobPanel, CARD_CREATE);

        reviewPanel = new ApplicationReviewPlaceholderPanel(this, null);
        cardRoot.add(reviewPanel, CARD_REVIEW);

        getContentPane().add(cardRoot, BorderLayout.CENTER);
        showDashboard();
        MoFrameGeometry.finishTopLevelFrame(this);
    }

    private void setNav(NavigationPanel.Tab tab) {
        if (navPanel != null) {
            getContentPane().remove(navPanel);
        }
        navPanel = NavigationPanel.create(tab, navActions(), this::onMoSwitch);
        getContentPane().add(navPanel, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    private NavigationPanel.Actions navActions() {
        return new NavigationPanel.Actions(
                this::showDashboard,
                this::showJobList,
                () -> showApplicationReview(null),
                this::performLogout
        );
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
        MoContext.setCurrentMoUserId(MoContext.U_MO_001);
        jobDataChanged();
        showDashboard();
    }

    private void onMoSwitch() {
        jobDataChanged();
        if (CARD_DETAIL.equals(currentCard) || CARD_ALLOC.equals(currentCard) || CARD_CREATE.equals(currentCard)
                || CARD_JOB_APPLICANTS.equals(currentCard)) {
            showJobList();
        } else if (CARD_DASH.equals(currentCard)) {
            dashboardPanel.refreshOnShow();
        }
    }

    @Override
    public void showDashboard() {
        setNav(NavigationPanel.Tab.HOME);
        dashboardPanel.refreshOnShow();
        cardLayout.show(cardRoot, CARD_DASH);
        currentCard = CARD_DASH;
        setTitle("MO System - Dashboard");
    }

    @Override
    public void showJobList() {
        setNav(NavigationPanel.Tab.JOB_MANAGEMENT);
        jobDataChanged();
        cardLayout.show(cardRoot, CARD_JOBS);
        currentCard = CARD_JOBS;
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
        setNav(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_DETAIL);
        currentCard = CARD_DETAIL;
        setTitle("MO System - Job Detail");
    }

    @Override
    public void showCreateJob() {
        createJobPanel.openForCreate();
        setNav(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_CREATE);
        currentCard = CARD_CREATE;
        setTitle("MO System - Create New Job");
    }

    @Override
    public void showEditJob(Job job) {
        Job j = resolveJob(job);
        createJobPanel.openForEdit(j);
        setNav(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_CREATE);
        currentCard = CARD_CREATE;
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
        setNav(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_JOB_APPLICANTS);
        currentCard = CARD_JOB_APPLICANTS;
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
        setNav(NavigationPanel.Tab.JOB_MANAGEMENT);
        cardLayout.show(cardRoot, CARD_ALLOC);
        currentCard = CARD_ALLOC;
        setTitle("MO System - TA Allocation Results");
    }

    @Override
    public void showApplicationReview(String fromJobId) {
        reviewPanel.setFromJobId(fromJobId);
        setNav(NavigationPanel.Tab.APPLICATION_REVIEW);
        cardLayout.show(cardRoot, CARD_REVIEW);
        currentCard = CARD_REVIEW;
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
