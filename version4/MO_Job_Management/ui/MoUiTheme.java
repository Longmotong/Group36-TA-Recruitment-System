package MO_system.ui;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * MO console colors and control chrome aligned with the TA Job Application portal ({@link JobsPortalUi}).
 */
public final class MoUiTheme {
    public static final Color PAGE_BG = JobsPortalUi.PAGE_BG;
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = JobsPortalUi.LIGHT_PURPLE_BORDER;
    public static final Color BORDER_SOFT = JobsPortalUi.LAVENDER;
    public static final Color ICON_BOX_BG = JobsPortalUi.LAVENDER;
    public static final Color TEXT_PRIMARY = JobsPortalUi.DARK_TEXT;
    public static final Color TEXT_SECONDARY = JobsPortalUi.MUTED_TEXT;
    public static final Color TEXT_MUTED = JobsPortalUi.TEXT_GRAY_LIGHT;

    /** Primary filled CTA — purple (same family as job portal). */
    public static final Color BTN_BLACK = JobsPortalUi.PRIMARY_PURPLE;
    public static final Color BTN_BLACK_HOVER = JobsPortalUi.DEEP_PURPLE;

    public static final Color ACCENT_PRIMARY = JobsPortalUi.PRIMARY_PURPLE;
    public static final Color ACCENT_PRIMARY_HOVER = JobsPortalUi.DEEP_PURPLE;

    public static final Color ACCENT_TEAL = JobsPortalUi.TEAL_ACCENT;
    public static final Color ACCENT_TEAL_HOVER = new Color(0x0E9B91);
    /** Light tint of {@link #ACCENT_TEAL} — table badges (same hue as Approve CTA). */
    public static final Color ACCENT_TEAL_SOFT_BG = new Color(0xE6F7F5);
    public static final Color ACCENT_TEAL_SOFT_BORDER = new Color(0xB8EBE6);
    public static final Color ACCENT_TEAL_SOFT_FG = new Color(0x0B7F76);
    /** My Jobs KPI row — same card layout; borders kept close to fill (subtle, not heavy). */
    public static final Color KPI_TOTAL_BG = new Color(0xF5F3FF);
    public static final Color KPI_TOTAL_BORDER = new Color(0xDDD6FE);
    public static final Color KPI_TOTAL_FG = new Color(0x7C3AED);
    public static final Color KPI_OPEN_BG = new Color(0xFDF2F8);
    public static final Color KPI_OPEN_BORDER = new Color(0xFBCFE8);
    public static final Color KPI_OPEN_FG = new Color(0xF472B6);
    public static final Color KPI_CLOSED_BG = new Color(0xEFF6FF);
    public static final Color KPI_CLOSED_BORDER = new Color(0xBFDBFE);
    public static final Color KPI_CLOSED_FG = new Color(0x3B82F6);

    public static final int CONTENT_MAX_W = 1040;
    public static final int GUTTER = 40;

    public static final int PAGE_INSET_TOP = 24;
    public static final int PAGE_INSET_X = 24;
    public static final int PAGE_INSET_BOTTOM = 28;

    private MoUiTheme() {
    }

    /** TA portal purple gradient primary CTA (custom-painted). */
    public static JobsPortalUi.PurpleGradientButton portalGradientPrimary(String text) {
        return JobsPortalUi.gradientButton(text, new Font(Font.SANS_SERIF, Font.BOLD, 14));
    }

    public static JobsPortalUi.PurpleGradientButton portalGradientPrimary(String text, Font font) {
        return JobsPortalUi.gradientButton(text, font);
    }

    /** Violet → coral gradient for AI-style actions (matches job portal smart-match CTA). */
    public static JobsPortalUi.PurpleGradientButton portalAiGradientPrimary(String text, Font font) {
        return JobsPortalUi.aiGradientButton(text, font, null);
    }

    /** TA portal white / purple-outline secondary button. */
    public static JobsPortalUi.OutlinePurpleButton portalOutlineSecondary(String text) {
        return JobsPortalUi.outlineButton(text, new Font(Font.SANS_SERIF, Font.BOLD, 13));
    }

    public static JobsPortalUi.OutlinePurpleButton portalOutlineSecondary(String text, Font font) {
        return JobsPortalUi.outlineButton(text, font);
    }

