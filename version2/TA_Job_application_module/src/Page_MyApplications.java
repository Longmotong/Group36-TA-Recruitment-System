

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;



/**
 * 我的申请页面（设计稿：统计卡片 + 搜索筛选 + 状态徽章表格）
 */
public class Page_MyApplications {

    public interface MyApplicationsCallback {
        void onViewStatus(Application application);
        void onBackToHome();
        void onBrowseJobs();
        void onCancelApplication(Application application);
    }

    private static final String PLACEHOLDER_SEARCH = "Search by job title...";
    private static final int JOB_TITLE_COL_MIN_WIDTH = 200;
    /** 与列宽匹配的 HTML 换行最小宽度（列变宽时会用更大值） */
    private static final int JOB_TITLE_WRAP_INNER_PX = 248;
    /** 第 2–7 列固定宽度（STATUS 列 = BADGE_OUTER_WIDTH+24，见 StatusBadgeRenderer） */
    private static final int[] REST_COL_WIDTHS = {96, 200, 140, 192, 140, 88, 100};

    private JPanel panel;
    private JPanel northStack;
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JLabel totalValueLabel;
    private JLabel pendingValueLabel;
    private JLabel underReviewValueLabel;
    private JLabel acceptedValueLabel;
    private JLabel rejectedValueLabel;

    private final DataService dataService;
    private final MyApplicationsCallback callback;
    /** 与表格行顺序一致，用于点击 View */
    private final List<Application> displayedApplications = new ArrayList<>();

    public Page_MyApplications(DataService dataService, MyApplicationsCallback callback) {
        this.dataService = dataService;
        this.callback = callback;
        initPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void refreshTable() {
        updateSummaryCards();
        applyFiltersAndFillTable();
    }

    private void initPanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(24, 40, 32, 40));

        northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);

        buildBackLink();
        buildTitleRow();
        buildSummaryCards();
        buildSearchFilterBar();

        panel.add(northStack, BorderLayout.NORTH);
        buildTable();
    }

    private void buildBackLink() {
        JButton back = new JButton("\u2190  Back to Home");
        back.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        back.setForeground(UI_Constants.TEXT_SECONDARY);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.setHorizontalAlignment(SwingConstants.LEFT);
        back.addActionListener(e -> callback.onBackToHome());
        northStack.add(back);
        northStack.add(Box.createVerticalStrut(16));
    }

    private void buildTitleRow() {
        JPanel row = new JPanel(new BorderLayout(24, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Track all your TA position applications");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(subtitleLabel);

        row.add(left, BorderLayout.WEST);

        JButton browseBtn = UI_Helper.createOutlineButton("Browse Jobs");
        browseBtn.addActionListener(e -> callback.onBrowseJobs());
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        east.setOpaque(false);
        east.add(browseBtn);
        row.add(east, BorderLayout.EAST);

        northStack.add(row);
        northStack.add(Box.createVerticalStrut(24));
    }

    private void buildSummaryCards() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 5, 14, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalValueLabel = new JLabel("0");
        pendingValueLabel = new JLabel("0");
        underReviewValueLabel = new JLabel("0");
        acceptedValueLabel = new JLabel("0");
        rejectedValueLabel = new JLabel("0");

        summaryPanel.add(createStatCard("Total Applications", totalValueLabel, UI_Constants.TEXT_PRIMARY));
        summaryPanel.add(createStatCard("Pending", pendingValueLabel, UI_Constants.WARNING_COLOR));
        summaryPanel.add(createStatCard("Under Review", underReviewValueLabel, UI_Constants.INFO_COLOR));
        summaryPanel.add(createStatCard("Accepted", acceptedValueLabel, UI_Constants.SUCCESS_COLOR));
        summaryPanel.add(createStatCard("Rejected", rejectedValueLabel, UI_Constants.DANGER_COLOR));

        northStack.add(summaryPanel);
        northStack.add(Box.createVerticalStrut(20));
        updateSummaryCards();
    }

    private JPanel createStatCard(String label, JLabel valueLabel, Color valueColor) {
        JPanel card = UI_Helper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UI_Constants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel top = new JLabel(label);
        top.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        top.setForeground(UI_Constants.TEXT_SECONDARY);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(top);
        card.add(Box.createVerticalStrut(8));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valueLabel);

        return card;
    }

    private void updateSummaryCards() {
        int pending = dataService.countApplicationsByStatus("pending");
        int under = dataService.countApplicationsByStatus("under_review");
        int accepted = dataService.countApplicationsByStatus("accepted");
        int rejected = dataService.countApplicationsByStatus("rejected");
        int total = pending + under + accepted + rejected;

        totalValueLabel.setText(String.valueOf(total));
        pendingValueLabel.setText(String.valueOf(pending));
        underReviewValueLabel.setText(String.valueOf(under));
        acceptedValueLabel.setText(String.valueOf(accepted));
        rejectedValueLabel.setText(String.valueOf(rejected));
    }

    private void buildSearchFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(10, 14, 10, 14)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (tableModel != null) applyFiltersAndFillTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (tableModel != null) applyFiltersAndFillTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (tableModel != null) applyFiltersAndFillTable();
            }
        });
        installSearchPlaceholder();

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        bar.add(searchWrap, BorderLayout.CENTER);
        searchWrap.add(searchField, BorderLayout.CENTER);

        String[] statuses = {"All Statuses", "Pending", "Under Review", "Accepted", "Rejected"};
        statusFilterCombo = new JComboBox<>(statuses);
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilterCombo.setBorder(BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR));
        statusFilterCombo.addActionListener(e -> applyFiltersAndFillTable());
        Dimension comboSize = statusFilterCombo.getPreferredSize();
        statusFilterCombo.setPreferredSize(new Dimension(Math.max(160, comboSize.width), 40));
        bar.add(statusFilterCombo, BorderLayout.EAST);

        northStack.add(bar);
        northStack.add(Box.createVerticalStrut(16));
    }

    private void installSearchPlaceholder() {
        searchField.setForeground(UI_Constants.TEXT_SECONDARY);
        searchField.setText(PLACEHOLDER_SEARCH);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (PLACEHOLDER_SEARCH.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(UI_Constants.TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(UI_Constants.TEXT_SECONDARY);
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
        Object sel = statusFilterCombo.getSelectedItem();
        if (sel == null) {
            return null;
        }
        return switch (sel.toString()) {
            case "Pending" -> "pending";
            case "Under Review" -> "under_review";
            case "Accepted" -> "accepted";
            case "Rejected" -> "rejected";
            default -> null;
        };
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
        applicationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        applicationsTable.setRowHeight(52);
        applicationsTable.setGridColor(new Color(243, 244, 246));
        applicationsTable.setShowGrid(false);
        applicationsTable.setIntercellSpacing(new Dimension(0, 1));
        applicationsTable.setSelectionBackground(new Color(238, 242, 255));
        applicationsTable.setSelectionForeground(UI_Constants.TEXT_PRIMARY);
        // 固定第 2–7 列；JOB TITLE 列随视口变宽填满，避免右侧大块空白
        applicationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = applicationsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(UI_Constants.TEXT_SECONDARY);
        header.setBackground(new Color(249, 250, 251));
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UI_Constants.BORDER_COLOR));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(JLabel.LEFT);
                c.setBorder(new EmptyBorder(0, 12, 0, 12));
                c.setBackground(new Color(249, 250, 251));
                c.setForeground(UI_Constants.TEXT_SECONDARY);
                return c;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(new EmptyBorder(0, 12, 0, 12));

        DefaultTableCellRenderer titleRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(JLabel.LEFT);
                c.setVerticalAlignment(JLabel.TOP);
                c.setBorder(new EmptyBorder(10, 12, 10, 12));
                String full = value != null ? value.toString() : "";
                c.setToolTipText(full.isEmpty() ? null : full);
                // 长标题按当前列宽换行，避免省略号
                if (full.length() > 42) {
                    int colW = table.getColumnModel().getColumn(column).getWidth();
                    int wrap = Math.max(JOB_TITLE_WRAP_INNER_PX, colW - 28);
                    c.setText("<html><div style='width:" + wrap + "px'>" + escapeHtmlPlain(full) + "</div></html>");
                } else {
                    c.setText(full);
                }
                return c;
            }
        };

        for (int i = 0; i < columns.length; i++) {
            TableColumn col = applicationsTable.getColumnModel().getColumn(i);
            if (i == 0) {
                col.setCellRenderer(titleRenderer);
            } else if (i == 4) {
                col.setCellRenderer(new StatusBadgeRenderer());
            } else if (i == 6) {
                col.setCellRenderer(new ViewLinkRenderer());
            } else if (i == 7) {
                col.setCellRenderer(new CancelButtonRenderer());
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
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        callback.onCancelApplication(app);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getViewport().setBackground(UI_Constants.CARD_BG);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncJobTitleColumnToFillViewport();
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        applyFiltersAndFillTable();
        SwingUtilities.invokeLater(this::syncJobTitleColumnToFillViewport);
    }

    /** 第 2–8 列固定宽度；JOB TITLE 列用剩余宽度铺满视口（过窄时出现横向滚动条） */
    private void applyColumnWidths() {
        TableColumnModel cm = applicationsTable.getColumnModel();
        if (cm.getColumnCount() < 8) {
            return;
        }
        cm.getColumn(0).setMinWidth(JOB_TITLE_COL_MIN_WIDTH);
        for (int i = 0; i < REST_COL_WIDTHS.length; i++) {
            TableColumn c = cm.getColumn(i + 1);
            c.setPreferredWidth(REST_COL_WIDTHS[i]);
            c.setMinWidth(i == 3 ? REST_COL_WIDTHS[i] : Math.max(48, REST_COL_WIDTHS[i] / 2));
        }
        cm.getColumn(4).setMinWidth(REST_COL_WIDTHS[3]);
    }

    /** 让表格总宽度等于视口宽度，消除右侧空白 */
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

        int fixed = 0;
        for (int w : REST_COL_WIDTHS) {
            fixed += w;
        }
        int spacing = applicationsTable.getIntercellSpacing().width * 6;
        int fudge = 8;
        int titleW = vw - fixed - spacing - fudge;
        titleW = Math.max(titleW, JOB_TITLE_COL_MIN_WIDTH);

        TableColumn c0 = cm.getColumn(0);
        c0.setPreferredWidth(titleW);
        c0.setWidth(titleW);

        for (int i = 0; i < REST_COL_WIDTHS.length; i++) {
            TableColumn c = cm.getColumn(i + 1);
            int w = REST_COL_WIDTHS[i];
            c.setPreferredWidth(w);
            c.setWidth(w);
        }

        applicationsTable.revalidate();
        applicationsTable.repaint();
    }

    private void applyFiltersAndFillTable() {
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
                submitted = formatDateOnly(app.getMeta().getSubmittedAt(), outDate);
            }
            String lastUp = "";
            if (app.getStatus() != null && app.getStatus().getLastUpdated() != null) {
                lastUp = formatDateOnly(app.getStatus().getLastUpdated(), outDate);
            }

            boolean canCancel = "pending".equalsIgnoreCase(
                app.getStatus() != null ? app.getStatus().getCurrent() : "");
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
            applicationsTable.setRowHeight(row, Math.max(52, h + 8));
        }
    }

    private static String escapeHtmlPlain(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String formatDateOnly(String iso, DateTimeFormatter outDate) {
        if (iso == null || iso.length() < 10) {
            return "";
        }
        try {
            return LocalDate.parse(iso.substring(0, 10)).format(outDate);
        } catch (DateTimeParseException e) {
            return iso.substring(0, 10);
        }
    }

    // ==================== Renderers ====================

    static class StatusBadgeRenderer implements TableCellRenderer {
        /** 状态列里色块统一宽度（与列宽计算一致） */
        static final int BADGE_OUTER_WIDTH = 168;
        private static final int BADGE_H = 30;

        private final JPanel wrap = new JPanel(new BorderLayout());
        private final JLabel inner = new JLabel("", SwingConstants.CENTER);

        StatusBadgeRenderer() {
            wrap.setOpaque(true);
            wrap.setBorder(new EmptyBorder(0, 12, 0, 8));
            inner.setFont(new Font("Segoe UI", Font.BOLD, 12));
            inner.setOpaque(true);
            inner.setBorder(new EmptyBorder(6, 8, 6, 8));
            inner.setPreferredSize(new Dimension(BADGE_OUTER_WIDTH, BADGE_H));
            inner.setMinimumSize(new Dimension(BADGE_OUTER_WIDTH, BADGE_H));
            inner.setMaximumSize(new Dimension(BADGE_OUTER_WIDTH, BADGE_H));
            wrap.add(inner, BorderLayout.WEST);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String label = value != null ? value.toString() : "";
            String key = label.toLowerCase(Locale.ROOT);
            Color bg;
            Color fg;
            if (key.contains("pending")) {
                bg = new Color(254, 243, 199);
                fg = new Color(180, 83, 9);
            } else if (key.contains("under review")) {
                bg = new Color(219, 234, 254);
                fg = new Color(29, 78, 216);
            } else if (key.contains("accepted")) {
                bg = new Color(209, 250, 229);
                fg = new Color(5, 122, 85);
            } else if (key.contains("rejected")) {
                bg = new Color(254, 226, 226);
                fg = new Color(185, 28, 28);
            } else {
                bg = UI_Constants.BG_COLOR;
                fg = UI_Constants.TEXT_SECONDARY;
            }
            inner.setText(label);
            inner.setBackground(bg);
            inner.setForeground(fg);
            inner.setToolTipText(label.isEmpty() ? null : label);
            wrap.setBackground(isSelected ? table.getSelectionBackground() : UI_Constants.CARD_BG);
            return wrap;
        }
    }

    static class CancelButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton btn = new JButton("Withdraw");

        CancelButtonRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(true);
            setBackground(UI_Constants.CARD_BG);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(UI_Constants.DANGER_COLOR);
            btn.setOpaque(true);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setBorder(new EmptyBorder(4, 10, 4, 10));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setVisible(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(UI_Constants.CARD_BG);
            }
            boolean show = "withdraw".equals(value);
            btn.setVisible(show);
            removeAll();
            if (show) {
                add(Box.createHorizontalGlue());
                add(btn);
                add(Box.createHorizontalGlue());
            }
            return this;
        }
    }


    static class ViewLinkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setText("<html><font color='#4F46E5'><u>View</u></font></html>");
            c.setHorizontalAlignment(SwingConstants.LEFT);
            c.setBorder(new EmptyBorder(0, 12, 0, 12));
            c.setIcon(null);
            c.setOpaque(true);
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
            } else {
                c.setBackground(UI_Constants.CARD_BG);
            }
            return c;
        }
    }
}
