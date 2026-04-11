package profile_module.ui.screens;

import profile_module.data.ProfileData;
import profile_module.data.SkillItem;
import profile_module.ui.AppFrame;
import profile_module.ui.Theme;
import profile_module.ui.Ui;
import profile_module.ui.components.Chip;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public final class ProfileScreen extends JPanel {
    private final AppFrame app;

    private final JLabel nameValue = Ui.body("—");
    private final JLabel majorValue = Ui.body("—");
    private final JLabel emailValue = Ui.body("—");
    private final JLabel studentIdValue = Ui.body("—");
    private final JLabel yearValue = Ui.body("—");
    private final JLabel phoneValue = Ui.body("—");
    private final JLabel cvFileValue = Ui.body("—");
    private final JLabel cvMetaValue = Ui.muted("—");

    private final JPanel programmingWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel teachingWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel communicationWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel otherWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

    public ProfileScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(buildNavBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    public void refresh() {
        ProfileData p = app.profile();
        nameValue.setText(nonEmpty(p.fullName));
        majorValue.setText(nonEmpty(p.programMajor));
        emailValue.setText(nonEmpty(p.email));
        studentIdValue.setText(nonEmpty(p.studentId));
        yearValue.setText(nonEmpty(p.year));
        phoneValue.setText(nonEmpty(p.phoneNumber));

        fillSkills(p);

        if (p.cv != null && p.cv.fileName != null && !p.cv.fileName.isBlank()) {
            cvFileValue.setText(p.cv.fileName);
            cvMetaValue.setText("Last Updated: " + nonEmpty(p.cv.lastUpdated));
        } else {
            cvFileValue.setText("No CV uploaded");
            cvMetaValue.setText("—");
        }
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(Ui.empty(10, 18, 10, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        left.setOpaque(false);
        JLabel brand = new JLabel("TA System");
        brand.setFont(Theme.BODY_BOLD.deriveFont(14f));
        left.add(brand);
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
        wrap.setBorder(Ui.empty(14, 18, 20, 18));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(navLink("← Back to Home", () -> app.showRoute(AppFrame.ROUTE_DASHBOARD)));
        head.add(Box.createVerticalStrut(6));
        head.add(Ui.h1("My Profile"));
        head.add(Box.createVerticalStrut(6));
        head.add(Ui.muted("View your complete personal information, skills, and CV status"));
        wrap.add(head, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(Ui.empty(14, 100, 10, 100));
        content.add(buildPersonalCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildSkillsCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildCvCard());
        content.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildPersonalCard() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(14, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));
        card.add(cardHeader("Personal Information", "Edit Profile", () -> app.showRoute(AppFrame.ROUTE_EDIT_PROFILE)), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 28, 0));
        grid.setOpaque(false);
        grid.setBorder(Ui.empty(12, 0, 0, 0));
        JPanel left = column(kv("Full Name", nameValue), kv("Program / Major", majorValue), kv("Email", emailValue));
        JPanel right = column(kv("Student ID", studentIdValue), kv("Year", yearValue), kv("Phone Number", phoneValue));
        grid.add(left);
        grid.add(right);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSkillsCard() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(14, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));
        card.add(cardHeader("Skills Information", "Edit Skills", () -> app.showRoute(AppFrame.ROUTE_EDIT_SKILLS)), BorderLayout.NORTH);

        programmingWrap.setOpaque(false);
        teachingWrap.setOpaque(false);
        communicationWrap.setOpaque(false);
        otherWrap.setOpaque(false);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(Ui.empty(12, 0, 0, 0));
        body.add(group("Programming Skills", programmingWrap));
        body.add(group("Teaching Skills", teachingWrap));
        body.add(group("Communication Skills", communicationWrap));
        body.add(group("Other Skills", otherWrap));
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCvCard() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(14, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));
        card.add(cardHeader("CV Information", "Manage CV", () -> app.showRoute(AppFrame.ROUTE_MANAGE_CV)), BorderLayout.NORTH);

        Ui.RoundedPanel line = new Ui.RoundedPanel(12, new java.awt.Color(0xF8, 0xFA, 0xFC), Theme.BORDER, 1);
        line.setLayout(new BoxLayout(line, BoxLayout.Y_AXIS));
        line.setBorder(Ui.empty(12, 12, 12, 12));
        line.add(cvFileValue);
        line.add(Box.createVerticalStrut(6));
        line.add(cvMetaValue);
        line.setPreferredSize(new java.awt.Dimension(10, 68));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(Ui.empty(12, 0, 0, 0));
        wrap.add(line, BorderLayout.CENTER);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private JPanel cardHeader(String title, String btnText, Runnable action) {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Ui.h2(title), BorderLayout.WEST);

        JButton btn = new JButton(btnText);
        btn.setFont(Theme.BODY_BOLD);
        btn.setForeground(Theme.TEXT);
        btn.setFocusPainted(false);
        btn.setBackground(Theme.SURFACE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                Ui.empty(7, 12, 7, 12)
        ));
        btn.addActionListener(e -> action.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btn);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel kv(String k, JLabel v) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel key = Ui.muted(k);
        v.setFont(Theme.BODY_BOLD);
        p.add(key);
        p.add(Box.createVerticalStrut(3));
        p.add(v);
        return p;
    }

    private JPanel group(String title, JPanel wrap) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = Ui.muted(title);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(wrap);
        p.add(Box.createVerticalStrut(6));
        return p;
    }

    private JPanel column(JPanel... items) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (JPanel item : items) {
            p.add(item);
            p.add(Box.createVerticalStrut(8));
        }
        return p;
    }

    private void fillSkills(ProfileData p) {
        programmingWrap.removeAll();
        teachingWrap.removeAll();
        communicationWrap.removeAll();
        otherWrap.removeAll();
        if (p.skills != null) {
            for (SkillItem s : p.skills) {
                String cat = s.category == null ? "" : s.category.toLowerCase();
                Chip chip = chipForCategory(cat, s.name);
                chip.setEnabled(false);
                if (cat.contains("program")) programmingWrap.add(chip);
                else if (cat.contains("teach")) teachingWrap.add(chip);
                else if (cat.contains("comm")) communicationWrap.add(chip);
                else otherWrap.add(chip);
            }
        }
        if (programmingWrap.getComponentCount() == 0) programmingWrap.add(Ui.muted("—"));
        if (teachingWrap.getComponentCount() == 0) teachingWrap.add(Ui.muted("—"));
        if (communicationWrap.getComponentCount() == 0) communicationWrap.add(Ui.muted("—"));
        if (otherWrap.getComponentCount() == 0) otherWrap.add(Ui.muted("No other skills added yet."));
    }

    private Chip chipForCategory(String category, String text) {
        if (category.contains("program")) return Chip.blue(text, false);
        if (category.contains("teach")) return Chip.green(text, false);
        if (category.contains("comm")) return Chip.purple(text, false);
        return Chip.blue(text, false);
    }

    private String nonEmpty(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}

