package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shared chrome helpers for profile-module screens. Centralises the
 * "← Back" link + icon tile + title + underline + subtitle header that
 * matches the TA Job Application Module's "Available Positions" page
 * (see {@code TA_Job_Application_Module.pages.jobs.Page_Jobs#buildHeader}).
 */
public final class PortalChrome {
    private PortalChrome() {}

    /** Soft top→bottom lavender gradient page background with corner dot grid. */
    public static JPanel pageSurface() {
        return new GradientBackground();
    }

    /** Wraps {@code body} in a vertical scroll pane that stays transparent over the page surface. */
    public static JScrollPane verticalScroll(JComponent body) {
        JScrollPane sp = new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    /** Purple ghost button used as the "← Back to …" affordance at the top of every screen. */
    public static JButton backLink(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(PortalUi.PRIMARY_PURPLE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(0, 0, 6, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(PortalUi.DEEP_PURPLE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(PortalUi.PRIMARY_PURPLE);
            }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    /**
     * Header strip: optional back link, lavender icon tile, big title with a
     * 40×3 purple underline accent, and a muted subtitle. The optional
     * {@code rightActions} panel is placed flush right (e.g. the "Edit
     * Profile" outline button on {@code Page_Profile}).
     */
    public static JPanel header(Icon glyph,
                                String title,
                                String subtitle,
                                String backText,
                                Runnable backAction,
                                JComponent rightActions) {
        JPanel host = new JPanel();
        host.setOpaque(false);
        host.setLayout(new BoxLayout(host, BoxLayout.Y_AXIS));
        host.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (backText != null && backAction != null) {
            JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            backRow.setOpaque(false);
            backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            backRow.add(backLink(backText, backAction));
            host.add(backRow);
        }

        JPanel titleRow = new JPanel(new BorderLayout(12, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setBorder(new EmptyBorder(2, 0, 12, 0));

        JPanel titleCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleCluster.setOpaque(false);

        if (glyph != null) {
            JPanel iconTile = PortalUi.wrapRoundedInner(new JLabel(glyph), 14,
                    PortalUi.LAVENDER, PortalUi.LIGHT_PURPLE_BORDER, 1f, false,
                    new Insets(8, 8, 8, 8));
            iconTile.setPreferredSize(new Dimension(46, 46));
            iconTile.setMinimumSize(new Dimension(46, 46));
            iconTile.setMaximumSize(new Dimension(46, 46));
            titleCluster.add(iconTile);
        }

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(PortalUi.DARK_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleStack.add(titleLabel);
        titleStack.add(Box.createVerticalStrut(4));

        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PortalUi.PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(40, 3));
        underline.setMinimumSize(new Dimension(40, 3));
        underline.setMaximumSize(new Dimension(40, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleStack.add(underline);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            sub.setForeground(PortalUi.MUTED_TEXT);
            sub.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleStack.add(Box.createVerticalStrut(4));
            titleStack.add(sub);
        }

        titleCluster.add(titleStack);
        titleRow.add(titleCluster, BorderLayout.WEST);

        if (rightActions != null) {
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(rightActions);
            titleRow.add(rightPanel, BorderLayout.EAST);
        }

        host.add(titleRow);
        return host;
    }

    /** Convenience: header without right-side actions. */
    public static JPanel header(Icon glyph, String title, String subtitle,
                                String backText, Runnable backAction) {
        return header(glyph, title, subtitle, backText, backAction, null);
    }

    /**
     * White rounded card with the same soft-purple shadow + lavender stroke as
     * {@code PortalUi.RoundedSurface}. {@code padding} is applied as the
     * card's inner content padding.
     */
    public static PortalUi.RoundedSurface card(Insets padding) {
        PortalUi.RoundedSurface card = new PortalUi.RoundedSurface(
                18, Color.WHITE, PortalUi.LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        card.setBorder(new EmptyBorder(padding));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    /** Lavender pill that serves as the section-icon background on edit screens. */
    public static JPanel iconBadge(Icon glyph, int size) {
        JPanel tile = PortalUi.wrapRoundedInner(new JLabel(glyph), 12,
                PortalUi.LAVENDER, PortalUi.LIGHT_PURPLE_BORDER, 1f, false,
                new Insets(6, 6, 6, 6));
        tile.setPreferredSize(new Dimension(size, size));
        tile.setMinimumSize(new Dimension(size, size));
        tile.setMaximumSize(new Dimension(size, size));
        return tile;
    }

    /**
     * Compact "Edit XYZ" outline button used in {@link ui.screens.ProfileScreen}'s
     * card headers. Renders the same outline-purple visual as
     * {@code PortalUi.OutlinePurpleButton} but with smaller padding.
     */
    public static JButton compactOutline(String text, Runnable action) {
        PortalUi.OutlinePurpleButton b = PortalUi.outlineButton(text, new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.addActionListener(e -> action.run());
        return b;
    }

    private static final class GradientBackground extends JPanel {
        GradientBackground() {
            setOpaque(true);
            setBackground(PortalUi.PAGE_BG);
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
