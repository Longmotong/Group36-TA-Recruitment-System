package com.mojobsystem.ui;

import com.mojobsystem.model.Job;
import com.mojobsystem.repository.JobIdGenerator;
import com.mojobsystem.repository.JobRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CreateJobPanel extends JPanel {
    private static final int LABEL_WIDTH = 200;

    private final MoShellHost host;
    private final JobRepository jobRepository;
    private Job editingJob;

    private JLabel formTitleLabel;
    private JLabel formSubtitleLabel;

    private final JTextField titleField = new JTextField();
    private final JTextField moduleCodeField = new JTextField();
    private final JTextField moduleNameField = new JTextField();
    private final JTextField quotaField = new JTextField();
    private final JTextField weeklyHoursField = new JTextField();
    private final JTextField departmentField = new JTextField("Computer Science");
    private final JTextField instructorNameField = new JTextField();
    private final JTextField instructorEmailField = new JTextField("mo@university.edu");
    private final JTextField deadlineField = new JTextField();
    private final JComboBox<String> locationCombo = new JComboBox<>(new String[]{"Hybrid", "On-Campus", "Remote"});
    private final JComboBox<String> employmentCombo = new JComboBox<>(new String[]{
            "Part-time TA", "Full-time TA"
    });
    private final JTextArea descriptionArea = new JTextArea(5, 42);
    private final JTextField skillInput = new JTextField();
    private final JPanel skillsChipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
    private final JTextArea additionalArea = new JTextArea(4, 42);
    private final JCheckBox publishImmediatelyCheck = new JCheckBox();
    private final JButton submitButton = new JButton("Publish Job");
    private JScrollPane formScrollPane;

    private final List<String> skills = new ArrayList<>();

    public CreateJobPanel(MoShellHost host, JobRepository jobRepository) {
        this.host = host;
        this.jobRepository = jobRepository;
        this.editingJob = null;

        setOpaque(false);
        setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBackground(MoUiTheme.PAGE_BG);
        main.add(buildPageHeaderStrip(), BorderLayout.NORTH);
        formScrollPane = buildFormScrollPane();
        main.add(formScrollPane, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBackground(MoUiTheme.PAGE_BG);
        south.setBorder(new EmptyBorder(12, 40, 32, 40));
        south.add(buildActionBar(), BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);

        styleInputs();
        openForCreate();
    }

    public void openForCreate() {
        this.editingJob = null;
        clearFormForNew();
        if (formTitleLabel != null) {
            formTitleLabel.setText("Create New Job");
            formSubtitleLabel.setText("Fill in the details to create a new TA recruitment position");
        }
        publishImmediatelyCheck.setSelected(true);
        updateSubmitLabel();
        SwingUtilities.invokeLater(this::scrollToTop);
    }

    public void openForEdit(Job job) {
        this.editingJob = job;
        populateFromJob(job);
        publishImmediatelyCheck.setSelected(!"Draft".equalsIgnoreCase(job.getStatus()));
        updateSubmitLabel();
        SwingUtilities.invokeLater(this::scrollToTop);
    }

    private void clearFormForNew() {
        titleField.setText("");
        moduleCodeField.setText("");
        moduleNameField.setText("");
        quotaField.setText("");
        weeklyHoursField.setText("");
        departmentField.setText("Computer Science");
        instructorNameField.setText("");
        instructorEmailField.setText("mo@university.edu");
        deadlineField.setText("");
        locationCombo.setSelectedIndex(0);
        employmentCombo.setSelectedIndex(0);
        descriptionArea.setText("");
        additionalArea.setText("");
        skills.clear();
        redrawSkillChips();
    }

    private void styleInputs() {
        Font fieldFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        for (JTextField f : new JTextField[]{
                titleField, moduleCodeField, moduleNameField, quotaField, weeklyHoursField,
                departmentField, instructorNameField, instructorEmailField, deadlineField, skillInput
        }) {
            f.setFont(fieldFont);
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MoUiTheme.BORDER),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        }
        locationCombo.setFont(fieldFont);
        employmentCombo.setFont(fieldFont);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        additionalArea.setFont(fieldFont);
        additionalArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        deadlineField.setToolTipText("Deadline yyyy-MM-dd (optional; default +2 weeks in index if empty)");
    }

    /** Scrollable form only; header + action bar stay fixed for predictable layout. */
    private JScrollPane buildFormScrollPane() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(MoUiTheme.PAGE_BG);
        root.setBorder(new EmptyBorder(12, 0, 24, 0));

        JPanel gutter = new JPanel(new BorderLayout());
        gutter.setOpaque(false);
        gutter.setBorder(new EmptyBorder(0, 40, 0, 40));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setMaximumSize(new Dimension(860, Integer.MAX_VALUE));

        inner.add(buildCard("Basic Information", buildBasicSection()));
        inner.add(Box.createVerticalStrut(18));
        inner.add(buildCard("Detailed Requirements", buildDetailedSection()));
        inner.add(Box.createVerticalStrut(18));
        inner.add(buildCard("Publish Settings", buildPublishSection()));

        gutter.add(inner, BorderLayout.CENTER);
        root.add(gutter);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MoUiTheme.PAGE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void scrollToTop() {
        if (formScrollPane == null) {
            return;
        }
        formScrollPane.getVerticalScrollBar().setValue(0);
        formScrollPane.getHorizontalScrollBar().setValue(0);
        formScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
    }

    private JPanel buildPageHeaderStrip() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS));
        strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 1, 0, new Color(0x93C5FD)),
                        BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER)
                ),
                new EmptyBorder(18, 36, 20, 40)
        ));

        JButton back = new JButton("Back");
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorder(new EmptyBorder(6, 4, 6, 4));
        back.setForeground(MoUiTheme.TEXT_SECONDARY);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> host.showJobList());
        strip.add(back);
        strip.add(Box.createVerticalStrut(10));

        formTitleLabel = new JLabel(editingJob == null ? "Create New Job" : "Edit Job");
        formTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        formTitleLabel.setForeground(new Color(0x0F172A));
        formTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(formTitleLabel);
        strip.add(Box.createVerticalStrut(6));

        formSubtitleLabel = new JLabel(editingJob == null
                ? "Fill in the details to create a new TA recruitment position"
                : "Update posting details — changes apply to this job record");
        formSubtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        formSubtitleLabel.setForeground(MoUiTheme.TEXT_SECONDARY);
        formSubtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(formSubtitleLabel);

        return strip;
    }

    private JPanel buildCard(String title, JPanel body) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(MoUiTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, sectionAccent(title)),
                        BorderFactory.createLineBorder(new Color(0xE2E8F0))
                ),
                new EmptyBorder(20, 22, 22, 22)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        heading.setForeground(new Color(0x0F172A));
        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private static Color sectionAccent(String title) {
        if ("Publish Settings".equals(title)) {
            return new Color(0x93C5FD);
        }
        if ("Detailed Requirements".equals(title)) {
            return new Color(0xBFDBFE);
        }
        return new Color(0x60A5FA);
    }

    private JPanel buildBasicSection() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        int row = 0;

        addLabeledFullWidth(grid, g, row++, requiredFieldLabel("Job Title"), titleField);
        addLabeledFullWidth(grid, g, row++, requiredFieldLabel("Module Code"), moduleCodeField);
        addLabeledFullWidth(grid, g, row++, requiredFieldLabel("Module Name"), moduleNameField);

        JPanel quotaHours = new JPanel(new GridLayout(1, 2, 16, 0));
        quotaHours.setOpaque(false);
        quotaHours.add(labeledStack(requiredFieldLabel("Quota (Number of TAs needed)"), quotaField));
        quotaHours.add(labeledStack(requiredFieldLabel("Expected Weekly Hours"), weeklyHoursField));
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 3;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 14, 0);
        g.anchor = GridBagConstraints.NORTHWEST;
        grid.add(quotaHours, g);
        row++;

        addLabeledFullWidth(grid, g, row++, fieldLabel("Department"), departmentField);
        addLabeledFullWidth(grid, g, row++, fieldLabel("Instructor name"), instructorNameField);
        addLabeledFullWidth(grid, g, row++, fieldLabel("Instructor email"), instructorEmailField);
        addLabeledFullWidth(grid, g, row++, fieldLabel("Application deadline"), deadlineField);

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 12, 16);
        g.anchor = GridBagConstraints.NORTHWEST;
        JLabel locLbl = fieldLabel("Location mode");
        locLbl.setPreferredSize(new Dimension(LABEL_WIDTH, locLbl.getPreferredSize().height));
        grid.add(locLbl, g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);
        grid.add(locationCombo, g);
        row++;

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 0, 16);
        JLabel empLbl = fieldLabel("Employment type");
        empLbl.setPreferredSize(new Dimension(LABEL_WIDTH, empLbl.getPreferredSize().height));
        grid.add(empLbl, g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 0);
        grid.add(employmentCombo, g);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(4, 0, 0, 0));
        wrap.add(grid, BorderLayout.NORTH);
        return wrap;
    }

    private void addLabeledFullWidth(JPanel grid, GridBagConstraints g, int row, JLabel label, JComponent field) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.gridheight = 1;
        g.weightx = 0;
        g.weighty = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 12, 16);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, label.getPreferredSize().height));
        grid.add(label, g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);
        grid.add(field, g);
    }

    private JPanel labeledStack(JLabel label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildDetailedSection() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        int row = 0;

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 8, 16);
        grid.add(fieldLabel("Job Description"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 4, 0);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        grid.add(descriptionArea, g);
        row++;

        g.gridx = 1;
        g.gridy = row;
        g.gridwidth = 2;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 14, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        grid.add(hintLabel("Provide a clear description of what the TA will be doing"), g);
        row++;

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 8, 16);
        grid.add(fieldLabel("Required Skills"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 6, 0);
        skillInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    addSkillFromInput();
                }
            }
        });
        grid.add(skillInput, g);
        row++;

        g.gridx = 1;
        g.gridy = row;
        g.gridwidth = 2;
        g.insets = new Insets(0, 0, 8, 0);
        grid.add(hintLabel("Type a skill and press Enter"), g);
        row++;

        skillsChipPanel.setOpaque(false);
        g.gridx = 1;
        g.gridy = row;
        g.gridwidth = 2;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 14, 0);
        g.anchor = GridBagConstraints.WEST;
        grid.add(skillsChipPanel, g);
        row++;

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 8, 16);
        grid.add(fieldLabel("Additional Requirements"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 0);
        additionalArea.setLineWrap(true);
        additionalArea.setWrapStyleWord(true);
        grid.add(additionalArea, g);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(4, 0, 0, 0));
        wrap.add(grid, BorderLayout.NORTH);
        return wrap;
    }

    private void addSkillFromInput() {
        String s = skillInput.getText().trim();
        if (s.isEmpty()) {
            return;
        }
        if (skills.contains(s)) {
            skillInput.setText("");
            return;
        }
        skills.add(s);
        skillInput.setText("");
        redrawSkillChips();
    }

    private void redrawSkillChips() {
        skillsChipPanel.removeAll();
        for (String skill : skills) {
            final String skillKey = skill;
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            chip.setOpaque(true);
            chip.setBackground(MoUiTheme.BORDER_SOFT);
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MoUiTheme.BORDER),
                    new EmptyBorder(4, 10, 4, 6)
            ));
            JLabel text = new JLabel(skillKey);
            text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            text.setForeground(MoUiTheme.TEXT_PRIMARY);
            JButton remove = new JButton("×");
            remove.setFocusPainted(false);
            remove.setContentAreaFilled(false);
            remove.setBorder(new EmptyBorder(0, 4, 0, 0));
            remove.setForeground(MoUiTheme.TEXT_SECONDARY);
            remove.addActionListener(e -> {
                skills.remove(skillKey);
                redrawSkillChips();
            });
            chip.add(text);
            chip.add(remove);
            skillsChipPanel.add(chip);
        }
        skillsChipPanel.revalidate();
        skillsChipPanel.repaint();
    }

    private JPanel buildPublishSection() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.add(fieldLabel("Publish Immediately"));
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(hintLabel("Make this job posting visible to students right away"));
        row.add(textCol, g);
        g.gridx = 1;
        g.weightx = 0;
        publishImmediatelyCheck.setSelected(true);
        publishImmediatelyCheck.addActionListener(e -> updateSubmitLabel());
        row.add(publishImmediatelyCheck, g);
        return row;
    }

    private void updateSubmitLabel() {
        if (editingJob != null) {
            submitButton.setText("Save Changes");
            return;
        }
        submitButton.setText(publishImmediatelyCheck.isSelected() ? "Publish Job" : "Save as Draft");
    }

    private void populateFromJob(Job j) {
        titleField.setText(j.getTitle());
        moduleCodeField.setText(j.getModuleCode());
        moduleNameField.setText(j.getModuleName());
        quotaField.setText(String.valueOf(j.getQuota()));
        weeklyHoursField.setText(String.valueOf(j.getWeeklyHours()));
        departmentField.setText(j.getDepartment());
        instructorNameField.setText(j.getInstructorName());
        instructorEmailField.setText(j.getInstructorEmail());
        deadlineField.setText(j.getDeadline());
        locationCombo.setSelectedItem(j.getLocationMode());
        employmentCombo.setSelectedItem(j.getEmploymentType());
        descriptionArea.setText(j.getDescription());
        additionalArea.setText(j.getAdditionalRequirements());
        descriptionArea.setCaretPosition(0);
        additionalArea.setCaretPosition(0);
        skills.clear();
        if (j.getRequiredSkills() != null) {
            skills.addAll(j.getRequiredSkills());
        }
        redrawSkillChips();
        if (formTitleLabel != null) {
            formTitleLabel.setText("Edit Job");
            formSubtitleLabel.setText("Update posting details — changes apply to this job record");
        }
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        MoUiTheme.styleOutlineButton(cancel, 8);
        cancel.addActionListener(e -> host.showJobList());
        right.add(cancel);

        MoUiTheme.styleAccentPrimaryButton(submitButton, 10);
        submitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        submitButton.setFocusPainted(false);
        submitButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        int sh = Math.max(42, submitButton.getPreferredSize().height);
        submitButton.setMinimumSize(new Dimension(0, sh));
        submitButton.addActionListener(e -> submitForm());
        right.add(submitButton);

        bar.add(right, BorderLayout.EAST);
        updateSubmitLabel();
        return bar;
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        l.setForeground(MoUiTheme.TEXT_PRIMARY);
        return l;
    }

    private static JLabel requiredFieldLabel(String text) {
        return new JLabel("<html><body style='width:" + LABEL_WIDTH + "px'>" + text
                + " <font color='#DC2626'>*</font></body></html>");
    }

    private static JLabel hintLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        l.setForeground(MoUiTheme.TEXT_SECONDARY);
        return l;
    }

    private void submitForm() {
        String title = titleField.getText().trim();
        String moduleCode = moduleCodeField.getText().trim();
        String moduleName = moduleNameField.getText().trim();
        String quotaStr = quotaField.getText().trim();
        String hoursStr = weeklyHoursField.getText().trim();

        if (title.isEmpty() || moduleCode.isEmpty() || moduleName.isEmpty() || quotaStr.isEmpty() || hoursStr.isEmpty()) {
            JOptionPane.showMessageDialog(host.getShellFrame(),
                    "Please fill in all required fields (title, module, quota, hours).",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quota;
        int weeklyHours;
        try {
            quota = Integer.parseInt(quotaStr);
            weeklyHours = Integer.parseInt(hoursStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(host.getShellFrame(),
                    "Quota and Expected Weekly Hours must be valid numbers.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (quota < 1 || weeklyHours < 1) {
            JOptionPane.showMessageDialog(host.getShellFrame(),
                    "Quota and weekly hours must be at least 1.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String deadlineStr = deadlineField.getText().trim();
        if (!deadlineStr.isEmpty()) {
            try {
                LocalDate.parse(deadlineStr);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(host.getShellFrame(),
                        "Deadline must be yyyy-MM-DD or leave empty.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int year = Year.now().getValue();
        if (editingJob != null) {
            Job job = new Job();
            job.setId(editingJob.getId());
            job.setTitle(title);
            job.setModuleCode(moduleCode);
            job.setModuleName(moduleName);
            job.setQuota(quota);
            job.setWeeklyHours(weeklyHours);
            job.setDepartment(departmentField.getText().trim().isEmpty()
                    ? "Computer Science"
                    : departmentField.getText().trim());
            job.setInstructorName(instructorNameField.getText().trim());
            job.setInstructorEmail(instructorEmailField.getText().trim().isEmpty()
                    ? "mo@university.edu"
                    : instructorEmailField.getText().trim());
            job.setDeadline(deadlineStr);
            job.setLocationMode(String.valueOf(locationCombo.getSelectedItem()));
            job.setEmploymentType(String.valueOf(employmentCombo.getSelectedItem()));
            job.setCourseTerm(editingJob.getCourseTerm());
            job.setCourseYear(editingJob.getCourseYear() > 0 ? editingJob.getCourseYear() : year);
            job.setDescription(descriptionArea.getText().trim());
            job.setAdditionalRequirements(additionalArea.getText().trim());
            job.setRequiredSkills(new ArrayList<>(skills));
            job.setApplicantsCount(editingJob.getApplicantsCount());
            job.setStatus(publishImmediatelyCheck.isSelected() ? "Open" : "Draft");
            
            List<Job> all = new ArrayList<>(jobRepository.loadAllJobs());
            boolean replaced = false;
            for (int i = 0; i < all.size(); i++) {
                if (job.getId().equals(all.get(i).getId())) {
                    all.set(i, job);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                all.add(job);
            }
            jobRepository.saveAllJobs(all);
        } else {
            List<Job> existing = jobRepository.loadAllJobs();
            String canonicalId = JobIdGenerator.nextId(moduleCode, year, "spring", existing);

            Job job = new Job();
            job.setId(canonicalId);
            job.setTitle(title);
            job.setModuleCode(moduleCode);
            job.setModuleName(moduleName);
            job.setQuota(quota);
            job.setWeeklyHours(weeklyHours);
            job.setDepartment(departmentField.getText().trim().isEmpty()
                    ? "Computer Science"
                    : departmentField.getText().trim());
            job.setInstructorName(instructorNameField.getText().trim());
            job.setInstructorEmail(instructorEmailField.getText().trim().isEmpty()
                    ? "mo@university.edu"
                    : instructorEmailField.getText().trim());
            job.setDeadline(deadlineStr);
            job.setLocationMode(String.valueOf(locationCombo.getSelectedItem()));
            job.setEmploymentType(String.valueOf(employmentCombo.getSelectedItem()));
            job.setCourseTerm("Spring");
            job.setCourseYear(year);
            job.setDescription(descriptionArea.getText().trim());
            job.setAdditionalRequirements(additionalArea.getText().trim());
            job.setRequiredSkills(new ArrayList<>(skills));
            job.setApplicantsCount(0);
            job.setStatus(publishImmediatelyCheck.isSelected() ? "Open" : "Draft");

            List<Job> jobs = new ArrayList<>(existing);
            jobs.add(job);
            jobRepository.saveAllJobs(jobs);
        }

        host.jobDataChanged();
        host.showJobList();
    }

}
