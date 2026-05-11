package taportal;

import com.example.tasystem.integration.OnboardingContext;
import com.example.tasystem.integration.TaOnboarding;
import com.example.tasystem.integration.TaPortalHost;
import com.example.tasystem.integration.TaUserProfileMapper;
import com.example.tasystem.data.ProfileData;
import com.example.tasystem.ui.screens.EditProfileScreen;
import com.example.tasystem.ui.screens.EditSkillsScreen;
import com.example.tasystem.ui.screens.ManageCvScreen;
import com.example.tasystem.ui.screens.OnboardingScreen;
import com.example.tasystem.ui.screens.ProfileScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * TA职位申请系统 - Swing桌面应用
 */
public class TAPortalApp extends JFrame implements TaPortalHost, OnboardingContext {

    private static final String OUTER_ONBOARDING = "onboarding";
    private static final String OUTER_APP = "app";

    /** When non-null (integrated app), closing the portal returns to login instead of exiting the JVM. */
    private final Runnable afterLogout;

    private DataService dataService;
    private TAUser currentUser;

    private CardLayout outerCardLayout;
    private JPanel outerRoot;
    /** Main app shell: north = nav, center = {@link #mainContentPanel}. */
    private JPanel appShell;

    private CardLayout profileCardLayout;
    private JPanel profileRoot;
    private ProfileScreen profileScreenPanel;
    private EditProfileScreen editProfileScreenPanel;
    private EditSkillsScreen editSkillsScreenPanel;
    private ManageCvScreen manageCvScreenPanel;

    private JLabel dashQuickProfileVal;
    private JLabel dashQuickCvVal;
    private JLabel dashQuickAppsVal;
    
    // Main container
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel navLinksPanel;
    private JButton homeBtn;
    private JButton profileNavBtn;
    private JButton jobsNavBtn;
    
    // Page panels
    private Map<String, JPanel> pages = new HashMap<>();
    
    // Color palette
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private static final Color PRIMARY_HOVER = new Color(67, 56, 202);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    private static final Color BG_COLOR = new Color(249, 250, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    /** 原型中的深色主按钮（近黑） */
    private static final Color DARK_BUTTON = new Color(31, 41, 55);
    private static final Color DARK_BUTTON_HOVER = new Color(17, 24, 39);
    private static final Color NAV_ACTIVE_BG = new Color(243, 244, 246);
    
    public TAPortalApp() {
        this(null);
    }

    public TAPortalApp(Runnable afterLogout) {
        this.afterLogout = afterLogout;
        dataService = DataService.getInstance();
        currentUser = dataService.getCurrentUser();

        initFrame();
        initNavigation();
        initDashboard();
        initJobsPage();
        initJobDetailPage();
        initApplyPage();
        initApplicationsPage();
        initStatusPage();
        initProfileModule();
        initOnboardingShell();

        if (TaOnboarding.needsProfileSetup(dataService.getCurrentUser())) {
            outerCardLayout.show(outerRoot, OUTER_ONBOARDING);
        } else {
            outerCardLayout.show(outerRoot, OUTER_APP);
            showPage("dashboard");
        }
        setVisible(true);
    }

    private void initOnboardingShell() {
        outerCardLayout = new CardLayout();
        outerRoot = new JPanel(outerCardLayout);
        JPanel onboardingWrap = new JPanel(new BorderLayout());
        onboardingWrap.add(new OnboardingScreen(this), BorderLayout.CENTER);
        outerRoot.add(onboardingWrap, OUTER_ONBOARDING);
        outerRoot.add(appShell, OUTER_APP);
        add(outerRoot, BorderLayout.CENTER);
    }
    
    private void initFrame() {
        setTitle("TA System - Dashboard");
        setSize(1400, 900);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());

        appShell = new JPanel(new BorderLayout());
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_COLOR);
        appShell.add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private void initNavigation() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(CARD_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        navPanel.setPreferredSize(new Dimension(0, 64));
        
        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        JLabel logoLabel = new JLabel("\u2709"); // Mail icon
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        logoLabel.setForeground(PRIMARY_COLOR);
        brandPanel.add(logoLabel);
        
        JLabel titleLabel = new JLabel("TA System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        brandPanel.add(titleLabel);
        
        // Navigation links
        navLinksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        navLinksPanel.setOpaque(false);
        
        homeBtn = createNavButton("Home", "dashboard");
        profileNavBtn = createNavButton("Profile Module", "profile");
        jobsNavBtn = createNavButton("Job Application Module", "jobs");
        
        styleNavButtonActive(homeBtn);
        
        navLinksPanel.add(homeBtn);
        navLinksPanel.add(profileNavBtn);
        navLinksPanel.add(jobsNavBtn);
        
        // Logout（与原型一致）
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        JButton logoutBtn = new JButton("\u2192 Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(TEXT_SECONDARY);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> confirmLogoutAndClose());
        userPanel.add(logoutBtn);
        
        navPanel.add(brandPanel, BorderLayout.WEST);
        navPanel.add(navLinksPanel, BorderLayout.CENTER);
        navPanel.add(userPanel, BorderLayout.EAST);
        
        appShell.add(navPanel, BorderLayout.NORTH);
    }
    
    private JButton createNavButton(String text, String page) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleNavButtonInactive(btn);
        btn.addActionListener(e -> showPage(page));
        return btn;
    }
    
    private void updateNavButtons(JButton activeBtn) {
        for (Component c : navLinksPanel.getComponents()) {
            if (c instanceof JButton btn) {
                if (btn == activeBtn) {
                    styleNavButtonActive(btn);
                } else {
                    styleNavButtonInactive(btn);
                }
            }
        }
    }
    
    private void styleNavButtonInactive(JButton btn) {
        btn.setForeground(TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBackground(null);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }
    
    private void styleNavButtonActive(JButton btn) {
        btn.setForeground(TEXT_PRIMARY);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(NAV_ACTIVE_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(9, 15, 9, 15)
        ));
    }
    
    // ==================== DASHBOARD PAGE ====================
    private void initDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BG_COLOR);
        dashboard.setBorder(new EmptyBorder(24, 48, 32, 48));
        pages.put("dashboard", dashboard);
        mainContentPanel.add(dashboard, "dashboard");
        
        // 顶部标题（靠左，与原型一致）
        JPanel topTitle = new JPanel(new BorderLayout());
        topTitle.setOpaque(false);
        JLabel titleMain = new JLabel("TA Dashboard");
        titleMain.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleMain.setForeground(TEXT_PRIMARY);
        topTitle.add(titleMain, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Welcome! Please select a function module to get started.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(8, 0, 0, 0));
        topTitle.add(subtitle, BorderLayout.SOUTH);
        dashboard.add(topTitle, BorderLayout.NORTH);
        
        // 中部：两个功能模块在可视区域水平+垂直居中
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        cardsRow.setOpaque(false);
        
        // Profile 模块卡片
        JPanel profileCard = createDashboardModuleCard(
            "\uD83D\uDC64",
            "Profile Module",
            "Manage personal information, skills, and CV",
            new Color(219, 234, 254),
            INFO_COLOR
        );
        JButton goProfile = createDarkPrimaryButton("Go to Profile");
        goProfile.setPreferredSize(new Dimension(300, 44));
        goProfile.addActionListener(e -> showPage("profile"));
        JPanel profileBtnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        profileBtnWrap.setOpaque(false);
        profileBtnWrap.add(goProfile);
        profileCard.add(profileBtnWrap, BorderLayout.SOUTH);
        cardsRow.add(profileCard);
        
