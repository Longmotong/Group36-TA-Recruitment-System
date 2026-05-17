package TA_Job_Application_Module.pages.jobs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

/**
 * Shared visuals for the TA portal jobs page (purple theme, rounded surfaces, gradient buttons).
 */
public final class JobsPortalUi {

    public static final Color PAGE_BG = new Color(249, 250, 251);
    public static final Color PURPLE_500 = new Color(139, 92, 246);
    public static final Color PURPLE_600 = new Color(124, 58, 237);
    public static final Color PURPLE_700 = new Color(109, 40, 217);
    public static final Color PURPLE_800 = new Color(91, 33, 182);
    public static final Color VIOLET_50 = new Color(245, 243, 255);
    public static final Color VIOLET_100 = new Color(237, 233, 254);
    public static final Color VIOLET_200 = new Color(221, 214, 254);
    public static final Color TEXT_GRAY = new Color(75, 85, 99);
    public static final Color TEXT_GRAY_LIGHT = new Color(107, 114, 128);

    private static final int BUTTON_ARC = 14;

    /** Shared padding so gradient / rose buttons align to the same height. */
    static final Insets BUTTON_CONTENT_PAD = new Insets(12, 22, 12, 22);

    private JobsPortalUi() {
    }

    private static int textWidthPx(Graphics2D g2, Font font, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(text, font, frc);
        return (int) Math.ceil(tl.getBounds().getWidth());
    }

    /** White card with soft shadow, rounded border, optional stroke. */
    public static final class RoundedSurface extends JPanel {

        private final int arc;
        private final Color fill;
        private final Color stroke;
        private final float strokeWidth;
        private final boolean shadow;

        public RoundedSurface(int arc, Color fill, Color stroke, float strokeWidth, boolean shadow, LayoutManager lm) {
            super(lm);
            this.arc = arc;
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int inset = shadow ? 2 : 0;
            int fw = w - 1 - inset;
            int fh = h - 1 - inset;
            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(3, 4, fw - 2, fh - 2, arc, arc);
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(2, 3, fw, fh, arc, arc);
            }
            int x = 0;
            int y = 0;
            g2.setColor(fill);
            g2.fillRoundRect(x, y, fw, fh, arc, arc);
            if (stroke != null && strokeWidth > 0) {
                g2.setStroke(new BasicStroke(strokeWidth));
                g2.setColor(stroke);
                g2.drawRoundRect(x, y, fw, fh, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel wrapRoundedInner(Component inner, int arc, Color fill, Color stroke,
                                          float strokeWidth, boolean shadow, Insets innerPadding) {
        RoundedSurface rs = new RoundedSurface(arc, fill, stroke, strokeWidth, shadow, new BorderLayout());
        JPanel pad = new JPanel(new BorderLayout());
        pad.setOpaque(false);
        pad.setBorder(innerPadding != null ? new EmptyBorder(innerPadding) : new EmptyBorder(2, 2, 2, 2));
        pad.add(inner, BorderLayout.CENTER);
        rs.add(pad, BorderLayout.CENTER);
        return rs;
    }

    /**
     * Inner uses {@link FlowLayout} so components like {@link JLabel} keep intrinsic width
     * (BorderLayout.CENTER can force truncation / ellipsis on some LAFs).
     */
    public static JPanel wrapRoundedInnerHug(Component inner, int arc, Color fill, Color stroke,
                                             float strokeWidth, boolean shadow, Insets innerPadding) {
        RoundedSurface rs = new RoundedSurface(arc, fill, stroke, strokeWidth, shadow, new BorderLayout());
        JPanel pad = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pad.setOpaque(false);
        pad.setBorder(innerPadding != null ? new EmptyBorder(innerPadding) : new EmptyBorder(2, 2, 2, 2));
        pad.add(inner);
        rs.add(pad, BorderLayout.CENTER);
        return rs;
    }

    /** Eight-point sparkle star (reference UI), vector-drawn. */
    public static Icon sparkleIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + size / 2;
                int cy = y + size / 2;
                int rLong = size / 2 - 1;
                int rShort = Math.max(2, rLong * 5 / 11);
                for (int i = 0; i < 8; i++) {
                    double ang = -Math.PI / 2 + i * Math.PI / 4;
                    int len = (i % 2 == 0) ? rLong : rShort;
                    int x2 = cx + (int) Math.round(Math.cos(ang) * len);
                    int y2 = cy + (int) Math.round(Math.sin(ang) * len);
                    g2.drawLine(cx, cy, x2, y2);
                }
                g2.fillOval(cx - 2, cy - 2, 5, 5);
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
        };
    }

