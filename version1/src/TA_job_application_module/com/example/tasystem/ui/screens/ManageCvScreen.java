package com.example.tasystem.ui.screens;

import com.example.tasystem.integration.TaPortalHost;
import com.example.tasystem.ui.Theme;
import com.example.tasystem.ui.Ui;
import com.example.tasystem.ui.components.PrimaryButton;
import com.example.tasystem.ui.components.SecondaryButton;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

public final class ManageCvScreen extends JPanel {
    private final TaPortalHost host;
    private final JLabel statusText = Ui.body("No CV uploaded");
    private final PrimaryButton replaceBtn = new PrimaryButton("Replace CV");

    public ManageCvScreen(TaPortalHost host) {
        this.host = host;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(buildNavBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    public void refresh() {
        var p = host.profile();
        boolean uploaded = p.cv != null && p.cv.fileName != null && !p.cv.fileName.isBlank();
        if (uploaded) {
            statusText.setText(p.cv.fileName + "   Uploaded: " + p.cv.lastUpdated + "   Size: " + p.cv.sizeLabel);
            replaceBtn.setEnabled(true);
        } else {
            statusText.setText("No CV uploaded");
            replaceBtn.setEnabled(false);
        }
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(Ui.empty(10, 18, 10, 18));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        left.setOpaque(false);
        left.add(new JLabel("TA System"));
        left.add(navLink("Home", host::showDashboard));
        left.add(navLink("Profile Module", () -> host.showRoute(TaPortalHost.ROUTE_PROFILE)));
        left.add(navLink("Job Application Module", host::showJobsModule));
        nav.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);
        right.add(navLink("Logout", host::logout));
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    private JButton navLink(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(Theme.BODY);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JPanel buildBody() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBorder(Ui.empty(16, 18, 12, 18));
        head.add(navLink("← Back to Profile", () -> host.showRoute(TaPortalHost.ROUTE_PROFILE)));
        head.add(Box.createVerticalStrut(8));
        head.add(Ui.h1("CV Upload"));
        head.add(Box.createVerticalStrut(6));
        head.add(Ui.muted("Upload or manage your curriculum vitae"));
        wrap.add(head, BorderLayout.NORTH);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(Ui.empty(0, 220, 18, 220));

        Ui.RoundedPanel card = new Ui.RoundedPanel(16, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(Ui.empty(16, 16, 16, 16));
        card.add(Ui.h2("Current CV Status"));
        card.add(Box.createVerticalStrut(10));

        Ui.RoundedPanel status = new Ui.RoundedPanel(12, Theme.GREEN_BG, Theme.BORDER, 1);
        status.setLayout(new BorderLayout());
        status.setBorder(Ui.empty(12, 12, 12, 12));
        status.add(statusText, BorderLayout.WEST);
        SecondaryButton remove = new SecondaryButton("Remove");
        remove.addActionListener(e -> {
            host.removeCvUpload();
            refresh();
        });
        JPanel removeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        removeWrap.setOpaque(false);
        removeWrap.add(remove);
        status.add(removeWrap, BorderLayout.EAST);
        card.add(status);

        card.add(Box.createVerticalStrut(14));
        card.add(Ui.h2("Replace CV"));
        card.add(Box.createVerticalStrut(10));
        Ui.RoundedPanel drop = new Ui.RoundedPanel(14, new java.awt.Color(0xFA, 0xFB, 0xFC), Theme.BORDER, 1);
        drop.setLayout(new BoxLayout(drop, BoxLayout.Y_AXIS));
        drop.setPreferredSize(new java.awt.Dimension(10, 220));
        drop.setBorder(Ui.empty(18, 18, 18, 18));
        JLabel drag = Ui.body("Drag and drop your CV here");
        drag.setAlignmentX(Component.CENTER_ALIGNMENT);
        PrimaryButton browse = new PrimaryButton("Browse File");
        browse.setAlignmentX(Component.CENTER_ALIGNMENT);
        browse.addActionListener(e -> chooseFile());
        drop.add(Box.createVerticalGlue());
        drop.add(drag);
        drop.add(Box.createVerticalStrut(10));
        drop.add(browse);
        drop.add(Box.createVerticalGlue());
        card.add(drop);

        card.add(Box.createVerticalStrut(12));
        card.add(Ui.h3("File Requirements"));
        JTextArea req = new JTextArea(
                "- Accepted file types: PDF, DOC, DOCX\n" +
                        "- Maximum file size: 5 MB\n" +
                        "- File name should not contain special characters\n" +
                        "- Recommended: Use a clear naming format (e.g., FirstName_LastName_CV.pdf)\n" +
                        "- Files are stored under data/uploads/profile_cv/{StudentId}/ (same as job application module)"
        );
        req.setEditable(false);
        req.setOpaque(false);
        req.setFont(Theme.BODY);
        req.setForeground(Theme.MUTED);
        card.add(req);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        replaceBtn.addActionListener(e -> chooseFile());
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> host.showRoute(TaPortalHost.ROUTE_PROFILE));
        bottom.add(replaceBtn);
        bottom.add(cancel);
        card.add(bottom);

        contentWrap.add(card, BorderLayout.CENTER);
        wrap.add(contentWrap, BorderLayout.CENTER);
        return wrap;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CV (PDF/DOC/DOCX)");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (f == null) return;
        try {
            host.uploadCvFromFile(f);
            refresh();
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Could not save CV: " + ex.getMessage(),
                    "Upload failed",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
