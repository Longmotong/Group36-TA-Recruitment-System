package view;

import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {

    private CardLayout layout;
    private JPanel container;

    public AppFrame() {

        setTitle("TA Recruitment System");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化
        layout = new CardLayout();
        container = new JPanel(layout);

        // =========================
        // 页面注册
        // =========================
        container.add(new HomePagePanel(this), "HOME"); //
        container.add(new LoginPagePanel(this), "LOGIN");
        container.add(new LoginPagePanel(this), "LOGIN");
        container.add(new RegisterPagePanel(this), "REGISTER");
        container.add(new TAHomePagePanel(this), "TA");
        container.add(new MOHomePagePanel(this), "MO");
        container.add(new AdminHomePagePanel(this), "ADMIN");

        // 加入主窗口
        setContentPane(container);

        // 默认显示首页
        layout.show(container, "HOME");

        setVisible(true);
    }

    // =========================
    // 页面切换方法（核心）
    // =========================
    public void showPage(String name) {
        layout.show(container, name);
    }
}