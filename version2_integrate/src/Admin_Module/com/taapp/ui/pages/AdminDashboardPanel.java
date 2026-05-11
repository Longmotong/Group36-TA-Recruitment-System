package com.taapp.ui.pages;

import com.taapp.data.DataStore;
import com.taapp.model.Statistics;
import com.taapp.ui.AppLayout;
import com.taapp.ui.DashboardModuleIcons;
import com.taapp.ui.TopNavigation;
import com.taapp.ui.UI;
import com.taapp.ui.components.Page;
import com.taapp.ui.components.RoundedActionButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Admin home — layout and metrics pattern aligned with {@code MoDashboardPanel} (max width, gutters, cards).
 */
public class AdminDashboardPanel extends Page {
    public AdminDashboardPanel(Consumer<String> onNavigate) {
        super();
        Objects.requireNonNull(onNavigate);

        Statistics stats = DataStore.defaultStore().getStatistics();

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(28, 0, 40, 0));

        root.add(welcomeBanner());
        root.add(Box.createVerticalStrut(22));

        JPanel cards = new JPanel(new GridLayout(1, 2, 24, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 420));
        final int iconPx = 40;
        cards.add(moduleCard(
                DashboardModuleIcons.listManagement(iconPx),
                ModuleCardStyle.TA_WORKLOAD,
                "TA Workload Module",
                "Manage TA workload",
                new String[]{
                        "Create and update course information",
                        "Post and manage TA workload allocations",
                        "Maintain module requirements and descriptions"
                },
                "Go to TA Workload",
                () -> onNavigate.accept(TopNavigation.ROUTE_WORKLOAD)
        ));
        cards.add(moduleCard(
                DashboardModuleIcons.barChart(iconPx),
                ModuleCardStyle.STATISTICS,
                "Statistics Module",
                "View key statistics",
                new String[]{
                        "Browse departmental workload summaries",
                        "Review application and allocation trends",
                        "Track staffing performance outcomes"
                },
                "Go to Statistics",
                () -> onNavigate.accept(TopNavigation.ROUTE_STATISTICS)
        ));
        root.add(cards);
        root.add(Box.createVerticalStrut(22));

        root.add(quickOverviewPanel(stats));
        root.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, Integer.MAX_VALUE));

        content().add(AppLayout.wrapCentered(root), BorderLayout.CENTER);
    }

    private enum ModuleCardStyle {
        TA_WORKLOAD(new Color(0x1A1A1A), new Color(0xF0F0F0), new Color(0xCCCCCC)),
        STATISTICS(new Color(0x404040), new Color(0xF0F0F0), new Color(0xCCCCCC));

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
        banner.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 200));
        banner.setBackground(Color.WHITE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x1A1A1A)),
                        BorderFactory.createLineBorder(UI.palette().border())
                ),
                new EmptyBorder(22, 24, 24, 24)
        ));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(UI.moFontBold(30));
        title.setForeground(new Color(0x0F172A));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><div style='width:720px;line-height:1.5'>"
                + "<span style='color:#525252;font-size:15px;font-family:sans-serif'>Welcome back. Pick a module below to manage workload or review statistics.</span></div></html>");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        banner.add(title);
        banner.add(Box.createVerticalStrut(10));
        banner.add(sub);
        return banner;
    }

    private JPanel moduleCard(Icon moduleIcon,
                              ModuleCardStyle style,
                              String title,
                              String intro,
                              String[] bulletItems,
                              String cta,
                              Runnable onCta) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, style.accent),
                        BorderFactory.createLineBorder(UI.palette().border())
                ),
                new EmptyBorder(22, 22, 20, 24)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setBackground(style.iconBg);
        iconWrap.setBorder(BorderFactory.createLineBorder(style.iconBorder));
        iconWrap.setPreferredSize(new Dimension(56, 56));
        JLabel ic = new JLabel(moduleIcon, SwingConstants.CENTER);
        iconWrap.add(ic, BorderLayout.CENTER);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UI.moFontBold(18));
        t.setForeground(new Color(0x0F172A));
        JLabel introL = new JLabel("<html><div style='width:360px;line-height:1.45;font-family:sans-serif;color:#525252;font-size:14px'>" + intro + "</div></html>");
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
            line.setFont(UI.moFontPlain(14));
            line.setForeground(new Color(0x262626));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            line.setBorder(new EmptyBorder(0, 0, 6, 0));
            bulletPanel.add(line);
        }

        JPanel upper = new JPanel(new BorderLayout(0, 0));
        upper.setOpaque(false);
        upper.add(header, BorderLayout.NORTH);
        upper.add(bulletPanel, BorderLayout.CENTER);

        RoundedActionButton btn = new RoundedActionButton(cta, RoundedActionButton.Scheme.PRIMARY_BLACK);
        btn.setFont(UI.moFontBold(13));
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

    private JPanel quickOverviewPanel(Statistics stats) {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(true);
        outer.setBackground(Color.WHITE);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(20, 24, 24, 24)
        ));
        outer.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 320));

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x1A1A1A)),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JLabel h = new JLabel("Quick Overview");
        h.setFont(UI.moFontBold(17));
        h.setForeground(new Color(0x0F172A));
        head.add(h, BorderLayout.CENTER);
        outer.add(head);
        outer.add(Box.createVerticalStrut(18));

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);

        Color statBg = new Color(0xF5F5F5);
        Color statBorder = new Color(0xD4D4D4);
        Color statValue = new Color(0x111111);

        JLabel v1 = new JLabel(String.valueOf(stats.getTotalPositions()), SwingConstants.CENTER);
        JLabel v2 = new JLabel(String.valueOf(stats.getOpenPositions()), SwingConstants.CENTER);
        JLabel v3 = new JLabel(String.valueOf(stats.getPendingApplications()), SwingConstants.CENTER);

        grid.add(statBox(v1, "Total Positions", statBg, statBorder, statValue));
        grid.add(statBox(v2, "Open Positions", statBg, statBorder, statValue));
        grid.add(statBox(v3, "Pending Reviews", statBg, statBorder, statValue));

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

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        valueLabel.setFont(UI.moFontBold(34));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(UI.moFontBold(12));
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
}
