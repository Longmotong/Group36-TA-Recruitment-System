package TA_Job_Application_Module.pages.applications;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.ApplicationStatusCodes;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;
import TA_Job_Application_Module.service.DataService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * My Applications + Drafts — modern purple portal layout.
 *
 * This version keeps the original data flow, callbacks, filtering, table model,
 * draft management, and column width synchronisation. Most changes are limited
 * to Swing presentation: background, cards, statistics, search strip, tabs, and renderers.
 */
public class Page_MyApplications {

    public interface MyApplicationsCallback {
        void onViewStatus(Application application);

        void onBackToHome();

        void onBrowseJobs();

        void onCancelApplication(Application application);

        void onEditDraft(Application application);
    }

    private static final String PLACEHOLDER_SEARCH = "Search by job title...";

    private static final int JOB_TITLE_COL_MIN_WIDTH = 200;
    private static final int JOB_TITLE_COL_ABS_MIN_WIDTH = 120;
    /** Last two: ACTION (View), CANCEL (Withdraw pill + icon needs ~110px+) */
    private static final int[] REST_COL_WIDTHS = {88, 172, 126, 188, 122, 82, 118};
    private static final int[] REST_COL_FLOORS = {76, 140, 100, 168, 96, 74, 108};
    /** COURSE, DEPT, SAVED, UPDATED, ACTION (Edit), DELETE (compact pill + icon) */
    private static final int[] DRAFT_REST_COL_WIDTHS = {100, 140, 120, 120, 72, 108};
    private static final int[] DRAFT_REST_COL_FLOORS = {72, 88, 80, 80, 56, 78};

    private static final Color PRIMARY_PURPLE = JobsPortalUi.PRIMARY_PURPLE;
    private static final Color DEEP_PURPLE = JobsPortalUi.DEEP_PURPLE;
    private static final Color LAVENDER = JobsPortalUi.LAVENDER;
    private static final Color LIGHT_PURPLE_BORDER = JobsPortalUi.LIGHT_PURPLE_BORDER;
    private static final Color DARK_TEXT = JobsPortalUi.DARK_TEXT;
    private static final Color MUTED_TEXT = JobsPortalUi.MUTED_TEXT;
    private static final Color PAGE_TOP = new Color(253, 252, 255);
    private static final Color PAGE_BOTTOM = new Color(248, 246, 255);
    private static final Color TABLE_LINE = new Color(236, 231, 255);

    private JPanel panel;
    private JPanel northStack;
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JLabel totalValueLabel;
    private JLabel pendingValueLabel;
    private JLabel acceptedValueLabel;
    private JLabel rejectedValueLabel;
    private JLabel offerPendingAlertLabel;
    private JPanel offerPendingAlertPanel;
    private JTabbedPane tabbedPane;

    private JScrollPane applicationsScrollPane;
    private JTable draftsTable;
    private DefaultTableModel draftsTableModel;

    private final List<Application> displayedApplications = new ArrayList<>();
    private final List<Application> displayedDrafts = new ArrayList<>();

    private int[] applicationsTableFixedMins;
    private int[] draftsTableFixedMins;

    private final DataService dataService;
    private final MyApplicationsCallback callback;

    public Page_MyApplications(DataService dataService, MyApplicationsCallback callback) {
        this.dataService = dataService;
        this.callback = callback;
        initPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void refreshTable() {
        String currentFilter = statusFilterCombo != null ? (String) statusFilterCombo.getSelectedItem() : "All Statuses";
        String currentSearch = searchField != null ? searchField.getText().trim() : "";
        List<Application> apps = getFilteredApplications(currentFilter, currentSearch);

        if (apps.isEmpty() && !"All Statuses".equals(currentFilter)) {
            if (statusFilterCombo != null) {
                statusFilterCombo.setSelectedItem("All Statuses");
            }
        }

        updateSummaryCards();
        applyFiltersAndFillTable();
        refreshDraftsTable();
        updateOfferPendingAlert(dataService.getUserApplications());
    }

    private List<Application> getFilteredApplications(String statusFilter, String searchText) {
        List<Application> apps = dataService.getUserApplications();
        if (apps == null) {
            return new ArrayList<>();
        }
        String statusKey = MyApplicationsQuerySupport.selectedStatusFilterKey(statusFilter);
        String query = searchText == null ? "" : searchText.toLowerCase(Locale.ROOT);

        return apps.stream()
                .filter(app -> MyApplicationsQuerySupport.matchesFilter(app, statusKey, query))
                .collect(Collectors.toList());
    }

    public void selectDraftsTab() {
        if (tabbedPane == null) {
            return;
        }
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            String t = tabbedPane.getTitleAt(i);
            if (t != null && t.startsWith("Drafts")) {
                tabbedPane.setSelectedIndex(i);
                scheduleDraftTableWidthSync();
                return;
            }
        }
        if (tabbedPane.getTabCount() > 1) {
            tabbedPane.setSelectedIndex(1);
            scheduleDraftTableWidthSync();
        }
    }

    public void selectApplicationsTab() {
        if (tabbedPane == null) {
            return;
        }
        if (tabbedPane.getTabCount() > 0) {
            tabbedPane.setSelectedIndex(0);
            SwingUtilities.invokeLater(() -> {
                syncJobTitleColumnToFillViewport();
                SwingUtilities.invokeLater(this::syncJobTitleColumnToFillViewport);
            });
        }
    }

    public void scheduleDraftTableWidthSync() {
        SwingUtilities.invokeLater(() -> {
            syncDraftJobTitleColumnToFillViewport();
            SwingUtilities.invokeLater(this::syncDraftJobTitleColumnToFillViewport);
        });
    }

    private void initPanel() {
        panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, PAGE_TOP, 0, getHeight(), PAGE_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(109, 77, 235, 16));
                int startX = Math.max(0, getWidth() - 190);
                for (int x = startX; x < getWidth() - 22; x += 10) {
                    for (int y = 0; y < 72; y += 10) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        panel.setOpaque(true);
        panel.setBackground(JobsPortalUi.PAGE_BG);
        panel.setBorder(new EmptyBorder(16, 40, 20, 40));

        northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.setAlignmentX(Component.LEFT_ALIGNMENT);

        buildBackLink();
        buildTitleRow();
        buildOfferPendingAlert();
        buildSummaryCards();
        buildSearchFilterBar();

        panel.add(northStack, BorderLayout.NORTH);
        buildTabbedPane();
    }

