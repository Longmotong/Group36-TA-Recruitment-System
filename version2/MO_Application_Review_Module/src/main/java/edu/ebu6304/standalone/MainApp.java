package edu.ebu6304.standalone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import edu.ebu6304.standalone.model.ApplicationItem;
import edu.ebu6304.standalone.model.DashboardMetrics;
import edu.ebu6304.standalone.service.DataImportService;

public class MainApp {
    private final DataImportService dataService = new DataImportService(resolveDataRoot());

    private List<ApplicationItem> allApplications = new ArrayList<>();
    private List<ApplicationItem> applicationTableData = new ArrayList<>();
    private DashboardMetrics metrics;

    private JFrame frame;
    private JPanel mainFrame;

    private JButton navHomeBtn;
    private JButton navJobBtn;
    private JButton navReviewBtn;

    private JLabel managedJobsLabel;
    private JLabel totalAppsLabel;
    private JLabel pendingLabel;
    private JLabel approvedLabel;
    private JLabel rejectedLabel;

    private JTextField searchField;
    private JComboBox<String> courseFilter;
    private JComboBox<String> statusFilter;

    private ApplicationTableModel applicationTableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().start());
    }

    private void start() {
        loadData();

        frame = new JFrame("TA Management Stand-alone Application");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1080, 760);
        frame.setMinimumSize(new Dimension(1200, 760));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xF6F8FB));

        mainFrame = new JPanel(new BorderLayout());
        mainFrame.setBackground(new Color(0xF6F8FB));
        mainFrame.add(buildTopNav(), BorderLayout.NORTH);
        setCenterContent(buildDashboardView());

        root.add(mainFrame, BorderLayout.CENTER);
        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private void loadData() {
        allApplications = dataService.loadApplications();
        metrics = dataService.buildDashboardMetrics("u_mo_001", allApplications);
        applicationTableData = new ArrayList<>(allApplications);
    }

    private JPanel buildTopNav() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB)));
        wrapper.setBackground(Color.WHITE);

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("MO System");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(0x111827));

        navHomeBtn = createTopNavButton("Home");
        navHomeBtn.addActionListener(e -> {
            setActiveNav("home");
            setCenterContent(buildDashboardView());
        });

        navJobBtn = createTopNavButton("Job Management");
        navJobBtn.addActionListener(e -> {
            setActiveNav("job");
            setCenterContent(buildJobModulePlaceholder());
        });

        navReviewBtn = createTopNavButton("Application Review");
        navReviewBtn.addActionListener(e -> {
            setActiveNav("review");
            setCenterContent(buildApplicationReviewView());
        });

        row.add(title);
        row.add(Box.createHorizontalGlue());
        row.add(navHomeBtn);
        row.add(Box.createHorizontalStrut(14));
        row.add(navJobBtn);
        row.add(Box.createHorizontalStrut(14));
        row.add(navReviewBtn);

        wrapper.add(row, BorderLayout.CENTER);
        setActiveNav("home");
        return wrapper;
    }

    private JButton createTopNavButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(Math.max(170, text.length() * 10 + 42), 48));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(11, 22, 11, 22)
        ));
        return button;
    }

    private void setActiveNav(String key) {
        setButtonActive(navHomeBtn, "home".equals(key));
        setButtonActive(navJobBtn, "job".equals(key));
        setButtonActive(navReviewBtn, "review".equals(key));
    }

    private void setButtonActive(JButton button, boolean active) {
        if (button == null) return;
        if (active) {
            button.setBackground(new Color(0x111827));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x111827)),
                    new EmptyBorder(11, 22, 11, 22)
            ));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(0x374151));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                    new EmptyBorder(11, 22, 11, 22)
            ));
        }
    }

    private JScrollPane buildDashboardView() {
        JPanel page = createPageContainer();
        page.setBorder(new EmptyBorder(0, 24, 24, 24));

        JPanel headerRow = new JPanel();
        headerRow.setOpaque(false);
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));

        JPanel headText = new JPanel();
        headText.setOpaque(false);
        headText.setLayout(new BoxLayout(headText, BoxLayout.Y_AXIS));

        JLabel header = styledLabel("Dashboard", 36, Font.BOLD, 0x111827);
        JLabel subtitle = styledLabel("Overview for current recruitment cycle (from real data)", 15, Font.PLAIN, 0x6B7280);
        headText.add(Box.createVerticalStrut(14));
        headText.add(header);
        headText.add(Box.createVerticalStrut(6));
        headText.add(subtitle);

        JButton logoutBtn = ghostButton("Log out");
        logoutBtn.setForeground(new Color(0xB91C1C));
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        logoutBtn.setPreferredSize(new Dimension(176, 50));
        logoutBtn.setMaximumSize(new Dimension(176, 50));
        logoutBtn.setAlignmentY(0.0f);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFCA5A5)),
                new EmptyBorder(10, 20, 10, 20)
        ));
        logoutBtn.addActionListener(e -> setCenterContent(buildLoggedOutView()));

        headerRow.add(headText);
        headerRow.add(Box.createHorizontalGlue());
        headerRow.add(logoutBtn);

        JPanel moduleRow = new JPanel(new GridLayout(1, 2, 28, 0));
        moduleRow.setOpaque(false);
        moduleRow.add(buildJobManagementCard());
        moduleRow.add(buildApplicationReviewCard());

        JPanel quickOverview = new JPanel(new GridLayout(1, 3, 28, 0));
        quickOverview.setOpaque(false);
        quickOverview.add(smallCard("Managed Jobs", String.valueOf(metrics.managedJobs()), new Color(0x111827)));
        quickOverview.add(smallCard("Total Applications", String.valueOf(metrics.totalApplications()), new Color(0x2563EB)));
        quickOverview.add(smallCard("Pending Reviews", String.valueOf(metrics.pendingReviews()), new Color(0xCA8A04)));

        JLabel quickTitle = styledLabel("Quick Overview", 22, Font.BOLD, 0x111827);

        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        moduleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickOverview.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(headerRow);
        page.add(Box.createVerticalStrut(32));
        page.add(moduleRow);
        page.add(Box.createVerticalStrut(48));
        page.add(quickTitle);
        page.add(Box.createVerticalStrut(26));
        page.add(quickOverview);
        page.add(Box.createVerticalStrut(28));

        return wrapInScroll(page);
    }

    private JPanel buildJobManagementCard() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(24, 24, 24, 24)
        ));
        card.setPreferredSize(new Dimension(620, 270));

        JLabel moduleTitle = styledLabel("Job Management Module", 30, Font.BOLD, 0x111827);
        moduleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel moduleDescMain = styledLabel("Manage course info, requirements, and job postings", 24, Font.BOLD, 0x374151);
        moduleDescMain.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel moduleDescBullets = multilineLabel("· Create and edit course information\n· Set TA requirements and qualifications\n· Post and manage job openings", 22, 0x4B5563);
        moduleDescBullets.setAlignmentX(Component.LEFT_ALIGNMENT);

        managedJobsLabel = styledLabel("Managed Jobs: " + metrics.managedJobs(), 28, Font.BOLD, 0x111827);
        managedJobsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(moduleTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(moduleDescMain);
        card.add(Box.createVerticalStrut(10));
        card.add(moduleDescBullets);
        card.add(Box.createVerticalStrut(12));
        card.add(managedJobsLabel);

        card.add(Box.createVerticalGlue());

        JButton open = ghostButton("Go to Job Management");
        open.setFont(new Font("SansSerif", Font.BOLD, 20));
        open.setAlignmentX(Component.LEFT_ALIGNMENT);
        open.setPreferredSize(new Dimension(360, 60));
        open.setMinimumSize(new Dimension(360, 60));
        open.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        open.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(12, 26, 12, 26)
        ));
        open.addActionListener(e -> {
            setActiveNav("job");
            setCenterContent(buildJobModulePlaceholder());
        });
        card.add(open);
        return card;
    }

    private JPanel buildApplicationReviewCard() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(24, 24, 24, 24)
        ));
        card.setPreferredSize(new Dimension(620, 270));

        JLabel moduleTitle = styledLabel("Application Review Module", 30, Font.BOLD, 0x111827);
        moduleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel moduleDescMain = styledLabel("View TA applications, review, and check allocation results", 24, Font.BOLD, 0x374151);
        moduleDescMain.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel moduleDescBullets = multilineLabel("· Browse and filter TA applications\n· Review and approve/reject applications\n· View TA allocation results", 22, 0x4B5563);
        moduleDescBullets.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(moduleTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(moduleDescMain);
        card.add(Box.createVerticalStrut(10));
        card.add(moduleDescBullets);
        card.add(Box.createVerticalStrut(12));

        JPanel counters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        counters.setOpaque(false);
        counters.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalAppsLabel = metricPill("Total " + metrics.totalApplications(), new Color(0xDBEAFE), new Color(0x1E40AF));
        pendingLabel = metricPill("Pending " + metrics.pendingReviews(), new Color(0xFEF3C7), new Color(0x92400E));
        approvedLabel = metricPill("Approved " + metrics.approvedCount(), new Color(0xDCFCE7), new Color(0x166534));
        rejectedLabel = metricPill("Rejected " + metrics.rejectedCount(), new Color(0xFEE2E2), new Color(0x991B1B));
        counters.add(totalAppsLabel);
        counters.add(pendingLabel);
        counters.add(approvedLabel);
        counters.add(rejectedLabel);
        card.add(counters);

        card.add(Box.createVerticalGlue());

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setPreferredSize(new Dimension(0, 60));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JButton openReview = primaryButton("Go to Application Review");
        openReview.setFont(new Font("SansSerif", Font.BOLD, 20));
        openReview.setPreferredSize(new Dimension(360, 60));
        openReview.setMinimumSize(new Dimension(360, 60));
        openReview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        openReview.addActionListener(e -> {
            setActiveNav("review");
            setCenterContent(buildApplicationReviewView());
        });

        JButton records = ghostButton("My Review Records");
        records.setFont(new Font("SansSerif", Font.BOLD, 20));
        records.setPreferredSize(new Dimension(360, 60));
        records.setMinimumSize(new Dimension(360, 60));
        records.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        records.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(12, 26, 12, 26)
        ));
        records.addActionListener(e -> {
            setActiveNav("review");
            setCenterContent(buildMyReviewRecordsView());
        });

        actions.add(openReview);
        actions.add(records);
        card.add(actions);
        return card;
    }

    private JLabel metricPill(String text, Color bg, Color fg) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(fg);
        label.setBorder(new EmptyBorder(8, 14, 8, 14));
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        return label;
    }

    private JScrollPane buildApplicationReviewView() {
        JPanel page = createPageContainer();

        JButton backHomeBtn = ghostButton("← Back to Home");
        backHomeBtn.addActionListener(e -> {
            setActiveNav("home");
            setCenterContent(buildDashboardView());
        });

        backHomeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel appTitle = styledLabel("TA Applications", 30, Font.BOLD, 0x111827);
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel appSubtitle = styledLabel("Review and manage Teaching Assistant applications", 13, Font.PLAIN, 0x6B7280);
        appSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(backHomeBtn);
        page.add(Box.createVerticalStrut(10));
        page.add(appTitle);
        page.add(appSubtitle);
        page.add(Box.createVerticalStrut(14));
        JPanel reviewSummary = buildReviewSummaryCards();
        reviewSummary.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(reviewSummary);
        page.add(Box.createVerticalStrut(14));

        JPanel filters = cardPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.X_AXIS));
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(320, 34));
        searchField.setMaximumSize(new Dimension(320, 34));

        courseFilter = new JComboBox<>(new String[]{"", "CS101", "CS201", "MATH205"});
        courseFilter.setPreferredSize(new Dimension(170, 34));
        courseFilter.setMaximumSize(new Dimension(170, 34));

        statusFilter = new JComboBox<>(new String[]{"", "pending", "approved", "rejected"});
        statusFilter.setPreferredSize(new Dimension(170, 34));
        statusFilter.setMaximumSize(new Dimension(170, 34));

        JButton filterBtn = primaryButton("Apply Filter");
        filterBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = ghostButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            courseFilter.setSelectedItem("");
            statusFilter.setSelectedItem("");
            applicationTableData = new ArrayList<>(allApplications);
            if (applicationTableModel != null) applicationTableModel.fireTableDataChanged();
        });

        JButton recordsBtn = ghostButton("My Review Records");
        recordsBtn.addActionListener(e -> setCenterContent(buildMyReviewRecordsView()));

        filters.add(searchField);
        filters.add(Box.createHorizontalStrut(8));
        filters.add(courseFilter);
        filters.add(Box.createHorizontalStrut(8));
        filters.add(statusFilter);
        filters.add(Box.createHorizontalStrut(8));
        filters.add(filterBtn);
        filters.add(Box.createHorizontalStrut(8));
        filters.add(resetBtn);
        filters.add(Box.createHorizontalGlue());
        filters.add(recordsBtn);

        filters.setPreferredSize(new Dimension(0, 80));
        filters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        page.add(filters);
        page.add(Box.createVerticalStrut(14));
        JPanel tablePanel = buildApplicationTablePanel();
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(tablePanel);

        return wrapInScroll(page);
    }

    private JPanel buildReviewSummaryCards() {
        int total = allApplications.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;
        for (ApplicationItem item : allApplications) {
            String status = resolveStatus(item);
            if ("pending".equals(status)) pending++;
            if ("approved".equals(status)) approved++;
            if ("rejected".equals(status)) rejected++;
        }

        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.add(smallCard("Total", String.valueOf(total), new Color(0x111827)));
        row.add(smallCard("Pending", String.valueOf(pending), new Color(0xCA8A04)));
        row.add(smallCard("Approved", String.valueOf(approved), new Color(0x15803D)));
        row.add(smallCard("Rejected", String.valueOf(rejected), new Color(0xDC2626)));
        return row;
    }

    private JPanel smallCard(String label, String value, Color valueColor) {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel l = styledLabel(label, 20, Font.BOLD, 0x6B7280);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 32));
        v.setForeground(valueColor);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(l);
        card.add(Box.createVerticalStrut(1));
        card.add(v);
        card.add(Box.createVerticalGlue());
        card.setPreferredSize(new Dimension(180, 30));
        card.setMinimumSize(new Dimension(180, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return card;
    }

    private JPanel buildApplicationTablePanel() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 500));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        applicationTableModel = new ApplicationTableModel();
        JTable table = new JTable(applicationTableModel);
        table.setRowHeight(40);

        DefaultTableCellRenderer centeredRenderer = new DefaultTableCellRenderer();
        centeredRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centeredRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centeredRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centeredRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(95);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(340);

        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionEditor(this::openDetail, this::openReview, this::quickApprove, this::quickReject,
                row -> row >= 0 && row < applicationTableData.size() ? resolveStatus(applicationTableData.get(row)) : "pending"));

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void openDetail(int row) {
        if (row < 0 || row >= applicationTableData.size()) return;
        setCenterContent(buildApplicationDetailView(applicationTableData.get(row)));
    }

    private void openReview(int row) {
        if (row < 0 || row >= applicationTableData.size()) return;
        setCenterContent(buildReviewApplicationView(applicationTableData.get(row)));
    }

    private void quickApprove(int row) {
        if (row < 0 || row >= applicationTableData.size()) return;
        ApplicationItem item = applicationTableData.get(row);
        dataService.submitReview(item, "approved", "Quick approved from list");
        loadData();
        refreshDashboardCards();
        applyFilters();
    }

    private void quickReject(int row) {
        if (row < 0 || row >= applicationTableData.size()) return;
        ApplicationItem item = applicationTableData.get(row);
        dataService.submitReview(item, "rejected", "Quick rejected from list");
        loadData();
        refreshDashboardCards();
        applyFilters();
    }

    private JScrollPane buildApplicationDetailView(ApplicationItem item) {
        JPanel page = createPageContainer();

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        head.setOpaque(false);

        JButton backBtn = ghostButton("← Back to Applications");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));
        JButton toReviewBtn = primaryButton("Go to Review Page");
        toReviewBtn.addActionListener(e -> setCenterContent(buildReviewApplicationView(item)));
        head.add(backBtn);
        head.add(toReviewBtn);

        page.add(head);
        page.add(Box.createVerticalStrut(8));
        page.add(styledLabel("TA Application Detail", 28, Font.BOLD, 0x111827));
        page.add(Box.createVerticalStrut(8));

        JPanel info = cardPanel();
        info.setLayout(new GridLayout(0, 2, 24, 10));
        addInfoRow(info, "Applicant", safe(getApplicantName(item)));
        addInfoRow(info, "Student ID", safe(item.getStudentId()));
        addInfoRow(info, "Email", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getEmail()));
        addInfoRow(info, "Phone", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getPhoneNumber()));
        addInfoRow(info, "Major", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getProgramMajor()));
        addInfoRow(info, "GPA", item.getApplicantSnapshot() == null || item.getApplicantSnapshot().getGpa() == null ? "" : String.valueOf(item.getApplicantSnapshot().getGpa()));
        addInfoRow(info, "Course", getCourseText(item));
        addInfoRow(info, "Current Status", resolveStatus(item));

        page.add(info);
        page.add(Box.createVerticalStrut(10));

        page.add(textAreaBlock("Relevant Skills", getSkillsText(item)));
        page.add(Box.createVerticalStrut(10));
        page.add(textAreaBlock("Relevant Experience", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getRelevantExperience())));
        page.add(Box.createVerticalStrut(10));
        page.add(textAreaBlock("Motivation Cover Letter", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getMotivationCoverLetter())));

        return wrapInScroll(page);
    }

    private JScrollPane buildReviewApplicationView(ApplicationItem item) {
        JPanel page = createPageContainer();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);

        JButton backBtn = ghostButton("← Back to Applications");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));

        JButton detailBtn = ghostButton("⌕  View Detail");
        detailBtn.addActionListener(e -> setCenterContent(buildApplicationDetailView(item)));

        top.add(backBtn);
        top.add(detailBtn);

        JLabel reviewTitle = styledLabel("Review TA Application", 28, Font.BOLD, 0x111827);
        reviewTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel reviewSubtitle = styledLabel("Evaluate the applicant and make a decision on their application", 13, Font.PLAIN, 0x6B7280);
        reviewSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(top);
        page.add(Box.createVerticalStrut(8));
        page.add(reviewTitle);
        page.add(reviewSubtitle);
        page.add(Box.createVerticalStrut(8));

        JPanel summary = cardPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.add(sectionTitle("Application Summary"));
        summary.add(Box.createVerticalStrut(6));
        summary.add(new JLabel("Application ID: " + safe(item.getApplicationId())));
        summary.add(new JLabel("Applicant: " + safe(getApplicantName(item))));
        summary.add(new JLabel("Course: " + getCourseText(item)));
        summary.add(new JLabel("Current Status: " + resolveStatus(item)));

        JPanel decisionCard = cardPanel();
        decisionCard.setLayout(new BoxLayout(decisionCard, BoxLayout.Y_AXIS));
        decisionCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        decisionCard.add(sectionTitle("Decision"));
        decisionCard.add(Box.createVerticalStrut(8));

        JRadioButton approve = new JRadioButton("Approve", true);
        JRadioButton reject = new JRadioButton("Reject");
        ButtonGroup group = new ButtonGroup();
        group.add(approve);
        group.add(reject);

        JPanel choices = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        choices.setOpaque(false);
        choices.add(approve);
        choices.add(reject);
        decisionCard.add(choices);
        decisionCard.add(Box.createVerticalStrut(8));

        JLabel notesLabel = new JLabel("Review Notes");
        notesLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        decisionCard.add(notesLabel);
        decisionCard.add(Box.createVerticalStrut(6));

        JTextArea notesArea = new JTextArea(6, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        decisionCard.add(new JScrollPane(notesArea));
        decisionCard.add(Box.createVerticalStrut(10));

        JButton submit = primaryButton("Submit Review Decision");
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        submit.addActionListener(e -> {
            String decision = approve.isSelected() ? "approved" : "rejected";
            dataService.submitReview(item, decision, notesArea.getText());
            loadData();
            refreshDashboardCards();
            setCenterContent(buildApplicationReviewView());
        });
        decisionCard.add(submit);

        page.add(summary);
        page.add(Box.createVerticalStrut(10));
        page.add(decisionCard);

        return wrapInScroll(page);
    }

    private JScrollPane buildMyReviewRecordsView() {
        JPanel page = createPageContainer();

        JButton backBtn = ghostButton("← Back to Application Review");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));

        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel recordsTitle = styledLabel("Review TA Application", 30, Font.BOLD, 0x111827);
        recordsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel recordsSubtitle = styledLabel("Evaluate the applicant and make a decision on their application", 13, Font.PLAIN, 0x6B7280);
        recordsSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(backBtn);
        page.add(Box.createVerticalStrut(8));
        page.add(recordsTitle);
        page.add(recordsSubtitle);
        page.add(Box.createVerticalStrut(10));

        List<ApplicationItem> reviewedList = allApplications.stream().filter(this::isReviewed).toList();
        int approved = (int) reviewedList.stream().filter(a -> "approved".equals(resolveStatus(a))).count();
        int rejected = (int) reviewedList.stream().filter(a -> "rejected".equals(resolveStatus(a))).count();

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.add(smallCard("Total Reviews", String.valueOf(reviewedList.size()), new Color(0x111827)));
        stats.add(smallCard("Approved", String.valueOf(approved), new Color(0x15803D)));
        stats.add(smallCard("Rejected", String.valueOf(rejected), new Color(0xDC2626)));

        page.add(stats);
        page.add(Box.createVerticalStrut(10));

        JTable recordsTable = new JTable(new AbstractTableModel() {
            private final String[] cols = {"Application ID", "Course", "TA Name", "Review Date", "Result", "Reviewer", "Actions"};

            @Override
            public int getRowCount() {
                return reviewedList.size();
            }

            @Override
            public int getColumnCount() {
                return cols.length;
            }

            @Override
            public String getColumnName(int column) {
                return cols[column];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                ApplicationItem item = reviewedList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> safe(item.getApplicationId());
                    case 1 -> item.getJobSnapshot() == null ? "" : safe(item.getJobSnapshot().getCourseCode());
                    case 2 -> getApplicantName(item);
                    case 3 -> getReviewDate(item);
                    case 4 -> resolveStatus(item);
                    case 5 -> item.getReview() == null ? "" : safe(item.getReview().getReviewedBy());
                    default -> "Detail";
                };
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 6;
            }
        });

        recordsTable.setRowHeight(40);
        recordsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        recordsTable.getColumnModel().getColumn(6).setCellRenderer(new SimpleDetailRenderer());
        recordsTable.getColumnModel().getColumn(6).setCellEditor(new SimpleDetailEditor(row -> setCenterContent(buildApplicationDetailView(reviewedList.get(row)))));

        page.add(new JScrollPane(recordsTable));
        return wrapInScroll(page);
    }

    private JScrollPane buildLoggedOutView() {
        JPanel page = createPageContainer();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        page.add(Box.createVerticalGlue());
        JLabel title = styledLabel("Logged out", 34, Font.BOLD, 0x111827);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel desc = styledLabel("You have logged out of MO account.", 15, Font.PLAIN, 0x6B7280);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginAgain = primaryButton("Back to Dashboard");
        loginAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginAgain.addActionListener(e -> {
            setActiveNav("home");
            setCenterContent(buildDashboardView());
        });

        page.add(title);
        page.add(Box.createVerticalStrut(10));
        page.add(desc);
        page.add(Box.createVerticalStrut(16));
        page.add(loginAgain);
        page.add(Box.createVerticalGlue());

        return wrapInScroll(page);
    }

    private JScrollPane buildJobModulePlaceholder() {
        JPanel page = createPageContainer();
        page.add(styledLabel("Job Management Module", 30, Font.BOLD, 0x111827));
        page.add(styledLabel("Not in current scope. As requested, this module is not implemented yet.", 14, Font.PLAIN, 0x6B7280));
        return wrapInScroll(page);
    }

    private JPanel createPageContainer() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(24, 24, 28, 24));
        page.setBackground(new Color(0xF6F8FB));
        return page;
    }

    private JScrollPane wrapInScroll(JPanel page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(0x111827));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setPreferredSize(new Dimension(Math.max(160, text.length() * 9 + 30), 44));
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(0x374151));
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        b.setPreferredSize(new Dimension(Math.max(160, text.length() * 9 + 30), 44));
        return b;
    }

    private JLabel sectionTitle(String text) {
        return styledLabel(text, 16, Font.BOLD, 0x111827);
    }

    private JPanel textAreaBlock(String title, String content) {
        JPanel block = cardPanel();
        block.setLayout(new BorderLayout(0, 8));

        block.add(sectionTitle(title), BorderLayout.NORTH);
        JTextArea area = new JTextArea(content);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        block.add(new JScrollPane(area), BorderLayout.CENTER);
        return block;
    }

    private void addInfoRow(JPanel panel, String key, String value) {
        JLabel k = new JLabel(key + ":");
        k.setForeground(new Color(0x6B7280));
        k.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel v = new JLabel(value);
        v.setForeground(new Color(0x111827));
        v.setFont(new Font("SansSerif", Font.PLAIN, 13));

        panel.add(k);
        panel.add(v);
    }

    private JLabel styledLabel(String text, int size, int style, int colorHex) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(new Color(colorHex));
        return label;
    }

    private JLabel multilineLabel(String text, int size, int colorHex) {
        JLabel label = new JLabel("<html>" + text.replace("\n", "<br>") + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        label.setForeground(new Color(colorHex));
        return label;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    private String resolveStatus(ApplicationItem item) {
        String status = item.getStatus() == null ? "" : safe(item.getStatus().getCurrent()).toLowerCase(Locale.ROOT);
        if ("under_review".equals(status)) return "pending";
        if (status.isBlank() && item.getReview() != null) {
            String decision = safe(item.getReview().getDecision()).toLowerCase(Locale.ROOT);
            if ("approved".equals(decision) || "rejected".equals(decision)) return decision;
            return "pending";
        }
        return status.isBlank() ? "pending" : status;
    }

    private boolean isReviewed(ApplicationItem item) {
        String status = resolveStatus(item);
        if ("approved".equals(status) || "rejected".equals(status)) return true;
        if (item.getReview() == null) return false;
        String decision = safe(item.getReview().getDecision()).toLowerCase(Locale.ROOT);
        return "approved".equals(decision) || "rejected".equals(decision);
    }

    private String getReviewDate(ApplicationItem item) {
        if (item.getReview() != null && !safe(item.getReview().getReviewedAt()).isBlank()) {
            return item.getReview().getReviewedAt();
        }
        if (item.getMeta() != null && !safe(item.getMeta().getUpdatedAt()).isBlank()) {
            return item.getMeta().getUpdatedAt();
        }
        return "";
    }

    private String getApplicantName(ApplicationItem item) {
        return item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getFullName());
    }

    private String getCourseText(ApplicationItem item) {
        if (item.getJobSnapshot() == null) return "";
        return safe(item.getJobSnapshot().getCourseCode()) + " - " + safe(item.getJobSnapshot().getCourseName());
    }

    private String getSkillsText(ApplicationItem item) {
        if (item.getApplicationForm() == null || item.getApplicationForm().getRelevantSkills() == null) {
            return "";
        }
        return String.join(", ", item.getApplicationForm().getRelevantSkills());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static Path resolveDataRoot() {
        String configured = System.getProperty("app.data.root", "").trim();
        if (!configured.isEmpty()) {
            Path configuredPath = Path.of(configured);
            if (configuredPath.toFile().exists()) {
                return configuredPath;
            }
        }

        Path[] candidates = new Path[]{
                Path.of("..", "..", "data2", "data"),
                Path.of("..", "data2", "data"),
                Path.of("..", "data"),
                Path.of("data")
        };

        for (Path candidate : candidates) {
            if (candidate.toFile().exists()) {
                return candidate;
            }
        }
        return Path.of("..", "..", "data2", "data");
    }

    private void applyFilters() {
        List<ApplicationItem> filtered = dataService.filterApplications(
                allApplications,
                searchField == null ? "" : searchField.getText(),
                courseFilter == null ? "" : (String) courseFilter.getSelectedItem(),
                statusFilter == null ? "" : (String) statusFilter.getSelectedItem()
        );
        applicationTableData = new ArrayList<>(filtered);
        if (applicationTableModel != null) applicationTableModel.fireTableDataChanged();
    }

    private void refreshDashboardCards() {
        metrics = dataService.buildDashboardMetrics("u_mo_001", allApplications);
        if (managedJobsLabel != null) managedJobsLabel.setText("Managed Jobs: " + metrics.managedJobs());
        if (totalAppsLabel != null) totalAppsLabel.setText("Total " + metrics.totalApplications());
        if (pendingLabel != null) pendingLabel.setText("Pending " + metrics.pendingReviews());
        if (approvedLabel != null) approvedLabel.setText("Approved " + metrics.approvedCount());
        if (rejectedLabel != null) rejectedLabel.setText("Rejected " + metrics.rejectedCount());
    }

    private void setCenterContent(JComponent component) {
        BorderLayout layout = (BorderLayout) mainFrame.getLayout();
        Component oldCenter = layout.getLayoutComponent(BorderLayout.CENTER);
        if (oldCenter != null) {
            mainFrame.remove(oldCenter);
        }
        mainFrame.add(component, BorderLayout.CENTER);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private class ApplicationTableModel extends AbstractTableModel {
        private final String[] cols = {"TA Name", "Student ID", "Applied Course", "Status", "Actions"};

        @Override
        public int getRowCount() {
            return applicationTableData.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ApplicationItem item = applicationTableData.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> getApplicantName(item);
                case 1 -> safe(item.getStudentId());
                case 2 -> item.getJobSnapshot() == null ? "" : safe(item.getJobSnapshot().getCourseCode());
                case 3 -> resolveStatus(item);
                default -> "Actions";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 4;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value == null ? "pending" : value.toString().toLowerCase(Locale.ROOT);

            switch (status) {
                case "approved" -> {
                    label.setBackground(new Color(0xDCFCE7));
                    label.setForeground(new Color(0x166534));
                    label.setText("Approved");
                }
                case "rejected" -> {
                    label.setBackground(new Color(0xFEE2E2));
                    label.setForeground(new Color(0x991B1B));
                    label.setText("Rejected");
                }
                default -> {
                    label.setBackground(new Color(0xFEF3C7));
                    label.setForeground(new Color(0x92400E));
                    label.setText("Pending");
                }
            }
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        }
    }

    private static class ActionRenderer extends JPanel implements TableCellRenderer {
        private final JButton detail = new JButton("⌕ Detail");
        private final JButton review = new JButton("✎ Review");
        private final JButton approve = new JButton("✓ Approve");
        private final JButton reject = new JButton("✕ Reject");

        ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
            for (JButton b : List.of(detail, review, approve, reject)) {
                b.setFocusPainted(false);
                b.setMargin(new Insets(3, 6, 3, 6));
                add(b);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private static class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        private final JButton detail = new JButton("⌕ Detail");
        private final JButton review = new JButton("✎ Review");
        private final JButton approve = new JButton("✓ Approve");
        private final JButton reject = new JButton("✕ Reject");

        private final java.util.function.Function<Integer, String> statusProvider;
        private int row;

        ActionEditor(Consumer<Integer> detailAction,
                     Consumer<Integer> reviewAction,
                     Consumer<Integer> approveAction,
                     Consumer<Integer> rejectAction,
                     java.util.function.Function<Integer, String> statusProvider) {
            this.statusProvider = statusProvider;

            for (JButton b : List.of(detail, review, approve, reject)) {
                b.setFocusPainted(false);
                b.setMargin(new Insets(3, 6, 3, 6));
                panel.add(b);
            }

            detail.addActionListener(e -> {
                detailAction.accept(row);
                fireEditingStopped();
            });
            review.addActionListener(e -> {
                reviewAction.accept(row);
                fireEditingStopped();
            });
            approve.addActionListener(e -> {
                approveAction.accept(row);
                fireEditingStopped();
            });
            reject.addActionListener(e -> {
                rejectAction.accept(row);
                fireEditingStopped();
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            String status = statusProvider == null ? "pending" : String.valueOf(statusProvider.apply(row));
            approve.setEnabled(!"approved".equalsIgnoreCase(status));
            reject.setEnabled(!"rejected".equalsIgnoreCase(status));
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    private static class SimpleDetailRenderer extends JPanel implements TableCellRenderer {
        private final JButton detail = new JButton("⌕ Detail");

        SimpleDetailRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
            detail.setFocusPainted(false);
            detail.setMargin(new Insets(3, 6, 3, 6));
            add(detail);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private static class SimpleDetailEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        private final JButton detail = new JButton("⌕ Detail");
        private int row;

        SimpleDetailEditor(Consumer<Integer> detailAction) {
            detail.setFocusPainted(false);
            detail.setMargin(new Insets(3, 6, 3, 6));
            panel.add(detail);
            detail.addActionListener(e -> {
                detailAction.accept(row);
                fireEditingStopped();
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "Detail";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }
}
