package Authentication_Module.view;

import Authentication_Module.controller.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class RegisterPagePanel extends JPanel {
    private JTextField userField;
    private JPasswordField passField;
    private JPasswordField confirmField;
    private JComboBox<String> roleCombo;

    public RegisterPagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

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
        card.setPreferredSize(new Dimension(420, 520));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Register to get started");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(new Color(120, 120, 120));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        userField = styledField("Username");
        passField = new JPasswordField();
        JPanel passPanel = createPasswordWithEye(passField, "Password");

        confirmField = new JPasswordField();
        JPanel confirmPanel = createPasswordWithEye(confirmField, "Confirm Password");

        JLabel hint = new JLabel("At least 8 characters, including letters and numbers");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(new Color(140, 140, 140));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        roleCombo = new JComboBox<>(new String[]{"ta", "mo", "admin"});
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        roleCombo.setPreferredSize(new Dimension(0, 54));
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 18));
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setOpaque(true);
        roleCombo.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)),
                "Role"
        ));

        JButton register = new JButton("Register");
        register.setAlignmentX(Component.CENTER_ALIGNMENT);
        register.setOpaque(true);
        register.setBackground(Color.BLACK);
        register.setForeground(Color.WHITE);
        register.setFocusPainted(false);
        register.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

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
                if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                    JOptionPane.showMessageDialog(this, "Password must be at least 8 characters and include letters and numbers");
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

        JButton back = new JButton("← Back to Home");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setForeground(new Color(100, 100, 100));
        back.addActionListener(e -> app.showPage("HOME"));

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);
        card.add(Box.createVerticalStrut(20));
        card.add(userField);
        card.add(Box.createVerticalStrut(12));
        card.add(passPanel);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(12));
        card.add(confirmPanel);
        card.add(Box.createVerticalStrut(12));
        card.add(roleCombo);
        card.add(Box.createVerticalStrut(22));
        card.add(register);
        card.add(Box.createVerticalStrut(15));
        card.add(back);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    public void clearForm() {
        if (userField != null) {
            userField.setText("");
        }
        if (passField != null) {
            passField.setText("");
        }
        if (confirmField != null) {
            confirmField.setText("");
        }
        if (roleCombo != null) {
            roleCombo.setSelectedIndex(0);
        }
    }

    private JTextField styledField(String title) {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        field.setPreferredSize(new Dimension(0, 54));
        field.setMinimumSize(new Dimension(0, 54));
        field.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)),
                title
        ));
        return field;
    }

    private JPanel createPasswordWithEye(JPasswordField field, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        panel.setPreferredSize(new Dimension(0, 54));
        panel.setMinimumSize(new Dimension(0, 54));
        panel.setBackground(Color.WHITE);

        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setBorder(null);

        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)),
                title
        ));

        JButton eye = new JButton("👁");
        eye.setFocusPainted(false);
        eye.setBorderPainted(false);
        eye.setContentAreaFilled(false);
        eye.setCursor(new Cursor(Cursor.HAND_CURSOR));

        char defaultEcho = field.getEchoChar();
        eye.addActionListener(e -> {
            if (field.getEchoChar() == (char) 0) {
                field.setEchoChar(defaultEcho);
            } else {
                field.setEchoChar((char) 0);
            }
        });

        panel.add(field, BorderLayout.CENTER);
        panel.add(eye, BorderLayout.EAST);
        return panel;
    }
}
