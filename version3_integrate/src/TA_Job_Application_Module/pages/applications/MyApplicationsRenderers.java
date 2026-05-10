package TA_Job_Application_Module.pages.applications;

import TA_Job_Application_Module.ui.UI_Constants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Locale;

public final class MyApplicationsRenderers {
    private MyApplicationsRenderers() {
    }

    public static class StatusBadgeRenderer implements TableCellRenderer {
        public static final int BADGE_OUTER_WIDTH = 168;
        private static final int BADGE_H = 30;

        private final JPanel wrap = new JPanel(new java.awt.BorderLayout());
        private final JLabel inner = new JLabel("", SwingConstants.CENTER);

        public StatusBadgeRenderer() {
            wrap.setOpaque(true);
            wrap.setBorder(new EmptyBorder(0, 12, 0, 8));
            inner.setFont(new Font("Segoe UI", Font.BOLD, 12));
            inner.setOpaque(true);
            inner.setBorder(new EmptyBorder(6, 8, 6, 8));
            inner.setPreferredSize(new Dimension(BADGE_OUTER_WIDTH, BADGE_H));
            inner.setMinimumSize(new Dimension(BADGE_OUTER_WIDTH, BADGE_H));
            inner.setMaximumSize(new Dimension(BADGE_OUTER_WIDTH, BADGE_H));
            wrap.add(inner, java.awt.BorderLayout.WEST);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String label = value != null ? value.toString() : "";
            String key = label.toLowerCase(Locale.ROOT);
            Color bg;
            Color fg;
            if (key.contains("offer") && key.contains("pending")) {
                bg = new Color(209, 250, 229);
                fg = new Color(5, 122, 85);
            } else if (key.contains("pending")) {
                bg = new Color(254, 243, 199);
                fg = new Color(180, 83, 9);
            } else if (key.contains("under review")) {
                bg = new Color(219, 234, 254);
                fg = new Color(29, 78, 216);
            } else if (key.contains("accepted")) {
                bg = new Color(209, 250, 229);
                fg = new Color(5, 122, 85);
            } else if (key.contains("rejected")) {
                bg = new Color(254, 226, 226);
                fg = new Color(185, 28, 28);
            } else {
                bg = UI_Constants.BG_COLOR;
                fg = UI_Constants.TEXT_SECONDARY;
            }
            inner.setText(label);
            inner.setBackground(bg);
            inner.setForeground(fg);
            inner.setToolTipText(label.isEmpty() ? null : label);
            wrap.setBackground(isSelected ? table.getSelectionBackground() : UI_Constants.CARD_BG);
            return wrap;
        }
    }

    public static class CancelButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton btn = new JButton("Withdraw");

        public CancelButtonRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(true);
            setBackground(UI_Constants.CARD_BG);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(UI_Constants.DANGER_COLOR);
            btn.setOpaque(true);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setBorder(new EmptyBorder(4, 10, 4, 10));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setVisible(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : UI_Constants.CARD_BG);
            boolean show = "withdraw".equals(value);
            btn.setVisible(show);
            removeAll();
            if (show) {
                add(Box.createHorizontalGlue());
                add(btn);
                add(Box.createHorizontalGlue());
            }
            return this;
        }
    }

    public static class ViewLinkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setText("<html><font color='#4F46E5'><u>View</u></font></html>");
            c.setHorizontalAlignment(SwingConstants.LEFT);
            c.setBorder(new EmptyBorder(0, 12, 0, 12));
            c.setIcon(null);
            c.setOpaque(true);
            c.setBackground(isSelected ? table.getSelectionBackground() : UI_Constants.CARD_BG);
            return c;
        }
    }

    public static class DraftActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setText("<html><u>Edit</u></html>");
            c.setForeground(UI_Constants.INFO_COLOR);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            c.setVerticalAlignment(JLabel.CENTER);
            c.setBorder(new EmptyBorder(0, 0, 0, 0));
            c.setOpaque(true);
            c.setBackground(isSelected ? table.getSelectionBackground() : UI_Constants.CARD_BG);
            c.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return c;
        }
    }

    public static class DraftDeleteRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setText("<html><u>Delete</u></html>");
            c.setForeground(UI_Constants.DANGER_COLOR);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            c.setVerticalAlignment(JLabel.CENTER);
            c.setBorder(new EmptyBorder(0, 0, 0, 0));
            c.setOpaque(true);
            c.setBackground(isSelected ? table.getSelectionBackground() : UI_Constants.CARD_BG);
            c.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return c;
        }
    }
}
