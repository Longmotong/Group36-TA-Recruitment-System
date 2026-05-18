package TA_Job_Application_Module.pages.apply;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.model.TAUser;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;
import TA_Job_Application_Module.service.DataService;
import TA_Job_Application_Module.ui.UI_Constants;
import TA_Job_Application_Module.ui.UI_Helper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Apply form (classic centered column layout). Purple accents on titles and primary/secondary actions
 * align with the TA portal theme ({@link JobsPortalUi}).
 */
public class Page_Apply {

    private static final int FORM_MAX_WIDTH = 880;
    private static final Color POSITION_HEADER_BG = new Color(0xF8F5FF);
    private static final Color POSITION_HEADER_BORDER = new Color(0xDED4FF);
    private static final Color HINT_BANNER_BG = new Color(0xFFFBEB);
    private static final Color HINT_BANNER_BORDER = new Color(0xFDE68A);
    /** Main page / section titles — portal purple. */
    private static final Color TITLE_PURPLE = JobsPortalUi.PURPLE_800;
    private static final Color DARK_TEXT = new Color(0x111033);
    private static final Color MUTED_TEXT = new Color(0x667085);
    private static final Color SOFT_BG_TOP = new Color(0xFDFCFF);
    private static final Color SOFT_BG_BOTTOM = new Color(0xF8F6FF);
    private static final Color CARD_BORDER = new Color(0xDED4FF);
    private static final Color LAVENDER_TILE = new Color(0xF3EEFF);
    private static final Color INPUT_BORDER = new Color(0xDDE3EE);
    private static final Color GREEN_BG = new Color(0xECFDF5);
    private static final Color GREEN_BORDER = new Color(0xA7F3D0);
    private static final Color GREEN_TEXT = new Color(0x15803D);

    private static String getCvUploadBase() {
        String binDir = System.getProperty("user.dir");
        return binDir + File.separator + "data" + File.separator + "uploads"
                + File.separator + "profile_cv";
    }

    private static void showCenteredMessage(Component anchor, String message, String title, int messageType) {
        showCenteredMessage(anchor, message, title, messageType, "OK");
    }

