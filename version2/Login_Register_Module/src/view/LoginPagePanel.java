package view;

import controller.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPagePanel extends JPanel {

    public LoginPagePanel(AppFrame app) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== 外层容器（用于居中）=====
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);

        // ===== 卡片 =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(360, 360));

        // ===== 标题 =====
        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Login to your account");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(new Color(120, 120, 120));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== 输入区域 =====
        JTextField user = new JTextField();
        user.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        user.setBorder(BorderFactory.createTitledBorder("Username"));

        JPasswordField pass = new JPasswordField();
        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pass.setBorder(BorderFactory.createTitledBorder("Password"));

        // ===== 按钮 =====
        JButton login = new JButton("Login");
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.setBackground(Color.BLACK);
        login.setForeground(Color.WHITE);
        login.setFocusPainted(false);
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        AuthController controller = new AuthController();

        login.addActionListener(e -> {
            boolean ok = controller.handleLogin(user.getText(), new String(pass.getPassword()));

            if (ok) {
                String role = controller.getUserRole(user.getText());

                switch (role) {
                    case "ta": app.showPage("TA"); break;
                    case "mo": app.showPage("MO"); break;
                    case "admin": app.showPage("ADMIN"); break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        });

        // ===== 返回 =====
        JButton back = new JButton("← Back to Home");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setForeground(new Color(100, 100, 100));
        back.addActionListener(e -> app.showPage("HOME"));

        // ===== 组装 =====
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);

        card.add(Box.createVerticalStrut(25));
        card.add(user);
        card.add(Box.createVerticalStrut(15));
        card.add(pass);

        card.add(Box.createVerticalStrut(25));
        card.add(login);

        card.add(Box.createVerticalStrut(15));
        card.add(back);

        wrapper.add(card);

        add(wrapper, BorderLayout.CENTER);
    }
}