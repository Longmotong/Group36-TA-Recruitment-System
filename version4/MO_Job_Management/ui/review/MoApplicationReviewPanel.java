package MO_system.ui.review;

import MO_system.DataRoot;
import MO_system.MoContext;
import MO_system.model.job.Job;
import MO_system.model.review.ApplicationItem;
import MO_system.repository.JobRepository;
import MO_system.service.ApplicationReviewDataService;
import MO_system.service.JobDescriptionAiService;
import MO_system.skill.MoSkillCatalog;
import MO_system.util.RelevantSkillsJson;
import MO_system.ui.MoShellFrame;
import MO_system.ui.MoShellHost;
import MO_system.ui.MoUiTheme;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;
import com.formdev.flatlaf.FlatClientProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
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
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Application Review flows merged from standalone {@code MainApp}; embedded in {@link MoShellFrame}.
 */
public final class MoApplicationReviewPanel extends JPanel {

    private final MoShellHost host;
    private final ApplicationReviewDataService dataService;
    private final String fromJobId;
    private final JobRepository jobRepository = new JobRepository();
    private final ObjectMapper userObjectMapper = new ObjectMapper();

    private List<ApplicationItem> allApplications = new ArrayList<>();
    private List<ApplicationItem> applicationTableData = new ArrayList<>();

    private JPanel mainFrame;

    private JTextField searchField;
    private JComboBox<String> courseFilter;
    private JComboBox<String> statusFilter;
    private JComboBox<String> matchScoreSortOrder;
    private JLabel filterCountLabel;

    private ApplicationTableModel applicationTableModel;
    private final Map<String, Job> jobById = new HashMap<>();

    /** Main list table + wrapper; used to re-sync column width after resize/show. */
    private JTable applicationListTable;
    private JScrollPane applicationListTableScroll;

    /** Value labels for Total / Pending / Approved / Rejected summary row (index 0–3). */
    private final JLabel[] summaryValueLabels = new JLabel[5];

    /** Filter row: one height for text field, combos, and buttons (aligned with FlatLaf controls). */
    private static final int FILTER_CONTROL_H = 38;

    /** Light gray for search placeholder and course/status combo hints (same color everywhere). */
    private static final Color FILTER_PLACEHOLDER_FG = new Color(0xB8C0CC);

    private static final Color LIST_TABLE_HEADER_BG = new Color(0xF3EEFF);
    private static final Color LIST_TABLE_BORDER = new Color(0xE6DBFF);
    private static final Color FILTER_SHELL_BG = new Color(0xFDFCFF);
    private static final Color LIST_TABLE_ROW_ALT = new Color(0xFCFAFF);
    private static final Color LIST_SELECTION_BG = new Color(0xECE6FF);

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
        panel.setOpaque(true);
        panel.setBackground(new Color(0xF8F8FF));
        panel.setBorder(new EmptyBorder(16, MoUiTheme.PAGE_INSET_X, 18, MoUiTheme.PAGE_INSET_X));

