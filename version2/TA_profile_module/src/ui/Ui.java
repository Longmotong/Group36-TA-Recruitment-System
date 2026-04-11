package profile_module.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

public final class Ui {
    private Ui() {}

    public static Border empty(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static JLabel h1(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.H1);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel h2(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.H2);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel h3(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.H3);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel muted(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.MUTED);
        return l;
    }

    public static JLabel body(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JTextField textField(String placeholder) {
        JTextField tf = new PlaceholderTextField(placeholder);
        tf.setFont(Theme.BODY);
        tf.setForeground(Theme.TEXT);
        tf.setOpaque(false);
        tf.setBorder(Ui.empty(10, 12, 10, 12));
        tf.setPreferredSize(new Dimension(240, 40));
        return tf;
    }

    public static JPanel row() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        p.setOpaque(false);
        return p;
    }

    public static void enableTextAntialias() {
        UIManager.put("Label.font", Theme.BODY);
    }

    public static final class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final Color stroke;
        private final int strokeWidth;

        public RoundedPanel(int radius, Color fill, Color stroke, int strokeWidth) {
            this.radius = radius;
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int inset = Math.max(0, strokeWidth);
            int x = inset / 2;
            int y = inset / 2;
            int rw = Math.max(0, w - inset);
            int rh = Math.max(0, h - inset);

            g2.setColor(fill);
            g2.fillRoundRect(x, y, rw, rh, radius, radius);
            if (stroke != null && strokeWidth > 0) {
                g2.setColor(stroke);
                g2.setStroke(new java.awt.BasicStroke(strokeWidth));
                g2.drawRoundRect(x, y, rw, rh, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setBackground(new Color(0, 0, 0, 0));
            setCaretColor(Theme.TEXT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (placeholder == null) return;
            if (getText() != null && !getText().isEmpty()) return;
            if (isFocusOwner()) return;
            Insets ins = getInsets();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(0xA1, 0xA6, 0xAE));
            Font f = getFont();
            g2.setFont(f);
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, ins.left, y);
            g2.dispose();
        }
    }

    public static final class RoundedTextField extends JComponent {
        private final JTextField field;

        public RoundedTextField(JTextField field) {
            this.field = field;
            setLayout(new java.awt.BorderLayout());
            setOpaque(false);
            add(field, java.awt.BorderLayout.CENTER);
            setPreferredSize(field.getPreferredSize());
        }

        public JTextField field() {
            return field;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(Theme.SURFACE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