    /** Minimal purple text “Back” link in page headers. */
    public static void styleTextBackLink(JButton back) {
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setForeground(JobsPortalUi.PRIMARY_PURPLE);
    }

    /**
     * Page subtitle that paints on the text baseline so descenders (g, y, p) are never clipped by JLabel/BoxLayout.
     */
    public static JLabel createPageSubtitle(String text) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(getForeground());
                    FontMetrics fm = g2.getFontMetrics();
                    Insets ins = getInsets();
                    g2.drawString(getText(), ins.left, ins.top + fm.getAscent());
                } finally {
                    g2.dispose();
                }
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                Insets ins = getInsets();
                // macOS fonts often need a few extra px below reported descent for y/g/p.
                int textH = fm.getAscent() + fm.getDescent() + 6;
                return new Dimension(
                        fm.stringWidth(getText()) + ins.left + ins.right,
                        textH + ins.top + ins.bottom);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }
        };
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        label.setForeground(TEXT_SECONDARY);
        label.setOpaque(false);
        return label;
    }

    public static JButton createBackToHomeButton(Runnable action) {
        String text = "← Back to Home";
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(JobsPortalUi.PRIMARY_PURPLE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER),
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

    /**
     * Solid purple via FlatLAF — prefer {@link #portalGradientPrimary(String)} for main CTAs to match
     * TA portal gradient buttons exactly.
     */
    public static void stylePrimaryButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 0"
                        + "; focusWidth: 0"
                        + "; background: " + asHex(BTN_BLACK)
                        + "; foreground: #ffffff"
                        + "; hoverBackground: " + asHex(BTN_BLACK_HOVER));
    }

    public static void styleAccentPrimaryButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 0"
                        + "; focusWidth: 0"
                        + "; background: " + asHex(ACCENT_PRIMARY)
                        + "; foreground: #ffffff"
                        + "; hoverBackground: " + asHex(ACCENT_PRIMARY_HOVER));
    }

    public static void styleTealPrimaryButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 0"
                        + "; focusWidth: 0"
                        + "; background: " + asHex(ACCENT_TEAL)
                        + "; foreground: #ffffff"
                        + "; hoverBackground: " + asHex(ACCENT_TEAL_HOVER));
    }

    public static void styleAccentOutlineButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 1"
                        + "; background: #ffffff"
                        + "; foreground: " + asHex(ACCENT_PRIMARY)
                        + "; borderColor: " + asHex(JobsPortalUi.LIGHT_PURPLE_BORDER)
                        + "; hoverBackground: " + asHex(JobsPortalUi.LAVENDER)
                        + "; focusWidth: 0");
    }

    public static void styleOutlineButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 1"
                        + "; background: #ffffff"
                        + "; foreground: " + asHex(TEXT_SECONDARY)
                        + "; borderColor: " + asHex(BORDER)
                        + "; hoverBackground: " + asHex(JobsPortalUi.VIOLET_50)
                        + "; focusWidth: 0");
    }

    public static void styleDangerOutlineButton(JButton btn, int arc) {
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + arc
                        + "; borderWidth: 1"
                        + "; background: #ffffff"
                        + "; foreground: " + asHex(JobsPortalUi.CORAL_ACCENT)
                        + "; borderColor: #fecaca"
                        + "; hoverBackground: #fef2f2"
                        + "; focusWidth: 0");
    }

    public static void styleSkillPill(JLabel label) {
        label.setOpaque(true);
        label.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999"
                        + "; borderWidth: 0"
                        + "; background: " + asHex(JobsPortalUi.LAVENDER)
                        + "; foreground: " + asHex(TEXT_PRIMARY));
    }

    /** Nav pills on light surfaces (rare); MO shell top bar uses the shared purple portal component. */
    public static void styleNavPill(JButton btn, boolean selected, int arc) {
        if (selected) {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: " + arc
                            + "; borderWidth: 0"
                            + "; background: " + asHex(JobsPortalUi.LAVENDER)
                            + "; foreground: " + asHex(ACCENT_PRIMARY)
                            + "; focusWidth: 0");
        } else {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: " + arc
                            + "; borderWidth: 0"
                            + "; background: #ffffff"
                            + "; foreground: " + asHex(TEXT_SECONDARY)
                            + "; hoverBackground: " + asHex(JobsPortalUi.VIOLET_50)
                            + "; focusWidth: 0");
        }
    }

    private static String asHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
