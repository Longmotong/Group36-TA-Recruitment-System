package com.mojobsystem.ui;

import com.mojobsystem.MoContext;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.ApplicationRepository.ApplicationSummary;
import com.mojobsystem.repository.JobRepository;

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
 * Application list for the current MO: filtered by job when opened from job detail, or all
 * MO-scoped applications when opened from the shell without a job id.
 * Embedded in {@link MoShellFrame}.
 */
public class ApplicationReviewPlaceholderPanel extends JPanel {

    private final MoShellHost host;
    private final ApplicationRepository applicationRepository = new ApplicationRepository();
    private final JobRepository jobRepository = new JobRepository();

    public ApplicationReviewPlaceholderPanel(MoShellHost host, String fromJobId) {
        this.host = host;
        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());
        rebuildContent(fromJobId);
    }

    public void setFromJobId(String fromJobId) {
        removeAll();
        setLayout(new BorderLayout());
        rebuildContent(fromJobId);
        revalidate();
        repaint();
    }

    private void rebuildContent(String fromJobId) {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBackground(MoUiTheme.PAGE_BG);
        main.add(buildPageHeaderStrip(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(26, MoUiTheme.GUTTER, 40, MoUiTheme.GUTTER));

        String moId = MoContext.getCurrentMoUserId();
        boolean fromJob = fromJobId != null && !fromJobId.isBlank();
        List<ApplicationSummary> rows = fromJob
                ? applicationRepository.listApplicationsForJob(fromJobId, moId)
                : applicationRepository.listApplicationsForMoJobs(moId, jobRepository.loadMoJobIds(moId));

        JLabel intro = new JLabel("<html><div style='width:720px;color:#666666'>Applications assigned to you "
                + "(<code>workflow.assignedMO</code>). Data is read from <code>data/applications/</code>. "
                + "Full CV review and decisions are planned for a later sprint.</div></html>");
        intro.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(intro);
        body.add(Box.createVerticalStrut(14));

        String scopeLine = fromJob
                ? "Filtered to posting: <b>" + fromJobId + "</b>"
                : "All applications on your jobs (from <code>mo_jobs_index</code>), assigned to you.";
        JLabel scope = new JLabel("<html><div style='width:720px;color:#333333'>" + scopeLine + "</div></html>");
        scope.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        scope.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(scope);
        body.add(Box.createVerticalStrut(12));

        if (rows.isEmpty()) {
            JLabel empty = new JLabel(fromJob
                    ? "<html><div style='width:720px;color:#888888'>No applications assigned to you for this posting.</div></html>"
                    : "<html><div style='width:720px;color:#888888'>No applications assigned to you across your indexed jobs, "
                    + "or your job list is empty.</div></html>");
            empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(empty);
        } else {
            String[] cols = fromJob
                    ? new String[]{"Application ID", "Student ID", "Name", "Email", "Status"}
                    : new String[]{"Application ID", "Job ID", "Student ID", "Name", "Email", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (ApplicationSummary r : rows) {
                if (fromJob) {
                    model.addRow(new Object[]{r.applicationId(), r.studentId(), r.fullName(), r.email(), r.statusLabel()});
                } else {
                    model.addRow(new Object[]{
                            r.applicationId(), r.jobId(), r.studentId(), r.fullName(), r.email(), r.statusLabel()
                    });
                }
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
            host.jobDataChanged();
            host.showJobList();
        });
        leftCol.add(back);
        leftCol.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("Application Review");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(title);

        leftCol.add(Box.createVerticalStrut(6));
        JLabel sub = new JLabel("Review applicant queues and manage decisions");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(sub);

        strip.add(leftCol, BorderLayout.CENTER);
        return strip;
    }

}
