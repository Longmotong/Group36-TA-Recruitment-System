package profile_module.ui.screens;

import profile_module.data.ProfileData;
import profile_module.ui.AppFrame;
import profile_module.ui.TaTopNavigationPanel;
import profile_module.ui.Theme;
import profile_module.ui.Ui;
import profile_module.ui.components.PrimaryButton;
import profile_module.ui.components.SecondaryButton;

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
        head.add(navLink("← Back to Profile", () -> app.showRoute(AppFrame.ROUTE_PROFILE)));
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
            ProfileData p = app.profile();
            p.cv.fileName = "";
            p.cv.status = "";
            p.cv.lastUpdated = "";
            p.cv.sizeLabel = "";
            app.updateProfile(p);
            // 同步更新 TAUser.cv
            updateTAUserCv(null, null, null, false);
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
                        "- Recommended: Use a clear naming format (e.g., FirstName_LastName_CV.pdf)"
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
        cancel.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
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
        // 设置文件过滤器，只允许选择 PDF, DOC, DOCX 文件
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CV Files (PDF, DOC, DOCX)", "pdf", "doc", "docx"));
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (f == null) return;

        // 检查文件扩展名
        String fileName = f.getName().toLowerCase();
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".doc") && !fileName.endsWith(".docx")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Only PDF, DOC, and DOCX files are accepted.", "Invalid File Type",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 检查文件大小（5MB = 5 * 1024 * 1024 bytes）
        long maxSize = 5 * 1024 * 1024;
        if (f.length() > maxSize) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "File size exceeds 5MB limit. Please choose a smaller file.", "File Too Large",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取登录ID（优先使用loginId，其次使用studentId，最后使用username）
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

        // 使用 loginId 创建目标文件夹并复制文件
        String destDir = "data" + File.separator + "uploads" + File.separator + "profile_cv" + File.separator + loginId;
        File destDirFile = new File(destDir);
        if (!destDirFile.exists()) {
            destDirFile.mkdirs();
        }

        // 生成目标文件名：保持原文件名
        String storedFileName = f.getName();
        File destFile = new File(destDirFile, storedFileName);

        try {
            // 如果目标文件已存在，先删除
            if (destFile.exists()) {
                destFile.delete();
            }
            // 复制文件
            Files.copy(f.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[ManageCvScreen] CV copied to: " + destFile.getAbsolutePath());
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to copy CV file: " + ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        // 更新 ProfileData.cv
        ProfileData p = app.profile();
        p.cv.fileName = f.getName();
        p.cv.status = "Uploaded";
        p.cv.lastUpdated = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        p.cv.sizeLabel = Math.max(1, f.length() / 1024) + " KB";
        app.updateProfile(p);

        // 同步更新 TAUser.cv 并保存到 JSON 文件
        String relativePath = destDir + File.separator + storedFileName;
        updateTAUserCv(f.getName(), relativePath, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), true);

        refresh();
    }

    /**
     * 更新 TAUser.cv 字段，并将更改保存到 data/users/{role}/*.json 文件。
     */
    private void updateTAUserCv(String originalFileName, String filePath, String uploadedAt, boolean uploaded) {
        try {
            TA_Job_Application_Module.DataService ds = TA_Job_Application_Module.DataService.getInstance();
            TA_Job_Application_Module.TAUser user = ds.getCurrentUser();
            if (user != null) {
                TA_Job_Application_Module.TAUser.CV cv = user.getCv();
                if (cv == null) {
                    cv = new TA_Job_Application_Module.TAUser.CV();
                    user.setCv(cv);
                }
                if (originalFileName != null) {
                    cv.setOriginalFileName(originalFileName);
                }
                if (filePath != null) {
                    cv.setFilePath(filePath);
                }
                if (uploadedAt != null) {
                    cv.setUploadedAt(uploadedAt);
                }
                cv.setUploaded(uploaded);
                // 保存到 JSON 文件
                ds.saveCurrentUserToFile();
                System.out.println("[ManageCvScreen] TAUser.cv updated and saved.");
            }
        } catch (Exception e) {
            System.err.println("[ManageCvScreen] Failed to update TAUser.cv: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

