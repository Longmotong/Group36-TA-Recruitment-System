package com.mojobsystem.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

public final class NavigationPanel {
    private static final Color NAV_BG = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_SUB = new Color(100, 116, 139);

    public enum Tab {
        HOME,
        JOB_MANAGEMENT,
        APPLICATION_REVIEW
    }

    private NavigationPanel() {
    }

    public static JPanel create(Tab active) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(NAV_BG);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        JLabel brand = new JLabel("MO Job System");
        brand.setForeground(TEXT_MAIN);
        brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        left.add(brand);

        JPanel centerNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        centerNav.setOpaque(false);
        centerNav.add(createNavButton("Home", active == Tab.HOME));
        centerNav.add(createNavButton("Job Management", active == Tab.JOB_MANAGEMENT));
        centerNav.add(createNavButton("Application Review", active == Tab.APPLICATION_REVIEW));
        left.add(centerNav);
        nav.add(left, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorder(new EmptyBorder(6, 8, 6, 8));
        logoutButton.setForeground(TEXT_MAIN);
        nav.add(logoutButton, BorderLayout.EAST);

        return nav;
    }

    private static JButton createNavButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        if (selected) {
            button.setBackground(new Color(241, 245, 249));
            button.setForeground(TEXT_MAIN);
            button.setOpaque(true);
            button.setBorderPainted(false);
        } else {
            button.setContentAreaFilled(false);
            button.setForeground(TEXT_SUB);
            button.setBorderPainted(false);
        }
        return button;
    }
}
