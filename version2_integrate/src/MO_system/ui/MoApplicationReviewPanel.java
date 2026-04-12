package com.mojobsystem.ui;

import com.mojobsystem.MoContext;
import com.mojobsystem.model.Job;
import com.mojobsystem.model.applicationreview.ApplicationItem;
import com.mojobsystem.repository.JobRepository;
import com.mojobsystem.service.ApplicationReviewDataService;


import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.Icon;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Application Review flows merged from standalone {@code MainApp}; embedded in {@link MoShellFrame}.
 */
public final class MoApplicationReviewPanel extends JPanel {

    private final MoShellHost host;
    private final ApplicationReviewDataService dataService;
    private final String fromJobId;
    private final JobRepository jobRepository = new JobRepository();

    private List<ApplicationItem> allApplications = new ArrayList<>();
    private List<ApplicationItem> applicationTableData = new ArrayList<>();

    private JPanel mainFrame;

    private JTextField searchField;
    private JComboBox<String> courseFilter;
    private JComboBox<String> statusFilter;

    private ApplicationTableModel applicationTableModel;
    private final Map<String, Job> jobById = new HashMap<>();

    /** Main list table + wrapper; used to re-sync column width after resize/show. */
    private JTable applicationListTable;
    private JScrollPane applicationListTableScroll;

    /** Value labels for Total / Pending / Approved / Rejected summary row (index 0–3). */
    private final JLabel[] summaryValueLabels = new JLabel[4];

    /** Filter row: one height for text field, combos, and buttons (aligned with FlatLaf controls). */
    private static final int FILTER_CONTROL_H = 38;

    /** Light gray for search placeholder and course/status combo hints (same color everywhere). */
    private static final Color FILTER_PLACEHOLDER_FG = new Color(0xB8C0CC);

