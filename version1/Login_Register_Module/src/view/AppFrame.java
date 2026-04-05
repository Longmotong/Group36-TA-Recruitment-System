package view;

import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {

    private CardLayout layout = new CardLayout();
    private JPanel container = new JPanel(layout);

    public AppFrame() {
        setTitle("TA Recruitment System");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        container.add(new HomePagePanel(this), "HOME");
        container.add(new LoginPagePanel(this), "LOGIN");
        container.add(new RegisterPagePanel(this), "REGISTER");
        container.add(new TAHomePagePanel(this), "TA");
        container.add(new MOHomePagePanel(this), "MO");
        container.add(new AdminHomePagePanel(this), "ADMIN");

        add(container);
        layout.show(container, "HOME");

        setVisible(true);
    }

    public void showPage(String name) {
        layout.show(container, name);
    }
}