package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

/**
 * TA portal — Modern Soft-Neo: pastel lavender/pink-purple, light gradients, low contrast (see ACCENT_SOFT / LAVENDER_LIGHT).
 */
public final class PortalUi {

    /** Shared modern purple palette for the Job Applications / Available Positions pages. */
    public static final Color PAGE_BG = new Color(0xFAFAFF);
    public static final Color PRIMARY_PURPLE = new Color(0x6D4DEB);
    public static final Color DEEP_PURPLE = new Color(0x4F35D9);
    public static final Color LAVENDER = new Color(0xF3EEFF);
    public static final Color LIGHT_PURPLE_BORDER = new Color(0xDED4FF);
    public static final Color DARK_TEXT = new Color(0x111033);
    public static final Color MUTED_TEXT = new Color(0x667085);
    public static final Color BLUE_ACCENT = new Color(0x2F80ED);
    public static final Color TEAL_ACCENT = new Color(0x12B3A8);
    public static final Color CORAL_ACCENT = new Color(0xFF5C5C);

    /** Compatibility aliases used by existing pages. */
    public static final Color ACCENT_SOFT = PRIMARY_PURPLE;
    public static final Color LAVENDER_LIGHT = LAVENDER;
    public static final Color PURPLE_500 = new Color(0x8064F2);
    public static final Color PURPLE_600 = PRIMARY_PURPLE;
    public static final Color PURPLE_700 = DEEP_PURPLE;
    public static final Color PURPLE_800 = new Color(0x3F2BB8);
    public static final Color VIOLET_50 = new Color(0xFDFCFF);
    public static final Color VIOLET_100 = LAVENDER;
    public static final Color VIOLET_200 = LIGHT_PURPLE_BORDER;
    public static final Color TEXT_GRAY = new Color(0x4B5563);
    public static final Color TEXT_GRAY_LIGHT = MUTED_TEXT;

    private static final int BUTTON_ARC = 16;

    /** Shared padding so gradient / rose buttons align to a modern 54–58 px visual height. */
    static final Insets BUTTON_CONTENT_PAD = new Insets(13, 24, 13, 24);

    private PortalUi() {
    }

