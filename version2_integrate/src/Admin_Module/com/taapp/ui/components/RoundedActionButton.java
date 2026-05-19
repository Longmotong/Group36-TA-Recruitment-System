package com.taapp.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Flat rounded CTA without FlatLaf — matches the team's black / blue primary buttons (arc ~10).
 */
public final class RoundedActionButton extends JButton {
    public enum Scheme {
        /** Black primary — same family as MO module CTAs. */
        PRIMARY_BLACK(new Color(0x000000), new Color(0x333333)),
        /** Blue accent — e.g. Export CSV in allocation / workload. */
        ACCENT_BLUE(new Color(0x2563EB), new Color(0x1D4ED8));

        final Color base;
        final Color hover;

        Scheme(Color base, Color hover) {
            this.base = base;
            this.hover = hover;
        }
    }

    private final int arc;
    private final Scheme scheme;
    private boolean hover;

    public RoundedActionButton(String text, Scheme scheme) {
        this(text, scheme, 10);
    }

    public RoundedActionButton(String text, Scheme scheme, int arc) {
        super(text);
        this.scheme = scheme;
        this.arc = arc;
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 28));
        g2.fillRoundRect(0, 2, getWidth() - 1, getHeight() - 3, arc, arc);
        Color bg = hover ? scheme.hover : scheme.base;
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 4, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 4, arc, arc);
        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        return new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc).contains(x, y);
    }
}
