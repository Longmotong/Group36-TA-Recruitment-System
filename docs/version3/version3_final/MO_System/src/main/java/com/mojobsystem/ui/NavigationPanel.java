package com.mojobsystem.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Objects;

import com.mojobsystem.MoContext;

public final class NavigationPanel {
    public enum Tab {
        HOME,
        JOB_MANAGEMENT,
        APPLICATION_REVIEW
    }

    public record Actions(Runnable home, Runnable jobManagement, Runnable applicationReview, Runnable logout) {
        public Actions {
            Objects.requireNonNull(home);
            Objects.requireNonNull(jobManagement);
            Objects.requireNonNull(applicationReview);
            Objects.requireNonNull(logout);
        }
    }

    private NavigationPanel() {
    }

    public static JPanel create(Tab active, Actions actions) {
        return create(active, actions, null);
    }

    /**
     * @param moSwitchRefresh when non-null, shows mo001/mo002 switch; runs after MO change (e.g. reopen frame).
     */
    public static JPanel create(Tab active, Actions actions, Runnable moSwitchRefresh) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(MoUiTheme.SURFACE);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER),
                new EmptyBorder(16, MoUiTheme.GUTTER, 16, MoUiTheme.GUTTER)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.setOpaque(false);

        JPanel brandCol = new JPanel();
        brandCol.setLayout(new BoxLayout(brandCol, BoxLayout.Y_AXIS));
        brandCol.setOpaque(false);
        JLabel brand = new JLabel("MO System");
        brand.setForeground(MoUiTheme.TEXT_PRIMARY);
        brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        brandCol.add(brand);
        left.add(brandCol);
        left.add(Box.createHorizontalStrut(28));

        JPanel centerNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        centerNav.setOpaque(false);
        JButton homeBtn = createNavButton("Home", active == Tab.HOME);
        homeBtn.addActionListener(e -> actions.home().run());
        JButton jobBtn = createNavButton("Job Management", active == Tab.JOB_MANAGEMENT);
        jobBtn.addActionListener(e -> actions.jobManagement().run());
        JButton appBtn = createNavButton("Application Review", active == Tab.APPLICATION_REVIEW);
        appBtn.addActionListener(e -> actions.applicationReview().run());
        centerNav.add(homeBtn);
        centerNav.add(jobBtn);
        centerNav.add(appBtn);
        left.add(centerNav);
        nav.add(left, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        logoutButton.setForeground(MoUiTheme.TEXT_SECONDARY);
        logoutButton.setFocusPainted(false);
        MoUiTheme.styleOutlineButton(logoutButton, 8);
        logoutButton.addActionListener(e -> actions.logout().run());

        if (moSwitchRefresh != null) {
            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            east.setOpaque(false);
            JLabel moLab = new JLabel("MO:");
            moLab.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            moLab.setForeground(MoUiTheme.TEXT_SECONDARY);
            JComboBox<String> moBox = new JComboBox<>(new String[]{"mo001", "mo002"});
            moBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            moBox.setSelectedIndex(MoContext.U_MO_002.equals(MoContext.getCurrentMoUserId()) ? 1 : 0);
            moBox.addActionListener(e -> {
                int i = moBox.getSelectedIndex();
                String next = i == 1 ? MoContext.U_MO_002 : MoContext.U_MO_001;
                if (next.equals(MoContext.getCurrentMoUserId())) {
                    return;
                }
                MoContext.setCurrentMoUserId(next);
                moSwitchRefresh.run();
            });
            east.add(moLab);
            east.add(moBox);
            east.add(Box.createHorizontalStrut(4));
            east.add(logoutButton);
            nav.add(east, BorderLayout.EAST);
        } else {
            nav.add(logoutButton, BorderLayout.EAST);
        }

        return nav;
    }

    private static JButton createNavButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        button.setFocusPainted(false);
        MoUiTheme.styleNavPill(button, selected, 10);
        return button;
    }
}
