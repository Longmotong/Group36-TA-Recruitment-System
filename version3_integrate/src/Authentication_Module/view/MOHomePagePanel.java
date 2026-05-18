package Authentication_Module.view;

import Authentication_Module.model.User;
import Authentication_Module.session.SessionManager;
import Authentication_Module.util.JsonUtil;

import com.mojobsystem.MoContext;
import com.mojobsystem.ui.MoShellFrame;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class MOHomePagePanel extends JPanel {

    private final AppFrame app;
    private final JLabel label;
    
    
    private MoShellFrame moFrame;
    private final AtomicBoolean openInProgress = new AtomicBoolean(false);

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
            tryOpenMoPortalWithRetry(0);
        } else {
            openInProgress.set(false);
        }
    }

    private void tryOpenMoPortalWithRetry(int attempt) {
        if (!openInProgress.compareAndSet(false, true)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (!isShowing()) {
                openInProgress.set(false);
                if (attempt < 10 && isVisible()) {
                    Timer timer = new Timer(120, e -> tryOpenMoPortalWithRetry(attempt + 1));
                    timer.setRepeats(false);
                    timer.start();
                } else if (attempt >= 10) {
                    label.setText("MO page failed to initialize. Please go back and login again.");
                }
                return;
            }
            tryOpenMoPortal();
        });
    }

    private void tryOpenMoPortal() {
        // 如果用户已经登出，不再打开门户
        if (SessionManager.getCurrentUser() == null) {
            label.setText("Session expired. Please login again.");
            openInProgress.set(false);
            return;
        }
        if (!isShowing()) {
            openInProgress.set(false);
            return;
        }
        // 再次检查登出状态（双重检查）
        if (SessionManager.getCurrentUser() == null) {
            label.setText("Session expired. Please login again.");
            openInProgress.set(false);
            return;
        }
        User authUser = SessionManager.getCurrentUser();
        if (authUser == null) {
            label.setText("No user logged in. Please login first.");
            openInProgress.set(false);
            return;
        }

        String moUserId = authUser.getSystemUserId();
        if (moUserId == null || moUserId.isBlank()) {
            moUserId = JsonUtil.findMoUserIdByAccountUsername(authUser.getUsername());
        }
        if (moUserId == null || moUserId.isBlank()) {
            label.setText("Cannot resolve MO user id. Check data/users/mo/*.json for this account.");
            openInProgress.set(false);
            return;
        }
        MoContext.setCurrentMoUserId(moUserId);

        SwingUtilities.invokeLater(() -> {
            
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                System.err.println("[MO] FlatLightLaf failed: " + e.getMessage());
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }

           
            try {
                if (moFrame == null || !moFrame.isDisplayable()) {
                    System.out.println("[MO] Creating new MoShellFrame...");
                    Runnable endSession = () -> {
                        System.out.println("[MO] Session ended");
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
                    System.out.println("[MO] MoShellFrame created and shown");
                } else {
                    moFrame.setVisible(true);
                    moFrame.toFront();
                }
                app.setVisible(false);
            } catch (Exception ex) {
                System.err.println("[MO] Error creating MoShellFrame: " + ex.getMessage());
                ex.printStackTrace();
                label.setText("Error opening MO Portal: " + ex.getMessage());
                app.setVisible(true);
            } finally {
                openInProgress.set(false);
            }
        });
    }
}
