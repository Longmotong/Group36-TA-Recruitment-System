package com.taapp.ui;

import com.taapp.data.DataStore;
import com.taapp.model.CurrentUser;
import com.taapp.ui.pages.AIAnalysisPanel;
import com.taapp.ui.pages.AdminDashboardPanel;
import com.taapp.ui.pages.StatisticsPanel;
import com.taapp.ui.pages.TAWorkloadPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
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
        setMinimumSize(new Dimension(980, 680));
        setPreferredSize(new Dimension(1080, 760));
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

        cardPanel.add(new AdminDashboardPanel(this::navigate), TopNavigation.ROUTE_DASHBOARD);
        cardPanel.add(new StatisticsPanel(this::navigate), TopNavigation.ROUTE_STATISTICS);
        cardPanel.add(new TAWorkloadPanel(this::navigate), TopNavigation.ROUTE_WORKLOAD);
        cardPanel.add(new AIAnalysisPanel(), TopNavigation.ROUTE_AI);

        add(cardPanel, BorderLayout.CENTER);

        navigate(TopNavigation.ROUTE_DASHBOARD);
        nav.setActiveRoute(TopNavigation.ROUTE_DASHBOARD);
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

        
        this.dispose();

        
        if (parentFrame instanceof java.awt.Window) {
            ((java.awt.Window) parentFrame).setVisible(true);
            if (parentFrame instanceof Authentication_Module.view.AppFrame) {
                ((Authentication_Module.view.AppFrame) parentFrame).showPage("HOME");
            }
        }
    }
}
