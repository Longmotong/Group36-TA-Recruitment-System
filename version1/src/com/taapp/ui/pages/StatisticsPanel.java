package com.taapp.ui.pages;

import com.taapp.data.DataStore;
import com.taapp.model.Statistics;
import com.taapp.ui.UI;
import com.taapp.ui.components.Card;
import com.taapp.ui.components.Page;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.Map;

public class StatisticsPanel extends Page {
    public StatisticsPanel() {
        super();

        DataStore store = DataStore.defaultStore();
        Statistics stats = store.getStatistics();

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel h1 = new JLabel("Statistics & Analytics");
        h1.setFont(UI.fontMedium(24));
        h1.setForeground(UI.palette().text());
        header.add(h1, BorderLayout.NORTH);
        JLabel sub = new JLabel("Comprehensive statistics on applications, positions, and TA assignments");
        sub.setFont(UI.fontPlain(14));
        sub.setForeground(UI.palette().textMuted());
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 12, 12));
        cardsRow.setOpaque(false);
        cardsRow.add(metricCard("Total Applications", String.valueOf(stats.getTotalApplications())));
        cardsRow.add(metricCard("Approval Rate", stats.getApprovalRate() + "%"));
        cardsRow.add(metricCard("Total Positions", String.valueOf(stats.getTotalPositions())));
        cardsRow.add(metricCard("Active TAs", String.valueOf(stats.getActiveTAs())));
        root.add(cardsRow, BorderLayout.CENTER);

        Card tableCard = new Card();
        tableCard.setLayout(new BorderLayout(0, 12));
        JLabel t = new JLabel("Department Breakdown");
        t.setFont(UI.fontMedium(16));
        t.setForeground(UI.palette().text());
        tableCard.add(t, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Department", "Total", "Filled", "Open", "Applications", "Fill Rate"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        stats.getDepartmentStats().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> {
                    String dept = e.getKey();
                    Statistics.DepartmentStats ds = e.getValue();
                    int open = ds.getTotal() - ds.getFilled();
                    int fillRate = ds.getTotal() > 0 ? (int) Math.round((ds.getFilled() * 100.0) / ds.getTotal()) : 0;
                    model.addRow(new Object[]{dept, ds.getTotal(), ds.getFilled(), open, ds.getApplications(), fillRate + "%"});
                });

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(900, 260));
        tableCard.add(sp, BorderLayout.CENTER);

        root.add(tableCard, BorderLayout.SOUTH);

        content().add(root, BorderLayout.NORTH);
    }

    private static Card metricCard(String label, String value) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 8));
        JLabel l = new JLabel(label);
        l.setFont(UI.fontPlain(12));
        l.setForeground(UI.palette().textMuted());
        JLabel v = new JLabel(value);
        v.setFont(UI.fontMedium(28));
        v.setForeground(UI.palette().text());
        c.add(l, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        return c;
    }
}

