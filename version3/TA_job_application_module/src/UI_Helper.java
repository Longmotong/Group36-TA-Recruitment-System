package TA_Job_Application_Module;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;




public class UI_Helper {
    
    
    
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
    
   
    
    public static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0);
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}

