package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.MoContext;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.JobRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MyJobsFrame extends JFrame {
    /** Softer than default LAF selection — avoids harsh blue + keeps text readable. */
    private static final Color TABLE_SELECTION_BG = new Color(0xD9E7FF);
    private static final Color TABLE_SELECTION_FG = new Color(0x111827);

    private final JobRepository jobRepository;
    private final JobTableModel tableModel;

    public MyJobsFrame() {
        this.jobRepository = new JobRepository();
        this.tableModel = new JobTableModel(new ArrayList<>());

        setTitle("MO System - My Jobs");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MoFrameGeometry.apply(this);
        setLayout(new BorderLayout());
        getContentPane().setBackground(MoUiTheme.PAGE_BG);

        add(NavigationPanel.create(NavigationPanel.Tab.JOB_MANAGEMENT, navActions()), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        // After first layout/paint (empty table), load from disk on a worker thread.
        SwingUtilities.invokeLater(this::loadJobsFromRepositoryAsync);

        MoFrameGeometry.finishTopLevelFrame(this);
    }

    private NavigationPanel.Actions navActions() {
        return new NavigationPanel.Actions(
                () -> MoFrameGeometry.navigateReplace(this, () -> new MoDashboardFrame().setVisible(true)),
                () -> { },
                () -> MoFrameGeometry.navigateReplace(this, () -> new ApplicationReviewPlaceholderFrame(null).setVisible(true)),
                () -> System.exit(0)
        );
    }

    public void reloadJobsFromRepository() {
        loadJobsFromRepositoryAsync();
    }

    private void loadJobsFromRepositoryAsync() {
        new SwingWorker<List<Job>, Void>() {
            @Override
            protected List<Job> doInBackground() {
                return jobRepository.loadJobsForMo(MoContext.CURRENT_MO_ID);
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
                            MyJobsFrame.this,
                            "Failed to load jobs: " + c.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JPanel buildMainContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, MoUiTheme.GUTTER, 28, MoUiTheme.GUTTER));

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
        MoUiTheme.styleAccentPrimaryButton(createNewJobButton, 10);
        createNewJobButton.setPreferredSize(new Dimension(200, 42));
        createNewJobButton.addActionListener(e -> {
            CreateJobFrame createFrame = new CreateJobFrame(this, jobRepository);
            createFrame.setVisible(true);
        });

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

        panel.add(headerCard, BorderLayout.NORTH);
        panel.add(buildJobsTablePane(), BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildJobsTablePane() {
        JTable table = new JTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0 || col != 5) {
                    return super.getToolTipText(e);
                }

                // Match FlowLayout.CENTER + 5 icon buttons (26px) + 8px gaps.
                int cellX = getCellRect(row, col, false).x;
                int relX = e.getX() - cellX;
                int iconW = 26;
                int gap = 8;
                int count = 5;
                int totalW = iconW * count + gap * (count - 1);
                int startX = Math.max(0, (getColumnModel().getColumn(col).getWidth() - totalW) / 2);
                int local = relX - startX;
                if (local < 0) {
                    return null;
                }
                int slot = local / (iconW + gap);
                int offset = local % (iconW + gap);
                if (slot < 0 || slot >= count || offset > iconW) {
                    return null;
                }
                return switch (slot) {
                    case 0 -> "View job details";
                    case 1 -> "View allocated TAs";
                    case 2 -> "Edit job";
                    case 3 -> "Mark job as closed/open";
                    case 4 -> "Delete job";
                    default -> null;
                };
            }
        };
        // Activate Swing tooltip manager for this table; actual text comes from getToolTipText(MouseEvent).
        table.setToolTipText("");
        table.setRowHeight(64);
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
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (c instanceof JLabel l) {
                    l.setHorizontalAlignment(col <= 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                    l.setBorder(new EmptyBorder(8, col <= 1 ? 12 : 2, 8, 4));
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
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
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

        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(104);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(320);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(MoUiTheme.BORDER));
        return scrollPane;
    }
    /** Hours/Week & Applicants — align with centered column headers. */
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
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
            // Two-line wrap within a reasonable width to avoid truncation.
            setText("<html><div style='width:250px;line-height:1.25'><span style='font-weight:700;color:#111827'>"
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
        jobRepository.saveJobsForMo(MoContext.CURRENT_MO_ID, tableModel.getJobs());
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
        boolean open = job != null && "Open".equalsIgnoreCase(normalizeStatus(job.getStatus()));
        toggleButton.setIcon(MoActionIcons.toggleSwitch(open));
        toggleButton.setToolTipText(open ? "Mark job as closed" : "Mark job as open");
    }

    private class JobTableModel extends AbstractTableModel {
        private final String[] columns = {"Job Title", "Module", "Hours/Week", "Status", "Applicants", "Actions"};
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
                case 4 -> job.getApplicantsCount();
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
            setText("<html><div style='line-height:1.25'><span style='font-weight:700;color:" + codeColor + "'>"
                    + moduleCode
                    + "</span><br/><span style='color:" + nameColor + "'>"
                    + moduleName
                    + "</span></div></html>");
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
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

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private class ActionsCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewButton = createSpecIconButton(MoActionIcons.viewJob(), "View job details");
        private final JButton taButton = createSpecIconButton(MoActionIcons.allocatedTas(), "View allocated TAs");
        private final JButton editButton = createSpecIconButton(MoActionIcons.editJobBlue(), "Edit job");
        private final JButton toggleButton = createSpecIconButton(MoActionIcons.toggleSwitch(false), "Mark job as open");
        private final JButton deleteButton = createSpecIconButton(MoActionIcons.deleteJobRed(), "Delete job");

        ActionsCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
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
            return this;
        }
    }

    private class ActionsCellEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        private final JButton viewButton = createSpecIconButton(MoActionIcons.viewJob(), "View job details");
        private final JButton taButton = createSpecIconButton(MoActionIcons.allocatedTas(), "View allocated TAs");
        private final JButton editButton = createSpecIconButton(MoActionIcons.editJobBlue(), "Edit job");
        private final JButton toggleButton = createSpecIconButton(MoActionIcons.toggleSwitch(false), "Mark job as open");
        private final JButton deleteButton = createSpecIconButton(MoActionIcons.deleteJobRed(), "Delete job");

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
                    new JobDetailFrame(MyJobsFrame.this, jobRepository, job, MyJobsFrame.this::reloadJobsFromRepository)
                            .setVisible(true);
                } else {
                    fireEditingStopped();
                }
            });
            taButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    fireEditingStopped();
                    new TaAllocationFrame(MyJobsFrame.this, job).setVisible(true);
                } else {
                    fireEditingStopped();
                }
            });
            editButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    fireEditingStopped();
                    new CreateJobFrame(MyJobsFrame.this, jobRepository, job).setVisible(true);
                } else {
                    fireEditingStopped();
                }
            });
            toggleButton.addActionListener(e -> {
                if (editingRow >= 0) {
                    Job job = tableModel.getJobAt(editingRow);
                    String nextStatus = "Open".equalsIgnoreCase(normalizeStatus(job.getStatus())) ? "Closed" : "Open";
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
                            MyJobsFrame.this,
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

    private static JButton createSpecIconButton(Icon icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(1, 2, 1, 2));
        button.setIconTextGap(0);
        button.setPreferredSize(new Dimension(26, 26));
        button.setFocusable(false);
        return button;
    }

}
