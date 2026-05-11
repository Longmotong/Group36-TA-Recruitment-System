package com.mojobsystem.ui;

import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Simple monochrome glyphs for dashboard module cards (reference UI).
 */
public final class MoGrayIcons {
    private static final Color INK = new Color(0x666666);
    private static final Color PAPER = new Color(0xF5F5F5);

    private MoGrayIcons() {
    }

    public static ImageIcon book(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(PAPER);
        g.fillRoundRect(4, 3, size - 8, size - 6, 6, 6);
        g.setColor(new Color(0xE0E0E0));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(4, 3, size - 8, size - 6, 6, 6);
        g.setColor(INK);
        g.fillRoundRect(7, 6, size - 14, size - 12, 3, 3);
        g.setColor(PAPER);
        g.fillRect(9, 9, size - 18, 3);
        g.fillRect(9, 14, size - 18, 2);
        g.fillRect(9, 18, size - 18, 2);
        g.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon clipboard(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(PAPER);
        g.fillRoundRect(6, 8, size - 12, size - 11, 4, 4);
        g.setColor(new Color(0xE0E0E0));
        g.drawRoundRect(6, 8, size - 12, size - 11, 4, 4);
        g.setColor(INK);
        g.fillRoundRect(size / 2 - 6, 4, 12, 6, 3, 3);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(10, 14, size - 10, 14);
        g.drawLine(10, 19, size - 10, 19);
        g.drawLine(10, 24, size - 14, 24);
        g.dispose();
        return new ImageIcon(img);
    }
}
