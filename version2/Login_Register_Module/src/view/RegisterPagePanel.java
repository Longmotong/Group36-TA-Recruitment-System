package view;

import controller.AuthController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class RegisterPagePanel extends JPanel {

    public RegisterPagePanel(AppFrame app) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== 全局去灰（关键）=====
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("TextField.background", Color.WHITE);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(30, 40, 30, 40)
        ));

        // ✅ 修复标题被截断
        card.setPreferredSize(new Dimension(420, 520));

        // ===== 标题 =====
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Register to get started");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(new Color(120, 120, 120));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== 输入框（嵌入标题风格）=====
        JTextField user = styledField("Username");
        JPasswordField pass = styledPassword("Password");
        JPasswordField confirm = styledPassword("Confirm Password");

        // ===== 密码提示（修复对齐+截断）=====
        JLabel hint = new JLabel("At least 8 characters, including letters and numbers");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(new Color(140, 140, 140));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // ===== Role（完全统一风格）=====
        JComboBox<String> role = new JComboBox<>(new String[]{"ta", "mo"});
        role.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        role.setBackground(Color.WHITE);
        role.setOpaque(true);
        role.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)),
                "Role"
        ));

        // ===== 注册按钮 =====
        JButton register = new JButton("Register");
        register.setAlignmentX(Component.CENTER_ALIGNMENT);
        register.setBackground(Color.BLACK);
        register.setForeground(Color.WHITE);
        register.setFocusPainted(false);
        register.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        AuthController controller = new AuthController();

        register.addActionListener(e -> {
            try {
                String username = user.getText().trim();
                String password = String.valueOf(pass.getPassword());
                String confirmPwd = String.valueOf(confirm.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Username and password cannot be empty");
                    return;
                }

                // ✅ 密码规则：>=8位 + 字母+数字
                if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                    JOptionPane.showMessageDialog(this,
                            "Password must be at least 8 characters and include letters and numbers");
                    return;
                }

                if (!password.equals(confirmPwd)) {
                    JOptionPane.showMessageDialog(this, "Passwords do not match");
                    return;
                }

                boolean ok = controller.handleRegister(
                        username,
                        password,
                        (String) role.getSelectedItem()
                );

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Registration successful");
                    app.showPage("LOGIN");
                } else {
                    JOptionPane.showMessageDialog(this, "User already exists");
                }

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
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

        card.add(Box.createVerticalStrut(20));
        card.add(user);

        card.add(Box.createVerticalStrut(12));
        card.add(pass);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);

        card.add(Box.createVerticalStrut(12));
        card.add(confirm);

        card.add(Box.createVerticalStrut(12));
        card.add(role);

        card.add(Box.createVerticalStrut(22));
        card.add(register);

        card.add(Box.createVerticalStrut(15));
        card.add(back);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    // ===== 输入框（嵌入标题）=====
    private JTextField styledField(String title) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)),
                title
        ));
        return field;
    }

    private JPasswordField styledPassword(String title) {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)),
                title
        ));
        return field;
    }
}