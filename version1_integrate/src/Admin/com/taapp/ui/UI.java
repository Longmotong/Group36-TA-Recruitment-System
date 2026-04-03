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

        private final Color appBg = new Color(0xF7F8FC);
        private final Color cardBg = Color.WHITE;
        private final Color border = new Color(0xE4E7EC);
        private final Color borderStrong = new Color(0xCBD5E1);
        private final Color text = new Color(0x0F172A);
        private final Color textMuted = new Color(0x64748B);
        private final Color textSoft = new Color(0x334155);

        private final Color primary = new Color(0x2563EB);
        private final Color success = new Color(0x16A34A);
        private final Color warning = new Color(0xD97706);
        private final Color danger = new Color(0xDC2626);
        private final Color violet = new Color(0x7C3AED);

        public Color appBg() { return appBg; }
        public Color cardBg() { return cardBg; }
        public Color border() { return border; }
        public Color borderStrong() { return borderStrong; }
        public Color text() { return text; }
        public Color textMuted() { return textMuted; }
        public Color textSoft() { return textSoft; }

        public Color primary() { return primary; }
        public Color success() { return success; }
        public Color warning() { return warning; }
        public Color danger() { return danger; }
        public Color violet() { return violet; }
    }
}
