package com.taapp.ui.pages;

import com.taapp.data.DataStore;
import com.taapp.model.Statistics;
import com.taapp.ui.AppLayout;
import com.taapp.ui.Dialogs;
import com.taapp.ui.UI;
import com.taapp.ui.components.Card;
import com.taapp.ui.components.Page;
import com.taapp.ui.components.RoundedActionButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * AI Analysis — spacing and CTAs aligned with MO console (gutters, black primary, outline secondary).
 */
public class AIAnalysisPanel extends Page {
    private final JLabel statusLabel = new JLabel("Ready to Run AI Analysis");
    private final JProgressBar progress = new JProgressBar();
    private final RoundedActionButton run = new RoundedActionButton("Run Analysis", RoundedActionButton.Scheme.PRIMARY_BLACK);
    private final JButton exportPdf = new JButton("Export PDF");
    private final JButton exportExcel = new JButton("Export Excel");

    private boolean analysisComplete = false;

    public AIAnalysisPanel() {
        super();

        Statistics stats = DataStore.defaultStore().getStatistics();

        run.setFont(UI.moFontBold(13));
        run.setPreferredSize(new Dimension(200, 42));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(AppLayout.PAGE_INSET_TOP, 0, AppLayout.PAGE_INSET_BOTTOM, 0));

        JPanel headerCard = new JPanel(new BorderLayout(0, 6));
        headerCard.setOpaque(true);
        headerCard.setBackground(Color.WHITE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JLabel h1 = new JLabel("AI Analysis & Insights");
        h1.setFont(UI.moFontBold(26));
        h1.setForeground(UI.palette().text());
        h1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("AI-powered analysis and recommendations for TA management optimization");
        sub.setFont(UI.moFontPlain(14));
        sub.setForeground(UI.palette().textSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(h1);
        leftCol.add(Box.createVerticalStrut(6));
        leftCol.add(sub);
        headerCard.add(leftCol, BorderLayout.CENTER);

        Card control = new Card();
        control.setLayout(new BorderLayout(0, 10));
        control.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(16, 18, 18, 18)
        ));

        JLabel ct = new JLabel("AI Analysis Engine");
        ct.setFont(UI.moFontBold(17));
        ct.setForeground(UI.palette().text());
        control.add(ct, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JButton upload = new JButton("Upload Data");
        UI.styleSecondaryButton(upload);
        upload.addActionListener(e -> Dialogs.showMessage(this, "Upload", "Upload is not implemented in this demo.", SwingConstants.CENTER));

        run.addActionListener(e -> runAnalysis());

        actions.add(upload);
        actions.add(run);
        control.add(actions, BorderLayout.SOUTH);

        JPanel status = new JPanel(new BorderLayout(0, 6));
        status.setOpaque(false);
        statusLabel.setFont(UI.moFontPlain(14));
        status.add(statusLabel, BorderLayout.NORTH);

        progress.setVisible(false);
        progress.setIndeterminate(true);
        status.add(progress, BorderLayout.SOUTH);
        control.add(status, BorderLayout.CENTER);

        JPanel mainCol = new JPanel();
        mainCol.setLayout(new BoxLayout(mainCol, BoxLayout.Y_AXIS));
        mainCol.setOpaque(false);
        mainCol.add(headerCard);
        mainCol.add(Box.createVerticalStrut(16));
        mainCol.add(control);

        root.add(mainCol, BorderLayout.NORTH);

        JPanel scores = new JPanel(new GridLayout(1, 3, 16, 0));
        scores.setOpaque(false);
        scores.add(scoreCard("System Health Score", "87/100", "Overall system efficiency is good"));
        scores.add(scoreCard("Workload Balance", "72/100", "Some TAs may be overloaded"));
        scores.add(scoreCard("Resource Utilization", "80%", "Efficient use of TA resources"));
        root.add(scores, BorderLayout.CENTER);

        Card exports = new Card();
        exports.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exports.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(12, 16, 12, 16)
        ));

        exportPdf.setEnabled(false);
        exportExcel.setEnabled(false);
        UI.styleSecondaryButton(exportPdf);
        UI.styleSecondaryButton(exportExcel);

        exportPdf.addActionListener(e -> Dialogs.showMessage(this, "Export", "Export PDF is not implemented in this demo.", SwingConstants.CENTER));
        exportExcel.addActionListener(e -> Dialogs.showMessage(this, "Export", "Export Excel is not implemented in this demo.", SwingConstants.CENTER));

        JLabel stat = new JLabel("Applications: " + stats.getTotalApplications() + " | Approval Rate: " + stats.getApprovalRate() + "%");
        stat.setForeground(UI.palette().textSecondary());
        stat.setFont(UI.moFontPlain(13));

        exports.add(stat);
        exports.add(exportPdf);
        exports.add(exportExcel);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(root, BorderLayout.CENTER);
        wrap.add(exports, BorderLayout.SOUTH);

        content().add(AppLayout.wrapCentered(wrap), BorderLayout.CENTER);
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

    private Card scoreCard(String title, String value, String note) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 6));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel t = new JLabel(title);
        t.setFont(UI.moFontBold(12));
        t.setForeground(UI.palette().textSecondary());

        JLabel v = new JLabel(value);
        v.setFont(UI.moFontBold(26));
        v.setForeground(UI.palette().text());

        JLabel n = new JLabel(note);
        n.setFont(UI.moFontPlain(12));
        n.setForeground(UI.palette().textSecondary());

        c.add(t, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        c.add(n, BorderLayout.SOUTH);
        return c;
    }
}
