package com.taapp.ui;

import com.taapp.ui.pages.AIAnalysisPanel;
import com.taapp.ui.pages.AdminDashboardPanel;
import com.taapp.ui.pages.StatisticsPanel;
import com.taapp.ui.pages.TAWorkloadPanel;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

public class MainFrame extends JFrame {
    public static final String ROUTE_DASHBOARD = "dashboard";
    public static final String ROUTE_WORKLOAD = "workload";
    public static final String ROUTE_STATISTICS = "statistics";
    public static final String ROUTE_AI = "ai-analysis";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final TopNavigation nav;

    public MainFrame() {
        super("Admin System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        nav = new TopNavigation(this::navigate, this::logout);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UI.palette().border()));
        add(nav, BorderLayout.NORTH);

        cardPanel.setOpaque(true);
        cardPanel.setBackground(UI.palette().appBg());

        cardPanel.add(new AdminDashboardPanel(this::navigate), ROUTE_DASHBOARD);
        cardPanel.add(new StatisticsPanel(), ROUTE_STATISTICS);
        cardPanel.add(new TAWorkloadPanel(), ROUTE_WORKLOAD);
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
        Dialogs.showMessage(this, "Logout", "This is a stand-alone demo. Logout is not implemented.", SwingConstants.CENTER);
    }
}

