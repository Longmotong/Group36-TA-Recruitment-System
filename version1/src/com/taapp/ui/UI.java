package com.taapp.ui;

import java.awt.Color;
import java.awt.Font;

public final class UI {
    private UI() {}

    public static Palette palette() {
        return Palette.INSTANCE;
    }

    public static Font fontPlain(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    public static Font fontMedium(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    public static final class Palette {
        private static final Palette INSTANCE = new Palette();

        private final Color appBg = new Color(0xF9FAFB);
        private final Color cardBg = Color.WHITE;
        private final Color border = new Color(0xE5E7EB);
        private final Color borderStrong = new Color(0xD1D5DB);
        private final Color text = new Color(0x111827);
        private final Color textMuted = new Color(0x6B7280);
        private final Color textSoft = new Color(0x374151);
        private final Color primary = new Color(0x1A1A1A);

        public Color appBg() { return appBg; }
        public Color cardBg() { return cardBg; }
        public Color border() { return border; }
        public Color borderStrong() { return borderStrong; }
        public Color text() { return text; }
        public Color textMuted() { return textMuted; }
        public Color textSoft() { return textSoft; }
        public Color primary() { return primary; }
    }
}

