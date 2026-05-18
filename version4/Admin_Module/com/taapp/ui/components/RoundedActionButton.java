package Admin_Module.com.taapp.ui.components;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
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
        /** Purple gradient primary — matches {@link JobsPortalUi.PurpleGradientButton}. */
        PRIMARY_BLACK(JobsPortalUi.PRIMARY_PURPLE, JobsPortalUi.DEEP_PURPLE),
        /** Blue accent — e.g. Export CSV in allocation / workload. */
        ACCENT_BLUE(JobsPortalUi.BLUE_ACCENT, new Color(0x1D4ED8));

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
        int w = getWidth();
        int h = getHeight();
        g2.setColor(new Color(79, 53, 217, hover ? 40 : 26));
        g2.fillRoundRect(0, 2, w - 1, h - 3, arc, arc);
        Color left = hover ? new Color(0x7C, 0x5C, 0xFF) : scheme.base;
        Color right = hover ? new Color(0x3F, 0x2B, 0xB8) : scheme.hover;
        g2.setPaint(new GradientPaint(0, h / 2f, left, w, h / 2f, right));
        g2.fillRoundRect(0, 0, w - 1, h - 4, arc, arc);
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 38), 0, Math.max(1, h / 2), new Color(255, 255, 255, 0)));
        g2.fillRoundRect(1, 1, w - 3, Math.max(1, h / 2), arc, arc);
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
