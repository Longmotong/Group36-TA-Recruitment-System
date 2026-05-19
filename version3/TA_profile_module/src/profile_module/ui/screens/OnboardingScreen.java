package profile_module.ui.screens;

import profile_module.data.ProfileData;
import profile_module.data.SkillItem;
import profile_module.ui.AppFrame;
import profile_module.ui.Theme;
import profile_module.ui.Ui;
import profile_module.ui.Ui.RoundedTextField;
import profile_module.ui.components.PrimaryButton;
import profile_module.ui.components.SecondaryButton;
import profile_module.ui.components.StepHeader;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    // Step 2 skills (same layout/data model as Edit Skills)
    private final ScrollableViewportPanel step2SectionsContainer = new ScrollableViewportPanel();
    private final Map<String, SelectedSkill> selectedSkills = new LinkedHashMap<>();
    private static final List<String> LEVELS = Arrays.asList("Beginner", "Intermediate", "Advanced");
    private final SecondaryButton step2SkipButton = new SecondaryButton("Skip for now");

    private static final SkillGroup TECHNICAL_SKILLS = new SkillGroup(
            "Technical Skills",
            "Select skills and set your proficiency level for each competency",
            "</>",
            Arrays.asList(
                    new SkillSubGroup("Programming Languages",
                            Arrays.asList("Java", "Python", "C/C++", "SQL", "Algorithms & Data Structures", "Object-Oriented Programming (OOP)")),
                    new SkillSubGroup("Hardware & Logic Design",
                            Arrays.asList("VHDL", "Verilog", "Digital Logic Design", "FPGA Development & Debugging")),
                    new SkillSubGroup("Embedded Systems",
                            Arrays.asList("STM32 Development", "FreeRTOS", "Embedded C", "Hardware Driver Development"))
            )
    );

    private static final SkillGroup TOOLS_SKILLS = new SkillGroup(
            "Software & Engineering Tools",
            "Select tools and set your proficiency level for each one",
            "T",
            Arrays.asList(
                    new SkillSubGroup("Professional Development & Simulation Tools",
                            Arrays.asList("Quartus Prime", "Keil5", "STM32CubeIDE", "STM32CubeMX", "CST Studio Suite", "Matlab / Simulink", "Cisco Packet Tracer"))
            )
    );

    private static final SkillGroup LANGUAGE_SKILLS = new SkillGroup(
            "Language Proficiency",
            "Set your English language proficiency level",
            "L",
            Arrays.asList(new SkillSubGroup("English", Arrays.asList("English")))
    );

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
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(Ui.h2("Skills & Competencies"));
        top.add(Box.createVerticalStrut(6));
        top.add(Ui.muted("Select your skills and set proficiency levels for each competency"));
        top.add(Box.createVerticalStrut(14));
        box.add(top, BorderLayout.NORTH);

        step2SectionsContainer.setOpaque(false);
        step2SectionsContainer.setLayout(new BoxLayout(step2SectionsContainer, BoxLayout.Y_AXIS));
        step2SectionsContainer.setBorder(Ui.empty(0, 0, 0, 0));
        rebuildFromProfileSkills();
        rebuildStep2SkillSections();
        JScrollPane scrollPane = new JScrollPane(step2SectionsContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        box.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(Ui.empty(14, 0, 0, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        SecondaryButton back = new SecondaryButton("Back");
        back.addActionListener(e -> {
            persistSelectedSkillsToProfile();
            setStep(1);
        });
        left.add(back);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        step2SkipButton.addActionListener(e -> {
            persistSelectedSkillsToProfile();
            setStep(3);
        });
        PrimaryButton next = new PrimaryButton("Continue to CV Upload");
        next.addActionListener(e -> {
            persistSelectedSkillsToProfile();
            setStep(3);
        });
        right.add(step2SkipButton);
        right.add(next);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        card.add(box, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
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
        if (step == 2) {
            rebuildFromProfileSkills();
            rebuildStep2SkillSections();
        }
        refreshStep3Ui();
        
        if (next == 1) {
            refreshStep1Fields();
        }
    }

    
    private void refreshStep1Fields() {
        
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
    }

   
    private void completeOnboardingAndShowDashboard() {
        persistStep1();
        ProfileData p = app.profile();
        p.recomputeCompletion();
        app.updateProfile(p);
        app.showRoute(AppFrame.ROUTE_DASHBOARD);
    }

    private void rebuildFromProfileSkills() {
        selectedSkills.clear();
        ProfileData p = app.profile();
        if (p.skills == null) return;
        for (SkillItem item : p.skills) {
            if (item == null || item.name == null || item.name.trim().isEmpty()) continue;
            String level = normalizeLevel(item.proficiency);
            if (level == null) continue;
            selectedSkills.put(item.name.trim(), new SelectedSkill(item.name.trim(), normalizeCategory(item.category), level));
        }
    }

    private void rebuildStep2SkillSections() {
        step2SectionsContainer.removeAll();
        step2SectionsContainer.add(buildGroupPanel(TECHNICAL_SKILLS, 2));
        step2SectionsContainer.add(Box.createVerticalStrut(18));
        step2SectionsContainer.add(buildGroupPanel(TOOLS_SKILLS, 2));
        step2SectionsContainer.add(Box.createVerticalStrut(18));
        step2SectionsContainer.add(buildGroupPanel(LANGUAGE_SKILLS, 2));
        step2SectionsContainer.add(Box.createVerticalGlue());
        step2SectionsContainer.revalidate();
        step2SectionsContainer.repaint();
    }

    private JPanel buildGroupPanel(SkillGroup group, int columns) {
        JPanel wrapper = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(createSectionIcon(group.iconLabel));
        header.add(Box.createHorizontalStrut(10));
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(group.title);
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        JLabel desc = Ui.muted(group.description);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(desc);
        header.add(titleBlock);
        header.add(Box.createHorizontalGlue());
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(12));

        for (int i = 0; i < group.subGroups.size(); i++) {
            wrapper.add(buildSubGroupSection(group.subGroups.get(i), columns));
            if (i < group.subGroups.size() - 1) wrapper.add(Box.createVerticalStrut(14));
        }
        return wrapper;
    }

    private JPanel buildSubGroupSection(SkillSubGroup subGroup, int columns) {
        JPanel section = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel(subGroup.title);
        sub.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.LEFT);
        sub.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, Color.BLACK),
                javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 0)));
        section.add(sub);
        section.add(Box.createVerticalStrut(8));

        int total = subGroup.skills.size();
        int rows = Math.max(1, (total + columns - 1) / columns);
        JPanel grid = new JPanel(new GridLayout(rows, columns, 12, 10)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String skillName : subGroup.skills) {
            grid.add(buildSkillCard(skillName, subGroup.title));
        }
        for (int i = 0; i < rows * columns - total; i++) {
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            grid.add(spacer);
        }
        section.add(grid);
        return section;
    }

    private JPanel buildSkillCard(String skillName, String categoryLabel) {
        boolean isSelected = selectedSkills.containsKey(skillName);
        String selectedLevel = isSelected ? selectedSkills.get(skillName).proficiency : null;

        Ui.RoundedPanel card = new Ui.RoundedPanel(12, Theme.SURFACE, isSelected ? Color.BLACK : Theme.BORDER, isSelected ? 2 : 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(Ui.empty(10, 12, 10, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel(skillName);
        name.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(name);
        card.add(Box.createVerticalStrut(8));

        JPanel levels = new JPanel(new GridLayout(1, 3, 4, 0));
        levels.setOpaque(false);
        levels.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String level : LEVELS) {
            PillButton btn = new PillButton(level, Objects.equals(selectedLevel, level));
            btn.addActionListener(e -> {
                SelectedSkill existing = selectedSkills.get(skillName);
                if (existing != null && Objects.equals(existing.proficiency, level)) {
                    selectedSkills.remove(skillName);
                } else {
                    selectedSkills.put(skillName, new SelectedSkill(skillName, categoryLabel, level));
                }
                persistSelectedSkillsToProfile();
                rebuildStep2SkillSections();
            });
            levels.add(btn);
        }
        card.add(levels);
        return card;
    }

    private void persistSelectedSkillsToProfile() {
        ProfileData p = app.profile();
        List<SkillItem> items = new ArrayList<>();
        for (SelectedSkill s : selectedSkills.values()) {
            items.add(new SkillItem(s.name, s.category, s.proficiency));
        }
        p.skills = items;
        p.recomputeCompletion();
        app.updateProfile(p);
    }

    private String normalizeCategory(String category) {
        if (category == null) return "Programming Languages";
        for (SkillSubGroup sg : allSubGroups()) {
            if (sg.title.equalsIgnoreCase(category.trim())) return sg.title;
        }
        return "Programming Languages";
    }

    private String normalizeLevel(String level) {
        if (level == null) return null;
        for (String known : LEVELS) {
            if (known.equalsIgnoreCase(level.trim())) return known;
        }
        return null;
    }

    private List<SkillSubGroup> allSubGroups() {
        List<SkillSubGroup> all = new ArrayList<>();
        all.addAll(TECHNICAL_SKILLS.subGroups);
        all.addAll(TOOLS_SKILLS.subGroups);
        all.addAll(LANGUAGE_SKILLS.subGroups);
        return all;
    }

    private JComponent createSectionIcon(String label) {
        final String text = label == null ? "" : label;
        return new JComponent() {
            {
                Dimension d = new Dimension(38, 38);
                setPreferredSize(d);
                setMaximumSize(d);
                setMinimumSize(d);
            }
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int th = fm.getAscent();
                g2.drawString(text, (getWidth() - tw) / 2, (getHeight() + th) / 2 - 3);
                g2.dispose();
            }
        };
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

        
        refreshStep3Ui();
    }

    private static final class SkillGroup {
        private final String title;
        private final String description;
        private final String iconLabel;
        private final List<SkillSubGroup> subGroups;

        private SkillGroup(String title, String description, String iconLabel, List<SkillSubGroup> subGroups) {
            this.title = title;
            this.description = description;
            this.iconLabel = iconLabel;
            this.subGroups = subGroups;
        }
    }

    private static final class SkillSubGroup {
        private final String title;
        private final List<String> skills;

        private SkillSubGroup(String title, List<String> skills) {
            this.title = title;
            this.skills = skills;
        }
    }

    private static final class SelectedSkill {
        private final String name;
        private final String category;
        private final String proficiency;

        private SelectedSkill(String name, String category, String proficiency) {
            this.name = name;
            this.category = category;
            this.proficiency = proficiency;
        }
    }

    private static final class ScrollableViewportPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class PillButton extends JButton {
        private final boolean filled;

        PillButton(String text, boolean filled) {
            super(text);
            this.filled = filled;
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(filled ? Color.WHITE : Theme.TEXT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(0, 4, 0, 4));
            setPreferredSize(new Dimension(60, 28));
            setMinimumSize(new Dimension(40, 28));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = h;
            if (filled) {
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            } else {
                g2.setColor(new Color(0xF2, 0xF3, 0xF5));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

