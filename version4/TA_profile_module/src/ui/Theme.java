package ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Profile-module palette — re-skinned to match the TA Job Application Module's
 * Soft-Neo lavender / purple visual language (see
 * {@code TA_Job_Application_Module.pages.jobs.PortalUi}).
 *
 * <p>Names are kept identical to the previous greyscale theme so existing
 * screens compile unchanged; only the actual color values have shifted.</p>
 */
public final class Theme {
    private Theme() {}

    /** Page chrome — PortalUi.PAGE_BG (#FAFAFF). */
    public static final Color BG = new Color(0xFA, 0xFA, 0xFF);
    public static final Color SURFACE = Color.WHITE;
    /** Card / nav rules — PortalUi.LIGHT_PURPLE_BORDER. */
    public static final Color BORDER = new Color(0xDE, 0xD4, 0xFF);

    public static final Color TEXT = new Color(0x11, 0x10, 0x33);
    public static final Color MUTED = new Color(0x66, 0x70, 0x85);

    /** Re-purposed: previously navy → now PortalUi.PRIMARY_PURPLE (#6D4DEB). */
    public static final Color NAVY = new Color(0x6D, 0x4D, 0xEB);
    public static final Color NAVY_HOVER = new Color(0x4F, 0x35, 0xD9);

    /** Primary CTA — purple gradient (left/right paired with {@link #PRIMARY_BTN_HOVER}). */
    public static final Color PRIMARY_BTN = new Color(0x6D, 0x4D, 0xEB);
    public static final Color PRIMARY_BTN_HOVER = new Color(0x4F, 0x35, 0xD9);

    /** Nav / outline secondary labels. */
    public static final Color NAV_TEXT_SECONDARY = new Color(0x4B, 0x55, 0x63);
    public static final Color NAV_TEXT_MUTED = new Color(0x9C, 0xA3, 0xAF);
    /** Secondary button text — deep purple to harmonise with the primary CTA. */
    public static final Color SECONDARY_FG = new Color(0x4F, 0x35, 0xD9);

    public static final Color GREEN = new Color(0x10, 0xB9, 0x81);
    public static final Color GREEN_BG = new Color(0xEC, 0xFD, 0xF5);

    /** Legacy name — same lavender + purple ink as {@code Chip.blue} / TA portal chips. */
    public static final Color CHIP_BLUE_BG = new Color(0xF3, 0xEE, 0xFF);
    public static final Color CHIP_BLUE_FG = new Color(0x6D, 0x4D, 0xEB);
    public static final Color CHIP_GREEN_BG = new Color(0xD1, 0xFA, 0xE5);
    public static final Color CHIP_GREEN_FG = new Color(0x05, 0x83, 0x4F);
    /** Default chip — lavender + primary-purple ink. */
    public static final Color CHIP_PURPLE_BG = new Color(0xF3, 0xEE, 0xFF);
    public static final Color CHIP_PURPLE_FG = new Color(0x6D, 0x4D, 0xEB);

    /** Soft accent surfaces frequently re-used by the screens. */
    public static final Color LAVENDER = new Color(0xF3, 0xEE, 0xFF);
    public static final Color DEEP_PURPLE = new Color(0x4F, 0x35, 0xD9);

    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font H3 = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
}
