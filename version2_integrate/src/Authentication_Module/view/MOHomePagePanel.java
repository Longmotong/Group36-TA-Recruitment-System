package Authentication_Module.view;

import Authentication_Module.model.User;
import Authentication_Module.session.SessionManager;
import Authentication_Module.util.JsonUtil;

import com.mojobsystem.MoContext;
import com.mojobsystem.ui.MoShellFrame;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;


public class MOHomePagePanel extends JPanel {

    private final AppFrame app;
    private final JLabel label;
    
    
    private MoShellFrame moFrame;

    public MOHomePagePanel(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        label = new JLabel("MO Portal - Loading...", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            SwingUtilities.invokeLater(this::tryOpenMoPortal);
        }
    }

    private void tryOpenMoPortal() {
        // 如果用户已经登出，不再打开门户
        if (SessionManager.getCurrentUser() == null) {
            return;
        }
        if (!isShowing()) {
            return;
        }
        // 再次检查登出状态（双重检查）
        if (SessionManager.getCurrentUser() == null) {
            return;
        }
        User authUser = SessionManager.getCurrentUser();
        if (authUser == null) {
            label.setText("No user logged in. Please login first.");
            return;
        }

        String moUserId = authUser.getSystemUserId();
        if (moUserId == null || moUserId.isBlank()) {
            moUserId = JsonUtil.findMoUserIdByAccountUsername(authUser.getUsername());
        }
        if (moUserId == null || moUserId.isBlank()) {
            label.setText("Cannot resolve MO user id. Check data/users/mo/*.json for this account.");
            return;
        }
        MoContext.setCurrentMoUserId(moUserId);

        SwingUtilities.invokeLater(() -> {
            
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }

           
            if (moFrame == null || !moFrame.isDisplayable()) {
                Runnable endSession = () -> {
                    SessionManager.logout();
                    app.setVisible(true);
                    app.showPage("HOME");
                    
                    if (moFrame != null) {
                        moFrame.dispose();
                        moFrame = null;
                    }
                };
                moFrame = new MoShellFrame(endSession);
                moFrame.setLocationRelativeTo(null);
                moFrame.setVisible(true);
            } else {
                moFrame.setVisible(true);
                moFrame.toFront();
            }
            app.setVisible(false);
        });
    }
}
