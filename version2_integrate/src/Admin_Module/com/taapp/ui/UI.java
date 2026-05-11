package com.taapp.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Admin console styling aligned with the MO reference ({@code MoUiTheme}, {@code MoDashboardPanel}):
 * grayscale chrome, black primary CTAs, blue accent for exports, {@link Font#SANS_SERIF} typography.
 */
public final class UI {
    private static final Palette PALETTE = new Palette();

    private UI() {}

    public static Palette palette() {
        return PALETTE;
    }

    /** Prefer {@link #moFontPlain(int)} / {@link #moFontBold(int)} for new UI to match the MO app. */
    public static Font fontPlain(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    public static Font fontMedium(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    public static Font moFontPlain(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    public static Font moFontBold(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    /**
     * Ghost outline "← Back to Home" — same intent as {@code MoUiTheme.createBackToHomeButton}.
     */
    public static JButton createBackToHomeButton(Runnable action) {
        String text = "← Back to Home";
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(0x374151));
        b.setFont(moFontBold(14));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        b.setPreferredSize(new Dimension(Math.max(160, text.length() * 9 + 30), 44));
        b.addActionListener(e -> action.run());
        return b;
    }

    public static void styleBackButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(0x374151));
        button.setFont(moFontBold(14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        String text = button.getText() == null ? "" : button.getText();
        int width = Math.max(170, text.length() * 9 + 44);
        button.setPreferredSize(new Dimension(width, 44));
    }

    /** Black filled primary — matches MO module card CTAs (FlatLaf arc 10 approximated by {@code RoundedActionButton}). */
    public static void stylePrimaryButton(JButton button) {
        String name = button.getClass().getSimpleName();
        if ("RoundedActionButton".equals(name)) {
            return;
        }
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFont(moFontBold(13));
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
    }

    /** Blue primary — e.g. Export CSV (see {@code TaAllocationPanel}). */
    public static void styleAccentPrimaryButton(JButton button) {
        String name = button.getClass().getSimpleName();
        if ("RoundedActionButton".equals(name)) {
            return;
        }
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBackground(new Color(0x2563EB));
        button.setForeground(Color.WHITE);
        button.setFont(moFontBold(13));
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
    }

    /** Gray outline secondary — matches {@code MoUiTheme.styleOutlineButton} intent. */
    public static void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(0x666666));
        button.setFont(moFontBold(13));
        button.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true));
    }

    public static void styleField(JComponent c) {
        c.setFont(moFontPlain(13));
        c.setBackground(Color.WHITE);
        c.setForeground(new Color(0x111111));
        c.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1, true));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xE5E5E5));
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(0xE5E7EB));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(moFontPlain(13));

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(moFontBold(13));
        table.getTableHeader().setBackground(new Color(0xF3F4F6));
        table.getTableHeader().setForeground(Color.BLACK);
    }

    public static final class Palette {
        public Color appBg() {
            return new Color(0xF5F5F5);
        }

        public Color border() {
            return new Color(0xE0E0E0);
        }

        public Color borderStrong() {
            return new Color(0xD1D5DB);
        }

        public Color text() {
            return new Color(0x000000);
        }

        /** Secondary body — matches MO {@code TEXT_SECONDARY} (#666666). */
        public Color textSecondary() {
            return new Color(0x666666);
        }

        /** Muted captions — matches MO {@code TEXT_MUTED} (#999999). */
        public Color textMuted() {
            return new Color(0x999999);
        }

        public Color textSoft() {
            return new Color(0x666666);
        }

        public Color cardBg() {
            return Color.WHITE;
        }
    }
}
