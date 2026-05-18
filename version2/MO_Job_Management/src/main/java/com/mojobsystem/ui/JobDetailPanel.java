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
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Locale;

/**
 * Job Detail — read-only content; actions delegate persistence to repository. Embedded in {@link MoShellFrame}.
 */
public class JobDetailPanel extends JPanel {
    private static final Color STATUS_OPEN_BG = new Color(0xDCFCE7);
    private static final Color STATUS_OPEN_FG = new Color(0x166534);
    private static final Color STATUS_CLOSED_BG = new Color(0xE0E7FF);
    private static final Color STATUS_CLOSED_FG = new Color(0x3730A3);
    private static final Color STATUS_DRAFT_BG = new Color(0xFFFBEB);
    private static final Color STATUS_DRAFT_FG = new Color(0x92400E);

    private static final Color STAT_MODULE_BG = new Color(0xEFF6FF);
    private static final Color STAT_MODULE_BORDER = new Color(0xBFDBFE);
    private static final Color STAT_MODULE_VALUE = new Color(0x1D4ED8);

    private static final Color STAT_QUOTA_BG = new Color(0xF5F3FF);
    private static final Color STAT_QUOTA_BORDER = new Color(0xDDD6FE);
    private static final Color STAT_QUOTA_VALUE = new Color(0x5B21B6);

    private static final Color STAT_HOURS_BG = new Color(0xFFFBEB);
    private static final Color STAT_HOURS_BORDER = new Color(0xFDE68A);
    private static final Color STAT_HOURS_VALUE = new Color(0xB45309);

    private static final Color STAT_APPLICANTS_BG = new Color(0xECFDF5);
    private static final Color STAT_APPLICANTS_BORDER = new Color(0xA7F3D0);
    private static final Color STAT_APPLICANTS_VALUE = new Color(0x047857);

    private static final Color BODY_WELL_BG = new Color(0xF8FAFC);
    private static final Color BODY_WELL_BORDER = new Color(0xE2E8F0);

    private final MoShellHost host;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final Runnable onDataChanged;
    private JScrollPane bodyScrollPane;
    private Job job;

