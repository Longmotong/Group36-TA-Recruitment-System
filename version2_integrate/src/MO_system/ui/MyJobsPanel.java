package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.MoContext;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.ButtonModel;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
    private static final Color TABLE_SELECTION_BG = new Color(0xD9E7FF);
    private static final Color TABLE_SELECTION_FG = new Color(0x111827);

    private final MoShellHost host;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository = new ApplicationRepository();
    private final JobTableModel tableModel;

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
                    tableModel.replaceJobs(get());
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
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(MoUiTheme.PAGE_INSET_TOP, MoUiTheme.PAGE_INSET_X, MoUiTheme.PAGE_INSET_BOTTOM, MoUiTheme.PAGE_INSET_X));

        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);

        JLabel title = new JLabel("My Jobs");
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        JLabel subtitle = new JLabel("Manage your TA recruitment positions");
        subtitle.setForeground(MoUiTheme.TEXT_SECONDARY);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(6));
        leftHeader.add(subtitle);

        JButton createNewJobButton = new JButton("+  Create New Job");
        createNewJobButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        createNewJobButton.setFocusPainted(false);
        MoUiTheme.stylePrimaryButton(createNewJobButton, 10);
        createNewJobButton.setPreferredSize(new Dimension(200, 42));
        createNewJobButton.addActionListener(e -> host.showCreateJob());

        JPanel headerCard = new JPanel(new BorderLayout(28, 0));
        headerCard.setOpaque(true);
        headerCard.setBackground(MoUiTheme.SURFACE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(18, 22, 18, 22)
        ));
        headerCard.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        headerCard.add(leftHeader, BorderLayout.CENTER);
        headerCard.add(createNewJobButton, BorderLayout.EAST);

        // Full-width row so BorderLayout does not center a narrow box (matches Application Review left edge).
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(MoUiTheme.createBackToHomeButton(() -> host.showDashboard()));

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(backRow, BorderLayout.NORTH);
        north.add(headerCard, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        panel.add(north, c);

        c.gridy = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(buildJobsTablePane(), c);
        return panel;
    }

    private JScrollPane buildJobsTablePane() {
        // Avoid thick focus rectangle on the focused cell (esp. numeric columns).
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder()));

        JTable table = new JTable(tableModel);
        table.setRowHeight(56);
        // No horizontal scrolling: fit columns into viewport and wrap long text.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TABLE_SELECTION_FG);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowVerticalLines(false);
        table.setGridColor(MoUiTheme.BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(MoUiTheme.TEXT_PRIMARY);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(MoUiTheme.TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, false, row, col);
                if (c instanceof JLabel l) {
                    l.setHorizontalAlignment(col <= 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                    l.setBorder(new EmptyBorder(8, col <= 1 ? 12 : 2, 8, 4));
                    String text = value == null ? "" : String.valueOf(value);
                    if (col == 2) {
                        // Keep full header visible without horizontal scrolling.
                        l.setText("<html><div style='text-align:center;line-height:1.1'>Hours<br/>/ Week</div></html>");
                    } else if (col == 4) {
                        l.setText("<html><div style='text-align:center;line-height:1.1'>Your<br/>applicants</div></html>");
                    } else {
                        l.setText(text);
                    }
                }
                return c;
            }
        };
        headerRenderer.setOpaque(true);
        headerRenderer.setBackground(Color.WHITE);
        table.getTableHeader().setDefaultRenderer(headerRenderer);

        // Ensure default renderers (columns without custom renderers) paint selection background fully.
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, false, row, column);
                c.setBackground(isSelected ? t.getSelectionBackground() : t.getBackground());
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

        table.getColumnModel().getColumn(0).setPreferredWidth(320);
        table.getColumnModel().getColumn(0).setMinWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(1).setMinWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(104);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
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
        scrollPane.setBorder(BorderFactory.createLineBorder(MoUiTheme.BORDER));
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
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            c.setForeground(isSelected ? TABLE_SELECTION_FG : table.getForeground());
            if (c instanceof JLabel lbl) {
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }
            return c;
        }
    }

    private static class JobTitleCellRenderer extends JLabel implements TableCellRenderer {
        JobTitleCellRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(0, 12, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            String title = String.valueOf(value);
            setToolTipText(title); // always allow full title via hover
            int colW = table.getColumnModel().getColumn(column).getWidth();
            if (colW <= 0) {
                colW = table.getColumnModel().getColumn(column).getPreferredWidth();
            }
            int contentW = Math.max(80, colW - 24);
            setText("<html><div style='width:" + contentW + "px;line-height:1.25'><span style='font-weight:700;color:#111827'>"
                    + escapeHtml(title)
                    + "</span></div></html>");
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            setForeground(isSelected ? TABLE_SELECTION_FG : MoUiTheme.TEXT_PRIMARY);
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

    private void persistJobs() {
        jobRepository.saveJobsForMo(MoContext.getCurrentMoUserId(), tableModel.getJobs());
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Open";
        }
        String lower = status.trim().toLowerCase(Locale.ENGLISH);
        if ("closed".equals(lower)) {
            return "Closed";
        }
        if ("draft".equals(lower)) {
            return "Draft";
        }
        return "Open";
    }

    private void refreshActionIconsForRow(int row, JButton editButton, JButton toggleButton, JButton deleteButton) {
        if (row < 0 || row >= tableModel.getRowCount()) {
            return;
        }
        Job job = tableModel.getJobAt(row);
        editButton.setIcon(MoActionIcons.editJobBlue());
        deleteButton.setIcon(MoActionIcons.deleteJobRed());
        
        String normalized = normalizeStatus(job != null ? job.getStatus() : null);
        
        if ("Draft".equalsIgnoreCase(normalized)) {
            toggleButton.setIcon(MoActionIcons.toggleSwitch(false));
            toggleButton.setText("Publish");
        } else if ("Open".equalsIgnoreCase(normalized)) {
            toggleButton.setIcon(MoActionIcons.toggleSwitch(true));
            toggleButton.setText("Open");
        } else {
            toggleButton.setIcon(MoActionIcons.toggleSwitch(false));
            toggleButton.setText("closed");
        }
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
            b.setBackground(new Color(0, 0, 0, 22));
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
                case 3 -> normalizeStatus(job.getStatus());
                case 4 -> applicationRepository.countApplicationsForJob(job.getId(), MoContext.getCurrentMoUserId());
                case 5 -> "";
                default -> "";
            };
        }

        public Job getJobAt(int rowIndex) {
            return jobs.get(rowIndex);
        }

        public List<Job> getJobs() {
            return jobs;
        }
    }

    private static class ModuleCellRenderer extends JLabel implements TableCellRenderer {
        ModuleCellRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(0, 12, 0, 8));
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
            setText("<html><div style='width:" + contentW + "px;line-height:1.25'><span style='font-weight:700;color:" + codeColor + "'>"
                    + escapeHtml(moduleCode)
                    + "</span><br/><span style='color:" + nameColor + "'>"
                    + escapeHtml(moduleName)
                    + "</span></div></html>");
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
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
            pill.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

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

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private class ActionsCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewButton = createSpecIconButton(MoActionIcons.viewJob(), "View", true);
        private final JButton taButton = createSpecIconButton(MoActionIcons.allocatedTas(), "TA", true);
        private final JButton editButton = createSpecIconButton(MoActionIcons.editJobBlue(), "Edit", true);
        private final JButton toggleButton = createSpecIconButton(MoActionIcons.toggleSwitch(false), "Open", true);
        private final JButton deleteButton = createSpecIconButton(MoActionIcons.deleteJobRed(), "Del", true);

        ActionsCellRenderer() {
            setLayout(new GridLayout(1, 5, 4, 0));
            setOpaque(true);
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
        private final JButton viewButton = createSpecIconButton(MoActionIcons.viewJob(), "View", false);
        private final JButton taButton = createSpecIconButton(MoActionIcons.allocatedTas(), "TA", false);
        private final JButton editButton = createSpecIconButton(MoActionIcons.editJobBlue(), "Edit", false);
        private final JButton toggleButton = createSpecIconButton(MoActionIcons.toggleSwitch(false), "Open", false);
        private final JButton deleteButton = createSpecIconButton(MoActionIcons.deleteJobRed(), "Del", false);

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
                    String current = normalizeStatus(job.getStatus());
                    String nextStatus;
                    if ("Draft".equalsIgnoreCase(current)) {
                        nextStatus = "Open";
                    } else if ("Open".equalsIgnoreCase(current)) {
                        nextStatus = "Closed";
                    } else {
                        nextStatus = "Open";
                    }
                    job.setStatus(nextStatus);
                    persistJobs();
                    tableModel.fireTableRowsUpdated(editingRow, editingRow);
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
                        tableModel.getJobs().remove(editingRow);
                        persistJobs();
                        tableModel.fireTableDataChanged();
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
        if (model.isPressed() || model.isArmed()) {
            button.setBackground(new Color(0, 0, 0, 36));
            button.setBorder(new EmptyBorder(1, 1, 1, 1));
        } else if (model.isRollover()) {
            button.setBackground(new Color(0, 0, 0, 22));
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
            int target = 56;
            for (int col = 0; col < cols; col++) {
                Component c = table.prepareRenderer(table.getCellRenderer(row, col), row, col);
                target = Math.max(target, c.getPreferredSize().height + 4);
            }
            if (table.getRowHeight(row) != target) {
                table.setRowHeight(row, target);
            }
        }
    }

}
