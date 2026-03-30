package com.example.tasystem.ui.screens;

import com.example.tasystem.data.ProfileData;
import com.example.tasystem.ui.AppFrame;
import com.example.tasystem.ui.Theme;
import com.example.tasystem.ui.Ui;
import com.example.tasystem.ui.Ui.RoundedTextField;
import com.example.tasystem.ui.components.PrimaryButton;
import com.example.tasystem.ui.components.SecondaryButton;
import com.example.tasystem.ui.components.StepHeader;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDate;

public final class OnboardingScreen extends JPanel {
    private final AppFrame app;

    private final StepHeader stepHeader = new StepHeader();
    private int step = 1;

    private final JPanel stepCards = new JPanel(new java.awt.CardLayout());
    private static final String STEP1 = "s1";
    private static final String STEP2 = "s2";
    private static final String STEP3 = "s3";

    // Step 1 fields
    private final JTextField fullName = Ui.textField("Enter your full name");
    private final JTextField studentId = Ui.textField("e.g., 20230001");
    private final JComboBox<String> year = new JComboBox<>(new String[]{"Select year", "1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate"});
    private final JTextField program = Ui.textField("e.g., Computer Science");
    private final JTextField email = Ui.textField("your.email@university.edu");
    private final JTextField phone = Ui.textField("(555) 123-4567");

    // Step 2 skill
    private final JTextField skillQuickAdd = Ui.textField("e.g., Python, Teaching, Communication");
    private final JLabel noSkillsLabel = Ui.muted("No skills added yet. Add some skills above or skip this step.");

    // Step 3 upload
    private final JLabel uploadStatus = Ui.muted("No file selected.");

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

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.add(new RoundedTextField(skillQuickAdd), BorderLayout.CENTER);
        PrimaryButton add = new PrimaryButton("Add");
        add.setPreferredSize(new Dimension(100, 44));
        add.addActionListener(e -> {
            String v = skillQuickAdd.getText().trim();
            if (!v.isEmpty()) {
                ProfileData p = app.profile();
                p.addSkill(v, "Other Skills", "Intermediate");
                app.updateProfile(p);
                skillQuickAdd.setText("");
                refreshSkillsEmptyState();
            }
        });
        row.add(add, BorderLayout.EAST);
        box.add(Ui.body("Add Skill"));
        box.add(Box.createVerticalStrut(6));
        box.add(row);
        box.add(Box.createVerticalStrut(22));

        JPanel emptyWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        emptyWrap.setOpaque(false);
        emptyWrap.add(noSkillsLabel);
        box.add(emptyWrap);
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
        JLabel skip = new JLabel("Skip for now");
        skip.setFont(Theme.BODY);
        skip.setForeground(Theme.MUTED);
        skip.setHorizontalAlignment(SwingConstants.RIGHT);
        PrimaryButton next = new PrimaryButton("Continue to CV Upload");
        next.addActionListener(e -> setStep(3));
        right.add(skip);
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
        box.add(Box.createVerticalStrut(10));
        box.add(uploadStatus);
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
        JLabel skip = new JLabel("Skip for now");
        skip.setFont(Theme.BODY);
        skip.setForeground(Theme.MUTED);
        PrimaryButton done = new PrimaryButton("Complete Profile");
        done.addActionListener(e -> {
            persistStep1();
            app.updateProfile(app.profile());
            app.showRoute(AppFrame.ROUTE_DASHBOARD);
        });
        right.add(skip);
        right.add(done);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        card.add(box, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
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
        Ui.RoundedPanel wrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        wrap.setLayout(new BorderLayout());
        wrap.add(combo, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(240, 40));
        return labeledField(label, wrap);
    }

    private void setStep(int next) {
        step = Math.max(1, Math.min(3, next));
        stepHeader.setCurrentStep(step);
        java.awt.CardLayout cl = (java.awt.CardLayout) stepCards.getLayout();
        cl.show(stepCards, step == 1 ? STEP1 : step == 2 ? STEP2 : STEP3);
        refreshSkillsEmptyState();
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
    }

    private void refreshSkillsEmptyState() {
        ProfileData p = app.profile();
        boolean empty = (p.skills == null || p.skills.isEmpty());
        noSkillsLabel.setVisible(empty);
        revalidate();
        repaint();
    }

    private void chooseCvFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CV (PDF/DOC/DOCX)");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (f == null) return;
        ProfileData p = app.profile();
        p.cv.fileName = f.getName();
        p.cv.status = "Uploaded";
        p.cv.lastUpdated = LocalDate.now().toString();
        p.cv.sizeLabel = (Math.max(1, f.length() / 1024)) + " KB";
        uploadStatus.setText("Selected: " + f.getName());
        app.updateProfile(p);
    }
}

