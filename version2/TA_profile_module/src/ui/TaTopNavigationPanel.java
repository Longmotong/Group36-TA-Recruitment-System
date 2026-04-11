package profile_module.ui;

import profile_module.data.ProfileData;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Supplier;

/**
 * Top navigation aligned with Admin {@link com.taapp.ui.TopNavigation} (not TA Job Application page chrome).
 */
public final class TaTopNavigationPanel extends JPanel {

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

    private final JButton navHome;
    private final JButton navProfile;
    private final JButton navJobs;
    private final JLabel userLabel;

    public TaTopNavigationPanel(Actions actions, Supplier<String> userLineSupplier, Active initialActive) {
        this.actions = actions;
        this.userLineSupplier = userLineSupplier != null ? userLineSupplier : () -> "";

        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(0, 8, 0, 8)
        ));
        setPreferredSize(new Dimension(10, 60));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel title = new JLabel("TA System");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(Color.BLACK);
        brand.add(title);
        add(brand);

        add(spacer(18));

        navHome = addNavButton("Home", actions::onHome);
        navProfile = addNavButton("Profile Module", actions::onProfileModule);
        navJobs = addNavButton("Job Application Module", actions::onJobApplicationModule);

        add(Box.createHorizontalStrut(28));

        userLabel = new JLabel();
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        userLabel.setForeground(Theme.NAV_TEXT_SECONDARY);
        add(userLabel);

        JButton logout = new JButton("Logout");
        logout.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setOpaque(false);
        logout.setBackground(Color.WHITE);
        logout.setBorder(new EmptyBorder(6, 10, 6, 10));
        logout.setForeground(Theme.NAV_TEXT_MUTED);
        logout.setHorizontalAlignment(SwingConstants.RIGHT);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> actions.onLogout());
        add(logout);

        refreshUserLabel();
        setActive(initialActive);
    }

    public static TaTopNavigationPanel forAppFrame(AppFrame app, Active initialActive) {
        Actions actions = new Actions() {
            @Override
            public void onHome() {
                app.showRoute(AppFrame.ROUTE_DASHBOARD);
            }

            @Override
            public void onProfileModule() {
                app.showRoute(AppFrame.ROUTE_PROFILE);
            }

            @Override
            public void onJobApplicationModule() {
                app.openJobApplicationPortal("jobs");
            }

            @Override
            public void onLogout() {
                app.confirmLogout();
            }
        };
        return new TaTopNavigationPanel(actions, () -> userLineForAppFrame(app), initialActive);
    }

    private static String userLineForAppFrame(AppFrame app) {
        String u = app.authenticatedUsername();
        if (u != null && !u.isBlank()) {
            return "Logged in: " + u;
        }
        ProfileData p = app.profile();
        String name = p != null ? p.fullName : null;
        if (name != null && !name.isBlank()) {
            return "Logged in: " + name;
        }
        return "";
    }

    public void setActive(Active active) {
        styleNavButton(navHome, active == Active.HOME);
        styleNavButton(navProfile, active == Active.PROFILE);
        styleNavButton(navJobs, active == Active.JOBS);
    }

    public void refreshUserLabel() {
        userLabel.setText(userLineSupplier.get());
    }

    /** Updates user line and highlight (e.g. after login or route change). */
    public void refresh(Active active) {
        refreshUserLabel();
        setActive(active);
    }

    private static void styleNavButton(JButton b, boolean active) {
        b.setForeground(active ? Color.BLACK : Theme.NAV_TEXT_SECONDARY);
        b.setFont(active ? new Font(Font.SANS_SERIF, Font.BOLD, 14) : new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    }

    private static JPanel spacer(int width) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(width, 1));
        return p;
    }

    private JButton addNavButton(String label, Runnable action) {
        JButton b = new JButton(label);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(6, 10, 6, 10));
        b.setBorderPainted(false);
        b.setForeground(Theme.NAV_TEXT_SECONDARY);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        add(b);
        return b;
    }
}
