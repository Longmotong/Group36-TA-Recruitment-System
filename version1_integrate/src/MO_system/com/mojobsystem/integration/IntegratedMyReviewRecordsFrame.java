package com.mojobsystem.integration;

import com.formdev.flatlaf.FlatClientProperties;
import com.mojobsystem.model.ApplicationItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * My Review Records Frame
 * Shows history of reviewed applications
 */
public class IntegratedMyReviewRecordsFrame extends JFrame {
    
    private final List<ApplicationItem> allApplications;
    private final IntegrationDataService dataService;
    private final JFrame parentFrame;
    private ReviewRecordsTableModel tableModel;
    private JTable table;
    
    private static final Color PAGE_BG = new Color(0xF5F5F5);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(0xE0E0E0);
    private static final Color TEXT_PRIMARY = new Color(0x000000);
    private static final Color TEXT_SECONDARY = new Color(0x666666);
    private static final int GUTTER = 40;
    
    public IntegratedMyReviewRecordsFrame(JFrame parent, List<ApplicationItem> applications, IntegrationDataService service) {
        this.parentFrame = parent;
        this.allApplications = applications;
        this.dataService = service;
        
        setTitle("MO System - My Review Records");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocation(100, 50);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        
        add(buildNorthPanel(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }
    
    private JPanel buildNorthPanel() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        nav.setPreferredSize(new Dimension(0, 56));
        nav.setOpaque(true);
        
        JLabel title = new JLabel("MO System");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(new Color(0x0F172A));
        title.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        navButtons.setOpaque(false);
        navButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        
        JButton homeBtn = createNavButton("Home", false);
        homeBtn.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });
        
        JButton jobBtn = createNavButton("Job Management", false);
        jobBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Please use Dashboard"));
        
