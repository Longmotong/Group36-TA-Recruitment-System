package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.ApplicationRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spec (6) TA Allocation Results — roster from accepted applications, summary cards from data.
 */
public class TaAllocationFrame extends JFrame {
    private static final Color CARD_REQUIRED_BG = new Color(0xEFF6FF);
    private static final Color CARD_REQUIRED_BORDER = new Color(0xBFDBFE);
    private static final Color CARD_REQUIRED_VALUE = new Color(0x1D4ED8);

    private static final Color CARD_ALLOC_BG = new Color(0xECFDF5);
    private static final Color CARD_ALLOC_BORDER = new Color(0xA7F3D0);
    private static final Color CARD_ALLOC_VALUE = new Color(0x047857);

    private final Job job;
    private final List<ApplicationRepository.AllocatedTaRecord> rows;

    public TaAllocationFrame(JFrame parent, Job job) {
        this.job = job;
        ApplicationRepository apps = new ApplicationRepository();
        this.rows = new ArrayList<>(apps.listAcceptedForJob(job.getId()));

        setTitle("MO System - TA Allocation Results");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        MoFrameGeometry.applyMatching(parent, this);
        getContentPane().setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());

        add(NavigationPanel.create(NavigationPanel.Tab.JOB_MANAGEMENT, navActions()), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBackground(MoUiTheme.PAGE_BG);
        main.add(buildPageHeaderStrip(), BorderLayout.NORTH);
        main.add(buildScrollBody(), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    private NavigationPanel.Actions navActions() {
        return new NavigationPanel.Actions(
                () -> MoFrameGeometry.navigateReplace(this, () -> new MoDashboardFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new MyJobsFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new ApplicationReviewPlaceholderFrame(job.getId()).setVisible(true)),
                () -> System.exit(0)
        );
    }

    /**
     * Same pattern as {@link CreateJobFrame#buildPageHeaderStrip()}: text-style Back, then title/subtitle.
     */
    private JPanel buildPageHeaderStrip() {
        JPanel strip = new JPanel(new BorderLayout(20, 0));
        strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER),
                new EmptyBorder(18, 40, 20, 40)
        ));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JButton back = new JButton("Back");
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorder(new EmptyBorder(6, 4, 6, 4));
        back.setForeground(MoUiTheme.TEXT_SECONDARY);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> dispose());
        leftCol.add(back);
        leftCol.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("TA Allocation Results");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(title);
        leftCol.add(Box.createVerticalStrut(6));

        String subLine = job.getTitle() + " · " + job.getModuleCode() + " — " + job.getModuleName();
        JLabel sub = new JLabel(subLine);
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(sub);

        JButton export = new JButton("Export to CSV");
        MoUiTheme.styleAccentPrimaryButton(export, 10);
        export.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        export.setFocusPainted(false);
        export.addActionListener(e -> exportCsv());

        JPanel east = new JPanel(new BorderLayout());
        east.setOpaque(false);
        east.add(export, BorderLayout.NORTH);

        strip.add(leftCol, BorderLayout.CENTER);
        strip.add(east, BorderLayout.EAST);
        return strip;
    }

    private JScrollPane buildScrollBody() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 40, 36, 40));

        int required = Math.max(0, job.getQuota());
        int allocated = rows.size();
        String pct;
        if (required <= 0) {
            pct = "N/A";
        } else if (allocated >= required) {
            pct = "100% Complete";
        } else {
            pct = (int) Math.min(100, (allocated * 100.0 / required)) + "% Complete";
        }

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        statsRow.add(summaryCard("TAs required", String.valueOf(required),
                CARD_REQUIRED_BG, CARD_REQUIRED_BORDER, CARD_REQUIRED_VALUE));
        statsRow.add(summaryCard("TAs allocated", String.valueOf(allocated),
                CARD_ALLOC_BG, CARD_ALLOC_BORDER, CARD_ALLOC_VALUE));
        statsRow.add(summaryCardForStatus(pct));
        root.add(statsRow);
        root.add(Box.createVerticalStrut(26));

        JPanel secHeader = new JPanel(new BorderLayout(0, 0));
        secHeader.setOpaque(false);
        secHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, MoUiTheme.ACCENT_PRIMARY),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JLabel sec = new JLabel("Allocated teaching assistants");
        sec.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        sec.setForeground(MoUiTheme.TEXT_PRIMARY);
        secHeader.add(sec, BorderLayout.CENTER);
        root.add(secHeader);
        root.add(Box.createVerticalStrut(12));

        TaTableModel model = new TaTableModel(rows, job.getWeeklyHours());
        JTable table = new JTable(model);
        table.setRowHeight(52);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.setForeground(new Color(0x1F2937));
        table.setGridColor(new Color(0xE5E7EB));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0xF1F5F9));
        table.getTableHeader().setForeground(new Color(0x334155));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        sp.getViewport().setBackground(Color.WHITE);
        root.add(sp);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MoUiTheme.PAGE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel summaryCard(String label, String value, Color bg, Color borderColor, Color valueColor) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(18, 18, 20, 18)
        ));
        p.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel a = new JLabel(label, SwingConstants.CENTER);
        a.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        a.setForeground(new Color(0x334155));
        a.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel b = new JLabel(value, SwingConstants.CENTER);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        b.setForeground(valueColor);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(Box.createVerticalGlue());
        inner.add(a);
        inner.add(Box.createVerticalStrut(8));
        inner.add(b);
        inner.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(inner, gbc);
        return p;
    }

    /** Status card uses semantic background / value color from completion string. */
    private JPanel summaryCardForStatus(String pct) {
        Color bg;
        Color border;
        Color fg;
        if ("N/A".equals(pct)) {
            bg = new Color(0xF9FAFB);
            border = new Color(0xE5E7EB);
            fg = MoUiTheme.TEXT_SECONDARY;
        } else if (pct.startsWith("100")) {
            bg = new Color(0xECFDF5);
            border = new Color(0xA7F3D0);
            fg = new Color(0x047857);
        } else if (pct.startsWith("0%")) {
            bg = new Color(0xFFF1F2);
            border = new Color(0xFECDD3);
            fg = new Color(0xBE123C);
        } else {
            bg = new Color(0xFFFBEB);
            border = new Color(0xFDE68A);
            fg = new Color(0xB45309);
        }
        return summaryCard("Allocation status", pct, bg, border, fg);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("allocated_tas_" + job.getId() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        StringBuilder sb = new StringBuilder("name,studentId,email,weeklyHours,status,skills\n");
        for (ApplicationRepository.AllocatedTaRecord r : rows) {
            String skills = r.skills().stream().map(s -> s.replace("\"", "\"\"")).collect(Collectors.joining(";"));
            sb.append(csv(r.fullName())).append(',')
                    .append(csv(r.studentId())).append(',')
                    .append(csv(r.email())).append(',')
                    .append(r.weeklyHours() > 0 ? r.weeklyHours() : job.getWeeklyHours()).append(',')
                    .append("Allocated").append(',')
                    .append('"').append(skills).append('"').append('\n');
        }
        try {
            Files.writeString(fc.getSelectedFile().toPath(), sb.toString(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Exported " + rows.size() + " row(s).", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) {
        if (s == null) {
            return "\"\"";
        }
        return '"' + s.replace("\"", "\"\"") + '"';
    }

    private static class TaTableModel extends AbstractTableModel {
        private final List<ApplicationRepository.AllocatedTaRecord> data;
        private final int fallbackHours;
        private final String[] cols = {"Name", "Student ID", "Status", "Email", "Hours / week", "Skills"};

        TaTableModel(List<ApplicationRepository.AllocatedTaRecord> data, int fallbackHours) {
            this.data = data;
            this.fallbackHours = fallbackHours;
        }

        @Override
        public int getRowCount() {
            return data.size();
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
            ApplicationRepository.AllocatedTaRecord r = data.get(rowIndex);
            int h = r.weeklyHours() > 0 ? r.weeklyHours() : fallbackHours;
            String skills = String.join(", ", r.skills());
            return switch (columnIndex) {
                case 0 -> r.fullName();
                case 1 -> r.studentId();
                case 2 -> "Allocated";
                case 3 -> r.email();
                case 4 -> h + "h";
                case 5 -> skills;
                default -> "";
            };
        }
    }

}
