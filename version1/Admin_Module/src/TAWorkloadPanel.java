package com.taapp.ui.pages;

import com.taapp.data.DataStore;
import com.taapp.model.AssignedPosition;
import com.taapp.model.TA;
import com.taapp.ui.Dialogs;
import com.taapp.ui.UI;
import com.taapp.ui.components.Card;
import com.taapp.ui.components.Page;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TAWorkloadPanel extends Page {
    private final List<TA> allTAs = DataStore.defaultStore().getTAs();
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField search = new JTextField(18);
    private final JComboBox<String> program = new JComboBox<>();
    private final JComboBox<String> status = new JComboBox<>(new String[]{"all", "active", "inactive"});

    private final JLabel totalTAsValue = new JLabel("0");
    private final JLabel activeTAsValue = new JLabel("0");
    private final JLabel totalWorkloadValue = new JLabel("0h");
    private final JLabel avgWorkloadValue = new JLabel("0h");

    public TAWorkloadPanel() {
        super();

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        JLabel h1 = new JLabel("TA Workload Management");
        h1.setFont(UI.fontMedium(26));
        header.add(h1, BorderLayout.NORTH);
        JLabel sub = new JLabel("View and manage TA information loaded from data（1）");
        sub.setFont(UI.fontPlain(14));
        sub.setForeground(UI.palette().textMuted());
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        Card filterCard = new Card();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterCard.add(new JLabel("Program:"));
        program.addItem("all");
        allTAs.stream().map(TA::getProgram).filter(s -> s != null && !s.isBlank()).distinct().sorted().forEach(program::addItem);
        filterCard.add(program);

        filterCard.add(new JLabel("Status:"));
        filterCard.add(status);

        filterCard.add(new JLabel("Search:"));
        filterCard.add(search);

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            program.setSelectedItem("all");
            status.setSelectedItem("all");
            search.setText("");
            refresh();
        });
        filterCard.add(clear);

        root.add(filterCard, BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 10));
        summary.setOpaque(false);
        summary.add(metricCard("Total TAs", totalTAsValue));
        summary.add(metricCard("Active TAs", activeTAsValue));
        summary.add(metricCard("Total Workload", totalWorkloadValue));
        summary.add(metricCard("Avg Workload", avgWorkloadValue));

        tableModel = new DefaultTableModel(new Object[]{"TA Name / ID", "Program", "Year", "Positions", "Workload", "Status", "Details"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewRow >= 0 && viewCol == 6) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    String key = String.valueOf(tableModel.getValueAt(modelRow, 0));
                    String studentId = key.substring(key.lastIndexOf(" / ") + 3);
                    allTAs.stream().filter(ta -> studentId.equals(ta.getStudentId())).findFirst().ifPresent(TAWorkloadPanel.this::showDetails);
                }
            }
        });

        Card tableCard = new Card();
        tableCard.setLayout(new BorderLayout(0, 8));
        JLabel t = new JLabel("TA List");
        t.setFont(UI.fontMedium(15));
        tableCard.add(t, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.add(summary, BorderLayout.NORTH);
        bottom.add(tableCard, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        content().add(root, BorderLayout.NORTH);

        program.addActionListener(e -> refresh());
        status.addActionListener(e -> refresh());
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        refresh();
    }

    private void refresh() {
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
        String prog = String.valueOf(program.getSelectedItem());
        String st = String.valueOf(status.getSelectedItem());

        List<TA> filtered = allTAs.stream().filter(ta -> {
            boolean matchesProgram = "all".equals(prog) || prog.equals(ta.getProgram());
            boolean matchesStatus = "all".equals(st) || st.equalsIgnoreCase(ta.getStatus());
            boolean matchesSearch = q.isBlank()
                    || ta.getName().toLowerCase(Locale.ROOT).contains(q)
                    || ta.getStudentId().toLowerCase(Locale.ROOT).contains(q);
            return matchesProgram && matchesStatus && matchesSearch;
        }).collect(Collectors.toList());

        tableModel.setRowCount(0);
        int active = 0;
        int totalHours = 0;
        for (TA ta : filtered) {
            if ("active".equalsIgnoreCase(ta.getStatus())) active++;
            totalHours += ta.getTotalWorkload();
            tableModel.addRow(new Object[]{
                    ta.getName() + " / " + ta.getStudentId(),
                    ta.getProgram(),
                    ta.getYear(),
                    ta.getAssignedPositions(),
                    ta.getTotalWorkload() + "h",
                    capitalize(ta.getStatus()),
                    "View"
            });
        }

        int avg = filtered.isEmpty() ? 0 : Math.round(totalHours * 1f / filtered.size());
        totalTAsValue.setText(String.valueOf(filtered.size()));
        activeTAsValue.setText(String.valueOf(active));
        totalWorkloadValue.setText(totalHours + "h");
        avgWorkloadValue.setText(avg + "h");
    }

    private void showDetails(TA ta) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel(ta.getName() + " (" + ta.getStudentId() + ")");
        title.setFont(UI.fontMedium(15));
        panel.add(title, BorderLayout.NORTH);

        DefaultTableModel dm = new DefaultTableModel(new Object[]{"Position", "Course", "Hours", "Period", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (AssignedPosition ap : ta.getPositions()) {
            String period = (ap.getStartDate().isBlank() && ap.getEndDate().isBlank()) ? "-" : ap.getStartDate() + " to " + ap.getEndDate();
            dm.addRow(new Object[]{ap.getPositionTitle(), ap.getCourse(), ap.getHours() + "h", period, capitalize(ap.getStatus())});
        }
        if (ta.getPositions().isEmpty()) {
            dm.addRow(new Object[]{"No assigned positions", "-", "-", "-", "-"});
        }

        JTable details = new JTable(dm);
        details.setRowHeight(24);
        panel.add(new JScrollPane(details), BorderLayout.CENTER);
        Dialogs.showScrollable(this, "TA Detail", panel, 740, 340);
    }

    private Card metricCard(String label, JLabel valueLabel) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 6));

        JLabel l = new JLabel(label);
        l.setFont(UI.fontPlain(12));
        l.setForeground(UI.palette().textMuted());

        valueLabel.setFont(UI.fontMedium(26));
        valueLabel.setForeground(UI.palette().text());

        c.add(l, BorderLayout.NORTH);
        c.add(valueLabel, BorderLayout.CENTER);
        return c;
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
