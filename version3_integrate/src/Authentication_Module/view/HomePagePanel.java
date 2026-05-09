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
        content.setBorder(new EmptyBorder(6, 0, 6, 0));

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
        header.setBorder(new EmptyBorder(8, 24, 8, 24));

        JLabel logo = new JLabel("TA System");
        logo.setFont(new Font("Arial", Font.BOLD, 18));
        logo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Color.WHITE);

        JButton loginBtn = blackButton("Login");
        loginBtn.setPreferredSize(new Dimension(84, 32));
        loginBtn.addActionListener(e -> app.showPage("LOGIN"));

        JButton registerBtn = whiteButton("Register");
        registerBtn.setPreferredSize(new Dimension(92, 32));
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
        wrapper.setBorder(new EmptyBorder(10, 24, 8, 24));

        JPanel hero = new JPanel();
        hero.setBackground(Color.WHITE);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(0, 40, 0, 40));

        JLabel eyebrow = new JLabel("MULTI-ROLE MANAGEMENT PLATFORM");
        eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);
        eyebrow.setFont(new Font("Arial", Font.PLAIN, 11));
        eyebrow.setForeground(new Color(90, 90, 90));
        eyebrow.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title = new JLabel("Teaching Assistant Management System");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel(
                "<html><div style='text-align:center;width:560px;'>"
                        + "A simple and efficient platform for TA, MO, and Admin management, designed with a clean black-and-white interface."
                        + "</div></html>"
        );
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(new Color(110, 110, 110));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(Color.WHITE);

        JButton startBtn = blackButton("Get Started");
        startBtn.setPreferredSize(new Dimension(128, 34));
        startBtn.addActionListener(e -> app.showPage("LOGIN"));

        JButton learnBtn = whiteButton("Learn More");
        learnBtn.setPreferredSize(new Dimension(128, 34));

        btnRow.add(startBtn);
        btnRow.add(learnBtn);

        hero.add(eyebrow);
        hero.add(Box.createVerticalStrut(8));
        hero.add(title);
        hero.add(Box.createVerticalStrut(8));
        hero.add(subtitle);
        hero.add(Box.createVerticalStrut(10));
        hero.add(btnRow);

        wrapper.add(hero, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRoleSection(String titleText, String descText) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(8, 24, 8, 24));

        JPanel inner = new JPanel();
        inner.setBackground(Color.WHITE);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(4, 40, 4, 40));

        JLabel title = new JLabel(titleText);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel desc = new JLabel(
                "<html><div style='text-align:center;width:560px;'>"
                        + descText
                        + "</div></html>"
        );
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        desc.setFont(new Font("Arial", Font.PLAIN, 12));
        desc.setForeground(new Color(110, 110, 110));
        desc.setHorizontalAlignment(SwingConstants.CENTER);

        inner.add(title);
        inner.add(Box.createVerticalStrut(4));
        inner.add(desc);

        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFooter(AppFrame app) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(8, 24, 10, 24));

        JPanel box = new JPanel();
        box.setBackground(Color.BLACK);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel txt = new JLabel("Ready to get started?");
        txt.setAlignmentX(Component.CENTER_ALIGNMENT);
        txt.setFont(new Font("Arial", Font.BOLD, 14));
        txt.setForeground(Color.WHITE);
        txt.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel hint = new JLabel("Create your account and access the system in a few steps.");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(Color.WHITE);
        hint.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btn = whiteButton("Register for Free");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(156, 32));
        btn.addActionListener(e -> app.showPage("REGISTER"));

        JPanel btnWrap = new JPanel();
        btnWrap.setBackground(Color.BLACK);
        btnWrap.add(btn);

        box.add(txt);
        box.add(Box.createVerticalStrut(4));
        box.add(hint);
        box.add(Box.createVerticalStrut(8));
        box.add(btnWrap);

        footer.add(box, BorderLayout.CENTER);
        return footer;
    }

    private JComponent buildDivider() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(3, 24, 3, 24));

        JSeparator line = new JSeparator();
        line.setForeground(new Color(210, 210, 210));
        p.add(line, BorderLayout.CENTER);

        return p;
    }

    private JButton blackButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.BLACK);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return btn;
    }

    private JButton whiteButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
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