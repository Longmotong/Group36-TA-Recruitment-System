package com.taapp.ui.pages;

import com.taapp.data.TaUserRepository;
import com.taapp.ui.UI;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

public class TAUserDetailsPanel extends JPanel {
    public TAUserDetailsPanel(String key) {
        setLayout(new BorderLayout());
        setOpaque(false);
        Map<String, Object> user = new TaUserRepository().findByKey(key);
        add(buildContent(key, user), BorderLayout.CENTER);
    }

    private JPanel buildContent(String key, Map<String, Object> user) {
        JPanel wrap = new JPanel(new BorderLayout(0, 14));
        wrap.setOpaque(true);
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        Map<String, Object> profile = map(user.get("profile"));
        Map<String, Object> academic = map(user.get("academic"));

        List<Map<String, Object>> applications = new TaUserRepository().findApplicationsForKey(key);
        int applicationCount = 0;
        int acceptedCount = 0;
        int pendingCount = 0;
        for (Map<String, Object> app : applications) {
            applicationCount++;
            String current = normalizeStatus(text(map(app.get("status")), "current", text(app, "status", "")));
            if ("accepted".equalsIgnoreCase(current)) {
                acceptedCount++;
            } else if ("pending".equalsIgnoreCase(current)) {
                pendingCount++;
            }
        }

        JLabel head = new JLabel("User Profile Details");
        head.setFont(UI.moFontBold(18));
        head.setForeground(new Color(0x111827));
        head.setBorder(new EmptyBorder(0, 2, 2, 2));

        JPanel topBar = new JPanel(new BorderLayout(0, 0));
        topBar.setOpaque(false);
        topBar.add(head, BorderLayout.WEST);
        wrap.add(topBar, BorderLayout.NORTH);

        JPanel summary = new JPanel(new GridLayout(2, 3, 10, 10));
        summary.setOpaque(false);
        summary.add(detailCard("User ID", text(user, "userId", "-")));
        summary.add(detailCard("Login ID", text(user, "loginId", "-")));
        summary.add(detailCard("Full Name", text(profile, "fullName", "-")));
        summary.add(detailCard("Department", text(profile, "department", "-")));
        summary.add(detailCard("Major", text(profile, "programMajor", "-")));
        summary.add(detailCard("Year", text(profile, "year", "-")));

        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 10));
        stats.setOpaque(false);
        stats.add(metricCard("Applications", String.valueOf(applicationCount)));
        stats.add(metricCard("Accepted", String.valueOf(acceptedCount)));
        stats.add(metricCard("Pending", String.valueOf(pendingCount)));
        stats.add(metricCard("GPA", text(academic, "gpa", "-")));

        JPanel infoWrap = new JPanel(new BorderLayout(0, 10));
        infoWrap.setOpaque(false);
        infoWrap.add(summary, BorderLayout.CENTER);
        infoWrap.add(stats, BorderLayout.SOUTH);

        wrap.add(infoWrap, BorderLayout.NORTH);
        wrap.add(buildApplicationsSection(applications, acceptedCount), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildApplicationsSection(List<Map<String, Object>> apps, int acceptedCount) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Application ID", "Job", "Course", "Department", "Submitted", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Map<String, Object> app : apps) {
            Map<String, Object> status = map(app.get("status"));
            String current = normalizeStatus(text(status, "current", text(app, "status", "")));
            Map<String, Object> job = map(app.get("jobSnapshot"));
            model.addRow(new Object[]{
                    text(app, "applicationId", "-"),
                    text(job, "title", text(app, "jobId", "-")),
                    text(job, "courseName", text(job, "courseCode", "-")),
                    text(job, "department", "-"),
                    text(map(app.get("meta")), "submittedAt", text(app, "appliedDate", "-")),
                    current
            });
        }

        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"No applications", "-", "-", "-", "-", "-"});
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UI.moFontBold(12));
                l.setForeground(new Color(0x4B5563));
                l.setBackground(Color.WHITE);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                return l;
            }
        });

        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(true);
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);

        JLabel title = new JLabel("All Applications");
        title.setFont(UI.moFontBold(16));
        title.setForeground(new Color(0x111827));
        sectionHeader.add(title, BorderLayout.WEST);

        if (acceptedCount >= 3) {
            sectionHeader.add(warningBanner("Application limit reached."), BorderLayout.EAST);
        }

        section.add(sectionHeader, BorderLayout.NORTH);
        section.add(new JScrollPane(table), BorderLayout.CENTER);
        return section;
    }

    private JPanel detailCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(true);
        card.setBackground(new Color(0xF9FAFB));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel l = new JLabel(label);
        l.setFont(UI.moFontBold(12));
        l.setForeground(new Color(0x6B7280));

        JLabel v = new JLabel(value == null ? "-" : value);
        v.setFont(UI.moFontBold(13));
        v.setForeground(new Color(0x111827));

        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel metricCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel v = new JLabel(value == null ? "-" : value, SwingConstants.CENTER);
        v.setFont(UI.moFontBold(20));
        v.setForeground(new Color(0x111827));

        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(UI.moFontBold(11));
        l.setForeground(new Color(0x6B7280));

        card.add(v, BorderLayout.CENTER);
        card.add(l, BorderLayout.SOUTH);
        return card;
    }

    private JPanel warningBanner(String message) {
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setOpaque(true);
        banner.setBackground(new Color(0xFFF7ED));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFCD34D)),
                new EmptyBorder(6, 10, 6, 10)
        ));
        banner.setPreferredSize(new java.awt.Dimension(220, 32));
        banner.setMaximumSize(new java.awt.Dimension(220, 32));

        JLabel dot = new JLabel("●");
        dot.setFont(UI.moFontBold(10));
        dot.setForeground(new Color(0xD97706));
        dot.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel label = new JLabel(message, SwingConstants.LEFT);
        label.setFont(UI.moFontBold(12));
        label.setForeground(new Color(0x92400E));

        banner.add(dot, BorderLayout.WEST);
        banner.add(label, BorderLayout.CENTER);
        return banner;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object v) { return v instanceof Map ? (Map<String, Object>) v : Map.of(); }

    private static String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) return "-";
        String v = raw.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (v) {
            case "pending" -> "pending";
            case "rejected" -> "rejected";
            case "accepted", "approved" -> "accepted";
            default -> v;
        };
    }

    private static String text(Map<String, Object> obj, String key, String def) { Object v = obj == null ? null : obj.get(key); return v == null ? def : String.valueOf(v); }
}
