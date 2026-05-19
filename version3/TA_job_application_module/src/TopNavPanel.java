package TA_Job_Application_Module;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Standalone top navigation panel for TA Job Application Module.
 * No external dependencies - fully self-contained.
 */
public class TopNavPanel extends JPanel {

    public enum Active {
        HOME,
        PROFILE,
        JOBS
    }

    public interface Actions {
        void onHome();
        void onProfileModule();
        void onJobApplicationModule();
        void onLogout();
    }

    private final Actions actions;
    private final Supplier<String> userLineSupplier;
    private final JLabel userLabel;
    private final JButton navHome;
    private final JButton navProfile;
    private final JButton navJobs;

    private static final Color BORDER_COLOR = new Color(0xE0, 0xE0, 0xE0);
    private static final Color NAV_TEXT_SECONDARY = new Color(0x66, 0x66, 0x66);
    private static final Color NAV_TEXT_MUTED = new Color(0x99, 0x99, 0x99);

    public TopNavPanel(Actions actions, Supplier<String> userLineSupplier, Active initialActive) {
        this.actions = actions;
        this.userLineSupplier = userLineSupplier != null ? userLineSupplier : () -> "";

        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        setPreferredSize(new Dimension(10, 60));

        // Brand / Title
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel title = new JLabel("TA System - Job Applications");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(Color.BLACK);
        brand.add(title);
        add(brand);

        add(Box.createHorizontalStrut(18));

        // Navigation buttons
        navHome = createNavButton("Home", actions::onHome);
        navProfile = createNavButton("Profile", actions::onProfileModule);
        navJobs = createNavButton("Job Applications", actions::onJobApplicationModule);

        add(Box.createHorizontalStrut(28));

        // User label
        userLabel = new JLabel();
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        userLabel.setForeground(NAV_TEXT_SECONDARY);
        add(userLabel);

        // Logout button
        JButton logout = new JButton("Exit");
        logout.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setOpaque(false);
        logout.setBackground(Color.WHITE);
        logout.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        logout.setForeground(NAV_TEXT_MUTED);
        logout.setHorizontalAlignment(SwingConstants.RIGHT);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> actions.onLogout());
        add(logout);

        refreshUserLabel();
        setActive(initialActive);
    }

    private JButton createNavButton(String label, Runnable action) {
        JButton b = new JButton(label);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        b.setBorderPainted(false);
        b.setForeground(NAV_TEXT_SECONDARY);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        add(b);
        return b;
    }

    public void setActive(Active active) {
        navHome.setForeground(active == Active.HOME ? Color.BLACK : NAV_TEXT_SECONDARY);
        navHome.setFont(new Font(Font.SANS_SERIF, active == Active.HOME ? Font.BOLD : Font.PLAIN, 14));
        navProfile.setForeground(active == Active.PROFILE ? Color.BLACK : NAV_TEXT_SECONDARY);
        navProfile.setFont(new Font(Font.SANS_SERIF, active == Active.PROFILE ? Font.BOLD : Font.PLAIN, 14));
        navJobs.setForeground(active == Active.JOBS ? Color.BLACK : NAV_TEXT_SECONDARY);
        navJobs.setFont(new Font(Font.SANS_SERIF, active == Active.JOBS ? Font.BOLD : Font.PLAIN, 14));
    }

    public void refreshUserLabel() {
        userLabel.setText(userLineSupplier.get());
    }

    public void refresh(Active active) {
        refreshUserLabel();
        setActive(active);
    }
}
