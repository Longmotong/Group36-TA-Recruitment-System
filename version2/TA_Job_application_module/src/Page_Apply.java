

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * 申请页面：单卡片布局（顶部浅蓝职位信息 + 提示 + 表单），由外层滚动条滚动整页
 */
public class Page_Apply {

    private static final int FORM_MAX_WIDTH = 880;
    private static final Color POSITION_HEADER_BG = new Color(239, 246, 255);
    private static final Color POSITION_HEADER_BORDER = new Color(191, 219, 254);
    /** 预填提示条：避免特殊符号缺字，与表单同宽 */
    private static final Color HINT_BANNER_BG = new Color(254, 252, 232);
    private static final Color HINT_BANNER_BORDER = new Color(253, 230, 138);

    private static String getCvUploadBase() {
        String userDir = System.getProperty("user.dir");
        return userDir + File.separator + ".." + File.separator + ".."
            + File.separator + "data" + File.separator + "uploads"
            + File.separator + "profile_cv";
    }

    /**
     * Modal message with English "OK" and centered on the application window
     * (default JOptionPane follows system locale for the button and can align oddly).
     */
    private static void showCenteredMessage(Component anchor, String message, String title, int messageType) {
        JOptionPane pane = new JOptionPane(message, messageType);
        pane.setOptions(new Object[]{"OK"});
        Window owner = SwingUtilities.getWindowAncestor(anchor);
        if (owner == null) {
            owner = JOptionPane.getRootFrame();
        }
        JDialog dialog = pane.createDialog(owner, title);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        dialog.dispose();
    }

    public interface ApplyCallback {
        void onBackToJobDetail(Job job);
        void onSubmitSuccess();
    }

    private JPanel panel;
    private ApplyCallback callback;
    private Job currentJob;
    private TAUser currentUser;
    private DataService dataService;

