package Admin_Module.com.taapp.ui;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Admin console styling aligned with the TA Job Application portal
 * ({@link JobsPortalUi}): lavender page chrome, purple CTAs, soft borders,
 * {@link Font#SANS_SERIF} typography.
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
        b.setForeground(JobsPortalUi.DEEP_PURPLE);
        b.setFont(moFontBold(14));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));
        b.setPreferredSize(new Dimension(Math.max(160, text.length() * 9 + 30), 44));
        b.addActionListener(e -> action.run());
        return b;
    }

    public static void styleBackButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(JobsPortalUi.DEEP_PURPLE);
        button.setFont(moFontBold(14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER),
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
        button.setBackground(JobsPortalUi.PRIMARY_PURPLE);
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
        button.setBackground(JobsPortalUi.BLUE_ACCENT);
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
        button.setForeground(JobsPortalUi.DEEP_PURPLE);
        button.setFont(moFontBold(13));
        button.setBorder(BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true));
    }

    public static void styleField(JComponent c) {
        c.setFont(moFontPlain(13));
        c.setBackground(Color.WHITE);
        c.setForeground(JobsPortalUi.DARK_TEXT);
        c.setBorder(BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowVerticalLines(false);
        table.setGridColor(JobsPortalUi.LIGHT_PURPLE_BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(JobsPortalUi.DARK_TEXT);
        table.setSelectionBackground(JobsPortalUi.LAVENDER);
        table.setSelectionForeground(JobsPortalUi.DARK_TEXT);
        table.setFont(moFontPlain(13));

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(moFontBold(13));
        table.getTableHeader().setBackground(JobsPortalUi.LAVENDER);
        table.getTableHeader().setForeground(JobsPortalUi.DARK_TEXT);
    }

    public static final class Palette {
        public Color appBg() {
            return JobsPortalUi.PAGE_BG;
        }

        public Color border() {
            return JobsPortalUi.LIGHT_PURPLE_BORDER;
        }

        public Color borderStrong() {
            return JobsPortalUi.LIGHT_PURPLE_BORDER;
        }

        public Color text() {
            return JobsPortalUi.DARK_TEXT;
        }

        public Color textSecondary() {
            return JobsPortalUi.MUTED_TEXT;
        }

        public Color textMuted() {
            return JobsPortalUi.MUTED_TEXT;
        }

        public Color textSoft() {
            return JobsPortalUi.TEXT_GRAY;
        }

        public Color cardBg() {
            return Color.WHITE;
        }
    }
}
