package ui.screens;

import data.ProfileData;
import data.SkillItem;
import ui.AppFrame;
import ui.PortalChrome;
import ui.TaTopNavigationPanel;
import ui.Theme;
import ui.Ui;
import ui.components.Chip;

import ui.PortalUi;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

    private final JPanel programmingLanguagesWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel hardwareLogicWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel embeddedSystemsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel toolsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel languageWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

    private final TaTopNavigationPanel topNav;
    private static final Set<String> PROGRAMMING_LANGUAGE_NAMES = new HashSet<>(Arrays.asList(
            "Java", "Python", "C/C++", "SQL", "Algorithms & Data Structures", "Object-Oriented Programming (OOP)"
    ));
    private static final Set<String> HARDWARE_LOGIC_NAMES = new HashSet<>(Arrays.asList(
            "VHDL", "Verilog", "Digital Logic Design", "FPGA Development & Debugging"
    ));
    private static final Set<String> EMBEDDED_SYSTEM_NAMES = new HashSet<>(Arrays.asList(
            "STM32 Development", "FreeRTOS", "Embedded C", "Hardware Driver Development"
    ));
    private static final Set<String> TOOLS_SKILL_NAMES = new HashSet<>(Arrays.asList(
            "Quartus Prime", "Keil5", "STM32CubeIDE", "STM32CubeMX", "CST Studio Suite", "Matlab / Simulink", "Cisco Packet Tracer"
    ));
    private static final Set<String> LANGUAGE_SKILL_NAMES = new HashSet<>(Arrays.asList("English"));

    public ProfileScreen(AppFrame app) {
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

    private JPanel buildBody() {
        JPanel page = PortalChrome.pageSurface();
        page.setLayout(new BorderLayout());
        page.setBorder(new EmptyBorder(14, 36, 20, 36));

        JPanel head = PortalChrome.header(
                PortalUi.userIcon(PortalUi.PRIMARY_PURPLE, 22),
                "My Profile",
                "View your complete personal information, skills, and CV status",
                "←  Back to Home", () -> app.showRoute(AppFrame.ROUTE_DASHBOARD));
        page.add(head, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(8, 0, 10, 0));
        content.add(buildPersonalCard());
        content.add(Box.createVerticalStrut(16));
        content.add(buildSkillsCard());
        content.add(Box.createVerticalStrut(16));
        content.add(buildCvCard());
        content.add(Box.createVerticalGlue());

        page.add(PortalChrome.verticalScroll(content), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildPersonalCard() {
        PortalUi.RoundedSurface card = PortalChrome.card(new Insets(20, 22, 20, 22));
        card.add(cardHeader("Personal Information",
                PortalUi.userIcon(PortalUi.PRIMARY_PURPLE, 18),
                "Edit Profile",
                () -> app.showRoute(AppFrame.ROUTE_EDIT_PROFILE)), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 28, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(14, 0, 0, 0));
        grid.add(column(kv("Full Name", nameValue), kv("Program / Major", majorValue), kv("Email", emailValue)));
        grid.add(column(kv("Student ID", studentIdValue), kv("Year", yearValue), kv("Phone Number", phoneValue)));
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSkillsCard() {
        PortalUi.RoundedSurface card = PortalChrome.card(new Insets(20, 22, 20, 22));
        card.add(cardHeader("Skills Information",
                PortalUi.sparkleIcon(PortalUi.PRIMARY_PURPLE, 18),
                "Edit Skills",
                () -> app.showRoute(AppFrame.ROUTE_EDIT_SKILLS)), BorderLayout.NORTH);

        programmingLanguagesWrap.setOpaque(false);
        hardwareLogicWrap.setOpaque(false);
        embeddedSystemsWrap.setOpaque(false);
        toolsWrap.setOpaque(false);
        languageWrap.setOpaque(false);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(14, 0, 0, 0));
        JPanel technicalContainer = new JPanel();
        technicalContainer.setOpaque(false);
        technicalContainer.setLayout(new BoxLayout(technicalContainer, BoxLayout.Y_AXIS));
        technicalContainer.add(subGroup("Programming Languages", programmingLanguagesWrap));
        technicalContainer.add(subGroup("Hardware & Logic Design", hardwareLogicWrap));
        technicalContainer.add(subGroup("Embedded Systems", embeddedSystemsWrap));
        body.add(group("Technical Skills", technicalContainer));
        body.add(group("Software & Engineering Tools", toolsWrap));
        body.add(group("Language Proficiency", languageWrap));
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCvCard() {
        PortalUi.RoundedSurface card = PortalChrome.card(new Insets(20, 22, 20, 22));
        card.add(cardHeader("CV Information",
                PortalUi.fileTextIcon(PortalUi.PRIMARY_PURPLE, 18),
                "Manage CV",
                () -> app.showRoute(AppFrame.ROUTE_MANAGE_CV)), BorderLayout.NORTH);

        PortalUi.RoundedSurface line = new PortalUi.RoundedSurface(
                14, PortalUi.LAVENDER, PortalUi.LIGHT_PURPLE_BORDER, 1f, false, new BorderLayout());
        line.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        cvFileValue.setFont(Theme.BODY_BOLD);
        cvFileValue.setForeground(PortalUi.DARK_TEXT);
        inner.add(cvFileValue);
        inner.add(Box.createVerticalStrut(6));
        inner.add(cvMetaValue);
        line.add(inner, BorderLayout.WEST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 0, 0, 0));
        wrap.add(line, BorderLayout.CENTER);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private JPanel cardHeader(String title, javax.swing.Icon glyph, String btnText, Runnable action) {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(PortalChrome.iconBadge(glyph, 36));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(PortalUi.DARK_TEXT);
        left.add(l);
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(PortalChrome.compactOutline(btnText, action));
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel kv(String k, JLabel v) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel key = Ui.muted(k);
        v.setFont(Theme.BODY_BOLD);
        v.setForeground(PortalUi.DARK_TEXT);
        p.add(key);
        p.add(Box.createVerticalStrut(3));
        p.add(v);
        return p;
    }

    private JPanel group(String title, JPanel wrap) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(PortalUi.DARK_TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(wrap);
        p.add(Box.createVerticalStrut(12));
        return p;
    }

    private JPanel subGroup(String title, JPanel wrap) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(PortalUi.MUTED_TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(wrap);
        p.add(Box.createVerticalStrut(8));
        return p;
    }

    private JPanel column(JPanel... items) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (JPanel item : items) {
            p.add(item);
            p.add(Box.createVerticalStrut(10));
        }
        return p;
    }

    private void fillSkills(ProfileData p) {
        programmingLanguagesWrap.removeAll();
        hardwareLogicWrap.removeAll();
        embeddedSystemsWrap.removeAll();
        toolsWrap.removeAll();
        languageWrap.removeAll();
        if (p.skills != null) {
            for (SkillItem s : p.skills) {
                String cat = s.category == null ? "" : s.category.trim();
                Chip chip = chipForCategory(cat, skillLabel(s));
                chip.setEnabled(false);
                if (isLanguageSkill(s.name, cat)) {
                    languageWrap.add(chip);
                } else if (isToolsSkill(s.name, cat)) {
                    toolsWrap.add(chip);
                } else if (isProgrammingLanguage(s.name, cat)) {
                    programmingLanguagesWrap.add(chip);
                } else if (isHardwareLogicSkill(s.name, cat)) {
                    hardwareLogicWrap.add(chip);
                } else if (isEmbeddedSystemSkill(s.name, cat)) {
                    embeddedSystemsWrap.add(chip);
                } else {
                    programmingLanguagesWrap.add(chip);
                }
            }
        }
        if (programmingLanguagesWrap.getComponentCount() == 0) programmingLanguagesWrap.add(Ui.muted("—"));
        if (hardwareLogicWrap.getComponentCount() == 0) hardwareLogicWrap.add(Ui.muted("—"));
        if (embeddedSystemsWrap.getComponentCount() == 0) embeddedSystemsWrap.add(Ui.muted("—"));
        if (toolsWrap.getComponentCount() == 0) toolsWrap.add(Ui.muted("—"));
        if (languageWrap.getComponentCount() == 0) languageWrap.add(Ui.muted("—"));
    }

    private Chip chipForCategory(String category, String text) {
        if (isLanguageSkill(text, category)) return Chip.purple(text, false);
        if (isToolsSkill(text, category)) return Chip.green(text, false);
        if (isProgrammingLanguage(text, category) || isHardwareLogicSkill(text, category) || isEmbeddedSystemSkill(text, category)) {
            return Chip.blue(text, false);
        }
        return Chip.purple(text, false);
    }

    private String skillLabel(SkillItem skill) {
        String name = skill != null && skill.name != null ? skill.name : "";
        String level = skill != null && skill.proficiency != null && !skill.proficiency.isBlank()
                ? skill.proficiency
                : "Unknown";
        return name + " (" + level + ")";
    }

    private boolean isProgrammingLanguage(String skillName, String category) {
        if (containsSkillIgnoreCase(PROGRAMMING_LANGUAGE_NAMES, skillName)) return true;
        String cat = category == null ? "" : category.toLowerCase(Locale.ROOT);
        return cat.contains("programming languages");
    }

    private boolean isHardwareLogicSkill(String skillName, String category) {
        if (containsSkillIgnoreCase(HARDWARE_LOGIC_NAMES, skillName)) return true;
        String cat = category == null ? "" : category.toLowerCase(Locale.ROOT);
        return cat.contains("hardware & logic design");
    }

    private boolean isEmbeddedSystemSkill(String skillName, String category) {
        if (containsSkillIgnoreCase(EMBEDDED_SYSTEM_NAMES, skillName)) return true;
        String cat = category == null ? "" : category.toLowerCase(Locale.ROOT);
        return cat.contains("embedded systems");
    }

    private boolean isToolsSkill(String skillName, String category) {
        if (containsSkillIgnoreCase(TOOLS_SKILL_NAMES, skillName)) return true;
        return isToolsCategory(category);
    }

    private boolean isLanguageSkill(String skillName, String category) {
        if (containsSkillIgnoreCase(LANGUAGE_SKILL_NAMES, skillName)) return true;
        return isLanguageCategory(category);
    }

    private boolean containsSkillIgnoreCase(Set<String> pool, String skillName) {
        if (skillName == null) return false;
        String target = skillName.trim().toLowerCase(Locale.ROOT);
        for (String s : pool) {
            if (s != null && s.trim().toLowerCase(Locale.ROOT).equals(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean isToolsCategory(String category) {
        String cat = category == null ? "" : category.toLowerCase(Locale.ROOT);
        return cat.contains("professional development & simulation tools");
    }

    private boolean isLanguageCategory(String category) {
        String cat = category == null ? "" : category.toLowerCase(Locale.ROOT);
        return cat.contains("english") || cat.contains("language proficiency");
    }

    private String nonEmpty(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
