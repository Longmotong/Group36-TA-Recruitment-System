package ui.components;

import ui.PortalUi;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.BasicStroke;
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
 * Outline secondary — matches {@link PortalUi.OutlinePurpleButton}: white fill, lavender hover,
 * {@link PortalUi#LIGHT_PURPLE_BORDER} stroke, primary / deep-purple label.
 */
public final class SecondaryButton extends JButton {
    private static final int ARC = 16;
    private boolean hover = false;

    public SecondaryButton(String text) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(PortalUi.PRIMARY_PURPLE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                setForeground(PortalUi.DEEP_PURPLE);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                setForeground(PortalUi.PRIMARY_PURPLE);
                repaint();
            }
        });
        Dimension p = computePreferred();
        setPreferredSize(new Dimension(Math.max(120, p.width), Math.max(44, p.height)));
        setMinimumSize(getPreferredSize());
    }

    private Dimension computePreferred() {
        JLabel scratch = new JLabel();
        FontMetrics fm = scratch.getFontMetrics(getFont());
        String t = getText() != null ? getText() : "";
        int tw = fm.stringWidth(t);
        int pad = 18 + 18 + 16;
        int h = 10 + 10 + fm.getHeight() + 4;
        return new Dimension(pad + tw, Math.max(h, 40));
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (getFont() != null) {
            Dimension p = computePreferred();
            setPreferredSize(new Dimension(Math.max(120, p.width), Math.max(44, p.height)));
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
            g2.setColor(new Color(0xF3, 0xF4, 0xF6));
            g2.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
            g2.setColor(new Color(0xE5, 0xE7, 0xEB));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
            return;
        }

        g2.setColor(hover ? PortalUi.LAVENDER : Color.WHITE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

        g2.setColor(PortalUi.LIGHT_PURPLE_BORDER);
        g2.setStroke(new BasicStroke(1.7f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

        g2.dispose();
        super.paintComponent(g);
    }
}
