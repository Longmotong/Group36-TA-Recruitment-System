package Authentication_Module.view;

import Authentication_Module.controller.AuthController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class RegisterPagePanel extends JPanel {

    private final JTextField userField;
    private final JPasswordField passField;
    private final JPasswordField confirmField;
    private final JComboBox<String> roleCombo;
    private final AppFrame app;

    public RegisterPagePanel(AppFrame app) {
        this.app = app;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setDoubleBuffered(true);

        // ===== 外层容器（用于居中）=====
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(30, 40, 30, 40)
        ));

        card.setMaximumSize(new Dimension(AuthUiDimensions.REGISTER_CARD_W, 620));
        card.setPreferredSize(new Dimension(AuthUiDimensions.REGISTER_CARD_W, AuthUiDimensions.REGISTER_CARD_H));

        // ===== 标题 =====
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_REGISTER_TITLE));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Register to get started");
        desc.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        desc.setForeground(new Color(120, 120, 120));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== 输入框（嵌入标题风格）=====
        userField = styledField("Username");
        passField = styledPassword("Password");
        confirmField = styledPassword("Confirm Password");

        // ===== 密码提示（修复对齐+截断）=====
        JLabel hint = new JLabel("At least 8 characters, including letters and numbers");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(new Color(140, 140, 140));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // ===== Role（完全统一风格）=====
        roleCombo = new JComboBox<>(new String[]{"ta", "mo"});
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, AuthUiDimensions.REGISTER_FIELD_H));
        roleCombo.setPreferredSize(new Dimension(AuthUiDimensions.REGISTER_FIELD_W, AuthUiDimensions.REGISTER_FIELD_H));
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setOpaque(true);
        roleCombo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Role"
        ));

        // ===== 注册按钮 =====
        JButton register = new JButton("Register");
        register.setAlignmentX(Component.CENTER_ALIGNMENT);
        register.setOpaque(true);
        register.setBackground(Color.BLACK);
        register.setForeground(Color.WHITE);
        register.setFocusPainted(false);
        register.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_REGISTER_BTN));
        register.setMaximumSize(new Dimension(Integer.MAX_VALUE, AuthUiDimensions.REGISTER_BTN_H));
        register.setPreferredSize(new Dimension(AuthUiDimensions.REGISTER_FIELD_W, AuthUiDimensions.REGISTER_BTN_H));

        AuthController controller = new AuthController();

        register.addActionListener(e -> {
            try {
                String username = userField.getText().trim();
                String password = String.valueOf(passField.getPassword());
                String confirmPwd = String.valueOf(confirmField.getPassword());

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
                        ((String) roleCombo.getSelectedItem()).toUpperCase()
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
        back.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        back.addActionListener(e -> app.showPage("HOME"));

        // ===== 组装 =====
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);

        card.add(Box.createVerticalStrut(20));
        card.add(userField);

        card.add(Box.createVerticalStrut(12));
        card.add(passField);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);

        card.add(Box.createVerticalStrut(12));
        card.add(confirmField);

        card.add(Box.createVerticalStrut(12));
        card.add(roleCombo);

        card.add(Box.createVerticalStrut(22));
        card.add(register);

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
                refreshBorders();
                revalidate();
                repaint();
            });
        }
    }

    private void refreshBorders() {
        if (userField != null) {
            userField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Username"
            ));
        }
        if (passField != null) {
            passField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Password"
            ));
        }
        if (confirmField != null) {
            confirmField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Confirm Password"
            ));
        }
        if (roleCombo != null) {
            roleCombo.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Role"
            ));
        }
        revalidate();
        repaint();
    }

    // ===== 输入框（嵌入标题）=====
    private JTextField styledField(String title) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, AuthUiDimensions.REGISTER_FIELD_H));
        field.setPreferredSize(new Dimension(AuthUiDimensions.REGISTER_FIELD_W, AuthUiDimensions.REGISTER_FIELD_H));
        field.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        field.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title
        ));
        return field;
    }

    private JPasswordField styledPassword(String title) {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, AuthUiDimensions.REGISTER_FIELD_H));
        field.setPreferredSize(new Dimension(AuthUiDimensions.REGISTER_FIELD_W, AuthUiDimensions.REGISTER_FIELD_H));
        field.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        field.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title
        ));
        return field;
    }
}