    public Page_Apply(TAUser currentUser, DataService dataService, ApplyCallback callback) {
        this.currentUser = currentUser;
        this.dataService = dataService;
        this.callback = callback;
        initPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void showJob(Job job) {
        this.currentJob = job;
        buildContent(job);
    }

    private void initPanel() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(16, 48, 40, 48));
    }

    private void buildContent(Job job) {
        panel.removeAll();

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        column.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.setMaximumSize(new Dimension(FORM_MAX_WIDTH, Integer.MAX_VALUE));

        JButton backBtn = new JButton("\u2190 Back to Job Detail");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(UI_Constants.TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 8, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> callback.onBackToJobDetail(job));
        column.add(backBtn);

        JLabel pageTitle = new JLabel("Apply for Job");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pageTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        column.add(pageTitle);

        JPanel mainCard = new JPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBackground(UI_Constants.CARD_BG);
        mainCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 24, 0)
        ));
        mainCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        mainCard.add(buildPositionHeader(job));
        mainCard.add(Box.createVerticalStrut(20));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 24, 0, 24));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel hintCard = new JPanel(new BorderLayout());
        hintCard.setOpaque(true);
        hintCard.setBackground(HINT_BANNER_BG);
        hintCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HINT_BANNER_BORDER, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));
        JLabel hintText = new JLabel(
            "<html><b>Note:</b> Your profile data, skills, and CV have been pre-filled. You may edit anything before submitting.</html>");
        hintText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hintText.setForeground(UI_Constants.TEXT_SECONDARY);
        hintCard.add(hintText, BorderLayout.CENTER);

        JPanel hintRow = new JPanel(new BorderLayout());
        hintRow.setOpaque(false);
        hintRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        hintRow.add(hintCard, BorderLayout.CENTER);
        body.add(hintRow);

        body.add(Box.createVerticalStrut(22));

        JLabel sectionTitle = new JLabel("Application Information");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sectionTitle);
        body.add(Box.createVerticalStrut(16));

        JTextField fullNameField = createEditableField(currentUser.getProfile().getFullName());
        JTextField studentIdField = createEditableField(currentUser.getProfile().getStudentId());
        JTextField emailField = createEditableField(currentUser.getAccount().getEmail());
        JTextField phoneField = createEditableField(currentUser.getProfile().getPhoneNumber());
        JTextField programField = createEditableField(currentUser.getProfile().getProgramMajor());
        JTextField gpaField = createEditableField(String.valueOf(currentUser.getAcademic().getGpa()));

        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        grid.add(createFieldPanel("Full Name *", fullNameField));
        grid.add(createFieldPanel("Student ID *", studentIdField));
        grid.add(createFieldPanel("Email *", emailField));
        grid.add(createFieldPanel("Phone Number *", phoneField));
        grid.add(createFieldPanel("Program / Major *", programField));
        grid.add(createFieldPanel("GPA (Optional)", gpaField));
        body.add(grid);

        body.add(Box.createVerticalStrut(18));

        JTextArea skillsArea = UI_Helper.createTextArea(4);
        body.add(createLabeledArea("Relevant Skills *",
            "e.g., Java, Python, Data Structures", skillsArea));
        skillsArea.setText(formatUserSkills(currentUser));

        body.add(Box.createVerticalStrut(14));

        JTextArea experienceArea = UI_Helper.createTextArea(5);
        body.add(createLabeledArea("Relevant Experience *",
            "Describe TA/grading experience, projects, etc.", experienceArea));

        body.add(Box.createVerticalStrut(14));

        JTextArea availabilityArea = UI_Helper.createTextArea(3);
        body.add(createLabeledArea("Availability *",
            "e.g., Monday/Wednesday 10am-12pm", availabilityArea));

        body.add(Box.createVerticalStrut(14));

        JTextArea motivationArea = UI_Helper.createTextArea(6);
        body.add(createLabeledArea("Motivation / Cover Letter *",
            "Why this position and why you are a good fit", motivationArea));

        body.add(Box.createVerticalStrut(20));

        JLabel resumeTitle = new JLabel("Resume / CV *");
        resumeTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resumeTitle.setForeground(UI_Constants.TEXT_SECONDARY);
        resumeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(resumeTitle);
        body.add(Box.createVerticalStrut(8));

        JPanel resumeBox = createUploadBox(
            currentUser.getProfile().getFullName() + "_CV.pdf attached from profile",
            "Click to choose a different file"
        );
        resumeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        body.add(resumeBox);

        body.add(Box.createVerticalStrut(16));

        JLabel supportTitle = new JLabel("Supporting Documents (Optional)");
        supportTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        supportTitle.setForeground(UI_Constants.TEXT_SECONDARY);
        supportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(supportTitle);
        body.add(Box.createVerticalStrut(8));

        JPanel supportBox = createUploadBox(
            "Transcripts, certificates, or other documents",
            "Click to upload"
        );
        supportBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        supportBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        body.add(supportBox);

        body.add(Box.createVerticalStrut(28));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submitBtn = new JButton("Submit Application");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(UI_Constants.DARK_BUTTON);
        submitBtn.setOpaque(true);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setBorder(new EmptyBorder(12, 28, 12, 28));
        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                submitBtn.setBackground(UI_Constants.DARK_BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                submitBtn.setBackground(UI_Constants.DARK_BUTTON);
            }
        });

        JButton cancelBtn = UI_Helper.createSecondaryButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.addActionListener(e -> callback.onBackToJobDetail(job));

        btnRow.add(submitBtn);
        btnRow.add(cancelBtn);
        body.add(btnRow);

        mainCard.add(body);
        column.add(mainCard);

        panel.add(column);

        final String[] selectedCvPath = {null};
        final List<String> selectedSupportPaths = new ArrayList<>();

        JButton resumePickBtn = (JButton) resumeBox.getClientProperty("pickButton");
        JLabel resumeHintLbl = (JLabel) resumeBox.getClientProperty("hintLabel");
        resumePickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(panel);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedCvPath[0] = f.getAbsolutePath();
                resumeHintLbl.setText(f.getName() + " (selected)");
                resumeHintLbl.setForeground(UI_Constants.SUCCESS_COLOR);
            }
        });

        JButton supportPickBtn = (JButton) supportBox.getClientProperty("pickButton");
        JLabel supportHintLbl = (JLabel) supportBox.getClientProperty("hintLabel");
        supportPickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            int res = chooser.showOpenDialog(panel);
            if (res == JFileChooser.APPROVE_OPTION) {
                selectedSupportPaths.clear();
                File[] files = chooser.getSelectedFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        selectedSupportPaths.add(f.getAbsolutePath());
                    }
                    supportHintLbl.setText(files.length + " file(s) selected");
                }
            }
        });

        submitBtn.addActionListener(e -> {
            // 防止重复申请：进入页面后用户又手动提交的情况
            if (dataService.hasAppliedToJob(job.getJobId())) {
                showCenteredMessage(panel,
                    "You have already submitted an application for this position.\n\nPlease check your application status in 'My Applications'.",
                    "Already Applied", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (fullNameField.getText().trim().isEmpty()
                || studentIdField.getText().trim().isEmpty()
                || emailField.getText().trim().isEmpty()
                || phoneField.getText().trim().isEmpty()
                || programField.getText().trim().isEmpty()
                || skillsArea.getText().trim().isEmpty()
                || experienceArea.getText().trim().isEmpty()
                || availabilityArea.getText().trim().isEmpty()
                || motivationArea.getText().trim().isEmpty()) {
                showCenteredMessage(panel, "Please fill in all required fields (*) before submitting.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Application app = new Application();

            Application.JobSnapshot jobSnap = new Application.JobSnapshot();
            jobSnap.setTitle(job.getTitle());
            jobSnap.setCourseCode(job.getCourseCode());
            if (job.getCourse() != null) {
                jobSnap.setCourseName(job.getCourse().getCourseName());
            }
            jobSnap.setDepartment(job.getDepartment());
            jobSnap.setInstructorName(job.getInstructorName());
            jobSnap.setInstructorEmail(job.getInstructorEmail());
            jobSnap.setDeadline(job.getDeadlineDisplay());
            jobSnap.setEmploymentType(job.getEmploymentType());
            if (job.getEmployment() != null) {
                jobSnap.setWeeklyHours(job.getEmployment().getWeeklyHours());
            } else {
                jobSnap.setWeeklyHours(0);
            }
            jobSnap.setLocationMode(job.getLocationMode());
            if (job.getEmployment() != null && job.getEmployment().getLocationDetail() != null) {
                jobSnap.setLocationDetail(job.getEmployment().getLocationDetail());
            }
            app.setJobSnapshot(jobSnap);

            Application.ApplicantSnapshot appSnap = new Application.ApplicantSnapshot();
            appSnap.setFullName(fullNameField.getText().trim());
            appSnap.setStudentId(studentIdField.getText().trim());
            appSnap.setEmail(emailField.getText().trim());
            appSnap.setPhoneNumber(phoneField.getText().trim());
            appSnap.setProgramMajor(programField.getText().trim());
            appSnap.setYear(currentUser.getProfile().getYear());
            try {
                String gpaTxt = gpaField.getText().trim();
                if (!gpaTxt.isEmpty()) {
                    appSnap.setGpa(Double.parseDouble(gpaTxt));
                }
            } catch (NumberFormatException ex) {
                showCenteredMessage(panel, "GPA must be a number (e.g., 3.8).", "Invalid GPA", JOptionPane.WARNING_MESSAGE);
                return;
            }
            app.setApplicantSnapshot(appSnap);

            Application.ApplicationForm appForm = new Application.ApplicationForm();
            String[] skills = skillsArea.getText().split(",");
            appForm.setRelevantSkills(Arrays.stream(skills).map(String::trim).filter(s -> !s.isEmpty()).toList());
            appForm.setRelevantExperience(experienceArea.getText().trim());
            appForm.setAvailability(availabilityArea.getText().trim());
            appForm.setMotivationCoverLetter(motivationArea.getText().trim());
            app.setApplicationForm(appForm);

            Application.Attachments at = new Application.Attachments();
            if (selectedCvPath[0] != null && !selectedCvPath[0].isEmpty()) {
                File sourceFile = new File(selectedCvPath[0]);
                String studentId = studentIdField.getText().trim();
                File studentDir = new File(getCvUploadBase() + File.separator + studentId);
                if (!studentDir.exists()) {
                    studentDir.mkdirs();
                }
                File destFile = new File(studentDir, sourceFile.getName());
                try {
                    java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (java.io.IOException ex) {
                    System.err.println("Error copying CV file: " + ex.getMessage());
                }
                Application.CVInfo cv = new Application.CVInfo();
                cv.setFileName(sourceFile.getName());
                cv.setFilePath(destFile.getAbsolutePath());
                String lower = sourceFile.getName().toLowerCase();
                cv.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
                at.setCv(cv);
            }
            if (!selectedSupportPaths.isEmpty()) {
                List<Application.Document> docs = new ArrayList<>();
                for (String p : selectedSupportPaths) {
                    File f = new File(p);
                    Application.Document d = new Application.Document();
                    d.setFileName(f.getName());
                    d.setFilePath(f.getAbsolutePath());
                    String lower = f.getName().toLowerCase();
                    d.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
                    docs.add(d);
                }
                at.setSupportingDocuments(docs);
            }
            app.setAttachments(at);
            app.setJobId(job.getJobId());

            dataService.addApplication(app);

            showCenteredMessage(panel,
                "Application submitted successfully!\n\nYou can track your application status in 'My Applications'.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

            callback.onSubmitSuccess();
        });

        panel.revalidate();
        panel.repaint();
    }

    private JPanel buildPositionHeader(Job job) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(true);
        wrap.setBackground(POSITION_HEADER_BG);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, POSITION_HEADER_BORDER),
            new EmptyBorder(18, 24, 18, 24)
        ));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel tag = new JLabel("Position Details");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tag.setForeground(UI_Constants.INFO_COLOR);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(tag);
        wrap.add(Box.createVerticalStrut(8));

        JLabel jt = new JLabel(job.getTitle());
        jt.setFont(new Font("Segoe UI", Font.BOLD, 20));
        jt.setForeground(UI_Constants.TEXT_PRIMARY);
        jt.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(jt);
        wrap.add(Box.createVerticalStrut(10));

        String courseLine = job.getCourseCode();
        if (job.getCourse() != null && job.getCourse().getCourseName() != null) {
            courseLine = courseLine + " \u2014 " + job.getCourse().getCourseName();
        }
        addMetaLine(wrap, "Course", courseLine);
        addMetaLine(wrap, "Instructor", job.getInstructorName());
        addMetaLine(wrap, "Application Deadline", formatDeadlinePretty(job.getDeadlineDisplay()));

        return wrap;
    }

    private void addMetaLine(JPanel wrap, String label, String value) {
        if (value == null) {
            value = "";
        }
        JLabel line = new JLabel("<html><b>" + label + ":</b> " + escapeHtml(value) + "</html>");
        line.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        line.setForeground(UI_Constants.TEXT_SECONDARY);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setBorder(new EmptyBorder(0, 0, 4, 0));
        wrap.add(line);
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String formatDeadlinePretty(String raw) {
        if (raw == null || raw.length() < 10) {
            return raw != null ? raw : "";
        }
        String ymd = raw.substring(0, 10);
        String[] p = ymd.split("-");
        if (p.length != 3) {
            return raw;
        }
        String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        try {
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            if (m >= 1 && m <= 12) {
                return months[m - 1] + " " + d + ", " + p[0];
            }
        } catch (NumberFormatException ignored) { }
        return raw;
    }

    private String formatUserSkills(TAUser user) {
        if (user == null || user.getSkills() == null) {
            return "";
        }
        List<String> all = new ArrayList<>();
        TAUser.Skills s = user.getSkills();
        addSkillNames(all, s.getProgramming());
        addSkillNames(all, s.getTeaching());
        addSkillNames(all, s.getCommunication());
        addSkillNames(all, s.getOther());
        return String.join(", ", all);
    }

    private void addSkillNames(List<String> out, List<TAUser.Skill> skills) {
        if (skills == null) {
            return;
        }
        for (TAUser.Skill sk : skills) {
            if (sk != null && sk.getName() != null && !sk.getName().trim().isEmpty()) {
                out.add(sk.getName().trim());
            }
        }
    }

    private JTextField createEditableField(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UI_Constants.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel createLabeledArea(String label, String hint, JTextArea area) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UI_Constants.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(lbl);

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLbl.setForeground(new Color(156, 163, 175));
        hintLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintLbl.setBorder(new EmptyBorder(2, 0, 6, 0));
        wrap.add(hintLbl);

        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)
        ));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(area);
        return wrap;
    }

    private JPanel createUploadBox(String topLine, String bottomLine) {
        JPanel box = new JPanel(new BorderLayout(12, 0));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(UI_Constants.BORDER_COLOR, 8, 4, 2, false),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("<html>" + topLine + "</html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(UI_Constants.TEXT_PRIMARY);
        center.add(hint);

        JLabel action = new JLabel(bottomLine);
        action.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        action.setForeground(UI_Constants.TEXT_SECONDARY);
        action.setBorder(new EmptyBorder(6, 0, 0, 0));
        center.add(action);

        box.add(center, BorderLayout.CENTER);

        JButton pick = UI_Helper.createSecondaryButton("Choose file");
        pick.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.add(pick, BorderLayout.EAST);

        box.putClientProperty("pickButton", pick);
        box.putClientProperty("hintLabel", hint);
        return box;
    }
}
