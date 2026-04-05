

import javax.swing.*;
import java.awt.*;

/**
 * TA职位申请系统 - 主框架
 * 负责页面导航和布局
 */
public class TAPortalApp extends JFrame {
    
    // 服务和数据
    private DataService dataService;
    private TAUser currentUser;
    
    // 布局
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    
    // 页面实例
    private Page_Dashboard dashboardPage;
    private Page_Jobs jobsPage;
    private Page_JobDetail jobDetailPage;
    private JScrollPane jobDetailScroll;
    private JScrollPane applicationStatusScroll;
    private JScrollPane applyScroll;
    private Page_Apply applyPage;
    private Page_MyApplications myApplicationsPage;
    private Page_ApplicationStatus applicationStatusPage;
    private Page_Profile profilePage;
    
    // 当前选中的职位
    private Job selectedJob;

    private JButton navHomeBtn;
    private JButton navProfileBtn;
    private JButton navJobsBtn;
    
    public TAPortalApp() {
        dataService = DataService.getInstance();
        currentUser = dataService.getCurrentUser();
        
        initFrame();
        initNavigation();
        initPages();
        
        showPage("dashboard");
        setVisible(true);
    }
    
    private void initFrame() {
        setTitle("TA System - Dashboard");
        setSize(1400, 900);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UI_Constants.BG_COLOR);
        
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private void initNavigation() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(UI_Constants.CARD_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UI_Constants.BORDER_COLOR));
        navPanel.setPreferredSize(new Dimension(0, 56));
        
        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("TA System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        brandPanel.add(titleLabel);
        
        // Navigation links
        JPanel navLinksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        navLinksPanel.setOpaque(false);
        
        // 不用 emoji：JButton 默认 Segoe UI 常显示为方块，改用纯文字（可后续接图标字体/图片）
        navHomeBtn = createNavButton("Home", "dashboard");
        navProfileBtn = createNavButton("Profile Module", "profile");
        navJobsBtn = createNavButton("Job Application Module", "jobs");
        
        styleNavButtonActive(navHomeBtn);
        
        navLinksPanel.add(navHomeBtn);
        navLinksPanel.add(navProfileBtn);
        navLinksPanel.add(navJobsBtn);
        
        // Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setForeground(UI_Constants.TEXT_SECONDARY);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "确定要退出吗？", "Logout", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                dispose();
                System.exit(0);
            }
        });
        userPanel.add(logoutBtn);
        
        navPanel.add(brandPanel, BorderLayout.WEST);
        navPanel.add(navLinksPanel, BorderLayout.CENTER);
        navPanel.add(userPanel, BorderLayout.EAST);
        
        add(navPanel, BorderLayout.NORTH);
    }
    
    private JButton createNavButton(String text, String page) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleNavButtonInactive(btn);
        btn.addActionListener(e -> showPage(page));
        return btn;
    }
    
    private void styleNavButtonInactive(JButton btn) {
        btn.setForeground(UI_Constants.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBackground(null);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
    }
    
    private void styleNavButtonActive(JButton btn) {
        btn.setForeground(UI_Constants.TEXT_PRIMARY);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(UI_Constants.NAV_ACTIVE_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 11, 6, 11)
        ));
    }

    /** 根据当前页高亮顶栏：申请相关子页归入 Job 模块 */
    private void updateNavHighlight(String pageName) {
        styleNavButtonInactive(navHomeBtn);
        styleNavButtonInactive(navProfileBtn);
        styleNavButtonInactive(navJobsBtn);
        switch (pageName) {
            case "dashboard" -> styleNavButtonActive(navHomeBtn);
            case "profile" -> styleNavButtonActive(navProfileBtn);
            case "jobs", "job-detail", "apply", "applications", "status" ->
                styleNavButtonActive(navJobsBtn);
            default -> { }
        }
    }
    
    private void initPages() {
        // Dashboard Page
        dashboardPage = new Page_Dashboard(currentUser, new Page_Dashboard.NavigationCallback() {
            @Override
            public void goToProfile() { showPage("profile"); }
            @Override
            public void goToJobs() { showPage("jobs"); }
            @Override
            public void goToApplications() { showPage("applications"); }
        });
        mainContentPanel.add(dashboardPage.getPanel(), "dashboard");
        
        // Jobs Page
        jobsPage = new Page_Jobs(dataService, new Page_Jobs.JobsCallback() {
            @Override
            public void onViewJobDetail(Job job) {
                selectedJob = job;
                jobDetailPage.showJob(job);
                showPage("job-detail");
            }
            @Override
            public void onGoToApplications() { showPage("applications"); }
            @Override
            public void onGoToHome() { showPage("dashboard"); }
        });
        mainContentPanel.add(jobsPage.getPanel(), "jobs");
        
        // Job Detail Page
        jobDetailPage = new Page_JobDetail(new Page_JobDetail.JobDetailCallback() {
            @Override
            public void onBackToJobs() { showPage("jobs"); }
            @Override
            public void onApply(Job job) {
                selectedJob = job;
                applyPage.showJob(job);
                showPage("apply");
            }
        });
        jobDetailScroll = wrapContentInScrollPane(jobDetailPage.getPanel());
        mainContentPanel.add(jobDetailScroll, "job-detail");
        
        // Apply Page
        applyPage = new Page_Apply(currentUser, dataService, new Page_Apply.ApplyCallback() {
            @Override
            public void onBackToJobDetail(Job job) {
                jobDetailPage.showJob(job);
                showPage("job-detail");
            }
            @Override
            public void onSubmitSuccess() {
                myApplicationsPage.refreshTable();
                showPage("applications");
            }
        });
        applyScroll = wrapContentInScrollPane(applyPage.getPanel());
        mainContentPanel.add(applyScroll, "apply");
        
        // My Applications Page
        myApplicationsPage = new Page_MyApplications(dataService, new Page_MyApplications.MyApplicationsCallback() {
            @Override
            public void onViewStatus(Application application) {
                applicationStatusPage.showApplication(application);
                showPage("status");
            }
        });
        mainContentPanel.add(myApplicationsPage.getPanel(), "applications");
        
        // Application Status Page
        applicationStatusPage = new Page_ApplicationStatus(new Page_ApplicationStatus.StatusCallback() {
            @Override
            public void onBackToApplications() { showPage("applications"); }
        });
        applicationStatusScroll = wrapContentInScrollPane(applicationStatusPage.getPanel());
        mainContentPanel.add(applicationStatusScroll, "status");
        
        // Profile Page
        profilePage = new Page_Profile(currentUser);
        mainContentPanel.add(profilePage.getPanel(), "profile");
    }
    
    /** 长页面垂直滚动；背景与主内容区一致 */
    private static JScrollPane wrapContentInScrollPane(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(UI_Constants.BG_COLOR);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void showPage(String pageName) {
        // 刷新数据
        if (pageName.equals("applications")) {
            myApplicationsPage.refreshTable();
        } else if (pageName.equals("jobs")) {
            jobsPage.refreshJobs();
        }
        
        cardLayout.show(mainContentPanel, pageName);
        if ("job-detail".equals(pageName) && jobDetailScroll != null) {
            SwingUtilities.invokeLater(() -> jobDetailScroll.getVerticalScrollBar().setValue(0));
        }
        if ("status".equals(pageName) && applicationStatusScroll != null) {
            SwingUtilities.invokeLater(() -> applicationStatusScroll.getVerticalScrollBar().setValue(0));
        }
        if ("apply".equals(pageName) && applyScroll != null) {
            SwingUtilities.invokeLater(() -> applyScroll.getVerticalScrollBar().setValue(0));
        }
        updateNavHighlight(pageName);
    }
    
    // Main method
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new TAPortalApp());
    }
}
