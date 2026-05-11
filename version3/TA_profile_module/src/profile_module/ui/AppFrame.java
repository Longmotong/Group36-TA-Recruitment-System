package profile_module.ui;

import profile_module.data.JsonStore;
import profile_module.data.ProfileData;
import profile_module.ui.screens.DashboardScreen;
import profile_module.ui.screens.EditProfileScreen;
import profile_module.ui.screens.EditSkillsScreen;
import profile_module.ui.screens.ManageCvScreen;
import profile_module.ui.screens.OnboardingScreen;
import profile_module.ui.screens.ProfileScreen;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Dimension;

public final class AppFrame extends JFrame {
    public static final String ROUTE_ONBOARDING = "onboarding";
    public static final String ROUTE_DASHBOARD = "dashboard";
    public static final String ROUTE_PROFILE = "profile";
    public static final String ROUTE_EDIT_PROFILE = "edit-profile";
    public static final String ROUTE_EDIT_SKILLS = "edit-skills";
    public static final String ROUTE_MANAGE_CV = "manage-cv";

    private final JsonStore store;
    private ProfileData profile;
    private Runnable onLogout;

    private String authenticatedUsername;
    private String authenticatedRole;

    private static String savedLookAndFeel;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final OnboardingScreen onboarding;
    private final DashboardScreen dashboard;
    private final ProfileScreen profileScreen;
    private final EditProfileScreen editProfileScreen;
    private final EditSkillsScreen editSkillsScreen;
    private final ManageCvScreen manageCvScreen;

    private final String initialRoute;

    public AppFrame(JsonStore store, ProfileData initialProfile) {
        this(store, initialProfile, ROUTE_ONBOARDING);
    }

    public AppFrame(JsonStore store, ProfileData initialProfile, String initialRoute) {
        super("TA Profile");
        this.store = store;
        this.profile = initialProfile;
        this.initialRoute = initialRoute != null ? initialRoute : ROUTE_DASHBOARD;

        try {
            savedLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            UIManager.put("OptionPane.yesButtonText", "Yes");
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.okButtonText", "OK");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");
        } catch (Exception ignored) {
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1080, 760);
        setLocationRelativeTo(null);

        onboarding = new OnboardingScreen(this);
        dashboard = new DashboardScreen(this);
        profileScreen = new ProfileScreen(this);
        editProfileScreen = new EditProfileScreen(this);
        editSkillsScreen = new EditSkillsScreen(this);
        manageCvScreen = new ManageCvScreen(this);

        root.add(onboarding, ROUTE_ONBOARDING);
        root.add(dashboard, ROUTE_DASHBOARD);
        root.add(profileScreen, ROUTE_PROFILE);
        root.add(editProfileScreen, ROUTE_EDIT_PROFILE);
        root.add(editSkillsScreen, ROUTE_EDIT_SKILLS);
        root.add(manageCvScreen, ROUTE_MANAGE_CV);
        setContentPane(root);

        showRoute(this.initialRoute);
    }

    public JsonStore store() {
        return store;
    }

    public ProfileData profile() {
        return profile;
    }

    public void updateProfile(ProfileData next) {
        profile = next;
        store.save(profile);
        dashboard.refresh();
        profileScreen.refresh();
        editProfileScreen.refresh();
        editSkillsScreen.refresh();
        manageCvScreen.refresh();
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public void setAuthenticatedContext(String username, String role) {
        this.authenticatedUsername = username;
        this.authenticatedRole = role;
    }

    public String authenticatedUsername() {
        return authenticatedUsername;
    }

    public String authenticatedRole() {
        return authenticatedRole;
    }

    /**
     * Folder key for CV uploads and similar paths (standalone: student id, username, or default).
     */
    public String currentLoginId() {
        if (authenticatedUsername != null && !authenticatedUsername.isBlank()) {
            return authenticatedUsername.trim();
        }
        if (profile != null && profile.studentId != null && !profile.studentId.isBlank()) {
            return profile.studentId.trim();
        }
        return "default";
    }

    public void showRoute(String route) {
        if (ROUTE_DASHBOARD.equals(route)) {
            try {
                if (savedLookAndFeel != null && !savedLookAndFeel.isEmpty()) {
                    UIManager.setLookAndFeel(savedLookAndFeel);
                }
            } catch (Exception ignored) {
            }
            dashboard.refresh();
        }
        if (ROUTE_PROFILE.equals(route)) {
            profileScreen.refresh();
        }
        if (ROUTE_EDIT_PROFILE.equals(route)) {
            editProfileScreen.refresh();
        }
        if (ROUTE_EDIT_SKILLS.equals(route)) {
            editSkillsScreen.refresh();
        }
        if (ROUTE_MANAGE_CV.equals(route)) {
            manageCvScreen.refresh();
        }
        cards.show(root, route);
    }

    /**
     * Standalone build: job application is not bundled; show an informative dialog.
     */
    public void openJobApplicationPortal(String initialPage) {
        JOptionPane.showMessageDialog(this,
                "The Job Application module is not included in this standalone profile build.\n"
                        + "Use the integrated TA system to browse jobs and applications.",
                "Job Application",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void confirmLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            if (onLogout != null) {
                onLogout.run();
            } else {
                System.exit(0);
            }
        }
    }
}
