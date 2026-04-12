package com.taapp.ui;

import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Monochrome dashboard glyphs for module cards (TA / MO / Admin), drawn without external assets.
 */
public final class DashboardModuleIcons {

    private static final Color INK = new Color(0x374151);
    private static final Color INK_SOFT = new Color(0x6B7280);
    private static final Color PAPER = new Color(0xF3F4F6);
    private static final Color LINE = new Color(0xE5E7EB);

    private DashboardModuleIcons() {
    }

    /**
     * Dark tile with three horizontal bars — job / workload / list management (matches MO Job Management reference).
     */
    public static ImageIcon listManagement(int size) {
        BufferedImage img = clear(size);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float pad = size * 0.14f;
        float w = size - 2 * pad;
        float h = size - 2 * pad;
        float arc = size * 0.12f;
        g.setColor(new Color(0x4B5563));
        g.fill(new RoundRectangle2D.Float(pad, pad, w, h, arc, arc));
        g.setColor(new Color(0xF9FAFB));
        float barW = w * 0.55f;
        float barH = Math.max(2f, size * 0.06f);
        float x = pad + (w - barW) / 2f;
        float y0 = pad + h * 0.32f;
        float gap = h * 0.14f;
        g.fillRoundRect((int) x, (int) y0, (int) barW, (int) barH, 2, 2);
        g.fillRoundRect((int) x, (int) (y0 + gap), (int) barW, (int) barH, 2, 2);
        g.fillRoundRect((int) x, (int) (y0 + 2 * gap), (int) barW, (int) barH, 2, 2);
        g.dispose();
        return new ImageIcon(img);
    }

    /**
     * Document with folded corner — application review / forms.
     */
    public static ImageIcon documentReview(int size) {
        BufferedImage img = clear(size);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float pad = size * 0.12f;
        float w = size - 2 * pad - size * 0.08f;
        float h = size - 2 * pad;
        float fold = size * 0.14f;
        float x0 = pad;
        float y0 = pad;

        g.setColor(PAPER);
        Path2D body = new Path2D.Float();
        body.moveTo(x0, y0);
        body.lineTo(x0 + w - fold, y0);
        body.lineTo(x0 + w, y0 + fold);
        body.lineTo(x0 + w, y0 + h);
        body.lineTo(x0, y0 + h);
        body.closePath();
        g.fill(body);

        g.setColor(LINE);
        g.setStroke(new BasicStroke(1f));
        g.draw(body);

        Path2D foldTri = new Path2D.Float();
        foldTri.moveTo(x0 + w - fold, y0);
        foldTri.lineTo(x0 + w, y0 + fold);
        foldTri.lineTo(x0 + w - fold, y0 + fold);
        foldTri.closePath();
        g.setColor(new Color(0xD1D5DB));
        g.fill(foldTri);
        g.setColor(LINE);
        g.draw(foldTri);

        g.setColor(INK_SOFT);
        float mx = x0 + w * 0.12f;
        float my = y0 + fold + size * 0.06f;
        g.fillRoundRect((int) mx, (int) my, (int) (w * 0.76f), (int) (size * 0.07f), 2, 2);
        g.setStroke(new BasicStroke(1.2f));
        float ly = my + size * 0.12f;
        g.drawLine((int) mx, (int) ly, (int) (mx + w * 0.65f), (int) ly);
        g.drawLine((int) mx, (int) (ly + size * 0.07f), (int) (mx + w * 0.55f), (int) (ly + size * 0.07f));
        g.drawLine((int) mx, (int) (ly + 2 * size * 0.07f), (int) (mx + w * 0.6f), (int) (ly + 2 * size * 0.07f));
        g.dispose();
        return new ImageIcon(img);
    }

    /** Person / profile module. */
    public static ImageIcon userProfile(int size) {
        BufferedImage img = clear(size);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float cx = size / 2f;
        float headR = size * 0.14f;
        g.setColor(INK);
        g.fillOval((int) (cx - headR), (int) (size * 0.18f), (int) (2 * headR), (int) (2 * headR));
        Path2D shoulders = new Path2D.Float();
        float bw = size * 0.42f;
        float top = size * 0.45f;
        shoulders.moveTo(cx - bw / 2f, size - pad(size));
        shoulders.quadTo(cx - bw * 0.35f, top, cx, top);
        shoulders.quadTo(cx + bw * 0.35f, top, cx + bw / 2f, size - pad(size));
        shoulders.lineTo(cx - bw / 2f, size - pad(size));
        shoulders.closePath();
        g.fill(shoulders);
        g.dispose();
        return new ImageIcon(img);
    }

    /** Briefcase — job applications / hiring. */
    public static ImageIcon briefcase(int size) {
        BufferedImage img = clear(size);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float pad = size * 0.18f;
        float w = size - 2 * pad;
        float h = size * 0.38f;
        float y = size * 0.38f;
        g.setColor(INK);
        g.fillRoundRect((int) pad, (int) y, (int) w, (int) h, 6, 6);
        float handleW = w * 0.35f;
        float hx = pad + (w - handleW) / 2f;
        g.setColor(PAPER);
        g.fillRoundRect((int) hx, (int) (y - size * 0.08f), (int) handleW, (int) (size * 0.12f), 4, 4);
        g.setColor(INK);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect((int) hx, (int) (y - size * 0.08f), (int) handleW, (int) (size * 0.12f), 4, 4);
        g.setColor(new Color(0xF9FAFB));
        g.setStroke(new BasicStroke(1.5f));
        float lx = pad + w * 0.22f;
        g.drawLine((int) lx, (int) (y + h * 0.45f), (int) (lx + w * 0.56f), (int) (y + h * 0.45f));
        g.dispose();
        return new ImageIcon(img);
    }

    /** Simple grid — roster / workload allocation. */
    public static ImageIcon workloadGrid(int size) {
        BufferedImage img = clear(size);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float pad = size * 0.16f;
        float cell = (size - 2 * pad) / 3f;
        g.setColor(INK);
        g.setStroke(new BasicStroke(1.5f));
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                float x = pad + c * cell;
                float y = pad + r * cell;
                g.drawRoundRect((int) x + 1, (int) y + 1, (int) cell - 2, (int) cell - 2, 3, 3);
            }
        }
        g.setColor(INK_SOFT);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if ((r + c) % 2 == 0) {
                    float x = pad + c * cell + cell * 0.2f;
                    float y = pad + r * cell + cell * 0.25f;
                    g.fillRoundRect((int) x, (int) y, (int) (cell * 0.6f), (int) (cell * 0.35f), 2, 2);
                }
            }
        }
        g.dispose();
        return new ImageIcon(img);
    }

    /** Bar chart — statistics. */
    public static ImageIcon barChart(int size) {
        BufferedImage img = clear(size);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float base = size * 0.72f;
        float pad = size * 0.2f;
        float bw = size * 0.14f;
        float[] heights = {size * 0.22f, size * 0.38f, size * 0.28f};
        float x = pad;
        g.setColor(INK);
        for (float h : heights) {
            g.fillRoundRect((int) x, (int) (base - h), (int) bw, (int) h, 3, 3);
            x += bw + size * 0.06f;
        }
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(LINE);
        g.drawLine((int) (pad * 0.5f), (int) base, (int) (size - pad * 0.5f), (int) base);
        g.dispose();
        return new ImageIcon(img);
    }

    private static float pad(int size) {
        return size * 0.12f;
    }

    private static BufferedImage clear(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        return img;
    }
}
