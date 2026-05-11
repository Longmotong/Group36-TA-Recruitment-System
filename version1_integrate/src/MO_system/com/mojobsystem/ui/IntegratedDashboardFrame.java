package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.MoContext;
import com.mojobsystem.review.ApplicationReviewApp;
import com.mojobsystem.repository.ApplicationRepository;
import com.mojobsystem.repository.JobRepository;
import com.mojobsystem.service.MoDashboardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
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

/**
 * Integrated Dashboard Frame
 * Uses version1.1's styling, links to version1.3.26's Application Review functionality
 */
public class IntegratedDashboardFrame extends JFrame {
    private static IntegratedDashboardFrame activeInstance;

    private final JobRepository jobRepository = new JobRepository();
    private final ApplicationRepository applicationRepository = new ApplicationRepository();

    private JLabel metricCourses;
    private JLabel metricOpen;
    private JLabel metricPending;

    private IntegratedDashboardFrame thisFrame;

    public IntegratedDashboardFrame() {
        activeInstance = this;
        thisFrame = this;
        setTitle("MO System - Dashboard");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        MoFrameGeometry.apply(this);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xF1F5F9));

        add(buildNorthPanel(), BorderLayout.NORTH);
        add(wrapCentered(buildBody()), BorderLayout.CENTER);

        MoFrameGeometry.finishTopLevelFrame(this);
    }

    private JPanel buildNorthPanel() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));
        nav.setPreferredSize(new Dimension(0, 56));
        nav.setOpaque(true);

        JLabel title = new JLabel("MO System");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(new Color(0x0F172A));
        title.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        navButtons.setOpaque(false);
        navButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        JButton homeBtn = createNavButton("Home", true);
        JButton jobBtn = createNavButton("Job Management", false);
        jobBtn.addActionListener(e -> {
            MyJobsFrame jobsFrame = new MyJobsFrame();
            jobsFrame.setVisible(true);
            setVisible(false);
        });
        JButton reviewBtn = createNavButton("Application Review", false);
        reviewBtn.addActionListener(e -> {
            ApplicationReviewPlaceholderFrame.getInstance(thisFrame).setVisible(true);
            setVisible(false);
        });

        JButton logoutBtn = createNavButton("Log out", false);
        logoutBtn.setForeground(new Color(0xb91c1c));
        logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(0xfecaca)));
        logoutBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Logged out");
            System.exit(0);
        });

        navButtons.add(homeBtn);
        navButtons.add(jobBtn);
        navButtons.add(reviewBtn);
        navButtons.add(logoutBtn);

        nav.add(title, BorderLayout.WEST);
        nav.add(navButtons, BorderLayout.EAST);

        return nav;
    }

    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 36));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 9");

        if (active) {
            btn.setBackground(new Color(0x111827));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(0x374151));
            btn.setBorder(BorderFactory.createLineBorder(new Color(0xd1d5db)));
        }
        return btn;
    }

    private JPanel wrapCentered(JPanel content) {
        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBackground(new Color(0xF1F5F9));

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
                () -> {
                    MyJobsFrame jobsFrame = new MyJobsFrame();
                    jobsFrame.setVisible(true);
                    dispose();
                }
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
                "My Review Records",
                ModuleCardStyle.APPLICATION_REVIEW,
                () -> {
                    ApplicationReviewPlaceholderFrame.getInstance(thisFrame).setVisible(true);
                    setVisible(false);
                },
                () -> {
                    ApplicationReviewApp.setPendingMyReviewRecords(true);
                    ApplicationReviewPlaceholderFrame.getInstance(thisFrame).setVisible(true);
                    setVisible(false);
                }
        ));
        root.add(cards);
        root.add(Box.createVerticalStrut(22));

        root.add(quickOverviewPanel());
        return root;
    }

    private enum ModuleCardStyle {
        JOB_MANAGEMENT(
                new Color(0x2563EB),
                new Color(0xEFF6FF),
                new Color(0xBFDBFE)
        ),
        APPLICATION_REVIEW(
                new Color(0x0D9488),
                new Color(0xCCFBF1),
                new Color(0x5EEAD4)
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
        banner.setBackground(new Color(0xEFF6FF));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 5, 0, 0, MoUiTheme.ACCENT_PRIMARY),
                        BorderFactory.createLineBorder(new Color(0xBFDBFE))
                ),
                new EmptyBorder(22, 24, 24, 24)
        ));
        banner.putClientProperty(FlatClientProperties.STYLE, "arc: 14");

        JLabel title = new JLabel("<html><span style='color:#0f172a;font-size:30px;font-weight:700'>MO </span>"
                + "<span style='color:#2563EB;font-size:30px;font-weight:700'>Dashboard</span></html>");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><div style='width:720px;line-height:1.5'><span style='color:#475569;font-size:15px'>"
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
        return moduleCard(title, icon, intro, bulletItems, cta, null, style, onCta, null);
    }

    private JPanel moduleCard(String title,
                              javax.swing.ImageIcon icon,
                              String intro,
                              String[] bulletItems,
                              String cta,
                              String secondaryCta,
                              ModuleCardStyle style,
                              Runnable onCta,
                              Runnable onSecondaryCta) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, style.accent),
                        BorderFactory.createLineBorder(new Color(0xE2E8F0))
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
        introL.setForeground(new Color(0x475569));
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
            line.setForeground(new Color(0x1E293B));
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
        if (style == ModuleCardStyle.JOB_MANAGEMENT) {
            MoUiTheme.styleAccentPrimaryButton(btn, 10);
        } else {
            MoUiTheme.styleTealPrimaryButton(btn, 10);
        }
        btn.addActionListener(e -> onCta.run());
        int h = Math.max(48, btn.getPreferredSize().height);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, h));
        btn.setMinimumSize(new Dimension(0, h));

        JPanel btnRow = new JPanel();
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(18, 0, 0, 0));

        if (secondaryCta != null && onSecondaryCta != null) {
            JButton secondaryBtn = new JButton(secondaryCta);
            secondaryBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            secondaryBtn.setFocusPainted(false);
            MoUiTheme.styleOutlineButton(secondaryBtn, 10);
            secondaryBtn.addActionListener(e -> onSecondaryCta.run());
            secondaryBtn.setPreferredSize(new Dimension(secondaryBtn.getPreferredSize().width, h));
            secondaryBtn.setMinimumSize(new Dimension(0, h));

            btnRow.setLayout(new GridLayout(1, 2, 12, 0));
            btnRow.add(btn);
            btnRow.add(secondaryBtn);
        } else {
            btnRow.setLayout(new BorderLayout());
            btnRow.add(btn, BorderLayout.CENTER);
        }

        card.add(upper, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quickOverviewPanel() {
        MoDashboardService.DashboardMetrics m = MoDashboardService.compute(
                jobRepository,
                applicationRepository,
                MoContext.CURRENT_MO_ID
        );

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(true);
        outer.setBackground(Color.WHITE);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(20, 24, 24, 24)
        ));
        outer.putClientProperty(FlatClientProperties.STYLE, "arc: 14");

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, MoUiTheme.ACCENT_PRIMARY),
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

        grid.add(statBox(metricCourses, "Active Courses",
                new Color(0xEFF6FF), new Color(0xBFDBFE), new Color(0x1D4ED8)));
        grid.add(statBox(metricOpen, "Open Job Postings",
                new Color(0xECFDF5), new Color(0xA7F3D0), new Color(0x047857)));
        grid.add(statBox(metricPending, "Pending Reviews",
                new Color(0xFFFBEB), new Color(0xFDE68A), new Color(0xB45309)));

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
        cap.setForeground(new Color(0x334155));
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

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            refreshMetrics();
        }
    }

    private void refreshMetrics() {
        MoDashboardService.DashboardMetrics m = MoDashboardService.compute(
                jobRepository,
                applicationRepository,
                MoContext.CURRENT_MO_ID
        );
        if (metricCourses != null) {
            metricCourses.setText(String.valueOf(m.activeCourses()));
            metricOpen.setText(String.valueOf(m.openJobPostings()));
            metricPending.setText(String.valueOf(m.pendingReviews()));
        }
    }

    public static JFrame getActiveInstance() { return activeInstance; }
}
