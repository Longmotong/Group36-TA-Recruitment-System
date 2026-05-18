package profile_module.ui.components;

import profile_module.ui.Theme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Filled primary — Admin {@code RoundedActionButton.Scheme.PRIMARY_BLACK}, not TA Job Application indigo. */
public final class PrimaryButton extends JButton {
    private boolean hover = false;

    public PrimaryButton(String text) {
        super(text);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(180, 44));

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
        Color base = Theme.PRIMARY_BTN;
        Color hi = Theme.PRIMARY_BTN_HOVER;
        g2.setColor(isEnabled() ? (hover ? hi : base) : new Color(0xC9, 0xCF, 0xD6));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}

