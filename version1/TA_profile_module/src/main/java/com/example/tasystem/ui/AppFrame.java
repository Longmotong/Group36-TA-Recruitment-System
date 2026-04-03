package com.example.tasystem.ui;

import com.example.tasystem.data.JsonStore;
import com.example.tasystem.data.ProfileData;
import com.example.tasystem.ui.screens.DashboardScreen;
import com.example.tasystem.ui.screens.EditProfileScreen;
import com.example.tasystem.ui.screens.EditSkillsScreen;
import com.example.tasystem.ui.screens.ManageCvScreen;
import com.example.tasystem.ui.screens.OnboardingScreen;
import com.example.tasystem.ui.screens.ProfileScreen;

import javax.swing.JFrame;
import javax.swing.JPanel;
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

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final OnboardingScreen onboarding;
    private final DashboardScreen dashboard;
    private final ProfileScreen profileScreen;
    private final EditProfileScreen editProfileScreen;
    private final EditSkillsScreen editSkillsScreen;
    private final ManageCvScreen manageCvScreen;

    public AppFrame(JsonStore store, ProfileData initialProfile) {
        super("TA System");
        this.store = store;
        this.profile = initialProfile;

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

        showRoute(ROUTE_ONBOARDING);
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

    public void showRoute(String route) {
        if (ROUTE_DASHBOARD.equals(route)) dashboard.refresh();
        if (ROUTE_PROFILE.equals(route)) profileScreen.refresh();
        if (ROUTE_EDIT_PROFILE.equals(route)) editProfileScreen.refresh();
        if (ROUTE_EDIT_SKILLS.equals(route)) editSkillsScreen.refresh();
        if (ROUTE_MANAGE_CV.equals(route)) manageCvScreen.refresh();
        cards.show(root, route);
    }
}

