package com.mojobsystem.ui.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojobsystem.DataRoot;
import com.mojobsystem.model.job.Job;
import com.mojobsystem.repository.JobIdGenerator;
import com.mojobsystem.repository.JobRepository;
import com.mojobsystem.service.JobDescriptionAiService;
import com.mojobsystem.ui.MoShellHost;
import com.mojobsystem.ui.MoUiTheme;
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
import java.awt.LayoutManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.concurrent.ExecutionException;

public class CreateJobPanel extends JPanel {
    private static final int LABEL_WIDTH = 200;
    private static final Path SKILL_POOL_FILE = DataRoot.resolve().resolve("skill_pool.json");
    private static final Path CREATE_FORM_DRAFT_FILE = DataRoot.resolve()
            .resolve("drafts")
            .resolve("create_job_form_draft.json");
    private static final String CUSTOM_OPTION = "Custom (16+)";
    private static final int NUMBER_DROPDOWN_VISIBLE_ROWS = 6;

    private final MoShellHost host;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JobDescriptionAiService jobDescriptionAiService = new JobDescriptionAiService();
    private final Set<String> skillPool;
    private Job editingJob;

    private JLabel formTitleLabel;
    private JLabel formSubtitleLabel;

    private final JTextField titleField = new JTextField();
    private final JTextField moduleCodeField = new JTextField();
    private final JTextField moduleNameField = new JTextField();
    private final JComboBox<String> quotaField = new JComboBox<>();
    private final JComboBox<String> weeklyHoursField = new JComboBox<>();
    private final JTextField departmentField = new JTextField("Computer Science");
    private final JTextField instructorNameField = new JTextField();
    private final JTextField instructorEmailField = new JTextField("mo@university.edu");
    private final JComboBox<String> deadlineYearField = new JComboBox<>();
    private final JComboBox<String> deadlineMonthField = new JComboBox<>();
    private final JComboBox<String> deadlineDayField = new JComboBox<>();
    private final JComboBox<String> locationCombo = new JComboBox<>(new String[]{"Hybrid", "On-Campus", "Remote"});
    private final JComboBox<String> employmentCombo = new JComboBox<>(new String[]{
            "Part-time TA", "Full-time TA"
    });
    private final JTextArea descriptionArea = new JTextArea(5, 42);
    private final JTextField descriptionPromptField = new JTextField();
    private final JButton generateAiDescriptionButton = new JButton("Generate Description");
    private final JButton undoAiDescriptionButton = new JButton("Undo AI Replace");
    private final JPanel skillPoolTagsPanel = new WrapFlowPanel(FlowLayout.LEFT, 6, 6);
    private final JPanel skillsChipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
    private final JLabel skillsMetaLabel = new JLabel();
    private final JTextArea additionalArea = new JTextArea(4, 42);
    private final JCheckBox publishImmediatelyCheck = new JCheckBox();
    private final JButton submitButton = new JButton("Publish Job");
    private final JLabel formValidationLabel = new JLabel();
    private JScrollPane formScrollPane;
    private boolean updatingNumberDropdowns;
    private Integer customQuotaValue;
    private Integer customWeeklyHoursValue;
    private final Deque<String> aiUndoHistory = new ArrayDeque<>();
    private boolean suspendDraftPersistence;
    private boolean draftDirty;

    private final List<String> skills = new ArrayList<>();

    public CreateJobPanel(MoShellHost host, JobRepository jobRepository) {
        this.host = host;
        this.jobRepository = jobRepository;
        this.editingJob = null;
        this.skillPool = loadSkillPool();

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
        initNumberDropdowns();
        initDeadlineDropdowns();
        initRealtimeValidation();
        initDraftPersistence();
        redrawSkillPoolTags();
        openForCreate(false);
    }

