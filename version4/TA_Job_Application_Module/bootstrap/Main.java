package TA_Job_Application_Module.bootstrap;

import TA_Job_Application_Module.portal.TAPortalApp;
import TA_Job_Application_Module.service.DataService;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        // 保存原来的 Look and Feel（可能是认证模块设置的）
        String oldLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        SwingUtilities.invokeLater(() -> {
            TAPortalApp app = new TAPortalApp("jobs");
            app.setVisible(true);
            
            // 当窗口关闭时，清除 AI 分析结果并恢复 Look and Feel
            app.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // 清除 AI 分析结果
                    DataService.getInstance().clearCachedAIResults();
                    
                    try {
                        UIManager.setLookAndFeel(oldLookAndFeel);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
    }
}
