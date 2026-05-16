package Authentication_Module.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class HomePagePanel extends JPanel {

    public HomePagePanel(AppFrame app) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(8, 0, 16, 0));

        content.add(buildHeader(app));
        content.add(buildHero(app));
        content.add(buildDivider());
        content.add(buildRoleSection(
                "01  TA Portal",
                "Manage personal information, teaching tasks, and assigned work with a clean and focused workflow."
        ));
        content.add(buildDivider());
        content.add(buildRoleSection(
                "02  MO Portal",
                "Oversee recruitment progress, review applications, and coordinate management tasks efficiently."
        ));
        content.add(buildDivider());
        content.add(buildRoleSection(
                "03  Admin Portal",
                "Control users, permissions, and system-level settings through a structured administrative panel."
        ));
        content.add(buildFooter(app));

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildHeader(AppFrame app) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(6, 40, 6, 40));

        JLabel logo = new JLabel("TA System");
        logo.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Color.WHITE);

        JButton loginBtn = blackButton("Login");
        loginBtn.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_HOME_HEADER_BTN));
        loginBtn.setPreferredSize(new Dimension(AuthUiDimensions.HOME_HEADER_BTN_LOGIN_W, AuthUiDimensions.HOME_HEADER_BTN_H));
        loginBtn.addActionListener(e -> app.showPage("LOGIN"));

        JButton registerBtn = whiteButton("Register");
        registerBtn.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_HOME_HEADER_BTN));
        registerBtn.setPreferredSize(new Dimension(AuthUiDimensions.HOME_HEADER_BTN_REGISTER_W, AuthUiDimensions.HOME_HEADER_BTN_H));
        registerBtn.addActionListener(e -> app.showPage("REGISTER"));

        actions.add(loginBtn);
        actions.add(registerBtn);

        header.add(logo, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel buildHero(AppFrame app) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(10, 40, 6, 40));

       
        JPanel hero = new JPanel(new GridBagLayout());
        hero.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);

        JLabel eyebrow = new JLabel("MULTI-ROLE MANAGEMENT PLATFORM");
        eyebrow.setFont(new Font("Arial", Font.PLAIN, 11));
        eyebrow.setForeground(new Color(90, 90, 90));
        eyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        hero.add(eyebrow, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 0, 0);
        JLabel title = new JLabel("Teaching Assistant Management System");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        hero.add(title, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(6, 0, 0, 0);
        JLabel subtitle = new JLabel("A simple and efficient platform for TA, MO, and Admin management.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 11));
        subtitle.setForeground(new Color(110, 110, 110));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setMaximumSize(new Dimension(500, 40));
        hero.add(subtitle, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 0, 0);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(Color.WHITE);

        JButton startBtn = blackButton("Get Started");
        startBtn.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_HOME_HERO_BTN));
        startBtn.setPreferredSize(new Dimension(AuthUiDimensions.HOME_HERO_BTN_W, AuthUiDimensions.HOME_HERO_BTN_H));
        startBtn.addActionListener(e -> app.showPage("LOGIN"));

        JButton learnBtn = whiteButton("Learn More");
        learnBtn.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_HOME_HERO_BTN));
        learnBtn.setPreferredSize(new Dimension(AuthUiDimensions.HOME_HERO_BTN_W, AuthUiDimensions.HOME_HERO_BTN_H));

        btnRow.add(startBtn);
        btnRow.add(learnBtn);
        hero.add(btnRow, gbc);

        wrapper.add(hero, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRoleSection(String titleText, String descText) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(10, 40, 10, 40));

        
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        inner.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);

        JLabel desc = new JLabel(descText);
        desc.setFont(new Font("Arial", Font.PLAIN, 10));
        desc.setForeground(new Color(110, 110, 110));
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        desc.setMaximumSize(new Dimension(400, 40));
        inner.add(desc, gbc);

        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFooter(AppFrame app) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(12, 40, 20, 40));

        JPanel box = new JPanel();
        box.setBackground(Color.BLACK);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel txt = new JLabel("Ready to get started?");
        txt.setAlignmentX(Component.CENTER_ALIGNMENT);
        txt.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel hint = new JLabel("Create your account and access the system.");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setFont(new Font("Arial", Font.PLAIN, 10));
        hint.setForeground(Color.WHITE);

        JButton btn = whiteButton("Register for Free");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(150, 34));
        btn.addActionListener(e -> app.showPage("REGISTER"));

        JPanel btnWrap = new JPanel();
        btnWrap.setBackground(Color.BLACK);
        btnWrap.add(btn);

        box.add(txt);
        box.add(Box.createVerticalStrut(6));
        box.add(hint);
        box.add(Box.createVerticalStrut(10));
        box.add(btnWrap);

        footer.add(box, BorderLayout.CENTER);
        return footer;
    }

    private JComponent buildDivider() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(4, 40, 4, 40));

        JSeparator line = new JSeparator();
        line.setForeground(new Color(210, 210, 210));
        p.add(line, BorderLayout.CENTER);

        return p;
    }

    private JButton blackButton(String text) {
        JButton btn = new JButton(text);
        btn.setOpaque(true);
        btn.setBackground(Color.BLACK);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_HOME_HEADER_BTN));
        return btn;
    }

    private JButton whiteButton(String text) {
        JButton btn = new JButton(text);
        btn.setOpaque(true);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        btn.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_HOME_HEADER_BTN));
        return btn;
    }

    
    public void refreshButtonStyles() {
        SwingUtilities.invokeLater(() -> {
            refreshAllButtons(this);
            revalidate();
            repaint();
        });
    }

    private void refreshAllButtons(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                applyButtonStyle(btn);
            } else if (comp instanceof Container) {
                refreshAllButtons((Container) comp);
            }
        }
    }

    private void applyButtonStyle(JButton btn) {
        
        boolean isMainButton = btn.getForeground().equals(Color.WHITE);

        if (isMainButton) {
           
            btn.setOpaque(true);
            btn.setBackground(Color.BLACK);
            btn.setForeground(Color.WHITE);
            btn.setContentAreaFilled(true);
            btn.setBorderPainted(false);
            btn.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_HOME_HEADER_BTN));
        } else {
           
            btn.setOpaque(true);
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setContentAreaFilled(true);
            btn.setBorderPainted(true);
            btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            btn.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_HOME_HEADER_BTN));
        }
        btn.repaint();
    }
}