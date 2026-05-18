package Admin_Module.com.taapp.ui;

import Admin_Module.com.taapp.data.DataStore;
import Admin_Module.com.taapp.model.CurrentUser;
import Admin_Module.com.taapp.ui.pages.AIAnalysisPanel;
import Admin_Module.com.taapp.ui.pages.AdminDashboardPanel;
import Admin_Module.com.taapp.ui.pages.StatisticsPanel;
import Admin_Module.com.taapp.ui.pages.TAWorkloadPanel;

import profile_module.ui.TaTopNavigationPanel;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;

public class MainFrame extends JFrame {
    public static final String ROUTE_DASHBOARD = "dashboard";
    public static final String ROUTE_WORKLOAD = "workload";
    public static final String ROUTE_STATISTICS = "statistics";
    public static final String ROUTE_AI = "ai-analysis";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final TaTopNavigationPanel nav;
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

        TaTopNavigationPanel.Actions navActions = new TaTopNavigationPanel.Actions() {
            @Override
            public void onHome() {
                MainFrame.this.navigate(ROUTE_DASHBOARD);
            }

            @Override
            public void onProfileModule() {
                MainFrame.this.navigate(ROUTE_WORKLOAD);
            }

            @Override
            public void onJobApplicationModule() {
                MainFrame.this.navigate(ROUTE_STATISTICS);
            }

            @Override
            public void onFourthModule() {
                MainFrame.this.navigate(ROUTE_AI);
            }

            @Override
            public void onLogout() {
                MainFrame.this.logout();
            }
        };
        nav = new TaTopNavigationPanel(
                navActions,
                () -> {
                    CurrentUser u = DataStore.defaultStore().getCurrentUser();
                    return "Logged in: " + u.getFullName() + " (" + u.getLoginId() + ")";
                },
                TaTopNavigationPanel.Active.ADMIN_HOME,
                TaTopNavigationPanel.NavStyle.PORTAL_PURPLE_GRADIENT,
                TaTopNavigationPanel.PortalChromeVariant.ADMIN_FOUR);
        add(nav, BorderLayout.NORTH);

        cardPanel.setOpaque(true);
        cardPanel.setBackground(JobsPortalUi.PAGE_BG);

        cardPanel.add(new AdminDashboardPanel(this::navigate), ROUTE_DASHBOARD);
        cardPanel.add(new StatisticsPanel(this::navigate), ROUTE_STATISTICS);
        cardPanel.add(new TAWorkloadPanel(this::navigate), ROUTE_WORKLOAD);
        cardPanel.add(new AIAnalysisPanel(), ROUTE_AI);

        JPanel pageHost = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint bg = new GradientPaint(
                        0, 0, new Color(253, 252, 255),
                        0, h, new Color(248, 246, 255));
                g2.setPaint(bg);
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(109, 77, 235, 18));
                int startX = Math.max(0, w - 240);
                for (int x = startX; x < w - 18; x += 10) {
                    for (int y = 0; y < 150; y += 10) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        pageHost.setOpaque(false);
        pageHost.add(cardPanel, BorderLayout.CENTER);
        add(pageHost, BorderLayout.CENTER);

        navigate(ROUTE_DASHBOARD);
    }

    private static TaTopNavigationPanel.Active routeToAdminActive(String route) {
        if (ROUTE_DASHBOARD.equals(route)) {
            return TaTopNavigationPanel.Active.ADMIN_HOME;
        }
        if (ROUTE_WORKLOAD.equals(route)) {
            return TaTopNavigationPanel.Active.ADMIN_WORKLOAD;
        }
        if (ROUTE_STATISTICS.equals(route)) {
            return TaTopNavigationPanel.Active.ADMIN_STATISTICS;
        }
        if (ROUTE_AI.equals(route)) {
            return TaTopNavigationPanel.Active.ADMIN_AI;
        }
        return TaTopNavigationPanel.Active.ADMIN_HOME;
    }

    private void navigate(String route) {
        cardLayout.show(cardPanel, route);
        nav.setActive(routeToAdminActive(route));
        nav.refreshUserLabel();
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
