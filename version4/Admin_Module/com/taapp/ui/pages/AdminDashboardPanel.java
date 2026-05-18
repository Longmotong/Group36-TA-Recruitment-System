package Admin_Module.com.taapp.ui.pages;

import Admin_Module.com.taapp.data.DataStore;
import Admin_Module.com.taapp.model.Statistics;
import Admin_Module.com.taapp.ui.AppLayout;
import Admin_Module.com.taapp.ui.MainFrame;
import Admin_Module.com.taapp.ui.UI;
import Admin_Module.com.taapp.ui.components.Page;
import Admin_Module.com.taapp.ui.components.RoundedActionButton;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Admin home — Soft-Neo chrome aligned with {@link JobsPortalUi} (lavender page, purple CTAs, rounded cards).
 */
public class AdminDashboardPanel extends Page {
    public AdminDashboardPanel(Consumer<String> onNavigate) {
        super();
        Objects.requireNonNull(onNavigate);

        Statistics stats = DataStore.defaultStore().getStatistics();

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 0, 32, 0));

        root.add(welcomeBanner());
        root.add(Box.createVerticalStrut(18));

        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 400));
        cards.add(moduleCard(
                "TA",
                "TA Workload Module",
                "Manage TA workload",
                new String[]{
                        "Create and update course information",
                        "Post and manage TA workload allocations",
                        "Maintain module requirements and descriptions"
                },
                "Go to TA Workload",
                () -> onNavigate.accept(MainFrame.ROUTE_WORKLOAD)
        ));
        cards.add(moduleCard(
                "ST",
                "Statistics Module",
                "View key statistics",
                new String[]{
                        "Browse departmental workload summaries",
                        "Review application and allocation trends",
                        "Track staffing performance outcomes"
                },
                "Go to Statistics",
                () -> onNavigate.accept(MainFrame.ROUTE_STATISTICS)
        ));
        root.add(cards);
        root.add(Box.createVerticalStrut(18));

        root.add(quickOverviewPanel(stats));
        root.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, Integer.MAX_VALUE));

        content().add(AppLayout.wrapCentered(root), BorderLayout.CENTER);
    }

    private JPanel welcomeBanner() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 22, 20, 22));

        JLabel title = new JLabel("Admin Workspace");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(JobsPortalUi.DARK_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(JobsPortalUi.PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(36, 3));
        underline.setMaximumSize(new Dimension(36, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><div style='width:720px;line-height:1.5'><span style='color:#525252;font-size:14px;font-family:sans-serif'>"
                + "Open a module to review live workload and statistics data from the JSON records.</span></div></html>");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createVerticalStrut(8));
        inner.add(underline);
        inner.add(Box.createVerticalStrut(10));
        inner.add(sub);

        JobsPortalUi.RoundedSurface banner = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, JobsPortalUi.LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 200));
        banner.add(inner, BorderLayout.CENTER);
        return banner;
    }

    private JPanel moduleCard(String iconText,
                              String title,
                              String intro,
                              String[] bulletItems,
                              String cta,
                              Runnable onCta) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, JobsPortalUi.LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(18, 20, 18, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);

        JLabel ic = new JLabel(iconText, SwingConstants.CENTER);
        ic.setFont(UI.moFontBold(14));
        ic.setForeground(JobsPortalUi.PRIMARY_PURPLE);
        JPanel iconWrap = JobsPortalUi.wrapRoundedInner(ic, 14, JobsPortalUi.LAVENDER,
                JobsPortalUi.LIGHT_PURPLE_BORDER, 1f, false, new Insets(10, 10, 10, 10));
        iconWrap.setPreferredSize(new Dimension(52, 52));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UI.moFontBold(18));
        t.setForeground(JobsPortalUi.DARK_TEXT);
        JLabel introL = new JLabel("<html><div style='width:360px;line-height:1.45;font-family:sans-serif;color:#525252;font-size:14px'>" + intro + "</div></html>");
        titles.add(t);
        titles.add(Box.createVerticalStrut(6));
        titles.add(introL);

        header.add(iconWrap, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel bulletPanel = new JPanel();
        bulletPanel.setLayout(new BoxLayout(bulletPanel, BoxLayout.Y_AXIS));
        bulletPanel.setOpaque(false);
        bulletPanel.setBorder(new EmptyBorder(14, 0, 0, 0));
        for (String b : bulletItems) {
            JLabel line = new JLabel("\u2022  " + b);
            line.setFont(UI.moFontPlain(13));
            line.setForeground(JobsPortalUi.TEXT_GRAY);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            line.setBorder(new EmptyBorder(0, 0, 4, 0));
            bulletPanel.add(line);
        }

        JPanel upper = new JPanel(new BorderLayout(0, 0));
        upper.setOpaque(false);
        upper.add(header, BorderLayout.NORTH);
        upper.add(bulletPanel, BorderLayout.CENTER);

        RoundedActionButton btn = new RoundedActionButton(cta, RoundedActionButton.Scheme.PRIMARY_BLACK);
        btn.setFont(UI.moFontBold(13));
        btn.addActionListener(e -> onCta.run());
        int minW = 260;
        int h = Math.max(46, btn.getPreferredSize().height);
        btn.setPreferredSize(new Dimension(Math.max(minW, btn.getPreferredSize().width), h));
        btn.setMinimumSize(new Dimension(minW, h));

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        btnRow.add(btn, BorderLayout.CENTER);

        card.add(upper, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quickOverviewPanel(Statistics stats) {
        JobsPortalUi.RoundedSurface outer = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, JobsPortalUi.LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setBorder(new EmptyBorder(16, 22, 20, 22));
        outer.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 300));

        JPanel inner = new JPanel(new BorderLayout(0, 14));
        inner.setOpaque(false);

        JPanel headerStack = new JPanel();
        headerStack.setLayout(new BoxLayout(headerStack, BoxLayout.Y_AXIS));
        headerStack.setOpaque(false);

        JLabel h = new JLabel("Quick Overview");
        h.setFont(UI.moFontBold(17));
        h.setForeground(JobsPortalUi.DARK_TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headLine = new JPanel();
        headLine.setOpaque(true);
        headLine.setBackground(JobsPortalUi.PRIMARY_PURPLE);
        headLine.setPreferredSize(new Dimension(28, 3));
        headLine.setMaximumSize(new Dimension(28, 3));
        headLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerStack.add(h);
        headerStack.add(Box.createVerticalStrut(4));
        headerStack.add(headLine);

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);

        JLabel v1 = new JLabel(String.valueOf(stats.getTotalPositions()), SwingConstants.CENTER);
        JLabel v2 = new JLabel(String.valueOf(stats.getOpenPositions()), SwingConstants.CENTER);
        JLabel v3 = new JLabel(String.valueOf(stats.getPendingApplications()), SwingConstants.CENTER);

        grid.add(statBox(v1, "Total Positions"));
        grid.add(statBox(v2, "Open Positions"));
        grid.add(statBox(v3, "Pending Reviews"));

        inner.add(headerStack, BorderLayout.NORTH);
        inner.add(grid, BorderLayout.CENTER);
        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    private JPanel statBox(JLabel valueLabel, String caption) {
        JobsPortalUi.RoundedSurface box = new JobsPortalUi.RoundedSurface(
                14, JobsPortalUi.LAVENDER, JobsPortalUi.LIGHT_PURPLE_BORDER, 1f, false, new GridBagLayout());
        box.setBorder(new EmptyBorder(12, 12, 14, 12));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        valueLabel.setFont(UI.moFontBold(30));
        valueLabel.setForeground(JobsPortalUi.PRIMARY_PURPLE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(UI.moFontBold(12));
        cap.setForeground(JobsPortalUi.MUTED_TEXT);
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
}
