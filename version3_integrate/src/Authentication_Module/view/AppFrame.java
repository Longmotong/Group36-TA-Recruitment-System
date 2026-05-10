package Authentication_Module.view;

import Authentication_Module.controller.AuthController;
import Authentication_Module.model.User;
import Authentication_Module.service.AuthService;
import Authentication_Module.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AppFrame extends JFrame {

    private CardLayout layout;
    private JPanel container;
    // 存储页面引用
    private Map<String, JPanel> pages = new HashMap<>();
    // 当前登录用户（供 MO 首登补全页使用）
    private User currentUser;

    public AppFrame() {

        setTitle("TA Recruitment System");
        setMinimumSize(new Dimension(980, 680));
        setSize(1080, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化
        layout = new CardLayout();
        container = new JPanel(layout);

        // =========================
        // 页面注册
        // =========================
        HomePagePanel homePanel = new HomePagePanel(this);
        LoginPagePanel loginPanel = new LoginPagePanel(this);
        RegisterPagePanel registerPanel = new RegisterPagePanel(this);
        TAHomePagePanel taPanel = new TAHomePagePanel(this);
        MOHomePagePanel moPanel = new MOHomePagePanel(this);
        AdminHomePagePanel adminPanel = new AdminHomePagePanel(this);
        MOFirstLoginPagePanel moFirstPanel = new MOFirstLoginPagePanel(this);

        container.add(homePanel, "HOME");
        container.add(loginPanel, "LOGIN");
        container.add(registerPanel, "REGISTER");
        container.add(taPanel, "TA");
        container.add(moPanel, "MO");
        container.add(adminPanel, "ADMIN");
        container.add(moFirstPanel, "MO_FIRST");

        // 存储页面引用
        pages.put("HOME", homePanel);
        pages.put("LOGIN", loginPanel);
        pages.put("REGISTER", registerPanel);
        pages.put("TA", taPanel);
        pages.put("MO", moPanel);
        pages.put("ADMIN", adminPanel);
        pages.put("MO_FIRST", moFirstPanel);

        // 加入主窗口
        setContentPane(container);

        // 默认显示首页
        layout.show(container, "HOME");

        // 确保所有页面的布局正确初始化
        SwingUtilities.invokeLater(() -> {
            for (JPanel page : pages.values()) {
                page.revalidate();
                page.repaint();
            }
            revalidate();
            repaint();
        });

        setVisible(true);
    }

    // =========================
    // 页面切换方法（核心）
    // =========================
    public void showPage(String name) {
        if ("ADMIN".equals(name)) {
            AdminHomePagePanel adminPanel = (AdminHomePagePanel) pages.get("ADMIN");
            if (adminPanel != null) {
                adminPanel.launchAdminWindow();
            }
        } else {
            layout.show(container, name);
            if ("LOGIN".equals(name)) {
                JPanel loginPanel = pages.get("LOGIN");
                if (loginPanel instanceof LoginPagePanel) {
                    ((LoginPagePanel) loginPanel).clearCredentials();
                }
            }
            if ("HOME".equals(name)) {
                HomePagePanel homePanel = (HomePagePanel) pages.get("HOME");
                if (homePanel != null) {
                    homePanel.refreshButtonStyles();
                }
            }
            if ("REGISTER".equals(name)) {
                JPanel registerPanel = pages.get("REGISTER");
                if (registerPanel != null) {
                    if (registerPanel instanceof RegisterPagePanel) {
                        ((RegisterPagePanel) registerPanel).clearForm();
                    }
                    SwingUtilities.invokeLater(() -> {
                        registerPanel.revalidate();
                        registerPanel.repaint();
                        // 强制刷新所有组件
                        for (Component c : registerPanel.getComponents()) {
                            if (c instanceof JPanel) {
                                ((JPanel) c).revalidate();
                                ((JPanel) c).repaint();
                            }
                        }
                    });
                }
            }
            container.revalidate();
            container.repaint();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}