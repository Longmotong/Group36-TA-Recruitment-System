package appreview.ui;

import appreview.model.ApplicationRecord;
import appreview.model.JobRecord;
import appreview.model.MoProfile;
import appreview.model.ReviewRecord;
import appreview.model.TaProfile;
import appreview.service.ApplicationReviewService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Swing standalone application for MO review workflow.
 */
public class DesktopApp extends JFrame {
    private static final String PAGE_DASHBOARD = "dashboard";
    private static final String PAGE_APPLICATIONS = "applications";
    private static final String PAGE_DETAIL = "detail";
    private static final String PAGE_REVIEW = "review";
    private static final String PAGE_RECORDS = "records";
    private static final int APP_ACTION_COL = 7;
    private static final int RECORD_ACTION_COL = 6;
    private static final Color PAGE_BG = new Color(243, 244, 247);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(219, 222, 229);
    private static final Color DARK = new Color(18, 18, 18);

    private final ApplicationReviewService service;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JButton navHome = new JButton("Home");
    private final JButton navJob = new JButton("Job Management");
    private final JButton navReview = new JButton("Application Review");
    private final JButton navLogout = new JButton("Logout");
    private String currentPage = PAGE_DASHBOARD;

    private final JLabel dashboardActiveCourses = new JLabel("0");
    private final JLabel dashboardOpenJobs = new JLabel("0");
    private final JLabel dashboardPendingReviews = new JLabel("0");
    private final JLabel dashboardCurrentMo = new JLabel("-");
    private final JLabel dashboardMoInfo = new JLabel("-");

    private final JTextField appKeywordField = new JTextField(16);
    private final JTextField appCourseField = new JTextField(12);
    private final JComboBox<String> appStatusCombo = new JComboBox<>(new String[]{"all", "pending", "approved", "rejected"});
    private final JLabel appStatsLabel = new JLabel();
    private final JLabel appTotalStat = new JLabel("0");
    private final JLabel appPendingStat = new JLabel("0");
    private final JLabel appApprovedStat = new JLabel("0");
    private final JLabel appRejectedStat = new JLabel("0");
    private final DefaultTableModel appTableModel = new DefaultTableModel(
            new Object[]{"TA Name", "Student ID", "Applied Course", "Match Score", "Missing Skills", "Current Workload", "Status", "Actions"}, 0);
    private final JTable appTable = new JTable(appTableModel);
    private List<ApplicationRecord> appRows = new ArrayList<>();

    private final JTextArea detailApplicationInfo = new JTextArea();
    private final JTextArea detailSkillsInfo = new JTextArea();
    private final JTextArea detailWorkloadInfo = new JTextArea();
    private final JTextArea detailPersonalInfo = new JTextArea();
    private final JTextArea detailExperienceInfo = new JTextArea();
    private ApplicationRecord currentApplication;

    private final JTextArea reviewContextArea = new JTextArea(3, 60);
    private final JTextArea reviewLeftArea = new JTextArea();
    private final JTextArea reviewRightArea = new JTextArea();
    private final JTextArea reviewNotesArea = new JTextArea(5, 50);
    private final JRadioButton approveBtn = new JRadioButton("Approve Application", true);
    private final JRadioButton rejectBtn = new JRadioButton("Reject Application", false);

    private final JLabel recordsStatLabel = new JLabel();
    private final JLabel recordsTotalStat = new JLabel("0");
    private final JLabel recordsApprovedStat = new JLabel("0");
    private final JLabel recordsRejectedStat = new JLabel("0");
    private final JTextField recordsKeywordField = new JTextField(18);
    private final JComboBox<String> recordsTimeCombo = new JComboBox<>(new String[]{"All Time", "7 Days", "30 Days"});
    private final JComboBox<String> recordsResultCombo = new JComboBox<>(new String[]{"all", "approved", "rejected"});
    private final DefaultTableModel recordsTableModel = new DefaultTableModel(
            new Object[]{"Course Name/Code", "TA Name", "Student ID", "Review Date", "Result", "Reviewer", "Actions"}, 0);
    private final JTable recordsTable = new JTable(recordsTableModel);
    private List<ReviewRecord> recordRows = new ArrayList<>();

    /**
     * Build UI with loaded service.
     *
     * @param service application service
     */
    public DesktopApp(ApplicationReviewService service) {
        this.service = service;
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);

        setTitle("MO System - Application Review");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        contentPanel.setBackground(PAGE_BG);

