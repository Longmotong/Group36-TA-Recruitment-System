package ui.screens;

import ui.AppLayout;

import ui.AppFrame;
import ui.TaTopNavigationPanel;
import ui.Theme;

import ui.PortalUi;
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * TA dashboard — re-skinned to match the TA Job Application Module's
 * Soft-Neo lavender / purple visual language. The layout still mirrors the
 * Admin/MO dashboard structure (welcome banner → module cards → quick
 * overview), but every chrome element uses {@link PortalUi} primitives.
 */
public final class DashboardScreen extends JPanel {
    private final AppFrame app;

    private final JLabel completionValue = new JLabel("0%", SwingConstants.CENTER);
    private final JLabel cvStatusValue = new JLabel("—", SwingConstants.CENTER);
    private final JLabel appsValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel welcomeMessage = new JLabel();

    private final TaTopNavigationPanel topNav;

    public DashboardScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.BG);

        topNav = TaTopNavigationPanel.forAppFrame(app, TaTopNavigationPanel.Active.HOME);
        add(topNav, BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildCenter() {
        JPanel host = new GradientBackgroundPanel();
        host.setLayout(new BorderLayout());
        host.add(AppLayout.wrapCentered(buildDashboardRoot()), BorderLayout.CENTER);
        return host;
    }

    private JPanel buildDashboardRoot() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(14, 0, 16, 0));

        root.add(welcomeBanner());
        root.add(Box.createVerticalStrut(12));

        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 300));
        final int iconPx = 24;
        cards.add(moduleCard(
                PortalUi.userIcon(PortalUi.PRIMARY_PURPLE, iconPx),
                "Profile Module",
                "Manage your TA profile",
                new String[]{
                        "Edit personal information and academic details",
                        "Record skills and proficiency for matching",
                        "Upload and maintain your CV"
                },
                "Go to Profile",
                () -> app.showRoute(AppFrame.ROUTE_PROFILE),
                false,
                null
        ));
        cards.add(moduleCard(
                PortalUi.briefcaseGlyph(PortalUi.PRIMARY_PURPLE, iconPx),
                "Job Application Module",
                "Browse positions and track applications",
                new String[]{
                        "Explore open TA positions and requirements",
                        "Submit and manage your applications",
                        "Monitor status updates and outcomes"
                },
                "Browse Jobs",
                () -> app.openJobApplicationPortal("jobs"),
                true,
                () -> app.openJobApplicationPortal("applications")
        ));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        root.add(quickOverviewPanel());
        root.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, Integer.MAX_VALUE));
        return root;
    }

    private JPanel welcomeBanner() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(2, 12, 2, 8));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(PortalUi.briefcaseGlyph(PortalUi.PRIMARY_PURPLE, 24));
        JPanel iconTile = PortalUi.wrapRoundedInner(iconLabel, 14, PortalUi.LAVENDER,
                PortalUi.LIGHT_PURPLE_BORDER, 1f, false, new Insets(8, 8, 8, 8));
        iconTile.setPreferredSize(new Dimension(46, 46));
        iconTile.setMaximumSize(new Dimension(46, 46));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TA Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(PortalUi.DARK_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PortalUi.PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(40, 3));
        underline.setMaximumSize(new Dimension(40, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(6));
        titleStack.add(underline);

        titleRow.add(iconTile);
        titleRow.add(titleStack);

        welcomeMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomeMessage.setBorder(new EmptyBorder(8, 4, 0, 0));

        inner.add(titleRow);
        inner.add(welcomeMessage);

        PortalUi.RoundedSurface banner = new PortalUi.RoundedSurface(
                18, Color.WHITE, PortalUi.LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 120));
        banner.setBorder(new EmptyBorder(12, 18, 14, 18));
        banner.add(inner, BorderLayout.CENTER);
        return banner;
    }

    private JPanel moduleCard(Icon moduleIcon,
                              String title,
                              String intro,
                              String[] bulletItems,
                              String primaryCta,
                              Runnable primaryAction,
                              boolean withSecondary,
                              Runnable secondaryAction) {
        PortalUi.RoundedSurface card = new PortalUi.RoundedSurface(
                18, Color.WHITE, PortalUi.LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);

        JPanel iconWrap = PortalUi.wrapRoundedInner(new JLabel(moduleIcon), 14,
                PortalUi.LAVENDER, PortalUi.LIGHT_PURPLE_BORDER, 1f, false,
                new Insets(10, 10, 10, 10));
        iconWrap.setPreferredSize(new Dimension(46, 46));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 22));
        t.setForeground(PortalUi.DARK_TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PortalUi.PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(28, 3));
        underline.setMaximumSize(new Dimension(28, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel introL = new JLabel("<html><div style='width:340px;line-height:1.35;font-family:sans-serif;color:#525252;font-size:13px'>"
                + intro + "</div></html>");
        introL.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(t);
        titles.add(Box.createVerticalStrut(4));
        titles.add(underline);
        titles.add(Box.createVerticalStrut(6));
        titles.add(introL);

        header.add(iconWrap, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel bulletPanel = new JPanel();
        bulletPanel.setLayout(new BoxLayout(bulletPanel, BoxLayout.Y_AXIS));
        bulletPanel.setOpaque(false);
        bulletPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        for (String b : bulletItems) {
            String bullet = "\u2022  " + b;
            JLabel line = new JLabel("<html><div style='width:340px;line-height:1.45;font-family:Segoe UI;font-size:14px;color:#4B5563'>"
                    + escapeHtml(bullet) + "</div></html>");
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            line.setBorder(new EmptyBorder(0, 0, 4, 0));
            bulletPanel.add(line);
        }

        JPanel upper = new JPanel(new BorderLayout(0, 0));
        upper.setOpaque(false);
        upper.add(header, BorderLayout.NORTH);
        upper.add(bulletPanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(10, 0, 0, 0));

        Font ctaFont = new Font("Segoe UI", Font.BOLD, 13);
        PortalUi.PurpleGradientButton primary = PortalUi.gradientButton(primaryCta, ctaFont);
        primary.addActionListener(e -> primaryAction.run());
        btnRow.add(primary);

        if (withSecondary && secondaryAction != null) {
            PortalUi.OutlinePurpleButton secondary = PortalUi.outlineButton("My Applications", ctaFont);
            secondary.setIcon(PortalUi.fileTextIcon(PortalUi.DEEP_PURPLE, 16));
            secondary.setIconTextGap(8);
            secondary.addActionListener(e -> secondaryAction.run());
            btnRow.add(secondary);
        }

        card.add(upper, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quickOverviewPanel() {
        PortalUi.RoundedSurface outer = new PortalUi.RoundedSurface(
                18, Color.WHITE, PortalUi.LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setBorder(new EmptyBorder(12, 18, 14, 18));
        outer.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 180));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        head.setOpaque(false);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel listIcon = new JLabel(PortalUi.listLinesIcon(PortalUi.PRIMARY_PURPLE, 18));
        head.add(listIcon);

        JPanel headStack = new JPanel();
        headStack.setOpaque(false);
        headStack.setLayout(new BoxLayout(headStack, BoxLayout.Y_AXIS));
        JLabel h = new JLabel("Quick Overview");
        h.setFont(new Font("Segoe UI", Font.BOLD, 16));
        h.setForeground(PortalUi.DARK_TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PortalUi.PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(26, 3));
        underline.setMaximumSize(new Dimension(26, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        headStack.add(h);
        headStack.add(Box.createVerticalStrut(3));
        headStack.add(underline);
        head.add(headStack);
        inner.add(head);
        inner.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        completionValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        completionValue.setForeground(PortalUi.PRIMARY_PURPLE);
        cvStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        cvStatusValue.setForeground(PortalUi.PRIMARY_PURPLE);
        appsValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appsValue.setForeground(PortalUi.PRIMARY_PURPLE);

        grid.add(statBox(completionValue, "Profile Completion"));
        grid.add(statBox(cvStatusValue, "CV Upload Status"));
        grid.add(statBox(appsValue, "Applications Submitted"));

        inner.add(grid);
        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    private JPanel statBox(JLabel valueLabel, String caption) {
        PortalUi.RoundedSurface box = new PortalUi.RoundedSurface(
                14, PortalUi.LAVENDER, PortalUi.LIGHT_PURPLE_BORDER, 1f, false, new GridBagLayout());
        box.setBorder(new EmptyBorder(8, 12, 10, 12));

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);

        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cap.setForeground(PortalUi.MUTED_TEXT);
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);

        column.add(Box.createVerticalGlue());
        column.add(valueLabel);
        column.add(Box.createVerticalStrut(4));
        column.add(cap);
        column.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        box.add(column, gbc);
        return box;
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

    public void refresh() {
        topNav.refresh(TaTopNavigationPanel.Active.HOME);

        int pct = app.profile().profileCompletionPercent;
        completionValue.setText(pct + "%");

        String cvText = app.profile().cv != null && !app.profile().cv.fileName.isBlank() ? "Uploaded" : "Not uploaded";
        cvStatusValue.setText(cvText);
        cvStatusValue.setFont(new Font("Segoe UI", Font.BOLD, cvText.length() > 8 ? 16 : 24));

        appsValue.setText(String.valueOf(app.profile().numberOfApplications));

        String name = app.profile().fullName;
        boolean hasName = name != null && !name.isBlank();
        boolean hasCv = app.profile().cv != null && !app.profile().cv.fileName.isBlank();
        boolean hasSkills = app.profile().skills != null && !app.profile().skills.isEmpty();

        String msg;
        if (!hasName && !hasCv && !hasSkills) {
            msg = "Complete your profile to get started with TA applications.";
        } else if (hasName && !hasCv && !hasSkills) {
            msg = "Your basic info is ready! Add skills and upload your CV to apply for TA positions.";
        } else if (hasName && hasCv && !hasSkills) {
            msg = "Almost there! Add your skills to make your profile stand out.";
        } else if (hasName && hasSkills && !hasCv) {
            msg = "Looking good! Upload your CV to start applying for TA positions.";
        } else if (hasName && hasCv && hasSkills) {
            if (app.profile().numberOfApplications > 0) {
                msg = "Your profile is complete! Track your " + app.profile().numberOfApplications + " application(s) below.";
            } else {
                msg = "Your profile is complete! Browse TA positions and start applying.";
            }
        } else {
            msg = "Welcome! Pick a module below to manage your profile or explore TA positions.";
        }

        welcomeMessage.setText("<html><div style='width:720px;line-height:1.5'><span style='color:#525252;font-size:14px;font-family:sans-serif'>"
                + escapeHtml(msg) + "</span></div></html>");

        revalidate();
        repaint();
    }

    /**
     * Soft top→bottom lavender gradient that mirrors {@code Page_Jobs}'s page
     * background, with the same subtle dot-grid in the upper-right corner.
     */
    private static final class GradientBackgroundPanel extends JPanel {
        GradientBackgroundPanel() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint bg = new GradientPaint(
                    0, 0, new Color(253, 252, 255),
                    0, getHeight(), new Color(248, 246, 255));
            g2.setPaint(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(109, 77, 235, 18));
            int startX = Math.max(0, getWidth() - 240);
            for (int x = startX; x < getWidth() - 18; x += 10) {
                for (int y = 0; y < 150; y += 10) {
                    g2.fillOval(x, y, 2, 2);
                }
            }
            g2.dispose();
        }
    }
}
