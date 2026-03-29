package com.example.tasystem.ui.screens;

import com.example.tasystem.data.ProfileData;
import com.example.tasystem.integration.TaPortalHost;
import com.example.tasystem.ui.Theme;
import com.example.tasystem.ui.Ui;
import com.example.tasystem.ui.components.PrimaryButton;
import com.example.tasystem.ui.components.SecondaryButton;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class EditProfileScreen extends JPanel {
    private final TaPortalHost host;
    private final JTextField fullName = Ui.textField("");
    private final JTextField studentId = Ui.textField("");
    private final JComboBox<String> year = new JComboBox<>(new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate"});
    private final JTextField program = Ui.textField("");
    private final JTextField email = Ui.textField("");
    private final JTextField phone = Ui.textField("");
    private final JTextField address = Ui.textField("123 Main St, City, State, ZIP");
    private final JTextArea bio = new JTextArea(5, 30);

    public EditProfileScreen(TaPortalHost host) {
        this.host = host;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(buildNavBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    public void refresh() {
        ProfileData p = host.profile();
        fullName.setText(p.fullName);
        studentId.setText(p.studentId);
        year.setSelectedItem((p.year == null || p.year.isBlank()) ? "3rd Year" : p.year);
        program.setText(p.programMajor);
        email.setText(p.email);
        phone.setText(p.phoneNumber);
        address.setText(p.address);
        bio.setText(p.shortBio);
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(Ui.empty(10, 18, 10, 18));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        left.setOpaque(false);
        left.add(new JLabel("TA System"));
        left.add(navLink("Home", host::showDashboard));
        left.add(navLink("Profile Module", () -> host.showRoute(TaPortalHost.ROUTE_PROFILE)));
        left.add(navLink("Job Application Module", host::showJobsModule));
        nav.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);
        right.add(navLink("Logout", host::logout));
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
        head.add(navLink("← Back to Profile", () -> host.showRoute(TaPortalHost.ROUTE_PROFILE)));
        head.add(Box.createVerticalStrut(8));
        head.add(Ui.h1("Edit Profile"));
        head.add(Box.createVerticalStrut(6));
        head.add(Ui.muted("Update your personal information"));
        wrap.add(head, BorderLayout.NORTH);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(Ui.empty(0, 220, 18, 220));
        JPanel card = new Ui.RoundedPanel(16, Theme.SURFACE, Theme.BORDER, 1);
        card.setLayout(new BorderLayout());
        card.setBorder(Ui.empty(16, 16, 16, 16));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
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
        yearWrap.setPreferredSize(new java.awt.Dimension(240, 40));
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
        bio.setBorder(Ui.empty(10, 12, 10, 12));
        Ui.RoundedPanel bioWrap = new Ui.RoundedPanel(12, Theme.SURFACE, Theme.BORDER, 1);
        bioWrap.setLayout(new BorderLayout());
        bioWrap.add(new JScrollPane(bio), BorderLayout.CENTER);
        c.gridy++;
        form.add(labeled("Short Bio (Optional)", bioWrap), c);
        card.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        PrimaryButton save = new PrimaryButton("Save Changes");
        save.addActionListener(e -> {
            ProfileData next = host.profile();
            next.fullName = fullName.getText().trim();
            next.studentId = studentId.getText().trim();
            next.year = String.valueOf(year.getSelectedItem());
            next.programMajor = program.getText().trim();
            next.email = email.getText().trim();
            next.phoneNumber = phone.getText().trim();
            next.address = address.getText().trim();
            next.shortBio = bio.getText().trim();
            host.updateProfile(next);
            host.showRoute(TaPortalHost.ROUTE_PROFILE);
        });
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> host.showRoute(TaPortalHost.ROUTE_PROFILE));
        bottom.add(save);
        bottom.add(cancel);
        card.add(bottom, BorderLayout.SOUTH);

        contentWrap.add(card, BorderLayout.CENTER);
        wrap.add(contentWrap, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel labeled(String label, Component field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Ui.body(label));
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }
}
