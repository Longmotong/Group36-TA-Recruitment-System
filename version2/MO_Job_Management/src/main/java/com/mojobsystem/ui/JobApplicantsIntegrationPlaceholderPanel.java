package com.mojobsystem.ui;

import com.mojobsystem.MoContext;
import com.mojobsystem.model.Job;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.ApplicationRepository.ApplicationSummary;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * Job-scoped applicant summary from Job Detail. Uses the same data rules as
 * {@link ApplicationReviewPlaceholderPanel} when filtered to one job (MO-assigned rows only).
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
                    + "<b>" + rows.size() + "</b> application(s) assigned to you on this posting · "
                    + "<b>" + totalOnPosting + "</b> total application file(s) for this job in <code>data/applications/</code>"
                    + "</div></html>");
            stats.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            stats.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(stats);
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
                table.setRowHeight(22);
                table.setShowGrid(true);
                table.setGridColor(MoUiTheme.BORDER);
                table.setFillsViewportHeight(true);
                table.setAutoCreateRowSorter(true);
                JScrollPane scroll = new JScrollPane(table);
                scroll.setPreferredSize(new Dimension(900, Math.min(420, 28 + rows.size() * table.getRowHeight())));
                scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                body.add(scroll);
            }

            body.add(Box.createVerticalStrut(20));
            JLabel note = new JLabel("<html><div style='width:720px;color:#94a3b8;font-size:12px'>"
                    + "Integration: full review workflow stays on the <b>Application Review</b> nav tab; "
                    + "swap this card’s content in <code>MoShellFrame</code> (<code>jobApplicants</code>) when merging."
                    + "</div></html>");
            note.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(note);
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
        JLabel sub = new JLabel("Summary for this posting (data from applications JSON)");
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
}