    /** White label on saturated fills — subtle shadow improves legibility on pastel gradients. */
    private static void drawWhiteButtonLabel(Graphics2D g2, Font font, String t, int startX, int baseline) {
        g2.setFont(font);
        g2.setColor(new Color(0, 0, 0, 52));
        g2.drawString(t, startX + 1, baseline + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(t, startX, baseline);
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
                g2.setColor(new Color(79, 53, 217, 16));
                g2.fillRoundRect(3, 5, fw - 2, fh - 2, arc, arc);
                g2.setColor(new Color(17, 16, 51, 8));
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

    /** Warm CTA used by AI Smart Match: saturated violet → coral, matching the reference mockup. */
    public static PurpleGradientButton aiGradientButton(String text, Font font, Icon leadingIcon) {
        PurpleGradientButton b = new PurpleGradientButton(
                text,
                leadingIcon,
                new Color(0x7B5CF6),
                new Color(0xFF5C5C),
                new Color(0x6D4DEB),
                new Color(0xFF6E63));
        if (font != null) {
            b.setFont(font);
        }
        return b;
    }

    /** Red gradient primary — matches footprint of {@link PurpleGradientButton} (e.g. Withdraw). */
    public static RedGradientButton dangerGradientButton(String text, Font font) {
        return dangerGradientButton(text, font, null);
    }

    public static RedGradientButton dangerGradientButton(String text, Font font, Icon leadingIcon) {
        RedGradientButton b = new RedGradientButton(text, leadingIcon);
        if (font != null) {
            b.setFont(font);
        }
        return b;
    }

    private static Dimension computeGradientPillSize(Font font, String text, Icon leadingIcon, Insets pad) {
        JLabel scratch = new JLabel();
        FontMetrics fm = scratch.getFontMetrics(font != null ? font : scratch.getFont());
        String t = text != null ? text : "";
        int tw = fm.stringWidth(t);
        int iw = leadingIcon != null ? leadingIcon.getIconWidth() : 0;
        int ih = leadingIcon != null ? leadingIcon.getIconHeight() : 0;
        int gap = leadingIcon != null ? 8 : 0;
        int w = pad.left + pad.right + iw + gap + tw + 16;
        int h = pad.top + pad.bottom + Math.max(ih, fm.getHeight()) + 2;
        return new Dimension(Math.max(w, 96), Math.max(h, 36));
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
        private final Color idleLeft;
        private final Color idleRight;
        private final Color hoverLeft;
        private final Color hoverRight;

        public PurpleGradientButton(String text) {
            this(text, null);
        }

        public PurpleGradientButton(String text, Icon leadingIcon) {
            this(text, leadingIcon, PRIMARY_PURPLE, DEEP_PURPLE, new Color(0x7C5CFF), new Color(0x3F2BB8));
        }

        public PurpleGradientButton(String text, Icon leadingIcon, Color idleLeft, Color idleRight, Color hoverLeft, Color hoverRight) {
            super(text);
            this.leadingIcon = leadingIcon;
            this.idleLeft = idleLeft;
            this.idleRight = idleRight;
            this.hoverLeft = hoverLeft;
            this.hoverRight = hoverRight;
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
            Color left = hover ? hoverLeft : idleLeft;
            Color right = hover ? hoverRight : idleRight;
            // Stronger horizontal gradient so the right-side coral accent remains visible,
            // especially on Windows/Swing where small diagonal gradients can look nearly solid.
            g2.setColor(new Color(79, 53, 217, hover ? 48 : 28));
            g2.fillRoundRect(0, 3, w, h - 1, BUTTON_ARC, BUTTON_ARC);

            g2.setPaint(new GradientPaint(0, h / 2f, left, w, h / 2f, right));
            g2.fillRoundRect(0, 0, w, h - 2, BUTTON_ARC, BUTTON_ARC);

            // Subtle top highlight, closer to the glossy reference CTA.
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 42), 0, Math.max(1, h / 2), new Color(255, 255, 255, 0)));
            g2.fillRoundRect(1, 1, w - 2, Math.max(1, h / 2), BUTTON_ARC, BUTTON_ARC);

            Insets pad = BUTTON_CONTENT_PAD;
            Font font = getFont();
            FontMetrics fm = g2.getFontMetrics(font);
            String t = getText();
            int tw = textWidthPx(g2, font, t);
            int ih = leadingIcon != null ? leadingIcon.getIconHeight() : 0;
            int iw = leadingIcon != null ? leadingIcon.getIconWidth() : 0;
            int gap = leadingIcon != null ? 8 : 0;
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
            drawWhiteButtonLabel(g2, font, t, startX, baseline);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return computeGradientPillSize(getFont(), getText(), leadingIcon, BUTTON_CONTENT_PAD);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    /** Red gradient pill — same layout math as {@link PurpleGradientButton}. */
    public static final class RedGradientButton extends JButton {
        private boolean hover;
        private final Icon leadingIcon;
        private static final Color RED_L = new Color(248, 113, 113);
        private static final Color RED_R = new Color(220, 38, 38);
        private static final Color RED_L_H = new Color(220, 38, 38);
        private static final Color RED_R_H = new Color(185, 28, 28);

        public RedGradientButton(String text, Icon leadingIcon) {
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
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color left = hover ? RED_L_H : RED_L;
            Color right = hover ? RED_R_H : RED_R;
            g2.setPaint(new GradientPaint(0, 0, left, w, h, right));
            g2.fillRoundRect(0, 0, w, h, BUTTON_ARC, BUTTON_ARC);

            Insets pad = BUTTON_CONTENT_PAD;
            Font font = getFont();
            FontMetrics fm = g2.getFontMetrics(font);
            String t = getText();
            int tw = textWidthPx(g2, font, t);
            int ih = leadingIcon != null ? leadingIcon.getIconHeight() : 0;
            int iw = leadingIcon != null ? leadingIcon.getIconWidth() : 0;
            int gap = leadingIcon != null ? 8 : 0;
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
            drawWhiteButtonLabel(g2, font, t, startX, baseline);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return computeGradientPillSize(getFont(), getText(), leadingIcon, BUTTON_CONTENT_PAD);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
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
            setForeground(new Color(10, 84, 46));
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
            g2.setColor(new Color(10, 84, 46));
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
            Color top = hover ? new Color(190, 78, 118) : new Color(218, 96, 138);
            Color bottom = hover ? new Color(168, 62, 98) : new Color(196, 72, 112);
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
            drawWhiteButtonLabel(g2, font, t, tx, baseline);
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
            setForeground(PURPLE_800);
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
            g2.setColor(hover ? LAVENDER : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, BUTTON_ARC, BUTTON_ARC);
            g2.setColor(LIGHT_PURPLE_BORDER);
            g2.setStroke(new BasicStroke(1.7f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, BUTTON_ARC, BUTTON_ARC);

            Font font = getFont();
            FontMetrics fm = g2.getFontMetrics(font);
            String t = getText();
            int tw = textWidthPx(g2, font, t);
            Insets pad = BUTTON_CONTENT_PAD;
            Icon icon = getIcon();
            int iw = icon != null ? icon.getIconWidth() : 0;
            int ih = icon != null ? icon.getIconHeight() : 0;
            int gap = icon != null ? 7 : 0;
            int inner = w - pad.left - pad.right;
            int contentW = iw + gap + tw;
            int groupStart = pad.left + Math.max(0, (inner - contentW) / 2);
            int startX = groupStart;
            if (icon != null) {
                int iy = (h - ih) / 2;
                icon.paintIcon(this, g2, startX, iy);
                startX += iw + gap;
            }
            int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(font);
            g2.setColor(hover ? DEEP_PURPLE : PRIMARY_PURPLE);
            g2.drawString(t, startX, baseline);
            g2.dispose();
        }
    }

    public static JPanel courseCodePill(String courseCode) {
        JLabel pill = new JLabel(courseCode != null ? courseCode : "");
        pill.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pill.setForeground(PRIMARY_PURPLE);
        pill.setOpaque(false);
        Color pillBg = LAVENDER;
        Color pillBorder = LIGHT_PURPLE_BORDER;
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

    /** Briefcase icon — vector outline with handle, lid seam, and centered clasp (job cards & toolbar). */
    public static Icon briefcaseGlyph(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float sw = Math.max(1.15f, size / 17f);
                g2.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(ink);

                int pad = Math.max(2, size / 7);
                int bx = x + pad;
                int by = y + pad;
                int bw = size - 2 * pad;
                int bh = size - 2 * pad;

                int handleSpan = bw * 11 / 20;
                int hx = bx + (bw - handleSpan) / 2;
                int handleArcH = Math.max(5, bh / 5);
                g2.drawArc(hx, by, handleSpan, handleArcH + handleArcH / 2, 0, 180);

                int bodyTop = by + handleArcH * 2 / 3;
                int bodyH = by + bh - bodyTop;
                int arcBody = Math.max(4, size / 5);
                g2.drawRoundRect(bx, bodyTop, bw, bodyH, arcBody, arcBody);

                int lidH = Math.max(5, bodyH / 5);
                int seamY = bodyTop + lidH;
                g2.drawLine(bx + bw / 7, seamY, bx + bw - bw / 7, seamY);

                int claspW = Math.max(6, bw / 4);
                int claspH = Math.max(5, bodyH / 5);
                int cx = bx + (bw - claspW) / 2;
                int cy = seamY + (bodyTop + bodyH - seamY) / 2 - claspH / 2;
                g2.drawRoundRect(cx, cy, claspW, claspH, Math.min(4, claspW / 2), Math.min(4, claspH / 2));
                g2.drawLine(cx + claspW / 2, cy + 2, cx + claspW / 2, cy + claspH - 2);

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

    /** Simple eye icon for “View Details”. */
    public static Icon eyeGlyph(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + size / 2;
                int cy = y + size / 2;
                int rw = size / 2 - 2;
                int rh = size / 3;
                g2.drawOval(cx - rw / 2, cy - rh / 2, rw, rh);
                g2.fillOval(cx - 2, cy - 1, 4, 3);
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

    /** Document / application icon drawn with Java2D; avoids missing Unicode glyphs on Windows. */
    public static Icon fileTextIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int w = Math.max(10, size - 5);
                int h = Math.max(12, size - 3);
                int left = x + 3;
                int top = y + 2;
                int fold = Math.max(4, size / 4);
                Path2D.Float p = new Path2D.Float();
                p.moveTo(left, top);
                p.lineTo(left + w - fold, top);
                p.lineTo(left + w, top + fold);
                p.lineTo(left + w, top + h);
                p.lineTo(left, top + h);
                p.closePath();
                g2.draw(p);
                g2.drawLine(left + w - fold, top, left + w - fold, top + fold);
                g2.drawLine(left + w - fold, top + fold, left + w, top + fold);
                int ly = top + fold + 4;
                g2.drawLine(left + 4, ly, left + w - 4, ly);
                g2.drawLine(left + 4, ly + 5, left + w - 6, ly + 5);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Department / building icon drawn with Java2D. */
    public static Icon buildingIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int left = x + 4;
                int top = y + 4;
                int w = size - 8;
                int h = size - 7;
                g2.drawRoundRect(left, top, w, h, 2, 2);
                int cell = Math.max(3, size / 6);
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 2; col++) {
                        int cx = left + 4 + col * (cell + 4);
                        int cy = top + 4 + row * (cell + 4);
                        g2.drawRect(cx, cy, cell, cell);
                    }
                }
                g2.drawLine(left + w / 2, top + h, left + w / 2, top + h - 5);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Simple user / instructor icon drawn with Java2D. */
    public static Icon userIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + size / 2;
                g2.drawOval(cx - 4, y + 3, 8, 8);
                g2.drawArc(x + 4, y + size - 10, size - 8, 9, 0, 180);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** List icon drawn with Java2D. */
    public static Icon listLinesIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 3; i++) {
                    int yy = y + 4 + i * 6;
                    g2.fillOval(x + 2, yy - 1, 3, 3);
                    g2.drawLine(x + 8, yy, x + size - 2, yy);
                }
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Clock icon without surrounding badge. */
    public static Icon clockPlainIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                int cx = x + size / 2;
                int cy = y + size / 2;
                g2.drawLine(cx, cy, cx, y + 5);
                g2.drawLine(cx, cy, x + size - 5, cy);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Calendar icon without surrounding badge. */
    public static Icon calendarPlainIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 3, y + 4, size - 6, size - 7, 3, 3);
                g2.drawLine(x + 3, y + 8, x + size - 3, y + 8);
                g2.drawLine(x + 6, y + 2, x + 6, y + 6);
                g2.drawLine(x + size - 6, y + 2, x + size - 6, y + 6);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Map pin icon without surrounding badge. */
    public static Icon mapPinIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + size / 2;
                int top = y + 2;
                g2.drawOval(cx - 5, top + 1, 10, 10);
                Path2D.Float p = new Path2D.Float();
                p.moveTo(cx - 5, top + 8);
                p.quadTo(cx, y + size - 1, cx + 5, top + 8);
                g2.draw(p);
                g2.fillOval(cx - 2, top + 5, 4, 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }


    /** Trophy / ranking icon without relying on font glyphs. */
    public static Icon trophyIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(Math.max(1.4f, size / 18f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                int cupX = x + s / 4;
                int cupY = y + s / 5;
                int cupW = s / 2;
                int cupH = s / 3;
                g2.drawRoundRect(cupX, cupY, cupW, cupH, 5, 5);
                g2.drawArc(x + 2, cupY + 2, s / 3, s / 3, 95, 155);
                g2.drawArc(x + s - s / 3 - 2, cupY + 2, s / 3, s / 3, -70, 155);
                g2.drawLine(x + s / 2, cupY + cupH, x + s / 2, y + s - s / 4);
                g2.drawLine(x + s / 3, y + s - s / 4, x + 2 * s / 3, y + s - s / 4);
                g2.drawRoundRect(x + s / 3 - 2, y + s - s / 5, s / 3 + 4, s / 8, 3, 3);
                Path2D.Float star = new Path2D.Float();
                double cx = x + s / 2.0;
                double cy = cupY + cupH / 2.0;
                double r1 = Math.max(3, s / 9.0);
                double r2 = r1 * 0.45;
                for (int i = 0; i < 10; i++) {
                    double a = -Math.PI / 2 + i * Math.PI / 5;
                    double r = (i % 2 == 0) ? r1 : r2;
                    double px = cx + Math.cos(a) * r;
                    double py = cy + Math.sin(a) * r;
                    if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
                }
                star.closePath();
                g2.fill(star);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Compact medal icon for rank cards and detail hero cards. */
    public static Icon medalIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = size;
                int cx = x + s / 2;
                int cy = y + s / 3;
                g2.setColor(ink);
                Path2D.Float leftRibbon = new Path2D.Float();
                leftRibbon.moveTo(cx - s / 9.0, y + s * 0.56);
                leftRibbon.lineTo(cx - s * 0.28, y + s - 2);
                leftRibbon.lineTo(cx - s * 0.04, y + s * 0.82);
                leftRibbon.lineTo(cx + s / 12.0, y + s * 0.58);
                leftRibbon.closePath();
                Path2D.Float rightRibbon = new Path2D.Float();
                rightRibbon.moveTo(cx + s / 9.0, y + s * 0.56);
                rightRibbon.lineTo(cx + s * 0.28, y + s - 2);
                rightRibbon.lineTo(cx + s * 0.04, y + s * 0.82);
                rightRibbon.lineTo(cx - s / 12.0, y + s * 0.58);
                rightRibbon.closePath();
                g2.fill(leftRibbon);
                g2.fill(rightRibbon);
                g2.fillOval(x + s / 5, y + 2, s * 3 / 5, s * 3 / 5);
                g2.setColor(new Color(255, 255, 255, 225));
                Path2D.Float star = new Path2D.Float();
                double r1 = s / 8.0;
                double r2 = r1 * 0.45;
                for (int i = 0; i < 10; i++) {
                    double a = -Math.PI / 2 + i * Math.PI / 5;
                    double r = (i % 2 == 0) ? r1 : r2;
                    double px = cx + Math.cos(a) * r;
                    double py = cy + Math.sin(a) * r;
                    if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
                }
                star.closePath();
                g2.fill(star);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Simple line chart icon. */
    public static Icon lineChartIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                int base = y + s - 4;
                for (int i = 0; i < 4; i++) {
                    int bx = x + 3 + i * (s - 8) / 4;
                    int bh = 5 + i * 3;
                    g2.drawRoundRect(bx, base - bh, Math.max(3, s / 8), bh, 2, 2);
                }
                int[] xs = {x + 3, x + s / 3, x + s / 2, x + s - 4};
                int[] ys = {y + s / 2, y + s / 3, y + s / 2 - 3, y + 4};
                for (int i = 0; i < xs.length - 1; i++) {
                    g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
                }
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Target icon for high-match KPI. */
    public static Icon targetIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                int cx = x + s / 2;
                int cy = y + s / 2;
                g2.drawOval(x + 3, y + 3, s - 7, s - 7);
                g2.drawOval(x + s / 4, y + s / 4, s / 2, s / 2);
                g2.fillOval(cx - 3, cy - 3, 6, 6);
                g2.drawLine(cx + 2, cy - 2, x + s - 3, y + 3);
                g2.drawLine(x + s - 3, y + 3, x + s - 4, y + 9);
                g2.drawLine(x + s - 3, y + 3, x + s - 9, y + 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    public static Icon starCircleIcon(Color ink, int size) {
        return circleBadgeIcon(ink, size, (g2, x, y, s) -> {
            Path2D.Float star = new Path2D.Float();
            double cx = x + s / 2.0;
            double cy = y + s / 2.0;
            double r1 = s / 4.5;
            double r2 = r1 * 0.45;
            for (int i = 0; i < 10; i++) {
                double a = -Math.PI / 2 + i * Math.PI / 5;
                double r = (i % 2 == 0) ? r1 : r2;
                double px = cx + Math.cos(a) * r;
                double py = cy + Math.sin(a) * r;
                if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
            }
            star.closePath();
            g2.fill(star);
        });
    }

    public static Icon exclamationCircleIcon(Color ink, int size) {
        return circleBadgeIcon(ink, size, (g2, x, y, s) -> {
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + s / 2;
            g2.drawLine(cx, y + s / 4, cx, y + s * 3 / 5);
            g2.fillOval(cx - 1, y + s * 3 / 4 - 1, 3, 3);
        });
    }

    public static Icon lightbulbIcon(Color ink, int size) {
        return circleBadgeIcon(ink, size, (g2, x, y, s) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + s / 2;
            g2.drawOval(cx - s / 5, y + s / 5, s * 2 / 5, s * 2 / 5);
            g2.drawLine(cx - s / 7, y + s * 3 / 5, cx + s / 7, y + s * 3 / 5);
            g2.drawLine(cx - s / 8, y + s * 2 / 3, cx + s / 8, y + s * 2 / 3);
            g2.drawLine(cx, y + s * 3 / 5, cx, y + s * 4 / 5);
        });
    }

    private interface BadgePainter {
        void paint(Graphics2D g2, int x, int y, int size);
    }

    private static Icon circleBadgeIcon(Color ink, int size, BadgePainter painter) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.fillOval(x + 1, y + 1, size - 2, size - 2);
                g2.setColor(Color.WHITE);
                painter.paint(g2, x, y, size);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

}
