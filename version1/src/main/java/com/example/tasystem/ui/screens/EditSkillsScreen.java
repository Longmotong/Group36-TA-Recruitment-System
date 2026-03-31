package com.example.tasystem.ui.screens;

import com.example.tasystem.data.ProfileData;
import com.example.tasystem.data.SkillItem;
import com.example.tasystem.ui.AppFrame;
import com.example.tasystem.ui.Theme;
import com.example.tasystem.ui.Ui;
import com.example.tasystem.ui.components.Chip;
import com.example.tasystem.ui.components.PrimaryButton;
import com.example.tasystem.ui.components.SecondaryButton;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public final class EditSkillsScreen extends JPanel {
    private final AppFrame app;
    private final JTextField skillName = Ui.textField("e.g., React, Data Analysis");
    private final JComboBox<String> category = new JComboBox<>(new String[]{"Programming", "Teaching / Tutoring", "Communication", "Other Skills"});
    private final JComboBox<String> proficiency = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"});
    private final JPanel programmingWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel teachingWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel communicationWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel otherWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

    public EditSkillsScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(buildNavBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    public void refresh() {
        rebuildSkills();
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(Ui.empty(10, 18, 10, 18));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        left.setOpaque(false);
        left.add(new JLabel("TA System"));
        left.add(navLink("Home", () -> app.showRoute(AppFrame.ROUTE_DASHBOARD)));
        left.add(navLink("Profile Module", () -> app.showRoute(AppFrame.ROUTE_PROFILE)));
        left.add(navLink("Job Application Module", () -> {}));
        nav.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);
        right.add(navLink("Logout", () -> {}));
        nav.add(right, BorderLayout.EAST);
        return nav;
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
        head.add(Ui.h1("Skills Information"));
        head.add(Box.createVerticalStrut(6));
        head.add(Ui.muted("Manage your skills and competencies"));
        wrap.add(head, BorderLayout.NORTH);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(Ui.empty(0, 170, 18, 170));
        Ui.RoundedPanel card = new Ui.RoundedPanel(16, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(1, 4, 10, 0));
        form.setOpaque(false);
        Ui.RoundedPanel categoryWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        categoryWrap.setLayout(new BorderLayout());
        categoryWrap.add(category, BorderLayout.CENTER);
        Ui.RoundedPanel profWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        profWrap.setLayout(new BorderLayout());
        profWrap.add(proficiency, BorderLayout.CENTER);
        PrimaryButton add = new PrimaryButton("+");
        add.setPreferredSize(new java.awt.Dimension(52, 44));
        add.addActionListener(e -> {
            String name = skillName.getText().trim();
            if (name.isEmpty()) return;
            ProfileData p = app.profile();
            p.addSkill(name, String.valueOf(category.getSelectedItem()), String.valueOf(proficiency.getSelectedItem()));
            app.updateProfile(p);
            skillName.setText("");
            rebuildSkills();
        });

        form.add(new Ui.RoundedTextField(skillName));
        form.add(categoryWrap);
        form.add(profWrap);
        form.add(add);

        programmingWrap.setOpaque(false);
        teachingWrap.setOpaque(false);
        communicationWrap.setOpaque(false);
        otherWrap.setOpaque(false);
        JPanel groups = new JPanel();
        groups.setOpaque(false);
        groups.setLayout(new BoxLayout(groups, BoxLayout.Y_AXIS));
        groups.setBorder(Ui.empty(12, 0, 0, 0));
        groups.add(group("Programming Skills", programmingWrap));
        groups.add(group("Teaching / Tutoring Skills", teachingWrap));
        groups.add(group("Communication Skills", communicationWrap));
        groups.add(group("Other Skills", otherWrap));

        card.add(form, BorderLayout.NORTH);
        card.add(new JScrollPane(groups, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        PrimaryButton save = new PrimaryButton("Save Skills");
        save.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
        bottom.add(save);
        bottom.add(cancel);
        card.add(bottom, BorderLayout.SOUTH);

        contentWrap.add(card, BorderLayout.CENTER);
        wrap.add(contentWrap, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel group(String title, JPanel wrap) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Ui.h3(title));
        p.add(wrap);
        p.add(Box.createVerticalStrut(6));
        return p;
    }

    private void rebuildSkills() {
        programmingWrap.removeAll();
        teachingWrap.removeAll();
        communicationWrap.removeAll();
        otherWrap.removeAll();
        ProfileData p = app.profile();
        if (p.skills != null) {
            for (int i = 0; i < p.skills.size(); i++) {
                SkillItem s = p.skills.get(i);
                int idx = i;
                Chip chip = chipForCategoryClosable(s.category, s.name + " (" + s.proficiency + ")");
                chip.addActionListener(e -> {
                    ProfileData next = app.profile();
                    if (next.skills != null && idx >= 0 && idx < next.skills.size()) {
                        next.skills.remove(idx);
                        app.updateProfile(next);
                        rebuildSkills();
                    }
                });
                String cat = s.category == null ? "" : s.category.toLowerCase();
                if (cat.contains("program")) programmingWrap.add(chip);
                else if (cat.contains("teach")) teachingWrap.add(chip);
                else if (cat.contains("comm")) communicationWrap.add(chip);
                else otherWrap.add(chip);
            }
        }
        if (programmingWrap.getComponentCount() == 0) programmingWrap.add(Ui.muted("No programming skills"));
        if (teachingWrap.getComponentCount() == 0) teachingWrap.add(Ui.muted("No teaching skills"));
        if (communicationWrap.getComponentCount() == 0) communicationWrap.add(Ui.muted("No communication skills"));
        if (otherWrap.getComponentCount() == 0) otherWrap.add(Ui.muted("No other skills added yet."));
        revalidate();
        repaint();
    }

    private Chip chipForCategoryClosable(String category, String text) {
        if (category == null) return Chip.blue(text, true);
        String c = category.toLowerCase();
        if (c.contains("program")) return Chip.blue(text, true);
        if (c.contains("teach")) return Chip.green(text, true);
        if (c.contains("comm")) return Chip.purple(text, true);
        return Chip.blue(text, true);
    }
}