    private void initRealtimeValidation() {
        DocumentListener requiredTextListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshFormValidationState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshFormValidationState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshFormValidationState();
            }
        };
        titleField.getDocument().addDocumentListener(requiredTextListener);
        moduleCodeField.getDocument().addDocumentListener(requiredTextListener);
        moduleNameField.getDocument().addDocumentListener(requiredTextListener);
        quotaField.addActionListener(e -> refreshFormValidationState());
        weeklyHoursField.addActionListener(e -> refreshFormValidationState());
        refreshFormValidationState();
    }

    private void initNumberDropdowns() {
        quotaField.removeAllItems();
        weeklyHoursField.removeAllItems();
        for (int i = 1; i <= 15; i++) {
            String v = String.valueOf(i);
            quotaField.addItem(v);
            weeklyHoursField.addItem(v);
        }
        quotaField.addItem(CUSTOM_OPTION);
        weeklyHoursField.addItem(CUSTOM_OPTION);
        quotaField.addActionListener(e -> onNumberDropdownChanged(quotaField, true));
        weeklyHoursField.addActionListener(e -> onNumberDropdownChanged(weeklyHoursField, false));
        quotaField.setMaximumRowCount(NUMBER_DROPDOWN_VISIBLE_ROWS);
        weeklyHoursField.setMaximumRowCount(NUMBER_DROPDOWN_VISIBLE_ROWS);
        quotaField.setToolTipText("Pick Custom (16+) if quota is greater than 15.");
        weeklyHoursField.setToolTipText("Pick Custom (16+) if weekly hours are greater than 15.");
        quotaField.setSelectedIndex(-1);
        weeklyHoursField.setSelectedIndex(-1);
    }

    private void initDeadlineDropdowns() {
        deadlineYearField.removeAllItems();
        deadlineMonthField.removeAllItems();
        deadlineDayField.removeAllItems();
        int currentYear = Year.now().getValue();
        for (int y = currentYear; y <= currentYear + 5; y++) {
            deadlineYearField.addItem(String.valueOf(y));
        }
        for (int m = 1; m <= 12; m++) {
            deadlineMonthField.addItem(String.format("%02d", m));
        }
        for (int d = 1; d <= 31; d++) {
            deadlineDayField.addItem(String.format("%02d", d));
        }
        deadlineYearField.setMaximumRowCount(8);
        deadlineMonthField.setMaximumRowCount(8);
        deadlineDayField.setMaximumRowCount(8);
        clearDeadlineSelection();
    }

    public void openForCreate() {
        openForCreate(true);
    }

    private void openForCreate(boolean allowDraftPrompt) {
        this.editingJob = null;
        suspendDraftPersistence = true;
        try {
            clearFormForNew();
        } finally {
            suspendDraftPersistence = false;
        }
        boolean restored = false;
        if (allowDraftPrompt && hasSavedDraftFile()) {
            Object[] options = {"Load Last Draft", "Start Blank", "Delete Draft"};
            int choice = JOptionPane.showOptionDialog(
                    host.getShellFrame(),
                    "A saved draft from your previous session was found. How would you like to continue?",
                    "Recovered Draft",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (choice == 0) {
                restored = restoreDraftIfExists();
            } else if (choice == JOptionPane.CLOSED_OPTION) {
                host.showJobList();
                return;
            } else if (choice == 2) {
                clearDraftFile();
            }
        }
        if (formTitleLabel != null) {
            formTitleLabel.setText("Create New Job");
            formSubtitleLabel.setText(restored
                    ? "Recovered your previous draft. Continue editing or publish when ready"
                    : "Fill in the details to create a new TA recruitment position");
        }
        if (!restored) {
            publishImmediatelyCheck.setSelected(true);
        }
        draftDirty = false;
        updateSubmitLabel();
        refreshFormValidationState();
        SwingUtilities.invokeLater(this::scrollToTop);
    }

    public void openForEdit(Job job) {
        this.editingJob = job;
        populateFromJob(job);
        draftDirty = false;
        publishImmediatelyCheck.setSelected(!"Draft".equalsIgnoreCase(job.getStatus()));
        updateSubmitLabel();
        refreshFormValidationState();
        SwingUtilities.invokeLater(this::scrollToTop);
    }

    private Set<String> loadSkillPool() {
        if (!Files.exists(SKILL_POOL_FILE)) {
            return Set.of();
        }
        try {
            List<String> skills = objectMapper.readValue(SKILL_POOL_FILE.toFile(), new TypeReference<List<String>>() {});
            return new LinkedHashSet<>(skills == null ? List.of() : skills);
        } catch (IOException ex) {
            return Set.of();
        }
    }

    private void clearFormForNew() {
        titleField.setText("");
        moduleCodeField.setText("");
        moduleNameField.setText("");
        resetNumberDropdownOptions(quotaField);
        resetNumberDropdownOptions(weeklyHoursField);
        quotaField.setSelectedIndex(-1);
        weeklyHoursField.setSelectedIndex(-1);
        customQuotaValue = null;
        customWeeklyHoursValue = null;
        clearDeadlineSelection();
        departmentField.setText("Computer Science");
        instructorNameField.setText("");
        instructorEmailField.setText("mo@university.edu");
        locationCombo.setSelectedIndex(0);
        employmentCombo.setSelectedIndex(0);
        descriptionArea.setText("");
        additionalArea.setText("");
        aiUndoHistory.clear();
        draftDirty = false;
        refreshAiAssistButtons();
        skills.clear();
        redrawSkillChips();
    }

    private void styleInputs() {
        Font fieldFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        for (JTextField f : new JTextField[]{
                titleField, moduleCodeField, moduleNameField,
                departmentField, instructorNameField, instructorEmailField
        }) {
            f.setFont(fieldFont);
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MoUiTheme.BORDER),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        }
        locationCombo.setFont(fieldFont);
        employmentCombo.setFont(fieldFont);
        quotaField.setFont(fieldFont);
        weeklyHoursField.setFont(fieldFont);
        deadlineYearField.setFont(fieldFont);
        deadlineMonthField.setFont(fieldFont);
        deadlineDayField.setFont(fieldFont);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        descriptionPromptField.setFont(fieldFont);
        descriptionPromptField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        descriptionPromptField.putClientProperty("JTextField.placeholderText",
                "Optional guidance for description (e.g. focus on grading quality and communication)");
        generateAiDescriptionButton.setFocusPainted(false);
        MoUiTheme.styleOutlineButton(generateAiDescriptionButton, 8);
        generateAiDescriptionButton.addActionListener(e -> generateDescriptionWithAi());
        undoAiDescriptionButton.setFocusPainted(false);
        MoUiTheme.styleOutlineButton(undoAiDescriptionButton, 8);
        undoAiDescriptionButton.addActionListener(e -> undoLastAiDescription());
        refreshAiAssistButtons();
        additionalArea.setFont(fieldFont);
        additionalArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MoUiTheme.BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        deadlineYearField.setToolTipText("Application deadline year (optional).");
        deadlineMonthField.setToolTipText("Application deadline month (optional).");
        deadlineDayField.setToolTipText("Application deadline day (optional).");
    }

    private void initDraftPersistence() {
        DocumentListener markDirtyTextListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                markDraftDirty();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                markDraftDirty();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                markDraftDirty();
            }
        };
        for (JTextField f : new JTextField[]{
                titleField, moduleCodeField, moduleNameField, departmentField,
                instructorNameField, instructorEmailField, descriptionPromptField
        }) {
            f.getDocument().addDocumentListener(markDirtyTextListener);
        }
        descriptionArea.getDocument().addDocumentListener(markDirtyTextListener);
        additionalArea.getDocument().addDocumentListener(markDirtyTextListener);

        quotaField.addActionListener(e -> markDraftDirty());
        weeklyHoursField.addActionListener(e -> markDraftDirty());
        deadlineYearField.addActionListener(e -> markDraftDirty());
        deadlineMonthField.addActionListener(e -> markDraftDirty());
        deadlineDayField.addActionListener(e -> markDraftDirty());
        locationCombo.addActionListener(e -> markDraftDirty());
        employmentCombo.addActionListener(e -> markDraftDirty());
        publishImmediatelyCheck.addActionListener(e -> markDraftDirty());
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
        back.addActionListener(e -> tryExitCreateForm());
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
        addLabeledFullWidth(grid, g, row++, fieldLabel("Application deadline"), buildDeadlinePicker());

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

    private JPanel buildDeadlinePicker() {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setOpaque(false);
        p.add(deadlineYearField);
        p.add(deadlineMonthField);
        p.add(deadlineDayField);
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
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 8, 16);
        grid.add(fieldLabel("AI Description Assistant"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);
        JPanel aiRow = new JPanel(new BorderLayout(8, 0));
        aiRow.setOpaque(false);
        aiRow.add(descriptionPromptField, BorderLayout.CENTER);
        JPanel aiActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        aiActions.setOpaque(false);
        aiActions.add(undoAiDescriptionButton);
        aiActions.add(generateAiDescriptionButton);
        aiRow.add(aiActions, BorderLayout.EAST);
        grid.add(aiRow, g);
        row++;

        g.gridx = 1;
        g.gridy = row;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 14, 0);
        grid.add(buildAiGuidePanel(), g);
        row++;

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.NONE;
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
        grid.add(hintLabel("AI output will be written here. You can edit the text before publishing."), g);
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
        skillPoolTagsPanel.setOpaque(false);
        JPanel skillSelectorBox = new JPanel(new BorderLayout(0, 8));
        skillSelectorBox.setBackground(Color.WHITE);
        skillSelectorBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel selectedWrap = new JPanel();
        selectedWrap.setOpaque(true);
        selectedWrap.setBackground(new Color(0xF8FAFC));
        selectedWrap.setLayout(new BoxLayout(selectedWrap, BoxLayout.Y_AXIS));
        selectedWrap.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        skillsMetaLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        skillsMetaLabel.setForeground(MoUiTheme.TEXT_SECONDARY);
        skillsMetaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedWrap.add(skillsMetaLabel);
        selectedWrap.add(Box.createVerticalStrut(6));
        skillsChipPanel.setOpaque(false);
        skillsChipPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedWrap.add(skillsChipPanel);
        skillSelectorBox.add(selectedWrap, BorderLayout.NORTH);

        JLabel poolLabel = new JLabel("Click to add/remove");
        poolLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        poolLabel.setForeground(MoUiTheme.TEXT_SECONDARY);
        poolLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        JScrollPane skillPoolScroll = new JScrollPane(skillPoolTagsPanel);
        skillPoolScroll.setBorder(BorderFactory.createEmptyBorder());
        skillPoolScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        skillPoolScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        skillPoolScroll.setWheelScrollingEnabled(false);
        skillPoolScroll.addMouseWheelListener(e -> forwardWheelToFormScroll(e.getWheelRotation(), e.getScrollAmount()));
        skillPoolScroll.getViewport().setBackground(new Color(0xF8FAFC));
        skillPoolScroll.setPreferredSize(new Dimension(10, 100));
        skillPoolScroll.setMinimumSize(new Dimension(10, 96));
        JPanel poolWrap = new JPanel(new BorderLayout(0, 6));
        poolWrap.setOpaque(false);
        poolWrap.add(poolLabel, BorderLayout.NORTH);
        poolWrap.add(skillPoolScroll, BorderLayout.CENTER);
        skillSelectorBox.add(poolWrap, BorderLayout.CENTER);
        grid.add(skillSelectorBox, g);
        row++;

        g.gridx = 1;
        g.gridy = row;
        g.gridwidth = 2;
        g.insets = new Insets(0, 0, 8, 0);
        grid.add(hintLabel("Blue tags are selected; use mouse wheel to continue page scrolling"), g);
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
        wrap.setBorder(new EmptyBorder(4, 0, 8, 0));
        wrap.add(grid, BorderLayout.NORTH);
        return wrap;
    }

    private JPanel buildAiGuidePanel() {
        JPanel guide = new JPanel();
        guide.setLayout(new BoxLayout(guide, BoxLayout.Y_AXIS));
        guide.setBackground(new Color(0xF8FAFC));
        guide.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel title = new JLabel("Use AI first, then review the Job Description below");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        title.setForeground(new Color(0x1E3A8A));
        guide.add(title);
        guide.add(Box.createVerticalStrut(4));
        guide.add(hintLabel("1) Optionally type guidance in the AI input box."));
        guide.add(hintLabel("2) Click Generate Description to replace the description."));
        guide.add(hintLabel("3) Use Undo AI Replace if you want to revert."));
        return guide;
    }

    private void forwardWheelToFormScroll(int wheelRotation, int scrollAmount) {
        if (formScrollPane == null) {
            return;
        }
        var bar = formScrollPane.getVerticalScrollBar();
        if (bar == null) {
            return;
        }
        int unit = Math.max(8, bar.getUnitIncrement() * Math.max(1, scrollAmount));
        int delta = wheelRotation * unit;
        int next = Math.max(bar.getMinimum(), Math.min(bar.getMaximum(), bar.getValue() + delta));
        bar.setValue(next);
    }

    private void toggleSkill(String s) {
        if (s == null || s.isBlank()) {
            return;
        }
        if (skills.contains(s)) {
            skills.remove(s);
        } else {
            skills.add(s);
        }
        redrawSkillChips();
        markDraftDirty();
    }

    private void redrawSkillPoolTags() {
        skillPoolTagsPanel.removeAll();
        for (String skill : skillPool) {
            final String skillKey = skill;
            boolean selected = skills.contains(skillKey);
            JButton tag = new JButton(skillKey);
            tag.setFocusPainted(false);
            tag.setContentAreaFilled(true);
            tag.setOpaque(true);
            tag.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(selected ? new Color(0x60A5FA) : new Color(0xCBD5E1), 1, true),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            tag.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            if (selected) {
                tag.setBackground(new Color(0xDBEAFE));
                tag.setForeground(new Color(0x1D4ED8));
            } else {
                tag.setBackground(Color.WHITE);
                tag.setForeground(new Color(0x334155));
            }
            tag.addActionListener(e -> toggleSkill(skillKey));
            skillPoolTagsPanel.add(tag);
        }
        if (skillPoolTagsPanel.getComponentCount() == 0) {
            JLabel empty = new JLabel("No skill pool configured");
            empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            empty.setForeground(MoUiTheme.TEXT_SECONDARY);
            skillPoolTagsPanel.add(empty);
        }
        skillPoolTagsPanel.revalidate();
        skillPoolTagsPanel.repaint();
    }

    /**
     * FlowLayout panel that reports wrapped preferred height based on parent width.
     * This keeps tags wrapping naturally and avoids horizontal growth.
     */
    private static final class WrapFlowPanel extends JPanel {
        WrapFlowPanel(int align, int hgap, int vgap) {
            super(new FlowLayout(align, hgap, vgap));
        }

        @Override
        public Dimension getPreferredSize() {
            return layoutSize(false);
        }

        @Override
        public Dimension getMinimumSize() {
            return layoutSize(true);
        }

        private Dimension layoutSize(boolean minimum) {
            LayoutManager layout = getLayout();
            if (!(layout instanceof FlowLayout flow)) {
                return minimum ? super.getMinimumSize() : super.getPreferredSize();
            }
            int targetWidth = getWidth();
            if (targetWidth <= 0 && getParent() != null) {
                targetWidth = getParent().getWidth();
            }
            if (targetWidth <= 0) {
                targetWidth = 760;
            }

            Insets insets = getInsets();
            int maxWidth = Math.max(120, targetWidth - insets.left - insets.right - flow.getHgap() * 2);
            int x = 0;
            int rowHeight = 0;
            int requiredHeight = insets.top + insets.bottom + flow.getVgap() * 2;

            for (Component c : getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = minimum ? c.getMinimumSize() : c.getPreferredSize();
                if (x > 0 && x + flow.getHgap() + d.width > maxWidth) {
                    requiredHeight += rowHeight + flow.getVgap();
                    x = 0;
                    rowHeight = 0;
                }
                if (x > 0) {
                    x += flow.getHgap();
                }
                x += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
            requiredHeight += rowHeight;
            return new Dimension(targetWidth, requiredHeight);
        }
    }

    private void redrawSkillChips() {
        skillsChipPanel.removeAll();
        if (skills.isEmpty()) {
            JLabel empty = new JLabel("No skills selected yet");
            empty.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            empty.setForeground(MoUiTheme.TEXT_SECONDARY);
            skillsChipPanel.add(empty);
        }
        for (String skill : skills) {
            final String skillKey = skill;
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            chip.setOpaque(true);
            chip.setBackground(new Color(0xEAF2FF));
            chip.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0x93C5FD), 1, true),
                    new EmptyBorder(5, 10, 5, 6)
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
                markDraftDirty();
            });
            chip.add(text);
            chip.add(remove);
            skillsChipPanel.add(chip);
        }
        skillsMetaLabel.setText("Selected skills (" + skills.size() + ")");
        skillsChipPanel.revalidate();
        skillsChipPanel.repaint();
        redrawSkillPoolTags();
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
            refreshFormValidationState();
            return;
        }
        submitButton.setText(publishImmediatelyCheck.isSelected() ? "Publish Job" : "Save as Draft");
        refreshFormValidationState();
    }

    private void populateFromJob(Job j) {
        titleField.setText(j.getTitle());
        moduleCodeField.setText(j.getModuleCode());
        moduleNameField.setText(j.getModuleName());
        applyNumberDropdownValue(quotaField, j.getQuota(), true);
        applyNumberDropdownValue(weeklyHoursField, j.getWeeklyHours(), false);
        departmentField.setText(j.getDepartment());
        instructorNameField.setText(j.getInstructorName());
        instructorEmailField.setText(j.getInstructorEmail());
        applyDeadlineValue(j.getDeadline());
        locationCombo.setSelectedItem(j.getLocationMode());
        employmentCombo.setSelectedItem(j.getEmploymentType());
        descriptionArea.setText(j.getDescription());
        aiUndoHistory.clear();
        refreshAiAssistButtons();
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

    private void generateDescriptionWithAi() {
        String currentDescription = descriptionArea.getText().trim();
        if (!currentDescription.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                    host.getShellFrame(),
                    "Replace current job description with AI-generated content?",
                    "AI Assist",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String moduleCode = moduleCodeField.getText().trim();
        String moduleName = moduleNameField.getText().trim();
        String title = titleField.getText().trim();
        Integer resolvedQuota = resolveNumberSelection(quotaField, true);
        Integer resolvedWeeklyHours = resolveNumberSelection(weeklyHoursField, false);
        int quota = resolvedQuota == null ? 0 : resolvedQuota;
        int weeklyHours = resolvedWeeklyHours == null ? 0 : resolvedWeeklyHours;
        String prompt = descriptionPromptField.getText().trim();
        String additional = additionalArea.getText().trim();
        List<String> requiredSkills = new ArrayList<>(skills);

        String previousDescription = descriptionArea.getText();
        setAiGenerationBusy(true);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                JobDescriptionAiService.JobDescriptionInput input = new JobDescriptionAiService.JobDescriptionInput(
                        title,
                        moduleCode,
                        moduleName,
                        departmentField.getText().trim(),
                        instructorNameField.getText().trim(),
                        weeklyHours,
                        quota,
                        String.valueOf(locationCombo.getSelectedItem()),
                        String.valueOf(employmentCombo.getSelectedItem()),
                        requiredSkills,
                        additional,
                        prompt
                );
                return jobDescriptionAiService.generateDescription(input);
            }

            @Override
            protected void done() {
                setAiGenerationBusy(false);
                try {
                    String generated = get();
                    if (generated == null || generated.isBlank()) {
                        JOptionPane.showMessageDialog(host.getShellFrame(),
                                "AI returned empty content. Please try refining your prompt.",
                                "AI Assist",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String generatedText = generated.trim();
                    if (!generatedText.equals(previousDescription)) {
                        aiUndoHistory.push(previousDescription);
                    }
                    descriptionArea.setText(generatedText);
                    descriptionArea.setCaretPosition(0);
                    refreshAiAssistButtons();
                    markDraftDirty();
                    refreshFormValidationState();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    String msg = cause.getMessage() == null ? String.valueOf(cause) : cause.getMessage();
                    if (msg.length() > 420) {
                        msg = msg.substring(0, 420) + "...";
                    }
                    JOptionPane.showMessageDialog(host.getShellFrame(),
                            "Failed to generate description:\n" + msg
                                    + "\n\nCheck OPENAI_API_KEY / OPENAI_BASE_URL / OPENAI_MODEL.",
                            "AI Assist",
                            JOptionPane.ERROR_MESSAGE);
                    refreshAiAssistButtons();
                }
            }
        }.execute();
    }

    private void undoLastAiDescription() {
        if (aiUndoHistory.isEmpty()) {
            JOptionPane.showMessageDialog(
                    host.getShellFrame(),
                    "Nothing to undo yet.",
                    "AI Assist",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        String previous = aiUndoHistory.pop();
        descriptionArea.setText(previous);
        descriptionArea.setCaretPosition(0);
        markDraftDirty();
        refreshAiAssistButtons();
        refreshFormValidationState();
    }

    private void setAiGenerationBusy(boolean busy) {
        generateAiDescriptionButton.setEnabled(!busy);
        undoAiDescriptionButton.setEnabled(!busy && !aiUndoHistory.isEmpty());
        generateAiDescriptionButton.setText(busy ? "Generating..." : "Generate Description");
    }

    private void refreshAiAssistButtons() {
        setAiGenerationBusy(false);
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        formValidationLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        formValidationLabel.setForeground(new Color(0xB45309));
        formValidationLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bar.add(formValidationLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        MoUiTheme.styleOutlineButton(cancel, 8);
        cancel.addActionListener(e -> tryExitCreateForm());
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
        Integer quota = resolveNumberSelection(quotaField, true);
        Integer weeklyHours = resolveNumberSelection(weeklyHoursField, false);

        if (title.isEmpty() || moduleCode.isEmpty() || moduleName.isEmpty() || quota == null || weeklyHours == null) {
            JOptionPane.showMessageDialog(host.getShellFrame(),
                    "Please fill in all required fields (title, module, quota, hours).",
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

        String deadlineStr = resolveDeadlineValue();
        if (deadlineStr == null) {
            return;
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
        clearDraftFile();
        draftDirty = false;
        host.showJobList();
    }

    private void markDraftDirty() {
        if (suspendDraftPersistence || editingJob != null) {
            return;
        }
        draftDirty = true;
    }

    private void saveDraftNow() {
        if (editingJob != null) {
            return;
        }
        FormDraft draft = captureCurrentDraft();
        if (!draft.hasAnyInput()) {
            clearDraftFile();
            draftDirty = false;
            return;
        }
        try {
            Files.createDirectories(CREATE_FORM_DRAFT_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(CREATE_FORM_DRAFT_FILE.toFile(), draft);
            draftDirty = false;
        } catch (IOException ignored) {
        }
    }

    private void tryExitCreateForm() {
        if (editingJob != null) {
            host.showJobList();
            return;
        }
        FormDraft draft = captureCurrentDraft();
        if (!draft.hasAnyInput()) {
            clearDraftFile();
            draftDirty = false;
            host.showJobList();
            return;
        }
        if (!draftDirty) {
            host.showJobList();
            return;
        }
        Object[] options = {"Save Draft", "Don't Save", "Continue Editing"};
        int choice = JOptionPane.showOptionDialog(
                host.getShellFrame(),
                "Do you want to save current content as draft before leaving?",
                "Unsaved Form Content",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 0) {
            saveDraftNow();
            host.showJobList();
        } else if (choice == 1) {
            clearDraftFile();
            draftDirty = false;
            host.showJobList();
        }
    }

    private boolean restoreDraftIfExists() {
        if (!Files.isRegularFile(CREATE_FORM_DRAFT_FILE)) {
            return false;
        }
        try {
            FormDraft draft = objectMapper.readValue(CREATE_FORM_DRAFT_FILE.toFile(), FormDraft.class);
            if (draft == null || !draft.hasAnyInput()) {
                return false;
            }
            applyDraft(draft);
            draftDirty = false;
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private boolean hasSavedDraftFile() {
        return Files.isRegularFile(CREATE_FORM_DRAFT_FILE);
    }

    private void clearDraftFile() {
        try {
            Files.deleteIfExists(CREATE_FORM_DRAFT_FILE);
        } catch (IOException ignored) {
        }
    }

    private FormDraft captureCurrentDraft() {
        FormDraft draft = new FormDraft();
        draft.title = titleField.getText();
        draft.moduleCode = moduleCodeField.getText();
        draft.moduleName = moduleNameField.getText();
        draft.department = departmentField.getText();
        draft.instructorName = instructorNameField.getText();
        draft.instructorEmail = instructorEmailField.getText();
        draft.description = descriptionArea.getText();
        draft.descriptionPrompt = descriptionPromptField.getText();
        draft.additionalRequirements = additionalArea.getText();
        draft.locationMode = selectedComboText(locationCombo);
        draft.employmentType = selectedComboText(employmentCombo);
        draft.quotaSelection = selectedComboText(quotaField);
        draft.weeklySelection = selectedComboText(weeklyHoursField);
        draft.customQuotaValue = customQuotaValue;
        draft.customWeeklyHoursValue = customWeeklyHoursValue;
        draft.deadlineYear = selectedComboText(deadlineYearField);
        draft.deadlineMonth = selectedComboText(deadlineMonthField);
        draft.deadlineDay = selectedComboText(deadlineDayField);
        draft.publishImmediately = publishImmediatelyCheck.isSelected();
        draft.skills = new ArrayList<>(skills);
        return draft;
    }

    private void applyDraft(FormDraft draft) {
        suspendDraftPersistence = true;
        updatingNumberDropdowns = true;
        try {
            titleField.setText(nz(draft.title));
            moduleCodeField.setText(nz(draft.moduleCode));
            moduleNameField.setText(nz(draft.moduleName));
            departmentField.setText(nz(draft.department));
            instructorNameField.setText(nz(draft.instructorName));
            instructorEmailField.setText(nz(draft.instructorEmail));
            descriptionArea.setText(nz(draft.description));
            descriptionPromptField.setText(nz(draft.descriptionPrompt));
            additionalArea.setText(nz(draft.additionalRequirements));

            if (!nz(draft.locationMode).isBlank()) {
                locationCombo.setSelectedItem(draft.locationMode);
            }
            if (!nz(draft.employmentType).isBlank()) {
                employmentCombo.setSelectedItem(draft.employmentType);
            }

            customQuotaValue = draft.customQuotaValue;
            customWeeklyHoursValue = draft.customWeeklyHoursValue;
            resetNumberDropdownOptions(quotaField);
            resetNumberDropdownOptions(weeklyHoursField);
            if (customQuotaValue != null && customQuotaValue > 15) {
                ensureCustomNumericOption(quotaField, customQuotaValue);
            }
            if (customWeeklyHoursValue != null && customWeeklyHoursValue > 15) {
                ensureCustomNumericOption(weeklyHoursField, customWeeklyHoursValue);
            }

            if (!nz(draft.quotaSelection).isBlank()) {
                quotaField.setSelectedItem(draft.quotaSelection);
            } else {
                quotaField.setSelectedIndex(-1);
            }
            if (!nz(draft.weeklySelection).isBlank()) {
                weeklyHoursField.setSelectedItem(draft.weeklySelection);
            } else {
                weeklyHoursField.setSelectedIndex(-1);
            }

            if (!nz(draft.deadlineYear).isBlank()) {
                deadlineYearField.setSelectedItem(draft.deadlineYear);
            } else {
                deadlineYearField.setSelectedIndex(-1);
            }
            if (!nz(draft.deadlineMonth).isBlank()) {
                deadlineMonthField.setSelectedItem(draft.deadlineMonth);
            } else {
                deadlineMonthField.setSelectedIndex(-1);
            }
            if (!nz(draft.deadlineDay).isBlank()) {
                deadlineDayField.setSelectedItem(draft.deadlineDay);
            } else {
                deadlineDayField.setSelectedIndex(-1);
            }

            publishImmediatelyCheck.setSelected(draft.publishImmediately);

            skills.clear();
            if (draft.skills != null) {
                for (String s : draft.skills) {
                    if (s != null && !s.isBlank()) {
                        skills.add(s.trim());
                    }
                }
            }
            redrawSkillChips();
        } finally {
            updatingNumberDropdowns = false;
            suspendDraftPersistence = false;
        }
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }

    private static final class FormDraft {
        public String title;
        public String moduleCode;
        public String moduleName;
        public String department;
        public String instructorName;
        public String instructorEmail;
        public String description;
        public String descriptionPrompt;
        public String additionalRequirements;
        public String locationMode;
        public String employmentType;
        public String quotaSelection;
        public String weeklySelection;
        public Integer customQuotaValue;
        public Integer customWeeklyHoursValue;
        public String deadlineYear;
        public String deadlineMonth;
        public String deadlineDay;
        public boolean publishImmediately = true;
        public List<String> skills = new ArrayList<>();

        public boolean hasAnyInput() {
            return hasText(title)
                    || hasText(moduleCode)
                    || hasText(moduleName)
                    || hasText(description)
                    || hasText(descriptionPrompt)
                    || hasText(additionalRequirements)
                    || hasText(quotaSelection)
                    || hasText(weeklySelection)
                    || hasText(deadlineYear)
                    || hasText(deadlineMonth)
                    || hasText(deadlineDay)
                    || (skills != null && !skills.isEmpty());
        }

        private static boolean hasText(String s) {
            return s != null && !s.isBlank();
        }
    }

    private void onNumberDropdownChanged(JComboBox<String> combo, boolean quota) {
        if (updatingNumberDropdowns) {
            return;
        }
        String selected = selectedComboText(combo);
        if (!CUSTOM_OPTION.equals(selected)) {
            return;
        }
        Integer existing = quota ? customQuotaValue : customWeeklyHoursValue;
        Integer entered = promptCustomNumber(quota ? "Quota" : "Expected Weekly Hours", existing);
        updatingNumberDropdowns = true;
        try {
            if (entered == null) {
                if (existing == null) {
                    combo.setSelectedIndex(-1);
                } else {
                    ensureCustomNumericOption(combo, existing);
                    combo.setSelectedItem(String.valueOf(existing));
                }
                return;
            }
            if (quota) {
                customQuotaValue = entered;
            } else {
                customWeeklyHoursValue = entered;
            }
            ensureCustomNumericOption(combo, entered);
            combo.setSelectedItem(String.valueOf(entered));
        } finally {
            updatingNumberDropdowns = false;
        }
        // Ensure required-field hint updates even if combo action ordering differs on some platforms.
        refreshFormValidationState();
    }

    private void applyNumberDropdownValue(JComboBox<String> combo, int value, boolean quota) {
        if (value <= 15) {
            updatingNumberDropdowns = true;
            try {
                combo.setSelectedItem(String.valueOf(Math.max(1, value)));
            } finally {
                updatingNumberDropdowns = false;
            }
            if (quota) {
                customQuotaValue = null;
            } else {
                customWeeklyHoursValue = null;
            }
            return;
        }
        if (quota) {
            customQuotaValue = value;
        } else {
            customWeeklyHoursValue = value;
        }
        updatingNumberDropdowns = true;
        try {
            ensureCustomNumericOption(combo, value);
            combo.setSelectedItem(String.valueOf(value));
        } finally {
            updatingNumberDropdowns = false;
        }
    }

    private Integer resolveNumberSelection(JComboBox<String> combo, boolean quota) {
        String selected = selectedComboText(combo);
        if (selected.isBlank()) {
            return null;
        }
        if (CUSTOM_OPTION.equals(selected)) {
            Integer custom = quota ? customQuotaValue : customWeeklyHoursValue;
            if (custom != null && custom >= 1) {
                return custom;
            }
            Integer entered = promptCustomNumber(quota ? "Quota" : "Expected Weekly Hours", custom);
            if (entered == null) {
                return null;
            }
            if (quota) {
                customQuotaValue = entered;
            } else {
                customWeeklyHoursValue = entered;
            }
            ensureCustomNumericOption(combo, entered);
            updatingNumberDropdowns = true;
            try {
                combo.setSelectedItem(String.valueOf(entered));
            } finally {
                updatingNumberDropdowns = false;
            }
            return entered;
        }
        try {
            return Integer.parseInt(selected);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer promptCustomNumber(String fieldName, Integer existing) {
        String initial = existing == null ? "16" : String.valueOf(existing);
        while (true) {
            String input = JOptionPane.showInputDialog(
                    host.getShellFrame(),
                    fieldName + " custom value (>= 16):",
                    initial
            );
            if (input == null) {
                return null;
            }
            String s = input.trim();
            if (s.isEmpty()) {
                JOptionPane.showMessageDialog(host.getShellFrame(),
                        fieldName + " cannot be empty.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                continue;
            }
            try {
                int n = Integer.parseInt(s);
                if (n < 16) {
                    JOptionPane.showMessageDialog(host.getShellFrame(),
                            fieldName + " custom value must be 16 or above.",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                return n;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(host.getShellFrame(),
                        fieldName + " must be a valid integer.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void ensureCustomNumericOption(JComboBox<String> combo, int value) {
        if (value <= 15) {
            return;
        }
        String label = String.valueOf(value);
        boolean found = false;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (label.equals(combo.getItemAt(i))) {
                found = true;
                break;
            }
        }
        if (found) {
            return;
        }
        int customIndex = Math.max(0, combo.getItemCount() - 1);
        combo.insertItemAt(label, customIndex);
    }

    private void resetNumberDropdownOptions(JComboBox<String> combo) {
        if (combo == null) {
            return;
        }
        for (int i = combo.getItemCount() - 1; i >= 0; i--) {
            String item = combo.getItemAt(i);
            if (item == null || CUSTOM_OPTION.equals(item)) {
                continue;
            }
            try {
                int n = Integer.parseInt(item);
                if (n > 15) {
                    combo.removeItemAt(i);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static String selectedComboText(JComboBox<String> combo) {
        Object selected = combo == null ? null : combo.getSelectedItem();
        return selected == null ? "" : String.valueOf(selected).trim();
    }

    private void refreshFormValidationState() {
        List<String> missing = new ArrayList<>();
        if (titleField.getText().trim().isEmpty()) {
            missing.add("Job Title");
        }
        if (moduleCodeField.getText().trim().isEmpty()) {
            missing.add("Module Code");
        }
        if (moduleNameField.getText().trim().isEmpty()) {
            missing.add("Module Name");
        }
        if (!hasNumberSelection(quotaField, true)) {
            missing.add("Quota");
        }
        if (!hasNumberSelection(weeklyHoursField, false)) {
            missing.add("Expected Weekly Hours");
        }
        boolean valid = missing.isEmpty();
        submitButton.setEnabled(valid);
        if (valid) {
            formValidationLabel.setText("All required fields completed.");
            formValidationLabel.setForeground(new Color(0x15803D));
        } else {
            formValidationLabel.setText("Missing required: " + String.join(", ", missing));
            formValidationLabel.setForeground(new Color(0xB45309));
        }
    }

    private boolean hasNumberSelection(JComboBox<String> combo, boolean quota) {
        String selected = selectedComboText(combo);
        if (selected.isBlank()) {
            return false;
        }
        if (CUSTOM_OPTION.equals(selected)) {
            Integer custom = quota ? customQuotaValue : customWeeklyHoursValue;
            return custom != null && custom >= 16;
        }
        try {
            return Integer.parseInt(selected) >= 1;
        } catch (NumberFormatException ex) {
            Integer custom = quota ? customQuotaValue : customWeeklyHoursValue;
            return custom != null && custom >= 16;
        }
    }

    private void clearDeadlineSelection() {
        deadlineYearField.setSelectedIndex(-1);
        deadlineMonthField.setSelectedIndex(-1);
        deadlineDayField.setSelectedIndex(-1);
    }

    private void applyDeadlineValue(String deadline) {
        if (deadline == null || deadline.isBlank()) {
            clearDeadlineSelection();
            return;
        }
        try {
            LocalDate d = LocalDate.parse(deadline.trim());
            deadlineYearField.setSelectedItem(String.valueOf(d.getYear()));
            deadlineMonthField.setSelectedItem(String.format("%02d", d.getMonthValue()));
            deadlineDayField.setSelectedItem(String.format("%02d", d.getDayOfMonth()));
        } catch (DateTimeParseException ex) {
            clearDeadlineSelection();
        }
    }

    private String resolveDeadlineValue() {
        String y = selectedComboText(deadlineYearField);
        String m = selectedComboText(deadlineMonthField);
        String d = selectedComboText(deadlineDayField);
        if (y.isBlank() && m.isBlank() && d.isBlank()) {
            return "";
        }
        if (y.isBlank() || m.isBlank() || d.isBlank()) {
            JOptionPane.showMessageDialog(host.getShellFrame(),
                    "Please select Year, Month and Day for deadline (or leave all empty).",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            LocalDate date = LocalDate.of(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d));
            return date.toString();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(host.getShellFrame(),
                    "Selected deadline date is invalid.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

}