    public JobDetailPanel(MoShellHost host,
                          JobRepository jobRepository,
                          Job job,
                          Runnable onDataChanged) {
        this.host = host;
        this.jobRepository = jobRepository;
        this.applicationRepository = new ApplicationRepository();
        this.job = job;
        this.onDataChanged = onDataChanged;

        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());
        bodyScrollPane = buildScrollBody();
        add(bodyScrollPane, BorderLayout.CENTER);
    }

    /** Reload UI for another job (same panel instance). */
    public void setJob(Job job) {
        this.job = job;
        removeAll();
        setLayout(new BorderLayout());
        bodyScrollPane = buildScrollBody();
        add(bodyScrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
        scrollToTop();
    }

    private JScrollPane buildScrollBody() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(16, 0, 40, 0));
        inner.setMaximumSize(new Dimension(MoUiTheme.CONTENT_MAX_W, Integer.MAX_VALUE));

        inner.add(buildHeroCard());
        inner.add(Box.createVerticalStrut(16));
        inner.add(metricRow());
        inner.add(Box.createVerticalStrut(16));

        inner.add(sectionCard("Job description",
                "What candidates will do in this role",
                wrapReadOnlyBody(emptyToPlaceholder(job.getDescription(), "No description provided."))));
        inner.add(Box.createVerticalStrut(12));

        inner.add(sectionCard("Required skills",
                "Skills used to match applicants",
                skillsBody()));
        inner.add(Box.createVerticalStrut(12));

        inner.add(sectionCard("Additional requirements",
                "Extra criteria or notes",
                wrapReadOnlyBody(emptyToPlaceholder(job.getAdditionalRequirements(),
                        "No additional requirements specified."))));
        inner.add(Box.createVerticalStrut(14));

        inner.add(applicantSection());

        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBackground(MoUiTheme.PAGE_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, MoUiTheme.GUTTER, 0, MoUiTheme.GUTTER);
        shell.add(inner, gbc);

        JScrollPane sp = new JScrollPane(shell);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MoUiTheme.PAGE_BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            SwingUtilities.invokeLater(this::scrollToTop);
        }
    }

    private void scrollToTop() {
        if (bodyScrollPane == null) {
            return;
        }
        bodyScrollPane.getVerticalScrollBar().setValue(0);
        bodyScrollPane.getHorizontalScrollBar().setValue(0);
        bodyScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
    }

    /** Same Back control as {@link CreateJobPanel#buildPageHeaderStrip()}. */
    private JPanel buildBackRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton back = new JButton("Back");
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorder(new EmptyBorder(4, 0, 4, 0));
        back.setForeground(MoUiTheme.TEXT_SECONDARY);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> {
            onDataChanged.run();
            host.showJobList();
        });
        row.add(back, BorderLayout.WEST);
        return row;
    }

    private JPanel buildHeroCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(18, 22, 20, 22)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        card.add(buildBackRow(), BorderLayout.NORTH);
        card.add(buildHero(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout(16, 0));
        hero.setOpaque(false);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel(job.getTitle());
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        String moduleLine = job.getModuleCode() + " · " + job.getModuleName();
        JLabel sub = new JLabel(moduleLine);
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        JPanel titleBand = new JPanel(new BorderLayout(20, 0));
        titleBand.setOpaque(false);
        titleBand.add(left, BorderLayout.CENTER);
        String st = normalizeStatus(job.getStatus());
        titleBand.add(buildHeroActionsRow(st), BorderLayout.EAST);

        hero.add(titleBand, BorderLayout.CENTER);
        return hero;
    }

    /** Status + Edit / Close / Delete: one row, even spacing, all outline buttons. */
    private JPanel buildHeroActionsRow(String normalizedStatus) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JButton edit = new JButton("Edit");
        MoUiTheme.styleAccentOutlineButton(edit, 10);
        edit.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        edit.setFocusPainted(false);
        edit.addActionListener(e -> host.showEditJob(job));

        JButton close = new JButton("Close job");
        MoUiTheme.styleOutlineButton(close, 10);
        close.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        close.setForeground(new Color(0x4B5563));
        close.setFocusPainted(false);
        close.addActionListener(e -> {
            job.setStatus("Closed");
            persistSingleJob();
            onDataChanged.run();
            host.showJobList();
        });

        JButton del = new JButton("Delete");
        MoUiTheme.styleDangerOutlineButton(del, 10);
        del.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        del.setFocusPainted(false);
        del.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(host.getShellFrame(),
                    "Permanently delete this job?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                deleteJob();
            }
        });

        row.add(statusPillHero(normalizedStatus));
        row.add(edit);
        row.add(close);
        row.add(del);
        return row;
    }

    private JPanel applicantSection() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(20, 22, 22, 22)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, MoUiTheme.ACCENT_PRIMARY),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JLabel h = new JLabel("Applicant management");
        h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        h.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel hint = new JLabel("Review submissions for this posting");
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        hint.setForeground(MoUiTheme.TEXT_SECONDARY);
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(h);
        titles.add(hint);
        top.add(titles, BorderLayout.WEST);

        int totalPosting = Math.max(applicationRepository.countApplicationsForJob(job.getId()), job.getApplicantsCount());
        int yours = applicationRepository.countApplicationsForJob(job.getId(), MoContext.getCurrentMoUserId());
        JLabel line = new JLabel(String.valueOf(yours));
        line.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        line.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel suffix = new JLabel(" assigned to you");
        suffix.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        suffix.setForeground(MoUiTheme.TEXT_SECONDARY);
        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setOpaque(false);
        countRow.add(line);
        countRow.add(suffix);
        if (totalPosting != yours) {
            JLabel totalNote = new JLabel("  ·  " + totalPosting + " total for this posting");
            totalNote.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            totalNote.setForeground(MoUiTheme.TEXT_SECONDARY);
            countRow.add(totalNote);
        }

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setOpaque(false);
        south.add(countRow);
        south.add(Box.createVerticalStrut(14));
        JButton go = new JButton("View applicants");
        MoUiTheme.styleAccentPrimaryButton(go, 10);
        go.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        go.setFocusPainted(false);
        go.setAlignmentX(Component.LEFT_ALIGNMENT);
        go.addActionListener(e -> host.showJobApplicantsPlaceholder(job));
        south.add(go);

        card.add(top, BorderLayout.NORTH);
        card.add(south, BorderLayout.CENTER);
        return card;
    }

    private JPanel metricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

        int yours = applicationRepository.countApplicationsForJob(job.getId(), MoContext.getCurrentMoUserId());
        row.add(statCard("Module", job.getModuleCode() + " — " + job.getModuleName(),
                STAT_MODULE_BG, STAT_MODULE_BORDER, STAT_MODULE_VALUE));
        row.add(statCard("Quota", String.valueOf(job.getQuota()),
                STAT_QUOTA_BG, STAT_QUOTA_BORDER, STAT_QUOTA_VALUE));
        row.add(statCard("Hours / week", job.getWeeklyHours() + "h",
                STAT_HOURS_BG, STAT_HOURS_BORDER, STAT_HOURS_VALUE));
        row.add(statCard("Your applicants", String.valueOf(yours),
                STAT_APPLICANTS_BG, STAT_APPLICANTS_BORDER, STAT_APPLICANTS_VALUE));
        return row;
    }

    private JPanel statCard(String label, String value, Color bg, Color borderColor, Color valueColor) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(16, 14, 18, 14)
        ));
        p.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel a = new JLabel(label.toUpperCase(Locale.ENGLISH), SwingConstants.CENTER);
        a.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        a.setForeground(new Color(0x334155));
        a.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel b = new JLabel("<html><div style='width:168px;text-align:center;line-height:1.35'>"
                + escapeHtml(value) + "</div></html>", SwingConstants.CENTER);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
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

    /**
     * Status column aligned with the action buttons (hint + pill, centered).
     */
    private JPanel statusPillHero(String normalized) {
        JLabel pill = new JLabel(normalized);
        pill.setOpaque(true);
        pill.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        pill.setBorder(new EmptyBorder(6, 14, 6, 14));
        if ("Open".equalsIgnoreCase(normalized)) {
            pill.setBackground(STATUS_OPEN_BG);
            pill.setForeground(STATUS_OPEN_FG);
        } else if ("Closed".equalsIgnoreCase(normalized)) {
            pill.setBackground(STATUS_CLOSED_BG);
            pill.setForeground(STATUS_CLOSED_FG);
        } else {
            pill.setBackground(STATUS_DRAFT_BG);
            pill.setForeground(STATUS_DRAFT_FG);
        }
        pill.putClientProperty(FlatClientProperties.STYLE, "arc: 999");

        JLabel hint = new JLabel("Status");
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        hint.setForeground(MoUiTheme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        pill.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(hint);
        stack.add(Box.createVerticalStrut(3));
        stack.add(pill);
        return stack;
    }

    private JPanel sectionCard(String title, String subtitle, Component body) {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(16, 18, 18, 18)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, MoUiTheme.ACCENT_PRIMARY),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel h = new JLabel(title);
        h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        h.setForeground(MoUiTheme.TEXT_PRIMARY);
        JLabel s = new JLabel(subtitle);
        s.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        s.setForeground(MoUiTheme.TEXT_SECONDARY);
        titles.add(h);
        titles.add(Box.createVerticalStrut(4));
        titles.add(s);
        head.add(titles, BorderLayout.CENTER);

        card.add(head, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel wrapReadOnlyBody(String text) {
        JTextPane a = readOnlyArea(text);
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(MoUiTheme.CONTENT_MAX_W - 80, Integer.MAX_VALUE));
        p.add(a, BorderLayout.CENTER);
        return p;
    }

    private JPanel skillsBody() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        flow.setOpaque(false);
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            JLabel empty = new JLabel("No skills listed for this job.");
            empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            empty.setForeground(MoUiTheme.TEXT_MUTED);
            flow.add(empty);
        } else {
            for (String s : job.getRequiredSkills()) {
                JLabel chip = new JLabel(s);
                MoUiTheme.styleSkillPill(chip);
                chip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                chip.setBorder(new EmptyBorder(7, 14, 7, 14));
                flow.add(chip);
            }
        }
        outer.add(flow, BorderLayout.NORTH);
        return outer;
    }

    /** Read-only body: centered text in a tinted well (matches TA Allocation polish). */
    private JTextPane readOnlyArea(String text) {
        JTextPane a = new JTextPane();
        a.setEditable(false);
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        a.setForeground(new Color(0x1F2937));
        a.setBackground(BODY_WELL_BG);
        a.setCaretColor(a.getForeground());
        a.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BODY_WELL_BORDER),
                new EmptyBorder(16, 18, 18, 18)
        ));
        String t = text == null ? "" : text;
        a.setText(t);
        applyCenterParagraphs(a);
        a.setCaretPosition(0);
        a.setFocusable(false);
        return a;
    }

    private static void applyCenterParagraphs(JTextPane pane) {
        StyledDocument doc = pane.getStyledDocument();
        int len = doc.getLength();
        if (len <= 0) {
            return;
        }
        SimpleAttributeSet sa = new SimpleAttributeSet();
        StyleConstants.setAlignment(sa, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, len, sa, false);
    }

    private static String emptyToPlaceholder(String raw, String placeholder) {
        if (raw == null || raw.isBlank()) {
            return placeholder;
        }
        return raw;
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

    private void persistSingleJob() {
        java.util.List<Job> mo = jobRepository.loadJobsForMo(com.mojobsystem.MoContext.getCurrentMoUserId());
        java.util.ArrayList<Job> next = new java.util.ArrayList<>(mo);
        for (int i = 0; i < next.size(); i++) {
            if (job.getId().equals(next.get(i).getId())) {
                next.set(i, job);
                break;
            }
        }
        jobRepository.saveJobsForMo(com.mojobsystem.MoContext.getCurrentMoUserId(), next);
    }

    private void deleteJob() {
        java.util.List<Job> mo = new java.util.ArrayList<>(jobRepository.loadJobsForMo(com.mojobsystem.MoContext.getCurrentMoUserId()));
        mo.removeIf(j -> job.getId().equals(j.getId()));
        jobRepository.saveJobsForMo(com.mojobsystem.MoContext.getCurrentMoUserId(), mo);
        onDataChanged.run();
        host.showJobList();
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

}
