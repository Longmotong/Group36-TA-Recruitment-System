package com.mojobsystem.ui;

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
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyJobsFrame extends JFrame {
    private static final Color PAGE_BG = new Color(248, 250, 252);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_SUB = new Color(100, 116, 139);
    private static final Color PRIMARY = new Color(3, 2, 19);

    private final JobRepository jobRepository;
    private final JobTableModel tableModel;

    public MyJobsFrame() {
        this.jobRepository = new JobRepository();
        this.tableModel = new JobTableModel(jobRepository.loadAllJobs());

        setTitle("MO Job System - My Jobs");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);

        add(NavigationPanel.create(NavigationPanel.Tab.JOB_MANAGEMENT), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    public void reloadJobsFromRepository() {
        tableModel.replaceJobs(jobRepository.loadAllJobs());
    }

    private JPanel buildMainContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(28, 40, 32, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);

        JLabel title = new JLabel("My Jobs");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
        JLabel subtitle = new JLabel("Manage your TA recruitment positions");
        subtitle.setForeground(TEXT_SUB);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(4));
        leftHeader.add(subtitle);
        header.add(leftHeader, BorderLayout.WEST);

        JButton createNewJobButton = new JButton("+  Create New Job");
        createNewJobButton.setPreferredSize(new Dimension(166, 40));
        createNewJobButton.setBackground(PRIMARY);
        createNewJobButton.setForeground(Color.WHITE);
        createNewJobButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        createNewJobButton.setFocusPainted(false);
        createNewJobButton.setBorder(new EmptyBorder(8, 14, 8, 14));
        createNewJobButton.addActionListener(e -> {
            CreateJobFrame createFrame = new CreateJobFrame(this, jobRepository);
            createFrame.setVisible(true);
        });
        header.add(createNewJobButton, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(buildJobsTablePane(), BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildJobsTablePane() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(56);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_MAIN);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(TEXT_MAIN);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        table.getColumnModel().getColumn(1).setCellRenderer(new ModuleCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionsCellEditor());

        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(280);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        return scrollPane;
    }

    private void persistJobs() {
        jobRepository.saveAllJobs(tableModel.getJobs());
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
            setBorder(new EmptyBorder(0, 0, 0, 0));
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

            setText("<html><div style='line-height:1.25'><span style='font-weight:700;color:#0f172a'>"
                    + moduleCode
                    + "</span><br/><span style='color:#64748b'>"
                    + moduleName
                    + "</span></div></html>");
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private static class StatusCellRenderer extends JLabel implements TableCellRenderer {
        StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            String status = String.valueOf(value);
            setText(status);

            if ("Closed".equalsIgnoreCase(status)) {
                setBackground(new Color(241, 245, 249));
                setForeground(new Color(100, 116, 139));
            } else if ("Draft".equalsIgnoreCase(status)) {
                setBackground(new Color(254, 243, 199));
                setForeground(new Color(146, 64, 14));
            } else {
                setBackground(new Color(220, 252, 231));
                setForeground(new Color(22, 101, 52));
            }
            return this;
        }
    }

    private class ActionsCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewButton = createGhostIconButton("View", new Color(30, 41, 59));
        private final JButton taButton = createGhostIconButton("TAs", new Color(37, 99, 235));
        private final JButton editButton = createGhostIconButton("Edit", new Color(30, 41, 59));
        private final JButton toggleButton = createGhostIconButton("Toggle", new Color(22, 163, 74));
        private final JButton deleteButton = createGhostIconButton("Delete", new Color(220, 38, 38));

        ActionsCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 8));
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
            return this;
        }
    }

    private class ActionsCellEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        private final JButton viewButton = createGhostIconButton("View", new Color(30, 41, 59));
        private final JButton taButton = createGhostIconButton("TAs", new Color(37, 99, 235));
        private final JButton editButton = createGhostIconButton("Edit", new Color(30, 41, 59));
        private final JButton toggleButton = createGhostIconButton("Toggle", new Color(22, 163, 74));
        private final JButton deleteButton = createGhostIconButton("Delete", new Color(220, 38, 38));

        private int editingRow = -1;

        ActionsCellEditor() {
            panel.setOpaque(true);
            panel.add(viewButton);
            panel.add(taButton);
            panel.add(editButton);
            panel.add(toggleButton);
            panel.add(deleteButton);

            viewButton.addActionListener(e -> {
                showActionHint("View Job Detail page");
                fireEditingStopped();
            });
            taButton.addActionListener(e -> {
                showActionHint("TA Allocation Results page");
                fireEditingStopped();
            });
            editButton.addActionListener(e -> {
                showActionHint("Edit Job page");
                fireEditingStopped();
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
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private void showActionHint(String pageName) {
        JOptionPane.showMessageDialog(
                this,
                pageName + " will be implemented in next step.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static JButton createGhostIconButton(String text, Color textColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(2, 6, 2, 6));
        button.setMargin(new Insets(2, 6, 2, 6));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        button.setForeground(textColor);
        button.setToolTipText(text);
        return button;
    }
}