        // Job Application 模块卡片
        JPanel jobCard = createDashboardModuleCard(
            "\uD83D\uDCBC",
            "Job Application Module",
            "Browse jobs, apply, and track application status",
            new Color(209, 250, 229),
            SUCCESS_COLOR
        );
        // 使用 GridLayout 并排固定两列，避免 FlowLayout 换行后第二行被裁切；Windows L&F 下按钮需 opaque 才绘制背景
        JPanel jobActions = new JPanel(new GridLayout(1, 2, 12, 0));
        jobActions.setOpaque(false);
        JButton browseJobsBtn = createDarkPrimaryButton("Browse Jobs");
        browseJobsBtn.addActionListener(e -> showPage("jobs"));
        JButton myAppsBtn = createOutlineButton("My Applications");
        myAppsBtn.addActionListener(e -> showPage("applications"));
        jobActions.add(browseJobsBtn);
        jobActions.add(myAppsBtn);
        jobCard.add(jobActions, BorderLayout.SOUTH);
        // Job 卡片略加宽，容纳两个按钮文案（高 DPI 下更稳）
        jobCard.setPreferredSize(new Dimension(420, 280));
        jobCard.setMinimumSize(new Dimension(400, 260));
        cardsRow.add(jobCard);
        
        centerWrap.add(cardsRow, gbc);
        dashboard.add(centerWrap, BorderLayout.CENTER);
        
        // 底部 Quick Status Overview
        JPanel statusSection = new JPanel();
        statusSection.setLayout(new BoxLayout(statusSection, BoxLayout.Y_AXIS));
        statusSection.setOpaque(false);
        statusSection.setBorder(new EmptyBorder(16, 0, 0, 0));
        
        JLabel statusTitle = new JLabel("Quick Status Overview");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusTitle.setForeground(TEXT_PRIMARY);
        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        statusSection.add(statusTitle);
        
        JPanel statusRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statusRow.setOpaque(false);
        dashQuickProfileVal = new JLabel();
        dashQuickCvVal = new JLabel();
        dashQuickAppsVal = new JLabel();
        statusRow.add(createQuickStatusItem("\u2713", SUCCESS_COLOR, "Profile Completion", dashQuickProfileVal));
        statusRow.add(createQuickStatusItem("\u2191", INFO_COLOR, "CV Upload Status", dashQuickCvVal));
        statusRow.add(createQuickStatusItem("\uD83D\uDCC4", PRIMARY_COLOR, "Number of Applications", dashQuickAppsVal));
        statusSection.add(statusRow);
        
        dashboard.add(statusSection, BorderLayout.SOUTH);

