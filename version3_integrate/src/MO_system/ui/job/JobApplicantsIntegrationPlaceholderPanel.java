package com.mojobsystem.ui.job;

import com.mojobsystem.MoContext;
import com.mojobsystem.model.job.Job;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.ApplicationRepository.ApplicationSummary;
import com.mojobsystem.ui.MoShellHost;
import com.mojobsystem.ui.MoUiTheme;
import com.mojobsystem.ui.review.ApplicationReviewPanelHost;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * Job-scoped applicant summary from Job Detail. Uses the same data rules as
 * {@link ApplicationReviewPanelHost} when filtered to one job (MO-assigned rows only).
 * Replace the body with a richer review UI when integrating.
 */
public class JobApplicantsIntegrationPlaceholderPanel extends JPanel {

    private final MoShellHost host;
    private final ApplicationRepository applicationRepository = new ApplicationRepository();
    private Job job;

    public JobApplicantsIntegrationPlaceholderPanel(MoShellHost host, Job job) {
        this.host = host;
        this.job = job;
        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());
        rebuild();
    }

    public void setJob(Job job) {
        this.job = job;
        removeAll();
        setLayout(new BorderLayout());
        rebuild();
        revalidate();
        repaint();
    }

    private void rebuild() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBackground(MoUiTheme.PAGE_BG);
        main.add(buildPageHeaderStrip(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(26, MoUiTheme.GUTTER, 40, MoUiTheme.GUTTER));

        String jid = job != null && job.getId() != null ? job.getId() : "";
        String jtitle = job != null && job.getTitle() != null ? job.getTitle() : "";

        if (jid.isBlank()) {
            JLabel err = new JLabel("<html><div style='width:720px;color:#64748b'>No job selected.</div></html>");
            err.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            err.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(err);
        } else {
            String moId = MoContext.getCurrentMoUserId();
            List<ApplicationSummary> rows = applicationRepository.listApplicationsForJob(jid, moId);
            int totalOnPosting = applicationRepository.countApplicationsForJob(jid, null);

            JLabel jobLine = new JLabel("<html><div style='width:900px;color:#334155'><b>" + escapeHtml(jtitle.isBlank() ? "(Untitled job)" : jtitle)
                    + "</b><br/><span style='color:#64748b'>Job ID: <code>" + escapeHtml(jid) + "</code></span></div></html>");
            jobLine.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            jobLine.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(jobLine);
            body.add(Box.createVerticalStrut(12));

            JLabel stats = new JLabel("<html><div style='width:900px;color:#475569'>"
                    + "Assigned to you: <b>" + rows.size() + "</b> · Total: <b>" + totalOnPosting + "</b>"
                    + "</div></html>");
            stats.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            stats.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(stats);
            body.add(Box.createVerticalStrut(16));

            int accepted = 0;
            for (ApplicationSummary r : rows) {
                if ("accepted".equalsIgnoreCase(r.status()) || "accepted".equalsIgnoreCase(r.statusLabel())) {
                    accepted++;
                }
            }
            int pct = rows.isEmpty() ? 0 : (int) Math.round(accepted * 100.0 / rows.size());
            JPanel metricRow = new JPanel(new GridLayout(1, 3, 14, 0));
            metricRow.setOpaque(false);
            metricRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            metricRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            metricRow.add(metricCard("Assigned to you", String.valueOf(rows.size()),
                    new Color(0xEFF6FF), new Color(0xBFDBFE), new Color(0x1D4ED8)));
            metricRow.add(metricCard("Total on posting", String.valueOf(totalOnPosting),
                    new Color(0xF5F3FF), new Color(0xDDD6FE), new Color(0x5B21B6)));
            metricRow.add(metricCard("Accepted rate", pct + "%",
                    new Color(0xECFDF5), new Color(0xA7F3D0), new Color(0x047857)));
            body.add(metricRow);
            body.add(Box.createVerticalStrut(16));

            if (rows.isEmpty()) {
                JLabel empty = new JLabel("<html><div style='width:720px;color:#888888'>"
                        + "No applications are assigned to you for this posting yet. "
                        + "If teammates submitted under another MO, they appear in the total above but not in this table."
                        + "</div></html>");
                empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                body.add(empty);
            } else {
                String[] cols = new String[]{"Application ID", "Student ID", "Name", "Email", "Status"};
                DefaultTableModel model = new DefaultTableModel(cols, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                for (ApplicationSummary r : rows) {
                    model.addRow(new Object[]{
                            r.applicationId(), r.studentId(), r.fullName(), r.email(), r.statusLabel()
                    });
                }
                JTable table = new JTable(model);
                table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                table.setRowHeight(34);
                table.setShowVerticalLines(false);
                table.setGridColor(new Color(0xE2E8F0));
                table.setFillsViewportHeight(true);
                table.setAutoCreateRowSorter(true);
                table.getTableHeader().setBackground(new Color(0xF8FAFC));
                table.getTableHeader().setForeground(new Color(0x334155));
                table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                table.getTableHeader().setReorderingAllowed(false);
                DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
                centered.setHorizontalAlignment(SwingConstants.CENTER);
                table.getColumnModel().getColumn(0).setCellRenderer(centered);
                table.getColumnModel().getColumn(1).setCellRenderer(centered);
                table.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
                table.getColumnModel().getColumn(0).setPreferredWidth(140);
                table.getColumnModel().getColumn(1).setPreferredWidth(110);
                table.getColumnModel().getColumn(2).setPreferredWidth(160);
                table.getColumnModel().getColumn(3).setPreferredWidth(250);
                table.getColumnModel().getColumn(4).setPreferredWidth(120);
                JScrollPane scroll = new JScrollPane(table);
                scroll.setPreferredSize(new Dimension(900, Math.min(420, 28 + rows.size() * table.getRowHeight())));
                scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
                scroll.getViewport().setBackground(Color.WHITE);
                body.add(scroll);
            }
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MoUiTheme.BORDER));
        wrap.add(body, BorderLayout.CENTER);
        main.add(wrap, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

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
        back.addActionListener(e -> {
            if (job != null) {
                host.showJobDetail(job);
            } else {
                host.showJobList();
            }
        });
        leftCol.add(back);
        leftCol.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("Applicants");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(title);

        leftCol.add(Box.createVerticalStrut(6));
        JLabel sub = new JLabel("Job applications overview");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(sub);

        strip.add(leftCol, BorderLayout.CENTER);
        return strip;
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JPanel metricCard(String label, String value, Color bg, Color border, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel top = new JLabel(label.toUpperCase(), SwingConstants.CENTER);
        top.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        top.setForeground(new Color(0x334155));
        JLabel num = new JLabel(value, SwingConstants.CENTER);
        num.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        num.setForeground(valueColor);
        p.add(top, BorderLayout.NORTH);
        p.add(num, BorderLayout.CENTER);
        return p;
    }

    private static final class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            String text = value == null ? "" : String.valueOf(value);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setOpaque(true);
            if (text.equalsIgnoreCase("accepted")) {
                l.setBackground(new Color(0xDCFCE7));
                l.setForeground(new Color(0x166534));
            } else if (text.equalsIgnoreCase("rejected")) {
                l.setBackground(new Color(0xFEE2E2));
                l.setForeground(new Color(0xB91C1C));
            } else {
                l.setBackground(new Color(0xE2E8F0));
                l.setForeground(new Color(0x334155));
            }
            return l;
        }
    }
}
