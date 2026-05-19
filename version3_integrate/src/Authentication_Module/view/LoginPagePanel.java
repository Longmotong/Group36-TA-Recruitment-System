package Authentication_Module.view;

import Authentication_Module.session.SessionManager;
import Authentication_Module.controller.AuthController;
import Authentication_Module.model.User;
import Authentication_Module.util.JsonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPagePanel extends JPanel {

    private final JTextField user;
    private final JPasswordField pass;

    public LoginPagePanel(AppFrame app) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== 外层容器（居中）=====
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

        // ===== 输入框 =====
        user = new JTextField();
        user.setFont(new Font("Arial", Font.PLAIN, 18));
        user.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        user.setPreferredSize(new Dimension(0, 54));
        user.setMinimumSize(new Dimension(0, 54));
        user.setBorder(BorderFactory.createTitledBorder("Username"));

        pass = new JPasswordField();
        pass.setFont(new Font("Arial", Font.PLAIN, 18));
        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        pass.setPreferredSize(new Dimension(0, 54));
        pass.setMinimumSize(new Dimension(0, 54));
        pass.setBorder(BorderFactory.createTitledBorder("Password"));

        // ===== 登录按钮 =====
        JButton login = new JButton("Login");
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.setBackground(Color.BLACK);
        login.setForeground(Color.WHITE);
        login.setFocusPainted(false);
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        AuthController controller = new AuthController();

        // ===== 登录逻辑（适配现有模块连接）=====
        login.addActionListener(e -> {
            String username = user.getText().trim();
            char[] pw = pass.getPassword();
            String password = new String(pw);
            java.util.Arrays.fill(pw, '\0');

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password");
                return;
            }

            User loginUser = controller.handleLogin(username, password);
            if (loginUser == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
                return;
            }

            SessionManager.login(loginUser);
            app.setCurrentUser(loginUser);
            String role = loginUser.getRole().toLowerCase();
            switch (role) {
                case "mo":
                    boolean firstLogin = JsonUtil.isFirstLogin(loginUser);
                    if (firstLogin) {
                        app.showPage("MO_FIRST");
                    } else {
                        app.showPage("MO");
                    }
                    break;
                case "ta":
                    app.showPage("TA");
                    break;
                case "admin":
                    app.showPage("ADMIN");
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Unknown role: " + role);
                    SessionManager.logout();
                    return;
            }
        });

        // ===== 返回按钮 =====
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

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            SwingUtilities.invokeLater(() -> {
                Container parent = getParent();
                if (parent != null) {
                    parent.revalidate();
                    parent.repaint();
                }
            });
        }
    }

    public void clearCredentials() {
        user.setText("");
        char[] pw = pass.getPassword();
        java.util.Arrays.fill(pw, '\0');
        pass.setText("");
    }
}