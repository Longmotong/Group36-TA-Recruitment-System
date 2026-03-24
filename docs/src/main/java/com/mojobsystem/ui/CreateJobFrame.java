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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
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

public class CreateJobFrame extends JFrame {
    private static final Color PAGE_BG = new Color(248, 250, 252);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_SUB = new Color(100, 116, 139);
    private static final Color PRIMARY = new Color(3, 2, 19);
    private static final Color CARD_BG = Color.WHITE;

    private static final int LABEL_WIDTH = 200;

    private final MyJobsFrame parentFrame;
    private final JobRepository jobRepository;

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

    private final List<String> skills = new ArrayList<>();

    public CreateJobFrame(MyJobsFrame parentFrame, JobRepository jobRepository) {
        this.parentFrame = parentFrame;
        this.jobRepository = jobRepository;

        setTitle("MO Job System - Create New Job");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 940);
        setMinimumSize(new Dimension(880, 700));
        setLocationRelativeTo(parentFrame);
        getContentPane().setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        add(NavigationPanel.create(NavigationPanel.Tab.JOB_MANAGEMENT), BorderLayout.NORTH);
        add(buildScrollContent(), BorderLayout.CENTER);

        styleInputs();
    }

    private void styleInputs() {
        Font fieldFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        for (JTextField f : new JTextField[]{
                titleField, moduleCodeField, moduleNameField, quotaField, weeklyHoursField,
                departmentField, instructorNameField, instructorEmailField, deadlineField, skillInput
        }) {
            f.setFont(fieldFont);
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        }
        locationCombo.setFont(fieldFont);
        employmentCombo.setFont(fieldFont);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        additionalArea.setFont(fieldFont);
        additionalArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        deadlineField.setToolTipText("Deadline yyyy-MM-dd (optional; default +2 weeks in index if empty)");
    }

    private JScrollPane buildScrollContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(PAGE_BG);
        root.setBorder(new EmptyBorder(0, 0, 32, 0));

        root.add(buildPageHeaderStrip());
        root.add(Box.createVerticalStrut(20));

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
        inner.add(Box.createVerticalStrut(28));
        inner.add(buildActionBar());

        gutter.add(inner, BorderLayout.CENTER);
        root.add(gutter);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE_BG);
        return scroll;
    }

    private JPanel buildPageHeaderStrip() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS));
        strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(18, 40, 20, 40)
        ));

        JButton back = new JButton("Back");
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorder(new EmptyBorder(6, 4, 6, 4));
        back.setForeground(TEXT_SUB);
        back.setAlignmentX(LEFT_ALIGNMENT);
        back.addActionListener(e -> dispose());
        strip.add(back);
        strip.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("Create New Job");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(LEFT_ALIGNMENT);
        strip.add(title);
        strip.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Fill in the details to create a new TA recruitment position");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SUB);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        strip.add(subtitle);

        return strip;
    }

    private JPanel buildCard(String title, JPanel body) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(20, 22, 22, 22)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        heading.setForeground(TEXT_MAIN);
        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
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
        addLabeledFullWidth(grid, g, row++, fieldLabel("Application deadline (yyyy-MM-dd)"), deadlineField);

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
            chip.setBackground(new Color(241, 245, 249));
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    new EmptyBorder(4, 10, 4, 6)
            ));
            JLabel text = new JLabel(skillKey);
            text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            text.setForeground(TEXT_MAIN);
            JButton remove = new JButton("×");
            remove.setFocusPainted(false);
            remove.setContentAreaFilled(false);
            remove.setBorder(new EmptyBorder(0, 4, 0, 0));
            remove.setForeground(TEXT_SUB);
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
        submitButton.setText(publishImmediatelyCheck.isSelected() ? "Publish Job" : "Save as Draft");
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());
        right.add(cancel);

        submitButton.setBackground(PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        submitButton.setFocusPainted(false);
        submitButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        submitButton.addActionListener(e -> submitForm());
        right.add(submitButton);

        bar.add(right, BorderLayout.EAST);
        updateSubmitLabel();
        return bar;
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        l.setForeground(TEXT_MAIN);
        return l;
    }

    private static JLabel requiredFieldLabel(String text) {
        return new JLabel("<html><body style='width:" + LABEL_WIDTH + "px'>" + text
                + " <font color='#dc2626'>*</font></body></html>");
    }

    private static JLabel hintLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        l.setForeground(TEXT_SUB);
        return l;
    }

    private void submitForm() {
        String title = titleField.getText().trim();
        String moduleCode = moduleCodeField.getText().trim();
        String moduleName = moduleNameField.getText().trim();
        String quotaStr = quotaField.getText().trim();
        String hoursStr = weeklyHoursField.getText().trim();

        if (title.isEmpty() || moduleCode.isEmpty() || moduleName.isEmpty() || quotaStr.isEmpty() || hoursStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
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
            JOptionPane.showMessageDialog(this,
                    "Quota and Expected Weekly Hours must be valid numbers.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (quota < 1 || weeklyHours < 1) {
            JOptionPane.showMessageDialog(this,
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
                JOptionPane.showMessageDialog(this,
                        "Deadline must be yyyy-MM-DD or leave empty.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        List<Job> existing = jobRepository.loadAllJobs();
        int year = Year.now().getValue();
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

        parentFrame.reloadJobsFromRepository();
        dispose();
    }
}
