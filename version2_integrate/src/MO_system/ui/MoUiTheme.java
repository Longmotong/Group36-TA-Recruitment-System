package com.mojobsystem.ui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Strict grayscale MO console (reference: monochrome panels, black CTAs, light gray chrome).
 */
public final class MoUiTheme {
    public static final Color PAGE_BG = new Color(0xF5F5F5);
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = new Color(0xE0E0E0);
    public static final Color BORDER_SOFT = new Color(0xEEEEEE);
    public static final Color ICON_BOX_BG = new Color(0xEEEEEE);
    public static final Color TEXT_PRIMARY = new Color(0x000000);
    public static final Color TEXT_SECONDARY = new Color(0x666666);
    public static final Color TEXT_MUTED = new Color(0x999999);

    public static final Color BTN_BLACK = new Color(0x000000);
    public static final Color BTN_BLACK_HOVER = new Color(0x333333);

    /** Restrained accent — primary actions (e.g. Create job) without rainbow UI. */
    public static final Color ACCENT_PRIMARY = new Color(0x2563EB);
    public static final Color ACCENT_PRIMARY_HOVER = new Color(0x1D4ED8);

    /** Teal CTA — pairs with Application Review module styling on the dashboard. */
    public static final Color ACCENT_TEAL = new Color(0x0D9488);
    public static final Color ACCENT_TEAL_HOVER = new Color(0x0F766E);

    public static final int CONTENT_MAX_W = 1040;
    public static final int GUTTER = 40;

    /**
     * Full-page content padding (Application Review list + Job Management list) — keep in sync everywhere.
     */
    public static final int PAGE_INSET_TOP = 24;
    public static final int PAGE_INSET_X = 24;
    public static final int PAGE_INSET_BOTTOM = 28;

    private MoUiTheme() {
    }

    /**
     * Ghost outline "← Back to Home" — single definition shared with Application Review and Job Management.
     */
    public static JButton createBackToHomeButton(Runnable action) {
        String text = "← Back to Home";
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(0x374151));
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        b.setPreferredSize(new Dimension(Math.max(160, text.length() * 9 + 30), 44));
        b.addActionListener(e -> action.run());
        return b;
    }

    public static void styleRoundedCard(JPanel panel, int arc) {
        panel.setBackground(SURFACE);
        panel.setOpaque(true);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: " + arc);
    }

    public static void stylePrimaryButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 0"
                        + "; focusWidth: 0"
                        + "; background: " + asHex(BTN_BLACK)
                        + "; foreground: #ffffff"
                        + "; hoverBackground: " + asHex(BTN_BLACK_HOVER));
    }

    /** Blue primary CTA — use sparingly (one per screen). */
    public static void styleAccentPrimaryButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 0"
                        + "; focusWidth: 0"
                        + "; background: " + asHex(ACCENT_PRIMARY)
                        + "; foreground: #ffffff"
                        + "; hoverBackground: " + asHex(ACCENT_PRIMARY_HOVER));
    }

    /** Solid teal primary CTA — white text (e.g. Application Review dashboard card). */
    public static void styleTealPrimaryButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 0"
                        + "; focusWidth: 0"
                        + "; background: " + asHex(ACCENT_TEAL)
                        + "; foreground: #ffffff"
                        + "; hoverBackground: " + asHex(ACCENT_TEAL_HOVER));
    }

    /** Blue outline — matches grey/red outline buttons for a consistent action row. */
    public static void styleAccentOutlineButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 1"
                        + "; background: #ffffff"
                        + "; foreground: " + asHex(ACCENT_PRIMARY)
                        + "; borderColor: #BFDBFE"
                        + "; hoverBackground: #EFF6FF"
                        + "; focusWidth: 0");
    }

    public static void styleOutlineButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 1"
                        + "; background: #ffffff"
                        + "; foreground: " + asHex(TEXT_SECONDARY)
                        + "; borderColor: " + asHex(BORDER)
                        + "; hoverBackground: " + asHex(PAGE_BG)
                        + "; focusWidth: 0");
    }

    /** Red outline — delete / destructive secondary actions. */
    public static void styleDangerOutlineButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 1"
                        + "; background: #ffffff"
                        + "; foreground: #b91c1c"
                        + "; borderColor: #fecaca"
                        + "; hoverBackground: #fef2f2"
                        + "; focusWidth: 0");
    }

    /**
     * Read-only skill tag: full pill shape without a square {@code LineBorder}
     * (which clashes with {@code arc} on the fill).
     */
    public static void styleSkillPill(JLabel label) {
        label.setOpaque(true);
        label.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999"
                        + "; borderWidth: 0"
                        + "; background: #f3f4f6"
                        + "; foreground: #374151");
    }

    public static void styleNavPill(JButton btn, boolean selected, int arc) {
        if (selected) {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: " + arc
                            + "; borderWidth: 0"
                            + "; background: " + asHex(BORDER_SOFT)
                            + "; foreground: " + asHex(TEXT_PRIMARY)
                            + "; focusWidth: 0");
        } else {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: " + arc
                            + "; borderWidth: 0"
                            + "; background: #ffffff"
                            + "; foreground: " + asHex(TEXT_SECONDARY)
                            + "; hoverBackground: " + asHex(PAGE_BG)
                            + "; focusWidth: 0");
        }
    }

    private static String asHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
