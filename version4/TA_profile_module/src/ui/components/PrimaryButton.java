package ui.components;

import ui.PortalUi;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Filled primary CTA — same purple gradient language as
 * {@link PortalUi.PurpleGradientButton} (arc 16, horizontal gradient, soft shadow + highlight).
 */
public final class PrimaryButton extends JButton {
    private static final int ARC = 16;
    private boolean hover = false;

    public PrimaryButton(String text) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
        Dimension p = computePreferred();
        setPreferredSize(new Dimension(Math.max(180, p.width), Math.max(44, p.height)));
        setMinimumSize(getPreferredSize());
    }

    private Dimension computePreferred() {
        JLabel scratch = new JLabel();
        FontMetrics fm = scratch.getFontMetrics(getFont());
        String t = getText() != null ? getText() : "";
        int tw = fm.stringWidth(t);
        int padH = 24 + 24 + 16;
        int h = 13 + 13 + fm.getHeight() + 2;
        return new Dimension(Math.max(padH + tw, 96), Math.max(h, 36));
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (getFont() != null) {
            Dimension p = computePreferred();
            setPreferredSize(new Dimension(Math.max(180, p.width), Math.max(44, p.height)));
            setMinimumSize(getPreferredSize());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        if (!isEnabled()) {
            g2.setColor(new Color(0xC9, 0xCF, 0xD6));
            g2.fillRoundRect(0, 0, w, h, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
            return;
        }

        Color left = hover ? new Color(0x7C, 0x5C, 0xFF) : PortalUi.PRIMARY_PURPLE;
        Color right = hover ? new Color(0x3F, 0x2B, 0xB8) : PortalUi.DEEP_PURPLE;

        g2.setColor(new Color(79, 53, 217, hover ? 48 : 28));
        g2.fillRoundRect(0, 3, w, h - 1, ARC, ARC);

        g2.setPaint(new java.awt.GradientPaint(0, h / 2f, left, w, h / 2f, right));
        g2.fillRoundRect(0, 0, w, h - 2, ARC, ARC);

        g2.setPaint(new java.awt.GradientPaint(0, 0,
                new Color(255, 255, 255, 42), 0, Math.max(1, h / 2),
                new Color(255, 255, 255, 0)));
        g2.fillRoundRect(1, 1, w - 2, Math.max(1, h / 2), ARC, ARC);

        g2.dispose();
        super.paintComponent(g);
    }
}
