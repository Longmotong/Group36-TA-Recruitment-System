package com.example.tasystem.ui;

import java.awt.Color;
import java.awt.Font;

public final class Theme {
    private Theme() {}

    public static final Color BG = new Color(0xF6, 0xF7, 0xF9);
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = new Color(0xE6, 0xE8, 0xEC);

    public static final Color TEXT = new Color(0x10, 0x14, 0x1F);
    public static final Color MUTED = new Color(0x6B, 0x72, 0x80);

    public static final Color NAVY = new Color(0x0B, 0x12, 0x20);
    public static final Color NAVY_HOVER = new Color(0x12, 0x1A, 0x2B);

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

