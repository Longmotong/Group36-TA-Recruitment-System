package com.mojobsystem.ui;

import com.taapp.ui.UI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Objects;

/**
 * Top bar aligned with {@link com.taapp.ui.TopNavigation}: white bar, bottom border, sans-serif typography,
 * bold black active link / gray inactive, text Logout.
 */
public final class NavigationPanel {
    public enum Tab {
        HOME,
        JOB_MANAGEMENT,
        APPLICATION_REVIEW
    }

    public record Actions(Runnable home, Runnable jobManagement, Runnable applicationReview, Runnable logout) {
        public Actions {
            Objects.requireNonNull(home);
            Objects.requireNonNull(jobManagement);
            Objects.requireNonNull(applicationReview);
            Objects.requireNonNull(logout);
        }
    }

    private NavigationPanel() {
    }

    /**
     * @param accountHint optional line after nav links (e.g. current MO {@code userId}); blank to omit
     */
    public static JPanel create(Tab active, Actions actions, String accountHint) {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        nav.setBackground(java.awt.Color.WHITE);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UI.palette().border()),
                new EmptyBorder(0, 8, 0, 8)
        ));
        nav.setPreferredSize(new Dimension(10, 60));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel title = new JLabel("MO System");
        title.setFont(UI.moFontBold(18));
        title.setForeground(UI.palette().text());
        brand.add(title);
        nav.add(brand);

        nav.add(spacer(18));

        addNavButton(nav, "Home", active == Tab.HOME, actions.home());
        addNavButton(nav, "Job Management", active == Tab.JOB_MANAGEMENT, actions.jobManagement());
        addNavButton(nav, "Application Review", active == Tab.APPLICATION_REVIEW, actions.applicationReview());

        nav.add(Box.createHorizontalStrut(28));

        if (accountHint != null && !accountHint.isBlank()) {
            JLabel hint = new JLabel(accountHint);
            hint.setFont(UI.moFontPlain(13));
            hint.setForeground(UI.palette().textSecondary());
            nav.add(hint);
        }

        JButton logout = new JButton("Logout");
        logout.setFont(UI.moFontPlain(14));
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setOpaque(false);
        logout.setBackground(java.awt.Color.WHITE);
        logout.setBorder(new EmptyBorder(6, 10, 6, 10));
        logout.setForeground(UI.palette().textMuted());
        logout.setHorizontalAlignment(SwingConstants.RIGHT);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> actions.logout().run());
        nav.add(logout);

        return nav;
    }

    public static JPanel create(Tab active, Actions actions) {
        return create(active, actions, null);
    }

    private static void addNavButton(JPanel nav, String label, boolean active, Runnable action) {
        JButton b = new JButton(label);
        b.setFont(active ? UI.moFontBold(14) : UI.moFontPlain(14));
        b.setForeground(active ? UI.palette().text() : UI.palette().textSecondary());
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(6, 10, 6, 10));
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        nav.add(b);
    }

    private static JPanel spacer(int width) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(width, 1));
        return p;
    }
}
