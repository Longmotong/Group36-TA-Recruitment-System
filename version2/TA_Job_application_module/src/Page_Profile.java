

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;



/**
 * 个人资料页面
 * 显示用户的个人信息和学术信息
 */
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
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("Profile Module");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel("Manage your personal and academic information");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        header.add(subtitleLabel, BorderLayout.SOUTH);
        
        panel.add(header);
        
        // Profile card
        JPanel profileCard = UI_Helper.createCard();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(new EmptyBorder(30, 30, 30, 30));
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
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setLayout(new GridBagLayout());
        JLabel initials = new JLabel(UI_Helper.getInitials(currentUser.getProfile().getFullName()));
        initials.setFont(new Font("Segoe UI", Font.BOLD, 28));
        initials.setForeground(Color.WHITE);
        avatar.add(initials);
        
        JPanel nameInfo = new JPanel();
        nameInfo.setLayout(new BoxLayout(nameInfo, BoxLayout.Y_AXIS));
        nameInfo.setOpaque(false);
        
        JLabel name = new JLabel(currentUser.getProfile().getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        name.setForeground(UI_Constants.TEXT_PRIMARY);
        nameInfo.add(name);
        
        JLabel program = new JLabel(currentUser.getProfile().getProgramMajor() + " - " + currentUser.getProfile().getYear());
        program.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        program.setForeground(UI_Constants.TEXT_SECONDARY);
        program.setBorder(new EmptyBorder(5, 0, 0, 0));
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
