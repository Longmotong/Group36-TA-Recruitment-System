package com.taapp.ui.pages;

import com.taapp.data.DataStore;
import com.taapp.model.Statistics;
import com.taapp.ui.MainFrame;
import com.taapp.ui.UI;
import com.taapp.ui.components.Card;
import com.taapp.ui.components.Page;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.function.Consumer;

public class AdminDashboardPanel extends Page {
    public AdminDashboardPanel(Consumer<String> onNavigate) {
        super();
        Objects.requireNonNull(onNavigate);

        Statistics stats = DataStore.defaultStore().getStatistics();

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel h1 = new JLabel("Admin Dashboard");
        h1.setFont(UI.fontMedium(32));
        h1.setForeground(UI.palette().text());
        header.add(h1, BorderLayout.NORTH);
        JLabel sub = new JLabel("Overview and quick access to TA management modules");
        sub.setFont(UI.fontPlain(16));
        sub.setForeground(UI.palette().textMuted());
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel modules = new JPanel(new GridLayout(1, 2, 12, 12));
        modules.setOpaque(false);

        modules.add(moduleCard(
                "TA Workload Management",
                "View and manage TA workload distribution and assignments.",
                "Go to Workload",
                () -> onNavigate.accept(MainFrame.ROUTE_WORKLOAD)
        ));
        modules.add(moduleCard(
                "Statistics & Analytics",
                "Comprehensive statistics on applications, positions, and trends.",
                "View Statistics",
                () -> onNavigate.accept(MainFrame.ROUTE_STATISTICS)
        ));

        root.add(modules, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 3, 12, 12));
        bottom.setOpaque(false);
        bottom.add(metric("Total Applications", String.valueOf(stats.getTotalApplications())));
        bottom.add(metric("Approval Rate", stats.getApprovalRate() + "%"));
        bottom.add(metric("Active TAs", String.valueOf(stats.getActiveTAs())));
        root.add(bottom, BorderLayout.SOUTH);

        content().add(root, BorderLayout.NORTH);
    }

    private static Card moduleCard(String title, String desc, String buttonLabel, Runnable action) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 12));

        JPanel text = new JPanel(new BorderLayout(0, 6));
        text.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UI.fontMedium(18));
        t.setForeground(UI.palette().text());
        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(UI.fontPlain(14));
        d.setForeground(UI.palette().textMuted());
        text.add(t, BorderLayout.NORTH);
        text.add(d, BorderLayout.CENTER);
        c.add(text, BorderLayout.CENTER);

        JButton b = new JButton(buttonLabel);
        b.setBackground(UI.palette().primary());
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(UI.fontMedium(14));
        b.addActionListener(e -> action.run());
        c.add(b, BorderLayout.SOUTH);
        return c;
    }

    private static Card metric(String label, String value) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 8));
        JLabel l = new JLabel(label);
        l.setFont(UI.fontPlain(12));
        l.setForeground(UI.palette().textMuted());
        JLabel v = new JLabel(value);
        v.setFont(UI.fontMedium(26));
        v.setForeground(UI.palette().text());
        c.add(l, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        return c;
    }
}

