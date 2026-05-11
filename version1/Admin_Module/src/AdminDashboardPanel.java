package com.taapp.ui.pages;

import com.taapp.data.DataStore;
import com.taapp.model.Statistics;
import com.taapp.ui.MainFrame;
import com.taapp.ui.UI;
import com.taapp.ui.components.Card;
import com.taapp.ui.components.Page;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
        h1.setFont(UI.fontMedium(38));
        h1.setForeground(UI.palette().text());
        header.add(h1, BorderLayout.NORTH);
        JLabel sub = new JLabel("Overview and quick access to TA management modules");
        sub.setFont(UI.fontPlain(16));
        sub.setForeground(UI.palette().textMuted());
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel modulesWrap = new JPanel(new GridLayout(2, 2, 14, 14));
        modulesWrap.setOpaque(false);

        modulesWrap.add(moduleCard(
                "⚇",
                "TA Workload Management",
                "View and manage TA workload distribution. Monitor assigned positions and total hours across all TAs.",
                new ButtonSpec("Go to Workload", true, () -> onNavigate.accept(MainFrame.ROUTE_WORKLOAD)),
                null
        ));

        modulesWrap.add(moduleCard(
                "⌗",
                "Statistics & Analytics",
                "Comprehensive statistics on applications, positions, and recruitment trends with visual data.",
                new ButtonSpec("View Statistics", true, () -> onNavigate.accept(MainFrame.ROUTE_STATISTICS)),
                new ButtonSpec("Reports", false, () -> {})
        ));

        modulesWrap.add(moduleCard(
                "✧",
                "AI Analysis & Insights",
                "AI-powered analysis and recommendations for optimal TA allocation and workload balance.",
                new ButtonSpec("Run AI Analysis", true, () -> onNavigate.accept(MainFrame.ROUTE_AI)),
                null
        ));

        JPanel empty = new JPanel();
        empty.setOpaque(false);
        modulesWrap.add(empty);

        root.add(modulesWrap, BorderLayout.CENTER);

        Card quick = new Card();
        quick.setLayout(new BorderLayout(0, 12));
        JLabel quickTitle = new JLabel("Quick Status Overview");
        quickTitle.setFont(UI.fontMedium(20));
        quickTitle.setForeground(UI.palette().text());
        quick.add(quickTitle, BorderLayout.NORTH);

        JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 12));
        metrics.setOpaque(false);
        metrics.add(statusItem("◉", new Color(0x16A34A), "Total Applications", String.valueOf(stats.getTotalApplications())));
        metrics.add(statusItem("↥", new Color(0x2563EB), "Approval Rate", stats.getApprovalRate() + "%"));
        metrics.add(statusItem("◈", new Color(0x9333EA), "Active TAs", String.valueOf(stats.getActiveTAs())));
        quick.add(metrics, BorderLayout.CENTER);

        root.add(quick, BorderLayout.SOUTH);

        content().add(root, BorderLayout.NORTH);
    }

    private static Card moduleCard(String icon, String title, String desc, ButtonSpec primary, ButtonSpec secondary) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 14));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel i = new JLabel(icon);
        i.setFont(UI.fontMedium(18));
        i.setForeground(UI.palette().textSoft());
        i.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel t = new JLabel(title);
        t.setFont(UI.fontMedium(26));
        t.setForeground(UI.palette().text());

        JLabel d = new JLabel("<html><div style='width:520px; line-height:1.5;'>" + desc + "</div></html>");
        d.setFont(UI.fontPlain(14));
        d.setForeground(UI.palette().textMuted());

        text.add(i);
        text.add(t);
        text.add(d);
        c.add(text, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, secondary == null ? 1 : 2, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(createButton(primary.label, primary.dark));
        if (secondary != null) {
            btnRow.add(createButton(secondary.label, secondary.dark));
        }

        ((JButton) btnRow.getComponent(0)).addActionListener(e -> primary.action.run());
        if (secondary != null) {
            ((JButton) btnRow.getComponent(1)).addActionListener(e -> secondary.action.run());
        }

        c.add(btnRow, BorderLayout.SOUTH);
        return c;
    }

    private static JPanel statusItem(String icon, Color iconColor, String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        JLabel i = new JLabel(icon);
        i.setFont(UI.fontMedium(14));
        i.setForeground(iconColor);
        i.setPreferredSize(new Dimension(20, 26));

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(UI.fontPlain(12));
        l.setForeground(UI.palette().textMuted());
        JLabel v = new JLabel(value);
        v.setFont(UI.fontMedium(34));
        v.setForeground(UI.palette().text());

        txt.add(l);
        txt.add(v);

        p.add(i);
        p.add(txt);
        p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return p;
    }

    private static JButton createButton(String text, boolean dark) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(UI.fontMedium(14));
        if (dark) {
            b.setBackground(Color.BLACK);
            b.setForeground(Color.WHITE);
            b.setBorderPainted(false);
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
            b.setBorder(BorderFactory.createLineBorder(UI.palette().borderStrong(), 1));
            b.setBorderPainted(true);
        }
        b.setPreferredSize(new Dimension(180, 38));
        return b;
    }

    private static final class ButtonSpec {
        final String label;
        final boolean dark;
        final Runnable action;

        ButtonSpec(String label, boolean dark, Runnable action) {
            this.label = label;
            this.dark = dark;
            this.action = action;
        }
    }
}
