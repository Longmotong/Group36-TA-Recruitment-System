package profile_module.ui.components;

import profile_module.ui.Theme;
import profile_module.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public final class StepHeader extends JPanel {
    private int currentStep = 1; // 1..3

    public StepHeader() {
        setOpaque(true);
        setBackground(Theme.SURFACE);
        setLayout(new FlowLayout(FlowLayout.LEFT, 28, 14));
        setPreferredSize(new Dimension(10, 74));
        rebuild();
    }

    public void setCurrentStep(int step) {
        currentStep = Math.max(1, Math.min(3, step));
        rebuild();
        revalidate();
        repaint();
    }

    private void rebuild() {
        removeAll();
        add(new StepItem(1, "Step 1", "Basic Information", stateFor(1)));
        add(new Connector(stateFor(2) == State.DONE ? Theme.GREEN : Theme.BORDER));
        add(new StepItem(2, "Step 2", "Skills", stateFor(2)));
        add(new Connector(stateFor(3) == State.DONE ? Theme.GREEN : Theme.BORDER));
        add(new StepItem(3, "Step 3", "Upload CV", stateFor(3)));
    }

    private State stateFor(int step) {
        if (step < currentStep) return State.DONE;
        if (step == currentStep) return State.CURRENT;
        return State.TODO;
    }

    private enum State { DONE, CURRENT, TODO }

    private static final class Connector extends JPanel {
        private final Color c;

        private Connector(Color c) {
            this.c = c;
            setOpaque(false);
            setPreferredSize(new Dimension(70, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            int y = getHeight() / 2;
            g2.fillRoundRect(0, y - 2, getWidth(), 4, 6, 6);
            g2.dispose();
        }
    }

    private static final class StepItem extends JPanel {
        private final int step;
        private final String top;
        private final String bottom;
        private final State state;

        private StepItem(int step, String top, String bottom, State state) {
            this.step = step;
            this.top = top;
            this.bottom = bottom;
            this.state = state;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(216, 48);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int r = 16;
            int cx = 6 + r;
            int cy = getHeight() / 2;

            if (state == State.DONE) {
                g2.setColor(Theme.GREEN_BG);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(Theme.GREEN);
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                paintCheckmark(g2, cx, cy);
            } else if (state == State.CURRENT) {
                g2.setColor(Theme.NAVY);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.BODY_BOLD);
                g2.drawString(String.valueOf(step), cx - 4, cy + 6);
            } else {
                g2.setColor(new Color(0xF1, 0xF3, 0xF5));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(Theme.BORDER);
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(new Color(0x9A, 0xA0, 0xAA));
                g2.setFont(Theme.BODY_BOLD);
                g2.drawString(String.valueOf(step), cx - 4, cy + 6);
            }

            g2.setColor(Theme.MUTED);
            g2.setFont(Theme.SMALL);
            int textX = cx + r + 12;
            g2.drawString(top, textX, cy - 2);

            g2.setColor(Theme.TEXT);
            g2.setFont(Theme.BODY_BOLD);
            g2.drawString(bottom, textX, cy + 16);
            g2.dispose();
        }

        /** Vector checkmark so completed steps render on fonts without U+2713 (Segoe UI often shows a box). */
        private static void paintCheckmark(Graphics2D g2, int cx, int cy) {
            Path2D.Float path = new Path2D.Float();
            path.moveTo(cx - 7, cy + 1);
            path.lineTo(cx - 2, cy + 6);
            path.lineTo(cx + 8, cy - 8);
            g2.setColor(Theme.GREEN);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);
        }
    }
}

