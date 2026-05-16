package ui.screens;

import data.ProfileData;
import ui.AppFrame;
import ui.PortalChrome;
import ui.TaTopNavigationPanel;
import ui.Theme;
import ui.Ui;
import ui.components.PrimaryButton;
import ui.components.SecondaryButton;

import ui.PortalUi;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class EditProfileScreen extends JPanel {
    private final AppFrame app;
    private final JTextField fullName = Ui.textField("Enter your full name");
    private final JTextField studentId = Ui.textField("e.g., 20230001");
    private final JComboBox<String> year = new JComboBox<>(new String[]{"Select year", "1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate"});
    private final JTextField program = Ui.textField("e.g., Computer Science");
    private final JTextField email = Ui.textField("your.email@university.edu");
    private final JTextField phone = Ui.textField("Phone number (11 digits)");
    private final JTextField address = Ui.textField("123 Main St, City, State, ZIP");
    private final JTextArea bio = new JTextArea(5, 30);

    private final TaTopNavigationPanel topNav;

    public EditProfileScreen(AppFrame app) {
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
        fullName.setText(nonNull(p.fullName));
        studentId.setText(nonNull(p.studentId));
        year.setSelectedItem((p.year == null || p.year.isBlank()) ? "3rd Year" : p.year);
        program.setText(nonNull(p.programMajor));
        email.setText(nonNull(p.email));
        phone.setText(nonNull(p.phoneNumber));
        address.setText(nonNull(p.address));
        bio.setText(nonNull(p.shortBio));
    }

    private String nonNull(String s) {
        return s != null ? s : "";
    }

    private JPanel buildBody() {
        JPanel page = PortalChrome.pageSurface();
        page.setLayout(new BorderLayout());
        page.setBorder(new EmptyBorder(14, 36, 20, 36));

        page.add(PortalChrome.header(
                PortalUi.userIcon(PortalUi.PRIMARY_PURPLE, 22),
                "Edit Profile",
                "Update your personal information",
                "←  Back to Profile", () -> app.showRoute(AppFrame.ROUTE_PROFILE)),
                BorderLayout.NORTH);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(new EmptyBorder(8, 100, 18, 100));

        PortalUi.RoundedSurface card = PortalChrome.card(new Insets(18, 18, 18, 18));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(labeled("Full Name *", new Ui.RoundedTextField(fullName)), c);
        c.gridy++; c.gridwidth = 1;
        form.add(labeled("Student ID *", new Ui.RoundedTextField(studentId)), c);
        c.gridx = 1;
        Ui.RoundedPanel yearWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        yearWrap.setLayout(new BorderLayout());
        yearWrap.add(year, BorderLayout.CENTER);
        yearWrap.setPreferredSize(new Dimension(240, 40));
        form.add(labeled("Year *", yearWrap), c);
        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        form.add(labeled("Program / Major *", new Ui.RoundedTextField(program)), c);
        c.gridy++; c.gridwidth = 1;
        form.add(labeled("Email *", new Ui.RoundedTextField(email)), c);
        c.gridx = 1;
        form.add(labeled("Phone Number *", new Ui.RoundedTextField(phone)), c);
        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        form.add(labeled("Address (Optional)", new Ui.RoundedTextField(address)), c);

        bio.setLineWrap(true);
        bio.setWrapStyleWord(true);
        bio.setFont(Theme.BODY);
        bio.setBorder(new EmptyBorder(10, 12, 10, 12));
        bio.setOpaque(false);
        Ui.RoundedPanel bioWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        bioWrap.setLayout(new BorderLayout());
        bioWrap.setPreferredSize(new Dimension(240, 110));
        JScrollPane bioScroll = new JScrollPane(bio);
        bioScroll.setBorder(null);
        bioScroll.getViewport().setOpaque(false);
        bioScroll.setOpaque(false);
        bioWrap.add(bioScroll, BorderLayout.CENTER);
        c.gridy++;
        form.add(labeled("Short Bio (Optional)", bioWrap), c);

        // 顶部对齐，让最后一行不会因 weighty=0 被推到中间
        JPanel formHolder = new JPanel(new BorderLayout());
        formHolder.setOpaque(false);
        formHolder.add(form, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(formHolder,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(14, 0, 0, 0));
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
        PrimaryButton save = new PrimaryButton("Save Changes");
        save.addActionListener(e -> {
            ProfileData next = app.profile();
            next.fullName = fullName.getText().trim();
            next.studentId = studentId.getText().trim();
            next.year = String.valueOf(year.getSelectedItem());
            next.programMajor = program.getText().trim();
            next.email = email.getText().trim();
            next.phoneNumber = phone.getText().trim();
            next.address = address.getText().trim();
            next.shortBio = bio.getText().trim();
            app.updateProfile(next);
            app.showRoute(AppFrame.ROUTE_PROFILE);
        });
        bottom.add(cancel);
        bottom.add(save);
        card.add(bottom, BorderLayout.SOUTH);

        contentWrap.add(card, BorderLayout.CENTER);
        page.add(contentWrap, BorderLayout.CENTER);
        return page;
    }

    private JPanel labeled(String label, Component field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        javax.swing.JLabel lbl = Ui.body(label);
        lbl.setFont(Theme.BODY_BOLD);
        lbl.setForeground(PortalUi.DARK_TEXT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }
}
