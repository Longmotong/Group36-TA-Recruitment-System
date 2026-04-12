package TA_Job_Application_Module;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;




public class Page_Profile {
    
    private JPanel panel;
    private TAUser currentUser;
    
    public Page_Profile(TAUser currentUser) {
        this.currentUser = currentUser;
        initPanel();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    private void initPanel() {
        panel = UI_Helper.createPagePanel();
        buildContent();
    }
    
    private void buildContent() {
        // Header
        JPanel header = new JPanel(new BorderLayout(24, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleLeft = new JPanel(new BorderLayout(0, 6));
        titleLeft.setOpaque(false);

        JLabel titleLabel = new JLabel("Profile Module");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleLeft.add(titleLabel, BorderLayout.NORTH);

        JLabel subtitleLabel = new JLabel("Manage your personal and academic information.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        titleLeft.add(subtitleLabel, BorderLayout.SOUTH);

        header.add(titleLeft, BorderLayout.WEST);

        panel.add(header);

        // Profile card
        JPanel profileCard = UI_Helper.createCard();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        profileCard.setMaximumSize(new Dimension(600, 800));

        // Avatar and name
        JPanel profileHeader = new JPanel(new BorderLayout(20, 0));
        profileHeader.setOpaque(false);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, UI_Constants.PRIMARY_COLOR, getWidth(), getHeight(), new Color(129, 140, 248));
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        avatar.setPreferredSize(new Dimension(72, 72));
        avatar.setLayout(new GridBagLayout());
        JLabel initials = new JLabel(UI_Helper.getInitials(currentUser.getProfile().getFullName()));
        initials.setFont(new Font("Segoe UI", Font.BOLD, 24));
        initials.setForeground(Color.WHITE);
        avatar.add(initials);

        JPanel nameInfo = new JPanel();
        nameInfo.setLayout(new BoxLayout(nameInfo, BoxLayout.Y_AXIS));
        nameInfo.setOpaque(false);

        JLabel name = new JLabel(currentUser.getProfile().getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 20));
        name.setForeground(UI_Constants.TEXT_PRIMARY);
        nameInfo.add(name);

        JLabel program = new JLabel(currentUser.getProfile().getProgramMajor() + " - " + currentUser.getProfile().getYear());
        program.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        program.setForeground(UI_Constants.TEXT_SECONDARY);
        program.setBorder(new EmptyBorder(4, 0, 0, 0));
        nameInfo.add(program);

        JLabel studentId = new JLabel("Student ID: " + currentUser.getProfile().getStudentId());
        studentId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentId.setForeground(UI_Constants.TEXT_SECONDARY);
        studentId.setBorder(new EmptyBorder(3, 0, 0, 0));
        nameInfo.add(studentId);

        profileHeader.add(avatar, BorderLayout.WEST);
        profileHeader.add(nameInfo, BorderLayout.CENTER);
        profileCard.add(profileHeader);
        
        // Details
        addProfileRow(profileCard, "Email", currentUser.getAccount().getEmail());
        addProfileRow(profileCard, "Phone", currentUser.getProfile().getPhoneNumber());
        addProfileRow(profileCard, "GPA", String.valueOf(currentUser.getAcademic().getGpa()));
        addProfileRow(profileCard, "Bio", currentUser.getProfile().getShortBio());
        
        // Completion bar
        JPanel completionSection = new JPanel();
        completionSection.setLayout(new BoxLayout(completionSection, BoxLayout.Y_AXIS));
        completionSection.setOpaque(false);
        completionSection.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JPanel completionHeader = new JPanel(new BorderLayout());
        completionHeader.setOpaque(false);
        JLabel compLabel = new JLabel("Profile Completion");
        compLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        compLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        completionHeader.add(compLabel, BorderLayout.WEST);
        
        JLabel compValue = new JLabel(currentUser.getProfileCompletion() + "%");
        compValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        compValue.setForeground(UI_Constants.PRIMARY_COLOR);
        completionHeader.add(compValue, BorderLayout.EAST);
        
        completionSection.add(completionHeader);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setValue(currentUser.getProfileCompletion());
        progressBar.setStringPainted(false);
        progressBar.setBackground(UI_Constants.BORDER_COLOR);
        progressBar.setForeground(UI_Constants.PRIMARY_COLOR);
        progressBar.setPreferredSize(new Dimension(0, 8));
        progressBar.setBorderPainted(false);
        progressBar.setBorder(null);
        completionSection.add(progressBar);
        
        profileCard.add(completionSection);
        
        // Edit button
        JButton editBtn = UI_Helper.createSecondaryButton("Edit Profile");
        editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editBtn.setBorder(new EmptyBorder(20, 0, 0, 0));
        profileCard.add(editBtn);
        
        panel.add(profileCard);
    }
    
    private void addProfileRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UI_Constants.TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(UI_Constants.TEXT_PRIMARY);
        val.setHorizontalAlignment(JLabel.RIGHT);
        row.add(val, BorderLayout.EAST);
        
        panel.add(row);
    }
}

