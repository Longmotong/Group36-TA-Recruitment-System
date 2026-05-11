package Authentication_Module.view;

import Authentication_Module.session.SessionManager;
import com.taapp.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class AdminHomePagePanel extends JPanel {
    private AppFrame appFrame;

    public AdminHomePagePanel(AppFrame app) {
        this.appFrame = app;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        
        JLabel loading = new JLabel("Loading Admin System...", SwingConstants.CENTER);
        loading.setFont(new Font("Arial", Font.PLAIN, 16));
        loading.setForeground(Color.GRAY);
        add(loading, BorderLayout.CENTER);
    }

    
    public void launchAdminWindow() {
        
        com.taapp.model.CurrentUserHolder.setCurrentUser(SessionManager.getCurrentUser());

        
        SwingUtilities.invokeLater(() -> {
           
            appFrame.setVisible(false);

            
            MainFrame adminFrame = new MainFrame(appFrame);
            adminFrame.setVisible(true);
        });
    }
}