package ui.screens;

import data.ProfileData;
import ui.AppFrame;
import ui.PortalChrome;
import ui.TaTopNavigationPanel;
import ui.Theme;
import ui.Ui;
import ui.components.PrimaryButton;
import ui.components.SecondaryButton;

import ui.PortalUi;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ManageCvScreen extends JPanel {
    private final AppFrame app;
    private final JLabel statusText = Ui.body("No CV uploaded");
    private final PrimaryButton replaceBtn = new PrimaryButton("Replace CV");

    private final TaTopNavigationPanel topNav;

    public ManageCvScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.BG);
        topNav = TaTopNavigationPanel.forAppFrame(app, TaTopNavigationPanel.Active.PROFILE);
        add(topNav, BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    public void refresh() {
        topNav.refresh(TaTopNavigationPanel.Active.PROFILE);
        ProfileData p = app.profile();
        boolean uploaded = p.cv != null && p.cv.fileName != null && !p.cv.fileName.isBlank();
        if (uploaded) {
            statusText.setText(p.cv.fileName + "   Uploaded: " + p.cv.lastUpdated + "   Size: " + p.cv.sizeLabel);
            replaceBtn.setEnabled(true);
        } else {
            statusText.setText("No CV uploaded");
            replaceBtn.setEnabled(false);
        }
    }

    private JPanel buildBody() {
        JPanel page = PortalChrome.pageSurface();
        page.setLayout(new BorderLayout());
        page.setBorder(new EmptyBorder(14, 36, 20, 36));

        page.add(PortalChrome.header(
                PortalUi.fileTextIcon(PortalUi.PRIMARY_PURPLE, 22),
                "CV Upload",
                "Upload or manage your curriculum vitae",
                "←  Back to Profile", () -> app.showRoute(AppFrame.ROUTE_PROFILE)),
                BorderLayout.NORTH);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(new EmptyBorder(8, 100, 18, 100));

        PortalUi.RoundedSurface card = PortalChrome.card(new Insets(18, 22, 18, 22));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel statusLabel = sectionLabel("Current CV Status");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(statusLabel);
        body.add(Box.createVerticalStrut(8));

        PortalUi.RoundedSurface status = new PortalUi.RoundedSurface(
                14, PortalUi.LAVENDER, PortalUi.LIGHT_PURPLE_BORDER, 1f, false, new BorderLayout());
        status.setBorder(new EmptyBorder(10, 16, 10, 12));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        statusText.setForeground(PortalUi.DARK_TEXT);
        status.add(statusText, BorderLayout.WEST);
        SecondaryButton remove = new SecondaryButton("Remove");
        remove.setPreferredSize(new Dimension(96, 34));
        remove.addActionListener(e -> {
            ProfileData p = app.profile();
            p.cv.fileName = "";
            p.cv.status = "";
            p.cv.lastUpdated = "";
            p.cv.sizeLabel = "";
            app.updateProfile(p);
            refresh();
        });
        JPanel removeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        removeWrap.setOpaque(false);
        removeWrap.add(remove);
        status.add(removeWrap, BorderLayout.EAST);
        body.add(status);

        body.add(Box.createVerticalStrut(14));
        JPanel replaceLabel = sectionLabel("Replace CV");
        replaceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(replaceLabel);
        body.add(Box.createVerticalStrut(8));

        PortalUi.RoundedSurface drop = new PortalUi.RoundedSurface(
                16, new Color(0xFA, 0xFB, 0xFF), PortalUi.LIGHT_PURPLE_BORDER, 1f, false, new BorderLayout());
        drop.setBorder(new EmptyBorder(14, 22, 14, 22));
        drop.setAlignmentX(Component.LEFT_ALIGNMENT);
        drop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        drop.setPreferredSize(new Dimension(10, 170));

        JPanel dropInner = new JPanel();
        dropInner.setOpaque(false);
        dropInner.setLayout(new BoxLayout(dropInner, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel(PortalUi.fileTextIcon(PortalUi.PRIMARY_PURPLE, 30),
                SwingConstants.CENTER);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel drag = new JLabel("Drag and drop your CV here", SwingConstants.CENTER);
        drag.setFont(Theme.BODY_BOLD);
        drag.setForeground(PortalUi.DARK_TEXT);
        drag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel or = new JLabel("or", SwingConstants.CENTER);
        or.setFont(Theme.BODY);
        or.setForeground(Theme.MUTED);
        or.setAlignmentX(Component.CENTER_ALIGNMENT);

        PrimaryButton browse = new PrimaryButton("Browse File");
        browse.setAlignmentX(Component.CENTER_ALIGNMENT);
        browse.setPreferredSize(new Dimension(150, 38));
        browse.addActionListener(e -> chooseFile());

        dropInner.add(Box.createVerticalGlue());
        dropInner.add(iconLabel);
        dropInner.add(Box.createVerticalStrut(6));
        dropInner.add(drag);
        dropInner.add(Box.createVerticalStrut(4));
        dropInner.add(or);
        dropInner.add(Box.createVerticalStrut(6));
        dropInner.add(browse);
        dropInner.add(Box.createVerticalGlue());

        drop.add(dropInner, BorderLayout.CENTER);
        body.add(drop);

        body.add(Box.createVerticalStrut(12));
        JPanel reqLabel = sectionLabel("File Requirements");
        reqLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(reqLabel);
        body.add(Box.createVerticalStrut(4));
        JTextArea req = new JTextArea(
                "- Accepted file types: PDF, DOC, DOCX\n" +
                        "- Maximum file size: 5 MB\n" +
                        "- File name should not contain special characters\n" +
                        "- Recommended: Use a clear naming format (e.g., FirstName_LastName_CV.pdf)"
        );
        req.setEditable(false);
        req.setOpaque(false);
        req.setFont(Theme.BODY);
        req.setForeground(Theme.MUTED);
        req.setBorder(new EmptyBorder(2, 6, 0, 4));
        req.setAlignmentX(Component.LEFT_ALIGNMENT);
        req.setMaximumSize(new Dimension(Integer.MAX_VALUE, req.getPreferredSize().height));
        body.add(req);

        JPanel bodyHolder = new JPanel(new BorderLayout());
        bodyHolder.setOpaque(false);
        bodyHolder.add(body, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(bodyHolder,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 0, 0, 0));
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
        replaceBtn.addActionListener(e -> chooseFile());
        bottom.add(cancel);
        bottom.add(replaceBtn);
        card.add(bottom, BorderLayout.SOUTH);

        contentWrap.add(card, BorderLayout.CENTER);
        page.add(contentWrap, BorderLayout.CENTER);
        return page;
    }

    private JPanel sectionLabel(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel accent = new JPanel();
        accent.setOpaque(true);
        accent.setBackground(PortalUi.PRIMARY_PURPLE);
        accent.setPreferredSize(new Dimension(4, 18));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(PortalUi.DARK_TEXT);

        row.add(accent);
        row.add(label);
        return row;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CV (PDF/DOC/DOCX)");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CV Files (PDF, DOC, DOCX)", "pdf", "doc", "docx"));
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (f == null) return;

        String fileName = f.getName().toLowerCase();
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".doc") && !fileName.endsWith(".docx")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Only PDF, DOC, and DOCX files are accepted.", "Invalid File Type",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        long maxSize = 5 * 1024 * 1024;
        if (f.length() > maxSize) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "File size exceeds 5MB limit. Please choose a smaller file.", "File Too Large",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loginId = app.currentLoginId();
        if (loginId == null || loginId.isBlank()) {
            loginId = app.profile().studentId;
            if (loginId == null || loginId.isBlank()) {
                loginId = app.authenticatedUsername();
                if (loginId == null || loginId.isBlank()) {
                    loginId = "default";
                }
            }
        }

        String destDir = "data" + File.separator + "uploads" + File.separator + "profile_cv" + File.separator + loginId;
        File destDirFile = new File(destDir);
        if (!destDirFile.exists()) {
            destDirFile.mkdirs();
        }

        String storedFileName = f.getName();
        File destFile = new File(destDirFile, storedFileName);

        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            Files.copy(f.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[ManageCvScreen] CV copied to: " + destFile.getAbsolutePath());
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to copy CV file: " + ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        ProfileData p = app.profile();
        p.cv.fileName = f.getName();
        p.cv.status = "Uploaded";
        p.cv.lastUpdated = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        p.cv.sizeLabel = Math.max(1, f.length() / 1024) + " KB";
        app.updateProfile(p);
        refresh();
    }
}
