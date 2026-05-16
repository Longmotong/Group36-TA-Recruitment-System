package com.mojobsystem.ui;

import com.mojobsystem.DataRoot;
import com.mojobsystem.MoContext;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.JobRepository;
import com.mojobsystem.service.ApplicationReviewDataService;
import com.taapp.ui.UI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private JPanel navPanel;

    private String currentCard = CARD_DASH;

    /** Remember scope for Application Review so MO switch can rebuild the panel with fresh data. */
    private String applicationReviewFromJobId;

    private final MoDashboardPanel dashboardPanel;
    private final MyJobsPanel jobsPanel;
    private final CreateJobPanel createJobPanel;
    private final ApplicationReviewPlaceholderPanel reviewPanel;

    private JobDetailPanel jobDetailPanel;
    private TaAllocationPanel allocationPanel;
    private JobApplicantsIntegrationPlaceholderPanel jobApplicantsPlaceholderPanel;

    /** 非空时表示从认证模块嵌入打开：关闭窗口或确认退出时回调（返回登录），且不退出 JVM。 */
    private final Runnable onEndSession;
    private final AtomicBoolean sessionEnded = new AtomicBoolean(false);

    /**
     * 独立运行（{@link com.mojobsystem.App#main}）：退出时结束进程。
     */
    public MoShellFrame() {
        this(null);
    }

    /**
     * @param onEndSession 从 TA 招聘主程序登录后进入 MO 时传入；关闭窗口或 Sign out 时调用一次（通常含 {@code SessionManager.logout()} 与显示登录窗）。
     */
    public MoShellFrame(Runnable onEndSession) {
        this.onEndSession = onEndSession;
        setTitle("MO System");
        setDefaultCloseOperation(onEndSession != null ? JFrame.DISPOSE_ON_CLOSE : JFrame.EXIT_ON_CLOSE);
        MoFrameGeometry.apply(this);
        if (onEndSession != null) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    endSessionOnce();
                }
            });
        }
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(UI.palette().appBg());

        cardRoot.setOpaque(true);
        cardRoot.setBackground(UI.palette().appBg());
        dashboardPanel = new MoDashboardPanel(this);
        cardRoot.add(dashboardPanel, CARD_DASH);

        jobsPanel = new MyJobsPanel(this);
        cardRoot.add(jobsPanel, CARD_JOBS);

        createJobPanel = new CreateJobPanel(this, jobRepository);
        cardRoot.add(createJobPanel, CARD_CREATE);

        reviewPanel = new ApplicationReviewPlaceholderPanel(this, applicationReviewDataService, null);
        cardRoot.add(reviewPanel, CARD_REVIEW);

        getContentPane().add(cardRoot, BorderLayout.CENTER);
        showDashboard();
        MoFrameGeometry.finishTopLevelFrame(this);
    }

    private void setNav(NavigationPanel.Tab tab) {
        if (navPanel != null) {
            getContentPane().remove(navPanel);
        }
        String hint = MoContext.getCurrentMoUserId();
        navPanel = NavigationPanel.create(tab, navActions(),
                hint != null && !hint.isBlank() ? "Logged in: " + hint : null);
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

    private void endSessionOnce() {
        if (onEndSession == null || !sessionEnded.compareAndSet(false, true)) {
            return;
        }
        onEndSession.run();
    }

    /**
     * 嵌入模式：返回登录/首页。独立模式：重置为演示账号并留在本应用内。
     */
    private void performLogout() {
        if (onEndSession != null) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Sign out and return to the login screen?",
                    "Sign out",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            endSessionOnce();
            dispose();
            return;
        }
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
        applicationReviewFromJobId = fromJobId;
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