        JButton backHomeBtn = MoUiTheme.createBackToHomeButton(() -> host.showDashboard());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backHomeBtn);

        JButton recordsBtn = MoUiTheme.portalGradientPrimary("My Review Records",
                new Font(Font.SANS_SERIF, Font.BOLD, 13));
        Dimension recPref = recordsBtn.getPreferredSize();
        recordsBtn.setPreferredSize(new Dimension(Math.max(200, recPref.width), Math.max(42, recPref.height)));
        recordsBtn.addActionListener(e -> setCenterContent(buildMyReviewRecordsView()));

        JPanel headerRow = new JPanel(new BorderLayout(16, 0));
        headerRow.setOpaque(false);
        headerRow.add(detailHeroCard(
                "TA Applications",
                "Review and manage Teaching Assistant applications"), BorderLayout.CENTER);
        headerRow.add(recordsBtn, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(backRow, BorderLayout.NORTH);
        north.add(headerRow, BorderLayout.CENTER);

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
        panel.add(buildApplicationTableSection(), c);

        SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(applicationListTable, applicationListTableScroll));
        return panel;
    }

    private JPanel buildFiltersPanel() {
        JPanel bar = new JPanel(new BorderLayout(0, 6));
        bar.setOpaque(false);

        filterCountLabel = new JLabel("Showing 0 of 0");
        filterCountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        filterCountLabel.setForeground(MoUiTheme.TEXT_SECONDARY);
        filterCountLabel.setHorizontalAlignment(SwingConstants.LEFT);

        searchField = new JTextField();
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search name or ID…");
        searchField.setPreferredSize(new Dimension(0, FILTER_CONTROL_H));
        searchField.setMinimumSize(new Dimension(180, FILTER_CONTROL_H));

        courseFilter = new JComboBox<>(computeCourseCodes());
        courseFilter.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        courseFilter.setPreferredSize(new Dimension(168, FILTER_CONTROL_H));
        courseFilter.setMinimumSize(new Dimension(148, FILTER_CONTROL_H));
        installFilterComboHint(courseFilter, "Course");
        courseFilter.setSelectedIndex(-1);

        statusFilter = new JComboBox<>(new String[]{"pending", "offer_pending", "approved", "rejected"});
        statusFilter.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statusFilter.setPreferredSize(new Dimension(150, FILTER_CONTROL_H));
        statusFilter.setMinimumSize(new Dimension(130, FILTER_CONTROL_H));
        installFilterComboHint(statusFilter, "Status");
        statusFilter.setSelectedIndex(-1);

        matchScoreSortOrder = new JComboBox<>(new String[]{"Descending", "Ascending"});
        matchScoreSortOrder.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        matchScoreSortOrder.setPreferredSize(new Dimension(132, FILTER_CONTROL_H));
        matchScoreSortOrder.setMinimumSize(new Dimension(120, FILTER_CONTROL_H));
        matchScoreSortOrder.setSelectedIndex(0);
        matchScoreSortOrder.addActionListener(e -> refreshApplicationTable(true));

        JButton filterBtn = MoUiTheme.portalGradientPrimary("Apply Filter",
                new Font(Font.SANS_SERIF, Font.BOLD, 13));
        filterBtn.setPreferredSize(new Dimension(Math.max(120, filterBtn.getPreferredSize().width), FILTER_CONTROL_H));
        filterBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        resetBtn.setFocusPainted(false);
        resetBtn.setPreferredSize(new Dimension(82, FILTER_CONTROL_H));
        MoUiTheme.styleOutlineButton(resetBtn, 8);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            courseFilter.setSelectedIndex(-1);
            statusFilter.setSelectedIndex(-1);
            matchScoreSortOrder.setSelectedIndex(0);
            refreshApplicationTable(true);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(courseFilter);
        right.add(statusFilter);
        right.add(matchScoreSortOrder);
        right.add(filterBtn);
        right.add(resetBtn);

        JPanel controlsRow = new JPanel(new BorderLayout(8, 0));
        controlsRow.setOpaque(false);
        controlsRow.add(searchField, BorderLayout.CENTER);
        controlsRow.add(right, BorderLayout.EAST);

        JPanel controlsShell = new JPanel(new BorderLayout());
        controlsShell.setOpaque(true);
        controlsShell.setBackground(FILTER_SHELL_BG);
        controlsShell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIST_TABLE_BORDER),
                new EmptyBorder(6, 8, 6, 8)
        ));
        controlsShell.add(controlsRow, BorderLayout.CENTER);

        bar.add(controlsShell, BorderLayout.NORTH);
        bar.add(filterCountLabel, BorderLayout.SOUTH);
        return bar;
    }

    /** Updates summary cards when data changes (e.g. after quick Approve/Reject). */
    private void refreshSummaryCounts() {
        int[] values = computeSummaryCounts();
        for (int i = 0; i < values.length; i++) {
            if (summaryValueLabels[i] != null) {
                summaryValueLabels[i].setText(String.valueOf(values[i]));
            }
        }
    }

    private JPanel buildReviewSummaryCards() {
        int[] counts = computeSummaryCounts();

        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 92));
        row.add(listSummaryMetricCard("Total", String.valueOf(counts[0]),
                new Color(0xF5F3FF), new Color(0xDDD6FE), new Color(0x5B21B6), 0));
        row.add(listSummaryMetricCard("Pending Review", String.valueOf(counts[1]),
                new Color(0xFFFBEB), new Color(0xFDE68A), new Color(0xB45309), 1));
        row.add(listSummaryMetricCard("Offer Pending", String.valueOf(counts[2]),
                new Color(0xFFF7ED), new Color(0xFED7AA), new Color(0xC2410C), 2));
        row.add(listSummaryMetricCard("Approved", String.valueOf(counts[3]),
                new Color(0xECFDF5), new Color(0xA7F3D0), new Color(0x15803D), 3));
        row.add(listSummaryMetricCard("Rejected", String.valueOf(counts[4]),
                new Color(0xFEF2F2), new Color(0xFECACA), new Color(0xDC2626), 4));
        return row;
    }

    private JPanel listSummaryMetricCard(String label, String value, Color bg, Color border, Color valueColor, int slot) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(12, 10, 12, 10)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JLabel top = new JLabel(label.toUpperCase(Locale.ROOT), SwingConstants.CENTER);
        top.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        top.setForeground(new Color(0x475569));

        JLabel num = new JLabel(value, SwingConstants.CENTER);
        num.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        num.setForeground(valueColor);
        if (slot >= 0 && slot < summaryValueLabels.length) {
            summaryValueLabels[slot] = num;
        }

        card.add(top, BorderLayout.NORTH);
        card.add(num, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(160, 84));
        card.setMinimumSize(new Dimension(130, 76));
        return card;
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
        if (summarySlot != null && summarySlot >= 0 && summarySlot < 5) {
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

    private int[] computeSummaryCounts() {
        int total = allApplications == null ? 0 : allApplications.size();
        int pendingReview = 0;
        int offerPending = 0;
        int approved = 0;
        int rejected = 0;
        if (allApplications != null) {
            for (ApplicationItem item : allApplications) {
                String s = ApplicationReviewDataService.normalizeStatusForMetrics(item);
                if ("pending".equals(s)) {
                    pendingReview++;
                } else if ("offer_pending".equals(s)) {
                    offerPending++;
                } else if ("approved".equals(s)) {
                    approved++;
                } else if ("rejected".equals(s)) {
                    rejected++;
                }
            }
        }
        return new int[]{total, pendingReview, offerPending, approved, rejected};
    }

    private JPanel buildApplicationTableSection() {
        JScrollPane scroll = buildApplicationTableScrollPane();
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.add(JobsPortalUi.wrapRoundedInner(
                scroll, 16, Color.WHITE, LIST_TABLE_BORDER, 1f, true, null), BorderLayout.CENTER);
        return section;
    }

    private JScrollPane buildApplicationTableScrollPane() {
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder()));

        applicationTableModel = new ApplicationTableModel();
        JTable table = new JTable(applicationTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(LIST_TABLE_BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(MoUiTheme.TEXT_PRIMARY);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.setSelectionBackground(LIST_SELECTION_BG);
        table.setSelectionForeground(MoUiTheme.TEXT_PRIMARY);
        table.getTableHeader().setBackground(LIST_TABLE_HEADER_BG);
        table.getTableHeader().setForeground(new Color(0x4F35D9));
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LIST_TABLE_BORDER));
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer centeredRenderer = listZebraRenderer(SwingConstants.CENTER);
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
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
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
        int padX = MoUiTheme.PAGE_INSET_X - 8;
        ViewportWidthMatchPanel page = new ViewportWidthMatchPanel(new GridBagLayout());
        page.setOpaque(true);
        page.setBackground(new Color(0xF8F8FF));
        page.setBorder(new EmptyBorder(MoUiTheme.PAGE_INSET_TOP, padX, MoUiTheme.PAGE_INSET_BOTTOM, padX));

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);

        JButton backBtn = ghostButton("← Back to Applications");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));
        JButton toReviewBtn = primaryButton("Go to Review Page");
        toReviewBtn.addActionListener(e -> setCenterContent(buildReviewApplicationView(item)));
        head.add(backBtn, BorderLayout.WEST);
        head.add(toReviewBtn, BorderLayout.EAST);

        JPanel heroRow = buildHeroRowWithMatchScore(item, "TA Application Detail",
                "Full application profile and skills analysis");

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
        page.add(heroRow, c);

        JPanel info = buildApplicationDetailInfoCard(item);
        c.gridy = 2;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(info, c);

        c.gridy = 3;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(textAreaBlock("Relevant Skills", getSkillsText(item)), c);

        c.gridy = 4;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(buildSkillsMatchingAnalysisCard(item), c);

        c.gridy = 5;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(buildAiReviewAssistCard(item), c);

        c.gridy = 6;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(textAreaBlock("Relevant Experience", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getRelevantExperience())), c);

        c.gridy = 7;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(textAreaBlock("Motivation Cover Letter", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getMotivationCoverLetter())), c);

        c.gridy = 8;
        c.insets = new Insets(12, 0, 0, 0);
        page.add(buildCvButtonBar(item), c);

        return wrapInScroll(page);
    }

    private JScrollPane buildReviewApplicationView(ApplicationItem item) {
        int padX = MoUiTheme.PAGE_INSET_X - 8;
        ViewportWidthMatchPanel page = new ViewportWidthMatchPanel(new GridBagLayout());
        page.setOpaque(true);
        page.setBackground(new Color(0xF8F8FF));
        page.setBorder(new EmptyBorder(MoUiTheme.PAGE_INSET_TOP, padX, MoUiTheme.PAGE_INSET_BOTTOM, padX));

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        JButton backBtn = ghostButton("← Back to Applications");
        backBtn.addActionListener(e -> setCenterContent(buildApplicationReviewView()));
        JButton detailBtn = ghostButton("⌕  View Detail");
        detailBtn.addActionListener(e -> setCenterContent(buildApplicationDetailView(item)));
        head.add(backBtn, BorderLayout.WEST);
        head.add(detailBtn, BorderLayout.EAST);

        JPanel heroRow = buildHeroRowWithMatchScore(item, "Review TA Application",
                "Evaluate the applicant and make a decision on their application");

        JPanel summary = buildReviewApplicationSummaryCard(item);

        JPanel decisionCard = detailAccentCard();
        decisionCard.setLayout(new BoxLayout(decisionCard, BoxLayout.Y_AXIS));
        decisionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel decisionHead = detailSectionHead("Decision");
        decisionHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        decisionCard.add(decisionHead);
        decisionCard.add(Box.createVerticalStrut(12));

        String normalizedStatus = ApplicationReviewDataService.normalizeStatusForMetrics(item);
        boolean defaultReject = "rejected".equalsIgnoreCase(normalizedStatus);
        JRadioButton approve = new JRadioButton("Approve", !defaultReject);
        approve.setHorizontalAlignment(SwingConstants.LEFT);
        approve.setMargin(new Insets(2, 4, 2, 4));
        approve.setOpaque(false);
        approve.setFocusPainted(false);
        approve.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        approve.setForeground(new Color(0x166534));
        JRadioButton reject = new JRadioButton("Reject", defaultReject);
        reject.setHorizontalAlignment(SwingConstants.LEFT);
        reject.setMargin(new Insets(2, 4, 2, 4));
        reject.setOpaque(false);
        reject.setFocusPainted(false);
        reject.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        reject.setForeground(new Color(0xB91C1C));
        ButtonGroup group = new ButtonGroup();
        group.add(approve);
        group.add(reject);

        JPanel approveWrap = wrapReviewDecisionChoice(approve, true);
        JPanel rejectWrap = wrapReviewDecisionChoice(reject, false);
        ItemListener choiceBorderSync = e -> {
            syncReviewDecisionChoiceBorder(approveWrap, approve);
            syncReviewDecisionChoiceBorder(rejectWrap, reject);
        };
        approve.addItemListener(choiceBorderSync);
        reject.addItemListener(choiceBorderSync);
        syncReviewDecisionChoiceBorder(approveWrap, approve);
        syncReviewDecisionChoiceBorder(rejectWrap, reject);

        JPanel choicesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        choicesRow.setOpaque(false);
        choicesRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        choicesRow.add(approveWrap);
        choicesRow.add(rejectWrap);
        decisionCard.add(choicesRow);
        decisionCard.add(Box.createVerticalStrut(14));

        JLabel notesLabel = new JLabel("Review Notes");
        notesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        notesLabel.setForeground(new Color(0x111827));
        notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        decisionCard.add(notesLabel);
        decisionCard.add(Box.createVerticalStrut(6));

        JTextArea notesArea = new JTextArea(10, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        if (item.getReview() != null && !safe(item.getReview().getReviewerNotes()).isBlank()) {
            notesArea.setText(safe(item.getReview().getReviewerNotes()));
            notesArea.setCaretPosition(0);
        }
        notesArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        notesArea.setForeground(new Color(0x334155));
        notesArea.setBackground(new Color(0xFDFCFF));
        notesArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        notesScroll.setPreferredSize(new Dimension(0, 180));
        notesScroll.setMinimumSize(new Dimension(0, 160));
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesScroll.setBorder(BorderFactory.createLineBorder(new Color(0xDED4FF)));
        notesScroll.getViewport().setBackground(new Color(0xFDFCFF));
        decisionCard.add(notesScroll);
        decisionCard.add(Box.createVerticalStrut(12));

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
        page.add(heroRow, c);

        c.gridy = 2;
        c.insets = new Insets(8, 0, 0, 0);
        page.add(summary, c);

        c.gridy = 3;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(buildCourseAndApplicantCards(item), c);

        c.gridy = 4;
        c.insets = new Insets(10, 0, 0, 0);
        page.add(decisionCard, c);

        return wrapInScroll(page);
    }

    private JPanel buildCourseAndApplicantCards(ApplicationItem item) {
        Job job = item == null ? null : jobById.get(item.getJobId());

        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 0));
        grid.setOpaque(false);

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

        JPanel courseReqCard = buildReviewQualificationCard("Course Requirements",
                new String[][]{
                        {"Required Skills", reqSkillsText},
                        {"Weekly Workload", weeklyHours},
                        {"TA Headcount Needed", taQuota},
                        {"Ideal Qualifications", idealQualText},
                        {"Role Responsibilities", duties}
                });

        String skillLevel = getSkillsText(item);
        if (skillLevel.isBlank()) {
            skillLevel = "Not provided";
        }
        String academic = "Major: " + (item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getProgramMajor()))
                + "; Year: " + (item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getYear()))
                + "; GPA: " + (item.getApplicantSnapshot() == null || item.getApplicantSnapshot().getGpa() == null
                ? "Not provided" : String.valueOf(item.getApplicantSnapshot().getGpa()));
        String experience = (item.getApplicationForm() == null || safe(item.getApplicationForm().getRelevantExperience()).isBlank())
                ? "Not provided"
                : safe(item.getApplicationForm().getRelevantExperience());

        JPanel applicantQualCard = buildReviewQualificationCard("Applicant Qualifications",
                new String[][]{
                        {"Skills Profile", skillLevel},
                        {"Academic Background", academic},
                        {"Experience Summary", experience}
                });

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
        recordsTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        JLabel recordsSubtitle = new JLabel("Track completed decisions and follow-up status");
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
                new EmptyBorder(12, 16, 12, 16)
        ));
        headerCard.putClientProperty("JComponent.style", "arc: 12");
        headerCard.add(titleLeft, BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout(0, 6));
        north.setOpaque(false);
        north.add(backRow, BorderLayout.NORTH);
        north.add(headerCard, BorderLayout.CENTER);

        List<ApplicationItem> reviewedList = allApplications.stream().filter(this::isReviewed).toList();
        int offerPending = (int) reviewedList.stream().filter(a -> "offer_pending".equals(ApplicationReviewDataService.normalizeStatusForMetrics(a))).count();
        int approved = (int) reviewedList.stream().filter(a -> "approved".equals(ApplicationReviewDataService.normalizeStatusForMetrics(a))).count();
        int rejected = (int) reviewedList.stream().filter(a -> "rejected".equals(ApplicationReviewDataService.normalizeStatusForMetrics(a))).count();

        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 0));
        stats.setOpaque(false);
        stats.add(smallCard("Total Reviews", String.valueOf(reviewedList.size()), new Color(0x111827)));
        stats.add(smallCard("Offer Pending", String.valueOf(offerPending), new Color(0xB45309)));
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

        recordsTable.setRowHeight(42);
        recordsTable.setFillsViewportHeight(false);
        recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        recordsTable.getTableHeader().setReorderingAllowed(false);
        recordsTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        recordsTable.getTableHeader().setBackground(new Color(0xF8FAFC));
        recordsTable.getTableHeader().setForeground(new Color(0x334155));
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
        JPanel tableCard = cardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(14, 14, 14, 14)
        ));
        JLabel tableTitle = new JLabel("Reviewed Applications");
        tableTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        tableTitle.setForeground(new Color(0x0F172A));
        tableCard.add(tableTitle, BorderLayout.NORTH);
        if (reviewedList.isEmpty()) {
            JLabel empty = new JLabel("No reviewed applications yet.", SwingConstants.CENTER);
            empty.setForeground(MoUiTheme.TEXT_SECONDARY);
            empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            JPanel emptyWrap = new JPanel(new BorderLayout());
            emptyWrap.setOpaque(false);
            emptyWrap.setBorder(new EmptyBorder(26, 0, 26, 0));
            emptyWrap.add(empty, BorderLayout.CENTER);
            tableCard.add(emptyWrap, BorderLayout.CENTER);
        } else {
            tableCard.add(recordsScroll, BorderLayout.CENTER);
        }

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
        panel.add(stats, c);

        c.gridy = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(8, 0, 0, 0);
        panel.add(tableCard, c);

        SwingUtilities.invokeLater(() -> syncApplicationTableViewportWidth(recordsTable, recordsScroll));
        return panel;
    }

    private JScrollPane wrapInScroll(JPanel page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    private JButton primaryButton(String text) {
        JButton b = MoUiTheme.portalGradientPrimary(text, new Font(Font.SANS_SERIF, Font.BOLD, 14));
        Dimension pref = b.getPreferredSize();
        int w = Math.max(160, Math.max(pref.width, text.length() * 9 + 30));
        int h = Math.max(44, pref.height);
        b.setPreferredSize(new Dimension(w, h));
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = MoUiTheme.portalOutlineSecondary(text, new Font(Font.SANS_SERIF, Font.BOLD, 14));
        Dimension pref = b.getPreferredSize();
        int w = Math.max(160, Math.max(pref.width, text.length() * 9 + 30));
        int h = Math.max(44, pref.height);
        b.setPreferredSize(new Dimension(w, h));
        return b;
    }

    private JPanel textAreaBlock(String title, String content) {
        JPanel block = detailAccentCard();
        block.setLayout(new BorderLayout(0, 10));

        block.add(detailSectionHead(title), BorderLayout.NORTH);

        String body = content == null || content.isBlank() ? "Not provided" : content;
        JTextArea area = new JTextArea(body);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(true);
        area.setBackground(new Color(0xFDFCFF));
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        area.setForeground(new Color(0x253048));
        int lines = estimateTextRows(body);
        area.setRows(Math.max(4, Math.min(lines, 14)));
        area.setColumns(60);

        JPanel bodyWrap = new JPanel(new BorderLayout());
        bodyWrap.setOpaque(false);
        bodyWrap.setBorder(BorderFactory.createLineBorder(new Color(0xDED4FF)));
        bodyWrap.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        bodyWrap.add(area, BorderLayout.CENTER);
        block.add(bodyWrap, BorderLayout.CENTER);
        return block;
    }

    private static int estimateTextRows(String text) {
        if (text == null || text.isBlank()) {
            return 4;
        }
        int byChars = Math.max(1, text.length() / 85);
        int byLines = text.split("\\R", -1).length;
        return Math.max(byChars, byLines + 1);
    }

    private JPanel buildCvButtonBar(ApplicationItem item) {
        JPanel bar = detailAccentCard();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton viewCvBtn = primaryButton("View TA CV");
        viewCvBtn.addActionListener(e -> openTaCvPdf(item));
        bar.add(viewCvBtn);
        return bar;
    }

    private JPanel buildAiReviewAssistCard(ApplicationItem item) {
        JPanel card = detailAccentCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel titleHead = detailSectionHead("AI Review Assist");
        titleHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleHead);
        card.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Generate a concise review analysis based on this application.");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(new Color(0x6B7280));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(10));

        JLabel guideLabel = new JLabel("Optional guidance for AI");
        guideLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        guideLabel.setForeground(new Color(0x111827));
        guideLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        guideLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(guideLabel);
        card.add(Box.createVerticalStrut(6));

        JTextField guideField = new JTextField();
        guideField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        guideField.setPreferredSize(new Dimension(0, 38));
        guideField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        guideField.setAlignmentX(Component.LEFT_ALIGNMENT);
        guideField.putClientProperty("JTextField.placeholderText", "e.g. Please focus on missing required skills first");
        card.add(guideField);
        card.add(Box.createVerticalStrut(10));

        JLabel outputLabel = new JLabel("AI analysis output");
        outputLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        outputLabel.setForeground(new Color(0x111827));
        outputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        outputLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(outputLabel);
        card.add(Box.createVerticalStrut(6));

        JTextArea outputArea = new JTextArea();
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        outputArea.setText("No AI analysis yet. Click 'Generate AI Analysis' to produce a review suggestion.");

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outputScroll.setPreferredSize(new Dimension(0, 180));
        outputScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        outputScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(outputScroll);
        card.add(Box.createVerticalStrut(10));

        final String[] lastOutputBeforeReplace = new String[1];

        JButton undoBtn = ghostButton("Undo AI Replace");
        JButton generateBtn = primaryButton("Generate AI Analysis");
        undoBtn.setEnabled(false);

        undoBtn.addActionListener(e -> {
            if (lastOutputBeforeReplace[0] != null) {
                outputArea.setText(lastOutputBeforeReplace[0]);
                outputArea.setCaretPosition(0);
                undoBtn.setEnabled(false);
            }
        });

        generateBtn.addActionListener(e -> {
            String guidance = safe(guideField.getText());
            String previous = outputArea.getText();
            lastOutputBeforeReplace[0] = previous;
            undoBtn.setEnabled(true);

            generateBtn.setEnabled(false);
            generateBtn.setText("Generating...");

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return generateAiReviewAnalysis(item, guidance);
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        outputArea.setText(cleanAiMarkdown(result));
                        outputArea.setCaretPosition(0);
                    } catch (Exception ex) {
                        outputArea.setText(previous);
                        lastOutputBeforeReplace[0] = null;
                        undoBtn.setEnabled(false);
                        JOptionPane.showMessageDialog(MoApplicationReviewPanel.this,
                                "Failed to generate AI analysis: " + ex.getMessage(),
                                "AI Review Assist",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        generateBtn.setEnabled(true);
                        generateBtn.setText("Generate AI Analysis");
                    }
                }
            }.execute();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(undoBtn);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(generateBtn);
        card.add(btnRow);

        return card;
    }

    private String cleanAiMarkdown(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String cleaned = raw;
        cleaned = cleaned.replace("**", "");
        cleaned = cleaned.replace("__", "");
        cleaned = cleaned.replace("`", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*#+\\s*", "");
        return cleaned.trim();
    }

    private String generateAiReviewAnalysis(ApplicationItem item, String guidance) throws IOException, InterruptedException {
        ScoreResult sr = calculateScoreResult(item);
        List<String> requiredSkills = getRequiredSkillsForItem(item);
        String applicantSkills = getSkillsText(item);
        String exp = item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getRelevantExperience());
        String letter = item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getMotivationCoverLetter());

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        Set<String> applicantSkillSet = extractApplicantSkills(item);
        for (String req : requiredSkills) {
            double best = 0.0;
            for (String s : applicantSkillSet) {
                best = Math.max(best, pairSkillScore(req, s));
            }
            if (best >= 0.55) {
                matched.add(req);
            } else {
                missing.add(req);
            }
        }

        String missingLine = missing.isEmpty() ? "None" : String.join(", ", missing);
        String matchedLine = matched.isEmpty() ? "None" : String.join(", ", matched);

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an MO application review assistant. Generate a concise evaluation for TA application review.\n");
        prompt.append("IMPORTANT: If there are missing required skills, you MUST explicitly list them under a section named 'Missing Required Skills'.\n");
        prompt.append("Use exactly these sections in order: Summary, Missing Required Skills, Strengths, Risks, Recommendation.\n");
        prompt.append("Do not invent skills not present in the provided data.\n\n");

        prompt.append("Application context:\n");
        prompt.append("- Applicant: ").append(safe(getApplicantName(item))).append("\n");
        prompt.append("- Course: ").append(getCourseText(item)).append("\n");
        prompt.append("- Match score: ").append(sr.score()).append("/100 (").append(evaluationText(sr.score())).append(")\n");
        prompt.append("- Required skills: ").append(requiredSkills.isEmpty() ? "None" : String.join(", ", requiredSkills)).append("\n");
        prompt.append("- Applicant submitted skills: ").append(applicantSkills.isBlank() ? "None" : applicantSkills).append("\n");
        prompt.append("- Matched required skills: ").append(matchedLine).append("\n");
        prompt.append("- Missing required skills: ").append(missingLine).append("\n");
        prompt.append("- Relevant experience: ").append(exp.isBlank() ? "None" : exp).append("\n");
        prompt.append("- Motivation cover letter: ").append(letter.isBlank() ? "None" : letter).append("\n");
        if (!guidance.isBlank()) {
            prompt.append("- MO guidance (highest priority): ").append(guidance.trim()).append("\n");
        }

        JobDescriptionAiService service = new JobDescriptionAiService();
        Job job = item == null ? null : jobById.get(item.getJobId());
        JobDescriptionAiService.JobDescriptionInput input = new JobDescriptionAiService.JobDescriptionInput(
                job == null ? "TA Application Review" : safe(job.getTitle()),
                item.getJobSnapshot() == null ? "" : safe(item.getJobSnapshot().getCourseCode()),
                item.getJobSnapshot() == null ? "" : safe(item.getJobSnapshot().getCourseName()),
                job == null ? "" : safe(job.getDepartment()),
                "MO Reviewer",
                job == null ? 0 : job.getWeeklyHours(),
                job == null ? 0 : job.getQuota(),
                "N/A",
                "Review",
                requiredSkills,
                "For this task, ignore job-description writing behavior and output application-review analysis only.",
                prompt.toString()
        );
        return service.generateDescription(input);
    }

    private JPanel buildSkillsMatchingAnalysisCard(ApplicationItem item) {
        ScoreResult sr = calculateScoreResult(item);
        List<String> requiredSkills = getRequiredSkillsForItem(item);
        Set<String> applicantSkills = extractApplicantSkills(item);

        JPanel card = detailAccentCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel titleHead = detailSectionHead("Skills Matching Analysis");
        titleHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleHead);
        card.add(Box.createVerticalStrut(10));

        JPanel scoreBanner = new JPanel(new BorderLayout());
        scoreBanner.setOpaque(true);
        scoreBanner.setBackground(new Color(0x6D4DEB));
        scoreBanner.setBorder(new EmptyBorder(12, 14, 12, 14));
        scoreBanner.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        scoreBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel scoreLine = new JLabel("Match Score: " + sr.score() + "/100");
        scoreLine.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        scoreLine.setForeground(Color.WHITE);
        JLabel evalLine = new JLabel(evaluationText(sr.score()));
        evalLine.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        evalLine.setForeground(new Color(0xEDE9FE));
        JPanel scoreText = new JPanel();
        scoreText.setOpaque(false);
        scoreText.setLayout(new BoxLayout(scoreText, BoxLayout.Y_AXIS));
        scoreText.add(scoreLine);
        scoreText.add(Box.createVerticalStrut(2));
        scoreText.add(evalLine);
        scoreBanner.add(scoreText, BorderLayout.WEST);
        card.add(scoreBanner);
        card.add(Box.createVerticalStrut(12));

        JPanel skillsCompareGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        skillsCompareGrid.setOpaque(false);
        skillsCompareGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel requiredCol = new JPanel();
        requiredCol.setOpaque(true);
        requiredCol.setBackground(new Color(0xFAF8FF));
        requiredCol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDED4FF)),
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
        applicantCol.setBackground(new Color(0xFAF8FF));
        applicantCol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDED4FF)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        applicantCol.setLayout(new BoxLayout(applicantCol, BoxLayout.Y_AXIS));

        JLabel applicantTitle = new JLabel("Applicant's Skills");
        applicantTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        applicantTitle.setForeground(new Color(0x111827));
        applicantTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        applicantCol.add(applicantTitle);
        applicantCol.add(Box.createVerticalStrut(8));

        List<ApplicationItem.RelevantSkill> submittedSkills = (item.getApplicationForm() == null
                || item.getApplicationForm().getRelevantSkills() == null)
                ? List.of()
                : item.getApplicationForm().getRelevantSkills();

        if (submittedSkills.isEmpty()) {
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
            for (ApplicationItem.RelevantSkill skillEntry : submittedSkills) {
                String skillRaw = RelevantSkillsJson.formatDisplay(skillEntry);
                String sNorm = normalizeSkill(RelevantSkillsJson.nameForMatching(skillEntry));
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
        if (!submittedSkills.isEmpty()) {
            Set<String> requiredNormalized = new HashSet<>();
            for (String r : requiredSkills) {
                String n = normalizeSkill(r);
                if (!n.isBlank()) {
                    requiredNormalized.add(n);
                }
            }
            for (ApplicationItem.RelevantSkill s : submittedSkills) {
                String display = RelevantSkillsJson.formatDisplay(s);
                String n = normalizeSkill(RelevantSkillsJson.nameForMatching(s));
                if (!n.isBlank() && !requiredNormalized.contains(n)) {
                    advantages.add(display);
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
        return job.getRequiredSkills().stream()
                .map(MoApplicationReviewPanel::canonicalSkillForMatch)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
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
        JPanel info = detailAccentCard();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JPanel head = detailSectionHead("Applicant & Application");
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(head);
        info.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.insets = new Insets(5, 0, 5, 0);

        int row = 0;
        addDetailInfoRow(grid, gc, row++, "Application ID", safe(item.getApplicationId()));
        addDetailInfoRow(grid, gc, row++, "Applicant", safe(getApplicantName(item)));
        addDetailInfoRow(grid, gc, row++, "Student ID", safe(item.getStudentId()));
        addDetailInfoRow(grid, gc, row++, "Email", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getEmail()));
        addDetailInfoRow(grid, gc, row++, "Phone", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getPhoneNumber()));
        addDetailInfoRow(grid, gc, row++, "Major", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getProgramMajor()));
        addDetailInfoRow(grid, gc, row++, "GPA", item.getApplicantSnapshot() == null || item.getApplicantSnapshot().getGpa() == null
                ? "Not provided" : String.valueOf(item.getApplicantSnapshot().getGpa()));
        addDetailInfoRow(grid, gc, row++, "Course", getCourseText(item));
        addDetailInfoRow(grid, gc, row++, "Match Score", computeMatchScore(item) + "/100");
        addDetailStatusRow(grid, gc, row, "Current Status",
                ApplicationReviewDataService.normalizeStatusForMetrics(item));
        info.add(grid);
        return info;
    }

    private void addDetailStatusRow(JPanel panel, GridBagConstraints gc, int row, String key, String status) {
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
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusWrap.setOpaque(false);
        statusWrap.add(statusPillLabel(status));
        panel.add(statusWrap, gc);
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

    private JPanel buildHeroRowWithMatchScore(ApplicationItem item, String title, String subtitleFallback) {
        String applicant = safe(getApplicantName(item));
        String course = getCourseText(item);
        String heroSubtitle = (applicant.isBlank() && course.isBlank())
                ? subtitleFallback
                : applicant + (course.isBlank() ? "" : " · " + course);

        JPanel heroRow = new JPanel(new BorderLayout(12, 0));
        heroRow.setOpaque(false);
        heroRow.add(detailHeroCard(title, heroSubtitle), BorderLayout.CENTER);

        JLabel scoreBadge = new JLabel(computeMatchScore(item) + "/100");
        scoreBadge.setOpaque(true);
        scoreBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        scoreBadge.setForeground(Color.WHITE);
        scoreBadge.setBackground(new Color(0x6D4DEB));
        scoreBadge.setBorder(new EmptyBorder(10, 16, 10, 16));
        scoreBadge.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        JPanel scoreWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        scoreWrap.setOpaque(false);
        scoreWrap.add(scoreBadge);
        heroRow.add(scoreWrap, BorderLayout.EAST);
        return heroRow;
    }

    private JPanel buildReviewApplicationSummaryCard(ApplicationItem item) {
        JPanel summary = detailAccentCard();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel head = detailSectionHead("Application Summary");
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.add(head);
        summary.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        int row = 0;
        addDetailInfoRow(grid, gc, row++, "Application ID", safe(item.getApplicationId()));
        addDetailInfoRow(grid, gc, row++, "Applicant", safe(getApplicantName(item)));
        addDetailInfoRow(grid, gc, row++, "Course", getCourseText(item));
        addDetailInfoRow(grid, gc, row++, "Match Score", computeMatchScore(item) + "/100");
        addDetailStatusRow(grid, gc, row, "Current Status",
                ApplicationReviewDataService.normalizeStatusForMetrics(item));
        summary.add(grid);
        return summary;
    }

    private JPanel buildReviewQualificationCard(String title, String[][] rows) {
        JPanel card = detailAccentCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel head = detailSectionHead(title);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(head);
        card.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        for (int i = 0; i < rows.length; i++) {
            addDetailInfoRow(grid, gc, i, rows[i][0], rows[i][1]);
        }
        card.add(grid);
        return card;
    }

    private JPanel wrapReviewDecisionChoice(JRadioButton radio, boolean approve) {
        Color bg = approve ? new Color(0xECFDF5) : new Color(0xFEF2F2);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(true);
        wrap.setBackground(bg);
        radio.setBorder(new EmptyBorder(0, 0, 0, 0));
        wrap.add(radio, BorderLayout.CENTER);
        wrap.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        return wrap;
    }

    private static void syncReviewDecisionChoiceBorder(JPanel wrap, JRadioButton radio) {
        boolean approve = "Approve".equals(radio.getText());
        Color border = approve ? new Color(0xA7F3D0) : new Color(0xFECACA);
        Color borderColor = radio.isSelected() ? new Color(0x6D4DEB) : border;
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                new EmptyBorder(10, 16, 10, 16)
        ));
    }

    private JPanel detailHeroCard(String title, String subtitle) {
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setOpaque(true);
        hero.setBackground(new Color(0xFCFBFF));
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDED4FF)),
                new EmptyBorder(14, 16, 12, 16)
        ));
        hero.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JLabel titleLabel = styledLabel(title, 28, Font.BOLD, 0x111827);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subLabel = styledLabel(subtitle, 13, Font.PLAIN, 0x6B7280);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(titleLabel);
        hero.add(Box.createVerticalStrut(4));
        hero.add(subLabel);
        return hero;
    }

    private JPanel detailSectionHead(String title) {
        JPanel head = new JPanel(new BorderLayout(10, 0));
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel accent = new JPanel();
        accent.setOpaque(true);
        accent.setBackground(new Color(0x6D4DEB));
        accent.setPreferredSize(new Dimension(4, 22));
        accent.setMinimumSize(new Dimension(4, 22));

        JLabel label = new JLabel(title);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        label.setForeground(new Color(0x111827));
        head.add(accent, BorderLayout.WEST);
        head.add(label, BorderLayout.CENTER);
        return head;
    }

    private JPanel detailAccentCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDED4FF)),
                new EmptyBorder(18, 20, 18, 20)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return panel;
    }

    private JLabel statusPillLabel(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        JLabel pill = new JLabel(status == null || status.isBlank() ? "—" : status);
        pill.setOpaque(true);
        pill.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        pill.setBorder(new EmptyBorder(4, 10, 4, 10));
        if ("rejected".equalsIgnoreCase(normalized)) {
            pill.setBackground(new Color(0xFEE2E2));
            pill.setForeground(new Color(0xB91C1C));
        } else if ("offer_pending".equalsIgnoreCase(normalized)) {
            pill.setBackground(new Color(0xFFFBEB));
            pill.setForeground(new Color(0xB45309));
        } else if ("approved".equalsIgnoreCase(normalized)) {
            pill.setBackground(new Color(0xDCFCE7));
            pill.setForeground(new Color(0x166534));
        } else {
            pill.setBackground(new Color(0xE0E7FF));
            pill.setForeground(new Color(0x3730A3));
        }
        pill.putClientProperty(FlatClientProperties.STYLE, "arc: 999");
        return pill;
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
        return "approved".equals(status) || "offer_pending".equals(status) || "rejected".equals(status);
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
        if ("approved".equals(targetStatus) || "offer_pending".equals(targetStatus)) {
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
            if ("approved".equals(status) || "offer_pending".equals(status)) {
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
        return RelevantSkillsJson.formatList(item.getApplicationForm().getRelevantSkills());
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
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(0xFAFAFF));
        root.setBorder(new EmptyBorder(14, 14, 12, 14));

        JPanel hero = new JPanel(new BorderLayout());
        hero.setOpaque(true);
        hero.setBackground(new Color(0x6D4DEB));
        hero.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel score = new JLabel("Match Score: " + sr.score() + "/100");
        score.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        score.setForeground(Color.WHITE);
        hero.add(score, BorderLayout.WEST);

        JPanel breakdownRow = new JPanel(new GridLayout(1, 3, 8, 0));
        breakdownRow.setOpaque(false);
        breakdownRow.add(scoreBadge(
                "Skills",
                String.format(Locale.ROOT, "%.1f / 70", sr.skillPart()),
                new Color(0xE0E7FF),
                new Color(0x3730A3)));
        breakdownRow.add(scoreBadge(
                "Course",
                String.format(Locale.ROOT, "%.1f / 15", sr.coursePart()),
                new Color(0xDBEAFE),
                new Color(0x1D4ED8)));
        breakdownRow.add(scoreBadge(
                "Description",
                String.format(Locale.ROOT, "%.1f / 15", sr.descriptionPart()),
                new Color(0xFCE7F3),
                new Color(0xBE185D)));

        String html = "<html><body style='font-family:Segoe UI,Arial,sans-serif;font-size:13px;"
                + "color:#1f2937;margin:8px 10px;'>"
                + "<div style='margin-bottom:10px;padding:8px 10px;background:#EEF2FF;border:1px solid #C7D2FE;"
                + "border-radius:8px;color:#3730A3;font-weight:600;'>Matched required skills</div>"
                + toHtmlList(sr.skillReasons(), "None")
                + "<div style='margin-top:14px;margin-bottom:10px;padding:8px 10px;background:#FEF2F2;border:1px solid #FECACA;"
                + "border-radius:8px;color:#B91C1C;font-weight:600;'>Unmatched required skills</div>"
                + toHtmlList(sr.unmatchedReasons(), "None")
                + "</body></html>";

        JEditorPane details = new JEditorPane("text/html", html);
        details.setEditable(false);
        details.setOpaque(true);
        details.setBackground(Color.WHITE);
        details.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(details);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xDED4FF)));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(620, 420));

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(hero, BorderLayout.NORTH);
        north.add(breakdownRow, BorderLayout.SOUTH);

        root.add(north, BorderLayout.NORTH);
        root.add(sp, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, root, "Match Score Reason", JOptionPane.PLAIN_MESSAGE);
    }

    private static JPanel scoreBadge(String title, String value, Color bg, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        t.setForeground(new Color(0x475569));
        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        v.setForeground(valueColor);
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private static String toHtmlList(List<String> items, String emptyText) {
        if (items == null || items.isEmpty()) {
            return "<div style='padding-left:8px;color:#6b7280;'>- " + escapeHtmlText(emptyText) + "</div>";
        }
        StringBuilder sb = new StringBuilder("<ul style='margin:0 0 4px 18px;padding:0;'>");
        for (String item : items) {
            sb.append("<li style='margin:2px 0;'>").append(escapeHtmlText(item)).append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private static String escapeHtmlText(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private record ScoreResult(int score,
                               double skillPart,
                               double coursePart,
                               double descriptionPart,
                               List<String> skillReasons,
                               List<String> unmatchedReasons) {
    }

    private record CourseRelevanceResult(double score, List<String> reasons) {
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
            String req = canonicalSkillForMatch(raw);
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
        int extraSkills = countExtraApplicantSkills(requiredWeight.keySet(), applicantSkills);
        double extraSkillBonus = extraSkills * 2.0;
        skillPart = clamp(skillPart + extraSkillBonus, 0.0, 70.0);

        double proficiencyBonus = calculateProficiencyBonus(item, requiredWeight.keySet(), applicantSkills, matchedReasons);
        if (proficiencyBonus > 0) {
            skillPart = clamp(skillPart + proficiencyBonus, 0.0, 70.0);
            matchedReasons.add("Proficiency bonus total: +" + String.format(Locale.ROOT, "%.1f", proficiencyBonus)
                    + " from direct TA skill mastery");
        }

        if (extraSkills > 0) {
            matchedReasons.add("Extra skills bonus: +" + (int) extraSkillBonus + " from " + extraSkills + " additional skill(s)");
        }

        CourseRelevanceResult courseResult = computeCourseRelevance(item, job, applicantText, applicantSkills);
        double coursePart = courseResult.score();

        double descRel = computeDescriptionRelevance(job, applicantText, applicantSkills);
        double descPart = descRel * 15.0;

        int finalScore = (int) Math.round(clamp(skillPart + coursePart + descPart, 0.0, 100.0));
        matchedReasons.addAll(courseResult.reasons());
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

    private static String canonicalSkillForMatch(String skill) {
        return normalizeSkill(MoSkillCatalog.canonicalSkillName(skill));
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private Set<String> extractApplicantSkills(ApplicationItem item) {
        Set<String> applicant = new HashSet<>();
        if (item.getApplicationForm() != null && item.getApplicationForm().getRelevantSkills() != null) {
            for (ApplicationItem.RelevantSkill s : item.getApplicationForm().getRelevantSkills()) {
                String n = canonicalSkillForMatch(RelevantSkillsJson.nameForMatching(s));
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

    private CourseRelevanceResult computeCourseRelevance(ApplicationItem item, Job job, String applicantText, Set<String> applicantSkills) {
        String courseCode = item.getJobSnapshot() == null ? "" : normalizeSkill(item.getJobSnapshot().getCourseCode());
        String courseName = item.getJobSnapshot() == null ? "" : normalizeSkill(item.getJobSnapshot().getCourseName());
        String moduleCode = normalizeSkill(job.getModuleCode());
        String moduleName = normalizeSkill(job.getModuleName());
        String experienceText = extractApplicantCourseExperienceText(item);

        List<String> reasons = new ArrayList<>();
        double score = 0.0;

        if (hasCourseTaExperience(courseCode, courseName, moduleCode, moduleName, experienceText)) {
            score += 0.35;
            reasons.add("Past TA experience on this course (+5)");
        }
        if (hasDirectCourseStudyExperience(courseCode, courseName, moduleCode, moduleName, experienceText)) {
            score += 0.22;
            reasons.add("Past direct study experience on this course");
        }

        double sameSeriesScore = computeSameSeriesCourseScore(courseCode, courseName, moduleCode, moduleName, experienceText, applicantText, applicantSkills);
        if (sameSeriesScore > 0) {
            score += sameSeriesScore;
            reasons.add("Same series course relevance matched");
        }

        double creditScore = computeCreditEvidenceScore(experienceText, applicantText, applicantSkills);
        if (creditScore > 0) {
            score += creditScore;
            reasons.add("Credit-bearing course evidence matched");
        }

        score = clamp(score, 0.0, 1.0);
        return new CourseRelevanceResult(score, reasons);
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

    private int countExtraApplicantSkills(Set<String> requiredSkills, Set<String> applicantSkills) {
        int extra = 0;
        for (String skill : applicantSkills) {
            boolean matched = false;
            for (String req : requiredSkills) {
                if (pairSkillScore(req, skill) >= 0.55) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                extra++;
            }
        }
        return extra;
    }

    private double calculateProficiencyBonus(ApplicationItem item, Set<String> requiredSkills, Set<String> applicantSkills, List<String> reasons) {
        if (item == null) {
            return 0.0;
        }

        // Prefer proficiency declared in this application's relevant skills; fallback to TA profile data.
        Map<String, String> skillProficiency = loadApplicationFormSkillProficiency(item);
        if (item.getUserId() != null && !item.getUserId().isBlank()) {
            Map<String, String> profileProficiency = loadApplicantSkillProficiency(item.getUserId());
            for (Map.Entry<String, String> e : profileProficiency.entrySet()) {
                skillProficiency.putIfAbsent(e.getKey(), e.getValue());
            }
        }
        if (skillProficiency.isEmpty()) {
            return 0.0;
        }

        double bonus = 0.0;
        for (String required : requiredSkills) {
            String bestSkill = null;
            double bestScore = 0.0;
            for (String candidate : applicantSkills) {
                double score = pairSkillScore(required, candidate);
                if (score > bestScore) {
                    bestScore = score;
                    bestSkill = candidate;
                }
            }
            if (bestSkill == null || bestScore < 0.55) {
                continue;
            }
            String proficiency = skillProficiency.get(normalizeSkill(bestSkill));
            double proficiencyWeight = proficiencyBonusValue(proficiency);
            double exactMatchMultiplier = bestScore >= 0.95 ? 1.0 : bestScore >= 0.80 ? 0.85 : 0.70;
            double contribution = proficiencyWeight * exactMatchMultiplier;
            if (contribution > 0) {
                bonus += contribution;
                if (reasons != null) {
                    reasons.add("Proficiency matched: " + required + " ↔ " + bestSkill
                            + " (" + proficiencyLabel(proficiency) + ", skill match "
                            + String.format(Locale.ROOT, "%.2f", bestScore) + ", +"
                            + String.format(Locale.ROOT, "%.1f", contribution) + ")");
                }
            }
        }
        return clamp(bonus, 0.0, 15.0);
    }

    private Map<String, String> loadApplicationFormSkillProficiency(ApplicationItem item) {
        Map<String, String> out = new HashMap<>();
        if (item == null
                || item.getApplicationForm() == null
                || item.getApplicationForm().getRelevantSkills() == null) {
            return out;
        }
        for (ApplicationItem.RelevantSkill skill : item.getApplicationForm().getRelevantSkills()) {
            String name = canonicalSkillForMatch(RelevantSkillsJson.nameForMatching(skill));
            String proficiency = skill == null ? "" : safe(skill.getProficiency());
            if (!name.isBlank() && !proficiency.isBlank()) {
                out.putIfAbsent(name, proficiency);
            }
        }
        return out;
    }

    private Map<String, String> loadApplicantSkillProficiency(String userId) {
        Map<String, String> result = new HashMap<>();
        try {
            Path userFile = findTaUserFile(userId);
            if (userFile == null || !Files.isRegularFile(userFile)) {
                return result;
            }
            JsonNode root = userObjectMapper.readTree(userFile.toFile());
            if (root == null) {
                return result;
            }
            JsonNode skills = root.path("skills");
            JsonNode pools = skills.path("taSkillPool");
            collectSkillProficiencies(result, pools);
            JsonNode other = skills.path("other");
            if (other.isArray()) {
                for (JsonNode node : other) {
                    String name = normalizeSkill(node.path("name").asText(""));
                    String proficiency = node.path("proficiency").asText("");
                    if (!name.isBlank() && !proficiency.isBlank()) {
                        result.putIfAbsent(name, proficiency);
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return result;
    }

    private void collectSkillProficiencies(Map<String, String> out, JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                String name = normalizeSkill(child.path("name").asText(""));
                String proficiency = child.path("proficiency").asText("");
                if (!name.isBlank() && !proficiency.isBlank()) {
                    out.putIfAbsent(name, proficiency);
                }
            }
            return;
        }
        node.fields().forEachRemaining(entry -> collectSkillProficiencies(out, entry.getValue()));
    }

    private double proficiencyBonusValue(String proficiency) {
        String p = normalizeSkill(proficiency);
        if (p.contains("advanced")) {
            return 2.0;
        }
        if (p.contains("intermediate")) {
            return 1.0;
        }
        if (p.contains("beginner")) {
            return 0.25;
        }
        return 0.0;
    }

    private String proficiencyLabel(String proficiency) {
        String p = normalizeSkill(proficiency);
        if (p.contains("advanced")) {
            return "Advanced";
        }
        if (p.contains("intermediate")) {
            return "Intermediate";
        }
        if (p.contains("beginner")) {
            return "Beginner";
        }
        return "Unknown";
    }

    private Path findTaUserFile(String userId) {
        String normalized = userId == null ? "" : userId.trim();
        if (normalized.isBlank()) {
            return null;
        }
        Path usersDir = DataRoot.resolve().resolve("users").resolve("ta");
        if (!Files.isDirectory(usersDir)) {
            return null;
        }
        try (var stream = Files.list(usersDir)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                if (!p.getFileName().toString().endsWith(".json")) {
                    continue;
                }
                try {
                    JsonNode root = userObjectMapper.readTree(p.toFile());
                    if (root != null && normalized.equals(root.path("userId").asText(""))) {
                        return p;
                    }
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private boolean hasCourseTaExperience(String courseCode, String courseName, String moduleCode, String moduleName, String experienceText) {
        return containsAny(experienceText, courseCode, courseName, moduleCode, moduleName)
                && containsAny(experienceText, "ta", "teaching assistant", "assistant");
    }

    private boolean hasDirectCourseStudyExperience(String courseCode, String courseName, String moduleCode, String moduleName, String experienceText) {
        return containsAny(experienceText, courseCode, courseName, moduleCode, moduleName)
                && containsAny(experienceText, "took", "studied", "completed", "passed", "enrolled", "course");
    }

    private double computeSameSeriesCourseScore(String courseCode, String courseName, String moduleCode, String moduleName, String experienceText, String applicantText, Set<String> applicantSkills) {
        Set<String> terms = new LinkedHashSet<>();
        terms.addAll(seriesTokens(courseCode));
        terms.addAll(seriesTokens(moduleCode));
        terms.addAll(seriesNameTokens(courseName));
        terms.addAll(seriesNameTokens(moduleName));

        double score = 0.0;
        for (String term : terms) {
            if (term.isBlank()) {
                continue;
            }
            boolean hit = containsAny(experienceText, term) || containsAny(applicantText, term);
            if (!hit) {
                for (String s : applicantSkills) {
                    if (s.contains(term) || term.contains(s)) {
                        hit = true;
                        break;
                    }
                }
            }
            if (hit) {
                score += term.matches("[A-Za-z]{2,}\\d{2,}") ? 0.16 : 0.08;
            }
        }
        return clamp(score, 0.0, 0.43);
    }

    private double computeCreditEvidenceScore(String experienceText, String applicantText, Set<String> applicantSkills) {
        String merged = normalizeSkill(experienceText + " " + applicantText);
        if (merged.isBlank()) {
            return 0.0;
        }
        double score = 0.0;
        if (containsAny(merged, "credit", "earned credits", "received credit", "academic credit")) {
            score += 0.12;
        }
        if (containsAny(merged, "passed", "completed", "grade", "final mark", "transcript")) {
            score += 0.08;
        }
        for (String skill : applicantSkills) {
            if (containsAny(merged, skill)) {
                score += 0.03;
            }
        }
        return clamp(score, 0.0, 0.18);
    }

    private Set<String> seriesTokens(String code) {
        Set<String> out = new LinkedHashSet<>();
        if (code == null) return out;
        String n = normalizeSkill(code);
        if (n.isBlank()) return out;
        out.add(n);
        String prefix = n.replaceAll("\\d+$", "").trim();
        if (!prefix.isBlank()) out.add(prefix);
        out.addAll(tokenize(n));
        return out;
    }

    private Set<String> seriesNameTokens(String name) {
        Set<String> out = new LinkedHashSet<>();
        if (name == null) return out;
        for (String token : tokenize(normalizeSkill(name))) {
            if (token.length() >= 4) {
                out.add(token);
            }
        }
        return out;
    }

    private boolean containsAny(String text, String... terms) {
        if (text == null || text.isBlank()) return false;
        for (String term : terms) {
            if (term != null && !term.isBlank() && text.contains(normalizeSkill(term))) {
                return true;
            }
        }
        return false;
    }

    private String extractApplicantCourseExperienceText(ApplicationItem item) {
        if (item == null || item.getApplicationForm() == null) {
            return "";
        }
        String relExp = safe(item.getApplicationForm().getRelevantExperience());
        String letter = safe(item.getApplicationForm().getMotivationCoverLetter());
        return normalizeSkill(relExp + " " + letter);
    }

    private void openTaCvPdf(ApplicationItem item) {
        Path pdf = findTaCvPdf(item);
        if (pdf == null) {
            JOptionPane.showMessageDialog(this, "No PDF CV found in data", "View CV", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdf.toFile());
            } else {
                JOptionPane.showMessageDialog(this, "No PDF CV found in data", "View CV", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No PDF CV found in data", "View CV", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Path findTaCvPdf(ApplicationItem item) {
        if (item == null) {
            return null;
        }
        String studentId = safe(item.getStudentId());
        if (studentId.isBlank()) {
            studentId = item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getStudentId());
        }
        if (studentId.isBlank()) {
            return null;
        }

        Path applicationsDir = DataRoot.resolve().resolve("applications");
        if (!Files.isDirectory(applicationsDir)) {
            return null;
        }

        String userId = null;
        try (var stream = Files.list(applicationsDir)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                if (!p.getFileName().toString().endsWith(".json")) {
                    continue;
                }
                try {
                    String text = Files.readString(p);
                    if (text.contains("\"studentId\": \"" + studentId + "\"")) {
                        userId = extractJsonValue(text, "userId");
                        if (userId != null && !userId.isBlank()) {
                            break;
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
            return null;
        }

        if (userId == null || !userId.startsWith("u_ta_")) {
            return null;
        }
        String folder = userId.substring("u_ta_".length());
        Path cvDir = DataRoot.resolve().resolve("uploads").resolve("profile_cv").resolve(folder);
        if (!Files.isDirectory(cvDir)) {
            return null;
        }
        try (var stream = Files.list(cvDir)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                if (p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                    return p;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private static String extractJsonValue(String text, String key) {
        String needle = "\"" + key + "\"";
        int idx = text.indexOf(needle);
        if (idx < 0) {
            return null;
        }
        idx = text.indexOf(':', idx + needle.length());
        if (idx < 0) {
            return null;
        }
        int start = text.indexOf('"', idx + 1);
        if (start < 0) {
            return null;
        }
        int end = text.indexOf('"', start + 1);
        if (end < 0) {
            return null;
        }
        return text.substring(start + 1, end);
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
        refreshApplicationTable(true);
    }

    private void refreshApplicationTable(boolean preserveFilters) {
        List<ApplicationItem> filtered = dataService.filterApplications(
                allApplications,
                searchField == null ? "" : searchField.getText(),
                courseFilter == null ? "" : (String) courseFilter.getSelectedItem(),
                statusFilter == null ? "" : (String) statusFilter.getSelectedItem()
        );
        filtered = sortByMatchScore(filtered);
        applicationTableData = new ArrayList<>(filtered);
        if (applicationTableModel != null) {
            applicationTableModel.fireTableDataChanged();
        }
        if (filterCountLabel != null) {
            int total = allApplications == null ? 0 : allApplications.size();
            filterCountLabel.setText("Showing " + applicationTableData.size() + " of " + total);
        }
    }

    private List<ApplicationItem> sortByMatchScore(List<ApplicationItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        boolean ascending = matchScoreSortOrder != null && matchScoreSortOrder.getSelectedIndex() == 1;
        List<ApplicationItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> {
            int sa = computeMatchScore(a);
            int sb = computeMatchScore(b);
            int cmp = Integer.compare(sa, sb);
            return ascending ? cmp : -cmp;
        });
        return sorted;
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

    private static DefaultTableCellRenderer listZebraRenderer(int alignment) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                label.setOpaque(true);
                label.setHorizontalAlignment(alignment);
                label.setBorder(new EmptyBorder(6, 8, 6, 8));
                if (isSelected) {
                    label.setBackground(table.getSelectionBackground());
                    label.setForeground(table.getSelectionForeground());
                } else {
                    label.setBackground((row % 2 == 0) ? Color.WHITE : LIST_TABLE_ROW_ALT);
                    label.setForeground(MoUiTheme.TEXT_PRIMARY);
                }
                return label;
            }
        };
    }

    private static Color listRowBackground(JTable table, int row, boolean isSelected) {
        if (isSelected) {
            return table.getSelectionBackground();
        }
        return (row % 2 == 0) ? Color.WHITE : LIST_TABLE_ROW_ALT;
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            String status = value == null ? "pending" : value.toString().toLowerCase(Locale.ROOT);

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
                label.setText(status);
            } else {
                switch (status) {
                    case "approved" -> {
                        label.setBackground(new Color(0xDCFCE7));
                        label.setForeground(new Color(0x166534));
                        label.setText("Approved");
                    }
                    case "offer_pending" -> {
                        label.setBackground(new Color(0xFFFBEB));
                        label.setForeground(new Color(0xB45309));
                        label.setText("Offer Pending");
                    }
                    case "rejected" -> {
                        label.setBackground(new Color(0xFEE2E2));
                        label.setForeground(new Color(0xB91C1C));
                        label.setText("Rejected");
                    }
                    default -> {
                        label.setBackground(new Color(0xE0E7FF));
                        label.setForeground(new Color(0x3730A3));
                        label.setText("Pending");
                    }
                }
            }
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new EmptyBorder(4, 10, 4, 10));
            return label;
        }
    }

    private static class MatchScoreRenderer extends JPanel implements TableCellRenderer {
        private final JButton reason = new JButton();

        MatchScoreRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            JLabel score = new JLabel("--/100");
            score.setName("scoreLabel");
            score.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            score.setForeground(new Color(0x5B21B6));
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
                    l.setForeground(isSelected ? table.getSelectionForeground() : new Color(0x5B21B6));
                    break;
                }
            }
            setBackground(listRowBackground(table, row, isSelected));
            return this;
        }
    }

    private static class MatchScoreEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JLabel score = new JLabel("0/100");
        private final JButton reason = new JButton();
        private int row;

        MatchScoreEditor(Consumer<Integer> reasonAction) {
            score.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            score.setForeground(new Color(0x5B21B6));
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
            score.setForeground(isSelected ? table.getSelectionForeground() : new Color(0x5B21B6));
            panel.setBackground(listRowBackground(table, row, isSelected));
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
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            Font small = new Font(Font.SANS_SERIF, Font.BOLD, 11);
            for (JButton b : List.of(detail, review, approve, reject)) {
                b.setFocusPainted(false);
                b.setMargin(new Insets(4, 8, 4, 8));
                b.setFont(small);
                add(b);
            }
            MoUiTheme.styleAccentOutlineButton(detail, 10);
            MoUiTheme.styleAccentOutlineButton(review, 10);
            MoUiTheme.styleTealPrimaryButton(approve, 10);
            MoUiTheme.styleDangerOutlineButton(reject, 10);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(listRowBackground(table, row, isSelected));
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

            Font small = new Font(Font.SANS_SERIF, Font.BOLD, 11);
            for (JButton b : List.of(detail, review, approve, reject)) {
                b.setFocusPainted(false);
                b.setMargin(new Insets(4, 8, 4, 8));
                b.setFont(small);
                panel.add(b);
            }
            MoUiTheme.styleAccentOutlineButton(detail, 10);
            MoUiTheme.styleAccentOutlineButton(review, 10);
            MoUiTheme.styleTealPrimaryButton(approve, 10);
            MoUiTheme.styleDangerOutlineButton(reject, 10);

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
            panel.setBackground(listRowBackground(table, row, isSelected));
            return panel;
        }
    }

    private static class SimpleDetailRenderer extends JPanel implements TableCellRenderer {
        private final JButton detail = new JButton("⌕ Detail");

        SimpleDetailRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
            detail.setFocusPainted(false);
            detail.setMargin(new Insets(3, 6, 3, 6));
            detail.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            MoUiTheme.styleAccentOutlineButton(detail, 8);
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
            detail.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            MoUiTheme.styleAccentOutlineButton(detail, 8);
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
