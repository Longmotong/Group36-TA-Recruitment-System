package login.view;

import com.mojobsystem.MoContext;
import com.mojobsystem.ui.IntegratedDashboardFrame;
import com.taapp.ui.MainFrame;
import login.SessionManager;
import login.User;
import taportal.DataService;
import taportal.TAPortalApp;

import login.AppDataRoot;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AppFrame extends JFrame {

    private final CardLayout layout = new CardLayout();
    private final JPanel container = new JPanel(layout);

    public AppFrame() {
        setTitle("TA Recruitment System");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        container.add(new HomePagePanel(this), "HOME");
        container.add(new LoginPagePanel(this), "LOGIN");
        container.add(new RegisterPagePanel(this), "REGISTER");

        add(container);
        layout.show(container, "HOME");
    }

    public void showPage(String name) {
        layout.show(container, name);
    }

    /**
     * Opens the Job_application_module TA portal (profile + jobs + applications) on the same {@code data/} tree.
     */
    public void showTaDashboard() {
        User u = SessionManager.getCurrentUser();
        if (u == null) {
            return;
        }
        File dataRoot = resolveProjectDataDirectory();
        System.setProperty("taportal.data.path", dataRoot.getAbsolutePath());
        System.setProperty("taportal.login.username", u.getUsername());
        DataService.resetInstance();
        dispose();
        SwingUtilities.invokeLater(() -> new TAPortalApp(() -> {
            SessionManager.logout();
            DataService.resetInstance();
            System.clearProperty("taportal.login.username");
            SwingUtilities.invokeLater(() -> {
                AppFrame f = new AppFrame();
                f.setVisible(true);
                f.showPage("LOGIN");
            });
        }).setVisible(true));
    }

    public void showMoDashboard() {
        dispose();
        MoContext.initFromSession();
        IntegratedDashboardFrame frame = new IntegratedDashboardFrame();
        frame.setVisible(true);
    }

    /**
     * Opens the Admin module ({@link MainFrame}) on the same {@code data/} tree as jobs, applications, and users.
     */
    public void showAdminDashboard() {
        User u = SessionManager.getCurrentUser();
        if (u == null) {
            return;
        }
        File dataRoot = resolveProjectDataDirectory();
        System.setProperty("admin.data.root", dataRoot.getAbsolutePath());
        System.setProperty("admin.login.username", u.getUsername());
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(() -> {
            SessionManager.logout();
            System.clearProperty("admin.data.root");
            System.clearProperty("admin.login.username");
            SwingUtilities.invokeLater(() -> {
                AppFrame f = new AppFrame();
                f.setVisible(true);
                f.showPage("LOGIN");
            });
        }).setVisible(true));
    }

    /** Resolves the shared {@code data} tree (sibling {@code ../data} or legacy layouts). */
    static File resolveProjectDataDirectory() {
        return AppDataRoot.asFile();
    }
}
