package ui.screens;

import data.ProfileData;
import data.SkillItem;
import ui.AppFrame;
import ui.PortalChrome;
import ui.TaTopNavigationPanel;
import ui.Theme;
import ui.Ui;
import ui.components.PrimaryButton;
import ui.components.SecondaryButton;

import ui.PortalUi;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EditSkillsScreen extends JPanel {
    private final AppFrame app;
    private final TaTopNavigationPanel topNav;
    private final ScrollableViewportPanel sectionsContainer = new ScrollableViewportPanel();
    private final Map<String, SelectedSkill> selectedSkills = new LinkedHashMap<>();
    private static final List<String> LEVELS = Arrays.asList("Beginner", "Intermediate", "Advanced");

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
            Arrays.asList(
                    new SkillSubGroup("English", Arrays.asList("English"))
            )
    );

    public EditSkillsScreen(AppFrame app) {
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
        rebuildFromProfile();
        rebuildSkillSections();
    }

    private JPanel buildBody() {
        JPanel page = PortalChrome.pageSurface();
        page.setLayout(new BorderLayout());
        page.setBorder(new EmptyBorder(14, 36, 20, 36));

        page.add(PortalChrome.header(
                PortalUi.sparkleIcon(PortalUi.PRIMARY_PURPLE, 22),
                "Skills & Competencies",
                "Select your skills and set proficiency levels for each competency",
                "←  Back to Profile", () -> app.showRoute(AppFrame.ROUTE_PROFILE)),
                BorderLayout.NORTH);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(new EmptyBorder(8, 0, 18, 0));
        PortalUi.RoundedSurface card = PortalChrome.card(new Insets(20, 22, 20, 22));
        sectionsContainer.setOpaque(false);
        sectionsContainer.setLayout(new BoxLayout(sectionsContainer, BoxLayout.Y_AXIS));
        sectionsContainer.setBorder(new EmptyBorder(8, 0, 0, 0));
        rebuildFromProfile();
        rebuildSkillSections();

        JScrollPane scrollPane = new JScrollPane(sectionsContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(16, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.75;
        gbc.insets = new Insets(0, 0, 0, 10);
        PrimaryButton save = new PrimaryButton("Save Skills & Proficiency");
        save.addActionListener(e -> {
            persistSelections();
            JOptionPane.showMessageDialog(
                    this,
                    "Skills saved successfully.",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
            app.showRoute(AppFrame.ROUTE_PROFILE);
        });
        bottom.add(save, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(0, 0, 0, 0);
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
        bottom.add(cancel, gbc);
        card.add(bottom, BorderLayout.SOUTH);

        contentWrap.add(card, BorderLayout.CENTER);
        page.add(contentWrap, BorderLayout.CENTER);
        return page;
    }

    private void rebuildFromProfile() {
        selectedSkills.clear();
        ProfileData p = app.profile();
        if (p.skills == null) {
            return;
        }
        for (SkillItem item : p.skills) {
            if (item == null || item.name == null || item.name.trim().isEmpty()) {
                continue;
            }
            String level = normalizeLevel(item.proficiency);
            if (level == null) {
                continue;
            }
            selectedSkills.put(item.name.trim(), new SelectedSkill(item.name.trim(), normalizeCategory(item.category), level));
        }
    }

    private void rebuildSkillSections() {
        sectionsContainer.removeAll();
        sectionsContainer.add(buildGroupPanel(TECHNICAL_SKILLS, 2));
        sectionsContainer.add(Box.createVerticalStrut(22));
        sectionsContainer.add(buildGroupPanel(TOOLS_SKILLS, 2));
        sectionsContainer.add(Box.createVerticalStrut(22));
        sectionsContainer.add(buildGroupPanel(LANGUAGE_SKILLS, 2));
        sectionsContainer.add(Box.createVerticalStrut(22));
        sectionsContainer.add(buildSummaryCard());
        sectionsContainer.add(Box.createVerticalGlue());
        sectionsContainer.revalidate();
        sectionsContainer.repaint();
    }

    private JPanel buildGroupPanel(SkillGroup group, int columns) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComponent icon = createSectionIcon(group.iconLabel);
        icon.setAlignmentY(Component.TOP_ALIGNMENT);
        header.add(icon);
        header.add(Box.createHorizontalStrut(12));
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setAlignmentY(Component.TOP_ALIGNMENT);
        JLabel title = new JLabel(group.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel desc = Ui.muted(group.description);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(desc);
        header.add(titleBlock);
        header.add(Box.createHorizontalGlue());
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(16));

        for (int i = 0; i < group.subGroups.size(); i++) {
            SkillSubGroup sg = group.subGroups.get(i);
            wrapper.add(buildSubGroupSection(sg, columns));
            if (i < group.subGroups.size() - 1) {
                wrapper.add(Box.createVerticalStrut(16));
            }
        }
        return wrapper;
    }

    private JPanel buildSubGroupSection(SkillSubGroup subGroup, int columns) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(subGroup.title);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sub.setForeground(Theme.TEXT);
        sub.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, PortalUi.PRIMARY_PURPLE),
                BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sub);
        section.add(Box.createVerticalStrut(10));

        int total = subGroup.skills.size();
        int rows = Math.max(1, (total + columns - 1) / columns);
        JPanel grid = new JPanel(new GridLayout(rows, columns, 12, 10));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String skillName : subGroup.skills) {
            grid.add(buildSkillCard(skillName, subGroup.title));
        }
        int remaining = rows * columns - total;
        for (int i = 0; i < remaining; i++) {
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

        Ui.RoundedPanel card = new Ui.RoundedPanel(
                12,
                isSelected ? PortalUi.LAVENDER : Theme.SURFACE,
                isSelected ? PortalUi.PRIMARY_PURPLE : Theme.BORDER,
                isSelected ? 2 : 1
        );
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(Ui.empty(10, 12, 10, 12));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel(skillName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(Theme.TEXT);
        topRow.add(name, BorderLayout.WEST);
        if (isSelected) {
            CheckmarkIcon checkmark = new CheckmarkIcon(16);
            topRow.add(checkmark, BorderLayout.EAST);
        }
        card.add(topRow);
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
                rebuildSkillSections();
            });
            levels.add(btn);
        }
        card.add(levels);
        return card;
    }

    private JComponent createSectionIcon(String label) {
        final String text = label == null ? "" : label;
        final boolean drawCheck = "\u2713".equals(text);
        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(PortalUi.LAVENDER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(PortalUi.LIGHT_PURPLE_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                if (drawCheck) {
                    g2.setColor(PortalUi.PRIMARY_PURPLE);
                    g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int w = getWidth();
                    int h = getHeight();
                    int sx = (int) (w * 0.28);
                    int sy = (int) (h * 0.52);
                    int mx = (int) (w * 0.45);
                    int my = (int) (h * 0.68);
                    int ex = (int) (w * 0.74);
                    int ey = (int) (h * 0.34);
                    g2.drawLine(sx, sy, mx, my);
                    g2.drawLine(mx, my, ex, ey);
                } else if (!text.isEmpty()) {
                    g2.setColor(PortalUi.PRIMARY_PURPLE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(text);
                    int th = fm.getAscent();
                    g2.drawString(text, (getWidth() - tw) / 2, (getHeight() + th) / 2 - 3);
                }
                g2.dispose();
            }
        };
        Dimension d = new Dimension(40, 40);
        icon.setPreferredSize(d);
        icon.setMaximumSize(d);
        icon.setMinimumSize(d);
        return icon;
    }

    private JPanel buildSummaryCard() {
        Ui.RoundedPanel summary = new Ui.RoundedPanel(14, Theme.SURFACE, Theme.BORDER, 1);
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setBorder(Ui.empty(16, 16, 16, 16));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComponent icon = createSectionIcon("\u2713");
        icon.setAlignmentY(Component.CENTER_ALIGNMENT);
        header.add(icon);
        header.add(Box.createHorizontalStrut(10));
        JLabel title = new JLabel("Selection Summary");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentY(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createHorizontalGlue());
        summary.add(header);
        summary.add(Box.createVerticalStrut(14));

        JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 0));
        metrics.setOpaque(false);
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);

        int total = selectedSkills.size();
        int prog = countCategory("Programming Languages");
        int hw = countCategory("Hardware & Logic Design");
        int emb = countCategory("Embedded Systems");
        int tools = countCategory("Professional Development & Simulation Tools");
        String breakdown = prog + " Programming  •  " + hw + " Hardware  •  " + emb + " Embedded  •  " + tools + " Tools";

        metrics.add(buildSummaryMetric("TOTAL SKILLS SELECTED", String.valueOf(total), breakdown));
        SelectedSkill english = selectedSkills.get("English");
        metrics.add(buildSummaryMetric("ENGLISH PROFICIENCY", english == null ? "—" : english.proficiency, ""));
        metrics.add(buildSummaryMetric("PROFILE STATUS", total > 0 ? "Complete" : "Incomplete", ""));
        summary.add(metrics);
        return summary;
    }

    private int countCategory(String category) {
        int n = 0;
        for (SelectedSkill s : selectedSkills.values()) {
            if (s.category != null && s.category.equalsIgnoreCase(category)) n++;
        }
        return n;
    }

    private JPanel buildSummaryMetric(String label, String value, String breakdown) {
        Ui.RoundedPanel p = new Ui.RoundedPanel(12, PortalUi.LAVENDER, PortalUi.LIGHT_PURPLE_BORDER, 1);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(Ui.empty(12, 14, 12, 14));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(PortalUi.PRIMARY_PURPLE);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(v);
        if (breakdown != null && !breakdown.isBlank()) {
            JLabel b = new JLabel(breakdown);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            b.setForeground(Theme.MUTED);
            p.add(Box.createVerticalStrut(4));
            p.add(b);
        }
        return p;
    }

    private void persistSelections() {
        ProfileData p = app.profile();
        List<SkillItem> items = new ArrayList<>();
        for (SelectedSkill s : selectedSkills.values()) {
            items.add(new SkillItem(s.name, s.category, s.proficiency));
        }
        p.skills = items;
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

    private static final class CheckmarkIcon extends JComponent {
        CheckmarkIcon(int size) {
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PortalUi.PRIMARY_PURPLE);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int w = getWidth();
            int h = getHeight();
            int sx = (int) (w * 0.18);
            int sy = (int) (h * 0.55);
            int mx = (int) (w * 0.42);
            int my = (int) (h * 0.78);
            int ex = (int) (w * 0.85);
            int ey = (int) (h * 0.25);
            g2.drawLine(sx, sy, mx, my);
            g2.drawLine(mx, my, ex, ey);
            g2.dispose();
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
            setForeground(filled ? Color.WHITE : PortalUi.PRIMARY_PURPLE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(0, 4, 0, 4));
            setPreferredSize(new Dimension(60, 28));
            setMinimumSize(new Dimension(40, 28));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = h;
            if (filled) {
                g2.setPaint(new java.awt.GradientPaint(0, 0,
                        PortalUi.PRIMARY_PURPLE, w, h, PortalUi.DEEP_PURPLE));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            } else {
                g2.setColor(PortalUi.LAVENDER);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(PortalUi.LIGHT_PURPLE_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
