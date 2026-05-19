package com.mojobsystem.ui.job;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Row actions on My Jobs: neutral (1–2), blue edit (3), green/grey switch (4), red delete (5).
 */
public final class JobActionIcons {
    private static final int S = 18;
    private static final Color INK = new Color(0x4B5563);
    public static final Color EDIT_BLUE = new Color(0x2563EB);
    public static final Color DELETE_RED = new Color(0xDC2626);

    private JobActionIcons() {
    }

    public static Icon viewJob() {
        return new BaseIcon() {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawOval(x + 2, y + 5, 14, 8);
                g2.fillOval(x + 7, y + 7, 4, 4);
            }
        };
    }

    public static Icon allocatedTas() {
        return new BaseIcon() {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawOval(x + 2, y + 3, 5, 5);
                g2.drawOval(x + 10, y + 3, 5, 5);
                g2.drawArc(x + 1, y + 9, 7, 6, 200, 140);
                g2.drawArc(x + 9, y + 9, 7, 6, 200, 140);
            }
        };
    }

    /** (3) Edit — blue pencil */
    public static Icon editJobBlue() {
        return new BaseIcon(EDIT_BLUE) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 12, y + 3, x + 4, y + 11);
                g2.drawLine(x + 3, y + 14, x + 6, y + 11);
            }
        };
    }

    /**
     * (4) Open/closed switch: green + thumb right when {@code open}, grey + thumb left when closed/draft.
     */
    public static Icon toggleSwitch(boolean open) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color track = open ? new Color(0x22C55E) : new Color(0xCBD5E1);
                g2.setColor(track);
                int trackW = 18;
                int trackH = 12;
                int trackX = x + 1;
                int trackY = y + 3;
                g2.fillRoundRect(trackX, trackY, trackW, trackH, 6, 6);
                g2.setColor(Color.WHITE);
                int thumb = 8;
                int thumbY = y + 5; // center in trackH(12)
                int thumbX = open ? x + 10 : x + 2;
                g2.fillOval(thumbX, thumbY, thumb, thumb);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 22;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    /** (5) Delete — red trash */
    public static Icon deleteJobRed() {
        return new BaseIcon(DELETE_RED) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRect(x + 5, y + 6, 8, 9);
                g2.drawLine(x + 3, y + 6, x + 15, y + 6);
                g2.drawLine(x + 7, y + 4, x + 11, y + 4);
            }
        };
    }

    private abstract static class BaseIcon implements Icon {
        private final Color ink;

        BaseIcon() {
            this(INK);
        }

        BaseIcon(Color ink) {
            this.ink = ink;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ink);
            draw(g2, x, y);
            g2.dispose();
        }

        abstract void draw(Graphics2D g2, int x, int y);

        @Override
        public int getIconWidth() {
            return S;
        }

        @Override
        public int getIconHeight() {
            return S;
        }
    }
}
