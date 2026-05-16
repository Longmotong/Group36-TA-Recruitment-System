package com.taapp.ui;

import com.taapp.data.DataStore;
import com.taapp.model.CurrentUser;
import com.taapp.ui.pages.AIAnalysisPanel;
import com.taapp.ui.pages.AdminDashboardPanel;
import com.taapp.ui.pages.StatisticsPanel;
import com.taapp.ui.pages.TAWorkloadPanel;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.Dimension;

public class MainFrame extends JFrame {
    public static final String ROUTE_DASHBOARD = "dashboard";
    public static final String ROUTE_WORKLOAD = "workload";
    public static final String ROUTE_STATISTICS = "statistics";
    public static final String ROUTE_AI = "ai-analysis";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final TopNavigation nav;
    private final Component parentFrame;

    public MainFrame() {
        this(null);
    }

    public MainFrame(Component parent) {
        super("Admin System");
        this.parentFrame = parent;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1080, 760));
        setMinimumSize(new Dimension(1080, 760));
        setSize(new Dimension(1080, 760));
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout());

        CurrentUser currentUser = DataStore.defaultStore().getCurrentUser();
        String userLabel = "Logged in: " + currentUser.getFullName() + " (" + currentUser.getLoginId() + ")";
        nav = new TopNavigation(this::navigate, this::logout, userLabel);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UI.palette().border()));
        add(nav, BorderLayout.NORTH);

        cardPanel.setOpaque(true);
        cardPanel.setBackground(UI.palette().appBg());

        cardPanel.add(new AdminDashboardPanel(this::navigate), ROUTE_DASHBOARD);
        cardPanel.add(new StatisticsPanel(this::navigate), ROUTE_STATISTICS);
        cardPanel.add(new TAWorkloadPanel(this::navigate), ROUTE_WORKLOAD);
        cardPanel.add(new AIAnalysisPanel(), ROUTE_AI);

        add(cardPanel, BorderLayout.CENTER);

        navigate(ROUTE_DASHBOARD);
        nav.setActiveRoute(ROUTE_DASHBOARD);
    }

    private void navigate(String route) {
        cardLayout.show(cardPanel, route);
        nav.setActiveRoute(route);
        cardPanel.requestFocusInWindow();
    }

    private void logout() {
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        int result = JOptionPane.showConfirmDialog(
                this,
                "You will be logged out. Continue?",
                "Logout",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        Authentication_Module.session.SessionManager.logout();
        dispose();
        if (parentFrame instanceof Window window) {
            window.setVisible(true);
            if (parentFrame instanceof Authentication_Module.view.AppFrame appFrame) {
                appFrame.showPage("HOME");
            }
        }
    }
}