    /** Down chevron for dropdowns (avoids missing font glyphs for ▾). */
    public static Icon chevronDownIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = Math.max(2, size / 5);
                int mid = x + size / 2;
                int top = y + pad + 1;
                int bot = y + size - pad - 1;
                g2.drawLine(x + pad, top, mid, bot);
                g2.drawLine(mid, bot, x + size - pad, top);
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
        };
    }

    public static PurpleGradientButton gradientButton(String text, Font font) {
        return gradientButton(text, font, null);
    }

    public static PurpleGradientButton gradientButton(String text, Font font, Icon leadingIcon) {
        PurpleGradientButton b = new PurpleGradientButton(text, leadingIcon);
        if (font != null) {
            b.setFont(font);
        }
        return b;
    }

    /** Rose accent — pairs with purple theme; same footprint as {@link PurpleGradientButton}. */
    public static HarmonyRoseButton roseHarmonyButton(String text, Font font) {
        HarmonyRoseButton b = new HarmonyRoseButton(text);
        if (font != null) {
            b.setFont(font);
        }
        return b;
    }

    /** Green outline pill for “Applied” on job cards (same padding as primary buttons). */
    public static AppliedGreenOutlineButton appliedOutlineButton(String text, Font font) {
        AppliedGreenOutlineButton b = new AppliedGreenOutlineButton(text);
        if (font != null) {
            b.setFont(font);
        }
        return b;
    }

    public static OutlinePurpleButton outlineButton(String text, Font font) {
        OutlinePurpleButton b = new OutlinePurpleButton(text);
        if (font != null) {
            b.setFont(font);
        }
        return b;
    }

    /**
     * Primary gradient pill button (white label). Hover darkens slightly.
     */
    public static final class PurpleGradientButton extends JButton {
        private boolean hover;
        private final Icon leadingIcon;

        public PurpleGradientButton(String text) {
            this(text, null);
        }

        public PurpleGradientButton(String text, Icon leadingIcon) {
            super(text);
            this.leadingIcon = leadingIcon;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(BUTTON_CONTENT_PAD));
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
        public void setBackground(Color bg) {
            // Gradient paints itself; ignore platform fills.
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color left = hover ? PURPLE_700 : new Color(167, 139, 250);
            Color right = hover ? PURPLE_800 : PURPLE_600;
            g2.setPaint(new GradientPaint(0, 0, left, w, h, right));
            g2.fillRoundRect(0, 0, w, h, BUTTON_ARC, BUTTON_ARC);

            Insets pad = BUTTON_CONTENT_PAD;
            Font font = getFont();
            FontMetrics fm = g2.getFontMetrics(font);
            String t = getText();
            int tw = textWidthPx(g2, font, t);
            int ih = leadingIcon != null ? leadingIcon.getIconHeight() : 0;
            int iw = leadingIcon != null ? leadingIcon.getIconWidth() : 0;
            int gap = leadingIcon != null ? 7 : 0;
            int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
            int contentW = iw + gap + tw;
            int inner = w - pad.left - pad.right;
            int groupStart = pad.left + Math.max(0, (inner - contentW) / 2);
            int startX = groupStart;
            if (leadingIcon != null) {
                int iy = (h - ih) / 2;
                leadingIcon.paintIcon(this, g2, startX, iy);
                startX += iw + gap;
            }
            g2.setColor(getForeground());
            g2.setFont(font);
            g2.drawString(t, startX, baseline);
            g2.dispose();
        }
    }

    /** Green-on-mint outline — clear “already applied” affordance. */
    public static final class AppliedGreenOutlineButton extends JButton {
        private boolean hover;

        public AppliedGreenOutlineButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(new Color(21, 128, 61));
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(BUTTON_CONTENT_PAD));
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
            int w = getWidth();
            int h = getHeight();
            g2.setColor(hover ? new Color(209, 250, 229) : new Color(236, 253, 245));
            g2.fillRoundRect(0, 0, w - 1, h - 1, BUTTON_ARC, BUTTON_ARC);
            g2.setColor(new Color(110, 231, 183));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, BUTTON_ARC, BUTTON_ARC);

            Font font = getFont();
            FontMetrics fm = g2.getFontMetrics(font);
            String t = getText();
            int tw = textWidthPx(g2, font, t);
            Insets pad = BUTTON_CONTENT_PAD;
            int inner = w - pad.left - pad.right;
            int tx = pad.left + Math.max(0, (inner - tw) / 2);
            int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(new Color(21, 128, 61));
            g2.setFont(font);
            g2.drawString(t, tx, baseline);
            g2.dispose();
        }
    }

    /** Soft rose filled button (same padding / arc as gradient primary). */
    public static final class HarmonyRoseButton extends JButton {
        private boolean hover;

        public HarmonyRoseButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(BUTTON_CONTENT_PAD));
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
        public void setBackground(Color bg) {
            // Painted manually
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color top = hover ? new Color(205, 96, 130) : new Color(225, 118, 154);
            Color bottom = hover ? new Color(184, 78, 112) : new Color(210, 98, 134);
            g2.setPaint(new GradientPaint(0, 0, top, w, h, bottom));
            g2.fillRoundRect(0, 0, w, h, BUTTON_ARC, BUTTON_ARC);

            Font font = getFont();
            FontMetrics fm = g2.getFontMetrics(font);
            String t = getText();
            int tw = textWidthPx(g2, font, t);
            Insets pad = BUTTON_CONTENT_PAD;
            int inner = w - pad.left - pad.right;
            int tx = pad.left + Math.max(0, (inner - tw) / 2);
            int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.setFont(font);
            g2.drawString(t, tx, baseline);
            g2.dispose();
        }
    }

    /** White fill, purple stroke and label; hover uses faint violet tint. */
    public static final class OutlinePurpleButton extends JButton {
        private boolean hover;

        public OutlinePurpleButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(PURPLE_600);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(BUTTON_CONTENT_PAD));
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
            int w = getWidth();
            int h = getHeight();
            g2.setColor(hover ? VIOLET_50 : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, BUTTON_ARC, BUTTON_ARC);
            g2.setColor(VIOLET_200);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, BUTTON_ARC, BUTTON_ARC);

            Font font = getFont();
            FontMetrics fm = g2.getFontMetrics(font);
            String t = getText();
            int tw = textWidthPx(g2, font, t);
            Insets pad = BUTTON_CONTENT_PAD;
            int inner = w - pad.left - pad.right;
            int tx = pad.left + Math.max(0, (inner - tw) / 2);
            int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(PURPLE_600);
            g2.setFont(font);
            g2.drawString(t, tx, baseline);
            g2.dispose();
        }
    }

    public static JPanel courseCodePill(String courseCode) {
        JLabel pill = new JLabel(courseCode != null ? courseCode : "");
        pill.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pill.setForeground(new Color(107, 70, 193));
        pill.setOpaque(false);
        Color pillBg = new Color(243, 240, 255);
        Color pillBorder = new Color(237, 233, 254);
        JPanel wrap = wrapRoundedInnerHug(pill, 8, pillBg, pillBorder, 1f, false, new Insets(6, 12, 6, 12));
        Dimension pref = wrap.getPreferredSize();
        wrap.setMaximumSize(new Dimension(pref.width, pref.height));
        wrap.setMinimumSize(new Dimension(pref.width, pref.height));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrap;
    }

    /** Small funnel icon for department filter row. */
    public static Icon funnelIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int m = size / 2;
                g2.drawLine(x + 4, y + 4, x + size - 4, y + 4);
                g2.drawLine(x + 6, y + m, x + size - 6, y + m);
                g2.drawLine(x + m, y + m, x + m, y + size - 3);
                g2.drawLine(x + m - 3, y + size - 3, x + m + 3, y + size - 3);
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
        };
    }

    /** Magnifying glass icon for search field. */
    public static Icon searchIcon(Color c, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int r = size / 2 - 3;
                g2.drawOval(x + 3, y + 3, r, r);
                g2.drawLine(x + 3 + r - 2, y + 3 + r - 2, x + size - 4, y + size - 4);
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
        };
    }

    public static Icon clockGlyph(Color c, int size) {
        return simpleCircleGlyph(c, size, (g2, ix, iy, s) -> {
            int cx = ix + s / 2;
            int cy = iy + s / 2;
            g2.drawOval(ix + 3, iy + 3, s - 6, s - 6);
            g2.drawLine(cx, cy, cx, iy + 5);
            g2.drawLine(cx, cy, ix + s - 5, cy);
        });
    }

    public static Icon calendarGlyph(Color c, int size) {
        return simpleCircleGlyph(c, size, (g2, ix, iy, s) -> {
            g2.drawRoundRect(ix + 4, iy + 5, s - 8, s - 9, 3, 3);
            g2.drawLine(ix + 4, iy + 9, ix + s - 4, iy + 9);
            g2.drawLine(ix + 7, iy + 3, ix + 7, iy + 7);
            g2.drawLine(ix + s - 7, iy + 3, ix + s - 7, iy + 7);
        });
    }

    public static Icon pinGlyph(Color c, int size) {
        return simpleCircleGlyph(c, size, (g2, ix, iy, s) -> {
            int cx = ix + s / 2;
            Polygon p = new Polygon();
            p.addPoint(cx, iy + 5);
            p.addPoint(ix + s - 6, iy + s / 2);
            p.addPoint(cx, iy + s - 5);
            p.addPoint(ix + 6, iy + s / 2);
            g2.drawPolygon(p);
            g2.fillOval(cx - 2, iy + s / 2 - 3, 5, 5);
        });
    }

    private interface GlyphPainter {
        void paint(Graphics2D g2, int x, int y, int size);
    }

    private static Icon simpleCircleGlyph(Color c, int size, GlyphPainter inner) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VIOLET_100);
                g2.fillOval(x + 1, y + 1, size - 2, size - 2);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                inner.paint(g2, x, y, size);
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
        };
    }

    /** Hybrid / work mode: light-blue circle + blue diamond + center dot (matches reference cards). */
    public static Icon hybridLocationIcon(int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(219, 234, 254));
                g2.fillOval(x + 1, y + 1, size - 2, size - 2);
                int cx = x + size / 2;
                int cy = y + size / 2;
                int r = Math.max(4, size / 3);
                Polygon d = new Polygon();
                d.addPoint(cx, cy - r);
                d.addPoint(cx + r, cy);
                d.addPoint(cx, cy + r);
                d.addPoint(cx - r, cy);
                g2.setColor(new Color(37, 99, 235));
                g2.setStroke(new BasicStroke(1.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolygon(d);
                g2.fillOval(cx - 2, cy - 2, 5, 5);
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
        };
    }

    /** Location row uses blue tones per mockup. */
    public static Icon pinGlyphBlue(int size) {
        Color blue = new Color(37, 99, 235);
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(219, 234, 254));
                g2.fillOval(x + 1, y + 1, size - 2, size - 2);
                g2.setColor(blue);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + size / 2;
                int s = size;
                int iy = y;
                Polygon p = new Polygon();
                p.addPoint(cx, iy + 5);
                p.addPoint(x + s - 6, iy + s / 2);
                p.addPoint(cx, iy + s - 5);
                p.addPoint(x + 6, iy + s / 2);
                g2.drawPolygon(p);
                g2.fillOval(cx - 2, iy + s / 2 - 3, 5, 5);
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
        };
    }
}
