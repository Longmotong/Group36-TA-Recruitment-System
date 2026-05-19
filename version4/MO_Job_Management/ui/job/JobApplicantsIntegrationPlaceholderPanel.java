package MO_system.ui.job;

import MO_system.MoContext;
import MO_system.model.job.Job;
import MO_system.repository.ApplicationRepository;
import MO_system.repository.ApplicationRepository.ApplicationSummary;
import MO_system.ui.MoShellHost;
import MO_system.ui.MoUiTheme;
import MO_system.ui.review.ApplicationReviewPanelHost;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Job-scoped applicant summary from Job Detail. Uses the same data rules as
 * {@link ApplicationReviewPanelHost} when filtered to one job (MO-assigned rows only).
 * Replace the body with a richer review UI when integrating.
 */
public class JobApplicantsIntegrationPlaceholderPanel extends JPanel {
    private static final Color TABLE_HEADER_BG = new Color(0xF3EEFF);
    private static final Color TABLE_BORDER = new Color(0xE6DBFF);
    private static final Color TABLE_ROW_ALT = new Color(0xFCFAFF);

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

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(26, MoUiTheme.GUTTER, 40, MoUiTheme.GUTTER));

        String jid = job != null && job.getId() != null ? job.getId() : "";
        String jtitle = job != null && job.getTitle() != null ? job.getTitle() : "";

        if (jid.isBlank()) {
            JLabel err = new JLabel("<html><div style='width:720px;color:#64748b'>No job selected.</div></html>");
            err.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            body.add(err, BorderLayout.NORTH);
        } else {
            String moId = MoContext.getCurrentMoUserId();
            List<ApplicationSummary> rows = applicationRepository.listApplicationsForJob(jid, moId);
            int totalOnPosting = applicationRepository.countApplicationsForJob(jid, null);

            JPanel topStack = new JPanel();
            topStack.setOpaque(false);
            topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
            topStack.add(buildJobSummaryCard(
                    jtitle.isBlank() ? "(Untitled job)" : jtitle,
                    rows.size(),
                    totalOnPosting
            ));
            topStack.add(Box.createVerticalStrut(14));

            int accepted = 0;
            for (ApplicationSummary r : rows) {
                if ("accepted".equalsIgnoreCase(r.status()) || "accepted".equalsIgnoreCase(r.statusLabel())) {
                    accepted++;
                }
            }
            int pct = rows.isEmpty() ? 0 : (int) Math.round(accepted * 100.0 / rows.size());
            JPanel metricRow = new JPanel(new GridLayout(1, 3, 14, 0));
            metricRow.setOpaque(false);
            metricRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            metricRow.add(metricCard("Assigned to you", String.valueOf(rows.size()),
                    new Color(0xEFF6FF), new Color(0xBFDBFE), new Color(0x1D4ED8)));
            metricRow.add(metricCard("Total on posting", String.valueOf(totalOnPosting),
                    new Color(0xF5F3FF), new Color(0xDDD6FE), new Color(0x5B21B6)));
            metricRow.add(metricCard("Accepted rate", pct + "%",
                    new Color(0xECFDF5), new Color(0xA7F3D0), new Color(0x047857)));
            topStack.add(metricRow);
            topStack.add(Box.createVerticalStrut(14));
            body.add(topStack, BorderLayout.NORTH);

            if (rows.isEmpty()) {
                JLabel empty = new JLabel("<html><div style='width:720px;color:#888888'>"
                        + "No applications are assigned to you for this posting yet. "
                        + "If teammates submitted under another MO, they appear in the total above but not in this table."
                        + "</div></html>");
                empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                body.add(empty, BorderLayout.CENTER);
            } else {
                String[] cols = new String[]{"Application ID", "Name", "Student ID", "Email", "Status"};
                DefaultTableModel model = new DefaultTableModel(cols, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                for (ApplicationSummary r : rows) {
                    model.addRow(new Object[]{
                            r.applicationId(), r.fullName(), r.studentId(), r.email(), r.statusLabel()
                    });
                }
                JTable table = new JTable(model);
                table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                table.setRowHeight(34);
                table.setShowVerticalLines(false);
                table.setGridColor(TABLE_BORDER);
                table.setFillsViewportHeight(true);
                table.setAutoCreateRowSorter(true);
                table.setSelectionBackground(new Color(0xECE6FF));
                table.setSelectionForeground(MoUiTheme.TEXT_PRIMARY);
                table.getTableHeader().setBackground(TABLE_HEADER_BG);
                table.getTableHeader().setForeground(new Color(0x4F35D9));
                table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                table.getTableHeader().setReorderingAllowed(false);
                table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_BORDER));
                ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(SwingConstants.CENTER);
                table.setDefaultRenderer(Object.class, new ApplicantsCellRenderer());
                DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
                centered.setHorizontalAlignment(SwingConstants.CENTER);
                table.getColumnModel().getColumn(0).setCellRenderer(centered);
                table.getColumnModel().getColumn(2).setCellRenderer(centered);
                table.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
                table.getColumnModel().getColumn(0).setPreferredWidth(140);
                table.getColumnModel().getColumn(1).setPreferredWidth(180);
                table.getColumnModel().getColumn(2).setPreferredWidth(120);
                table.getColumnModel().getColumn(3).setPreferredWidth(280);
                table.getColumnModel().getColumn(4).setPreferredWidth(120);
                JScrollPane scroll = new JScrollPane(table);
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.getViewport().setBackground(Color.WHITE);

                JPanel tableSection = new JPanel(new BorderLayout(0, 10));
                tableSection.setOpaque(false);
                JPanel sectionHeader = new JPanel(new BorderLayout());
                sectionHeader.setOpaque(false);
                sectionHeader.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, MoUiTheme.ACCENT_PRIMARY),
                        new EmptyBorder(0, 12, 0, 0)
                ));
                JLabel sectionTitle = new JLabel("Allocated teaching assistants");
                sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32 - 15));
                sectionTitle.setForeground(MoUiTheme.TEXT_PRIMARY);
                sectionHeader.add(sectionTitle, BorderLayout.WEST);
                tableSection.add(sectionHeader, BorderLayout.NORTH);
                JPanel tableShell = JobsPortalUi.wrapRoundedInner(
                        scroll,
                        16,
                        Color.WHITE,
                        TABLE_BORDER,
                        1f,
                        true,
                        new java.awt.Insets(0, 0, 0, 0)
                );
                tableSection.add(tableShell, BorderLayout.CENTER);

                body.add(tableSection, BorderLayout.CENTER);
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
        back.setBorder(new EmptyBorder(6, 4, 6, 4));
        MoUiTheme.styleTextBackLink(back);
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

    private static JPanel buildJobSummaryCard(String title, int assignedCount, int totalCount) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE6DBFF)),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        titleLabel.setForeground(MoUiTheme.TEXT_PRIMARY);
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badges.setOpaque(false);
        badges.add(summaryBadge("Assigned to you", String.valueOf(assignedCount), new Color(0xEEF2FF), new Color(0x4338CA)));
        badges.add(summaryBadge("Total on posting", String.valueOf(totalCount), new Color(0xECFDF5), new Color(0x0F766E)));
        card.add(badges, BorderLayout.CENTER);
        return card;
    }

    private static JLabel summaryBadge(String label, String value, Color bg, Color valueColor) {
        JLabel l = new JLabel(label + ": " + value);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(valueColor);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        return l;
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
            if (isSelected) {
                l.setBackground(table.getSelectionBackground());
                l.setForeground(table.getSelectionForeground());
                return l;
            }
            if (text.equalsIgnoreCase("accepted")) {
                l.setBackground(new Color(0xDCFCE7));
                l.setForeground(new Color(0x166534));
            } else if (text.equalsIgnoreCase("offer pending") || text.equalsIgnoreCase("offer_pending")) {
                l.setBackground(new Color(0xFFFBEB));
                l.setForeground(new Color(0xB45309));
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

    private static final class ApplicantsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            l.setOpaque(true);
            l.setBorder(new EmptyBorder(6, 8, 6, 8));
            l.setHorizontalAlignment(column == 0 || column == 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
            if (isSelected) {
                l.setBackground(table.getSelectionBackground());
                l.setForeground(table.getSelectionForeground());
            } else {
                l.setBackground((row % 2 == 0) ? Color.WHITE : TABLE_ROW_ALT);
                l.setForeground(MoUiTheme.TEXT_PRIMARY);
            }
            return l;
        }
    }
}
