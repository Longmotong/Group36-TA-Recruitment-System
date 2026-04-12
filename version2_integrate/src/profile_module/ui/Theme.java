package profile_module.ui;

import java.awt.Color;
import java.awt.Font;

public final class Theme {
    private Theme() {}

    /** Page chrome — aligned with Admin/MO {@code UI.palette().appBg()}. */
    public static final Color BG = new Color(0xF5, 0xF5, 0xF5);
    public static final Color SURFACE = Color.WHITE;
    /** Card / nav rules — aligned with Admin {@code UI.palette().border()}. */
    public static final Color BORDER = new Color(0xE0, 0xE0, 0xE0);

    public static final Color TEXT = new Color(0x10, 0x14, 0x1F);
    public static final Color MUTED = new Color(0x6B, 0x72, 0x80);

    public static final Color NAVY = new Color(0x0B, 0x12, 0x20);
    public static final Color NAVY_HOVER = new Color(0x12, 0x1A, 0x2B);

    /** Primary CTA — Admin {@code RoundedActionButton.Scheme.PRIMARY_BLACK}, not TA Job indigo. */
    public static final Color PRIMARY_BTN = new Color(0x00, 0x00, 0x00);
    public static final Color PRIMARY_BTN_HOVER = new Color(0x33, 0x33, 0x33);

    /** Nav / outline secondary labels — Admin palette secondary & muted. */
    public static final Color NAV_TEXT_SECONDARY = new Color(0x66, 0x66, 0x66);
    public static final Color NAV_TEXT_MUTED = new Color(0x99, 0x99, 0x99);
    /** Secondary button text — {@code UI.styleSecondaryButton}. */
    public static final Color SECONDARY_FG = new Color(0x66, 0x66, 0x66);

    public static final Color GREEN = new Color(0x2E, 0xC4, 0x78);
    public static final Color GREEN_BG = new Color(0xE8, 0xFB, 0xF1);

    public static final Color CHIP_BLUE_BG = new Color(0xE8, 0xF2, 0xFF);
    public static final Color CHIP_BLUE_FG = new Color(0x1F, 0x4F, 0xB6);
    public static final Color CHIP_GREEN_BG = new Color(0xE8, 0xFB, 0xF1);
    public static final Color CHIP_GREEN_FG = new Color(0x12, 0x7A, 0x43);
    public static final Color CHIP_PURPLE_BG = new Color(0xF0, 0xEA, 0xFF);
    public static final Color CHIP_PURPLE_FG = new Color(0x58, 0x2D, 0xB6);

    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font H3 = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
}

