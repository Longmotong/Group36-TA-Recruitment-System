package com.taapp.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class TopNavigation extends JPanel {
    private final Consumer<String> onNavigate;
    private final Runnable onLogout;
    private final Map<String, JButton> routeButtons = new LinkedHashMap<>();

    public TopNavigation(Consumer<String> onNavigate, Runnable onLogout, String currentUserLabel) {
        this.onNavigate = Objects.requireNonNull(onNavigate);
        this.onLogout = Objects.requireNonNull(onLogout);

        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 12));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(10, 60));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        javax.swing.JLabel title = new javax.swing.JLabel("Admin System");
        title.setFont(UI.fontMedium(18));
        title.setForeground(UI.palette().text());
        brand.add(title);
        add(brand);

        add(spacer(18));

        addNavButton("Home", MainFrame.ROUTE_DASHBOARD);
        addNavButton("TA Workload", MainFrame.ROUTE_WORKLOAD);
        addNavButton("Statistics", MainFrame.ROUTE_STATISTICS);
        addNavButton("AI Analysis", MainFrame.ROUTE_AI);

        add(javax.swing.Box.createHorizontalStrut(28));

        javax.swing.JLabel userLabel = new javax.swing.JLabel(currentUserLabel);
        userLabel.setFont(UI.fontPlain(13));
        userLabel.setForeground(UI.palette().textSoft());
        add(userLabel);

        JButton logout = new JButton("Logout");
        logout.setFont(UI.fontPlain(14));
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false);
        logout.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        logout.setForeground(UI.palette().textMuted());
        logout.setHorizontalAlignment(SwingConstants.RIGHT);
        logout.addActionListener(e -> this.onLogout.run());
        add(logout);
    }

    private void addNavButton(String label, String route) {
        JButton b = new JButton(label);
        b.setFont(UI.fontPlain(14));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        b.setForeground(UI.palette().textMuted());
        b.addActionListener(e -> onNavigate.accept(route));
        routeButtons.put(route, b);
        add(b);
    }

    public void setActiveRoute(String route) {
        for (Map.Entry<String, JButton> entry : routeButtons.entrySet()) {
            boolean active = entry.getKey().equals(route);
            JButton b = entry.getValue();
            b.setForeground(active ? UI.palette().text() : UI.palette().textMuted());
            b.setFont(active ? UI.fontMedium(14) : UI.fontPlain(14));
        }
    }

    private static JPanel spacer(int width) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(width, 1));
        return p;
    }
}