        JButton reviewBtn = createNavButton("Application Review", false);
        reviewBtn.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });
        
        JButton logoutBtn = createNavButton("Log out", false);
        logoutBtn.setForeground(new Color(0xb91c1c));
        logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(0xfecaca)));
        logoutBtn.addActionListener(e -> System.exit(0));
        
        navButtons.add(homeBtn);
        navButtons.add(jobBtn);
        navButtons.add(reviewBtn);
        navButtons.add(logoutBtn);
        
        nav.add(title, BorderLayout.WEST);
        nav.add(navButtons, BorderLayout.EAST);
        
        return nav;
    }
    
    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 36));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 9");
        
        if (active) {
            btn.setBackground(new Color(0x111827));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(0x374151));
            btn.setBorder(BorderFactory.createLineBorder(new Color(0xd1d5db)));
        }
        return btn;
    }
    
    private JPanel buildMainContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, GUTTER, 28, GUTTER));
        
        List<ApplicationItem> reviewedItems = allApplications.stream()
                .filter(this::isReviewed)
                .collect(Collectors.toList());
        
        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);
        
        JLabel title = new JLabel("My Review Records");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        JLabel subtitle = new JLabel("History derived from reviewed application data");
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        
        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(6));
        leftHeader.add(subtitle);
        
        JPanel headerCard = new JPanel(new BorderLayout(28, 0));
        headerCard.setOpaque(true);
        headerCard.setBackground(SURFACE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(18, 22, 18, 22)
        ));
        headerCard.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        headerCard.add(leftHeader, BorderLayout.CENTER);
        
        panel.add(headerCard, BorderLayout.NORTH);
        
        int approved = (int) reviewedItems.stream()
                .filter(a -> "approved".equals(dataService.normalizeStatus(a)))
                .count();
        int rejected = (int) reviewedItems.stream()
                .filter(a -> "rejected".equals(dataService.normalizeStatus(a)))
                .count();
        
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        
        JLabel totalLabel = new JLabel(String.valueOf(reviewedItems.size()), SwingConstants.CENTER);
        JLabel approvedLabel = new JLabel(String.valueOf(approved), SwingConstants.CENTER);
        JLabel rejectedLabel = new JLabel(String.valueOf(rejected), SwingConstants.CENTER);
        
        statsRow.add(statBox(totalLabel, "Total Reviews", new Color(0xEFF6FF), new Color(0xBFDBFE), new Color(0x1D4ED8)));
        statsRow.add(statBox(approvedLabel, "Approved", new Color(0xECFDF5), new Color(0xA7F3D0), new Color(0x047857)));
        statsRow.add(statBox(rejectedLabel, "Rejected", new Color(0xFEE2E2), new Color(0xFECACA), new Color(0xDC2626)));
        
        JPanel statsCard = new JPanel(new BorderLayout());
        statsCard.setOpaque(true);
        statsCard.setBackground(SURFACE);
        statsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(16, 20, 16, 20)
        ));
        statsCard.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        statsCard.add(statsRow, BorderLayout.CENTER);
        
        panel.add(statsCard, BorderLayout.NORTH);
        panel.add(buildTablePane(reviewedItems), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel statBox(JLabel valueLabel, String caption, Color bg, Color border, Color valueColor) {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(bg);
        box.setOpaque(true);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(12, 10, 14, 10)
        ));
        box.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        cap.setForeground(new Color(0x334155));
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        inner.add(Box.createVerticalGlue());
        inner.add(valueLabel);
        inner.add(Box.createVerticalStrut(4));
        inner.add(cap);
        inner.add(Box.createVerticalGlue());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        box.add(inner, gbc);
        return box;
    }
    
    private JScrollPane buildTablePane(List<ApplicationItem> items) {
        tableModel = new ReviewRecordsTableModel(items);
        table = new JTable(tableModel);
        
        table.setRowHeight(52);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(0xD9E7FF));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (c instanceof JLabel l) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                    l.setBorder(new EmptyBorder(8, 12, 8, 4));
                }
                return c;
            }
        };
        headerRenderer.setOpaque(true);
        headerRenderer.setBackground(Color.WHITE);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setBackground(isSelected ? t.getSelectionBackground() : t.getBackground());
                c.setForeground(isSelected ? new Color(0x111827) : t.getForeground());
                if (c instanceof javax.swing.JLabel lbl) {
                    lbl.setOpaque(true);
                }
                return c;
            }
        });
        
        table.getColumnModel().getColumn(0).setCellRenderer(new RecordsAppIdRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new RecordsCourseRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new RecordsNameRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new RecordsDateRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new RecordsStatusRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RecordsReviewerRenderer());
        
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        return scrollPane;
    }
    
    private boolean isReviewed(ApplicationItem item) {
        String status = dataService.normalizeStatus(item);
        return "approved".equals(status) || "rejected".equals(status);
    }
    
    private class ReviewRecordsTableModel extends AbstractTableModel {
        private final String[] columns = {"Application ID", "Course", "TA Name", "Review Date", "Result", "Reviewer"};
        private final List<ApplicationItem> items;
        
        ReviewRecordsTableModel(List<ApplicationItem> items) {
            this.items = items;
        }
        
        @Override
        public int getRowCount() { return items.size(); }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int col) { return columns[col]; }
        
        @Override
        public Object getValueAt(int row, int col) {
            ApplicationItem item = items.get(row);
            switch (col) {
                case 0: return item.getApplicationId();
                case 1: return item.getJobSnapshot() == null ? "" : dataService.safe(item.getJobSnapshot().getCourseCode());
                case 2: return item.getApplicantSnapshot() == null ? "" : dataService.safe(item.getApplicantSnapshot().getFullName());
                case 3: return getReviewDate(item);
                case 4: return dataService.normalizeStatus(item);
                case 5: return item.getReview() == null ? "" : dataService.safe(item.getReview().getReviewedBy());
                default: return "";
            }
        }
        
        private String getReviewDate(ApplicationItem item) {
            if (item.getReview() != null && !dataService.safe(item.getReview().getReviewedAt()).isBlank()) {
                return item.getReview().getReviewedAt();
            }
            if (item.getMeta() != null && !dataService.safe(item.getMeta().getUpdatedAt()).isBlank()) {
                return item.getMeta().getUpdatedAt();
            }
            return "";
        }
    }
    
    private class RecordsAppIdRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        RecordsAppIdRenderer() { setOpaque(true); setBorder(new EmptyBorder(0, 12, 0, 8)); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setText("<html><span style='font-family:monospace;font-size:12px;color:#374151'>" + v + "</span></html>");
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }
    
    private class RecordsCourseRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        RecordsCourseRenderer() { setOpaque(true); setBorder(new EmptyBorder(0, 12, 0, 8)); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setText("<html><span style='font-weight:600'>" + v + "</span></html>");
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }
    
    private class RecordsNameRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        RecordsNameRenderer() { setOpaque(true); setBorder(new EmptyBorder(0, 12, 0, 8)); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setText("<html><span>" + v + "</span></html>");
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }
    
    private class RecordsDateRenderer extends DefaultTableCellRenderer {
        RecordsDateRenderer() { setHorizontalAlignment(SwingConstants.LEFT); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Component c2 = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            c2.setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return c2;
        }
    }
    
    private class RecordsStatusRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private static final Color APPROVED_BG = new Color(0xDCFCE7);
        private static final Color APPROVED_FG = new Color(0x166534);
        private static final Color REJECTED_BG = new Color(0xFEE2E2);
        private static final Color REJECTED_FG = new Color(0x991B1B);
        
        private final JLabel pill = new JLabel();
        
        RecordsStatusRenderer() {
            super(new GridBagLayout());
            setOpaque(true);
            pill.setOpaque(true);
            pill.setHorizontalAlignment(SwingConstants.CENTER);
            pill.setVerticalAlignment(SwingConstants.CENTER);
            pill.setBorder(new EmptyBorder(3, 10, 3, 10));
            pill.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            add(pill, gbc);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String status = v.toString().toLowerCase(Locale.ROOT);
            pill.setText(capitalize(status));

            if ("approved".equals(status)) {
                pill.setBackground(APPROVED_BG);
                pill.setForeground(APPROVED_FG);
            } else {
                pill.setBackground(REJECTED_BG);
                pill.setForeground(REJECTED_FG);
            }
            
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }
    
    private class RecordsReviewerRenderer extends DefaultTableCellRenderer {
        RecordsReviewerRenderer() { setHorizontalAlignment(SwingConstants.LEFT); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Component c2 = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            c2.setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return c2;
        }
    }
    
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