        add(buildTopNav(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(buildDashboardPage(), PAGE_DASHBOARD);
        contentPanel.add(buildApplicationsPage(), PAGE_APPLICATIONS);
        contentPanel.add(buildDetailPage(), PAGE_DETAIL);
        contentPanel.add(buildReviewPage(), PAGE_REVIEW);
        contentPanel.add(buildRecordsPage(), PAGE_RECORDS);

        refreshAll();
        initTableVisuals();
        installTableActionHandlers();
        showPage(PAGE_DASHBOARD);
    }

    private JPanel buildTopNav() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        JLabel brand = new JLabel("MO System");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 18f));
        nav.add(brand);
        styleNavButton(navHome);
        styleNavButton(navJob);
        styleNavButton(navReview);
        styleNavButton(navLogout);
        nav.add(navHome);
        nav.add(navJob);
        nav.add(navReview);
        nav.add(navLogout);

        navHome.addActionListener(e -> showPage(PAGE_DASHBOARD));
        navJob.addActionListener(e -> JOptionPane.showMessageDialog(this, "Job Management is placeholder."));
        navReview.addActionListener(e -> {
            refreshApplicationsTable();
            showPage(PAGE_APPLICATIONS);
        });
        navLogout.addActionListener(e -> System.exit(0));
        return nav;
    }

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("MO Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Welcome back! Please select a module to continue.");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(title, BorderLayout.NORTH);
        head.add(subtitle, BorderLayout.SOUTH);
        page.add(head, BorderLayout.NORTH);

        JPanel modules = new JPanel(new GridLayout(1, 2, 16, 16));
        modules.setOpaque(false);
        modules.add(buildJobManagementModuleCard());
        modules.add(buildApplicationReviewModuleCard());

        JPanel quickWrap = new JPanel(new BorderLayout(8, 8));
        quickWrap.setOpaque(false);
        JLabel quickTitle = new JLabel("Quick Overview");
        quickTitle.setFont(quickTitle.getFont().deriveFont(Font.BOLD, 14f));
        JPanel quickCards = new JPanel(new GridLayout(1, 3, 12, 12));
        quickCards.setOpaque(false);
        quickCards.add(overviewTile("Active Courses", dashboardActiveCourses, DARK));
        quickCards.add(overviewTile("Open Job Postings", dashboardOpenJobs, DARK));
        quickCards.add(overviewTile("Pending Reviews", dashboardPendingReviews, new Color(0, 120, 215)));
        quickWrap.add(quickTitle, BorderLayout.NORTH);
        quickWrap.add(quickCards, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(modules, BorderLayout.CENTER);
        center.add(quickWrap, BorderLayout.SOUTH);
        page.add(center, BorderLayout.CENTER);

        return page;
    }

    private JPanel buildJobManagementModuleCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        card.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Job Management Module");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BorderLayout());
        JPanel bullets = new JPanel(new GridLayout(4, 1, 2, 2));
        bullets.setOpaque(false);
        bullets.add(new JLabel("• Manage course info, requirements, and job postings"));
        bullets.add(new JLabel("• Create and edit course information"));
        bullets.add(new JLabel("• Set TA requirements and qualifications"));
        bullets.add(new JLabel("• Post and manage job openings"));
        body.add(bullets, BorderLayout.CENTER);

        JButton btn = new JButton("Go to Job Management");
        btn.setPreferredSize(new Dimension(340, 48));
        stylePrimaryButton(btn);
        btn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Job Management is placeholder."));

        card.add(body, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildApplicationReviewModuleCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        card.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Application Review Module");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BorderLayout());
        JPanel bullets = new JPanel(new GridLayout(3, 1, 2, 2));
        bullets.setOpaque(false);
        bullets.add(new JLabel("• View TA applications, review, and check allocation results"));
        bullets.add(new JLabel("• Review and approve/reject applications"));
        bullets.add(new JLabel("• View TA allocation results"));
        body.add(bullets, BorderLayout.CENTER);

        JButton btn = new JButton("Go to Application Review");
        btn.setPreferredSize(new Dimension(340, 48));
        stylePrimaryButton(btn);
        btn.addActionListener(e -> {
            refreshApplicationsTable();
            showPage(PAGE_APPLICATIONS);
        });
        card.add(body, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    private JPanel overviewTile(String label, JLabel value, Color numberColor) {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        p.setLayout(new BorderLayout());

        JLabel caption = new JLabel(label);
        caption.setFont(caption.getFont().deriveFont(14f));
        caption.setForeground(new Color(110, 120, 130));
        caption.setHorizontalAlignment(SwingConstants.CENTER);

        value.setFont(value.getFont().deriveFont(Font.BOLD, 26f));
        value.setForeground(numberColor);
        value.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(value, BorderLayout.NORTH);
        p.add(caption, BorderLayout.SOUTH);
        return p;
    }

    private JPanel metricCard(String title, JLabel value) {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createTitledBorder(title)));
        value.setHorizontalAlignment(SwingConstants.CENTER);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 24f));
        if ("Pending Reviews".equals(title)) {
            value.setForeground(new Color(171, 122, 14));
        } else if ("Current Reviewer".equals(title)) {
            value.setForeground(new Color(35, 82, 170));
        } else {
            value.setForeground(DARK);
        }
        p.add(value);
        return p;
    }

    private JPanel buildApplicationsPage() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("TA Applications");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Review and manage Teaching Assistant applications");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        JButton back = new JButton("Back to Home");
        styleGhostButton(back);
        back.addActionListener(e -> showPage(PAGE_DASHBOARD));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        header.add(back, BorderLayout.EAST);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 12));
        statsRow.setOpaque(false);
        statsRow.add(overviewTile("Total Applications", appTotalStat, DARK));
        statsRow.add(overviewTile("Pending Reviews", appPendingStat, new Color(0, 140, 200)));
        statsRow.add(overviewTile("Approved", appApprovedStat, new Color(0, 160, 70)));
        statsRow.add(overviewTile("Rejected", appRejectedStat, new Color(180, 35, 24)));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        filter.setBackground(CARD_BG);
        filter.setBorder(new LineBorder(BORDER, 1, true));

        filter.add(new JLabel("Search by TA / Student ID"));
        filter.add(appKeywordField);

        // Course dropdown derived from jobs
        java.util.Set<String> courseCodes = new java.util.LinkedHashSet<String>();
        for (JobRecord j : service.getJobs()) {
            if (j.courseCode != null && !j.courseCode.trim().isEmpty()) {
                courseCodes.add(j.courseCode.trim());
            }
        }
        JComboBox<String> courseCombo = new JComboBox<String>();
        courseCombo.addItem("all");
        for (String cc : courseCodes) {
            courseCombo.addItem(cc);
        }
        courseCombo.setSelectedItem("all");
        filter.add(new JLabel("Course"));
        filter.add(courseCombo);

        filter.add(new JLabel("Status"));
        filter.add(appStatusCombo);

        JButton reset = new JButton("Reset");
        styleGhostButton(reset);
        filter.add(reset);

        appCourseField.setText("all");
        courseCombo.addActionListener(e -> {
            Object sel = courseCombo.getSelectedItem();
            appCourseField.setText(sel == null ? "all" : String.valueOf(sel));
            refreshApplicationsTable();
        });
        appStatusCombo.addActionListener(e -> refreshApplicationsTable());
        appKeywordField.addActionListener(e -> refreshApplicationsTable());
        reset.addActionListener(e -> {
            appKeywordField.setText("");
            appStatusCombo.setSelectedItem("all");
            courseCombo.setSelectedItem("all");
            appCourseField.setText("all");
            refreshApplicationsTable();
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JButton records = new JButton("View My Review Records \u2192");
        records.setForeground(new Color(0, 102, 204));
        records.setOpaque(false);
        records.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        records.addActionListener(e -> {
            refreshRecordsTable();
            showPage(PAGE_RECORDS);
        });
        bottom.add(new JPanel(), BorderLayout.CENTER);
        bottom.add(records, BorderLayout.EAST);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BorderLayout(8, 8));
        JPanel northTop = new JPanel(new BorderLayout());
        northTop.setOpaque(false);
        northTop.add(header, BorderLayout.NORTH);
        northTop.add(statsRow, BorderLayout.CENTER);
        north.add(northTop, BorderLayout.NORTH);
        north.add(filter, BorderLayout.SOUTH);

        page.add(north, BorderLayout.NORTH);
        page.add(new JScrollPane(appTable), BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildDetailPage() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        setupReadOnlyArea(detailApplicationInfo);
        setupReadOnlyArea(detailSkillsInfo);
        setupReadOnlyArea(detailWorkloadInfo);
        setupReadOnlyArea(detailPersonalInfo);
        setupReadOnlyArea(detailExperienceInfo);
        body.add(sectionPanel("Application Information", detailApplicationInfo));
        body.add(sectionPanel("Skills Matching Analysis", detailSkillsInfo));
        body.add(sectionPanel("Current TA Assignments & Workload", detailWorkloadInfo));
        body.add(sectionPanel("Personal Information", detailPersonalInfo));
        body.add(sectionPanel("Experience & Background", detailExperienceInfo));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton reviewTop = new JButton("Review Application");
        JButton reviewNow = new JButton("Review Now");
        JButton back = new JButton("Back to Applications");
        stylePrimaryButton(reviewTop);
        stylePrimaryButton(reviewNow);
        styleGhostButton(back);
        actions.add(reviewTop);
        actions.add(reviewNow);
        actions.add(back);
        reviewTop.addActionListener(e -> {
            if (currentApplication != null) {
                loadReviewPage(currentApplication);
                showPage(PAGE_REVIEW);
            }
        });
        reviewNow.addActionListener(e -> {
            if (currentApplication != null) {
                loadReviewPage(currentApplication);
                showPage(PAGE_REVIEW);
            }
        });
        back.addActionListener(e -> showPage(PAGE_APPLICATIONS));
        page.add(new JScrollPane(body), BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildReviewPage() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        setupReadOnlyArea(reviewContextArea);
        reviewContextArea.setBackground(new Color(248, 249, 252));
        JPanel contextPanel = sectionPanel("Application Context", reviewContextArea);
        reviewLeftArea.setEditable(false);
        reviewRightArea.setEditable(false);
        setupReadOnlyArea(reviewLeftArea);
        setupReadOnlyArea(reviewRightArea);
        JPanel split = new JPanel(new GridLayout(1, 2, 8, 8));
        split.setOpaque(false);
        split.add(sectionPanel("Course Requirements", reviewLeftArea));
        split.add(sectionPanel("Applicant Qualifications", reviewRightArea));

        JPanel decisionPanel = new JPanel(new BorderLayout(6, 6));
        decisionPanel.setBackground(CARD_BG);
        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radios.setBackground(CARD_BG);
        javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
        group.add(approveBtn);
        group.add(rejectBtn);
        radios.add(approveBtn);
        radios.add(rejectBtn);
        decisionPanel.add(radios, BorderLayout.NORTH);
        decisionPanel.add(new JScrollPane(reviewNotesArea), BorderLayout.CENTER);
        decisionPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDER, 1, true), "Review Decision"));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton submit = new JButton("Submit Review");
        JButton cancel = new JButton("Cancel");
        stylePrimaryButton(submit);
        styleGhostButton(cancel);
        bottom.add(submit);
        bottom.add(cancel);
        submit.addActionListener(e -> submitReview());
        cancel.addActionListener(e -> showPage(PAGE_DETAIL));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(contextPanel, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        page.add(decisionPanel, BorderLayout.SOUTH);
        page.add(bottom, BorderLayout.NORTH);
        return page;
    }

    private JPanel buildRecordsPage() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("My Review Records");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        JLabel subtitle = new JLabel("View your application review history and decisions");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        JButton back = new JButton("Back to Application Review");
        styleGhostButton(back);
        back.addActionListener(e -> showPage(PAGE_APPLICATIONS));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(back, BorderLayout.WEST);
        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);
        header.add(headerText, BorderLayout.CENTER);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 12));
        statsRow.setOpaque(false);
        statsRow.add(overviewTile("Total Reviews", recordsTotalStat, DARK));
        statsRow.add(overviewTile("Approved Applications", recordsApprovedStat, new Color(0, 160, 70)));
        statsRow.add(overviewTile("Rejected Applications", recordsRejectedStat, new Color(180, 35, 24)));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        filter.setBackground(CARD_BG);
        filter.setBorder(new LineBorder(BORDER, 1, true));
        filter.add(new JLabel("Search by course or TA"));
        filter.add(recordsKeywordField);
        filter.add(new JLabel("Time"));
        filter.add(recordsTimeCombo);
        filter.add(new JLabel("Result"));
        filter.add(recordsResultCombo);
        JButton apply = new JButton("Apply");
        styleGhostButton(apply);
        filter.add(apply);
        apply.addActionListener(e -> refreshRecordsTable());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(new JScrollPane(recordsTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JButton export = new JButton("Export Records");
        stylePrimaryButton(export);
        export.addActionListener(e -> exportRecords());
        bottom.add(new JPanel(), BorderLayout.CENTER);
        bottom.add(export, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        north.add(statsRow, BorderLayout.CENTER);
        north.add(filter, BorderLayout.SOUTH);

        page.add(north, BorderLayout.NORTH);
        page.add(tablePanel, BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private void initTableVisuals() {
        appTable.setRowHeight(28);
        appTable.getTableHeader().setBackground(new Color(240, 241, 245));
        appTable.setGridColor(new Color(236, 238, 242));
        appTable.getTableHeader().setReorderingAllowed(false);
        appTable.getColumnModel().getColumn(0).setPreferredWidth(180); // TA Name
        appTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // Student ID
        appTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Applied Course
        appTable.getColumnModel().getColumn(3).setPreferredWidth(90);  // Match Score
        appTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Missing Skills
        appTable.getColumnModel().getColumn(5).setPreferredWidth(140); // Current Workload
        appTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Status
        appTable.getColumnModel().getColumn(7).setPreferredWidth(130); // Actions

        appTable.getColumnModel().getColumn(3).setCellRenderer(new ScoreCellRenderer());
        appTable.getColumnModel().getColumn(4).setCellRenderer(new MissingSkillsCellRenderer());
        appTable.getColumnModel().getColumn(5).setCellRenderer(new WorkloadCellRenderer());
        appTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        appTable.getColumnModel().getColumn(7).setCellRenderer(new ApplicationsActionsRenderer());

        recordsTable.setRowHeight(28);
        recordsTable.getTableHeader().setBackground(new Color(240, 241, 245));
        recordsTable.setGridColor(new Color(236, 238, 242));
        recordsTable.getTableHeader().setReorderingAllowed(false);
        recordsTable.getColumnModel().getColumn(0).setPreferredWidth(260); // Course Name/Code
        recordsTable.getColumnModel().getColumn(1).setPreferredWidth(140); // TA Name
        recordsTable.getColumnModel().getColumn(2).setPreferredWidth(90);  // Student ID
        recordsTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Review Date
        recordsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Result
        recordsTable.getColumnModel().getColumn(5).setPreferredWidth(170); // Reviewer
        recordsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Actions

        recordsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        recordsTable.getColumnModel().getColumn(6).setCellRenderer(new RecordsActionsRenderer());
    }

    /**
     * Install mouse handlers for action icon columns.
     */
    private void installTableActionHandlers() {
        appTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = appTable.rowAtPoint(e.getPoint());
                int col = appTable.columnAtPoint(e.getPoint());
                if (row < 0 || col != APP_ACTION_COL) {
                    return;
                }
                if (row >= appRows.size()) {
                    return;
                }
                ApplicationRecord app = appRows.get(row);
                String status = app.statusCurrent == null ? "" : app.statusCurrent.toLowerCase();
                boolean pending = status.contains("pending");

                java.awt.Rectangle rect = appTable.getCellRect(row, col, true);
                int offsetX = e.getX() - rect.x;
                int segment = Math.max(1, rect.width / 4);
                int iconIndex = Math.min(3, Math.max(0, offsetX / segment));

                // 0: view, 1: approve, 2: reject, 3: full review
                if (iconIndex == 0) {
                    loadDetailPage(app);
                    showPage(PAGE_DETAIL);
                } else if (iconIndex == 1) {
                    if (!pending) {
                        JOptionPane.showMessageDialog(DesktopApp.this, "Approve is disabled for current status.");
                        return;
                    }
                    quickDecisionFromModel(app, true);
                } else if (iconIndex == 2) {
                    if (!pending) {
                        JOptionPane.showMessageDialog(DesktopApp.this, "Reject is disabled for current status.");
                        return;
                    }
                    quickDecisionFromModel(app, false);
                } else if (iconIndex == 3) {
                    if (!pending) {
                        JOptionPane.showMessageDialog(DesktopApp.this, "Full review is disabled for current status.");
                        return;
                    }
                    loadReviewPage(app);
                    showPage(PAGE_REVIEW);
                }
            }
        });

        recordsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = recordsTable.rowAtPoint(e.getPoint());
                int col = recordsTable.columnAtPoint(e.getPoint());
                if (row < 0 || col != RECORD_ACTION_COL) {
                    return;
                }
                if (row >= recordRows.size()) {
                    return;
                }
                recordsTable.setRowSelectionInterval(row, row);
                openDetailFromRecordTable();
            }
        });
    }

    private void quickDecisionFromModel(ApplicationRecord app, boolean approve) {
        try {
            service.quickDecision(app, approve, approve ? "Approved by MO" : "Rejected by MO");
            refreshAll();
            showPage(PAGE_APPLICATIONS);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Write failed: " + ex.getMessage());
        }
    }

    private static class RecordsActionsRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setText("");
            label.setBackground(isSelected ? new Color(230, 232, 238) : Color.WHITE);
            label.setForeground(new Color(58, 65, 79));
            label.setOpaque(true);
            return label;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2;

            Color stroke = new Color(58, 65, 79);

            // icon button box
            int box = 22;
            int x = cx - box / 2;
            int y = cy - box / 2;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, box, box, 5, 5);
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, box, box, 5, 5);

            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(2.0f));

            // eye icon
            int ew = 16;
            int eh = 10;
            int ex = cx - ew / 2;
            int ey = cy - eh / 2;
            g2.drawOval(ex, ey, ew, eh);
            g2.fillOval(cx - 2, cy - 2, 4, 4);
        }
    }

    private static class ApplicationsActionsRenderer extends DefaultTableCellRenderer {
        private String statusLower = "";

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setText("");
            label.setBackground(isSelected ? new Color(230, 232, 238) : Color.WHITE);
            label.setOpaque(true);
            this.statusLower = value == null ? "" : String.valueOf(value).toLowerCase();
            return label;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int segment = Math.max(1, w / 4);

            boolean pending = statusLower.contains("pending");

            // enable flags (match screenshot behavior)
            boolean viewEnabled = true;
            boolean approveEnabled = pending;
            boolean rejectEnabled = pending;
            boolean reviewEnabled = pending;

            // centers per segment
            int cx0 = segment * 0 + segment / 2;
            int cx1 = segment * 1 + segment / 2;
            int cx2 = segment * 2 + segment / 2;
            int cx3 = segment * 3 + segment / 2;
            int cy = h / 2;

            drawIconButtonBox(g2, cx0, cy, viewEnabled);
            drawIconButtonBox(g2, cx1, cy, approveEnabled);
            drawIconButtonBox(g2, cx2, cy, rejectEnabled);
            drawIconButtonBox(g2, cx3, cy, reviewEnabled);

            // draw each icon
            drawEye(g2, cx0, cy, viewEnabled);
            drawCheck(g2, cx1, cy, approveEnabled);
            drawX(g2, cx2, cy, rejectEnabled);
            drawDoc(g2, cx3, cy, reviewEnabled);
        }

        private static void drawIconButtonBox(Graphics2D g2, int cx, int cy, boolean enabled) {
            int box = 22;
            int x = cx - box / 2;
            int y = cy - box / 2;
            Color bg = enabled ? Color.WHITE : new Color(245, 245, 245);
            Color border = enabled ? new Color(220, 220, 220) : new Color(232, 232, 232);
            g2.setColor(bg);
            g2.fillRoundRect(x, y, box, box, 5, 5);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, box, box, 5, 5);
        }

        private static void drawEye(Graphics2D g2, int cx, int cy, boolean enabled) {
            Color stroke = enabled ? new Color(58, 65, 79) : new Color(190, 190, 190);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(2.0f));
            int ew = 16;
            int eh = 10;
            int ex = cx - ew / 2;
            int ey = cy - eh / 2;
            g2.drawOval(ex, ey, ew, eh);
            g2.fillOval(cx - 2, cy - 2, 4, 4);
        }

        private static void drawCheck(Graphics2D g2, int cx, int cy, boolean enabled) {
            Color stroke = enabled ? new Color(15, 124, 65) : new Color(190, 190, 190);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(cx - 6, cy + 1, cx - 1, cy + 6);
            g2.drawLine(cx - 1, cy + 6, cx + 7, cy - 5);
        }

        private static void drawX(Graphics2D g2, int cx, int cy, boolean enabled) {
            Color stroke = enabled ? new Color(180, 35, 24) : new Color(190, 190, 190);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(cx - 6, cy - 6, cx + 6, cy + 6);
            g2.drawLine(cx - 6, cy + 6, cx + 6, cy - 6);
        }

        private static void drawDoc(Graphics2D g2, int cx, int cy, boolean enabled) {
            Color stroke = enabled ? new Color(58, 65, 79) : new Color(190, 190, 190);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(2.0f));
            int w = 14;
            int h = 18;
            int x = cx - w / 2;
            int y = cy - h / 2;
            g2.drawRoundRect(x, y, w, h, 2, 2);
            // fold corner
            g2.drawLine(x + w - 1, y + 1, x + w - 5, y + 1);
            g2.drawLine(x + w - 1, y + 1, x + w - 1, y + 5);
            // text lines
            g2.drawLine(x + 3, y + 6, x + 11, y + 6);
            g2.drawLine(x + 3, y + 10, x + 10, y + 10);
        }
    }

    private void showPage(String page) {
        currentPage = page;
        updateNavHighlight();
        cardLayout.show(contentPanel, page);
    }

    private void refreshAll() {
        Map<String, Long> appStats = service.getApplicationStats();
        dashboardActiveCourses.setText(String.valueOf(service.getActiveCourses()));
        dashboardOpenJobs.setText(String.valueOf(service.getOpenJobs()));
        dashboardPendingReviews.setText(String.valueOf(service.getPendingReviews()));
        dashboardCurrentMo.setText(service.getCurrentMoName());

        appTotalStat.setText(String.valueOf(appStats.get("total")));
        appPendingStat.setText(String.valueOf(appStats.get("pending")));
        appApprovedStat.setText(String.valueOf(appStats.get("approved")));
        appRejectedStat.setText(String.valueOf(appStats.get("rejected")));

        Map<String, Long> reviewStats = service.reviewStats();
        recordsTotalStat.setText(String.valueOf(reviewStats.get("total")));
        recordsApprovedStat.setText(String.valueOf(reviewStats.get("approved")));
        recordsRejectedStat.setText(String.valueOf(reviewStats.get("rejected")));
        MoProfile mo = service.findMoByUserId(service.getCurrentMoId());
        if (mo != null) {
            dashboardMoInfo.setText("Reviewer: " + mo.fullName + " | Dept: " + mo.department
                    + " | Email: " + mo.email + " | Phone: " + mo.phone);
        } else {
            dashboardMoInfo.setText("Reviewer: " + service.getCurrentMoName());
        }
        refreshApplicationsTable();
        refreshRecordsTable();
        recordsStatLabel.setText("");
    }

    private void refreshApplicationsTable() {
        Map<String, Long> stats = service.getApplicationStats();
        appStatsLabel.setText("Total Applications: " + stats.get("total")
                + " | Pending Reviews: " + stats.get("pending")
                + " | Approved: " + stats.get("approved")
                + " | Rejected: " + stats.get("rejected"));
        String keyword = appKeywordField.getText().trim();
        String course = appCourseField.getText().trim();
        String status = String.valueOf(appStatusCombo.getSelectedItem());
        appRows = service.filterApplications(keyword, course, status);
        appTableModel.setRowCount(0);
        for (ApplicationRecord a : appRows) {
            int[] score = service.scoreAndMissings(a);
            int workload = service.getCurrentWorkloadHours(a.studentId);
            int courses = service.getCurrentAssignments(a.studentId).size();
            String moName = service.getMoDisplayName(service.getAssignedMoId(a));
            Integer scoreValue = score[0];
            String missing = score[2] > 0 ? "Missing " + score[2] : "✓ All skills met";
            String workloadText = workload + " hrs/week";
            appTableModel.addRow(new Object[]{
                    a.taName,
                    a.studentId,
                    a.courseCode,
                    scoreValue,
                    missing,
                    workloadText + "\n" + courses + " course(s)",
                    a.statusLabel,
                    a.statusCurrent
            });
        }
    }

    private void loadDetailPage(ApplicationRecord app) {
        this.currentApplication = app;
        JobRecord job = service.findJob(app.jobId);
        TaProfile ta = service.findTaByUserId(app.userId);
        MoProfile mo = service.findMoByUserId(service.getAssignedMoId(app));
        int[] score = service.scoreAndMissings(app);
        int workload = service.getCurrentWorkloadHours(app.studentId);
        List<ApplicationRecord> assigned = service.getCurrentAssignments(app.studentId);
        StringBuilder appInfo = new StringBuilder();
        appInfo.append("Applied Course: ").append(app.courseName).append(" (").append(app.courseCode).append(")\n");
        appInfo.append("Application Date: ").append(app.applicationDate).append("\n");
        appInfo.append("Current Status: ").append(app.statusLabel).append("\n");
        appInfo.append("Assigned MO: ").append(mo == null ? service.getAssignedMoId(app) : mo.fullName).append("\n");
        if (mo != null) {
            appInfo.append("MO Contact: ").append(mo.email).append(" | ").append(mo.phone).append("\n");
        }
        StringBuilder skills = new StringBuilder();
        skills.append("Overall Match Score: ").append(score[0]).append("/100\n");
        if (job != null) {
            skills.append("Required Skills: ").append(String.join(", ", job.preferredSkills)).append("\n");
            skills.append("Course Department: ").append(job.department).append("\n");
            skills.append("Job Title: ").append(job.title).append("\n");
        }
        if (ta != null) {
            skills.append("Applicant Skills: ").append(String.join(", ", ta.skills)).append("\n");
        }
        skills.append(score[2] > 0 ? "Missing Skills Warning: " + score[2] + "\n" : "✓ All skills met\n");
        StringBuilder workloadInfo = new StringBuilder();
        workloadInfo.append("Total Weekly Workload: ").append(workload).append(" hours/week\n");
        if (assigned.isEmpty()) {
            workloadInfo.append("No current assignments.\n");
        } else {
            for (ApplicationRecord row : assigned) {
                workloadInfo.append("- ").append(row.courseName).append(", ").append(row.weeklyHours).append(" hrs/week\n");
            }
        }
        if (workload >= 15) {
            workloadInfo.append("Warning: workload >= 15 hours/week.\n");
        }
        StringBuilder personal = new StringBuilder();
        StringBuilder exp = new StringBuilder();
        if (ta != null) {
            personal.append("Name: ").append(ta.fullName).append("\n");
            personal.append("Student ID: ").append(ta.studentId).append("\n");
            personal.append("Major: ").append(ta.major).append("\n");
            personal.append("GPA: ").append(ta.gpa).append("\n");
            personal.append("Contact: ").append(ta.phone).append(" | ").append(ta.email).append("\n");
            exp.append("Experience: ").append(app.experience).append("\n");
            exp.append("CV Path: ").append(app.cvPath).append("\n");
        }
        detailApplicationInfo.setText(appInfo.toString());
        detailSkillsInfo.setText(skills.toString());
        detailWorkloadInfo.setText(workloadInfo.toString());
        detailPersonalInfo.setText(personal.toString());
        detailExperienceInfo.setText(exp.toString());
        detailApplicationInfo.setCaretPosition(0);
    }

    private void loadReviewPage(ApplicationRecord app) {
        this.currentApplication = app;
        JobRecord job = service.findJob(app.jobId);
        TaProfile ta = service.findTaByUserId(app.userId);
        MoProfile mo = service.findMoByUserId(service.getAssignedMoId(app));
        StringBuilder left = new StringBuilder();
        left.append("Course Requirements\n\n");
        left.append("Applied Course: ").append(app.courseName).append(" (").append(app.courseCode).append(")\n");
        if (job != null) {
            left.append("Required Skills: ").append(String.join(", ", job.preferredSkills)).append("\n");
            left.append("Weekly Workload: ").append(job.weeklyHours).append(" hours/week\n");
            left.append("TAs Needed: N/A (no field in source data)\n");
            left.append("Preferred Qualifications: ").append(String.join("; ", job.requirements)).append("\n");
            left.append("Responsibilities: ").append(String.join("; ", job.responsibilities)).append("\n");
        }
        StringBuilder right = new StringBuilder();
        right.append("Applicant Qualifications\n\n");
        right.append("Reviewer (MO): ").append(service.getCurrentMoName()).append("\n");
        if (mo != null) {
            right.append("Assigned MO: ").append(mo.fullName).append(" / ").append(mo.department).append("\n");
            right.append("MO Contact: ").append(mo.email).append(", ").append(mo.phone).append("\n");
        }
        right.append("TA Name: ").append(app.taName).append("\n");
        right.append("Student ID: ").append(app.studentId).append("\n");
        if (ta != null) {
            right.append("Skills: ").append(String.join(", ", ta.skills)).append("\n");
            right.append("Academic: ").append(ta.year).append(", GPA ").append(ta.gpa).append(", ").append(ta.major).append("\n");
        }
        right.append("Experience: ").append(app.experience).append("\n");
        reviewContextArea.setText("Applied Course: " + app.courseName + " (" + app.courseCode + ")\n"
                + "TA Name: " + app.taName + "    Student ID: " + app.studentId + "\n"
                + "Assigned MO: " + (mo == null ? service.getAssignedMoId(app) : mo.fullName));
        reviewLeftArea.setText(left.toString());
        reviewRightArea.setText(right.toString());
        reviewNotesArea.setText("");
        approveBtn.setSelected(true);
    }

    private void submitReview() {
        if (currentApplication == null) {
            return;
        }
        boolean approve = approveBtn.isSelected();
        String notes = reviewNotesArea.getText().trim();
        try {
            service.quickDecision(currentApplication, approve, notes);
            refreshAll();
            showPage(PAGE_APPLICATIONS);
            JOptionPane.showMessageDialog(this, "Review submitted successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to submit review: " + ex.getMessage());
        }
    }

    private void quickDecisionFromTable(boolean approve) {
        ApplicationRecord app = selectedApplication();
        if (app == null) {
            return;
        }
        try {
            service.quickDecision(app, approve, approve ? "Quick approved by MO" : "Quick rejected by MO");
            refreshAll();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Write failed: " + ex.getMessage());
        }
    }

    private void openDetailFromTable() {
        ApplicationRecord app = selectedApplication();
        if (app == null) {
            return;
        }
        loadDetailPage(app);
        showPage(PAGE_DETAIL);
    }

    private void openReviewFromTable() {
        ApplicationRecord app = selectedApplication();
        if (app == null) {
            return;
        }
        loadReviewPage(app);
        showPage(PAGE_REVIEW);
    }

    private ApplicationRecord selectedApplication() {
        int row = appTable.getSelectedRow();
        if (row < 0 || row >= appRows.size()) {
            JOptionPane.showMessageDialog(this, "Please select one application row first.");
            return null;
        }
        return appRows.get(row);
    }

    private void refreshRecordsTable() {
        int dayRange = 0;
        if (recordsTimeCombo.getSelectedIndex() == 1) {
            dayRange = 7;
        } else if (recordsTimeCombo.getSelectedIndex() == 2) {
            dayRange = 30;
        }
        String keyword = recordsKeywordField.getText().trim();
        String result = String.valueOf(recordsResultCombo.getSelectedItem());
        recordRows = service.filterReviews(keyword, result, dayRange);
        recordsTableModel.setRowCount(0);
        for (ReviewRecord r : recordRows) {
            String courseCell = r.courseName + "\n" + r.courseCode;
            String dateCell = formatReviewDate(r.reviewDate);
            recordsTableModel.addRow(new Object[]{
                    courseCell,
                    r.taName,
                    r.studentId,
                    dateCell,
                    r.result,
                    r.reviewer,
                    "view"
            });
        }
        Map<String, Long> stats = service.reviewStats();
        recordsStatLabel.setText("Total Reviews: " + stats.get("total")
                + " | Approved Applications: " + stats.get("approved")
                + " | Rejected Applications: " + stats.get("rejected"));
    }

    private static String formatReviewDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }
        String v = raw.trim();
        if (v.length() >= 10) {
            return v.substring(0, 10);
        }
        return v;
    }

    private void openDetailFromRecordTable() {
        int row = recordsTable.getSelectedRow();
        if (row < 0 || row >= recordRows.size()) {
            JOptionPane.showMessageDialog(this, "Please select one review record.");
            return;
        }
        String appId = recordRows.get(row).applicationId;
        ApplicationRecord app = service.findApplicationById(appId);
        if (app == null) {
            JOptionPane.showMessageDialog(this, "Application not found: " + appId);
            return;
        }
        loadDetailPage(app);
        showPage(PAGE_DETAIL);
    }

    private void exportRecords() {
        try {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Path dir = Paths.get("export");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path csv = dir.resolve("review_records_" + ts + ".csv");
            Path txt = dir.resolve("review_records_" + ts + ".txt");
            Path json = dir.resolve("review_records_" + ts + ".json");
            StringBuilder csvContent = new StringBuilder("applicationId,courseName,courseCode,taName,studentId,reviewDate,result,reviewer,notes\n");
            StringBuilder txtContent = new StringBuilder();
            StringBuilder jsonContent = new StringBuilder("[\n");
            for (int i = 0; i < recordRows.size(); i++) {
                ReviewRecord r = recordRows.get(i);
                csvContent.append(q(r.applicationId)).append(",").append(q(r.courseName)).append(",")
                        .append(q(r.courseCode)).append(",").append(q(r.taName)).append(",")
                        .append(q(r.studentId)).append(",").append(q(r.reviewDate)).append(",")
                        .append(q(r.result)).append(",").append(q(r.reviewer)).append(",")
                        .append(q(r.notes)).append("\n");
                txtContent.append(r.applicationId).append(" | ").append(r.courseName).append(" | ")
                        .append(r.taName).append(" | ").append(r.result).append(" | ").append(r.reviewDate).append("\n");
                jsonContent.append("  {\"applicationId\":\"").append(escape(r.applicationId)).append("\",")
                        .append("\"courseName\":\"").append(escape(r.courseName)).append("\",")
                        .append("\"courseCode\":\"").append(escape(r.courseCode)).append("\",")
                        .append("\"taName\":\"").append(escape(r.taName)).append("\",")
                        .append("\"studentId\":\"").append(escape(r.studentId)).append("\",")
                        .append("\"reviewDate\":\"").append(escape(r.reviewDate)).append("\",")
                        .append("\"result\":\"").append(escape(r.result)).append("\",")
                        .append("\"reviewer\":\"").append(escape(r.reviewer)).append("\",")
                        .append("\"notes\":\"").append(escape(r.notes)).append("\"}");
                if (i < recordRows.size() - 1) {
                    jsonContent.append(",");
                }
                jsonContent.append("\n");
            }
            jsonContent.append("]\n");
            Files.write(csv, csvContent.toString().getBytes(StandardCharsets.UTF_8));
            Files.write(txt, txtContent.toString().getBytes(StandardCharsets.UTF_8));
            Files.write(json, jsonContent.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Export success:\n" + csv + "\n" + json + "\n" + txt);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        }
    }

    private static String q(String s) {
        return "\"" + escape(s) + "\"";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }

    private JPanel sectionPanel(String title, Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 8, 0),
                BorderFactory.createTitledBorder(new LineBorder(BORDER, 1, true), title)));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void setupReadOnlyArea(JTextArea area) {
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void styleGhostButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(DARK);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(BORDER, 1, true));
    }

    private void styleNavButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private void updateNavHighlight() {
        navHome.setOpaque(true);
        navJob.setOpaque(true);
        navReview.setOpaque(true);
        navLogout.setOpaque(true);
        navHome.setBackground(Color.WHITE);
        navJob.setBackground(Color.WHITE);
        navReview.setBackground(Color.WHITE);
        if (PAGE_DASHBOARD.equals(currentPage)) {
            navHome.setBackground(new Color(230, 232, 238));
        } else if (PAGE_APPLICATIONS.equals(currentPage) || PAGE_DETAIL.equals(currentPage)
                || PAGE_REVIEW.equals(currentPage) || PAGE_RECORDS.equals(currentPage)) {
            navReview.setBackground(new Color(230, 232, 238));
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                return c;
            }
            String text = value == null ? "" : value.toString().toLowerCase();
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);
            if (text.contains("approved")) {
                c.setBackground(new Color(223, 245, 229));
                c.setForeground(new Color(15, 124, 65));
            } else if (text.contains("rejected")) {
                c.setBackground(new Color(252, 228, 228));
                c.setForeground(new Color(180, 35, 24));
            } else if (text.contains("pending")) {
                c.setBackground(new Color(255, 245, 215));
                c.setForeground(new Color(145, 102, 8));
            }
            return c;
        }
    }

    private static class ScoreCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                return c;
            }
            int score = 0;
            try {
                if (value instanceof Number) {
                    score = ((Number) value).intValue();
                } else {
                    score = Integer.parseInt(String.valueOf(value).trim());
                }
            } catch (Exception ignored) {
            }

            if (score >= 80) {
                c.setBackground(new Color(223, 245, 229));
                c.setForeground(new Color(15, 124, 65));
            } else if (score >= 60) {
                c.setBackground(new Color(255, 245, 215));
                c.setForeground(new Color(145, 102, 8));
            } else {
                c.setBackground(new Color(252, 228, 228));
                c.setForeground(new Color(180, 35, 24));
            }
            return c;
        }
    }

    private static class MissingSkillsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                return c;
            }
            String text = value == null ? "" : value.toString();
            if (text.startsWith("✓")) {
                c.setBackground(new Color(223, 245, 229));
                c.setForeground(new Color(15, 124, 65));
            } else {
                c.setBackground(new Color(252, 228, 228));
                c.setForeground(new Color(180, 35, 24));
            }
            return c;
        }
    }

    private static class WorkloadCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                return c;
            }
            String text = value == null ? "" : value.toString();
            c.setForeground(Color.BLACK);
            c.setBackground(Color.WHITE);
            try {
                int hours = Integer.parseInt(text.split(" ")[0]);
                if (hours == 0) {
                    c.setBackground(new Color(238, 241, 245));
                } else if (hours >= 15) {
                    c.setBackground(new Color(255, 234, 210));
                    c.setForeground(new Color(173, 93, 17));
                } else {
                    c.setBackground(new Color(230, 239, 255));
                    c.setForeground(new Color(34, 88, 170));
                }
            } catch (NumberFormatException ignored) {
            }
            return c;
        }
    }

    private static class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                return label;
            }
            label.setBackground(new Color(247, 248, 251));
            label.setForeground(new Color(58, 65, 79));
            return label;
        }
    }

    /**
     * Start UI safely on EDT.
     *
     * @param service service
     */
    public static void launch(ApplicationReviewService service) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException
                     | InstantiationException
                     | IllegalAccessException
                     | UnsupportedLookAndFeelException ignored) {
            }
            DesktopApp app = new DesktopApp(service);
            app.setVisible(true);
        });
    }
}
