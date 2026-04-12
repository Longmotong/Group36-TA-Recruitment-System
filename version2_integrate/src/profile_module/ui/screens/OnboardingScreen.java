package profile_module.ui.screens;

import profile_module.data.ProfileData;
import profile_module.data.SkillItem;
import profile_module.ui.AppFrame;
import profile_module.ui.Theme;
import profile_module.ui.Ui;
import profile_module.ui.Ui.RoundedTextField;
import profile_module.ui.components.Chip;
import profile_module.ui.components.PrimaryButton;
import profile_module.ui.components.SecondaryButton;
import profile_module.ui.components.StepHeader;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class OnboardingScreen extends JPanel {
    private final AppFrame app;

    private final StepHeader stepHeader = new StepHeader();
    private int step = 1;

    private final JPanel stepCards = new JPanel(new java.awt.CardLayout());
    private static final String STEP1 = "s1";
    private static final String STEP2 = "s2";
    private static final String STEP3 = "s3";

    /**
     * Email: local@domain.tld — local part uses letters, digits, and ._%+-; domain has at least one dot; TLD is 2+ letters.
     * Examples valid: a@b.co, user.name+tag@university.edu
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$"
    );

    /**
     * Mainland China mobile: 11 digits, no +86. Second digit 3–9 (covers common ranges 13x–19x).
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // Step 1 fields
    private final JTextField fullName = Ui.textField("Enter your full name");
    private final JTextField studentId = Ui.textField("e.g., 20230001");
    private final JComboBox<String> year = new JComboBox<>(new String[]{"Select year", "1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate"});
    private final JTextField program = Ui.textField("e.g., Computer Science");
    private final JTextField email = Ui.textField("your.email@university.edu");
    private final JTextField phone = Ui.textField("Phone number (11 digits)");

    
    private final JTextField skillName = Ui.textField("e.g., React, Data Analysis");
    private final JComboBox<String> skillCategory = new JComboBox<>(new String[]{
            "Programming", "Teaching / Tutoring", "Communication", "Other Skills"});
    private final JComboBox<String> skillProficiency = new JComboBox<>(new String[]{
            "Beginner", "Intermediate", "Advanced"});
    private final JLabel noSkillsLabel = Ui.muted("No skills added yet. Add some skills above or skip this step.");
    private final JPanel skillsPreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final SecondaryButton step2SkipButton = new SecondaryButton("Skip for now");

    // Step 3 upload
    private final JPanel step3CvStatusHost = new JPanel(new BorderLayout());
    private final SecondaryButton step3SkipButton = new SecondaryButton("Skip for now");

    public OnboardingScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(buildTop(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        setStep(1);
    }

    private JPanel buildTop() {
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        top.setBackground(Theme.SURFACE);
        top.setBorder(Ui.empty(20, 28, 10, 28));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel h1 = new JLabel("Welcome to TA System");
        h1.setFont(Theme.H2.deriveFont(26f));
        h1.setForeground(Theme.TEXT);
        JLabel sub = new JLabel("Let's set up your profile to get started");
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.MUTED);
        titles.add(h1);
        titles.add(Box.createVerticalStrut(4));
        titles.add(sub);

        top.add(titles, BorderLayout.NORTH);
        top.add(stepHeader, BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildBody() {
        JPanel bodyWrap = new JPanel(new BorderLayout());
        bodyWrap.setOpaque(true);
        bodyWrap.setBackground(Theme.BG);
        bodyWrap.setBorder(Ui.empty(18, 28, 28, 28));

        stepCards.setOpaque(false);
        stepCards.add(buildStep1Card(), STEP1);
        stepCards.add(buildStep2Card(), STEP2);
        stepCards.add(buildStep3Card(), STEP3);

        bodyWrap.add(stepCards, BorderLayout.CENTER);
        return bodyWrap;
    }

    private JPanel surfaceCard() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(18, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(22, 22, 22, 22));
        return card;
    }

    private JPanel buildStep1Card() {
        JPanel card = surfaceCard();

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(Ui.h2("Basic Information"));
        header.add(Box.createVerticalStrut(6));
        header.add(Ui.muted("Please provide your basic information to create your TA profile"));

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        content.add(header, c);

        c.gridwidth = 2;
        c.gridx = 0; c.gridy = 1;
        content.add(labeledField("Full Name *", new RoundedTextField(fullName)), c);

        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 2;
        content.add(labeledField("Student ID *", new RoundedTextField(studentId)), c);
        c.gridx = 1; c.gridy = 2;
        content.add(labeledCombo("Year *", year), c);

        c.gridwidth = 2;
        c.gridx = 0; c.gridy = 3;
        content.add(labeledField("Program / Major *", new RoundedTextField(program)), c);

        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 4;
        content.add(labeledField("Email *", new RoundedTextField(email)), c);
        c.gridx = 1; c.gridy = 4;
        content.add(labeledField("Phone Number *", new RoundedTextField(phone)), c);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottom.setOpaque(false);
        PrimaryButton next = new PrimaryButton("Continue to Skills");
        next.addActionListener(e -> {
            String err = validateStep1();
            if (err != null) {
                JOptionPane.showMessageDialog(this, err, "Cannot continue", JOptionPane.WARNING_MESSAGE);
                return;
            }
            persistStep1();
            setStep(2);
        });
        bottom.add(next);

        card.add(content, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildStep2Card() {
        JPanel card = surfaceCard();
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        box.add(Ui.h2("Add Your Skills"));
        box.add(Box.createVerticalStrut(6));
        box.add(Ui.muted("Tell us about your skills and competencies (you can skip and add later)"));
        box.add(Box.createVerticalStrut(18));

        styleSkillCombo(skillCategory);
        styleSkillCombo(skillProficiency);

        JPanel form = new JPanel(new GridLayout(1, 4, 10, 0));
        form.setOpaque(false);
        Ui.RoundedPanel categoryWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        categoryWrap.setLayout(new BorderLayout());
        categoryWrap.add(skillCategory, BorderLayout.CENTER);
        Ui.RoundedPanel profWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        profWrap.setLayout(new BorderLayout());
        profWrap.add(skillProficiency, BorderLayout.CENTER);
        PrimaryButton add = new PrimaryButton("+");
        add.setPreferredSize(new Dimension(52, 44));
        add.addActionListener(e -> {
            String name = skillName.getText().trim();
            if (name.isEmpty()) {
                return;
            }
            ProfileData p = app.profile();
            String cat = String.valueOf(skillCategory.getSelectedItem());
            String prof = String.valueOf(skillProficiency.getSelectedItem());
            p.addSkill(name, cat, prof);
            app.updateProfile(p);
            syncSkillsToTAUser(p);
            skillName.setText("");
            refreshSkillsEmptyState();
        });
        form.add(new RoundedTextField(skillName));
        form.add(categoryWrap);
        form.add(profWrap);
        form.add(add);
        box.add(Ui.body("Add Skill"));
        box.add(Box.createVerticalStrut(6));
        box.add(form);
        box.add(Box.createVerticalStrut(22));

        JPanel emptyWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        emptyWrap.setOpaque(false);
        emptyWrap.add(noSkillsLabel);
        box.add(emptyWrap);
        box.add(Ui.body("Added skills"));
        box.add(Box.createVerticalStrut(6));
        skillsPreviewPanel.setOpaque(false);
        JPanel skillsWrap = new JPanel(new BorderLayout());
        skillsWrap.setOpaque(false);
        skillsWrap.add(skillsPreviewPanel, BorderLayout.CENTER);
        box.add(skillsWrap);
        box.add(Box.createVerticalStrut(10));
        box.add(Box.createVerticalGlue());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        SecondaryButton back = new SecondaryButton("Back");
        back.addActionListener(e -> setStep(1));
        left.add(back);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        step2SkipButton.addActionListener(e -> setStep(3));
        PrimaryButton next = new PrimaryButton("Continue to CV Upload");
        next.addActionListener(e -> setStep(3));
        right.add(step2SkipButton);
        right.add(next);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        card.add(box, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        refreshSkillsEmptyState();
        return card;
    }

    private JPanel buildStep3Card() {
        JPanel card = surfaceCard();
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        box.add(Ui.h2("Upload Your CV"));
        box.add(Box.createVerticalStrut(6));
        box.add(Ui.muted("Upload your curriculum vitae to complete your profile (optional)"));
        box.add(Box.createVerticalStrut(18));

        Ui.RoundedPanel drop = new Ui.RoundedPanel(18, new java.awt.Color(0xFA, 0xFB, 0xFC), Theme.BORDER, 1);
        drop.setLayout(new BoxLayout(drop, BoxLayout.Y_AXIS));
        drop.setBorder(new EmptyBorder(22, 22, 22, 22));
        drop.setPreferredSize(new Dimension(10, 260));

        JLabel icon = new JLabel("📄", SwingConstants.CENTER);
        icon.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 36));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel drag = new JLabel("Drag and drop your CV here", SwingConstants.CENTER);
        drag.setFont(Theme.BODY_BOLD);
        drag.setForeground(Theme.TEXT);
        drag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel or = new JLabel("or", SwingConstants.CENTER);
        or.setFont(Theme.BODY);
        or.setForeground(Theme.MUTED);
        or.setAlignmentX(Component.CENTER_ALIGNMENT);

        PrimaryButton browse = new PrimaryButton("Browse File");
        browse.setAlignmentX(Component.CENTER_ALIGNMENT);
        browse.setPreferredSize(new Dimension(140, 44));
        browse.addActionListener(e -> chooseCvFile());

        JLabel accepts = new JLabel("Accepted formats: PDF, DOC, DOCX (Max 5MB)", SwingConstants.CENTER);
        accepts.setFont(Theme.SMALL);
        accepts.setForeground(Theme.MUTED);
        accepts.setAlignmentX(Component.CENTER_ALIGNMENT);

        drop.add(Box.createVerticalGlue());
        drop.add(icon);
        drop.add(Box.createVerticalStrut(10));
        drop.add(drag);
        drop.add(Box.createVerticalStrut(6));
        drop.add(or);
        drop.add(Box.createVerticalStrut(10));
        drop.add(browse);
        drop.add(Box.createVerticalStrut(12));
        drop.add(accepts);
        drop.add(Box.createVerticalGlue());

        JPanel dropWrap = new JPanel(new BorderLayout());
        dropWrap.setOpaque(false);
        dropWrap.add(drop, BorderLayout.CENTER);

        box.add(dropWrap);
        box.add(Box.createVerticalStrut(14));
        step3CvStatusHost.setOpaque(false);
        box.add(step3CvStatusHost);
        box.add(Box.createVerticalGlue());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        SecondaryButton back = new SecondaryButton("Back");
        back.addActionListener(e -> setStep(2));
        left.add(back);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        step3SkipButton.addActionListener(e -> completeOnboardingAndShowDashboard());
        PrimaryButton done = new PrimaryButton("Complete Profile");
        done.addActionListener(e -> completeOnboardingAndShowDashboard());
        right.add(step3SkipButton);
        right.add(done);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        card.add(box, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        refreshStep3Ui();
        return card;
    }

    private JPanel labeledField(String label, Component field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }

    private JPanel labeledCombo(String label, JComboBox<String> combo) {
        combo.setFont(Theme.BODY);
        combo.setForeground(Theme.TEXT);
        combo.setBackground(Theme.SURFACE);
        combo.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
        suppressComboFocusOutline(combo);
        Ui.RoundedPanel wrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        wrap.setLayout(new BorderLayout());
        wrap.add(combo, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(240, 40));
        return labeledField(label, wrap);
    }

    private void styleSkillCombo(JComboBox<String> combo) {
        combo.setFont(Theme.BODY);
        combo.setForeground(Theme.TEXT);
        combo.setBackground(Theme.SURFACE);
        combo.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
        suppressComboFocusOutline(combo);
    }

    /** Same category coloring as {@link EditSkillsScreen}. */
    private static Chip chipForSkillCategory(String category, String text, boolean closable) {
        if (category == null) return Chip.blue(text, closable);
        String c = category.toLowerCase();
        if (c.contains("program")) return Chip.blue(text, closable);
        if (c.contains("teach")) return Chip.green(text, closable);
        if (c.contains("comm")) return Chip.purple(text, closable);
        return Chip.blue(text, closable);
    }

    /** Removes the default dotted keyboard-focus outline on the year dropdown (Windows LAF). */
    private static void suppressComboFocusOutline(JComboBox<?> combo) {
        combo.putClientProperty("JComponent.outline", null);
        try {
            Component ec = combo.getEditor().getEditorComponent();
            if (ec instanceof JTextField tf) {
                tf.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
            }
        } catch (RuntimeException ignored) {
        }
        for (int i = 0; i < combo.getComponentCount(); i++) {
            Component c = combo.getComponent(i);
            if (c instanceof AbstractButton ab) {
                ab.setFocusPainted(false);
            }
            if (c instanceof JComponent jc) {
                jc.putClientProperty("JComponent.outline", null);
            }
        }
    }

    private void setStep(int next) {
        step = Math.max(1, Math.min(3, next));
        stepHeader.setCurrentStep(step);
        java.awt.CardLayout cl = (java.awt.CardLayout) stepCards.getLayout();
        cl.show(stepCards, step == 1 ? STEP1 : step == 2 ? STEP2 : STEP3);
        refreshSkillsEmptyState();
        refreshStep3Ui();
        
        if (next == 1) {
            refreshStep1Fields();
        }
    }

    
    private void refreshStep1Fields() {
        
    }

    
    private String nonNull(String s) {
        return s != null ? s : "";
    }

    
    private String validateStep1() {
        String name = fullName.getText().trim();
        String sid = studentId.getText().trim();
        String prog = program.getText().trim();
        String em = email.getText().trim();
        String ph = phone.getText().trim();
        String y = (String) year.getSelectedItem();

        if (name.isEmpty() || sid.isEmpty() || prog.isEmpty() || em.isEmpty() || ph.isEmpty()
                || y == null || "Select year".equals(y)) {
            return "Please fill in all fields marked with *";
        }
        if (!EMAIL_PATTERN.matcher(em).matches()) {
            return "Please enter a valid email address.";
        }
        if (!PHONE_PATTERN.matcher(ph).matches()) {
            return "Please enter a valid Chinese mobile number: 11 digits starting with 1 (no +86, spaces, or dashes).";
        }
        return null;
    }

    private void persistStep1() {
        ProfileData p = app.profile();
        p.fullName = fullName.getText().trim();
        p.studentId = studentId.getText().trim();
        String y = (String) year.getSelectedItem();
        p.year = (y == null || "Select year".equals(y)) ? "" : y;
        p.programMajor = program.getText().trim();
        p.email = email.getText().trim();
        p.phoneNumber = phone.getText().trim();
        app.updateProfile(p);
        
        syncProfileToTAUser(p);
    }

   
    private void completeOnboardingAndShowDashboard() {
        persistStep1();
        ProfileData p = app.profile();
        p.recomputeCompletion();
        app.updateProfile(p);
        try {
            TA_Job_Application_Module.DataService ds = TA_Job_Application_Module.DataService.getInstance();
            TA_Job_Application_Module.TAUser user = ds.getCurrentUser();
            if (user != null) {
                user.setProfileCompletion(p.profileCompletionPercent);
                user.setOnboardingCompleted(true);
                ds.saveCurrentUserToFile();
            }
        } catch (Exception e) {
            System.err.println("[OnboardingScreen] Failed to finalize onboarding: " + e.getMessage());
            e.printStackTrace();
        }
        app.showRoute(AppFrame.ROUTE_DASHBOARD);
    }

    
    private void syncProfileToTAUser(ProfileData profileData) {
        try {
            TA_Job_Application_Module.DataService ds = TA_Job_Application_Module.DataService.getInstance();
            TA_Job_Application_Module.TAUser user = ds.getCurrentUser();
            if (user != null) {
                
                TA_Job_Application_Module.TAUser.Profile profile = user.getProfile();
                if (profile == null) {
                    profile = new TA_Job_Application_Module.TAUser.Profile();
                    user.setProfile(profile);
                }
                profile.setFullName(nonNull(profileData.fullName));
                profile.setStudentId(nonNull(profileData.studentId));
                profile.setYear(nonNull(profileData.year));
                profile.setProgramMajor(nonNull(profileData.programMajor));
                profile.setPhoneNumber(nonNull(profileData.phoneNumber));
                profile.setAddress(nonNull(profileData.address));
                profile.setShortBio(nonNull(profileData.shortBio));

                
                if (user.getAccount() != null) {
                    user.getAccount().setEmail(nonNull(profileData.email));
                }

                profileData.recomputeCompletion();
                user.setProfileCompletion(profileData.profileCompletionPercent);

                
                ds.saveCurrentUserToFile();
                System.out.println("[OnboardingScreen] Profile synchronized to TAUser and saved.");
            }
        } catch (Exception e) {
            System.err.println("[OnboardingScreen] Failed to sync profile to TAUser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshSkillsEmptyState() {
        ProfileData p = app.profile();
        boolean empty = (p.skills == null || p.skills.isEmpty());
        noSkillsLabel.setVisible(empty);
        step2SkipButton.setVisible(empty);

        skillsPreviewPanel.removeAll();
        if (!empty) {
            for (SkillItem item : p.skills) {
                if (item == null || item.name == null || item.name.trim().isEmpty()) continue;
                String prof = item.proficiency != null && !item.proficiency.isBlank() ? item.proficiency : "";
                String label = prof.isEmpty() ? item.name.trim() : item.name.trim() + " (" + prof + ")";
                skillsPreviewPanel.add(chipForSkillCategory(item.category, label, false));
            }
        }
        revalidate();
        repaint();
    }

    private void refreshStep3Ui() {
        ProfileData p = app.profile();
        boolean hasCv = p.cv != null && p.cv.fileName != null && !p.cv.fileName.trim().isEmpty();
        step3SkipButton.setVisible(!hasCv);

        step3CvStatusHost.removeAll();
        Color fill = hasCv ? Theme.CHIP_GREEN_BG : new Color(0xFA, 0xFB, 0xFC);
        Ui.RoundedPanel banner = new Ui.RoundedPanel(12, fill, Theme.BORDER, 1);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(Ui.empty(14, 16, 14, 16));

        JLabel head = Ui.body("CV upload status");
        head.setFont(Theme.BODY_BOLD);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.add(head);
        banner.add(Box.createVerticalStrut(8));

        if (hasCv) {
            JLabel st = Ui.body("Status: " + (p.cv.status != null && !p.cv.status.isBlank() ? p.cv.status : "Uploaded"));
            st.setAlignmentX(Component.LEFT_ALIGNMENT);
            banner.add(st);
            banner.add(Box.createVerticalStrut(4));
            JLabel fn = Ui.muted("File: " + p.cv.fileName);
            fn.setAlignmentX(Component.LEFT_ALIGNMENT);
            banner.add(fn);
            if (p.cv.sizeLabel != null && !p.cv.sizeLabel.isBlank()) {
                JLabel sz = Ui.muted("Size: " + p.cv.sizeLabel);
                sz.setAlignmentX(Component.LEFT_ALIGNMENT);
                banner.add(Box.createVerticalStrut(2));
                banner.add(sz);
            }
            if (p.cv.lastUpdated != null && !p.cv.lastUpdated.isBlank()) {
                JLabel lu = Ui.muted("Last updated: " + p.cv.lastUpdated);
                lu.setAlignmentX(Component.LEFT_ALIGNMENT);
                banner.add(Box.createVerticalStrut(2));
                banner.add(lu);
            }
        } else {
            JLabel hint = Ui.muted("No file selected. Use Browse File to upload your CV (PDF, DOC, DOCX).");
            hint.setAlignmentX(Component.LEFT_ALIGNMENT);
            banner.add(hint);
        }

        step3CvStatusHost.add(banner, BorderLayout.CENTER);
        step3CvStatusHost.revalidate();
        step3CvStatusHost.repaint();
        revalidate();
        repaint();
    }

    private void chooseCvFile() {
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
            
            java.nio.file.Files.copy(f.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[OnboardingScreen] CV copied to: " + destFile.getAbsolutePath());
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to copy CV file: " + ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        
        ProfileData p = app.profile();
        p.cv.fileName = f.getName();
        p.cv.status = "Uploaded";
        p.cv.lastUpdated = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        p.cv.sizeLabel = Math.max(1, f.length() / 1024) + " KB";
        app.updateProfile(p);

        
        String relativePath = destDir + File.separator + storedFileName;
        updateTAUserCv(f.getName(), relativePath, LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), true);

        refreshStep3Ui();
    }

    
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
                ProfileData p = app.profile();
                p.recomputeCompletion();
                user.setProfileCompletion(p.profileCompletionPercent);
                app.updateProfile(p);
                
                ds.saveCurrentUserToFile();
                System.out.println("[OnboardingScreen] TAUser.cv updated and saved.");
            }
        } catch (Exception e) {
            System.err.println("[OnboardingScreen] Failed to update TAUser.cv: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private void syncSkillsToTAUser(ProfileData profileData) {
        try {
            TA_Job_Application_Module.DataService ds = TA_Job_Application_Module.DataService.getInstance();
            TA_Job_Application_Module.TAUser user = ds.getCurrentUser();
            if (user != null) {
                TA_Job_Application_Module.TAUser.Skills taSkills = user.getSkills();
                if (taSkills == null) {
                    taSkills = new TA_Job_Application_Module.TAUser.Skills();
                    user.setSkills(taSkills);
                }

                java.util.List<TA_Job_Application_Module.TAUser.Skill> programming = new java.util.ArrayList<>();
                java.util.List<TA_Job_Application_Module.TAUser.Skill> teaching = new java.util.ArrayList<>();
                java.util.List<TA_Job_Application_Module.TAUser.Skill> communication = new java.util.ArrayList<>();
                java.util.List<TA_Job_Application_Module.TAUser.Skill> other = new java.util.ArrayList<>();

                if (profileData.skills != null) {
                    for (SkillItem s : profileData.skills) {
                        TA_Job_Application_Module.TAUser.Skill taSkill = new TA_Job_Application_Module.TAUser.Skill();
                        taSkill.setSkillId("skill_" + System.currentTimeMillis() + "_" + programming.size());
                        taSkill.setName(s.name != null ? s.name : "");
                        taSkill.setProficiency(s.proficiency != null ? s.proficiency : "");

                        String cat = s.category != null ? s.category.toLowerCase() : "";
                        if (cat.contains("program")) {
                            programming.add(taSkill);
                        } else if (cat.contains("teach")) {
                            teaching.add(taSkill);
                        } else if (cat.contains("comm")) {
                            communication.add(taSkill);
                        } else {
                            other.add(taSkill);
                        }
                    }
                }

                taSkills.setProgramming(programming);
                taSkills.setTeaching(teaching);
                taSkills.setCommunication(communication);
                taSkills.setOther(other);

                profileData.recomputeCompletion();
                user.setProfileCompletion(profileData.profileCompletionPercent);
                app.updateProfile(profileData);

                
                ds.saveCurrentUserToFile();
                System.out.println("[OnboardingScreen] Skills synchronized to TAUser and saved.");
            }
        } catch (Exception e) {
            System.err.println("[OnboardingScreen] Failed to sync skills to TAUser: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

