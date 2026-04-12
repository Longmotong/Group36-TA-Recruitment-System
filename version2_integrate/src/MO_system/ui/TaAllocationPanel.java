package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
 * Embedded in {@link MoShellFrame}.
 */
public class TaAllocationPanel extends JPanel {
    private static final Color CARD_REQUIRED_BG = new Color(0xEFF6FF);
    private static final Color CARD_REQUIRED_BORDER = new Color(0xBFDBFE);
    private static final Color CARD_REQUIRED_VALUE = new Color(0x1D4ED8);

    private static final Color CARD_ALLOC_BG = new Color(0xECFDF5);
    private static final Color CARD_ALLOC_BORDER = new Color(0xA7F3D0);
    private static final Color CARD_ALLOC_VALUE = new Color(0x047857);

    private final MoShellHost host;
    private Job job;
    private List<ApplicationRepository.AllocatedTaRecord> rows;

    public TaAllocationPanel(MoShellHost host, JobRepository jobRepository, Job job) {
        this.host = host;
        this.job = job;
        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());
        rebuildBody(jobRepository);
    }

    public void setJob(JobRepository jobRepository, Job job) {
        this.job = job;
        removeAll();
        setLayout(new BorderLayout());
        rebuildBody(jobRepository);
        revalidate();
        repaint();
    }

    private void rebuildBody(JobRepository jobRepository) {
        ApplicationRepository apps = new ApplicationRepository();
        this.rows = new ArrayList<>(apps.listAcceptedForJob(job.getId()));
        JobRepository.RichJobStats stats = jobRepository.readRichJobStats(job.getId());
        int richAccepted = stats == null ? 0 : stats.acceptedCount();
        if (rows.isEmpty() && richAccepted > 0) {
            JOptionPane.showMessageDialog(host.getShellFrame(),
                    "Job stats list " + richAccepted + " accepted hire(s), but no applications in data/applications "
                            + "have status \"accepted\" yet. The table below will populate when those records exist.",
                    "Allocation data",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBackground(MoUiTheme.PAGE_BG);
        main.add(buildPageHeaderStrip(), BorderLayout.NORTH);
        main.add(buildScrollBody(stats), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    /**
     * Same pattern as {@link CreateJobPanel#buildPageHeaderStrip()}: text-style Back, then title/subtitle.
     */
    private JPanel buildPageHeaderStrip() {
        JPanel strip = new JPanel(new BorderLayout(20, 0));
        strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER),
                new EmptyBorder(18, MoUiTheme.GUTTER, 20, MoUiTheme.GUTTER)
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
        back.addActionListener(e -> host.showJobList());
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

    private JScrollPane buildScrollBody(JobRepository.RichJobStats stats) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, MoUiTheme.GUTTER, 36, MoUiTheme.GUTTER));

        int required = Math.max(0, job.getQuota());
        int allocated = rows.size();
        if (stats != null && stats.acceptedCount() > allocated) {
            allocated = stats.acceptedCount();
        }
        String pct;
        if (required <= 0) {
            pct = "N/A";
        } else if (allocated >= required) {
            pct = "100% Complete";
        } else {
            pct = (int) Math.min(100, (allocated * 100.0 / required)) + "% Complete";
        }

        int allocatedShown = Math.max(rows.size(), stats != null ? stats.acceptedCount() : 0);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        statsRow.add(summaryCard("TAs required", String.valueOf(required),
                CARD_REQUIRED_BG, CARD_REQUIRED_BORDER, CARD_REQUIRED_VALUE));
        statsRow.add(summaryCard("TAs allocated", String.valueOf(allocatedShown),
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] colWidths = {110, 140, 88, 112, 200, 130, 72, 44, 96, 112, 200, 220};
        for (int i = 0; i < colWidths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
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
        HtmlWrappingCellRenderer wrapRenderer = new HtmlWrappingCellRenderer();
        table.setDefaultRenderer(Object.class, wrapRenderer);
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnMarginChanged(ChangeEvent e) {
                scheduleRowHeightAdjust(table);
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
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
        table.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                scheduleRowHeightAdjust(table);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scheduleRowHeightAdjust(table);
            }
        });
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        sp.getViewport().setBackground(Color.WHITE);
        root.add(sp);
        scheduleRowHeightAdjust(table);

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
        if (fc.showSaveDialog(host.getShellFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        StringBuilder sb = new StringBuilder(
                "applicationId,name,studentId,phone,email,program,year,gpa,weeklyHours,status,availability,skills\n");
        for (ApplicationRepository.AllocatedTaRecord r : rows) {
            String skills = r.skills().stream().map(s -> s.replace("\"", "\"\"")).collect(Collectors.joining(";"));
            int h = r.weeklyHours() > 0 ? r.weeklyHours() : job.getWeeklyHours();
            sb.append(csv(r.applicationId())).append(',')
                    .append(csv(r.fullName())).append(',')
                    .append(csv(r.studentId())).append(',')
                    .append(csv(r.phoneNumber())).append(',')
                    .append(csv(r.email())).append(',')
                    .append(csv(r.programMajor())).append(',')
                    .append(csv(r.year())).append(',')
                    .append(csv(r.gpa())).append(',')
                    .append(h).append(',')
                    .append(csv(r.statusLabel())).append(',')
                    .append(csv(r.availability())).append(',')
                    .append('"').append(skills).append('"').append('\n');
        }
        try {
            Files.writeString(fc.getSelectedFile().toPath(), sb.toString(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(host.getShellFrame(), "Exported " + rows.size() + " row(s).", "Export", JOptionPane.INFORMATION_MESSAGE);
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

    /**
     * Run after layout so column widths are known; double-pass fixes first-paint underestimates.
     */
    private static void scheduleRowHeightAdjust(JTable table) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            adjustWrappedRowHeights(table);
            javax.swing.SwingUtilities.invokeLater(() -> adjustWrappedRowHeights(table));
        });
    }

    /**
     * Recompute row heights so wrapped text is fully visible. Uses JLabel+HTML preferred size and a
     * font-metrics fallback when Swing still underestimates height.
     */
    private static void adjustWrappedRowHeights(JTable table) {
        int rows = table.getRowCount();
        int cols = table.getColumnCount();
        if (rows <= 0 || cols <= 0) {
            return;
        }
        FontMetrics fm = table.getFontMetrics(table.getFont());
        int lineH = fm.getHeight();
        int minH = Math.max(28, lineH + 8);
        for (int row = 0; row < rows; row++) {
            int maxH = minH;
            for (int col = 0; col < cols; col++) {
                Component c = table.prepareRenderer(table.getCellRenderer(row, col), row, col);
                int h = c.getPreferredSize().height;
                Object val = table.getValueAt(row, col);
                String text = val == null ? "" : val.toString();
                int colW = table.getColumnModel().getColumn(col).getWidth();
                if (colW <= 0) {
                    colW = table.getColumnModel().getColumn(col).getPreferredWidth();
                }
                int maxW = Math.max(24, colW - 16);
                int fallbackLines = estimateWrappedLines(text, maxW, fm);
                int fallbackH = fallbackLines * lineH + 12;
                maxH = Math.max(maxH, Math.max(h, fallbackH));
            }
            table.setRowHeight(row, maxH);
        }
    }

    /**
     * Word-wrap line count (same font as table); handles long tokens that exceed column width.
     */
    private static int estimateWrappedLines(String text, int maxWidth, FontMetrics fm) {
        if (text == null || text.isEmpty()) {
            return 1;
        }
        String[] words = text.split("\\s+");
        int lines = 0;
        int currentWidth = 0;
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            int wordW = fm.stringWidth(word);
            if (wordW > maxWidth) {
                if (currentWidth > 0) {
                    lines++;
                    currentWidth = 0;
                }
                lines += Math.max(1, (wordW + maxWidth - 1) / maxWidth);
                continue;
            }
            int gap = currentWidth > 0 ? fm.stringWidth(" ") : 0;
            if (currentWidth > 0 && currentWidth + gap + wordW > maxWidth) {
                lines++;
                currentWidth = wordW;
            } else {
                currentWidth += gap + wordW;
            }
        }
        if (currentWidth > 0) {
            lines++;
        }
        return Math.max(1, lines);
    }

    /**
     * HTML label with fixed width so wrapped text reports correct preferred height (JTextArea
     * renderer often under-reports in JTable).
     */
    private static final class HtmlWrappingCellRenderer extends DefaultTableCellRenderer {
        HtmlWrappingCellRenderer() {
            setVerticalAlignment(SwingConstants.TOP);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setVerticalAlignment(SwingConstants.TOP);
            c.setBorder(new EmptyBorder(6, 8, 6, 8));
            int w = table.getColumnModel().getColumn(column).getWidth();
            if (w <= 0) {
                w = table.getColumnModel().getColumn(column).getPreferredWidth();
            }
            int contentW = Math.max(40, w - 16);
            String raw = value == null ? "" : value.toString();
            String html = escapeHtmlBasic(raw);
            c.setText("<html><div style='width:" + contentW + "px'>" + html + "</div></html>");
            return c;
        }
    }

    private static String escapeHtmlBasic(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static class TaTableModel extends AbstractTableModel {
        private final List<ApplicationRepository.AllocatedTaRecord> data;
        private final int fallbackHours;
        private final String[] cols = {
                "Application ID",
                "Name",
                "Student ID",
                "Phone",
                "Email",
                "Program",
                "Year",
                "GPA",
                "Status",
                "Hours/wk",
                "Availability",
                "Skills"
        };

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
                case 0 -> r.applicationId();
                case 1 -> r.fullName();
                case 2 -> r.studentId();
                case 3 -> r.phoneNumber();
                case 4 -> r.email();
                case 5 -> r.programMajor();
                case 6 -> r.year();
                case 7 -> r.gpa();
                case 8 -> r.statusLabel();
                case 9 -> h + "h";
                case 10 -> r.availability();
                case 11 -> skills;
                default -> "";
            };
        }
    }

}
