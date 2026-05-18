

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



/**
 * UI 辅助方法类
 * 提供常用的 UI 组件创建方法
 */
public class UI_Helper {
    
    // ==================== 按钮创建 ====================
    
    public static JButton createDarkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(UI_Constants.DARK_BUTTON);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UI_Constants.DARK_BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UI_Constants.DARK_BUTTON);
            }
        });
        return btn;
    }

    /** 与 createDarkButton 相同样式，字号略大（仪表盘等主操作区） */
    public static JButton createDarkButtonLarge(String text) {
        JButton btn = createDarkButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        return btn;
    }
    
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(UI_Constants.PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UI_Constants.PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UI_Constants.PRIMARY_COLOR);
            }
        });
        return btn;
    }
    
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(UI_Constants.TEXT_SECONDARY);
        btn.setBackground(UI_Constants.CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UI_Constants.BG_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UI_Constants.CARD_BG);
            }
        });
        return btn;
    }
    
    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(UI_Constants.TEXT_PRIMARY);
        btn.setBackground(UI_Constants.BG_COLOR);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
            new EmptyBorder(12, 16, 12, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UI_Constants.PRIMARY_COLOR);
                btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UI_Constants.BG_COLOR);
                btn.setForeground(UI_Constants.TEXT_PRIMARY);
            }
        });
        return btn;
    }
    
    // ==================== 卡片和容器 ====================
    
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(UI_Constants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(0, 0, 0, 0)
        ));
        return card;
    }
    
    public static JPanel createPagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        return panel;
    }
    
    // ==================== 标签 ====================
    
    public static JLabel createTag(String text, Color bg, Color fg) {
        JLabel tag = new JLabel(text);
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(fg);
        tag.setOpaque(true);
        tag.setBackground(bg);
        tag.setBorder(new EmptyBorder(5, 10, 5, 10));
        return tag;
    }
    
    public static JLabel createSkillTag(String text) {
        JLabel tag = new JLabel(text);
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(UI_Constants.TEXT_SECONDARY);
        tag.setOpaque(true);
        tag.setBackground(UI_Constants.BG_COLOR);
        tag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return tag;
    }
    
    // ==================== 文本框 ====================
    
    public static JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setRows(rows);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(10, 12, 10, 12)
        ));
        return area;
    }
    
    // ==================== 其他辅助方法 ====================
    
    public static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0);
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
