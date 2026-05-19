package profile_module.ui.dashboard;

import profile_module.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Local dashboard visuals (replaces {@code com.taapp.ui.*} for standalone profile module).
 */
public final class DashboardUi {
    private DashboardUi() {}

    private static final Palette PALETTE = new Palette();

    public static Palette palette() {
        return PALETTE;
    }

    public static Font moFontBold(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    public static Font moFontPlain(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    public static void styleSecondaryButton(JButton b) {
        b.setFont(moFontPlain(13));
        b.setForeground(Theme.SECONDARY_FG);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(10, 16, 10, 16)));
    }

    public static final class Palette {
        public Color appBg() {
            return Theme.BG;
        }

        public Color border() {
            return Theme.BORDER;
        }
    }

    public static final class AppLayout {
        public static final int CONTENT_MAX_W = 1080;

        public static JPanel wrapCentered(JComponent inner) {
            JPanel outer = new JPanel(new BorderLayout());
            outer.setOpaque(false);
            outer.add(inner, BorderLayout.CENTER);
            return outer;
        }
    }

    public static final class DashboardModuleIcons {
        public static Icon userProfile(int px) {
            return new SimpleIcon(px, (g2, size) -> {
                int s = Math.max(4, size - 8);
                int cx = size / 2;
                int headR = Math.max(3, s / 5);
                g2.setColor(new Color(0x1A1A1A));
                g2.fillOval(cx - headR, headR, headR * 2, headR * 2);
                int bodyW = headR * 3;
                int bodyH = Math.max(4, size / 3);
                g2.fillRoundRect(cx - bodyW / 2, headR * 2 + 2, bodyW, bodyH, 8, 8);
            });
        }

        public static Icon briefcase(int px) {
            return new SimpleIcon(px, (g2, size) -> {
                g2.setColor(new Color(0x404040));
                int m = size / 8;
                int w = size - m * 2;
                int h = size / 2;
                int x = m;
                int y = size / 2 - h / 2 + 2;
                g2.fillRoundRect(x, y, w, h, 6, 6);
                int handleW = w / 3;
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x + (w - handleW) / 2, y - 6, handleW, 8, 4, 4);
            });
        }
    }

    @FunctionalInterface
    private interface IconPainter {
        void paint(Graphics2D g2, int px);
    }

    private static final class SimpleIcon implements Icon {
        private final int size;
        private final IconPainter painter;

        SimpleIcon(int size, IconPainter painter) {
            this.size = size;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            painter.paint(g2, size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    public static final class RoundedActionButton extends JButton {
        public enum Scheme {
            PRIMARY_BLACK
        }

        private final Scheme scheme;
        private boolean hover;

        public RoundedActionButton(String text, Scheme scheme) {
            super(text);
            this.scheme = scheme;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color fill = Theme.PRIMARY_BTN;
            if (scheme == Scheme.PRIMARY_BLACK) {
                fill = hover ? Theme.PRIMARY_BTN_HOVER : Theme.PRIMARY_BTN;
            }
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));
            g2.setFont(getFont());
            g2.setColor(Color.WHITE);
            var metrics = g2.getFontMetrics();
            String t = getText();
            int tw = metrics.stringWidth(t);
            int tx = (w - tw) / 2;
            int ty = (h - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(t, tx, ty);
            g2.dispose();
        }
    }
}
