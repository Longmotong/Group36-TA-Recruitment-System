package com.example.tasystem.ui.components;

import com.example.tasystem.ui.Theme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class Chip extends JButton {
    private final Color bg;
    private final Color fg;

    public Chip(String text, Color bg, Color fg, boolean closable) {
        super(closable ? text + "   ×" : text);
        this.bg = bg;
        this.fg = fg;
        setFont(Theme.SMALL);
        setForeground(fg);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(closable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        setEnabled(closable);
        setPreferredSize(new Dimension(getPreferredSize().width + 12, 28));
    }

    public static Chip blue(String text, boolean closable) {
        return new Chip(text, Theme.CHIP_BLUE_BG, Theme.CHIP_BLUE_FG, closable);
    }

    public static Chip green(String text, boolean closable) {
        return new Chip(text, Theme.CHIP_GREEN_BG, Theme.CHIP_GREEN_FG, closable);
    }

    public static Chip purple(String text, boolean closable) {
        return new Chip(text, Theme.CHIP_PURPLE_BG, Theme.CHIP_PURPLE_FG, closable);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 999, 999);
        g2.dispose();
        super.paintComponent(g);
    }
}

