package TA_Job_Application_Module;

import javax.swing.*;
import java.awt.*;

/**
 * UI utilities for TA Job Application Module.
 * Self-contained, no external dependencies.
 */
public class TAUI {

    private static final Palette PALETTE = new Palette();

    private TAUI() {}

    public static Palette palette() {
        return PALETTE;
    }

    public static Font fontPlain(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    public static Font fontBold(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(fontPlain(13));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(0x111111));
        field.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1, true));
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
        table.setFont(fontPlain(13));

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(fontBold(13));
        table.getTableHeader().setBackground(new Color(0xF3F4F6));
        table.getTableHeader().setForeground(Color.BLACK);
    }

    public static void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBackground(new Color(0x10, 0x82, 0x45)); // Green
        button.setForeground(Color.WHITE);
        button.setFont(fontBold(13));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(0x374151));
        button.setFont(fontBold(13));
        button.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1, true));
    }

    public static void styleCard(JPanel card) {
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true));
    }

    public static final class Palette {
        public Color appBg() {
            return new Color(0xF5F5F5);
        }

        public Color cardBg() {
            return Color.WHITE;
        }

        public Color border() {
            return new Color(0xE0E0E0);
        }

        public Color borderStrong() {
            return new Color(0xD1D5DB);
        }

        public Color text() {
            return new Color(0x111111);
        }

        public Color textSecondary() {
            return new Color(0x6B7280);
        }

        public Color textMuted() {
            return new Color(0x9CA3AF);
        }

        public Color primary() {
            return new Color(0x10, 0x82, 0x45);
        }

        public Color primaryHover() {
            return new Color(0x16, 0x34, 0x58);
        }

        public Color secondary() {
            return new Color(0x6B7280);
        }

        public Color success() {
            return new Color(0x10, 0x82, 0x45);
        }

        public Color successBg() {
            return new Color(0xDCFCE7);
        }

        public Color warning() {
            return new Color(0xD97706);
        }

        public Color warningBg() {
            return new Color(0xFEF3C7);
        }

        public Color danger() {
            return new Color(0xDC2626);
        }

        public Color dangerBg() {
            return new Color(0xFEE2E2);
        }

        public Color info() {
            return new Color(0x2563EB);
        }

        public Color infoBg() {
            return new Color(0xDBEAFE);
        }
    }
}