    /**
     * Hint-only label on the closed combo (like search placeholder); not a model item. Dropdown rows use index &gt;= 0.
     */
    private static void installFilterComboHint(JComboBox<String> combo, String hint) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                c.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                c.setHorizontalAlignment(SwingConstants.LEADING);
                if (index < 0) {
                    if (combo.getSelectedIndex() < 0) {
                        c.setText(hint);
                        c.setForeground(FILTER_PLACEHOLDER_FG);
                    } else {
                        Object sel = combo.getSelectedItem();
                        c.setText(sel == null ? "" : sel.toString());
                        c.setForeground(MoUiTheme.TEXT_PRIMARY);
                    }
                    return c;
                }
                c.setText(value == null ? "" : value.toString());
                c.setForeground(MoUiTheme.TEXT_PRIMARY);
                return c;
            }
        });

        // Keep closed-combo foreground in sync with hint state (no selection -> placeholder gray).
        combo.setForeground(FILTER_PLACEHOLDER_FG);
        combo.addActionListener(e -> combo.setForeground(
                combo.getSelectedIndex() < 0 ? FILTER_PLACEHOLDER_FG : MoUiTheme.TEXT_PRIMARY));
    }

    public MoApplicationReviewPanel(MoShellHost host, ApplicationReviewDataService dataService, String fromJobId) {
        super(new BorderLayout());
        this.host = host;
        this.dataService = dataService;
        this.fromJobId = fromJobId;
        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        mainFrame = new JPanel(new BorderLayout());
        mainFrame.setOpaque(false);
        loadData();
        setCenterContent(buildApplicationReviewView());
        add(mainFrame, BorderLayout.CENTER);
    }

    private void loadData() {
        String moId = MoContext.getCurrentMoUserId();
        Set<String> jobIds = jobRepository.loadMoJobIds(moId);
        String restrict = (fromJobId != null && !fromJobId.isBlank()) ? fromJobId : null;
        allApplications = dataService.loadApplicationsForMo(moId, jobIds, restrict);
        applicationTableData = new ArrayList<>(allApplications);

        jobById.clear();
        for (Job j : jobRepository.loadJobsForMo(moId)) {
            if (j != null && j.getId() != null) {
                jobById.put(j.getId(), j);
            }
        }
    }

    /** Distinct course codes from loaded applications for the filter combo (no blank entry — placeholder is painted in the combo). */
    private String[] computeCourseCodes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (ApplicationItem a : allApplications) {
            if (a.getJobSnapshot() != null && a.getJobSnapshot().getCourseCode() != null
                    && !a.getJobSnapshot().getCourseCode().isBlank()) {
                set.add(a.getJobSnapshot().getCourseCode());
            }
        }
        return set.toArray(new String[0]);
    }

    /**
     * Keeps {@link JTable#AUTO_RESIZE_ALL_COLUMNS} in sync with the scroll pane width. Does not expand
     * viewport height to all rows — the table scrolls inside its own {@link JScrollPane}.
     */
    private static void syncApplicationTableViewportWidth(JTable table, JScrollPane tableScroll) {
        if (table == null || tableScroll == null) {
            return;
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.doLayout();
        if (table.getTableHeader() != null) {
            table.getTableHeader().doLayout();
        }
        Insets tin = tableScroll.getInsets();
        int availW = tableScroll.getWidth() - tin.left - tin.right;
        if (availW <= 0) {
            Container parent = tableScroll.getParent();
            if (parent != null && parent.getWidth() > 0) {
                Insets pin = parent.getInsets();
                availW = parent.getWidth() - pin.left - pin.right;
            }
        }
        if (availW <= 0) {
            int w = 0;
            for (int i = 0; i < table.getColumnCount(); i++) {
                w += table.getColumnModel().getColumn(i).getPreferredWidth();
            }
            availW = Math.max(w, 400);
        }
        int rowH = Math.max(1, table.getRowHeight());
        int headerH = table.getTableHeader().getPreferredSize().height;
        int defaultH = headerH + rowH * 8;
        table.setPreferredScrollableViewportSize(new Dimension(availW, defaultH));
        tableScroll.revalidate();
    }

    /**
     * Content inside a {@link JScrollPane}: {@link Scrollable#getScrollableTracksViewportWidth()} makes the viewport
     * stretch this panel to full width (no grey gutter on the right). Height stays content-driven.
     */
    private static final class ViewportWidthMatchPanel extends JPanel implements Scrollable {
        ViewportWidthMatchPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return switch (orientation) {
                case SwingConstants.VERTICAL -> visibleRect.height;
                case SwingConstants.HORIZONTAL -> visibleRect.width;
                default -> visibleRect.height;
            };
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * TA Applications main list (screenshot): same layout idea as {@link MyJobsPanel} — header/stats/filters
     * stay at the top; the table sits in the remaining height and scrolls inside its own {@link JScrollPane}.
     */
    private JPanel buildApplicationReviewView() {
        ViewportWidthMatchPanel panel = new ViewportWidthMatchPanel(new GridBagLayout());
        // Slightly tighter vertical insets than default page padding so the table gets more height.
        panel.setBorder(new EmptyBorder(16, MoUiTheme.PAGE_INSET_X, 18, MoUiTheme.PAGE_INSET_X));

        JButton backHomeBtn = MoUiTheme.createBackToHomeButton(() -> host.showDashboard());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backHomeBtn);

        JLabel appTitle = new JLabel("TA Applications");
        appTitle.setForeground(MoUiTheme.TEXT_PRIMARY);
        appTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        JLabel appSubtitle = new JLabel("Review and manage Teaching Assistant applications");
        appSubtitle.setForeground(MoUiTheme.TEXT_SECONDARY);
        appSubtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

        JButton recordsBtn = new JButton("My Review Records");
        recordsBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        recordsBtn.setFocusPainted(false);
        MoUiTheme.stylePrimaryButton(recordsBtn, 10);
        recordsBtn.setPreferredSize(new Dimension(200, 42));
        recordsBtn.addActionListener(e -> setCenterContent(buildMyReviewRecordsView()));

        JPanel titleLeft = new JPanel();
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.setOpaque(false);
        titleLeft.add(appTitle);
        titleLeft.add(Box.createVerticalStrut(6));
        titleLeft.add(appSubtitle);

        JPanel headerCard = new JPanel(new BorderLayout(28, 0));
        headerCard.setOpaque(true);
        headerCard.setBackground(MoUiTheme.SURFACE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(14, 20, 14, 20)
        ));
        headerCard.putClientProperty("JComponent.style", "arc: 12");
        headerCard.add(titleLeft, BorderLayout.CENTER);
        headerCard.add(recordsBtn, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(backRow, BorderLayout.NORTH);
        north.add(headerCard, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(north, c);

        c.gridy = 1;
        c.insets = new Insets(8, 0, 0, 0);
        panel.add(buildReviewSummaryCards(), c);

        c.gridy = 2;
        c.insets = new Insets(10, 0, 0, 0);
        panel.add(buildFiltersPanel(), c);

        c.gridy = 3;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(8, 0, 0, 0);
        panel.add(buildApplicationTableScrollPane(), c);

        SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(applicationListTable, applicationListTableScroll));
        return panel;
    }

    private JPanel buildFiltersPanel() {
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(true);
        filters.setBackground(Color.WHITE);
        filters.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        searchField = new JTextField();
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search name or ID…");
        searchField.putClientProperty("JComponent.style",
                "placeholderForeground: #" + String.format("%06X", FILTER_PLACEHOLDER_FG.getRGB() & 0xFFFFFF));
        Dimension searchPref = new Dimension(300, FILTER_CONTROL_H);
        searchField.setPreferredSize(searchPref);
        searchField.setMinimumSize(new Dimension(200, FILTER_CONTROL_H));
        searchField.setMaximumSize(new Dimension(420, FILTER_CONTROL_H));

        courseFilter = new JComboBox<>(computeCourseCodes());
        courseFilter.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        courseFilter.setPreferredSize(new Dimension(200, FILTER_CONTROL_H));
        courseFilter.setMinimumSize(new Dimension(168, FILTER_CONTROL_H));
        installFilterComboHint(courseFilter, "Course");
        courseFilter.setSelectedIndex(-1);

        statusFilter = new JComboBox<>(new String[]{"pending", "approved", "rejected"});
        statusFilter.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statusFilter.setPreferredSize(new Dimension(176, FILTER_CONTROL_H));
        statusFilter.setMinimumSize(new Dimension(148, FILTER_CONTROL_H));
        installFilterComboHint(statusFilter, "Status");
        statusFilter.setSelectedIndex(-1);

        JButton filterBtn = new JButton("Apply Filter");
        filterBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        filterBtn.setFocusPainted(false);
        MoUiTheme.stylePrimaryButton(filterBtn, 10);
        filterBtn.setPreferredSize(new Dimension(128, FILTER_CONTROL_H));
        filterBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        resetBtn.setFocusPainted(false);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.setForeground(new Color(0x374151));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(6, 14, 6, 14)
        ));
        resetBtn.setPreferredSize(new Dimension(88, FILTER_CONTROL_H));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            courseFilter.setSelectedIndex(-1);
            statusFilter.setSelectedIndex(-1);
            applicationTableData = new ArrayList<>(allApplications);
            if (applicationTableModel != null) {
                applicationTableModel.fireTableDataChanged();
            }
        });

        JPanel eastGlue = new JPanel();
        eastGlue.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 0, 10);

        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        filters.add(searchField, gc);

        gc.gridx = 1;
        filters.add(courseFilter, gc);

        gc.gridx = 2;
        gc.insets = new Insets(0, 0, 0, 10);
        filters.add(statusFilter, gc);

        gc.gridx = 3;
        gc.insets = new Insets(0, 4, 0, 8);
        filters.add(filterBtn, gc);

        gc.gridx = 4;
        gc.insets = new Insets(0, 0, 0, 0);
        filters.add(resetBtn, gc);

        gc.gridx = 5;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, 0);
        filters.add(eastGlue, gc);

        return filters;
    }

    private int[] computeSummaryCounts() {
        int total = allApplications.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;
        for (ApplicationItem item : allApplications) {
            String status = ApplicationReviewDataService.normalizeStatusForMetrics(item);
            if ("pending".equals(status)) {
                pending++;
            }
            if ("approved".equals(status)) {
                approved++;
            }
            if ("rejected".equals(status)) {
                rejected++;
            }
        }
        return new int[]{total, pending, approved, rejected};
    }

    /** Updates the four summary cards when data changes (e.g. after quick Approve/Reject). */
    private void refreshSummaryCounts() {
        int[] c = computeSummaryCounts();
        for (int i = 0; i < 4; i++) {
            if (summaryValueLabels[i] != null) {
                summaryValueLabels[i].setText(String.valueOf(c[i]));
            }
        }
    }

    private JPanel buildReviewSummaryCards() {
        int[] c = computeSummaryCounts();

        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 96));
        row.add(smallCard("Total", String.valueOf(c[0]), new Color(0x111827), 0));
        row.add(smallCard("Pending", String.valueOf(c[1]), new Color(0xCA8A04), 1));
        row.add(smallCard("Approved", String.valueOf(c[2]), new Color(0x15803D), 2));
        row.add(smallCard("Rejected", String.valueOf(c[3]), new Color(0xDC2626), 3));
        return row;
    }

    /** Summary row cards on the main list: {@code slot} 0–3 updates {@link #summaryValueLabels} for live refresh. */
    private JPanel smallCard(String label, String value, Color valueColor, int slot) {
        return smallCardImpl(label, value, valueColor, slot);
    }

    /** Compact stat cards (e.g. Review Records page) — not wired to {@link #refreshSummaryCounts()}. */
    private JPanel smallCard(String label, String value, Color valueColor) {
        return smallCardImpl(label, value, valueColor, null);
    }

    private JPanel smallCardImpl(String label, String value, Color valueColor, Integer summarySlot) {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel l = styledLabel(label, 14, Font.BOLD, 0x6B7280);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel v = new JLabel(value);
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        v.setForeground(valueColor);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (summarySlot != null && summarySlot >= 0 && summarySlot < 4) {
            summaryValueLabels[summarySlot] = v;
        }

        if (summarySlot != null) {
            card.add(Box.createVerticalStrut(2));
            card.add(l);
            card.add(Box.createVerticalStrut(4));
            card.add(v);
            card.add(Box.createVerticalStrut(4));
            card.setPreferredSize(new Dimension(200, 86));
            card.setMinimumSize(new Dimension(160, 76));
        } else {
            card.add(Box.createVerticalStrut(4));
            card.add(l);
            card.add(Box.createVerticalStrut(6));
            card.add(v);
            card.add(Box.createVerticalStrut(8));
            card.setPreferredSize(new Dimension(200, 100));
            card.setMinimumSize(new Dimension(160, 88));
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        return card;
    }

    /** Same pattern as {@link MyJobsPanel}: table in {@link JScrollPane} with border, no outer card wrapper. */
    private JScrollPane buildApplicationTableScrollPane() {
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder()));

        applicationTableModel = new ApplicationTableModel();
        JTable table = new JTable(applicationTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(false);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(MoUiTheme.BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(MoUiTheme.TEXT_PRIMARY);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(MoUiTheme.TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER));

        DefaultTableCellRenderer centeredRenderer = new DefaultTableCellRenderer();
        centeredRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centeredRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centeredRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centeredRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centeredRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(95);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(340);

        table.getColumnModel().getColumn(3).setCellRenderer(new MatchScoreRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new MatchScoreEditor(this::openMatchScoreReason));
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor(this::openDetail, this::openReview, this::quickApprove, this::quickReject,
                row -> row >= 0 && row < applicationTableData.size()
                        ? ApplicationReviewDataService.normalizeStatusForMetrics(applicationTableData.get(row)) : "pending"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(MoUiTheme.BORDER));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        applicationListTable = table;
        applicationListTableScroll = scrollPane;

        syncApplicationTableViewportWidth(table, scrollPane);
        applicationTableModel.addTableModelListener(e ->
                SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(table, scrollPane)));
        scrollPane.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && scrollPane.isShowing()) {
                SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(table, scrollPane));
            }
        });
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(table, scrollPane));
            }
        });

        return scrollPane;
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
        if (!canApproveForQuota(item)) {
            JOptionPane.showMessageDialog(this, "TA quota has been reached", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            dataService.submitReview(item, "approved", "Quick approved from list", MoContext.getCurrentMoUserId());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save review: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        afterReviewSaved();
    }

    private void quickReject(int row) {
        if (row < 0 || row >= applicationTableData.size()) return;
        ApplicationItem item = applicationTableData.get(row);

        String applicantName = getApplicantName(item);
        String courseText = getCourseText(item);

        UIManager.put("OptionPane.yesButtonText", "Yes, Reject");
        UIManager.put("OptionPane.noButtonText", "Cancel");

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reject this application?\n\n" +
                "Applicant: " + applicantName + "\n" +
                "Course: " + courseText + "\n\n" +
                "This action cannot be undone.",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dataService.submitReview(item, "rejected", "Quick rejected from list", MoContext.getCurrentMoUserId());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save review: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        afterReviewSaved();
    }

    private void afterReviewSaved() {
        loadData();
        refreshSummaryCounts();
        host.jobDataChanged();
        applyFilters();
    }

    private JScrollPane buildApplicationDetailView(ApplicationItem item) {
        // Use GridBagLayout (not BoxLayout): BoxLayout + pref width 0 on the button row was collapsing /
        // centering the header strip. Same Scrollable pattern as the main list for full viewport width.
        int padX = MoUiTheme.PAGE_INSET_X - 8;
        ViewportWidthMatchPanel page = new ViewportWidthMatchPanel(new GridBagLayout());
        page.setOpaque(true);
        page.setBackground(new Color(0xF6F8FB));
        // Match TA Application Detail: reduce top whitespace above the back/detail button row.
        page.setBorder(new EmptyBorder(12, padX, MoUiTheme.PAGE_INSET_BOTTOM, padX));

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);

        JButton backBtn = ghostButton("← Back to Applications");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));
        JButton toReviewBtn = primaryButton("Go to Review Page");
        toReviewBtn.addActionListener(e -> setCenterContent(buildReviewApplicationView(item)));
        head.add(backBtn, BorderLayout.WEST);
        head.add(toReviewBtn, BorderLayout.EAST);

        JLabel detailTitle = styledLabel("TA Application Detail", 28, Font.BOLD, 0x111827);
        detailTitle.setHorizontalAlignment(SwingConstants.LEFT);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 0;
        page.add(head, c);

        c.gridy = 1;
        c.insets = new Insets(8, 0, 0, 0);
        page.add(detailTitle, c);

        JPanel info = buildApplicationDetailInfoCard(item);
        c.gridy = 2;
        c.insets = new Insets(8, 0, 0, 0);
        page.add(info, c);

        c.gridy = 3;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(textAreaBlock("Relevant Skills", getSkillsText(item)), c);

        c.gridy = 4;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(buildSkillsMatchingAnalysisCard(item), c);

        c.gridy = 5;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(textAreaBlock("Relevant Experience", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getRelevantExperience())), c);

        c.gridy = 6;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(textAreaBlock("Motivation Cover Letter", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getMotivationCoverLetter())), c);

        return wrapInScroll(page);
    }

    private JScrollPane buildReviewApplicationView(ApplicationItem item) {
        // Match detail page: GridBag + same horizontal inset so the top bar and cards share one left edge.
        int padX = MoUiTheme.PAGE_INSET_X - 8;
        ViewportWidthMatchPanel page = new ViewportWidthMatchPanel(new GridBagLayout());
        page.setOpaque(true);
        page.setBackground(new Color(0xF6F8FB));
        page.setBorder(new EmptyBorder(MoUiTheme.PAGE_INSET_TOP, padX, MoUiTheme.PAGE_INSET_BOTTOM, padX));

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        JButton backBtn = ghostButton("← Back to Applications");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));
        JButton detailBtn = ghostButton("⌕  View Detail");
        detailBtn.addActionListener(e -> setCenterContent(buildApplicationDetailView(item)));
        head.add(backBtn, BorderLayout.WEST);
        head.add(detailBtn, BorderLayout.EAST);

        JLabel reviewTitle = styledLabel("Review TA Application", 28, Font.BOLD, 0x111827);
        reviewTitle.setHorizontalAlignment(SwingConstants.LEFT);
        JLabel reviewSubtitle = styledLabel("Evaluate the applicant and make a decision on their application", 13, Font.PLAIN, 0x6B7280);
        reviewSubtitle.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel summary = cardPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.add(sectionTitle("Application Summary"));
        summary.add(Box.createVerticalStrut(6));
        summary.add(leftPaddedSummaryLine("Application ID: " + safe(item.getApplicationId())));
        summary.add(leftPaddedSummaryLine("Applicant: " + safe(getApplicantName(item))));
        summary.add(leftPaddedSummaryLine("Course: " + getCourseText(item)));
        summary.add(leftPaddedSummaryLine("Current Status: " + ApplicationReviewDataService.normalizeStatusForMetrics(item)));

        JPanel decisionCard = cardPanel();
        decisionCard.setLayout(new BoxLayout(decisionCard, BoxLayout.Y_AXIS));
        decisionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel decisionHeading = sectionTitle("Decision");
        decisionHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        decisionCard.add(decisionHeading);
        decisionCard.add(Box.createVerticalStrut(8));

        JRadioButton approve = new JRadioButton("Approve", true);
        approve.setHorizontalAlignment(SwingConstants.LEFT);
        approve.setMargin(new Insets(0, 0, 0, 0));
        JRadioButton reject = new JRadioButton("Reject");
        reject.setHorizontalAlignment(SwingConstants.LEFT);
        reject.setMargin(new Insets(0, 0, 0, 0));
        ButtonGroup group = new ButtonGroup();
        group.add(approve);
        group.add(reject);

        JPanel choices = new JPanel(new GridBagLayout());
        choices.setOpaque(false);
        choices.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints rg = new GridBagConstraints();
        rg.gridy = 0;
        rg.anchor = GridBagConstraints.LINE_START;
        rg.gridx = 0;
        rg.weightx = 0;
        rg.insets = new Insets(0, 0, 0, 0);
        choices.add(approve, rg);
        rg.gridx = 1;
        rg.insets = new Insets(0, 16, 0, 0);
        choices.add(reject, rg);
        JPanel choicesWrap = new JPanel(new BorderLayout());
        choicesWrap.setOpaque(false);
        choicesWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        choicesWrap.add(choices, BorderLayout.WEST);
        decisionCard.add(choicesWrap);
        decisionCard.add(Box.createVerticalStrut(8));

        JLabel notesLabel = new JLabel("Review Notes");
        notesLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesLabel.setHorizontalAlignment(SwingConstants.LEFT);
        decisionCard.add(notesLabel);
        decisionCard.add(Box.createVerticalStrut(6));

        JTextArea notesArea = new JTextArea(10, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        notesScroll.setPreferredSize(new Dimension(0, 180));
        notesScroll.setMinimumSize(new Dimension(0, 160));
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        decisionCard.add(notesScroll);
        decisionCard.add(Box.createVerticalStrut(10));

        JButton submit = primaryButton("Submit Review Decision");
        submit.addActionListener(e -> {
            String decision = approve.isSelected() ? "approved" : "rejected";
            if ("approved".equals(decision) && !canApproveForQuota(item)) {
                JOptionPane.showMessageDialog(this, "TA quota has been reached", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                dataService.submitReview(item, decision, notesArea.getText(), MoContext.getCurrentMoUserId());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save review: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            afterReviewSaved();
            setCenterContent(buildApplicationReviewView());
        });
        JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        submitRow.setOpaque(false);
        submitRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitRow.add(submit);
        decisionCard.add(submitRow);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 0;
        page.add(head, c);

        c.gridy = 1;
        c.insets = new Insets(8, 0, 0, 0);
        page.add(reviewTitle, c);

        c.gridy = 2;
        c.insets = new Insets(2, 0, 0, 0);
        page.add(reviewSubtitle, c);

        c.gridy = 3;
        c.insets = new Insets(8, 0, 0, 0);
        page.add(summary, c);

        c.gridy = 4;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(buildCourseAndApplicantCards(item), c);

        c.gridy = 5;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(decisionCard, c);

        return wrapInScroll(page);
    }

    private JPanel buildCourseAndApplicantCards(ApplicationItem item) {
        Job job = item == null ? null : jobById.get(item.getJobId());

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setOpaque(false);

        JPanel courseReqCard = cardPanel();
        courseReqCard.setLayout(new BoxLayout(courseReqCard, BoxLayout.Y_AXIS));
        courseReqCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        courseReqCard.add(sectionTitle("Course Requirements"));
        courseReqCard.add(Box.createVerticalStrut(8));

        List<String> reqSkills = (job == null || job.getRequiredSkills() == null) ? List.of() : job.getRequiredSkills();
        String reqSkillsText = reqSkills.isEmpty() ? "Not specified" : String.join(", ", reqSkills);

        String weeklyHours = (job == null || job.getWeeklyHours() <= 0) ? "Not specified" : (job.getWeeklyHours() + "h/week");
        String taQuota = (job == null || job.getQuota() <= 0) ? "Not specified" : String.valueOf(job.getQuota());

        String idealQualText;
        if (job == null) {
            idealQualText = "Not specified";
        } else {
            List<String> idealQuals = new ArrayList<>();
            if (!safe(job.getAdditionalRequirements()).isBlank()) {
                idealQuals.add(job.getAdditionalRequirements());
            }
            if (!safe(job.getDepartment()).isBlank()) {
                idealQuals.add("Department preference: " + safe(job.getDepartment()));
            }
            idealQualText = idealQuals.isEmpty() ? "Not specified" : String.join("; ", idealQuals);
        }

        String duties = (job == null || safe(job.getDescription()).isBlank()) ? "Not specified" : safe(job.getDescription());

        courseReqCard.add(leftPaddedSummaryLine("Required Skills: " + reqSkillsText));
        courseReqCard.add(Box.createVerticalStrut(4));
        courseReqCard.add(leftPaddedSummaryLine("Weekly Workload: " + weeklyHours));
        courseReqCard.add(Box.createVerticalStrut(4));
        courseReqCard.add(leftPaddedSummaryLine("TA Headcount Needed: " + taQuota));
        courseReqCard.add(Box.createVerticalStrut(4));
        courseReqCard.add(leftPaddedSummaryLine("Ideal Qualifications: " + idealQualText));
        courseReqCard.add(Box.createVerticalStrut(4));
        courseReqCard.add(leftPaddedSummaryLine("Role Responsibilities: " + duties));

        JPanel applicantQualCard = cardPanel();
        applicantQualCard.setLayout(new BoxLayout(applicantQualCard, BoxLayout.Y_AXIS));
        applicantQualCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        applicantQualCard.add(sectionTitle("Applicant Qualifications"));
        applicantQualCard.add(Box.createVerticalStrut(8));

        String skillLevel = getSkillsText(item);
        if (skillLevel.isBlank()) {
            skillLevel = "Not provided";
        }

        String academic = "Major: " + (item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getProgramMajor()))
                + "; Year: " + (item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getYear()))
                + "; GPA: " + (item.getApplicantSnapshot() == null || item.getApplicantSnapshot().getGpa() == null
                ? "Not provided" : item.getApplicantSnapshot().getGpa());

        String experience = (item.getApplicationForm() == null || safe(item.getApplicationForm().getRelevantExperience()).isBlank())
                ? "Not provided"
                : safe(item.getApplicationForm().getRelevantExperience());

        applicantQualCard.add(leftPaddedSummaryLine("Skills Profile: " + skillLevel));
        applicantQualCard.add(Box.createVerticalStrut(4));
        applicantQualCard.add(leftPaddedSummaryLine("Academic Background: " + academic));
        applicantQualCard.add(Box.createVerticalStrut(4));
        applicantQualCard.add(leftPaddedSummaryLine("Experience Summary: " + experience));

        grid.add(courseReqCard);
        grid.add(applicantQualCard);
        return grid;
    }

    private JPanel buildMyReviewRecordsView() {
        ViewportWidthMatchPanel panel = new ViewportWidthMatchPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(MoUiTheme.PAGE_INSET_TOP, MoUiTheme.PAGE_INSET_X, MoUiTheme.PAGE_INSET_BOTTOM, MoUiTheme.PAGE_INSET_X));

        JButton backBtn = ghostButton("← Back to Application Review");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backBtn);

        JLabel recordsTitle = new JLabel("My Review Records");
        recordsTitle.setForeground(MoUiTheme.TEXT_PRIMARY);
        recordsTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        JLabel recordsSubtitle = new JLabel("Completed reviews for TA applications");
        recordsSubtitle.setForeground(MoUiTheme.TEXT_SECONDARY);
        recordsSubtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

        JPanel titleLeft = new JPanel();
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.setOpaque(false);
        titleLeft.add(recordsTitle);
        titleLeft.add(Box.createVerticalStrut(6));
        titleLeft.add(recordsSubtitle);

        JPanel headerCard = new JPanel(new BorderLayout(28, 0));
        headerCard.setOpaque(true);
        headerCard.setBackground(MoUiTheme.SURFACE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(18, 22, 18, 22)
        ));
        headerCard.putClientProperty("JComponent.style", "arc: 12");
        headerCard.add(titleLeft, BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(backRow, BorderLayout.NORTH);
        north.add(headerCard, BorderLayout.CENTER);

        List<ApplicationItem> reviewedList = allApplications.stream().filter(this::isReviewed).toList();
        int approved = (int) reviewedList.stream().filter(a -> "approved".equals(ApplicationReviewDataService.normalizeStatusForMetrics(a))).count();
        int rejected = (int) reviewedList.stream().filter(a -> "rejected".equals(ApplicationReviewDataService.normalizeStatusForMetrics(a))).count();

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.add(smallCard("Total Reviews", String.valueOf(reviewedList.size()), new Color(0x111827)));
        stats.add(smallCard("Approved", String.valueOf(approved), new Color(0x15803D)));
        stats.add(smallCard("Rejected", String.valueOf(rejected), new Color(0xDC2626)));

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
                    case 4 -> ApplicationReviewDataService.normalizeStatusForMetrics(item);
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
        recordsTable.setFillsViewportHeight(false);
        recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        recordsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        recordsTable.getColumnModel().getColumn(6).setCellRenderer(new SimpleDetailRenderer());
        recordsTable.getColumnModel().getColumn(6).setCellEditor(new SimpleDetailEditor(row -> setCenterContent(buildApplicationDetailView(reviewedList.get(row)))));

        JScrollPane recordsScroll = new JScrollPane(recordsTable);
        recordsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        recordsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recordsScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(recordsTable, recordsScroll));
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(north, c);

        c.gridy = 1;
        c.insets = new Insets(14, 0, 0, 0);
        panel.add(stats, c);

        c.gridy = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(14, 0, 0, 0);
        panel.add(recordsScroll, c);

        SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(recordsTable, recordsScroll));
        return panel;
    }

    private JScrollPane wrapInScroll(JPanel page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        JLabel t = styledLabel(text, 16, Font.BOLD, 0x111827);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.setHorizontalAlignment(SwingConstants.LEFT);
        return t;
    }

    private static JLabel leftPaddedSummaryLine(String text) {
        JLabel line = new JLabel(text);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setHorizontalAlignment(SwingConstants.LEFT);
        line.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        line.setForeground(new Color(0x111827));
        return line;
    }

    private JPanel textAreaBlock(String title, String content) {
        JPanel block = cardPanel();
        block.setLayout(new BorderLayout(0, 8));

        block.add(sectionTitle(title), BorderLayout.NORTH);
        JTextArea area = new JTextArea(content);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane areaScroll = new JScrollPane(area);
        areaScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        block.add(areaScroll, BorderLayout.CENTER);
        return block;
    }

    private JPanel buildSkillsMatchingAnalysisCard(ApplicationItem item) {
        ScoreResult sr = calculateScoreResult(item);
        List<String> requiredSkills = getRequiredSkillsForItem(item);
        Set<String> applicantSkills = extractApplicantSkills(item);

        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = sectionTitle("Skills Matching Analysis");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel scoreLine = new JLabel("Overall Match Score: " + sr.score() + "/100");
        scoreLine.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        scoreLine.setForeground(new Color(0x1E3A8A));
        scoreLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(scoreLine);

        JLabel evalLine = new JLabel("Matching Evaluation: " + evaluationText(sr.score()));
        evalLine.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        evalLine.setForeground(new Color(0x334155));
        evalLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(evalLine);
        card.add(Box.createVerticalStrut(12));

        JPanel skillsCompareGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        skillsCompareGrid.setOpaque(false);
        skillsCompareGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel requiredCol = new JPanel();
        requiredCol.setOpaque(true);
        requiredCol.setBackground(new Color(0xF8FAFC));
        requiredCol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        requiredCol.setLayout(new BoxLayout(requiredCol, BoxLayout.Y_AXIS));

        JLabel requiredTitle = new JLabel("Required Skills for Position");
        requiredTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        requiredTitle.setForeground(new Color(0x111827));
        requiredTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        requiredCol.add(requiredTitle);
        requiredCol.add(Box.createVerticalStrut(8));

        if (requiredSkills.isEmpty()) {
            JLabel none = new JLabel("No required skills are configured for this job.");
            none.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            none.setForeground(new Color(0x6B7280));
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            requiredCol.add(none);
        } else {
            for (String reqRaw : requiredSkills) {
                String req = normalizeSkill(reqRaw);
                double best = 0.0;
                for (String cand : applicantSkills) {
                    best = Math.max(best, pairSkillScore(req, cand));
                }
                JLabel line = new JLabel((best >= 0.55 ? "✓ " : "✗ ") + reqRaw);
                line.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                line.setForeground(best >= 0.55 ? new Color(0x15803D) : new Color(0xB91C1C));
                line.setAlignmentX(Component.LEFT_ALIGNMENT);
                requiredCol.add(line);
                requiredCol.add(Box.createVerticalStrut(4));
            }
        }

        JPanel applicantCol = new JPanel();
        applicantCol.setOpaque(true);
        applicantCol.setBackground(new Color(0xF8FAFC));
        applicantCol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        applicantCol.setLayout(new BoxLayout(applicantCol, BoxLayout.Y_AXIS));

        JLabel applicantTitle = new JLabel("Applicant's Skills");
        applicantTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        applicantTitle.setForeground(new Color(0x111827));
        applicantTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        applicantCol.add(applicantTitle);
        applicantCol.add(Box.createVerticalStrut(8));

        List<String> applicantRawSkills = (item.getApplicationForm() == null || item.getApplicationForm().getRelevantSkills() == null)
                ? List.of()
                : item.getApplicationForm().getRelevantSkills();

        if (applicantRawSkills.isEmpty()) {
            JLabel none = new JLabel("No submitted relevant skills.");
            none.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            none.setForeground(new Color(0x6B7280));
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            applicantCol.add(none);
        } else {
            Set<String> requiredNormalized = new HashSet<>();
            for (String r : requiredSkills) {
                String n = normalizeSkill(r);
                if (!n.isBlank()) {
                    requiredNormalized.add(n);
                }
            }
            for (String skillRaw : applicantRawSkills) {
                String sNorm = normalizeSkill(skillRaw);
                boolean matched = false;
                for (String req : requiredNormalized) {
                    if (pairSkillScore(req, sNorm) >= 0.55) {
                        matched = true;
                        break;
                    }
                }
                JLabel line = new JLabel((matched ? "✓ " : "✗ ") + skillRaw);
                line.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                line.setForeground(matched ? new Color(0x15803D) : new Color(0xB91C1C));
                line.setAlignmentX(Component.LEFT_ALIGNMENT);
                applicantCol.add(line);
                applicantCol.add(Box.createVerticalStrut(4));
            }
        }

        skillsCompareGrid.add(requiredCol);
        skillsCompareGrid.add(applicantCol);
        card.add(skillsCompareGrid);
        card.add(Box.createVerticalStrut(12));

        JPanel hintGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        hintGrid.setOpaque(false);
        hintGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel missingAlert = buildHintBox(
                "Missing Skills Alert",
                sr.unmatchedReasons().isEmpty() ? "No missing required skills." : String.join("; ", sr.unmatchedReasons()),
                new Color(0xFEF2F2),
                new Color(0xFECACA),
                new Color(0xB91C1C)
        );

        List<String> advantages = new ArrayList<>();
        if (!applicantRawSkills.isEmpty()) {
            Set<String> requiredNormalized = new HashSet<>();
            for (String r : requiredSkills) {
                String n = normalizeSkill(r);
                if (!n.isBlank()) {
                    requiredNormalized.add(n);
                }
            }
            for (String s : applicantRawSkills) {
                String n = normalizeSkill(s);
                if (!n.isBlank() && !requiredNormalized.contains(n)) {
                    advantages.add(s);
                }
            }
        }

        JPanel advantageHint = buildHintBox(
                "Applicant Advantage Skills",
                advantages.isEmpty() ? "No extra advantage skills identified from the submitted data." : String.join(", ", advantages),
                new Color(0xEFF6FF),
                new Color(0xBFDBFE),
                new Color(0x1D4ED8)
        );

        hintGrid.add(missingAlert);
        hintGrid.add(advantageHint);
        card.add(hintGrid);

        return card;
    }

    private JPanel buildHintBox(String title, String content, Color bg, Color border, Color fg) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(true);
        box.setBackground(bg);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        t.setForeground(fg);

        JTextArea body = new JTextArea(content);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setOpaque(false);
        body.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        body.setForeground(new Color(0x1F2937));
        body.setBorder(null);

        box.add(t, BorderLayout.NORTH);
        box.add(body, BorderLayout.CENTER);
        return box;
    }

    private List<String> getRequiredSkillsForItem(ApplicationItem item) {
        if (item == null || item.getJobId() == null) {
            return List.of();
        }
        Job job = jobById.get(item.getJobId());
        if (job == null || job.getRequiredSkills() == null) {
            return List.of();
        }
        return job.getRequiredSkills();
    }

    private static String evaluationText(int score) {
        if (score >= 85) {
            return "Excellent fit";
        }
        if (score >= 70) {
            return "Good fit";
        }
        if (score >= 55) {
            return "Moderate fit";
        }
        return "Needs improvement";
    }

    /**
     * Detail card: label column fixed width, value column follows immediately (avoids wide gaps from {@link GridLayout} 50/50 split).
     */
    private JPanel buildApplicationDetailInfoCard(ApplicationItem item) {
        JPanel info = cardPanel();
        info.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.insets = new Insets(5, 0, 5, 0);

        int row = 0;
        addDetailInfoRow(info, gc, row++, "Applicant", safe(getApplicantName(item)));
        addDetailInfoRow(info, gc, row++, "Student ID", safe(item.getStudentId()));
        addDetailInfoRow(info, gc, row++, "Email", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getEmail()));
        addDetailInfoRow(info, gc, row++, "Phone", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getPhoneNumber()));
        addDetailInfoRow(info, gc, row++, "Major", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getProgramMajor()));
        addDetailInfoRow(info, gc, row++, "GPA", item.getApplicantSnapshot() == null || item.getApplicantSnapshot().getGpa() == null
                ? "" : String.valueOf(item.getApplicantSnapshot().getGpa()));
        addDetailInfoRow(info, gc, row++, "Course", getCourseText(item));
        addDetailInfoRow(info, gc, row++, "Current Status", ApplicationReviewDataService.normalizeStatusForMetrics(item));
        return info;
    }

    private static void addDetailInfoRow(JPanel panel, GridBagConstraints gc, int row, String key, String value) {
        gc.gridy = row;
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.weightx = 0;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(5, 0, 5, 16);

        JLabel k = new JLabel(key + ":");
        k.setForeground(new Color(0x6B7280));
        k.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        k.setHorizontalAlignment(SwingConstants.LEFT);
        int labelH = k.getPreferredSize().height;
        k.setPreferredSize(new Dimension(140, labelH));
        k.setMinimumSize(new Dimension(120, labelH));
        panel.add(k, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 0);
        JLabel v = new JLabel(value == null ? "" : value);
        v.setForeground(new Color(0x111827));
        v.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        v.setVerticalAlignment(SwingConstants.TOP);
        panel.add(v, gc);
    }

    private JLabel styledLabel(String text, int size, int style, int colorHex) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
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

    private boolean isReviewed(ApplicationItem item) {
        String status = ApplicationReviewDataService.normalizeStatusForMetrics(item);
        return "approved".equals(status) || "rejected".equals(status);
    }

    /**
     * Enforce job quota: approved count for the same job must stay below QUOTA.
     * Re-approving an already approved item is allowed.
     */
    private boolean canApproveForQuota(ApplicationItem target) {
        if (target == null || target.getJobId() == null || target.getJobId().isBlank()) {
            return true;
        }

        int quota = 0;
        for (Job j : jobRepository.loadJobsForMo(MoContext.getCurrentMoUserId())) {
            if (target.getJobId().equals(j.getId())) {
                quota = Math.max(0, j.getQuota());
                break;
            }
        }
        if (quota <= 0) {
            return true;
        }

        String targetStatus = ApplicationReviewDataService.normalizeStatusForMetrics(target);
        if ("approved".equals(targetStatus)) {
            return true;
        }

        int approvedForSameJob = 0;
        for (ApplicationItem item : allApplications) {
            if (item == null || item.getJobId() == null) {
                continue;
            }
            if (!target.getJobId().equals(item.getJobId())) {
                continue;
            }
            String status = ApplicationReviewDataService.normalizeStatusForMetrics(item);
            if ("approved".equals(status)) {
                approvedForSameJob++;
            }
        }
        return approvedForSameJob < quota;
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

    private int computeMatchScore(ApplicationItem item) {
        return calculateScoreResult(item).score();
    }

    private void openMatchScoreReason(int row) {
        if (row < 0 || row >= applicationTableData.size()) {
            return;
        }
        ApplicationItem item = applicationTableData.get(row);
        ScoreResult sr = calculateScoreResult(item);
        StringBuilder sb = new StringBuilder();
        sb.append("Match Score: ").append(sr.score()).append("/100\n\n");
        sb.append("Breakdown:\n");
        sb.append("- Skills weighted match: ").append(String.format(Locale.ROOT, "%.1f", sr.skillPart())).append(" / 70\n");
        sb.append("- Course relevance: ").append(String.format(Locale.ROOT, "%.1f", sr.coursePart())).append(" / 15\n");
        sb.append("- Job description relevance: ").append(String.format(Locale.ROOT, "%.1f", sr.descriptionPart())).append(" / 15\n\n");

        sb.append("Matched required skills (weight):\n");
        if (sr.skillReasons().isEmpty()) {
            sb.append("- None\n");
        } else {
            for (String r : sr.skillReasons()) {
                sb.append("- ").append(r).append("\n");
            }
        }

        sb.append("\nUnmatched required skills:\n");
        if (sr.unmatchedReasons().isEmpty()) {
            sb.append("- None\n");
        } else {
            for (String r : sr.unmatchedReasons()) {
                sb.append("- ").append(r).append("\n");
            }
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(560, 420));
        JOptionPane.showMessageDialog(this, sp, "Match Score Reason", JOptionPane.INFORMATION_MESSAGE);
    }

    private record ScoreResult(int score,
                               double skillPart,
                               double coursePart,
                               double descriptionPart,
                               List<String> skillReasons,
                               List<String> unmatchedReasons) {
    }

    private ScoreResult calculateScoreResult(ApplicationItem item) {
        if (item == null || item.getJobId() == null || item.getJobId().isBlank()) {
            return new ScoreResult(0, 0, 0, 0, List.of(), List.of("No job context"));
        }

        Job job = jobById.get(item.getJobId());
        if (job == null) {
            return new ScoreResult(0, 0, 0, 0, List.of(), List.of("Job not found for this application"));
        }

        List<String> requiredSkillsRaw = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
        Set<String> applicantSkills = extractApplicantSkills(item);
        String applicantText = extractApplicantEvidenceText(item);

        Map<String, Double> requiredWeight = new LinkedHashMap<>();
        for (String raw : requiredSkillsRaw) {
            String req = normalizeSkill(raw);
            if (req.isBlank() || requiredWeight.containsKey(req)) {
                continue;
            }
            requiredWeight.put(req, isCoreSkill(req, job) ? 1.6 : 1.0);
        }

        double totalWeight = requiredWeight.values().stream().mapToDouble(Double::doubleValue).sum();
        double matchedWeight = 0.0;
        List<String> matchedReasons = new ArrayList<>();
        List<String> unmatchedReasons = new ArrayList<>();

        for (Map.Entry<String, Double> e : requiredWeight.entrySet()) {
            String req = e.getKey();
            double w = e.getValue();
            double best = 0.0;
            String bestSource = "";

            for (String cand : applicantSkills) {
                double s = pairSkillScore(req, cand);
                if (s > best) {
                    best = s;
                    bestSource = "skill: " + cand;
                }
            }
            double txtScore = textEvidenceScore(req, applicantText);
            if (txtScore > best) {
                best = txtScore;
                bestSource = "experience text";
            }

            matchedWeight += (best * w);
            if (best >= 0.55) {
                matchedReasons.add(req + " (w=" + String.format(Locale.ROOT, "%.1f", w)
                        + ") via " + bestSource + " -> " + String.format(Locale.ROOT, "%.2f", best));
            } else {
                unmatchedReasons.add(req + " (w=" + String.format(Locale.ROOT, "%.1f", w) + ")");
            }
        }

        double skillCoverage = totalWeight <= 0 ? 0.0 : matchedWeight / totalWeight;
        double skillPart = clamp(skillCoverage, 0.0, 1.0) * 70.0;

        double courseRel = computeCourseRelevance(item, job, applicantText, applicantSkills);
        double coursePart = courseRel * 15.0;

        double descRel = computeDescriptionRelevance(job, applicantText, applicantSkills);
        double descPart = descRel * 15.0;

        int finalScore = (int) Math.round(clamp(skillPart + coursePart + descPart, 0.0, 100.0));
        return new ScoreResult(finalScore, skillPart, coursePart, descPart, matchedReasons, unmatchedReasons);
    }

    private static final Map<String, Set<String>> SKILL_SYNONYMS = buildSkillSynonyms();

    private static Map<String, Set<String>> buildSkillSynonyms() {
        Map<String, Set<String>> m = new HashMap<>();
        putSynonymGroup(m, "java", "jdk", "spring", "springboot");
        putSynonymGroup(m, "python", "py", "pandas", "numpy");
        putSynonymGroup(m, "sql", "mysql", "postgresql", "sqlite", "database", "db");
        putSynonymGroup(m, "javascript", "js", "typescript", "ts", "nodejs");
        putSynonymGroup(m, "machine learning", "ml", "ai", "artificial intelligence");
        putSynonymGroup(m, "data analysis", "data analytics", "analytics", "statistical analysis");
        putSynonymGroup(m, "communication", "presentation", "teamwork", "collaboration");
        putSynonymGroup(m, "teaching", "tutoring", "mentoring", "instruction");
        return m;
    }

    private static void putSynonymGroup(Map<String, Set<String>> map, String... terms) {
        Set<String> group = new HashSet<>();
        Arrays.stream(terms).map(MoApplicationReviewPanel::normalizeSkill).forEach(group::add);
        for (String t : group) {
            map.put(t, group);
        }
    }

    private static double pairSkillScore(String req, String cand) {
        if (req.isBlank() || cand.isBlank()) {
            return 0.0;
        }
        if (req.equals(cand)) {
            return 1.0;
        }
        Set<String> reqSyn = SKILL_SYNONYMS.get(req);
        if (reqSyn != null && reqSyn.contains(cand)) {
            return 0.85;
        }
        Set<String> candSyn = SKILL_SYNONYMS.get(cand);
        if (candSyn != null && candSyn.contains(req)) {
            return 0.85;
        }
        if (req.contains(cand) || cand.contains(req)) {
            return 0.70;
        }
        Set<String> reqTokens = tokenize(req);
        Set<String> candTokens = tokenize(cand);
        if (reqTokens.isEmpty() || candTokens.isEmpty()) {
            return 0.0;
        }
        int overlap = 0;
        for (String t : reqTokens) {
            if (candTokens.contains(t)) {
                overlap++;
            }
        }
        if (overlap <= 0) {
            return 0.0;
        }
        double tokenRatio = overlap / (double) Math.max(reqTokens.size(), candTokens.size());
        return Math.min(0.75, 0.45 + tokenRatio * 0.3);
    }

    private static double textEvidenceScore(String req, String text) {
        if (req.isBlank() || text == null || text.isBlank()) {
            return 0.0;
        }
        if (text.contains(req)) {
            return 0.65;
        }
        Set<String> syn = SKILL_SYNONYMS.get(req);
        if (syn != null) {
            for (String s : syn) {
                if (!s.isBlank() && text.contains(s)) {
                    return 0.6;
                }
            }
        }
        Set<String> reqTokens = tokenize(req);
        if (reqTokens.isEmpty()) {
            return 0.0;
        }
        int hit = 0;
        for (String t : reqTokens) {
            if (text.contains(t)) {
                hit++;
            }
        }
        if (hit == 0) {
            return 0.0;
        }
        return Math.min(0.55, 0.25 + (hit / (double) reqTokens.size()) * 0.3);
    }

    private static Set<String> tokenize(String s) {
        Set<String> out = new HashSet<>();
        if (s == null || s.isBlank()) {
            return out;
        }
        for (String t : normalizeSkill(s).split("[^a-z0-9+#]+")) {
            if (!t.isBlank()) {
                out.add(t);
            }
        }
        return out;
    }

    private static String normalizeSkill(String s) {
        if (s == null) {
            return "";
        }
        String n = s.trim().toLowerCase(Locale.ROOT);
        n = n.replace("-", " ").replace("_", " ");
        n = n.replaceAll("\\s+", " ");
        return n;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private Set<String> extractApplicantSkills(ApplicationItem item) {
        Set<String> applicant = new HashSet<>();
        if (item.getApplicationForm() != null && item.getApplicationForm().getRelevantSkills() != null) {
            for (String s : item.getApplicationForm().getRelevantSkills()) {
                String n = normalizeSkill(s);
                if (!n.isBlank()) {
                    applicant.add(n);
                }
            }
        }
        return applicant;
    }

    private String extractApplicantEvidenceText(ApplicationItem item) {
        if (item.getApplicationForm() == null) {
            return "";
        }
        return normalizeSkill(item.getApplicationForm().getRelevantExperience()) + " "
                + normalizeSkill(item.getApplicationForm().getMotivationCoverLetter());
    }

    private boolean isCoreSkill(String req, Job job) {
        String title = normalizeSkill(job.getTitle());
        String moduleName = normalizeSkill(job.getModuleName());
        if (!title.isBlank() && (title.contains(req) || req.contains(title))) {
            return true;
        }
        if (!moduleName.isBlank() && (moduleName.contains(req) || req.contains(moduleName))) {
            return true;
        }

        String reqFirstToken = firstToken(req);
        if (!reqFirstToken.isBlank() && (title.contains(reqFirstToken) || moduleName.contains(reqFirstToken))) {
            return true;
        }
        return req.contains("core") || req.contains("must") || req.contains("required");
    }

    private static String firstToken(String s) {
        Set<String> t = tokenize(s);
        return t.isEmpty() ? "" : t.iterator().next();
    }

    private double computeCourseRelevance(ApplicationItem item, Job job, String applicantText, Set<String> applicantSkills) {
        String courseCode = item.getJobSnapshot() == null ? "" : normalizeSkill(item.getJobSnapshot().getCourseCode());
        String courseName = item.getJobSnapshot() == null ? "" : normalizeSkill(item.getJobSnapshot().getCourseName());
        String target = courseCode + " " + courseName + " " + normalizeSkill(job.getModuleName());
        return relevanceToText(target, applicantText, applicantSkills);
    }

    private double computeDescriptionRelevance(Job job, String applicantText, Set<String> applicantSkills) {
        String desc = normalizeSkill(job.getDescription()) + " " + normalizeSkill(job.getAdditionalRequirements());
        return relevanceToText(desc, applicantText, applicantSkills);
    }

    private double relevanceToText(String target, String applicantText, Set<String> applicantSkills) {
        Set<String> targetTokens = tokenize(target);
        if (targetTokens.isEmpty()) {
            return 0.0;
        }

        int hits = 0;
        for (String tk : targetTokens) {
            if (tk.length() < 3) {
                continue;
            }
            boolean matched = applicantText.contains(tk);
            if (!matched) {
                for (String s : applicantSkills) {
                    if (s.contains(tk) || tk.contains(s)) {
                        matched = true;
                        break;
                    }
                }
            }
            if (matched) {
                hits++;
            }
        }
        return clamp(hits / (double) Math.max(1, targetTokens.size()), 0.0, 1.0);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @SuppressWarnings("unused")
    private void downloadCvForItem(ApplicationItem item) {
        if (item == null || item.getAttachments() == null || item.getAttachments().getCv() == null) {
            JOptionPane.showMessageDialog(this, "No CV attachment found for this application.", "Download CV", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ApplicationItem.CvFile cv = item.getAttachments().getCv();
        String sourceRelPath = safe(cv.getFilePath());
        if (sourceRelPath.isBlank()) {
            JOptionPane.showMessageDialog(this, "CV file path is missing in application data.", "Download CV", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fileName = safe(cv.getOriginalFileName());
        if (fileName.isBlank()) {
            fileName = safe(cv.getStoredFileName());
        }
        if (fileName.isBlank()) {
            fileName = "cv.pdf";
        }
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            fileName = fileName + ".pdf";
        }

        Path source = dataService.getDataRoot().resolve(sourceRelPath).normalize();
        if (!Files.exists(source)) {
            // fallback: in some records path already starts with data/
            source = Paths.get(sourceRelPath).normalize();
        }
        if (!Files.exists(source)) {
            JOptionPane.showMessageDialog(this, "CV file does not exist: " + sourceRelPath, "Download CV", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Download CV");
        chooser.setSelectedFile(new java.io.File(fileName));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            target = target.resolveSibling(target.getFileName().toString() + ".pdf");
        }

        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "CV downloaded to:\n" + target, "Download CV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to download CV: " + ex.getMessage(), "Download CV", JOptionPane.ERROR_MESSAGE);
        }
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
        private final String[] cols = {"TA Name", "Student ID", "Applied Course", "Match Score", "Status", "Actions"};

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
                case 3 -> computeMatchScore(item) + "/100";
                case 4 -> ApplicationReviewDataService.normalizeStatusForMetrics(item);
                default -> "Actions";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3 || columnIndex == 5;
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

    private static class MatchScoreRenderer extends JPanel implements TableCellRenderer {
        private final JButton reason = new JButton();

        MatchScoreRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            JLabel score = new JLabel("--/100");
            score.setName("scoreLabel");
            score.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            reason.setToolTipText("View Reason");
            reason.setFocusPainted(false);
            reason.setContentAreaFilled(false);
            reason.setBorderPainted(false);
            reason.setOpaque(false);
            reason.setIcon(createEyeIcon(new Color(0x4B5563)));
            reason.setPreferredSize(new Dimension(22, 22));
            add(score);
            add(reason);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            for (Component c : getComponents()) {
                if (c instanceof JLabel l && "scoreLabel".equals(l.getName())) {
                    l.setText(value == null ? "0/100" : value.toString());
                    break;
                }
            }
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private static class MatchScoreEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JLabel score = new JLabel("0/100");
        private final JButton reason = new JButton();
        private int row;

        MatchScoreEditor(Consumer<Integer> reasonAction) {
            score.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            reason.setToolTipText("View Reason");
            reason.setFocusPainted(false);
            reason.setContentAreaFilled(false);
            reason.setBorderPainted(false);
            reason.setOpaque(false);
            reason.setIcon(createEyeIcon(new Color(0x4B5563)));
            reason.setPreferredSize(new Dimension(22, 22));
            panel.add(score);
            panel.add(reason);
            reason.addActionListener(e -> {
                reasonAction.accept(row);
                fireEditingStopped();
            });
        }

        @Override
        public Object getCellEditorValue() {
            return score.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            score.setText(value == null ? "0/100" : value.toString());
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    private static Icon createEyeIcon(Color strokeColor) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                try {
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(strokeColor);
                    g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g2.drawOval(x + 1, y + 4, 14, 8);
                    g2.fillOval(x + 6, y + 6, 4, 4);
                } finally {
                    g2.dispose();
                }
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    private static class ActionRenderer extends JPanel implements TableCellRenderer {
        private final JButton detail = new JButton("⌕ Detail");
        private final JButton review = new JButton("✎ Review");
        private final JButton approve = new JButton("✓ Approve");
        private final JButton reject = new JButton("✕ Reject");

        ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
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
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
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
            boolean isPending = "pending".equalsIgnoreCase(status);

            // ?? pending ????????
            approve.setEnabled(isPending);
            reject.setEnabled(isPending);
            review.setEnabled(isPending);
            detail.setEnabled(true);

            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    private static class SimpleDetailRenderer extends JPanel implements TableCellRenderer {
        private final JButton detail = new JButton("⌕ Detail");

        SimpleDetailRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
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
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
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
