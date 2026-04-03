package com.taapp.ui;

import java.awt.Color;
import java.awt.Font;

public final class UI {
    private static final Palette PALETTE = new Palette();

    private UI() {}

    public static Palette palette() {
        return PALETTE;
    }

    public static Font fontPlain(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font fontMedium(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    public static final class Palette {
        public Color appBg() { return new Color(0xF5F7FB); }
        public Color border() { return new Color(0xE5E7EB); }
        public Color borderStrong() { return new Color(0xCBD5E1); }
        public Color text() { return new Color(0x111827); }
        public Color textMuted() { return new Color(0x6B7280); }
        public Color textSoft() { return new Color(0x374151); }
        public Color cardBg() { return Color.WHITE; }
    }
}
