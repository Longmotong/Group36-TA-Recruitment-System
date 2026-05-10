package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.MoContext;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;
import com.mojobsystem.service.MoDashboardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * MO Dashboard — monochrome layout; metrics from {@code data/}. Embedded in {@link MoShellFrame}.
 */
public class MoDashboardPanel extends JPanel {
    private final MoShellHost host;
    private final JobRepository jobRepository = new JobRepository();
    private final ApplicationRepository applicationRepository = new ApplicationRepository();

    private JLabel metricCourses;
    private JLabel metricOpen;
    private JLabel metricPending;

    public MoDashboardPanel(MoShellHost host) {
        this.host = host;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBackground(MoUiTheme.PAGE_BG);
        add(wrapCentered(buildBody()), BorderLayout.CENTER);
    }

    /** Called when this card becomes visible (e.g. after MO switch). */
    public void refreshOnShow() {
        refreshMetrics();
    }

    private JPanel wrapCentered(JPanel content) {
        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBackground(MoUiTheme.PAGE_BG);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, MoUiTheme.GUTTER, 0, MoUiTheme.GUTTER);

        content.setMaximumSize(new Dimension(MoUiTheme.CONTENT_MAX_W, Integer.MAX_VALUE));
        content.setAlignmentX(Component.CENTER_ALIGNMENT);
        shell.add(content, c);
        return shell;
    }

    private JPanel buildBody() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(28, 0, 40, 0));

        root.add(welcomeBanner());
        root.add(Box.createVerticalStrut(22));

        JPanel cards = new JPanel(new GridLayout(1, 2, 24, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.add(moduleCard(
                "Job Management Module",
                MoGrayIcons.book(40),
                "Manage course info, requirements, and job postings",
                new String[]{
                        "Create and update course information",
                        "Post and manage TA job openings",
                        "Maintain module requirements and descriptions"
                },
                "Go to Job Management",
                ModuleCardStyle.JOB_MANAGEMENT,
                host::showJobList
        ));
        cards.add(moduleCard(
                "Application Review Module",
                MoGrayIcons.clipboard(40),
                "View TA applications, review, and check allocation results",
                new String[]{
                        "Browse applicants linked to your module jobs",
                        "Review submissions and supporting documents",
                        "Track allocation and hiring outcomes"
                },
                "Go to Application Review",
                ModuleCardStyle.APPLICATION_REVIEW,
                () -> host.showApplicationReview(null)
        ));
        root.add(cards);
        root.add(Box.createVerticalStrut(22));

        root.add(quickOverviewPanel());
        return root;
    }

    private enum ModuleCardStyle {
        JOB_MANAGEMENT(
                new Color(0x1A1A1A),
                new Color(0xF0F0F0),
                new Color(0xCCCCCC)
        ),
        APPLICATION_REVIEW(
                new Color(0x404040),
                new Color(0xF0F0F0),
                new Color(0xCCCCCC)
        );

        final Color accent;
        final Color iconBg;
        final Color iconBorder;

        ModuleCardStyle(Color accent, Color iconBg, Color iconBorder) {
            this.accent = accent;
            this.iconBg = iconBg;
            this.iconBorder = iconBorder;
        }
    }

    private JPanel welcomeBanner() {
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setOpaque(true);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(MoUiTheme.CONTENT_MAX_W, 200));
        banner.setBackground(Color.WHITE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x1A1A1A)),
                        BorderFactory.createLineBorder(MoUiTheme.BORDER)
                ),
                new EmptyBorder(22, 24, 24, 24)
        ));
        banner.putClientProperty(FlatClientProperties.STYLE, "arc: 14");

        JLabel title = new JLabel("<html><span style='color:#0f172a;font-size:30px;font-weight:700'>MO Dashboard</span></html>");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><div style='width:720px;line-height:1.5'><span style='color:#525252;font-size:15px'>"
                + "Welcome back. Pick a module below to manage jobs or review applications.</span></div></html>");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        banner.add(title);
        banner.add(Box.createVerticalStrut(10));
        banner.add(sub);
        return banner;
    }

    private JPanel moduleCard(String title,
                              javax.swing.ImageIcon icon,
                              String intro,
                              String[] bulletItems,
                              String cta,
                              ModuleCardStyle style,
                              Runnable onCta) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, style.accent),
                        BorderFactory.createLineBorder(MoUiTheme.BORDER)
                ),
                new EmptyBorder(22, 22, 20, 24)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 14");

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setBackground(style.iconBg);
        iconWrap.setBorder(BorderFactory.createLineBorder(style.iconBorder));
        iconWrap.setPreferredSize(new Dimension(56, 56));
        JLabel ic = new JLabel(icon);
        ic.setHorizontalAlignment(SwingConstants.CENTER);
        iconWrap.add(ic, BorderLayout.CENTER);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        t.setForeground(new Color(0x0F172A));
        JLabel introL = new JLabel("<html><div style='width:400px;line-height:1.45'>" + intro + "</div></html>");
        introL.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        introL.setForeground(new Color(0x525252));
        titles.add(t);
        titles.add(Box.createVerticalStrut(8));
        titles.add(introL);

        header.add(iconWrap, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel bulletPanel = new JPanel();
        bulletPanel.setLayout(new BoxLayout(bulletPanel, BoxLayout.Y_AXIS));
        bulletPanel.setOpaque(false);
        bulletPanel.setBorder(new EmptyBorder(18, 0, 0, 0));
        for (String b : bulletItems) {
            JLabel line = new JLabel("\u2022  " + b);
            line.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            line.setForeground(new Color(0x262626));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            line.setBorder(new EmptyBorder(0, 0, 6, 0));
            bulletPanel.add(line);
        }
        bulletPanel.add(Box.createVerticalGlue());

        JPanel upper = new JPanel(new BorderLayout(0, 0));
        upper.setOpaque(false);
        upper.add(header, BorderLayout.NORTH);
        upper.add(bulletPanel, BorderLayout.CENTER);

        JButton btn = new JButton(cta);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btn.setFocusPainted(false);
        MoUiTheme.stylePrimaryButton(btn, 10);
        btn.addActionListener(e -> onCta.run());
        int minW = 268;
        int h = Math.max(48, btn.getPreferredSize().height);
        btn.setPreferredSize(new Dimension(Math.max(minW, btn.getPreferredSize().width), h));
        btn.setMinimumSize(new Dimension(minW, h));

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(18, 0, 0, 0));
        btnRow.add(btn, BorderLayout.CENTER);

        card.add(upper, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quickOverviewPanel() {
        MoDashboardService.DashboardMetrics m = MoDashboardService.compute(
                jobRepository,
                applicationRepository,
                MoContext.getCurrentMoUserId()
        );

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(true);
        outer.setBackground(Color.WHITE);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(20, 24, 24, 24)
        ));
        outer.putClientProperty(FlatClientProperties.STYLE, "arc: 14");

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x1A1A1A)),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JLabel h = new JLabel("Quick Overview");
        h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        h.setForeground(new Color(0x0F172A));
        head.add(h, BorderLayout.CENTER);
        outer.add(head);
        outer.add(Box.createVerticalStrut(18));

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);

        metricCourses = new JLabel(String.valueOf(m.activeCourses()), SwingConstants.CENTER);
        metricOpen = new JLabel(String.valueOf(m.openJobPostings()), SwingConstants.CENTER);
        metricPending = new JLabel(String.valueOf(m.pendingReviews()), SwingConstants.CENTER);

        Color statBg = new Color(0xF5F5F5);
        Color statBorder = new Color(0xD4D4D4);
        Color statValue = new Color(0x111111);
        grid.add(statBox(metricCourses, "Active Courses", statBg, statBorder, statValue));
        grid.add(statBox(metricOpen, "Open Job Postings", statBg, statBorder, statValue));
        grid.add(statBox(metricPending, "Pending Reviews", statBg, statBorder, statValue));

        outer.add(grid);
        return outer;
    }

    private JPanel statBox(JLabel valueLabel, String caption, Color bg, Color border, Color valueColor) {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(bg);
        box.setOpaque(true);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(14, 12, 16, 12)
        ));
        box.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        cap.setForeground(new Color(0x404040));
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(Box.createVerticalGlue());
        inner.add(valueLabel);
        inner.add(Box.createVerticalStrut(6));
        inner.add(cap);
        inner.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        box.add(inner, gbc);
        return box;
    }

    private void refreshMetrics() {
        MoDashboardService.DashboardMetrics m = MoDashboardService.compute(
                jobRepository,
                applicationRepository,
                MoContext.getCurrentMoUserId()
        );
        if (metricCourses != null) {
            metricCourses.setText(String.valueOf(m.activeCourses()));
            metricOpen.setText(String.valueOf(m.openJobPostings()));
            metricPending.setText(String.valueOf(m.pendingReviews()));
        }
    }
}