    private void buildOfferPendingAlert() {
        offerPendingAlertPanel = new JPanel(new BorderLayout(12, 0)) {
            private boolean hover;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(239, 68, 68, hover ? 20 : 12));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.setColor(new Color(248, 113, 113, hover ? 190 : 130));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        offerPendingAlertPanel.setOpaque(false);
        offerPendingAlertPanel.setBorder(new EmptyBorder(8, 14, 8, 14));
        offerPendingAlertPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        offerPendingAlertPanel.setVisible(false);
        offerPendingAlertPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        offerPendingAlertPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (tabbedPane != null && tabbedPane.getTabCount() > 0) {
                    tabbedPane.setSelectedIndex(0);
                }
                if (statusFilterCombo != null) {
                    statusFilterCombo.setSelectedItem("Offer Pending");
                }
            }
        });

        JLabel alertIcon = new JLabel(alertCircleIcon(new Color(220, 38, 38), 22));
        offerPendingAlertPanel.add(alertIcon, BorderLayout.WEST);

        offerPendingAlertLabel = new JLabel();
        offerPendingAlertLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        offerPendingAlertLabel.setForeground(new Color(185, 28, 28));
        offerPendingAlertPanel.add(offerPendingAlertLabel, BorderLayout.CENTER);

        offerPendingAlertPanel.add(new JLabel(chevronRightIcon(new Color(220, 38, 38), 16)), BorderLayout.EAST);

        northStack.add(offerPendingAlertPanel);
        northStack.add(Box.createVerticalStrut(8));
    }

    public void updateOfferPendingAlert(List<Application> apps) {
        long offerPendingCount = apps.stream()
                .filter(a -> ApplicationStatusCodes.isOfferPending(
                        a.getStatus() != null ? a.getStatus().getCurrent() : ""))
                .count();

        if (offerPendingCount > 0) {
            offerPendingAlertLabel.setText("ATTENTION: You have " + offerPendingCount
                    + " offer(s) pending your response! Click here to view.");
            offerPendingAlertPanel.setVisible(true);
        } else {
            offerPendingAlertPanel.setVisible(false);
        }
    }

    private void buildBackLink() {
        JButton back = new JButton("←  Back to Home");
        back.setFont(new Font("Segoe UI", Font.BOLD, 13));
        back.setForeground(PRIMARY_PURPLE);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.setHorizontalAlignment(SwingConstants.LEFT);
        back.addActionListener(e -> callback.onBackToHome());
        northStack.add(back);
        northStack.add(Box.createVerticalStrut(6));
    }

    private void buildTitleRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JPanel leftCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftCluster.setOpaque(false);

        JPanel iconTile = JobsPortalUi.wrapRoundedInner(
                new JLabel(applicationsStackIcon(PRIMARY_PURPLE, 22)),
                14,
                LAVENDER,
                LIGHT_PURPLE_BORDER,
                1f,
                false,
                new Insets(8, 8, 8, 8));
        iconTile.setPreferredSize(new Dimension(46, 46));
        iconTile.setMinimumSize(new Dimension(46, 46));
        iconTile.setMaximumSize(new Dimension(46, 46));
        leftCluster.add(iconTile);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titleLabel);
        left.add(Box.createVerticalStrut(2));

        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(42, 3));
        underline.setMinimumSize(new Dimension(42, 3));
        underline.setMaximumSize(new Dimension(42, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(underline);
        left.add(Box.createVerticalStrut(3));

        JLabel subtitleLabel = new JLabel("Track and manage all your TA position applications");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(subtitleLabel);

        leftCluster.add(left);
        row.add(leftCluster, BorderLayout.WEST);

        JobsPortalUi.OutlinePurpleButton browseBtn =
                JobsPortalUi.outlineButton("Browse Jobs", new Font("Segoe UI", Font.BOLD, 13));
        browseBtn.setIcon(JobsPortalUi.briefcaseGlyph(PRIMARY_PURPLE, 15));
        browseBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        browseBtn.setIconTextGap(8);
        browseBtn.addActionListener(e -> callback.onBrowseJobs());
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        east.setOpaque(false);
        east.add(browseBtn);
        row.add(east, BorderLayout.EAST);

        northStack.add(row);
        northStack.add(Box.createVerticalStrut(10));
    }

    private void buildSummaryCards() {
        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 8, 0));
        summaryRow.setOpaque(false);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        summaryRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalValueLabel = new JLabel("0");
        pendingValueLabel = new JLabel("0");
        acceptedValueLabel = new JLabel("0");
        rejectedValueLabel = new JLabel("0");

        Color totalInk = PRIMARY_PURPLE;
        Color amberInk = new Color(217, 119, 6);
        Color greenInk = new Color(5, 150, 105);
        Color redInk = new Color(220, 38, 38);

        int iconPx = 22;

        summaryRow.add(summaryStatTile(
                applicationsStackIcon(totalInk, iconPx),
                new Color(247, 243, 255),
                LIGHT_PURPLE_BORDER,
                totalInk,
                "TOTAL APPLICATIONS",
                totalValueLabel));
        summaryRow.add(summaryStatTile(
                clockOutlineIcon(amberInk, iconPx),
                new Color(255, 251, 235),
                new Color(253, 230, 138),
                amberInk,
                "PENDING",
                pendingValueLabel));
        summaryRow.add(summaryStatTile(
                checkCircleIcon(greenInk, iconPx),
                new Color(236, 253, 245),
                new Color(167, 243, 208),
                greenInk,
                "ACCEPTED",
                acceptedValueLabel));
        summaryRow.add(summaryStatTile(
                xCircleIcon(redInk, iconPx),
                new Color(254, 242, 242),
                new Color(252, 165, 165),
                redInk,
                "REJECTED",
                rejectedValueLabel));

        northStack.add(summaryRow);
        northStack.add(Box.createVerticalStrut(10));
        updateSummaryCards();
    }

    private JPanel summaryStatTile(Icon icon, Color fill, Color stroke, Color ink, String label, JLabel valueLabel) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(12, fill, stroke, 1f, true, new BorderLayout());
        JPanel inner = new JPanel(new BorderLayout(10, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel iconBack = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 170));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(255, 255, 255, 210));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBack.setOpaque(false);
        iconBack.setPreferredSize(new Dimension(38, 38));
        iconBack.add(new JLabel(icon));
        inner.add(iconBack, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel lab = new JLabel(label);
        lab.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lab.setForeground(ink.darker());
        lab.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(ink);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(lab);
        text.add(Box.createVerticalStrut(2));
        text.add(valueLabel);
        inner.add(text, BorderLayout.CENTER);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void updateSummaryCards() {
        int pending = dataService.countApplicationsByStatus("pending");
        int under = dataService.countApplicationsByStatus("under_review");
        int offerPending = dataService.countApplicationsByStatus("offer_pending");
        int accepted = dataService.countApplicationsByStatus("accepted");
        int rejected = dataService.countApplicationsByStatus("rejected");
        int total = pending + under + offerPending + accepted + rejected;

        totalValueLabel.setText(String.valueOf(total));
        pendingValueLabel.setText(String.valueOf(pending));
        acceptedValueLabel.setText(String.valueOf(offerPending + accepted));
        rejectedValueLabel.setText(String.valueOf(rejected));
    }

    private void buildSearchFilterBar() {
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] statuses = {"All Statuses", "Pending", "Under Review", "Offer Pending", "Accepted", "Rejected"};
        statusFilterCombo = new JComboBox<>(statuses);
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilterCombo.setForeground(DARK_TEXT);
        statusFilterCombo.setBackground(Color.WHITE);
        statusFilterCombo.setOpaque(false);
        statusFilterCombo.setFocusable(false);
        statusFilterCombo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        statusFilterCombo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton();
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setFocusable(false);
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        statusFilterCombo.addActionListener(e -> {
            if (tableModel != null) {
                applyFiltersAndFillTable();
            }
        });
        Dimension comboSize = statusFilterCombo.getPreferredSize();
        statusFilterCombo.setPreferredSize(new Dimension(Math.max(176, comboSize.width), 30));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (tableModel != null) {
                    applyFiltersAndFillTable();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (tableModel != null) {
                    applyFiltersAndFillTable();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (tableModel != null) {
                    applyFiltersAndFillTable();
                }
            }
        });
        installSearchPlaceholder();

        JPanel strip = buildModernSearchFilterStrip();
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        northStack.add(strip);
        northStack.add(Box.createVerticalStrut(8));
    }

    private JPanel buildModernSearchFilterStrip() {
        JobsPortalUi.RoundedSurface strip = new JobsPortalUi.RoundedSurface(14, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        JPanel inner = new JPanel(new BorderLayout(12, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel(searchIcon(MUTED_TEXT, 17)), BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);
        JPanel searchShell = JobsPortalUi.wrapRoundedInner(searchRow, 11, Color.WHITE, new Color(221, 226, 236), 1f, false, new Insets(4, 8, 4, 8));
        searchShell.setPreferredSize(new Dimension(440, 34));
        inner.add(searchShell, BorderLayout.CENTER);

        JPanel filterRow = new JPanel(new BorderLayout(6, 0));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel(funnelIcon(PRIMARY_PURPLE, 15)), BorderLayout.WEST);
        filterRow.add(statusFilterCombo, BorderLayout.CENTER);
        filterRow.add(new JLabel(JobsPortalUi.chevronDownIcon(MUTED_TEXT, 12)), BorderLayout.EAST);
        JPanel filterShell = JobsPortalUi.wrapRoundedInner(filterRow, 11, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, false, new Insets(4, 8, 4, 8));
        filterShell.setPreferredSize(new Dimension(255, 34));
        inner.add(filterShell, BorderLayout.EAST);

        strip.add(inner, BorderLayout.CENTER);
        return strip;
    }

    private void installSearchPlaceholder() {
        searchField.setForeground(MUTED_TEXT);
        searchField.setText(PLACEHOLDER_SEARCH);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (PLACEHOLDER_SEARCH.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(DARK_TEXT);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(MUTED_TEXT);
                    searchField.setText(PLACEHOLDER_SEARCH);
                }
            }
        });
    }

    private String getSearchQuery() {
        String t = searchField.getText();
        if (PLACEHOLDER_SEARCH.equals(t)) {
            return "";
        }
        return t == null ? "" : t.trim();
    }

    private String selectedStatusFilterKey() {
        if (statusFilterCombo == null) {
            return null;
        }
        Object sel = statusFilterCombo.getSelectedItem();
        return MyApplicationsQuerySupport.selectedStatusFilterKey(sel);
    }

    private void buildTabbedPane() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setForeground(MUTED_TEXT);
        tabbedPane.setBackground(new Color(0, 0, 0, 0));
        tabbedPane.setOpaque(false);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tabbedPane.setUI(new ModernTabbedPaneUI());

        JPanel applicationsTab = new JPanel(new BorderLayout());
        applicationsTab.setOpaque(false);
        applicationsTab.setBackground(JobsPortalUi.PAGE_BG);
        buildTable();
        applicationsTab.add(wrapTableInCard(applicationsTable.getTableHeader(), applicationsScrollPane), BorderLayout.CENTER);

        JPanel draftsTab = new JPanel(new BorderLayout());
        draftsTab.setOpaque(false);
        draftsTab.setBackground(JobsPortalUi.PAGE_BG);
        buildDraftsTable();
        JScrollPane draftsScroll = new JScrollPane(draftsTable);
        styleScrollPane(draftsScroll);
        draftsScroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncDraftJobTitleColumnToFillViewport();
            }
        });
        draftsTab.add(wrapTableInCard(draftsTable.getTableHeader(), draftsScroll), BorderLayout.CENTER);

        tabbedPane.addTab("Applications", applicationsTab);
        tabbedPane.addTab("Drafts", draftsTab);
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int idx = tabbedPane.getSelectedIndex();
                if (idx < 0) {
                    return;
                }
                if (idx == 0) {
                    SwingUtilities.invokeLater(() -> {
                        syncJobTitleColumnToFillViewport();
                        SwingUtilities.invokeLater(Page_MyApplications.this::syncJobTitleColumnToFillViewport);
                    });
                }
                String t = tabbedPane.getTitleAt(idx);
                if (t != null && t.startsWith("Drafts")) {
                    scheduleDraftTableWidthSync();
                }
            }
        });

        panel.add(tabbedPane, BorderLayout.CENTER);
        refreshDraftsTable();
        SwingUtilities.invokeLater(this::syncDraftJobTitleColumnToFillViewport);
    }

    private JPanel wrapTableInCard(JTableHeader header, JScrollPane scrollPane) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(16, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        card.add(header, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(4, 0, 0, 0));
        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private void buildTable() {
        String[] columns = {"JOB TITLE", "COURSE", "DEPARTMENT", "APPLIED DATE", "STATUS", "LAST UPDATED", "ACTION", "CANCEL"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        applicationsTable = new JTable(tableModel);
        applicationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        applicationsTable.setRowHeight(48);
        applicationsTable.setGridColor(TABLE_LINE);
        applicationsTable.setShowGrid(false);
        applicationsTable.setShowHorizontalLines(false);
        applicationsTable.setIntercellSpacing(new Dimension(0, 0));
        applicationsTable.setSelectionBackground(new Color(239, 235, 255));
        applicationsTable.setSelectionForeground(DARK_TEXT);
        applicationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        applicationsTable.setFillsViewportHeight(true);
        applicationsTable.setOpaque(false);

        JTableHeader header = applicationsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(PRIMARY_PURPLE);
        header.setBackground(new Color(252, 250, 255));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_LINE));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(JLabel.LEFT);
                c.setBorder(new EmptyBorder(0, 14, 0, 14));
                c.setBackground(new Color(252, 250, 255));
                c.setForeground(PRIMARY_PURPLE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String t = value != null ? value.toString() : "";
                c.setText("<html><body style='white-space:nowrap'>" + escapeHtmlPlain(t) + "</body></html>");
                return c;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        DefaultTableCellRenderer leftRenderer = new ModernTextRenderer(false);
        DefaultTableCellRenderer titleRenderer = new ModernTextRenderer(true);

        for (int i = 0; i < columns.length; i++) {
            TableColumn col = applicationsTable.getColumnModel().getColumn(i);
            if (i == 0) {
                col.setCellRenderer(titleRenderer);
            } else if (i == 4) {
                col.setCellRenderer(new StatusPillRenderer());
            } else if (i == 6) {
                col.setCellRenderer(new LinkRenderer("View"));
            } else if (i == 7) {
                col.setCellRenderer(new WithdrawRenderer());
            } else {
                col.setCellRenderer(leftRenderer);
            }
        }

        applyColumnWidths();

        final int actionCol = applicationsTable.getColumn("ACTION").getModelIndex();
        final int cancelCol = applicationsTable.getColumn("CANCEL").getModelIndex();
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
                int modelRow = applicationsTable.convertRowIndexToModel(viewRow);
                if (modelRow < 0 || modelRow >= displayedApplications.size()) {
                    return;
                }
                if (modelCol == actionCol) {
                    callback.onViewStatus(displayedApplications.get(modelRow));
                } else if (modelCol == cancelCol) {
                    Application app = displayedApplications.get(modelRow);
                    UIManager.put("OptionPane.yesButtonText", "Yes");
                    UIManager.put("OptionPane.noButtonText", "No");
                    int confirm = JOptionPane.showConfirmDialog(
                            panel,
                            "Are you sure you want to withdraw this application?\nThis action cannot be undone.",
                            "Confirm Withdrawal",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        callback.onCancelApplication(app);
                    }
                }
            }
        });

        applicationsScrollPane = new JScrollPane(applicationsTable);
        styleScrollPane(applicationsScrollPane);
        applicationsScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncJobTitleColumnToFillViewport();
            }
        });

        applyFiltersAndFillTable();
        SwingUtilities.invokeLater(this::syncJobTitleColumnToFillViewport);
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setBackground(new Color(250, 250, 255));
    }

    private void applyColumnWidths() {
        TableColumnModel cm = applicationsTable.getColumnModel();
        if (cm.getColumnCount() < 8) {
            return;
        }
        Font headerFont = new Font("Segoe UI", Font.BOLD, 10);
        String[] hdrs = {"JOB TITLE", "COURSE", "DEPARTMENT", "APPLIED DATE", "STATUS", "LAST UPDATED", "ACTION", "CANCEL"};
        int jobHdrW = textWidthForFont(headerFont, hdrs[0]) + 22;
        cm.getColumn(0).setMinWidth(Math.max(JOB_TITLE_COL_ABS_MIN_WIDTH, jobHdrW));

        applicationsTableFixedMins = new int[7];
        for (int i = 0; i < 7; i++) {
            int headerNeed = textWidthForFont(headerFont, hdrs[i + 1]) + 22;
            applicationsTableFixedMins[i] = Math.max(REST_COL_FLOORS[i], headerNeed);
            TableColumn c = cm.getColumn(i + 1);
            c.setPreferredWidth(REST_COL_WIDTHS[i]);
            c.setMinWidth(applicationsTableFixedMins[i]);
        }
    }

    private void syncJobTitleColumnToFillViewport() {
        if (applicationsTable == null) {
            return;
        }
        Container parent = applicationsTable.getParent();
        if (!(parent instanceof JViewport vp)) {
            return;
        }
        int vw = vp.getWidth();
        if (vw <= 0) {
            return;
        }
        TableColumnModel cm = applicationsTable.getColumnModel();
        if (cm.getColumnCount() < 8) {
            return;
        }

        int nGap = Math.max(0, cm.getColumnCount() - 1);
        int spacing = applicationsTable.getIntercellSpacing().width * nGap;
        int fudge = 20;
        int available = vw - spacing - fudge;
        if (available < 80) {
            return;
        }

        int fixedNominal = 0;
        for (int w : REST_COL_WIDTHS) {
            fixedNominal += w;
        }
        int[] floorArr = applicationsTableFixedMins != null ? applicationsTableFixedMins : REST_COL_FLOORS;
        int floorsSum = 0;
        for (int w : floorArr) {
            floorsSum += w;
        }

        TableColumn c0 = cm.getColumn(0);
        int titleW;
        int[] applied = new int[REST_COL_WIDTHS.length];

        if (available >= fixedNominal + JOB_TITLE_COL_MIN_WIDTH) {
            titleW = available - fixedNominal;
            titleW = Math.max(titleW, JOB_TITLE_COL_MIN_WIDTH);
            System.arraycopy(REST_COL_WIDTHS, 0, applied, 0, REST_COL_WIDTHS.length);
        } else {
            int budgetForFixed = available - JOB_TITLE_COL_MIN_WIDTH;
            if (budgetForFixed < floorsSum) {
                System.arraycopy(floorArr, 0, applied, 0, floorArr.length);
                titleW = Math.max(JOB_TITLE_COL_ABS_MIN_WIDTH, available - floorsSum);
            } else {
                float scale = (float) budgetForFixed / (float) fixedNominal;
                int sumApplied = 0;
                for (int i = 0; i < REST_COL_WIDTHS.length; i++) {
                    int w = Math.round(REST_COL_WIDTHS[i] * scale);
                    w = Math.max(floorArr[i], w);
                    applied[i] = w;
                    sumApplied += w;
                }
                while (sumApplied > budgetForFixed) {
                    int best = -1;
                    int bestSlack = 0;
                    for (int i = 0; i < applied.length; i++) {
                        int slack = applied[i] - floorArr[i];
                        if (slack > bestSlack) {
                            bestSlack = slack;
                            best = i;
                        }
                    }
                    if (best < 0 || bestSlack <= 0) {
                        break;
                    }
                    applied[best]--;
                    sumApplied--;
                }
                titleW = available - sumApplied;
                titleW = Math.max(JOB_TITLE_COL_ABS_MIN_WIDTH, titleW);
                if (titleW + sumApplied > available) {
                    titleW = available - sumApplied;
                }
            }
            for (int i = 0; i < applied.length; i++) {
                TableColumn c = cm.getColumn(i + 1);
                c.setPreferredWidth(applied[i]);
                c.setWidth(applied[i]);
            }
            c0.setPreferredWidth(titleW);
            c0.setWidth(titleW);
            applicationsTable.revalidate();
            applicationsTable.repaint();
            SwingUtilities.invokeLater(this::adjustRowHeightsForJobTitles);
            return;
        }

        c0.setPreferredWidth(titleW);
        c0.setWidth(titleW);
        for (int i = 0; i < REST_COL_WIDTHS.length; i++) {
            TableColumn c = cm.getColumn(i + 1);
            c.setPreferredWidth(applied[i]);
            c.setWidth(applied[i]);
        }

        applicationsTable.revalidate();
        applicationsTable.repaint();
        SwingUtilities.invokeLater(this::adjustRowHeightsForJobTitles);
    }

    private void applyDraftColumnWidths() {
        TableColumnModel cm = draftsTable.getColumnModel();
        if (cm.getColumnCount() < 7) {
            return;
        }
        Font headerFont = new Font("Segoe UI", Font.BOLD, 11);
        String[] hdrs = {"JOB TITLE", "COURSE", "DEPARTMENT", "SAVED DATE", "LAST UPDATED", "ACTION", "DELETE"};
        int jobHdrW = textWidthForFont(headerFont, hdrs[0]) + 22;
        cm.getColumn(0).setMinWidth(Math.max(JOB_TITLE_COL_ABS_MIN_WIDTH, jobHdrW));

        draftsTableFixedMins = new int[DRAFT_REST_COL_WIDTHS.length];
        for (int i = 0; i < DRAFT_REST_COL_WIDTHS.length; i++) {
            int headerNeed = textWidthForFont(headerFont, hdrs[i + 1]) + 22;
            draftsTableFixedMins[i] = Math.max(DRAFT_REST_COL_FLOORS[i], headerNeed);
            TableColumn c = cm.getColumn(i + 1);
            c.setPreferredWidth(DRAFT_REST_COL_WIDTHS[i]);
            c.setMinWidth(draftsTableFixedMins[i]);
        }
    }

    private void syncDraftJobTitleColumnToFillViewport() {
        if (draftsTable == null) {
            return;
        }
        Container parent = draftsTable.getParent();
        if (!(parent instanceof JViewport vp)) {
            return;
        }
        int vw = vp.getWidth();
        if (vw <= 0) {
            return;
        }
        TableColumnModel cm = draftsTable.getColumnModel();
        if (cm.getColumnCount() < 7) {
            return;
        }

        int nGap = Math.max(0, cm.getColumnCount() - 1);
        int spacing = draftsTable.getIntercellSpacing().width * nGap;
        int fudge = 16;
        int available = vw - spacing - fudge;
        if (available < 80) {
            return;
        }

        int fixedNominal = 0;
        for (int w : DRAFT_REST_COL_WIDTHS) {
            fixedNominal += w;
        }
        int[] floorArr = draftsTableFixedMins != null ? draftsTableFixedMins : DRAFT_REST_COL_FLOORS;
        int floorsSum = 0;
        for (int w : floorArr) {
            floorsSum += w;
        }

        TableColumn c0 = cm.getColumn(0);
        int titleW;
        int[] applied = new int[DRAFT_REST_COL_WIDTHS.length];

        if (available >= fixedNominal + JOB_TITLE_COL_ABS_MIN_WIDTH) {
            titleW = available - fixedNominal;
            titleW = Math.max(titleW, JOB_TITLE_COL_ABS_MIN_WIDTH);
            System.arraycopy(DRAFT_REST_COL_WIDTHS, 0, applied, 0, DRAFT_REST_COL_WIDTHS.length);
        } else {
            int budgetForFixed = available - JOB_TITLE_COL_ABS_MIN_WIDTH;
            if (budgetForFixed < floorsSum) {
                System.arraycopy(floorArr, 0, applied, 0, floorArr.length);
                titleW = Math.max(JOB_TITLE_COL_ABS_MIN_WIDTH, available - floorsSum);
            } else {
                float scale = (float) budgetForFixed / (float) fixedNominal;
                int sumApplied = 0;
                for (int i = 0; i < DRAFT_REST_COL_WIDTHS.length; i++) {
                    int w = Math.round(DRAFT_REST_COL_WIDTHS[i] * scale);
                    w = Math.max(floorArr[i], w);
                    applied[i] = w;
                    sumApplied += w;
                }
                while (sumApplied > budgetForFixed) {
                    int best = -1;
                    int bestSlack = 0;
                    for (int i = 0; i < applied.length; i++) {
                        int slack = applied[i] - floorArr[i];
                        if (slack > bestSlack) {
                            bestSlack = slack;
                            best = i;
                        }
                    }
                    if (best < 0 || bestSlack <= 0) {
                        break;
                    }
                    applied[best]--;
                    sumApplied--;
                }
                titleW = available - sumApplied;
                titleW = Math.max(JOB_TITLE_COL_ABS_MIN_WIDTH, titleW);
                if (titleW + sumApplied > available) {
                    titleW = available - sumApplied;
                }
            }
        }

        c0.setPreferredWidth(titleW);
        c0.setWidth(titleW);
        for (int i = 0; i < applied.length; i++) {
            TableColumn c = cm.getColumn(i + 1);
            c.setPreferredWidth(applied[i]);
            c.setWidth(applied[i]);
        }

        draftsTable.revalidate();
        draftsTable.repaint();
    }

    private void applyFiltersAndFillTable() {
        if (tableModel == null) {
            return;
        }
        displayedApplications.clear();
        tableModel.setRowCount(0);

        String q = getSearchQuery().toLowerCase(Locale.ROOT);
        String statusKey = selectedStatusFilterKey();

        for (Application app : dataService.getUserApplications()) {
            String title = app.getJobSnapshot() != null ? app.getJobSnapshot().getTitle() : "";
            if (!q.isEmpty() && (title == null || !title.toLowerCase(Locale.ROOT).contains(q))) {
                continue;
            }
            if (statusKey != null) {
                String cur = app.getStatus() != null ? app.getStatus().getCurrent() : "";
                if (!statusKey.equals(cur)) {
                    continue;
                }
            }
            displayedApplications.add(app);
        }

        DateTimeFormatter outDate = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

        for (Application app : displayedApplications) {
            String statusLabel = app.getStatus() != null ? app.getStatus().getLabel() : "";
            String submitted = "";
            if (app.getMeta() != null && app.getMeta().getSubmittedAt() != null) {
                submitted = MyApplicationsQuerySupport.formatDateOnly(app.getMeta().getSubmittedAt(), outDate);
            }
            String lastUp = "";
            if (app.getStatus() != null && app.getStatus().getLastUpdated() != null) {
                lastUp = MyApplicationsQuerySupport.formatDateOnly(app.getStatus().getLastUpdated(), outDate);
            }

            String curStatus = app.getStatus() != null ? app.getStatus().getCurrent() : "";
            boolean canCancel = "pending".equalsIgnoreCase(curStatus);
            Object[] row = {
                    app.getJobSnapshot() != null ? app.getJobSnapshot().getTitle() : "",
                    app.getJobSnapshot() != null ? app.getJobSnapshot().getCourseCode() : "",
                    app.getJobSnapshot() != null ? app.getJobSnapshot().getDepartment() : "",
                    submitted,
                    statusLabel,
                    lastUp,
                    "view",
                    canCancel ? "withdraw" : ""
            };
            tableModel.addRow(row);
        }

        SwingUtilities.invokeLater(() -> {
            syncJobTitleColumnToFillViewport();
            adjustRowHeightsForJobTitles();
            updateOfferPendingAlert(displayedApplications);
        });
    }

    private void adjustRowHeightsForJobTitles() {
        if (applicationsTable == null || tableModel == null) {
            return;
        }
        int rows = tableModel.getRowCount();
        for (int row = 0; row < rows; row++) {
            Component comp = applicationsTable.prepareRenderer(applicationsTable.getCellRenderer(row, 0), row, 0);
            int h = comp.getPreferredSize().height;
            applicationsTable.setRowHeight(row, Math.max(48, h + 6));
        }
    }

    private static String escapeHtmlPlain(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static int textWidthForFont(Font font, String text) {
        JLabel scratch = new JLabel();
        FontMetrics fm = scratch.getFontMetrics(font);
        return fm.stringWidth(text == null ? "" : text);
    }

    private static String ellipsizeToWidth(String full, FontMetrics fm, int maxPx) {
        if (full == null || full.isEmpty()) {
            return "";
        }
        if (maxPx <= 14) {
            return "...";
        }
        if (fm.stringWidth(full) <= maxPx) {
            return full;
        }
        final String ell = "...";
        int ellW = fm.stringWidth(ell);
        if (ellW >= maxPx) {
            return ell;
        }
        int lo = 0;
        int hi = full.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (fm.stringWidth(full.substring(0, mid)) + ellW <= maxPx) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo == 0 ? ell : full.substring(0, lo) + ell;
    }

    private void buildDraftsTable() {
        String[] columns = {"JOB TITLE", "COURSE", "DEPARTMENT", "SAVED DATE", "LAST UPDATED", "ACTION", "DELETE"};
        draftsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        draftsTable = new JTable(draftsTableModel);
        draftsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        draftsTable.setRowHeight(48);
        draftsTable.setGridColor(TABLE_LINE);
        draftsTable.setShowGrid(false);
        draftsTable.setShowHorizontalLines(false);
        draftsTable.setIntercellSpacing(new Dimension(0, 0));
        draftsTable.setSelectionBackground(new Color(239, 235, 255));
        draftsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        draftsTable.setFillsViewportHeight(true);
        draftsTable.setOpaque(false);

        JTableHeader header = draftsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(PRIMARY_PURPLE);
        header.setBackground(new Color(252, 250, 255));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_LINE));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(JLabel.LEFT);
                c.setBorder(new EmptyBorder(0, 14, 0, 14));
                c.setBackground(new Color(252, 250, 255));
                c.setForeground(PRIMARY_PURPLE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return c;
            }
        });

        DefaultTableCellRenderer leftRenderer = new ModernTextRenderer(false);

        for (int i = 0; i < columns.length; i++) {
            TableColumn col = draftsTable.getColumnModel().getColumn(i);
            if (i == 5) {
                col.setCellRenderer(new LinkRenderer("Edit", true));
            } else if (i == 6) {
                col.setCellRenderer(new DeleteRenderer());
            } else {
                col.setCellRenderer(leftRenderer);
            }
        }

        applyDraftColumnWidths();

        draftsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                Point p = e.getPoint();
                int viewRow = draftsTable.rowAtPoint(p);
                int viewCol = draftsTable.columnAtPoint(p);
                if (viewRow < 0 || viewCol < 0) {
                    return;
                }
                int modelCol = draftsTable.convertColumnIndexToModel(viewCol);
                int modelRow = draftsTable.convertRowIndexToModel(viewRow);
                if (modelRow < 0 || modelRow >= displayedDrafts.size()) {
                    return;
                }
                if (modelCol == 5) {
                    callback.onEditDraft(displayedDrafts.get(modelRow));
                } else if (modelCol == 6) {
                    Application draft = displayedDrafts.get(modelRow);
                    UIManager.put("OptionPane.yesButtonText", "Yes");
                    UIManager.put("OptionPane.noButtonText", "No");
                    int confirm = JOptionPane.showConfirmDialog(panel,
                            "Delete this draft?\n\nJob: " + (draft.getJobSnapshot() != null ? draft.getJobSnapshot().getTitle() : ""),
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        dataService.deleteDraft(draft.getApplicationId());
                        refreshDraftsTable();
                    }
                }
            }
        });
    }

    private void refreshDraftsTable() {
        if (draftsTableModel == null) {
            return;
        }
        draftsTableModel.setRowCount(0);
        displayedDrafts.clear();

        DateTimeFormatter outDate = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
        for (Application draft : dataService.getDrafts()) {
            String title = draft.getJobSnapshot() != null ? draft.getJobSnapshot().getTitle() : "";
            String course = draft.getJobSnapshot() != null ? draft.getJobSnapshot().getCourseCode() : "";
            String dept = draft.getJobSnapshot() != null ? draft.getJobSnapshot().getDepartment() : "";
            String saved = MyApplicationsQuerySupport.formatDateOnly(draft.getMeta() != null ? draft.getMeta().getSubmittedAt() : null, outDate);
            String updated = MyApplicationsQuerySupport.formatDateOnly(draft.getStatus() != null ? draft.getStatus().getLastUpdated() : null, outDate);
            displayedDrafts.add(draft);
            draftsTableModel.addRow(new Object[]{title, course, dept, saved, updated, "Edit", "Delete"});
        }

        int draftCount = dataService.getDrafts().size();
        if (tabbedPane != null) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getTitleAt(i) != null && tabbedPane.getTitleAt(i).startsWith("Drafts")) {
                    tabbedPane.setTitleAt(i, "Drafts" + (draftCount > 0 ? " (" + draftCount + ")" : ""));
                }
            }
        }
        SwingUtilities.invokeLater(this::syncDraftJobTitleColumnToFillViewport);
    }

    private final class ModernTextRenderer extends DefaultTableCellRenderer {
        private final boolean titleColumn;

        ModernTextRenderer(boolean titleColumn) {
            this.titleColumn = titleColumn;
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setHorizontalAlignment(JLabel.LEFT);
            c.setVerticalAlignment(JLabel.CENTER);
            c.setBorder(new EmptyBorder(0, 14, 0, 14));
            c.setOpaque(true);
            c.setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            c.setForeground(titleColumn ? DARK_TEXT : new Color(31, 41, 55));
            c.setFont(new Font("Segoe UI", titleColumn ? Font.BOLD : Font.PLAIN, 13));

            if (titleColumn) {
                String full = value != null ? value.toString() : "";
                c.setToolTipText(full.isEmpty() ? null : full);
                int colW = table.getColumnModel().getColumn(column).getWidth();
                FontMetrics fm = table.getFontMetrics(c.getFont());
                int avail = Math.max(24, colW - 30);
                c.setText(ellipsizeToWidth(full, fm, avail));
            } else {
                c.setText(value != null ? value.toString() : "");
            }
            return c;
        }
    }

    private final class StatusPillRenderer extends JPanel implements TableCellRenderer {
        private final JLabel dot = new JLabel();
        private final JLabel text = new JLabel();

        StatusPillRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(true);
            setBorder(new EmptyBorder(10, 8, 10, 8));
            JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Color bg = (Color) getClientProperty("pillBg");
                    Color border = (Color) getClientProperty("pillBorder");
                    if (bg == null) bg = Color.WHITE;
                    if (border == null) border = TABLE_LINE;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.setColor(border);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setBorder(new EmptyBorder(5, 11, 5, 11));
            dot.setPreferredSize(new Dimension(8, 8));
            text.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pill.add(dot);
            pill.add(text);
            add(pill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            String label = value != null ? value.toString() : "";
            Color ink = statusInk(label);
            Color bg = statusBg(label);
            Color border = statusBorder(label);

            JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.setColor(border);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setBorder(new EmptyBorder(5, 11, 5, 11));
            JLabel d = new JLabel(statusDotIcon(ink, 8));
            JLabel t = new JLabel(label);
            t.setFont(new Font("Segoe UI", Font.BOLD, 12));
            t.setForeground(ink);
            pill.add(d);
            pill.add(t);
            add(pill);
            return this;
        }
    }

    private final class LinkRenderer extends JLabel implements TableCellRenderer {
        private final String label;

        LinkRenderer(String label) {
            this(label, false);
        }

        LinkRenderer(String label, boolean centerAlign) {
            this.label = label;
            setOpaque(true);
            setHorizontalAlignment(centerAlign ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, centerAlign ? 4 : 14, 0, centerAlign ? 4 : 14));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(PRIMARY_PURPLE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            setText("<html><u>" + label + "</u></html>");
            return this;
        }
    }

    private final class WithdrawRenderer extends JPanel implements TableCellRenderer {
        WithdrawRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setBorder(new EmptyBorder(6, 4, 6, 4));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            String v = value != null ? value.toString() : "";
            if (v.isBlank()) {
                JLabel dash = new JLabel("—");
                dash.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                dash.setForeground(new Color(156, 163, 175));
                add(dash);
            } else {
                add(actionPill("Withdraw", new Color(254, 242, 242), new Color(252, 165, 165), new Color(220, 38, 38),
                        trashIcon(new Color(220, 38, 38), 12), true));
            }
            return this;
        }
    }

    private final class DeleteRenderer extends JPanel implements TableCellRenderer {
        DeleteRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            setBorder(new EmptyBorder(6, 2, 6, 2));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            add(actionPill("Delete", new Color(254, 242, 242), new Color(252, 165, 165), new Color(220, 38, 38),
                    trashIcon(new Color(220, 38, 38), 12), true));
            return this;
        }
    }

    /**
     * @param compact tighter padding / font / corner radius so pills fit narrow table columns without clipping
     */
    private JPanel actionPill(String text, Color bg, Color border, Color ink, Icon icon, boolean compact) {
        final int gap = compact ? 3 : 6;
        final int padH = compact ? 5 : 10;
        final int padV = compact ? 3 : 6;
        final int arc = compact ? 8 : 10;
        final int fontPx = compact ? 11 : 12;

        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.setBorder(new EmptyBorder(padV, padH, padV, padH));
        if (icon != null) {
            pill.add(new JLabel(icon));
        }
        JLabel lab = new JLabel(text);
        lab.setForeground(ink);
        lab.setFont(new Font("Segoe UI", Font.BOLD, fontPx));
        pill.add(lab);
        return pill;
    }

    private static Color rowBackground(int row) {
        return row % 2 == 0 ? Color.WHITE : new Color(253, 252, 255);
    }

    private static Color statusInk(String label) {
        String v = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (v.contains("pending")) return new Color(180, 83, 9);
        if (v.contains("review")) return new Color(37, 99, 235);
        if (v.contains("accepted") || v.contains("offer")) return new Color(5, 150, 105);
        if (v.contains("reject")) return new Color(220, 38, 38);
        return PRIMARY_PURPLE;
    }

    private static Color statusBg(String label) {
        String v = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (v.contains("pending")) return new Color(255, 251, 235);
        if (v.contains("review")) return new Color(239, 246, 255);
        if (v.contains("accepted") || v.contains("offer")) return new Color(236, 253, 245);
        if (v.contains("reject")) return new Color(254, 242, 242);
        return LAVENDER;
    }

    private static Color statusBorder(String label) {
        String v = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (v.contains("pending")) return new Color(253, 230, 138);
        if (v.contains("review")) return new Color(191, 219, 254);
        if (v.contains("accepted") || v.contains("offer")) return new Color(167, 243, 208);
        if (v.contains("reject")) return new Color(252, 165, 165);
        return LIGHT_PURPLE_BORDER;
    }

    private static final class ModernTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets = new Insets(0, 0, 0, 0);
            contentBorderInsets = new Insets(4, 0, 0, 0);
            tabInsets = new Insets(7, 16, 7, 16);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(new Color(243, 238, 255));
                g2.fillRoundRect(x + 3, y + 4, w - 6, h - 8, 12, 12);
            }
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(PRIMARY_PURPLE);
                g2.fillRoundRect(x + 8, y + h - 4, w - 16, 3, 3, 3);
                g2.dispose();
            }
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // Table card paints its own border.
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // No dotted focus rectangle for the dashboard look.
        }
    }

    private static Icon applicationsStackIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                g2.drawRoundRect(x + 6, y + 4, s - 10, s - 9, 4, 4);
                g2.drawRoundRect(x + 3, y + 7, s - 10, s - 9, 4, 4);
                g2.drawLine(x + 8, y + 12, x + s - 9, y + 12);
                g2.drawLine(x + 8, y + 17, x + s - 11, y + 17);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private static Icon alertCircleIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(x, y, size, size);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + size / 2;
                g2.drawLine(cx, y + 7, cx, y + size - 11);
                g2.fillOval(cx - 2, y + size - 7, 4, 4);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }

            @Override
            public int getIconHeight() { return size; }
        };
    }

    private static Icon clockOutlineIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 4, y + 4, size - 8, size - 8);
                int cx = x + size / 2;
                int cy = y + size / 2;
                g2.drawLine(cx, cy, cx, y + 8);
                g2.drawLine(cx, cy, x + size - 8, cy);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Icon checkCircleIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 4, y + 4, size - 8, size - 8);
                g2.drawLine(x + size / 3, y + size / 2, x + size / 2 - 1, y + size - 9);
                g2.drawLine(x + size / 2 - 1, y + size - 9, x + size - 7, y + 8);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Icon xCircleIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 4, y + 4, size - 8, size - 8);
                g2.drawLine(x + 10, y + 10, x + size - 10, y + size - 10);
                g2.drawLine(x + size - 10, y + 10, x + 10, y + size - 10);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Icon searchIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int r = size / 2;
                g2.drawOval(x + 3, y + 3, r, r);
                g2.drawLine(x + 3 + r - 1, y + 3 + r - 1, x + size - 4, y + size - 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Icon funnelIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 4;
                Polygon p = new Polygon();
                p.addPoint(x + pad, y + pad);
                p.addPoint(x + size - pad, y + pad);
                p.addPoint(x + size / 2 + 2, y + size / 2 + 1);
                p.addPoint(x + size / 2 + 2, y + size - pad);
                p.addPoint(x + size / 2 - 2, y + size - pad - 2);
                p.addPoint(x + size / 2 - 2, y + size / 2 + 1);
                g2.drawPolygon(p);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Icon chevronRightIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 6, y + 4, x + size - 5, y + size / 2);
                g2.drawLine(x + size - 5, y + size / 2, x + 6, y + size - 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Icon statusDotIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(x, y, size, size);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Icon trashIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + 5, x + size - 4, y + 5);
                g2.drawLine(x + 6, y + 7, x + 7, y + size - 3);
                g2.drawLine(x + size - 6, y + 7, x + size - 7, y + size - 3);
                g2.drawLine(x + 7, y + size - 3, x + size - 7, y + size - 3);
                g2.drawLine(x + size / 2 - 3, y + 3, x + size / 2 + 3, y + 3);
                g2.drawLine(x + size / 2 - 3, y + 3, x + size / 2 - 4, y + 5);
                g2.drawLine(x + size / 2 + 3, y + 3, x + size / 2 + 4, y + 5);
                g2.drawLine(x + 9, y + 9, x + 9, y + size - 5);
                g2.drawLine(x + size - 9, y + 9, x + size - 9, y + size - 5);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }
}
