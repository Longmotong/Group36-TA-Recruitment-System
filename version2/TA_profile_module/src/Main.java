package profile_module;

import profile_module.data.JsonStore;
import profile_module.data.ProfileData;
import profile_module.ui.AppFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ignored) {
            // fall back to default
        }

        // 设置 JOptionPane 按钮为英文（防止系统语言影响）
        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        SwingUtilities.invokeLater(() -> {
            JsonStore store = new JsonStore();
            ProfileData profile = store.loadOrCreateDemo();
            AppFrame frame = new AppFrame(store, profile);
            frame.setVisible(true);
        });
    }
}

