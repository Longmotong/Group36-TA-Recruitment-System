package profile_module.ui.screens;

import profile_module.data.ProfileData;
import profile_module.ui.AppFrame;
import profile_module.ui.TaTopNavigationPanel;
import profile_module.ui.Theme;
import profile_module.ui.Ui;
import profile_module.ui.components.PrimaryButton;
import profile_module.ui.components.SecondaryButton;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
            
            syncProfileToTAUser(next);
            app.showRoute(AppFrame.ROUTE_PROFILE);
        });
        SecondaryButton cancel = new SecondaryButton("Cancel");
        cancel.addActionListener(e -> app.showRoute(AppFrame.ROUTE_PROFILE));
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

    
    private void syncProfileToTAUser(ProfileData profileData) {
        try {
            TA_Job_Application_Module.DataService ds = TA_Job_Application_Module.DataService.getInstance();
            TA_Job_Application_Module.TAUser user = ds.getCurrentUser();
            if (user != null) {
                
                TA_Job_Application_Module.TAUser.Profile profile = user.getProfile();
                if (profile == null) {
                    profile = new TA_Job_Application_Module.TAUser.Profile();
                    user.setProfile(profile);
                }
                profile.setFullName(nonNull(profileData.fullName));
                profile.setStudentId(nonNull(profileData.studentId));
                profile.setYear(nonNull(profileData.year));
                profile.setProgramMajor(nonNull(profileData.programMajor));
                profile.setPhoneNumber(nonNull(profileData.phoneNumber));
                profile.setAddress(nonNull(profileData.address));
                profile.setShortBio(nonNull(profileData.shortBio));

                
                if (user.getAccount() != null) {
                    user.getAccount().setEmail(nonNull(profileData.email));
                }

                
                ds.saveCurrentUserToFile();
                System.out.println("[EditProfileScreen] Profile synchronized to TAUser and saved.");
            }
        } catch (Exception e) {
            System.err.println("[EditProfileScreen] Failed to sync profile to TAUser: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

