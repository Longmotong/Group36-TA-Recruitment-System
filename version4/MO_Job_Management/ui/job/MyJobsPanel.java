package MO_system.ui.job;

import com.formdev.flatlaf.FlatClientProperties;
import MO_system.MoContext;
import MO_system.model.job.Job;
import MO_system.model.job.JobStatusUtil;
import MO_system.repository.ApplicationRepository;
import MO_system.repository.JobRepository;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import MO_system.ui.MoShellHost;
import MO_system.ui.MoUiTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.plaf.BorderUIResource;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MyJobsPanel extends JPanel {
    /** Softer than default LAF selection — avoids harsh blue + keeps text readable. */
    private static final Color TABLE_SELECTION_BG = JobsPortalUi.LAVENDER;
    private static final Color TABLE_SELECTION_FG = JobsPortalUi.DARK_TEXT;
    private static final Color TABLE_HEADER_BG = new Color(0xF3EEFF);
    private static final Color TABLE_BORDER = new Color(0xE6DBFF);
    private static final Color TABLE_ROW_ALT = new Color(0xFCFAFF);
    /** Minimum row height for the jobs table (content breathing room). */
    private static final int TABLE_ROW_MIN_HEIGHT = 64;
    private static final int TABLE_ROW_HEIGHT_PAD = 6;

    private final MoShellHost host;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository = new ApplicationRepository();
    private final JobTableModel tableModel;
    private final List<Job> allJobs = new ArrayList<>();
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel filterCountLabel;
    private JLabel totalJobsValueLabel;
    private JLabel openJobsValueLabel;
    private JLabel closedJobsValueLabel;

    /**
     * Table renderers do not receive real mouse events; we track hover on the Actions column (5)
     * and paint the matching slot with the hover chrome.
     */
    private int actionsHoverRow = -1;
    private int actionsHoverSlot = -1;

    public MyJobsPanel(MoShellHost host) {
        this.host = host;
        this.jobRepository = new JobRepository();
        this.tableModel = new JobTableModel(new ArrayList<>());

        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        add(buildMainContent(), BorderLayout.CENTER);

        // After first layout/paint (empty table), load from disk on a worker thread.
        SwingUtilities.invokeLater(this::loadJobsFromRepositoryAsync);
    }

    public void reloadJobsFromRepository() {
        loadJobsFromRepositoryAsync();
    }

    private void loadJobsFromRepositoryAsync() {
        new SwingWorker<List<Job>, Void>() {
            @Override
            protected List<Job> doInBackground() {
                return jobRepository.loadJobsForMo(MoContext.getCurrentMoUserId());
            }

            @Override
            protected void done() {
                try {
                    allJobs.clear();
                    allJobs.addAll(get());
                    applyFilters();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(
                            host.getShellFrame(),
                            "Failed to load jobs: " + c.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JPanel buildMainContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(0xF8F8FF));
        panel.setBorder(new EmptyBorder(14, MoUiTheme.PAGE_INSET_X, 14, MoUiTheme.PAGE_INSET_X));

        JButton backHomeBtn = MoUiTheme.createBackToHomeButton(() -> host.showDashboard());
        Dimension backPref = backHomeBtn.getPreferredSize();
        backHomeBtn.setPreferredSize(new Dimension(backPref.width, 42));
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backHomeBtn);

        JButton createNewJobButton = MoUiTheme.portalGradientPrimary("+  Create New Job",
                new Font(Font.SANS_SERIF, Font.BOLD, 13));
        Dimension createPref = createNewJobButton.getPreferredSize();
        createNewJobButton.setPreferredSize(new Dimension(Math.max(220, createPref.width), Math.max(42, createPref.height)));
        createNewJobButton.addActionListener(e -> host.showCreateJob());

        JPanel headerRow = new JPanel(new BorderLayout(16, 0));
        headerRow.setOpaque(false);
        headerRow.add(buildPageHeroCard(
                "My Jobs",
                "Manage your TA recruitment positions"), BorderLayout.CENTER);
        headerRow.add(createNewJobButton, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout(0, 6));
        north.setOpaque(false);
        north.add(backRow, BorderLayout.NORTH);
        north.add(headerRow, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        panel.add(north, c);

        c.gridy = 1;
        c.weighty = 0;
        c.insets = new Insets(6, 0, 6, 0);
        panel.add(buildKpiRow(), c);

        c.gridy = 2;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 5, 0);
        panel.add(buildFilterBar(), c);

        c.gridy = 3;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(buildJobsTableSection(), c);
        return panel;
    }

    private JPanel buildJobsTableSection() {
        JScrollPane scroll = buildJobsTableScrollPane();
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.add(JobsPortalUi.wrapRoundedInner(
                scroll, 16, Color.WHITE, TABLE_BORDER, 1f, true, null), BorderLayout.CENTER);
        return section;
    }

    /** Same header hero as TA Applications list ({@link MO_system.ui.review.MoApplicationReviewPanel}). */
    private JPanel buildPageHeroCard(String title, String subtitle) {
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setOpaque(true);
        hero.setBackground(new Color(0xFCFBFF));
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDED4FF)),
                new EmptyBorder(12, 14, 14, 14)
        ));
        hero.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        titleLabel.setForeground(new Color(0x111827));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = MoUiTheme.createPageSubtitle(subtitle);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        hero.add(titleLabel);
        hero.add(Box.createVerticalStrut(3));
        hero.add(subLabel);
        return hero;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        totalJobsValueLabel = new JLabel("0", SwingConstants.CENTER);
        openJobsValueLabel = new JLabel("0", SwingConstants.CENTER);
        closedJobsValueLabel = new JLabel("0", SwingConstants.CENTER);

        row.add(buildKpiCard("Total Jobs", totalJobsValueLabel,
                MoUiTheme.KPI_TOTAL_BG, MoUiTheme.KPI_TOTAL_BORDER, MoUiTheme.KPI_TOTAL_FG));
        row.add(buildKpiCard("Open Jobs", openJobsValueLabel,
                MoUiTheme.KPI_OPEN_BG, MoUiTheme.KPI_OPEN_BORDER, MoUiTheme.KPI_OPEN_FG));
        row.add(buildKpiCard("Closed Jobs", closedJobsValueLabel,
                MoUiTheme.KPI_CLOSED_BG, MoUiTheme.KPI_CLOSED_BORDER, MoUiTheme.KPI_CLOSED_FG));
        return row;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, Color bg, Color border, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(10, 8, 10, 8)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JLabel top = new JLabel(title.toUpperCase(Locale.ROOT), SwingConstants.CENTER);
        top.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        top.setForeground(new Color(0x475569));

        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        valueLabel.setForeground(valueColor);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 4));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));

        filterCountLabel = new JLabel("Showing 0 of 0");
        filterCountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        filterCountLabel.setForeground(MoUiTheme.TEXT_SECONDARY);
        filterCountLabel.setHorizontalAlignment(SwingConstants.LEFT);

        searchField = new JTextField();
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search Job ID / Module / Title");
        searchField.setPreferredSize(new Dimension(0, 36));
        searchField.setMinimumSize(new Dimension(180, 36));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
        });

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "Open", "Closed", "Draft"});
        statusFilter.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statusFilter.setPreferredSize(new Dimension(150, 36));
        statusFilter.setMinimumSize(new Dimension(130, 36));
        statusFilter.addActionListener(e -> applyFilters());

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        resetButton.setFocusPainted(false);
        resetButton.setPreferredSize(new Dimension(82, 36));
        MoUiTheme.styleOutlineButton(resetButton, 8);
        resetButton.addActionListener(e -> {
            if (searchField != null) {
                searchField.setText("");
            }
            if (statusFilter != null) {
                statusFilter.setSelectedIndex(0);
            }
            applyFilters();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(statusFilter);
        right.add(resetButton);

        JPanel controlsRow = new JPanel(new BorderLayout(8, 0));
        controlsRow.setOpaque(false);
        controlsRow.add(searchField, BorderLayout.CENTER);
        controlsRow.add(right, BorderLayout.EAST);

        JPanel controlsShell = new JPanel(new BorderLayout());
        controlsShell.setOpaque(true);
        controlsShell.setBackground(new Color(0xFDFCFF));
        controlsShell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TABLE_BORDER),
                new EmptyBorder(5, 8, 5, 8)
        ));
        controlsShell.add(controlsRow, BorderLayout.CENTER);

        JPanel filterTop = new JPanel(new BorderLayout(10, 0));
        filterTop.setOpaque(false);
        filterTop.add(filterCountLabel, BorderLayout.WEST);
        filterTop.add(controlsShell, BorderLayout.CENTER);
        bar.add(filterTop, BorderLayout.CENTER);
        return bar;
    }

    private JScrollPane buildJobsTableScrollPane() {
        // Avoid thick focus rectangle on the focused cell (esp. numeric columns).
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder()));

        JTable table = new JTable(tableModel);
        table.setRowHeight(TABLE_ROW_MIN_HEIGHT);
        // No horizontal scrolling: fit columns into viewport and wrap long text.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TABLE_SELECTION_FG);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(TABLE_BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(MoUiTheme.TEXT_PRIMARY);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(new Color(0x4F35D9));
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_BORDER));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, false, row, col);
                if (c instanceof JLabel l) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setForeground(new Color(0x4F35D9));
                    l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                    l.setOpaque(true);
                    l.setBackground(TABLE_HEADER_BG);
                    l.setBorder(new EmptyBorder(7, 6, 7, 6));
                    l.setText(value == null ? "" : String.valueOf(value));
                }
                return c;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);

        // Ensure default renderers (columns without custom renderers) paint selection background fully.
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, false, row, column);
                c.setBackground(isSelected ? t.getSelectionBackground() : rowBackground(row));
                c.setForeground(isSelected ? TABLE_SELECTION_FG : t.getForeground());
                if (c instanceof javax.swing.JLabel lbl) {
                    lbl.setOpaque(true);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setCellRenderer(new JobTitleCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new ModuleCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new CenteredTextCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new CenteredTextCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionsCellEditor());

        table.getColumnModel().getColumn(0).setPreferredWidth(380);
        table.getColumnModel().getColumn(0).setMinWidth(280);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(1).setMinWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(108);
        table.getColumnModel().getColumn(2).setMinWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(96);
        table.getColumnModel().getColumn(3).setMinWidth(88);
        table.getColumnModel().getColumn(4).setPreferredWidth(132);
        table.getColumnModel().getColumn(4).setMinWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(280);
        table.getColumnModel().getColumn(5).setMinWidth(240);
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnMarginChanged(ChangeEvent e) {
                SwingUtilities.invokeLater(() -> adjustTableRowHeights(table));
            }

            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {
            }
        });

        int actionsCol = 5;
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateActionsColumnHover(table, e.getX(), e.getY(), actionsCol);
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                clearActionsColumnHover(table, actionsCol);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableModel.addTableModelListener(e -> SwingUtilities.invokeLater(() -> adjustTableRowHeights(table)));
        scrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> adjustTableRowHeights(table));
            }
        });
        SwingUtilities.invokeLater(() -> adjustTableRowHeights(table));
        return scrollPane;
    }
    /** Hours/Week & Your applicants — align with centered column headers. */
    private static class CenteredTextCellRenderer extends DefaultTableCellRenderer {
        CenteredTextCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(7, 6, 7, 6));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            c.setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            c.setForeground(isSelected ? TABLE_SELECTION_FG : table.getForeground());
            if (c instanceof JLabel lbl) {
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }
            return c;
        }
    }

    /** Job title — soft teal rounded pill; black text on {@link MoUiTheme#ACCENT_TEAL_SOFT_BG}. */
    private static class JobTitleCellRenderer extends JPanel implements TableCellRenderer {
        private final TealRoundedPanel pill = new TealRoundedPanel();
        private final JLabel titleLabel = new JLabel();

        JobTitleCellRenderer() {
            super(new GridBagLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(8, 12, 8, 8));
            pill.setLayout(new BorderLayout());
            pill.setBorder(new EmptyBorder(7, 10, 7, 10));
            titleLabel.setOpaque(false);
            titleLabel.setForeground(MoUiTheme.TEXT_PRIMARY);
            titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
            titleLabel.setVerticalAlignment(SwingConstants.CENTER);
            pill.add(titleLabel, BorderLayout.CENTER);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            add(pill, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            String title = value == null ? "" : String.valueOf(value);
            titleLabel.setToolTipText(title);
            int colW = table.getColumnModel().getColumn(column).getWidth();
            if (colW <= 0) {
                colW = table.getColumnModel().getColumn(column).getPreferredWidth();
            }
            int contentW = Math.max(100, colW - 44);
            String textColor = isSelected ? "#111827" : "#000000";
            titleLabel.setFont(table.getFont());
            titleLabel.setText("<html><div style='width:" + contentW + "px;line-height:1.35'><span style='font-weight:700;color:"
                    + textColor + "'>"
                    + escapeHtml(title)
                    + "</span></div></html>");
            setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            return this;
        }

        private static String escapeHtml(String s) {
            if (s == null) {
                return "";
            }
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }
    }

    /** Paints a capsule-shaped teal tint (reliable round corners with HTML labels inside). */
    private static final class TealRoundedPanel extends JPanel {
        TealRoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MoUiTheme.ACCENT_TEAL_SOFT_BG);
                int w = getWidth();
                int h = getHeight();
                int arc = Math.max(12, h);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private void persistJobs() {
        jobRepository.saveJobsForMo(MoContext.getCurrentMoUserId(), allJobs);
    }

    private void applyFilters() {
        String keyword = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String status = statusFilter == null ? "All Statuses" : String.valueOf(statusFilter.getSelectedItem());
        List<Job> filtered = new ArrayList<>();
        int openCount = 0;
        int closedCount = 0;
        for (Job job : allJobs) {
            if (job == null) {
                continue;
            }
            String canonical = JobStatusUtil.canonical(job.getStatus());
            if ("open".equals(canonical)) {
                openCount++;
            } else if ("closed".equals(canonical)) {
                closedCount++;
            }
            boolean statusMatch = "All Statuses".equals(status)
                    || ("Open".equals(status) && "open".equals(canonical))
                    || ("Closed".equals(status) && "closed".equals(canonical))
                    || ("Draft".equals(status) && "draft".equals(canonical));
            if (!statusMatch) {
                continue;
            }
            String title = job.getTitle() == null ? "" : job.getTitle().toLowerCase();
            String id = job.getId() == null ? "" : job.getId().toLowerCase();
            String moduleCode = job.getModuleCode() == null ? "" : job.getModuleCode().toLowerCase();
            String moduleName = job.getModuleName() == null ? "" : job.getModuleName().toLowerCase();
            boolean keywordMatch = keyword.isBlank()
                    || title.contains(keyword)
                    || id.contains(keyword)
                    || moduleCode.contains(keyword)
                    || moduleName.contains(keyword);
            if (keywordMatch) {
                filtered.add(job);
            }
        }
        tableModel.replaceJobs(filtered);
        if (totalJobsValueLabel != null) {
            totalJobsValueLabel.setText(String.valueOf(allJobs.size()));
        }
        if (openJobsValueLabel != null) {
            openJobsValueLabel.setText(String.valueOf(openCount));
        }
        if (closedJobsValueLabel != null) {
            closedJobsValueLabel.setText(String.valueOf(closedCount));
        }
        if (filterCountLabel != null) {
            filterCountLabel.setText("Showing " + filtered.size() + " of " + allJobs.size());
        }
    }

    private void refreshActionIconsForRow(int row, JButton editButton, JButton toggleButton, JButton deleteButton) {
        if (row < 0 || row >= tableModel.getRowCount()) {
            return;
        }
        Job job = tableModel.getJobAt(row);
        editButton.setIcon(JobActionIcons.editJobBlue());
        deleteButton.setIcon(JobActionIcons.deleteJobRed());
        boolean open = job != null && JobStatusUtil.isOpen(job.getStatus());
        toggleButton.setIcon(JobActionIcons.toggleSwitch(open));
        toggleButton.setText(open ? "Open" : "Close");
    }

    /**
     * JTable cell renderers are not interactive: JButton rollover never fires. We derive hover from
     * mouse position over column {@code actionsCol} and repaint that cell.
     */
    private void updateActionsColumnHover(JTable table, int x, int y, int actionsCol) {
        int row = table.rowAtPoint(new Point(x, y));
        int col = table.columnAtPoint(new Point(x, y));
        int prevRow = actionsHoverRow;
        int prevSlot = actionsHoverSlot;
        int newRow = -1;
        int newSlot = -1;
        if (row >= 0 && col == actionsCol) {
            Rectangle cell = table.getCellRect(row, actionsCol, false);
            int relX = x - cell.x;
            int w = Math.max(1, cell.width);
            if (relX >= 0 && relX < w) {
                newRow = row;
                newSlot = (int) ((relX * 5L) / w);
                if (newSlot > 4) {
                    newSlot = 4;
                }
            }
        }
        if (newRow != prevRow || newSlot != prevSlot) {
            actionsHoverRow = newRow;
            actionsHoverSlot = newSlot;
            Rectangle dirty = null;
            if (prevRow >= 0) {
                dirty = table.getCellRect(prevRow, actionsCol, false);
            }
            if (newRow >= 0) {
                Rectangle r2 = table.getCellRect(newRow, actionsCol, false);
                dirty = dirty == null ? r2 : dirty.union(r2);
            }
            if (dirty != null) {
                table.repaint(dirty);
            }
        }
    }

    private void clearActionsColumnHover(JTable table, int actionsCol) {
        if (actionsHoverRow < 0) {
            return;
        }
        Rectangle r = table.getCellRect(actionsHoverRow, actionsCol, false);
        actionsHoverRow = -1;
        actionsHoverSlot = -1;
        table.repaint(r);
    }

    private void paintRendererActionSlot(JButton b, int row, int slot) {
        boolean hover = row >= 0 && row == actionsHoverRow && slot == actionsHoverSlot;
        if (hover) {
            // Subtle transparent hover (no hard square frame).
            b.setBackground(new Color(JobsPortalUi.PRIMARY_PURPLE.getRed(),
                    JobsPortalUi.PRIMARY_PURPLE.getGreen(),
                    JobsPortalUi.PRIMARY_PURPLE.getBlue(),
                    40));
            b.setBorder(new EmptyBorder(1, 1, 1, 1));
        } else {
            b.setBackground(new Color(0, 0, 0, 0));
            b.setBorder(new EmptyBorder(1, 1, 1, 1));
        }
    }

    private class JobTableModel extends AbstractTableModel {
        private final String[] columns = {"Job Title", "Module", "Hours/Week", "Status", "Your applicants", "Actions"};
        private final List<Job> jobs;

        private JobTableModel(List<Job> jobs) {
            this.jobs = (jobs == null) ? new ArrayList<>() : jobs;
        }

        private void replaceJobs(List<Job> next) {
            jobs.clear();
            if (next != null) {
                jobs.addAll(next);
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return jobs.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 5;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Job job = jobs.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> job.getTitle();
                case 1 -> job.getModuleCode() + "|" + job.getModuleName();
                case 2 -> job.getWeeklyHours() + "h";
                case 3 -> JobStatusUtil.display(job.getStatus());
                case 4 -> applicationRepository.countApplicationsForJob(job.getId(), MoContext.getCurrentMoUserId());
                case 5 -> "";
                default -> "";
            };
        }

        public Job getJobAt(int rowIndex) {
            return jobs.get(rowIndex);
        }

    }

    private static class ModuleCellRenderer extends JLabel implements TableCellRenderer {
        ModuleCellRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(8, 12, 8, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            String raw = String.valueOf(value);
            String[] parts = raw.split("\\|", 2);
            String moduleCode = parts.length > 0 ? parts[0] : "";
            String moduleName = parts.length > 1 ? parts[1] : "";

            String codeColor = isSelected ? "#111827" : "#000000";
            String nameColor = isSelected ? "#4B5563" : "#666666";
            int colW = table.getColumnModel().getColumn(column).getWidth();
            if (colW <= 0) {
                colW = table.getColumnModel().getColumn(column).getPreferredWidth();
            }
            int contentW = Math.max(80, colW - 24);
            setText("<html><div style='width:" + contentW + "px;line-height:1.35'><span style='font-weight:700;color:" + codeColor + "'>"
                    + escapeHtml(moduleCode)
                    + "</span><br/><span style='color:" + nameColor + "'>"
                    + escapeHtml(moduleName)
                    + "</span></div></html>");
            setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            return this;
        }

        private static String escapeHtml(String s) {
            if (s == null) {
                return "";
            }
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }
    }

    /**
     * Compact centered pill — does not stretch to full row height; cell uses soft selection tint.
     */
    private static class StatusCellRenderer extends JPanel implements TableCellRenderer {
        private static final Color OPEN_BG = new Color(0xDCFCE7);
        private static final Color OPEN_FG = new Color(0x166534);
        private static final Color CLOSED_BG = new Color(0xE0E7FF);
        private static final Color CLOSED_FG = new Color(0x3730A3);
        private static final Color DRAFT_BG = new Color(0xFFFBEB);
        private static final Color DRAFT_FG = new Color(0x92400E);

        private final JLabel pill = new JLabel();

        StatusCellRenderer() {
            super(new GridBagLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(7, 4, 7, 4));
            pill.setOpaque(true);
            pill.setHorizontalAlignment(SwingConstants.CENTER);
            pill.setVerticalAlignment(SwingConstants.CENTER);
            pill.setBorder(new EmptyBorder(3, 10, 3, 10));
            pill.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            add(pill, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            String status = String.valueOf(value);
            pill.setText(status);
            pill.putClientProperty(FlatClientProperties.STYLE, "arc: 999");

            if ("Closed".equalsIgnoreCase(status)) {
                pill.setBackground(CLOSED_BG);
                pill.setForeground(CLOSED_FG);
            } else if ("Draft".equalsIgnoreCase(status)) {
                pill.setBackground(DRAFT_BG);
                pill.setForeground(DRAFT_FG);
            } else {
                pill.setBackground(OPEN_BG);
                pill.setForeground(OPEN_FG);
            }

            setBackground(isSelected ? table.getSelectionBackground() : rowBackground(row));
            return this;
        }
    }

    private class ActionsCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewButton = createSpecIconButton(JobActionIcons.viewJob(), "View", true);
        private final JButton taButton = createSpecIconButton(JobActionIcons.allocatedTas(), "TA", true);
        private final JButton editButton = createSpecIconButton(JobActionIcons.editJobBlue(), "Edit", true);
        private final JButton toggleButton = createSpecIconButton(JobActionIcons.toggleSwitch(false), "Open", true);
        private final JButton deleteButton = createSpecIconButton(JobActionIcons.deleteJobRed(), "Del", true);

        ActionsCellRenderer() {
            setLayout(new GridLayout(1, 5, 4, 0));
            setOpaque(true);
            setBorder(new EmptyBorder(6, 4, 6, 4));
            add(viewButton);
            add(taButton);
            add(editButton);
            add(toggleButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            if (!isSelected) {
                setBackground(rowBackground(row));
            }
            refreshActionIconsForRow(row, editButton, toggleButton, deleteButton);
            paintRendererActionSlot(viewButton, row, 0);
            paintRendererActionSlot(taButton, row, 1);
            paintRendererActionSlot(editButton, row, 2);
            paintRendererActionSlot(toggleButton, row, 3);
            paintRendererActionSlot(deleteButton, row, 4);
            return this;
        }
    }

    private class ActionsCellEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new GridLayout(1, 5, 4, 0));
        private final JButton viewButton = createSpecIconButton(JobActionIcons.viewJob(), "View", false);
        private final JButton taButton = createSpecIconButton(JobActionIcons.allocatedTas(), "TA", false);
        private final JButton editButton = createSpecIconButton(JobActionIcons.editJobBlue(), "Edit", false);
        private final JButton toggleButton = createSpecIconButton(JobActionIcons.toggleSwitch(false), "Open", false);
        private final JButton deleteButton = createSpecIconButton(JobActionIcons.deleteJobRed(), "Del", false);

        private int editingRow = -1;

        ActionsCellEditor() {
            panel.setOpaque(true);
            panel.add(viewButton);
            panel.add(taButton);
            panel.add(editButton);
            panel.add(toggleButton);
            panel.add(deleteButton);

            viewButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    fireEditingStopped();
                    host.showJobDetail(job);
                } else {
                    fireEditingStopped();
                }
            });
            taButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    fireEditingStopped();
                    host.showTaAllocation(job);
                } else {
                    fireEditingStopped();
                }
            });
            editButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    fireEditingStopped();
                    host.showEditJob(job);
                } else {
                    fireEditingStopped();
                }
            });
            toggleButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    String nextStatus = JobStatusUtil.isOpen(job.getStatus()) ? "Closed" : "Open";
                    job.setStatus(nextStatus);
                    persistJobs();
                    applyFilters();
                }
                fireEditingStopped();
            });
            deleteButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    int choice = JOptionPane.showConfirmDialog(
                            host.getShellFrame(),
                            "Delete job: " + job.getTitle() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        allJobs.remove(job);
                        persistJobs();
                        applyFilters();
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            this.editingRow = row;
            panel.setBackground(table.getSelectionBackground());
            refreshActionIconsForRow(row, editButton, toggleButton, deleteButton);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    /**
     * @param forTableRenderer {@code true} for painted-only buttons (hover comes from table mouse tracking).
     */
    private JButton createSpecIconButton(Icon icon, String label, boolean forTableRenderer) {
        JButton button = new JButton(label, icon);
        button.setToolTipText(null);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorder(new EmptyBorder(1, 1, 1, 1));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setIconTextGap(1);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        button.setPreferredSize(new Dimension(56, 40));
        button.setFocusable(false);
        button.setOpaque(true);
        button.setBorderPainted(true);
        if (forTableRenderer) {
            button.setRolloverEnabled(false);
        } else {
            button.setRolloverEnabled(true);
            applyEditorActionButtonVisualState(button);
            button.getModel().addChangeListener(e -> applyEditorActionButtonVisualState(button));
        }
        return button;
    }

    /** Real JButton hover (only used in {@link ActionsCellEditor}). */
    private static void applyEditorActionButtonVisualState(JButton button) {
        ButtonModel model = button.getModel();
        int pr = JobsPortalUi.PRIMARY_PURPLE.getRed();
        int pg = JobsPortalUi.PRIMARY_PURPLE.getGreen();
        int pb = JobsPortalUi.PRIMARY_PURPLE.getBlue();
        if (model.isPressed() || model.isArmed()) {
            button.setBackground(new Color(pr, pg, pb, 55));
            button.setBorder(new EmptyBorder(1, 1, 1, 1));
        } else if (model.isRollover()) {
            button.setBackground(new Color(pr, pg, pb, 40));
            button.setBorder(new EmptyBorder(1, 1, 1, 1));
        } else {
            button.setBackground(new Color(0, 0, 0, 0));
            button.setBorder(new EmptyBorder(1, 1, 1, 1));
        }
    }

    private static void adjustTableRowHeights(JTable table) {
        int rows = table.getRowCount();
        int cols = table.getColumnCount();
        if (rows <= 0 || cols <= 0) {
            return;
        }
        for (int row = 0; row < rows; row++) {
            int target = TABLE_ROW_MIN_HEIGHT;
            for (int col = 0; col < cols; col++) {
                Component c = table.prepareRenderer(table.getCellRenderer(row, col), row, col);
                target = Math.max(target, c.getPreferredSize().height + TABLE_ROW_HEIGHT_PAD);
            }
            if (table.getRowHeight(row) != target) {
                table.setRowHeight(row, target);
            }
        }
    }

    private static Color rowBackground(int row) {
        return (row % 2 == 0) ? Color.WHITE : TABLE_ROW_ALT;
    }

}
