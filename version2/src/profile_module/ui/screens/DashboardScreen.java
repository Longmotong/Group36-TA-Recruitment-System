package profile_module.ui.screens;

import profile_module.ui.AppFrame;
import profile_module.ui.Theme;
import profile_module.ui.Ui;
import profile_module.ui.components.PrimaryButton;
import profile_module.ui.components.SecondaryButton;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public final class DashboardScreen extends JPanel {
    private final AppFrame app;

    private final JLabel completionValue = Ui.h3("0%");
    private final JLabel cvStatusValue = Ui.h3("—");
    private final JLabel appsValue = Ui.h3("0");

    public DashboardScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(buildNavBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    public void refresh() {
        int pct = app.profile().profileCompletionPercent;
        completionValue.setText(pct + "%");
        cvStatusValue.setText(app.profile().cv != null && !app.profile().cv.fileName.isBlank() ? "Uploaded" : "Not Uploaded");
        appsValue.setText(String.valueOf(app.profile().numberOfApplications));
        revalidate();
        repaint();
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(Ui.empty(10, 18, 10, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        left.setOpaque(false);
        JLabel brand = new JLabel("✉  TA System");
        brand.setFont(Theme.BODY_BOLD.deriveFont(14f));
        brand.setForeground(Theme.TEXT);
        left.add(brand);

        JButton home = navLink("Home");
        JButton profile = navLink("Profile Module");
        JButton job = navLink("Job Application Module");
        home.addActionListener(e -> app.showRoute(AppFrame.ROUTE_DASHBOARD));
        profile.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));

        left.add(home);
        left.add(profile);
        left.add(job);
        nav.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);
        JButton logout = navLink("Logout");
        right.add(logout);
        nav.add(right, BorderLayout.EAST);

        return nav;
    }

    private JButton navLink(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.BODY);
        b.setForeground(Theme.TEXT);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel buildBody() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(Ui.empty(22, 28, 28, 28));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Ui.h1("TA Dashboard"));
        titles.add(Box.createVerticalStrut(6));
        titles.add(Ui.muted("Welcome! Please select a function module to get started."));
        wrap.add(titles, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalStrut(26));
        center.add(buildModuleCardsRow());
        center.add(Box.createVerticalStrut(32));
        center.add(buildQuickStatus());

        wrap.add(center, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildModuleCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);

        row.add(buildModuleCard(
                "Profile Module",
                "Manage personal information, skills, and CV",
                "Go to Profile",
                () -> app.showRoute(AppFrame.ROUTE_PROFILE),
                false
        ));
        row.add(buildModuleCard(
                "Job Application Module",
                "Browse jobs, apply, and track application status",
                "Browse Jobs",
                () -> {},
                true
        ));
        return row;
    }

    private JPanel buildModuleCard(String title, String desc, String primaryText, Runnable primaryAction, boolean withSecondary) {
        Ui.RoundedPanel card = new Ui.RoundedPanel(14, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(18, 20, 18, 20));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        head.setOpaque(false);
        head.add(moduleIcon(title));
        head.add(Ui.h2(title));
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(6));
        content.add(Ui.muted(desc));
        content.add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        PrimaryButton primary = new PrimaryButton(primaryText);
        primary.setPreferredSize(new java.awt.Dimension(withSecondary ? 170 : 350, 42));
        primary.addActionListener(e -> primaryAction.run());
        buttons.add(primary);
        if (withSecondary) {
            SecondaryButton secondary = new SecondaryButton("My Applications");
            secondary.setPreferredSize(new java.awt.Dimension(170, 42));
            buttons.add(secondary);
        }

        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(buttons);
        content.add(Box.createVerticalGlue());
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel moduleIcon(String title) {
        Color iconBg = title.startsWith("Profile") ? new Color(0xEAF1FF) : new Color(0xDDF9EC);
        Color iconFg = title.startsWith("Profile") ? new Color(0x4D7CFE) : new Color(0x34C38F);
        String text = title.startsWith("Profile") ? "👤" : "👜";
        Ui.RoundedPanel icon = new Ui.RoundedPanel(8, iconBg, null, 0);
        icon.setLayout(new BorderLayout());
        icon.setPreferredSize(new java.awt.Dimension(56, 46));
        icon.setMinimumSize(new java.awt.Dimension(56, 46));
        icon.setMaximumSize(new java.awt.Dimension(56, 46));
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(iconFg);
        l.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        icon.add(l, BorderLayout.CENTER);
        return icon;
    }

    private JPanel buildQuickStatus() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel title = Ui.h3("Quick Status Overview");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(title);
        box.add(Box.createVerticalStrut(12));

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);

        grid.add(statusTile("Profile Completion", completionValue));
        grid.add(statusTile("CV Upload Status", cvStatusValue));
        grid.add(statusTile("Number of Applications", appsValue));

        box.add(grid);
        return box;
    }

    private JPanel statusTile(String label, JLabel value) {
        Ui.RoundedPanel tile = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(Ui.empty(16, 16, 16, 16));
        JLabel l = Ui.muted(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setFont(Theme.H2);
        tile.add(l);
        tile.add(Box.createVerticalStrut(6));
        tile.add(value);
        return tile;
    }
}