        refreshDashboardQuickStatus();
    }
    
    /** 仪表盘功能模块卡片（固定宽度，白底描边） */
    private JPanel createDashboardModuleCard(String icon, String title, String desc,
            Color iconBg, Color iconFg) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(28, 28, 28, 28)
        ));
        card.setPreferredSize(new Dimension(380, 260));
        card.setMaximumSize(new Dimension(420, 320));
        
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        iconPanel.setPreferredSize(new Dimension(52, 52));
        iconPanel.setLayout(new GridBagLayout());
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconLbl.setForeground(iconFg);
        iconPanel.add(iconLbl);
        header.add(iconPanel);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(TEXT_PRIMARY);
        header.add(titleLbl);
        card.add(header, BorderLayout.NORTH);
        
        JLabel descLbl = new JLabel("<html><div style='width:300px'>" + desc + "</div></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLbl.setForeground(TEXT_SECONDARY);
        card.add(descLbl, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createQuickStatusItem(String icon, Color iconColor, String label, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(12, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setForeground(iconColor);
        p.add(iconLbl, BorderLayout.WEST);
        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_PRIMARY);
        text.add(valueLabel, BorderLayout.NORTH);
        JLabel lab = new JLabel(label);
        lab.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lab.setForeground(TEXT_SECONDARY);
        text.add(lab, BorderLayout.SOUTH);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private void refreshDashboardQuickStatus() {
        currentUser = dataService.getCurrentUser();
        dataService.syncApplicationSummaryFromApplications();
        if (dashQuickProfileVal != null) {
            dashQuickProfileVal.setText(currentUser.getProfileCompletion() + "%");
        }
        if (dashQuickCvVal != null) {
            TAUser.CV cv = currentUser.getCv();
            dashQuickCvVal.setText(cv != null && cv.isUploaded() ? "Uploaded" : "Not Uploaded");
        }
        if (dashQuickAppsVal != null) {
            dashQuickAppsVal.setText(String.valueOf(dataService.getUserApplications().size()));
        }
    }
    
    private JButton createDarkPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(DARK_BUTTON);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(DARK_BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(DARK_BUTTON);
            }
        });
        return btn;
    }
    
    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_COLOR);               // 浅灰背景，与卡片白底区分开
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_COLOR);
                btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_COLOR);
                btn.setForeground(TEXT_PRIMARY);
            }
        });
        return btn;
    }
    
    // ==================== JOBS PAGE ====================
    private JPanel jobsPage;
    private JTextField searchField;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> jobTypeFilter;
    private JPanel jobsListPanel;
    private List<Job> filteredJobs;
    private JLabel jobListCountLabel;
    
    private void initJobsPage() {
        jobsPage = new JPanel(new BorderLayout(0, 0));
        jobsPage.setBackground(BG_COLOR);
        jobsPage.setBorder(new EmptyBorder(16, 48, 32, 48));
        pages.put("jobs", jobsPage);
        mainContentPanel.add(jobsPage, "jobs");
        filteredJobs = new ArrayList<>(dataService.getOpenJobs());
        
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        
        // ← Back to Home
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        JButton backHome = new JButton("\u2190 Back to Home");
        backHome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backHome.setForeground(TEXT_SECONDARY);
        backHome.setContentAreaFilled(false);
        backHome.setBorder(new EmptyBorder(0, 0, 16, 0));
        backHome.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backHome.addActionListener(e -> showPage("dashboard"));
        backRow.add(backHome);
        northStack.add(backRow);
        
        // 标题行：左标题 + 右 My Applications（白底描边）
        JPanel titleRow = new JPanel(new BorderLayout(24, 0));
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 20, 0));
        JPanel titleLeft = new JPanel(new BorderLayout(0, 6));
        titleLeft.setOpaque(false);
        JLabel titleLabel = new JLabel("Available Jobs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLeft.add(titleLabel, BorderLayout.NORTH);
        JLabel subtitleLabel = new JLabel("Browse all open TA positions.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        titleLeft.add(subtitleLabel, BorderLayout.SOUTH);
        titleRow.add(titleLeft, BorderLayout.WEST);
        JButton myAppsBtn = createJobsPageOutlineButton("\uD83D\uDCCB  My Applications");
        myAppsBtn.addActionListener(e -> showPage("applications"));
        JPanel myAppsWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        myAppsWrap.setOpaque(false);
        myAppsWrap.add(myAppsBtn);
        titleRow.add(myAppsWrap, BorderLayout.EAST);
        northStack.add(titleRow);
        
        // 搜索与筛选：白底卡片一行
        JPanel searchCard = new JPanel(new BorderLayout(16, 0));
        searchCard.setBackground(CARD_BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(14, 18, 14, 18)
        ));
        
        JPanel searchWithIcon = new JPanel(new BorderLayout(10, 0));
        searchWithIcon.setOpaque(false);
        JLabel magIcon = new JLabel("\uD83D\uDD0D");
        magIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        magIcon.setForeground(TEXT_SECONDARY);
        magIcon.setBorder(new EmptyBorder(0, 4, 0, 0));
        searchWithIcon.add(magIcon, BorderLayout.WEST);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        searchField.putClientProperty("JTextField.placeholderText", "Search by job title or course...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterJobs(); }
            public void removeUpdate(DocumentEvent e) { filterJobs(); }
            public void insertUpdate(DocumentEvent e) { filterJobs(); }
        });
        searchWithIcon.add(searchField, BorderLayout.CENTER);
        
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        filters.setOpaque(false);
        JLabel funnel = new JLabel("\u25BC ");
        funnel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        funnel.setForeground(TEXT_SECONDARY);
        filters.add(funnel);
        departmentFilter = new JComboBox<>(new String[]{"All Departments", "Computer Science", "Mathematics", "Physics", "Chemistry", "Biology"});
        styleJobsFilterCombo(departmentFilter);
        departmentFilter.addActionListener(e -> filterJobs());
        filters.add(departmentFilter);
        jobTypeFilter = new JComboBox<>(new String[]{"All Job Types", "TA", "Lab TA", "Grading TA", "Part-time TA"});
        styleJobsFilterCombo(jobTypeFilter);
        jobTypeFilter.addActionListener(e -> filterJobs());
        filters.add(jobTypeFilter);
        
        searchCard.add(searchWithIcon, BorderLayout.CENTER);
        searchCard.add(filters, BorderLayout.EAST);
        northStack.add(searchCard);
        
        jobListCountLabel = new JLabel(" ");
        jobListCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobListCountLabel.setForeground(TEXT_SECONDARY);
        jobListCountLabel.setBorder(new EmptyBorder(12, 4, 16, 0));
        jobListCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        northStack.add(jobListCountLabel);
        
        jobsPage.add(northStack, BorderLayout.NORTH);
        
        jobsListPanel = new JPanel();
        jobsListPanel.setLayout(new BoxLayout(jobsListPanel, BoxLayout.Y_AXIS));
        jobsListPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(jobsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        jobsPage.add(scrollPane, BorderLayout.CENTER);
        
        refreshJobsList();
    }
    
    /** 职位列表页右上角「My Applications」白底按钮 */
    private JButton createJobsPageOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(CARD_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 18, 10, 18)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BG_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(CARD_BG);
            }
        });
        return btn;
    }
    
    private void styleJobsFilterCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(CARD_BG);
        combo.setPreferredSize(new Dimension(168, 36));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
    }
    
    private void filterJobs() {
        filteredJobs.clear();
        String search = searchField.getText().toLowerCase();
        String dept = (String) departmentFilter.getSelectedItem();
        String type = (String) jobTypeFilter.getSelectedItem();
        
        for (Job job : dataService.getOpenJobs()) {
            boolean match = true;
            
            if (!search.isEmpty()) {
                String title = job.getTitle().toLowerCase();
                String course = job.getCourseCode().toLowerCase();
                if (!title.contains(search) && !course.contains(search)) {
                    match = false;
                }
            }
            
            if (dept != null && !dept.equals("All Departments")) {
                if (!job.getDepartment().equals(dept)) {
                    match = false;
                }
            }
            
            if (type != null && !type.equals("All Job Types")) {
                if (!job.getEmploymentType().contains(type)) {
                    match = false;
                }
            }
            
            if (match) {
                filteredJobs.add(job);
            }
        }
        
        refreshJobsList();
    }
    
    private void refreshJobsList() {
        jobsListPanel.removeAll();
        
        int total = dataService.getOpenJobs().size();
        int shown = filteredJobs.size();
        if (jobListCountLabel != null) {
            jobListCountLabel.setText("Showing " + shown + " of " + total + " positions");
        }
        
        if (filteredJobs.isEmpty()) {
            JLabel emptyLabel = new JLabel("No jobs found matching your criteria");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(CENTER_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            jobsListPanel.add(emptyLabel);
        } else {
            for (Job job : filteredJobs) {
                jobsListPanel.add(createJobCard(job));
                jobsListPanel.add(Box.createVerticalStrut(20));
            }
        }
        
        jobsListPanel.revalidate();
        jobsListPanel.repaint();
    }
    
    /** 截止日期展示为 March 25, 2026 */
    private String formatDeadlinePretty(Job job) {
        String raw = job.getDeadlineDisplay();
        if (raw == null || raw.length() < 10) {
            return raw != null ? raw : "";
        }
        String ymd = raw.substring(0, 10);
        String[] p = ymd.split("-");
        if (p.length != 3) {
            return "Deadline: " + raw;
        }
        String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        try {
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            if (m >= 1 && m <= 12) {
                return months[m - 1] + " " + d + ", " + p[0];
            }
        } catch (NumberFormatException ignored) { }
        return raw;
    }
    
    /**
     * 职位卡片：左（标题 / 课程·系·教师 / 摘要 / 底部图标行），右深色「View Details &gt;」垂直居中
     */
    private JPanel createJobCard(Job job) {
        JPanel card = new JPanel(new BorderLayout(28, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(22, 26, 22, 26)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        
        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titleLabel);
        
        left.add(Box.createVerticalStrut(6));
        String meta = job.getCourseCode() + "  \u2022  " + job.getDepartment() + "  \u2022  " + job.getInstructorName();
        JLabel metaLabel = new JLabel(meta);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaLabel.setForeground(TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(metaLabel);
        
        left.add(Box.createVerticalStrut(8));
        String summary = job.getSummary();
        if (summary == null || summary.isEmpty()) {
            summary = job.getDescription();
        }
        if (summary != null && summary.length() > 120) {
            summary = summary.substring(0, 117) + "...";
        }
        JLabel sumLabel = new JLabel("<html><div style='width:720px'>" + (summary != null ? summary : "") + "</div></html>");
        sumLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sumLabel.setForeground(new Color(75, 85, 99));
        sumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(sumLabel);
        
        left.add(Box.createVerticalStrut(14));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(createJobMetaChip("\u23F0", job.getWeeklyHoursDisplay()));
        footer.add(createJobMetaChip("\uD83D\uDCC5", "Deadline: " + formatDeadlinePretty(job)));
        footer.add(createJobMetaChip("\uD83D\uDCCD", job.getLocationMode()));
        left.add(footer);
        
        card.add(left, BorderLayout.CENTER);
        
        JButton viewBtn = createDarkPrimaryButton("View Details  >");
        viewBtn.setPreferredSize(new Dimension(160, 44));
        viewBtn.addActionListener(e -> showJobDetail(job));
        JPanel btnCol = new JPanel(new GridBagLayout());
        btnCol.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        btnCol.add(viewBtn, c);
        card.add(btnCol, BorderLayout.EAST);
        
        return card;
    }
    
    private JLabel createJobMetaChip(String icon, String text) {
        JLabel l = new JLabel(icon + "  " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }
    
    // ==================== JOB DETAIL PAGE ====================
    private void initJobDetailPage() {
        JPanel page = createPagePanel();
        pages.put("job-detail", page);
        mainContentPanel.add(page, "job-detail");
    }
    
    private void showJobDetail(Job job) {
        JPanel page = pages.get("job-detail");
        page.removeAll();
        
        // Back button
        JButton backBtn = new JButton("\u2190 Back to Jobs");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 20, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> showPage("jobs"));
        page.add(backBtn);
        
        // Main container
        JPanel content = new JPanel(new BorderLayout(30, 0));
        content.setOpaque(false);
        
        // Left column - Job Info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        
        JLabel title = new JLabel(job.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        leftPanel.add(title);
        
        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        tags.setOpaque(false);
        tags.add(createTag(job.getCourseCode(), new Color(219, 234, 254), INFO_COLOR));
        tags.add(Box.createHorizontalStrut(8));
        tags.add(createTag(job.getDepartment(), new Color(209, 250, 229), SUCCESS_COLOR));
        leftPanel.add(tags);
        
        // Instructor
        addSection(leftPanel, "Instructor", job.getInstructorName(), TEXT_PRIMARY);
        addSection(leftPanel, "Email", job.getInstructorEmail(), TEXT_SECONDARY);
        
        // Description
        addSection(leftPanel, "Job Description", job.getDescription(), TEXT_PRIMARY);
        
        // Responsibilities
        JLabel respTitle = new JLabel("Responsibilities");
        respTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        respTitle.setForeground(TEXT_PRIMARY);
        respTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        leftPanel.add(respTitle);
        
        for (String resp : job.getResponsibilities()) {
            JLabel item = new JLabel("\u2022 " + resp);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.setForeground(TEXT_PRIMARY);
            item.setBorder(new EmptyBorder(3, 0, 3, 0));
            leftPanel.add(item);
        }
        
        // Requirements
        JLabel reqTitle = new JLabel("Requirements");
        reqTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reqTitle.setForeground(TEXT_PRIMARY);
        reqTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        leftPanel.add(reqTitle);
        
        for (String req : job.getRequirements()) {
            JLabel item = new JLabel("\u2022 " + req);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.setForeground(TEXT_PRIMARY);
            item.setBorder(new EmptyBorder(3, 0, 3, 0));
            leftPanel.add(item);
        }
        
        // Preferred Skills
        JLabel skillsTitle = new JLabel("Preferred Skills");
        skillsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        skillsTitle.setForeground(TEXT_PRIMARY);
        skillsTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        leftPanel.add(skillsTitle);
        
        JPanel skillsTags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        skillsTags.setOpaque(false);
        for (String skill : job.getPreferredSkills()) {
            skillsTags.add(createSkillTag(skill));
            skillsTags.add(Box.createHorizontalStrut(8));
        }
        leftPanel.add(skillsTags);
        
        content.add(leftPanel, BorderLayout.CENTER);
        
        // Right column - Summary
        JPanel summaryPanel = createCard("");
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel summaryTitle = new JLabel("Position Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryTitle.setForeground(TEXT_PRIMARY);
        summaryTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        summaryPanel.add(summaryTitle);
        
        addSummaryItem(summaryPanel, "Employment Type", job.getEmploymentType());
        addSummaryItem(summaryPanel, "Weekly Hours", job.getWeeklyHoursDisplay());
        addSummaryItem(summaryPanel, "Application Deadline", job.getDeadlineDisplay());
        addSummaryItem(summaryPanel, "Work Mode", job.getLocationMode());
        
        JButton applyBtn = createPrimaryButton("Apply Now");
        applyBtn.setPreferredSize(new Dimension(0, 45));
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        applyBtn.addActionListener(e -> showApplyPage(job));
        summaryPanel.add(Box.createVerticalStrut(20));
        summaryPanel.add(applyBtn);
        
        content.add(summaryPanel, BorderLayout.EAST);
        content.setPreferredSize(new Dimension(0, 600));
        
        page.add(content);
        
        showPage("job-detail");
    }
    
    private void addSection(JPanel panel, String label, String value, Color valueColor) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(12, 0, 5, 0));
        panel.add(lbl);
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(valueColor);
        panel.add(val);
    }
    
    private void addSummaryItem(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 8, 0));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(TEXT_PRIMARY);
        row.add(val, BorderLayout.EAST);
        
        panel.add(row);
    }
    
    // ==================== APPLY PAGE ====================
    private void initApplyPage() {
        JPanel page = createPagePanel();
        pages.put("apply", page);
        mainContentPanel.add(page, "apply");
    }
    
    private void showApplyPage(Job job) {
        JPanel page = pages.get("apply");
        page.removeAll();
        page.setLayout(new BorderLayout(0, 0));
        
        // Top section: Back button and title
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.setBorder(new EmptyBorder(0, 0, 16, 0));
        
        // Back button
        JButton backBtn = new JButton("\u2190 Back to Job Details");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 12, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> showJobDetail(job));
        topSection.add(backBtn);
        
        // Page title
        JLabel title = new JLabel("Apply for Job");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(title);
        
        page.add(topSection, BorderLayout.NORTH);
        
        // Scrollable content
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        scrollContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // Two-column layout container
        JPanel content = new JPanel(new BorderLayout(30, 0));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // ========== LEFT COLUMN: Application Form ==========
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Info banner
        JPanel hintCard = createCard("");
        hintCard.setLayout(new BorderLayout(12, 0));
        hintCard.setBackground(new Color(239, 246, 255));
        hintCard.setOpaque(true);
        hintCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(191, 219, 254)),
            new EmptyBorder(14, 16, 14, 16)
        ));
        hintCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel hintIcon = new JLabel("\u2139");
        hintIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hintIcon.setForeground(INFO_COLOR);
        hintCard.add(hintIcon, BorderLayout.WEST);
        JLabel hintText = new JLabel("Your profile data, skills, and CV have been auto-filled from your saved profile. You may modify any information before submitting.");
        hintText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hintText.setForeground(TEXT_SECONDARY);
        hintCard.add(hintText, BorderLayout.CENTER);
        leftPanel.add(hintCard);
        
        leftPanel.add(Box.createVerticalStrut(16));
        
        // Application Information card
        JPanel appInfoCard = createCard("");
        appInfoCard.setLayout(new BoxLayout(appInfoCard, BoxLayout.Y_AXIS));
        appInfoCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        appInfoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        JLabel sectionTitle = new JLabel("Application Information");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(sectionTitle);
        
        // Two-column grid for basic info
        JPanel grid = new JPanel(new GridLayout(3, 2, 18, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextField fullNameField = createEditableField(currentUser.getProfile().getFullName());
        JTextField studentIdField = createEditableField(currentUser.getProfile().getStudentId());
        JTextField emailField = createEditableField(currentUser.getAccount().getEmail());
        JTextField phoneField = createEditableField(currentUser.getProfile().getPhoneNumber());
        JTextField programField = createEditableField(currentUser.getProfile().getProgramMajor());
        JTextField gpaField = createEditableField(String.valueOf(currentUser.getAcademic().getGpa()));
        
        // Larger text fields
        fullNameField.setPreferredSize(new Dimension(0, 48));
        studentIdField.setPreferredSize(new Dimension(0, 48));
        emailField.setPreferredSize(new Dimension(0, 48));
        phoneField.setPreferredSize(new Dimension(0, 48));
        programField.setPreferredSize(new Dimension(0, 48));
        gpaField.setPreferredSize(new Dimension(0, 48));
        
        grid.add(createEditableFieldPanel("Full Name *", fullNameField));
        grid.add(createEditableFieldPanel("Student ID *", studentIdField));
        grid.add(createEditableFieldPanel("Email *", emailField));
        grid.add(createEditableFieldPanel("Phone Number *", phoneField));
        grid.add(createEditableFieldPanel("Program / Major *", programField));
        grid.add(createEditableFieldPanel("GPA (Optional)", gpaField));
        appInfoCard.add(grid);
        
        appInfoCard.add(Box.createVerticalStrut(16));
        
        // Single-column detailed fields
        JTextArea skillsArea = createTextArea("", 4);
        JPanel skillsWrap = createLabeledArea("Relevant Skills *", skillsArea,
            "e.g., Java, Python, Data Structures, Machine Learning");
        skillsWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!formatUserSkills(currentUser).isEmpty()) {
            skillsArea.setText(formatUserSkills(currentUser));
        }
        appInfoCard.add(skillsWrap);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        JTextArea experienceArea = createTextArea("", 5);
        JPanel expWrap = createLabeledArea("Relevant Experience *", experienceArea,
            "Describe your relevant work experience, TA/grading experience, or projects...");
        expWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(expWrap);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        JTextArea availabilityArea = createTextArea("", 3);
        JPanel availWrap = createLabeledArea("Availability *", availabilityArea,
            "e.g., Monday/Wednesday 10am-12pm, Tuesday 2pm-4pm");
        availWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(availWrap);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        JTextArea motivationArea = createTextArea("", 6);
        JPanel motivWrap = createLabeledArea("Motivation / Cover Letter *", motivationArea,
            "Explain why you are interested in this TA position and what makes you a good fit...");
        motivWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(motivWrap);
        
        appInfoCard.add(Box.createVerticalStrut(18));
        
        // Resume / CV upload
        JLabel resumeTitle = new JLabel("Resume / CV *");
        resumeTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resumeTitle.setForeground(TEXT_SECONDARY);
        resumeTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        resumeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(resumeTitle);
        
        JPanel resumeBox = createUploadBox(
            currentUser.getProfile().getFullName() + "_CV.pdf is attached from your profile",
            "Click to upload a different file"
        );
        resumeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        appInfoCard.add(resumeBox);
        
        appInfoCard.add(Box.createVerticalStrut(14));
        
        JLabel supportTitle = new JLabel("Supporting Documents (Optional)");
        supportTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        supportTitle.setForeground(TEXT_SECONDARY);
        supportTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        supportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appInfoCard.add(supportTitle);
        
        JPanel supportBox = createUploadBox(
            "Upload transcripts, certificates, or other documents",
            "Click to upload"
        );
        supportBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        supportBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        appInfoCard.add(supportBox);
        
        leftPanel.add(appInfoCard);
        content.add(leftPanel, BorderLayout.CENTER);
        
        // ========== RIGHT COLUMN: Position Summary ==========
        JPanel summaryPanel = createCard("");
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        summaryPanel.setAlignmentX(Component.TOP_ALIGNMENT);
        
        JLabel summaryTitle = new JLabel("Position Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryTitle.setForeground(TEXT_PRIMARY);
        summaryTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        summaryPanel.add(summaryTitle);
        
        // Job info summary items
        JPanel summaryItems = new JPanel();
        summaryItems.setLayout(new BoxLayout(summaryItems, BoxLayout.Y_AXIS));
        summaryItems.setOpaque(false);
        
        summaryItems.add(createSummaryRow("Position", job.getTitle()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Course", job.getCourseCode() + " " + job.getCourse().getCourseName()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Instructor", job.getInstructorName()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Department", job.getDepartment()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Employment Type", job.getEmploymentType()));
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Weekly Hours", job.getWeeklyHoursDisplay()));
        summaryItems.add(Box.createVerticalStrut(10));
        
        // Deadline with warning color
        JPanel deadlineRow = new JPanel(new BorderLayout());
        deadlineRow.setOpaque(false);
        JLabel dlLbl = new JLabel("Application Deadline");
        dlLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dlLbl.setForeground(TEXT_SECONDARY);
        deadlineRow.add(dlLbl, BorderLayout.WEST);
        JLabel dlVal = new JLabel(job.getDeadlineDisplay());
        dlVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dlVal.setForeground(WARNING_COLOR);
        deadlineRow.add(dlVal, BorderLayout.EAST);
        summaryItems.add(deadlineRow);
        
        summaryItems.add(Box.createVerticalStrut(10));
        summaryItems.add(createSummaryRow("Work Mode", job.getLocationMode()));
        
        summaryPanel.add(summaryItems);
        
        // Divider
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER_COLOR);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        divider.setBorder(new EmptyBorder(16, 0, 16, 0));
        divider.setOpaque(false);
        summaryPanel.add(divider);
        
        // Summary footer note
        JLabel noteLbl = new JLabel("Please review all information before submitting your application.");
        noteLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLbl.setForeground(TEXT_SECONDARY);
        noteLbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        summaryPanel.add(noteLbl);
        
        // Submit button (full width)
        JButton submitBtn = new JButton("Submit Application");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(DARK_BUTTON);
        submitBtn.setOpaque(true);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setBorder(new EmptyBorder(14, 20, 14, 20));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { submitBtn.setBackground(DARK_BUTTON_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent evt) { submitBtn.setBackground(DARK_BUTTON); }
        });
        summaryPanel.add(submitBtn);
        
        // Cancel button
        JButton cancelBtn = createSecondaryButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.setBorder(new EmptyBorder(10, 0, 0, 0));
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cancelBtn.addActionListener(e -> showJobDetail(job));
        summaryPanel.add(cancelBtn);
        
        content.add(summaryPanel, BorderLayout.EAST);
        
        // Set right column width
        summaryPanel.setPreferredSize(new Dimension(320, 700));
        
        // Add two-column layout to scrollable content
        scrollContent.add(content);
        
        // File selection storage
        final String[] selectedCvPath = {null};
        final java.util.List<String> selectedSupportPaths = new ArrayList<>();
        
        JButton resumePickBtn = (JButton) resumeBox.getClientProperty("pickButton");
        JLabel resumeHintLbl = (JLabel) resumeBox.getClientProperty("hintLabel");
        resumePickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedCvPath[0] = f.getAbsolutePath();
                resumeHintLbl.setText(f.getName());
            }
        });
        
        JButton supportPickBtn = (JButton) supportBox.getClientProperty("pickButton");
        JLabel supportHintLbl = (JLabel) supportBox.getClientProperty("hintLabel");
        supportPickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                selectedSupportPaths.clear();
                File[] files = chooser.getSelectedFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) selectedSupportPaths.add(f.getAbsolutePath());
                    supportHintLbl.setText(files.length + " file(s) selected");
                }
            }
        });
        
        submitBtn.addActionListener(e -> {
            if (fullNameField.getText().trim().isEmpty()
                || studentIdField.getText().trim().isEmpty()
                || emailField.getText().trim().isEmpty()
                || phoneField.getText().trim().isEmpty()
                || programField.getText().trim().isEmpty()
                || skillsArea.getText().trim().isEmpty()
                || experienceArea.getText().trim().isEmpty()
                || availabilityArea.getText().trim().isEmpty()
                || motivationArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields (*) before submitting.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Application app = new Application();
            
            Application.JobSnapshot jobSnap = new Application.JobSnapshot();
            jobSnap.setTitle(job.getTitle());
            jobSnap.setCourseCode(job.getCourseCode());
            jobSnap.setCourseName(job.getCourse().getCourseName());
            jobSnap.setDepartment(job.getDepartment());
            jobSnap.setInstructorName(job.getInstructorName());
            jobSnap.setInstructorEmail(job.getInstructorEmail());
            jobSnap.setDeadline(job.getDeadlineDisplay());
            jobSnap.setEmploymentType(job.getEmploymentType());
            jobSnap.setWeeklyHours(job.getEmployment().getWeeklyHours());
            jobSnap.setLocationMode(job.getLocationMode());
            app.setJobSnapshot(jobSnap);
            
            Application.ApplicantSnapshot appSnap = new Application.ApplicantSnapshot();
            appSnap.setFullName(fullNameField.getText().trim());
            appSnap.setStudentId(studentIdField.getText().trim());
            appSnap.setEmail(emailField.getText().trim());
            appSnap.setPhoneNumber(phoneField.getText().trim());
            appSnap.setProgramMajor(programField.getText().trim());
            appSnap.setYear(currentUser.getProfile().getYear());
            try {
                String gpaTxt = gpaField.getText().trim();
                if (!gpaTxt.isEmpty()) appSnap.setGpa(Double.parseDouble(gpaTxt));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "GPA must be a number (e.g., 3.8).", "Invalid GPA", JOptionPane.WARNING_MESSAGE);
                return;
            }
            app.setApplicantSnapshot(appSnap);
            
            Application.ApplicationForm appForm = new Application.ApplicationForm();
            String[] skills = skillsArea.getText().split(",");
            appForm.setRelevantSkills(Arrays.stream(skills).map(String::trim).filter(s -> !s.isEmpty()).toList());
            appForm.setRelevantExperience(experienceArea.getText().trim());
            appForm.setAvailability(availabilityArea.getText().trim());
            appForm.setMotivationCoverLetter(motivationArea.getText().trim());
            app.setApplicationForm(appForm);
            
            Application.Attachments at = new Application.Attachments();
            if (selectedCvPath[0] != null && !selectedCvPath[0].isEmpty()) {
                File sourceFile = new File(selectedCvPath[0]);
                
                // 目标目录: data/uploads/profile_cv/{学号}/
                String studentId = studentIdField.getText().trim();
                File studentDir = new File(DataService.resolveDataRootDirectory(),
                        "uploads" + File.separator + "profile_cv" + File.separator + studentId);
                if (!studentDir.exists()) {
                    studentDir.mkdirs();
                }
                
                // 保留原文件名
                File destFile = new File(studentDir, sourceFile.getName());
                
                // 复制文件（覆盖已存在的文件）
                try {
                    java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (java.io.IOException ex) {
                    System.err.println("Error copying CV file: " + ex.getMessage());
                }
                
                Application.CVInfo cv = new Application.CVInfo();
                cv.setFileName(sourceFile.getName());
                cv.setFilePath(destFile.getAbsolutePath());
                String lower = sourceFile.getName().toLowerCase();
                cv.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
                at.setCv(cv);
            }
            if (!selectedSupportPaths.isEmpty()) {
                java.util.List<Application.Document> docs = new ArrayList<>();
                for (String p : selectedSupportPaths) {
                    File f = new File(p);
                    Application.Document d = new Application.Document();
                    d.setFileName(f.getName());
                    d.setFilePath(f.getAbsolutePath());
                    String lower = f.getName().toLowerCase();
                    d.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
                    docs.add(d);
                }
                at.setSupportingDocuments(docs);
            }
            app.setAttachments(at);
            app.setJobId(job.getJobId());
            
            dataService.addApplication(app);
            
            JOptionPane.showMessageDialog(this,
                "Application submitted successfully!\n\nYou can track your application status in 'My Applications'.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            currentUser = dataService.getCurrentUser();
            refreshDashboardQuickStatus();
            showPage("applications");
            refreshApplicationsTable();
        });
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        page.add(scrollPane, BorderLayout.CENTER);
        
        showPage("apply");
    }
    
    /** Creates a summary row with label on left and value on right */
    private JPanel createSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(TEXT_PRIMARY);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JTextField createEditableField(String value) {
        return createEditableField(value, null);
    }

    private JTextField createEditableField(String value, String placeholder) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)
        ));
        
        if (placeholder != null && !placeholder.isEmpty()) {
            field.putClientProperty("placeholder", placeholder);
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    String ph = (String) field.getClientProperty("placeholder");
                    if (ph != null && field.getText().equals(ph)) {
                        field.setText("");
                        field.setForeground(TEXT_PRIMARY);
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        String ph = (String) field.getClientProperty("placeholder");
                        if (ph != null) {
                            field.setText(ph);
                            field.setForeground(new Color(156, 163, 175));
                        }
                    }
                }
            });
        }
        return field;
    }

    private JPanel createEditableFieldPanel(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private String formatUserSkills(TAUser user) {
        if (user == null || user.getSkills() == null) return "";
        java.util.List<String> all = new ArrayList<>();

        TAUser.Skills s = user.getSkills();
        addSkillNames(all, s.getProgramming());
        addSkillNames(all, s.getTeaching());
        addSkillNames(all, s.getCommunication());
        addSkillNames(all, s.getOther());

        return String.join(", ", all);
    }

    private void addSkillNames(java.util.List<String> out, java.util.List<TAUser.Skill> skills) {
        if (skills == null) return;
        for (TAUser.Skill sk : skills) {
            if (sk != null && sk.getName() != null && !sk.getName().trim().isEmpty()) {
                out.add(sk.getName().trim());
            }
        }
    }

    private JPanel createLabeledArea(String label, JTextArea area, String placeholder) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        wrap.add(lbl);
        
        JPanel areaContainer = new JPanel(new BorderLayout());
        areaContainer.setOpaque(false);
        
        areaContainer.add(area, BorderLayout.CENTER);
        
        if (placeholder != null && !placeholder.isEmpty()) {
            JLabel placeholderLabel = new JLabel(placeholder);
            placeholderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            placeholderLabel.setForeground(new Color(156, 163, 175));
            placeholderLabel.setBorder(new EmptyBorder(10, 12, 10, 12));
            placeholderLabel.setName("placeholder");
            areaContainer.add(placeholderLabel, BorderLayout.NORTH);
            
            area.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    placeholderLabel.setVisible(false);
                }
                @Override
                public void focusLost(FocusEvent e) {
                    if (area.getText().isEmpty()) {
                        placeholderLabel.setVisible(true);
                    }
                }
            });
            
            area.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    placeholderLabel.setVisible(false);
                }
            });
        }
        
        JScrollPane sp = new JScrollPane(areaContainer);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setOpaque(false);
        wrap.add(sp);
        return wrap;
    }

    private JPanel createUploadBox(String topLine, String bottomLine) {
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(BORDER_COLOR, 6, 6),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel(topLine);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(TEXT_SECONDARY);
        center.add(hint);

        JLabel action = new JLabel(bottomLine);
        action.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        action.setForeground(TEXT_SECONDARY);
        action.setBorder(new EmptyBorder(4, 0, 0, 0));
        center.add(action);

        box.add(center, BorderLayout.CENTER);

        JButton pick = createSecondaryButton("Upload");
        pick.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.add(pick, BorderLayout.EAST);

        box.putClientProperty("pickButton", pick);
        box.putClientProperty("hintLabel", hint);
        return box;
    }
    
    // ==================== APPLICATIONS PAGE ====================
    private JPanel applicationsPage;
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    
    private void initApplicationsPage() {
        // 必须用 BorderLayout：createPagePanel() 是 BoxLayout，若混用 BorderLayout 约束会导致表格区域异常、点击无响应
        applicationsPage = new JPanel(new BorderLayout());
        applicationsPage.setBackground(BG_COLOR);
        applicationsPage.setBorder(new EmptyBorder(30, 40, 30, 40));
        pages.put("applications", applicationsPage);
        mainContentPanel.add(applicationsPage, "applications");
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerLeft.add(titleLabel, BorderLayout.NORTH);
        
        JLabel subtitleLabel = new JLabel("Track and manage your TA applications");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        headerLeft.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Summary cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        summaryPanel.add(createSummaryCard("Total Applications", 
            String.valueOf(dataService.countApplicationsByStatus("pending") + 
                          dataService.countApplicationsByStatus("under_review") +
                          dataService.countApplicationsByStatus("accepted") +
                          dataService.countApplicationsByStatus("rejected")),
            new Color(219, 234, 254), INFO_COLOR));
        
        summaryPanel.add(createSummaryCard("Pending", 
            String.valueOf(dataService.countApplicationsByStatus("pending")),
            new Color(254, 243, 199), WARNING_COLOR));
        
        summaryPanel.add(createSummaryCard("Accepted", 
            String.valueOf(dataService.countApplicationsByStatus("accepted")),
            new Color(209, 250, 229), SUCCESS_COLOR));
        
        summaryPanel.add(createSummaryCard("Rejected", 
            String.valueOf(dataService.countApplicationsByStatus("rejected")),
            new Color(254, 226, 226), DANGER_COLOR));
        
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(headerLeft);
        northStack.add(summaryPanel);
        applicationsPage.add(northStack, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Job Title", "Course", "Department", "Applied Date", "Status", "Last Updated", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        applicationsTable = new JTable(tableModel);
        applicationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        applicationsTable.setRowHeight(50);
        applicationsTable.setGridColor(BORDER_COLOR);
        applicationsTable.setShowGrid(true);
        applicationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        applicationsTable.getTableHeader().setBackground(BG_COLOR);
        applicationsTable.getTableHeader().setForeground(TEXT_PRIMARY);
        applicationsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            applicationsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);

        applicationsPage.add(scrollPane, BorderLayout.CENTER);

        // 监听器和渲染器只需注册一次（用 mousePressed：Windows 上 mouseClicked 常因轻微位移不触发）
        applicationsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        applicationsTable.getColumn("Status").setCellRenderer(new StatusRenderer());
        final int actionModelIndex = applicationsTable.getColumn("Action").getModelIndex();
        applicationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                Point p = e.getPoint();
                int viewRow = applicationsTable.rowAtPoint(p);
                int viewCol = applicationsTable.columnAtPoint(p);
                if (viewRow < 0 || viewCol < 0) {
                    return;
                }
                int modelCol = applicationsTable.convertColumnIndexToModel(viewCol);
                if (modelCol != actionModelIndex) {
                    return;
                }
                int modelRow = applicationsTable.convertRowIndexToModel(viewRow);
                List<Application> apps = dataService.getUserApplications();
                if (modelRow >= 0 && modelRow < apps.size()) {
                    System.out.println("[DEBUG] View clicked! row=" + modelRow + ", app=" + apps.get(modelRow).getApplicationId());
                    showStatusPage(apps.get(modelRow));
                }
            }
        });
    }
    
    private JPanel createSummaryCard(String label, String value, Color bg, Color iconColor) {
        JPanel card = createCard("");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(iconColor);
        card.add(valueLabel);
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelLabel.setForeground(TEXT_PRIMARY);
        labelLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        card.add(labelLabel);
        
        return card;
    }
    
    private void refreshApplicationsTable() {
        tableModel.setRowCount(0);

        for (Application app : dataService.getUserApplications()) {
            String status = app.getStatus().getLabel();

            Object[] row = {
                app.getJobSnapshot().getTitle(),
                app.getJobSnapshot().getCourseCode(),
                app.getJobSnapshot().getDepartment(),
                app.getMeta().getSubmittedAt().substring(0, 10),
                status,
                app.getStatus().getLastUpdated().substring(0, 16).replace("T", " "),
                "View"
            };
            tableModel.addRow(row);
        }
    }
    
    private Color getStatusColor(String color) {
        if (color == null) {
            return TEXT_SECONDARY;
        }
        return switch (color) {
            case "yellow" -> WARNING_COLOR;
            case "blue" -> INFO_COLOR;
            case "green" -> SUCCESS_COLOR;
            case "red" -> DANGER_COLOR;
            default -> TEXT_SECONDARY;
        };
    }
    
    // ==================== STATUS PAGE ====================
    private void initStatusPage() {
        JPanel page = createPagePanel();
        pages.put("status", page);
        mainContentPanel.add(page, "status");
    }
    
    private void showStatusPage(Application app) {
        JPanel page = pages.get("status");
        page.removeAll();
        
        try {
        
        // Back button
        JButton backBtn = new JButton("\u2190 Back to My Applications");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 20, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            showPage("applications");
            refreshApplicationsTable();
        });
        page.add(backBtn);
        
        // Header
        JPanel header = createCard("");
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel title = new JLabel(app.getJobSnapshot().getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        header.add(title);
        
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        meta.setOpaque(false);
        meta.add(createMetaItem("Course: ", app.getJobSnapshot().getCourseCode()));
        meta.add(Box.createHorizontalStrut(20));
        meta.add(createMetaItem("Applicant: ", app.getApplicantSnapshot().getFullName()));
        meta.add(Box.createHorizontalStrut(20));
        meta.add(createMetaItem("Applied: ", app.getMeta().getSubmittedAt().substring(0, 10)));
        meta.add(Box.createHorizontalStrut(20));
        
        JLabel statusLbl = new JLabel(app.getStatus().getLabel());
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLbl.setForeground(getStatusColor(app.getStatus().getColor()));
        statusLbl.setOpaque(true);
        statusLbl.setBackground(getStatusColor(app.getStatus().getColor()).brighter().brighter());
        statusLbl.setBorder(new EmptyBorder(5, 12, 5, 12));
        statusLbl.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                ((JLabel)c).setVerticalTextPosition(JLabel.CENTER);
                super.paint(g, c);
            }
        });
        meta.add(statusLbl);
        
        header.add(meta);
        page.add(header);
        
        // Timeline
        JPanel timelineSection = createCard("");
        timelineSection.setLayout(new BoxLayout(timelineSection, BoxLayout.Y_AXIS));
        timelineSection.setBorder(new EmptyBorder(20, 25, 20, 25));
        timelineSection.setPreferredSize(new Dimension(0, 180));
        
        JLabel timelineTitle = new JLabel("Application Progress");
        timelineTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timelineTitle.setForeground(TEXT_PRIMARY);
        timelineTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        timelineSection.add(timelineTitle);
        
        // Timeline steps
        JPanel timeline = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timeline.setOpaque(false);
        
        String[] steps = {"Application Submitted", "Under Review", "Interview Scheduled", "Decision"};
        String[] stepKeys = {"submitted", "under_review", "interview_scheduled", "decision"};
        
        List<Application.TimelineEvent> tl = app.getTimeline();
        if (tl == null) {
            tl = List.of();
        }
        for (int i = 0; i < steps.length; i++) {
            boolean completed = false;
            boolean current = false;
            
            for (Application.TimelineEvent event : tl) {
                if (event.getStepKey().equals(stepKeys[i]) || 
                    (i == 0 && event.getStepKey().equals("submitted")) ||
                    (i == 3 && (app.getStatus().getCurrent().equals("accepted") || app.getStatus().getCurrent().equals("rejected")))) {
                    completed = true;
                    if (i == steps.length - 1) {
                        current = app.getStatus().getCurrent().equals("accepted") || app.getStatus().getCurrent().equals("rejected");
                    } else {
                        current = app.getStatus().getCurrent().equals("under_review") && i == 1;
                    }
                }
            }
            
            timeline.add(createTimelineStep(steps[i], completed, current));
            
            if (i < steps.length - 1) {
                final boolean isCompleted = completed;
                JPanel connector = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setStroke(new BasicStroke(2));
                        g2.setColor(isCompleted ? SUCCESS_COLOR : BORDER_COLOR);
                        g2.drawLine(0, 15, 60, 15);
                    }
                };
                connector.setPreferredSize(new Dimension(60, 30));
                connector.setOpaque(false);
                timeline.add(connector);
            }
        }
        
        timelineSection.add(timeline);
        page.add(timelineSection);
        
        // Status Details（review 可能为 null，避免 NPE 导致界面不切换）
        Application.Review review = app.getReview();
        JPanel details = new JPanel(new GridLayout(1, 3, 20, 20));
        details.setOpaque(false);
        details.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        details.add(createDetailSection("Status Information", 
            review != null && review.getStatusMessage() != null && !review.getStatusMessage().isEmpty() 
                ? review.getStatusMessage() 
                : "Your application is being processed."));
        
        details.add(createDetailSection("Reviewer Notes",
            review != null && review.getReviewerNotes() != null && !review.getReviewerNotes().isEmpty()
                ? review.getReviewerNotes()
                : "No notes available yet."));
        
        details.add(createDetailSection("Next Steps",
            review != null && review.getNextSteps() != null && !review.getNextSteps().isEmpty()
                ? review.getNextSteps()
                : "Please wait for further updates."));
        
        page.add(details);
        
        showPage("status");
        
        } catch (Exception ex) {
            System.err.println("[ERROR] Exception in showStatusPage: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private JLabel createMetaItem(String label, String value) {
        JLabel lbl = new JLabel("<html><b>" + label + "</b> " + value + "</html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }
    
    private JPanel createTimelineStep(String label, boolean completed, boolean current) {
        JPanel step = new JPanel();
        step.setLayout(new BoxLayout(step, BoxLayout.Y_AXIS));
        step.setOpaque(false);
        
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (current) {
                    g2.setColor(PRIMARY_COLOR);
                    g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(8, 8, getWidth() - 16, getHeight() - 16);
                } else if (completed) {
                    g2.setColor(SUCCESS_COLOR);
                    g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                } else {
                    g2.setColor(BORDER_COLOR);
                    g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                }
            }
        };
        circle.setPreferredSize(new Dimension(32, 32));
        circle.setMaximumSize(new Dimension(32, 32));
        circle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel stepLabel = new JLabel("<html><center>" + label + "</center></html>");
        stepLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stepLabel.setForeground(completed || current ? TEXT_PRIMARY : TEXT_SECONDARY);
        stepLabel.setMaximumSize(new Dimension(80, 40));
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        step.add(circle);
        step.add(Box.createVerticalStrut(8));
        step.add(stepLabel);
        
        return step;
    }
    
    private JPanel createDetailSection(String title, String content) {
        JPanel section = createCard("");
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        section.add(titleLabel);
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setForeground(TEXT_SECONDARY);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setRows(3);
        section.add(contentArea);
        
        return section;
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void showPage(String pageName) {
        if (pageName.equals("applications")) {
            refreshApplicationsTable();
        }
        if ("dashboard".equals(pageName)) {
            refreshDashboardQuickStatus();
        }
        if ("profile".equals(pageName) && profileCardLayout != null && profileRoot != null) {
            profileCardLayout.show(profileRoot, TaPortalHost.ROUTE_PROFILE);
            refreshProfileScreens();
        }

        // 导航高亮与当前页一致
        switch (pageName) {
            case "dashboard" -> updateNavButtons(homeBtn);
            case "profile" -> updateNavButtons(profileNavBtn);
            case "jobs", "job-detail", "apply" -> updateNavButtons(jobsNavBtn);
            case "applications", "status" -> updateNavButtons(jobsNavBtn);
            default -> { /* 保持当前 */ }
        }

        cardLayout.show(mainContentPanel, pageName);
    }

    private void refreshProfileScreens() {
        if (profileScreenPanel != null) {
            profileScreenPanel.refresh();
        }
        if (editProfileScreenPanel != null) {
            editProfileScreenPanel.refresh();
        }
        if (editSkillsScreenPanel != null) {
            editSkillsScreenPanel.refresh();
        }
        if (manageCvScreenPanel != null) {
            manageCvScreenPanel.refresh();
        }
    }

    private void initProfileModule() {
        profileRoot = new JPanel();
        profileCardLayout = new CardLayout();
        profileRoot.setLayout(profileCardLayout);
        profileRoot.setBackground(BG_COLOR);

        profileScreenPanel = new ProfileScreen(this);
        editProfileScreenPanel = new EditProfileScreen(this);
        editSkillsScreenPanel = new EditSkillsScreen(this);
        manageCvScreenPanel = new ManageCvScreen(this);

        profileRoot.add(profileScreenPanel, TaPortalHost.ROUTE_PROFILE);
        profileRoot.add(editProfileScreenPanel, TaPortalHost.ROUTE_EDIT_PROFILE);
        profileRoot.add(editSkillsScreenPanel, TaPortalHost.ROUTE_EDIT_SKILLS);
        profileRoot.add(manageCvScreenPanel, TaPortalHost.ROUTE_MANAGE_CV);

        pages.put("profile", profileRoot);
        mainContentPanel.add(profileRoot, "profile");
    }

    // ----- TaPortalHost（Profile 模块数据以 DataService / TAUser JSON 为准）-----

    @Override
    public ProfileData profile() {
        dataService.syncApplicationSummaryFromApplications();
        return TaUserProfileMapper.toProfileData(dataService.getCurrentUser(), dataService.getUserApplications().size());
    }

    @Override
    public void updateProfile(ProfileData next) {
        TaUserProfileMapper.applyToUser(next, dataService.getCurrentUser());
        currentUser = dataService.getCurrentUser();
        dataService.recomputeProfileCompletion();
        dataService.saveCurrentUser();
        refreshProfileScreens();
        refreshDashboardQuickStatus();
    }

    @Override
    public void showRoute(String route) {
        if (profileCardLayout == null || profileRoot == null) {
            return;
        }
        profileCardLayout.show(profileRoot, route);
        if (TaPortalHost.ROUTE_PROFILE.equals(route)) {
            profileScreenPanel.refresh();
        } else if (TaPortalHost.ROUTE_EDIT_PROFILE.equals(route)) {
            editProfileScreenPanel.refresh();
        } else if (TaPortalHost.ROUTE_EDIT_SKILLS.equals(route)) {
            editSkillsScreenPanel.refresh();
        } else if (TaPortalHost.ROUTE_MANAGE_CV.equals(route)) {
            manageCvScreenPanel.refresh();
        }
    }

    @Override
    public void showDashboard() {
        showPage("dashboard");
    }

    @Override
    public void showJobsModule() {
        showPage("jobs");
    }

    @Override
    public void showApplicationsModule() {
        showPage("applications");
    }

    @Override
    public void logout() {
        confirmLogoutAndClose();
    }

    private void confirmLogoutAndClose() {
        int ok = JOptionPane.showConfirmDialog(this, "确定要退出吗？", "Logout", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            dispose();
            if (afterLogout != null) {
                afterLogout.run();
            } else {
                System.exit(0);
            }
        }
    }

    @Override
    public void uploadCvFromFile(File source) throws IOException {
        TAUser user = dataService.getCurrentUser();
        if (user.getProfile() == null || user.getProfile().getStudentId() == null
                || user.getProfile().getStudentId().isBlank()) {
            throw new IOException("Student ID is required before uploading a CV.");
        }
        String studentId = user.getProfile().getStudentId().trim();
        File studentDir = new File(DataService.resolveDataRootDirectory(),
                "uploads" + File.separator + "profile_cv" + File.separator + studentId);
        if (!studentDir.exists()) {
            studentDir.mkdirs();
        }
        File destFile = new File(studentDir, source.getName());
        Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        TAUser.CV cv = user.getCv();
        if (cv == null) {
            cv = new TAUser.CV();
            user.setCv(cv);
        }
        cv.setUploaded(true);
        cv.setOriginalFileName(source.getName());
        cv.setStoredFileName(destFile.getName());
        cv.setFilePath(destFile.getAbsolutePath());
        String lower = source.getName().toLowerCase();
        cv.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
        cv.setFileSizeKB(Math.max(1, (int) (source.length() / 1024)));
        cv.setUploadedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        currentUser = user;
        dataService.recomputeProfileCompletion();
        dataService.saveCurrentUser();
        refreshProfileScreens();
        refreshDashboardQuickStatus();
    }

    @Override
    public void removeCvUpload() {
        TAUser user = dataService.getCurrentUser();
        TAUser.CV cv = user.getCv();
        if (cv != null) {
            cv.setUploaded(false);
            cv.setOriginalFileName(null);
            cv.setStoredFileName(null);
            cv.setFilePath(null);
            cv.setFileType(null);
            cv.setFileSizeKB(0);
            cv.setUploadedAt(null);
        }
        currentUser = user;
        dataService.recomputeProfileCompletion();
        dataService.saveCurrentUser();
        refreshProfileScreens();
        refreshDashboardQuickStatus();
    }

    @Override
    public void completeOnboarding() {
        outerCardLayout.show(outerRoot, OUTER_APP);
        showPage("dashboard");
        refreshDashboardQuickStatus();
        refreshProfileScreens();
    }

    @Override
    public void syncCvFromPendingFile(File f) throws IOException {
        uploadCvFromFile(f);
    }

    private JPanel createPagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        return panel;
    }
    
    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(0, 0, 0, 0)
        ));
        return card;
    }
    
    private JLabel createTag(String text, Color bg, Color fg) {
        JLabel tag = new JLabel(text);
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(fg);
        tag.setOpaque(true);
        tag.setBackground(bg);
        tag.setBorder(new EmptyBorder(5, 10, 5, 10));
        return tag;
    }
    
    private JLabel createSkillTag(String text) {
        JLabel tag = new JLabel(text);
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(TEXT_SECONDARY);
        tag.setOpaque(true);
        tag.setBackground(BG_COLOR);
        tag.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        tag.setBorder(new EmptyBorder(5, 10, 5, 10));
        return tag;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_COLOR);
            }
        });
        
        return btn;
    }
    
    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(BG_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(CARD_BG);
            }
        });
        
        return btn;
    }
    
    private JTextArea createTextArea(String label, int rows) {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setRows(rows);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 12, 10, 12)
        ));
        return area;
    }
    
    private String getInitials(String name) {
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0);
        }
        return name.substring(0, 2).toUpperCase();
    }
    
    // Table renderers
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setHorizontalAlignment(JLabel.CENTER);
            
            String status = (String) value;
            Color bg, fg;
            
            switch (status.toLowerCase()) {
                case "pending" -> { bg = new Color(254, 243, 199); fg = WARNING_COLOR; }
                case "under review" -> { bg = new Color(219, 234, 254); fg = INFO_COLOR; }
                case "accepted" -> { bg = new Color(209, 250, 229); fg = SUCCESS_COLOR; }
                case "rejected" -> { bg = new Color(254, 226, 226); fg = DANGER_COLOR; }
                default -> { bg = BG_COLOR; fg = TEXT_SECONDARY; }
            }
            
            cell.setBackground(bg);
            cell.setForeground(fg);
            cell.setOpaque(true);
            cell.setBorder(new EmptyBorder(5, 10, 5, 10));
            
            return cell;
        }
    }
    
    class ButtonRenderer extends JLabel implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View");
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                setForeground(PRIMARY_COLOR);
                setBackground(new Color(238, 242, 255));
                setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
            } else {
                setForeground(PRIMARY_COLOR);
                setBackground(BG_COLOR);
                setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
            }
            setOpaque(true);
            return this;
        }
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
