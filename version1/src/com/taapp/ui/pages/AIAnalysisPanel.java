package com.taapp.ui.pages;

import com.taapp.data.DataStore;
import com.taapp.model.Statistics;
import com.taapp.ui.Dialogs;
import com.taapp.ui.UI;
import com.taapp.ui.components.Card;
import com.taapp.ui.components.Page;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class AIAnalysisPanel extends Page {
    private final JLabel statusLabel = new JLabel("Ready to Run AI Analysis");
    private final JProgressBar progress = new JProgressBar();
    private final JButton run = new JButton("Run Analysis");
    private final JButton exportPdf = new JButton("Export PDF");
    private final JButton exportExcel = new JButton("Export Excel");

    private boolean analysisComplete = false;

    public AIAnalysisPanel() {
        super();

        Statistics stats = DataStore.defaultStore().getStatistics();

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel h1 = new JLabel("AI Analysis & Insights");
        h1.setFont(UI.fontMedium(32));
        h1.setForeground(UI.palette().text());
        header.add(h1, BorderLayout.NORTH);
        JLabel sub = new JLabel("AI-powered analysis and recommendations for TA management optimization");
        sub.setFont(UI.fontPlain(16));
        sub.setForeground(UI.palette().textMuted());
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        Card control = new Card();
        control.setLayout(new BorderLayout(0, 10));
        JLabel ct = new JLabel("AI Analysis Engine");
        ct.setFont(UI.fontMedium(16));
        ct.setForeground(UI.palette().text());
        control.add(ct, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton upload = new JButton("Upload Data");
        upload.setFocusPainted(false);
        upload.addActionListener(e -> Dialogs.showMessage(this, "Upload", "Upload is not implemented in this demo.", SwingConstants.CENTER));

        run.setBackground(UI.palette().primary());
        run.setForeground(Color.WHITE);
        run.setFocusPainted(false);
        run.setBorderPainted(false);
        run.setFont(UI.fontMedium(14));
        run.addActionListener(e -> runAnalysis());

        actions.add(upload);
        actions.add(run);
        control.add(actions, BorderLayout.SOUTH);

        JPanel status = new JPanel(new BorderLayout(0, 6));
        status.setOpaque(false);
        statusLabel.setFont(UI.fontPlain(14));
        statusLabel.setForeground(UI.palette().textSoft());
        status.add(statusLabel, BorderLayout.NORTH);
        progress.setVisible(false);
        progress.setIndeterminate(true);
        status.add(progress, BorderLayout.SOUTH);
        control.add(status, BorderLayout.CENTER);

        root.add(control, BorderLayout.CENTER);

        JPanel scores = new JPanel(new GridLayout(1, 3, 12, 12));
        scores.setOpaque(false);
        scores.add(scoreCard("System Health Score", "87/100", "Overall system efficiency is good"));
        scores.add(scoreCard("Workload Balance", "72/100", "Some TAs may be overloaded"));
        scores.add(scoreCard("Resource Utilization", "80%", "Efficient use of TA resources"));
        root.add(scores, BorderLayout.SOUTH);

        Card exports = new Card();
        exports.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        exportPdf.setEnabled(false);
        exportExcel.setEnabled(false);
        exportPdf.addActionListener(e -> Dialogs.showMessage(this, "Export", "Export PDF is not implemented in this demo.", SwingConstants.CENTER));
        exportExcel.addActionListener(e -> Dialogs.showMessage(this, "Export", "Export Excel is not implemented in this demo.", SwingConstants.CENTER));
        exports.add(new JLabel("Applications: " + stats.getTotalApplications() + " | Approval Rate: " + stats.getApprovalRate() + "%"));
        exports.add(exportPdf);
        exports.add(exportExcel);

        content().add(root, BorderLayout.NORTH);
        content().add(exports, BorderLayout.SOUTH);
    }

    private void runAnalysis() {
        if (analysisComplete) {
            Dialogs.showMessage(this, "AI Analysis", "Analysis already completed. Export options are enabled.", SwingConstants.CENTER);
            return;
        }
        run.setEnabled(false);
        statusLabel.setText("Analyzing TA data, workload patterns, and application trends...");
        progress.setVisible(true);

        Timer t = new Timer(2000, e -> {
            progress.setVisible(false);
            analysisComplete = true;
            statusLabel.setText("Analysis completed successfully");
            exportPdf.setEnabled(true);
            exportExcel.setEnabled(true);
            run.setEnabled(true);
            run.setText("Run Again");
        });
        t.setRepeats(false);
        t.start();
    }

    private static Card scoreCard(String title, String value, String note) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 8));
        JLabel t = new JLabel(title);
        t.setFont(UI.fontPlain(12));
        t.setForeground(UI.palette().textMuted());
        JLabel v = new JLabel(value);
        v.setFont(UI.fontMedium(32));
        v.setForeground(UI.palette().text());
        JLabel n = new JLabel(note);
        n.setFont(UI.fontPlain(12));
        n.setForeground(UI.palette().textMuted());
        c.add(t, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        c.add(n, BorderLayout.SOUTH);
        return c;
    }
}