    private static void showCenteredMessage(Component anchor, String message, String title, int messageType, String okLabel) {
        JOptionPane pane = new JOptionPane(message, messageType);
        pane.setOptions(new Object[]{okLabel});
        Window owner = SwingUtilities.getWindowAncestor(anchor);
        if (owner == null) {
            owner = JOptionPane.getRootFrame();
        }
        JDialog dialog = pane.createDialog(owner, title);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Prefer the enclosing application window so {@link JFileChooser} centers on the TA portal,
     * not on a detached screen corner.
     */
    private static Component fileChooserParent(Component anchor) {
        if (anchor == null) {
            return JOptionPane.getRootFrame();
        }
        Window w = SwingUtilities.getWindowAncestor(anchor);
        if (w != null) {
            return w;
        }
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (active != null) {
            return active;
        }
        Frame f = JOptionPane.getFrameForComponent(anchor);
        return f != null ? f : JOptionPane.getRootFrame();
    }

    public interface ApplyCallback {
        void onBackToJobDetail(Job job);
        void onSubmitSuccess();
        void onDraftSaved();
    }

    private JPanel panel;
    private ApplyCallback callback;
    private Job currentJob;
    private TAUser currentUser;
    private DataService dataService;
    private String currentDraftId;

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

    public void showJobForDraft(Job job, Application draft) {
        this.currentJob = job;
        this.currentDraftId = draft.getApplicationId();
        buildContentForDraft(job, draft);
    }

    private void initPanel() {
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, SOFT_BG_TOP, 0, getHeight(), SOFT_BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative dotted pattern, visually consistent with the portal jobs pages.
                g2.setColor(new Color(109, 77, 235, 18));
                int startX = Math.max(0, getWidth() - 260);
                for (int x = startX; x < getWidth() - 18; x += 10) {
                    for (int y = 0; y < 170; y += 10) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(JobsPortalUi.PAGE_BG);
        panel.setBorder(new EmptyBorder(18, 48, 40, 48));
    }

    private void buildContent(Job job) {
        panel.removeAll();

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        column.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.setMaximumSize(new Dimension(FORM_MAX_WIDTH, Integer.MAX_VALUE));

        column.add(buildPageHeader(
                "\u2190  Back to Job Detail",
                "Apply for Job",
                "Please fill out the application form below",
                () -> callback.onBackToJobDetail(job)));

        JPanel mainCard = new SoftCardPanel(22, Color.WHITE, CARD_BORDER, true);
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainCard.setBorder(new EmptyBorder(0, 0, 24, 0));
        mainCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        mainCard.add(buildPositionHeader(job));
        mainCard.add(Box.createVerticalStrut(20));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 24, 0, 24));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(createHintBanner());

        body.add(Box.createVerticalStrut(22));

        body.add(sectionTitle("Application Information", miniIcon(JobsPortalUi.PURPLE_600, 18, "clipboard")));
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
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
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

        TAUser.CV userCv = currentUser.getCv();
        boolean hasExistingCv = userCv != null && userCv.isUploaded() &&
                userCv.getOriginalFileName() != null &&
                !userCv.getOriginalFileName().isEmpty();

        String cvHint;
        String cvTopLine;
        boolean showGreenStyle = hasExistingCv;
        if (hasExistingCv) {
            cvTopLine = userCv.getOriginalFileName() + " (already uploaded in profile)";
            cvHint = "Click to choose a different file";
        } else {
            cvTopLine = "No CV uploaded yet";
            cvHint = "Click to upload your CV";
        }

        JPanel resumeBox = createUploadBox(cvTopLine, cvHint, showGreenStyle);
        resumeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        body.add(resumeBox);

        body.add(Box.createVerticalStrut(28));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JobsPortalUi.OutlinePurpleButton saveDraftBtn = JobsPortalUi.outlineButton(
                "Save as Draft", new Font("Segoe UI", Font.BOLD, 14));
        JobsPortalUi.PurpleGradientButton submitBtn = JobsPortalUi.gradientButton(
                "Submit Application", new Font("Segoe UI", Font.BOLD, 15), miniIcon(Color.WHITE, 16, "send"));
        JobsPortalUi.OutlinePurpleButton cancelBtn = JobsPortalUi.outlineButton(
                "Cancel", new Font("Segoe UI", Font.BOLD, 14));

        final String[] selectedCvPath = {null};
        if (hasExistingCv && userCv.getFilePath() != null && !userCv.getFilePath().isBlank()) {
            selectedCvPath[0] = userCv.getFilePath();
        }

        saveDraftBtn.addActionListener(ev -> {
            Application draftApp = buildApplicationFromForm(job, fullNameField, studentIdField,
                    emailField, phoneField, programField, gpaField, skillsArea,
                    experienceArea, availabilityArea, motivationArea, selectedCvPath);
            draftApp.setDraft(true);
            dataService.saveDraft(draftApp, job);
            showCenteredMessage(panel,
                    "Draft saved successfully.\n\nYou can continue editing or submit later from My Applications - Drafts.",
                    "Draft Saved", JOptionPane.INFORMATION_MESSAGE);
            callback.onDraftSaved();
        });

        cancelBtn.addActionListener(e -> callback.onBackToJobDetail(job));

        btnRow.add(saveDraftBtn);
        btnRow.add(submitBtn);
        btnRow.add(cancelBtn);
        body.add(btnRow);

        mainCard.add(body);
        column.add(mainCard);

        panel.add(column);

        JButton resumePickBtn = (JButton) resumeBox.getClientProperty("pickButton");
        JLabel resumeHintLbl = (JLabel) resumeBox.getClientProperty("hintLabel");
        resumePickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(fileChooserParent(panel));
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedCvPath[0] = f.getAbsolutePath();
                resumeHintLbl.setText(f.getName() + " (selected)");
                resumeHintLbl.setForeground(UI_Constants.SUCCESS_COLOR);
            }
        });

        submitBtn.addActionListener(e -> {
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

            Application app = buildApplicationFromForm(job, fullNameField, studentIdField,
                    emailField, phoneField, programField, gpaField, skillsArea,
                    experienceArea, availabilityArea, motivationArea, selectedCvPath);

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
        JPanel wrap = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, POSITION_HEADER_BG, w, h, Color.WHITE));
                g2.fillRoundRect(0, 0, w, h, 18, 18);
                g2.setColor(POSITION_HEADER_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(18, 22, 18, 22));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel icon = new JLabel(miniIcon(JobsPortalUi.PURPLE_600, 28, "briefcase"));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(icon, 34,
                new Color(0xEEE8FF), new Color(0xE2D8FF), 1f, false,
                new Insets(16, 16, 16, 16));
        iconTile.setPreferredSize(new Dimension(72, 72));
        iconTile.setMinimumSize(new Dimension(72, 72));
        iconTile.setMaximumSize(new Dimension(72, 72));
        wrap.add(iconTile, BorderLayout.WEST);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel tag = new JLabel("Position Details");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tag.setForeground(JobsPortalUi.PURPLE_700);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(tag);
        textCol.add(Box.createVerticalStrut(8));

        JLabel jt = new JLabel(job.getTitle());
        jt.setFont(new Font("Segoe UI", Font.BOLD, 22));
        jt.setForeground(DARK_TEXT);
        jt.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(jt);
        textCol.add(Box.createVerticalStrut(10));

        String courseLine = job.getCourseCode();
        if (job.getCourse() != null && job.getCourse().getCourseName() != null) {
            courseLine = courseLine + "  —  " + job.getCourse().getCourseName();
        }

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.add(metaChip("Course", courseLine));
        meta.add(metaSeparator());
        meta.add(metaChip("Instructor", job.getInstructorName()));
        meta.add(metaSeparator());
        meta.add(metaChip("Application Deadline", formatDeadlinePretty(job.getDeadlineDisplay())));
        textCol.add(meta);

        wrap.add(textCol, BorderLayout.CENTER);
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
        } catch (NumberFormatException ignored) {
        }
        return raw;
    }

    private String formatUserSkills(TAUser user) {
        if (user == null || user.getSkills() == null) {
            return "";
        }
        List<String> all = new ArrayList<>();
        TAUser.Skills s = user.getSkills();
        List<TAUser.Skill> selectedSkills = s.getSelectedSkills();
        if (selectedSkills != null) {
            for (TAUser.Skill sk : selectedSkills) {
                if (sk != null && sk.getName() != null && !sk.getName().trim().isEmpty()) {
                    all.add(sk.getName().trim());
                }
            }
        }
        return String.join(", ", all);
    }

    private JTextField createEditableField(String value) {
        JTextField field = new RoundedTextField(value == null ? "" : value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(DARK_TEXT);
        field.setCaretColor(JobsPortalUi.PURPLE_600);
        field.setBorder(new EmptyBorder(9, 12, 9, 12));
        return field;
    }

    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(MUTED_TEXT);
        p.add(lbl, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(formIconTile(fieldIconForLabel(label)), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    private JPanel createLabeledArea(String label, String hint, JTextArea area) {
        JPanel wrap = new JPanel(new BorderLayout(12, 0));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrap.add(formIconTile(fieldIconForLabel(label)), BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(MUTED_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lbl);

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLbl.setForeground(new Color(148, 163, 184));
        hintLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintLbl.setBorder(new EmptyBorder(2, 0, 6, 0));
        content.add(hintLbl);

        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(DARK_TEXT);
        area.setCaretColor(JobsPortalUi.PURPLE_600);
        area.setOpaque(false);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        JPanel areaShell = JobsPortalUi.wrapRoundedInner(area, 12, Color.WHITE, INPUT_BORDER, 1f, false,
                new Insets(0, 0, 0, 0));
        areaShell.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(areaShell);

        wrap.add(content, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createUploadBox(String topLine, String bottomLine) {
        return createUploadBox(topLine, bottomLine, false);
    }

    private JPanel createUploadBox(String topLine, String bottomLine, boolean isUploaded) {
        JPanel box = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color bg = isUploaded ? GREEN_BG : Color.WHITE;
                Color border = isUploaded ? GREEN_BORDER : CARD_BORDER;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.setColor(border);
                if (isUploaded) {
                    g2.setStroke(new BasicStroke(1.2f));
                } else {
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8f, 6f}, 0));
                }
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(formIconTile(miniIcon(isUploaded ? GREEN_TEXT : JobsPortalUi.PURPLE_600, 18,
                isUploaded ? "file-check" : "folder")), BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("<html>" + escapeHtml(topLine) + "</html>");
        hint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hint.setForeground(isUploaded ? GREEN_TEXT : DARK_TEXT);
        center.add(hint);

        JLabel action = new JLabel(bottomLine);
        action.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        action.setForeground(isUploaded ? new Color(21, 128, 61) : MUTED_TEXT);
        action.setBorder(new EmptyBorder(6, 0, 0, 0));
        center.add(action);
        left.add(center, BorderLayout.CENTER);

        box.add(left, BorderLayout.CENTER);

        JButton pick = JobsPortalUi.outlineButton("Choose file", new Font("Segoe UI", Font.BOLD, 13));
        box.add(pick, BorderLayout.EAST);

        box.putClientProperty("pickButton", pick);
        box.putClientProperty("hintLabel", hint);
        return box;
    }

    private Application buildApplicationFromForm(Job job, JTextField fullNameField,
            JTextField studentIdField, JTextField emailField, JTextField phoneField,
            JTextField programField, JTextField gpaField, JTextArea skillsArea,
            JTextArea experienceArea, JTextArea availabilityArea, JTextArea motivationArea,
            String[] selectedCvPath) {
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
        if (gpaField != null && !gpaField.getText().trim().isEmpty()) {
            try {
                appSnap.setGpa(Double.parseDouble(gpaField.getText().trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        app.setApplicantSnapshot(appSnap);

        Application.ApplicationForm appForm = new Application.ApplicationForm();
        if (skillsArea != null) {
            String[] skills = skillsArea.getText().split(",");
            appForm.setRelevantSkills(Arrays.stream(skills).map(String::trim).filter(s -> !s.isEmpty()).toList());
        }
        if (experienceArea != null) {
            appForm.setRelevantExperience(experienceArea.getText().trim());
        }
        if (availabilityArea != null) {
            appForm.setAvailability(availabilityArea.getText().trim());
        }
        if (motivationArea != null) {
            appForm.setMotivationCoverLetter(motivationArea.getText().trim());
        }
        app.setApplicationForm(appForm);

        Application.Attachments at = ApplyAttachmentService.buildAttachments(
                currentUser,
                dataService,
                getCvUploadBase(),
                selectedCvPath,
                List.of()
        );
        app.setAttachments(at);
        app.setJobId(job.getJobId());
        return app;
    }

    private void buildContentForDraft(Job job, Application draft) {
        panel.removeAll();

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        column.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.setMaximumSize(new Dimension(FORM_MAX_WIDTH, Integer.MAX_VALUE));

        column.add(buildPageHeader(
                "\u2190  Back to My Applications",
                "Continue Editing Draft",
                "Review and complete your saved TA application",
                () -> callback.onBackToJobDetail(job)));

        JPanel mainCard = new SoftCardPanel(22, Color.WHITE, CARD_BORDER, true);
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainCard.setBorder(new EmptyBorder(0, 0, 24, 0));
        mainCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        mainCard.add(buildPositionHeader(job));
        mainCard.add(Box.createVerticalStrut(20));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 24, 0, 24));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(createHintBanner());

        body.add(Box.createVerticalStrut(22));

        body.add(sectionTitle("Application Information", miniIcon(JobsPortalUi.PURPLE_600, 18, "clipboard")));
        body.add(Box.createVerticalStrut(16));

        Application.ApplicantSnapshot snap = draft.getApplicantSnapshot();
        String fullName = snap != null && snap.getFullName() != null ? snap.getFullName() : currentUser.getProfile().getFullName();
        String studentId = snap != null && snap.getStudentId() != null ? snap.getStudentId() : currentUser.getProfile().getStudentId();
        String email = snap != null && snap.getEmail() != null ? snap.getEmail() : currentUser.getAccount().getEmail();
        String phone = snap != null && snap.getPhoneNumber() != null ? snap.getPhoneNumber() : currentUser.getProfile().getPhoneNumber();
        String program = snap != null && snap.getProgramMajor() != null ? snap.getProgramMajor() : currentUser.getProfile().getProgramMajor();
        String gpa = snap != null && snap.getGpa() > 0 ? String.valueOf(snap.getGpa()) : String.valueOf(currentUser.getAcademic().getGpa());

        JTextField fullNameField = createEditableField(fullName);
        JTextField studentIdField = createEditableField(studentId);
        JTextField emailField = createEditableField(email);
        JTextField phoneField = createEditableField(phone);
        JTextField programField = createEditableField(program);
        JTextField gpaField = createEditableField(gpa);

        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        grid.add(createFieldPanel("Full Name *", fullNameField));
        grid.add(createFieldPanel("Student ID *", studentIdField));
        grid.add(createFieldPanel("Email *", emailField));
        grid.add(createFieldPanel("Phone Number *", phoneField));
        grid.add(createFieldPanel("Program / Major *", programField));
        grid.add(createFieldPanel("GPA (Optional)", gpaField));
        body.add(grid);

        body.add(Box.createVerticalStrut(18));

        Application.ApplicationForm form = draft.getApplicationForm();
        String skills = form != null && form.getRelevantSkills() != null ? String.join(", ", form.getRelevantSkills()) : formatUserSkills(currentUser);
        String experience = form != null && form.getRelevantExperience() != null ? form.getRelevantExperience() : "";
        String availability = form != null && form.getAvailability() != null ? form.getAvailability() : "";
        String motivation = form != null && form.getMotivationCoverLetter() != null ? form.getMotivationCoverLetter() : "";

        JTextArea skillsArea = UI_Helper.createTextArea(4);
        body.add(createLabeledArea("Relevant Skills *", "e.g., Java, Python, Data Structures", skillsArea));
        skillsArea.setText(skills);

        body.add(Box.createVerticalStrut(14));

        JTextArea experienceArea = UI_Helper.createTextArea(5);
        body.add(createLabeledArea("Relevant Experience *", "Describe TA/grading experience, projects, etc.", experienceArea));
        experienceArea.setText(experience);

        body.add(Box.createVerticalStrut(14));

        JTextArea availabilityArea = UI_Helper.createTextArea(3);
        body.add(createLabeledArea("Availability *", "e.g., Mon/Wed 10am-12pm, available for office hours", availabilityArea));
        availabilityArea.setText(availability);

        body.add(Box.createVerticalStrut(14));

        JTextArea motivationArea = UI_Helper.createTextArea(5);
        body.add(createLabeledArea("Motivation / Cover Letter *", "Why are you interested in this TA position?", motivationArea));
        motivationArea.setText(motivation);

        body.add(Box.createVerticalStrut(20));

        JLabel resumeTitle = new JLabel("Resume / CV *");
        resumeTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resumeTitle.setForeground(UI_Constants.TEXT_SECONDARY);
        resumeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(resumeTitle);
        body.add(Box.createVerticalStrut(8));

        TAUser.CV userCv = currentUser.getCv();
        boolean hasExistingCv = userCv != null && userCv.isUploaded() &&
                userCv.getOriginalFileName() != null &&
                !userCv.getOriginalFileName().isEmpty();

        String draftCvFileName = null;
        if (draft.getAttachments() != null && draft.getAttachments().getCv() != null) {
            draftCvFileName = draft.getAttachments().getCv().getOriginalFileName();
        }

        String cvHint;
        String cvTopLine;
        boolean showGreenStyle = (draftCvFileName != null && !draftCvFileName.isEmpty()) || hasExistingCv;
        if (draftCvFileName != null && !draftCvFileName.isEmpty()) {
            cvTopLine = draftCvFileName + " (previously selected)";
            cvHint = "Click to choose a different file";
        } else if (hasExistingCv) {
            cvTopLine = userCv.getOriginalFileName() + " (already uploaded in profile)";
            cvHint = "Click to choose a different file";
        } else {
            cvTopLine = "No CV uploaded yet";
            cvHint = "Click to upload your CV";
        }

        JPanel resumeBox = createUploadBox(cvTopLine, cvHint, showGreenStyle);
        resumeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        body.add(resumeBox);

        body.add(Box.createVerticalStrut(28));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JobsPortalUi.OutlinePurpleButton saveDraftBtn = JobsPortalUi.outlineButton(
                "Save Draft", new Font("Segoe UI", Font.BOLD, 14));
        JobsPortalUi.PurpleGradientButton submitBtn = JobsPortalUi.gradientButton(
                "Submit Application", new Font("Segoe UI", Font.BOLD, 15), miniIcon(Color.WHITE, 16, "send"));
        JobsPortalUi.OutlinePurpleButton cancelBtn = JobsPortalUi.outlineButton(
                "Cancel", new Font("Segoe UI", Font.BOLD, 14));

        btnRow.add(saveDraftBtn);
        btnRow.add(submitBtn);
        btnRow.add(cancelBtn);
        body.add(btnRow);

        mainCard.add(body);
        column.add(mainCard);
        panel.add(column);

        final String[] selectedCvPath = {null};
        if (draft.getAttachments() != null && draft.getAttachments().getCv() != null) {
            selectedCvPath[0] = draft.getAttachments().getCv().getFilePath();
        }

        JButton resumePickBtn = (JButton) resumeBox.getClientProperty("pickButton");
        JLabel resumeHintLbl = (JLabel) resumeBox.getClientProperty("hintLabel");
        if (draft.getAttachments() != null && draft.getAttachments().getCv() != null) {
            resumeHintLbl.setText(draft.getAttachments().getCv().getOriginalFileName() + " (selected)");
            resumeHintLbl.setForeground(UI_Constants.SUCCESS_COLOR);
        }
        resumePickBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(fileChooserParent(panel));
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedCvPath[0] = f.getAbsolutePath();
                resumeHintLbl.setText(f.getName() + " (selected)");
                resumeHintLbl.setForeground(UI_Constants.SUCCESS_COLOR);
            }
        });

        saveDraftBtn.addActionListener(ev -> {
            Application draftApp = buildApplicationFromForm(job, fullNameField, studentIdField,
                    emailField, phoneField, programField, gpaField, skillsArea,
                    experienceArea, availabilityArea, motivationArea, null);
            draftApp.setDraft(true);
            String savedId = dataService.saveDraft(draftApp, job);
            currentDraftId = savedId;
            showCenteredMessage(panel,
                    "Draft saved successfully.\n\nYou can continue editing or submit later from My Applications - Drafts.",
                    "Draft Saved", JOptionPane.INFORMATION_MESSAGE);
            callback.onDraftSaved();
        });

        cancelBtn.addActionListener(e -> callback.onBackToJobDetail(job));

        submitBtn.addActionListener(e -> {
            if (dataService.hasAppliedToJob(job.getJobId())) {
                showCenteredMessage(panel,
                        "You have already submitted an application for this position.",
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

            dataService.deleteDraft(currentDraftId);
            Application app = buildApplicationFromForm(job, fullNameField, studentIdField,
                    emailField, phoneField, programField, gpaField, skillsArea,
                    experienceArea, availabilityArea, motivationArea, selectedCvPath);
            dataService.addApplication(app);

            showCenteredMessage(panel,
                    "Application submitted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            callback.onSubmitSuccess();
        });

        panel.revalidate();
        panel.repaint();
    }

    private JPanel buildPageHeader(String backText, String title, String subtitle, Runnable onBack) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(new EmptyBorder(0, 0, 22, 0));

        JButton backBtn = new JButton(backText);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(JobsPortalUi.PURPLE_600);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 12, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> onBack.run());
        header.add(backBtn);

        JPanel titleRow = new JPanel(new BorderLayout(18, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(miniIcon(JobsPortalUi.PURPLE_600, 24, "clipboard"));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(icon, 18, LAVENDER_TILE, CARD_BORDER, 1f, false,
                new Insets(12, 12, 12, 12));
        iconTile.setPreferredSize(new Dimension(56, 56));
        iconTile.setMinimumSize(new Dimension(56, 56));
        iconTile.setMaximumSize(new Dimension(56, 56));
        titleRow.add(iconTile, BorderLayout.WEST);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(titleLabel);
        textCol.add(Box.createVerticalStrut(6));
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(subtitleLabel);
        titleRow.add(textCol, BorderLayout.CENTER);

        header.add(titleRow);
        return header;
    }

    private JPanel createHintBanner() {
        JPanel hintCard = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HINT_BANNER_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(HINT_BANNER_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hintCard.setOpaque(false);
        hintCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        hintCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintCard.add(new JLabel(miniIcon(new Color(0xD97706), 18, "info")), BorderLayout.WEST);
        JLabel hintText = new JLabel(
                "<html><b>Note:</b> Your profile data, skills, and CV have been pre-filled. You may edit anything before submitting.</html>");
        hintText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hintText.setForeground(MUTED_TEXT);
        hintCard.add(hintText, BorderLayout.CENTER);
        return hintCard;
    }

    private JPanel sectionTitle(String title, Icon icon) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bar = new JPanel();
        bar.setBackground(JobsPortalUi.PURPLE_600);
        Dimension barSize = new Dimension(4, 24);
        bar.setPreferredSize(barSize);
        bar.setMinimumSize(barSize);
        bar.setMaximumSize(new Dimension(4, 26));
        row.add(bar);
        row.add(Box.createHorizontalStrut(10));
        row.add(new JLabel(icon));
        row.add(Box.createHorizontalStrut(8));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(TITLE_PURPLE);
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        row.add(label);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JPanel formIconTile(Icon icon) {
        JLabel iconLabel = new JLabel(icon);
        JPanel tile = JobsPortalUi.wrapRoundedInner(iconLabel, 10,
                new Color(0xF5F0FF), new Color(0xE5DAFF), 1f, false,
                new Insets(10, 10, 10, 10));
        tile.setPreferredSize(new Dimension(42, 42));
        tile.setMinimumSize(new Dimension(42, 42));
        tile.setMaximumSize(new Dimension(42, 42));
        return tile;
    }

    private Icon fieldIconForLabel(String label) {
        String s = label == null ? "" : label.toLowerCase();
        if (s.contains("name")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "person");
        if (s.contains("student")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "id");
        if (s.contains("email")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "mail");
        if (s.contains("phone")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "phone");
        if (s.contains("program")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "cap");
        if (s.contains("gpa")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "chart");
        if (s.contains("skill")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "pin");
        if (s.contains("experience")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "briefcase");
        if (s.contains("availability")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "clock");
        if (s.contains("motivation")) return miniIcon(JobsPortalUi.PURPLE_600, 18, "edit");
        return miniIcon(JobsPortalUi.PURPLE_600, 18, "clipboard");
    }

    private Component metaSeparator() {
        JLabel sep = new JLabel("  |  ");
        sep.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sep.setForeground(new Color(0xB8A8F8));
        return sep;
    }

    private JPanel metaChip(String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label + ": ");
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(JobsPortalUi.PURPLE_700);
        JLabel v = new JLabel(value == null || value.isBlank() ? "—" : value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        v.setForeground(MUTED_TEXT);
        p.add(l);
        p.add(v);
        return p;
    }

    private Icon miniIcon(Color ink, int size, String kind) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setColor(ink);
                float sw = Math.max(1.55f, size * 0.11f);
                g2.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                int cx = s / 2;
                int cy = s / 2;
                switch (kind) {
                    case "person" -> {
                        // 按画布比例绘制，避免原先 8×8 头像 + 细弧在 18px 下发糊
                        float cxf = s / 2f;
                        float headR = s * 0.24f;
                        float headCy = s * 0.32f;
                        g2.draw(new Ellipse2D.Float(cxf - headR, headCy - headR, headR * 2f, headR * 2f));
                        float aw = s * 0.82f;
                        float ah = s * 0.48f;
                        float ax = cxf - aw / 2f;
                        float ay = s * 0.52f;
                        g2.draw(new Arc2D.Float(ax, ay, aw, ah, 210, 125, Arc2D.OPEN));
                    }
                    case "id" -> {
                        g2.drawRoundRect(3, 4, s - 6, s - 8, 3, 3);
                        g2.drawOval(6, 7, 4, 4);
                        g2.drawLine(12, 8, s - 5, 8);
                        g2.drawLine(6, 14, s - 5, 14);
                    }
                    case "mail" -> {
                        g2.drawRoundRect(3, 5, s - 6, s - 10, 3, 3);
                        g2.drawLine(4, 6, cx, cy + 1);
                        g2.drawLine(cx, cy + 1, s - 4, 6);
                    }
                    case "phone" -> {
                        // 圆角矩形「手机」轮廓，比双弧翻盖示意更易辨认
                        float cxf = s / 2f;
                        float bw = s * 0.46f;
                        float bh = s * 0.72f;
                        float x0 = cxf - bw / 2f;
                        float y0 = s * 0.12f;
                        float corner = Math.max(2f, s * 0.13f);
                        g2.draw(new RoundRectangle2D.Float(x0, y0, bw, bh, corner, corner));
                        float earHalf = s * 0.11f;
                        float earY = y0 + s * 0.10f;
                        g2.draw(new Line2D.Float(cxf - earHalf, earY, cxf + earHalf, earY));
                        float homeR = s * 0.055f;
                        g2.draw(new Ellipse2D.Float(cxf - homeR, y0 + bh - s * 0.15f, homeR * 2f, homeR * 2f));
                    }
                    case "cap" -> {
                        Polygon cap = new Polygon();
                        cap.addPoint(2, 7); cap.addPoint(cx, 3); cap.addPoint(s - 2, 7); cap.addPoint(cx, 11);
                        g2.drawPolygon(cap);
                        g2.drawLine(6, 10, 6, 14);
                        g2.drawLine(6, 14, s - 6, 14);
                        g2.drawLine(s - 6, 10, s - 6, 14);
                    }
                    case "chart" -> {
                        g2.drawLine(4, s - 4, s - 4, s - 4);
                        g2.drawLine(5, s - 5, 8, s - 9);
                        g2.drawLine(8, s - 9, 11, s - 7);
                        g2.drawLine(11, s - 7, s - 4, 5);
                    }
                    case "pin" -> {
                        g2.drawOval(cx - 5, 3, 10, 10);
                        g2.fillOval(cx - 2, 7, 4, 4);
                        g2.drawLine(cx, 13, cx, s - 3);
                    }
                    case "briefcase" -> {
                        g2.drawRoundRect(3, 6, s - 6, s - 8, 3, 3);
                        g2.drawLine(cx - 4, 6, cx - 4, 4);
                        g2.drawLine(cx + 4, 6, cx + 4, 4);
                        g2.drawLine(cx - 4, 4, cx + 4, 4);
                    }
                    case "clock" -> {
                        g2.drawOval(3, 3, s - 6, s - 6);
                        g2.drawLine(cx, cy, cx, 6);
                        g2.drawLine(cx, cy, s - 6, cy);
                    }
                    case "edit" -> {
                        g2.drawLine(5, s - 5, s - 5, 5);
                        g2.drawLine(s - 7, 3, s - 3, 7);
                        g2.drawLine(4, s - 4, 8, s - 5);
                    }
                    case "file-check" -> {
                        g2.drawRoundRect(4, 3, s - 8, s - 6, 2, 2);
                        g2.drawLine(7, cy, cx - 1, cy + 4);
                        g2.drawLine(cx - 1, cy + 4, s - 6, 7);
                    }
                    case "folder" -> {
                        g2.drawRoundRect(3, 6, s - 6, s - 8, 3, 3);
                        g2.drawLine(4, 7, 8, 4);
                        g2.drawLine(8, 4, 12, 4);
                        g2.drawLine(12, 4, 14, 6);
                    }
                    case "info" -> {
                        g2.drawOval(3, 3, s - 6, s - 6);
                        g2.drawLine(cx, 8, cx, s - 5);
                        g2.fillOval(cx - 1, 5, 3, 3);
                    }
                    case "send" -> {
                        Polygon p = new Polygon();
                        p.addPoint(3, 4); p.addPoint(s - 3, cx); p.addPoint(3, s - 4); p.addPoint(6, cx);
                        g2.drawPolygon(p);
                        g2.drawLine(6, cx, s - 3, cx);
                    }
                    default -> {
                        g2.drawRoundRect(4, 3, s - 8, s - 6, 2, 2);
                        g2.drawLine(7, 7, s - 7, 7);
                        g2.drawLine(7, 11, s - 7, 11);
                    }
                }
                g2.dispose();
            }

            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static final class RoundedTextField extends JTextField {
        RoundedTextField(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(INPUT_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class SoftCardPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color border;
        private final boolean shadow;

        SoftCardPanel(int arc, Color fill, Color border, boolean shadow) {
            this.arc = arc;
            this.fill = fill;
            this.border = border;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (shadow) {
                g2.setColor(new Color(79, 53, 217, 14));
                g2.fillRoundRect(2, 5, w - 5, h - 7, arc, arc);
                g2.setColor(new Color(17, 16, 51, 7));
                g2.fillRoundRect(1, 3, w - 3, h - 5, arc, arc);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 3, h - 4, arc, arc);
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 4, h - 5, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

}
