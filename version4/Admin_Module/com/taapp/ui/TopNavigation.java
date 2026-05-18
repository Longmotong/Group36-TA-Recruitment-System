package Admin_Module.com.taapp.ui;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Admin top bar — Soft-Neo purple gradient and pill nav, aligned with
 * {@link TA_Job_Application_Module.pages.jobs.JobsPortalUi} / TA portal chrome.
 */
public class TopNavigation extends JPanel {
    private static final Color BAR_LEFT = new Color(79, 53, 217);
    private static final Color BAR_RIGHT = new Color(142, 107, 245);
    private static final Color TEXT_SOFT = new Color(255, 255, 255, 222);

    private final Consumer<String> onNavigate;
    private final Runnable onLogout;
    private final Map<String, JButton> routeButtons = new LinkedHashMap<>();

    public TopNavigation(Consumer<String> onNavigate, Runnable onLogout, String currentUserLabel) {
        this.onNavigate = Objects.requireNonNull(onNavigate);
        this.onLogout = Objects.requireNonNull(onLogout);

        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(6, 20, 6, 20));
        setPreferredSize(new Dimension(10, 64));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        west.setOpaque(false);

        JPanel brandStack = new JPanel();
        brandStack.setOpaque(false);
        brandStack.setLayout(new javax.swing.BoxLayout(brandStack, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Admin System");
        title.setFont(UI.moFontBold(16));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("ADMIN CONSOLE");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sub.setForeground(TEXT_SOFT);
        brandStack.add(title);
        brandStack.add(Box.createVerticalStrut(2));
        brandStack.add(sub);
        west.add(brandStack);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.X_AXIS));
        center.add(Box.createHorizontalGlue());
        addNavPill(center, "Home", MainFrame.ROUTE_DASHBOARD);
        center.add(Box.createHorizontalStrut(8));
        addNavPill(center, "TA Workload", MainFrame.ROUTE_WORKLOAD);
        center.add(Box.createHorizontalStrut(8));
        addNavPill(center, "Statistics", MainFrame.ROUTE_STATISTICS);
        center.add(Box.createHorizontalStrut(8));
        addNavPill(center, "AI", MainFrame.ROUTE_AI);
        center.add(Box.createHorizontalGlue());

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        east.setOpaque(false);
        JLabel userLabel = new JLabel(currentUserLabel);
        userLabel.setFont(UI.moFontPlain(12));
        userLabel.setForeground(TEXT_SOFT);

        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 70));
                int x = getWidth() / 2;
                g2.drawLine(x, 4, x, getHeight() - 4);
                g2.dispose();
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(1, 28));

        JButton logout = new JButton("Sign Out");
        logout.setFont(UI.moFontPlain(12));
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setOpaque(false);
        logout.setForeground(Color.WHITE);
        logout.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        logout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logout.addActionListener(e -> this.onLogout.run());

        east.add(userLabel);
        east.add(sep);
        east.add(logout);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.fill = GridBagConstraints.NONE;
        add(west, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 16, 0, 16);
        add(center, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 0, 0, 0);
        add(east, gc);

        setActiveRoute(MainFrame.ROUTE_DASHBOARD);
    }

    private void addNavPill(JPanel row, String label, String route) {
        JButton b = new JButton(label);
        b.setFont(UI.moFontPlain(13));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        b.addActionListener(e -> onNavigate.accept(route));
        routeButtons.put(route, b);
        row.add(b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, BAR_LEFT, w, 0, BAR_RIGHT));
        g2.fillRect(0, 0, w, h);
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 28), 0, h, new Color(255, 255, 255, 0)));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(255, 255, 255, 16));
        g2.fillOval(w - 220, -80, 320, 200);
        g2.dispose();
        super.paintComponent(g);
    }

    public void setActiveRoute(String route) {
        for (Map.Entry<String, JButton> entry : routeButtons.entrySet()) {
            boolean active = entry.getKey().equals(route);
            JButton b = entry.getValue();
            b.setOpaque(active);
            b.setContentAreaFilled(active);
            b.setBackground(active ? Color.WHITE : new Color(0, 0, 0, 0));
            b.setForeground(active ? JobsPortalUi.PRIMARY_PURPLE : Color.WHITE);
            b.setFont(active ? UI.moFontBold(13) : UI.moFontPlain(13));
        }
    }
}
