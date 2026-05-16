package Authentication_Module.view;

import Authentication_Module.model.User;
import Authentication_Module.util.JsonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MOFirstLoginPagePanel extends JPanel {

    public MOFirstLoginPagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(25, 40, 25, 40)
        ));
        card.setPreferredSize(new Dimension(420, 580));
        card.setMaximumSize(new Dimension(460, Integer.MAX_VALUE));

        JLabel title = new JLabel("Complete Your Profile (MO)");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Please fill in your information");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(new Color(120, 120, 120));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField fullName = createField("Full Name *");
        JTextField staffId = createField("Staff ID *");
        JTextField department = createField("Department *");
        JTextField phone = createField("Phone Number *");
        JTextField school = createField("School *");
        JTextField email = createField("Campus Email *");
        JTextField campus = createField("Teaching Campus");
        JTextField courseTypes = createField("Course Types (comma separated)");

        JButton submit = new JButton("Save & Continue");
        submit.setAlignmentX(Component.CENTER_ALIGNMENT);
        submit.setBackground(Color.BLACK);
        submit.setForeground(Color.WHITE);
        submit.setFocusPainted(false);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        submit.addActionListener(e -> {
            User user = app.getCurrentUser();
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Session expired. Please login again.");
                app.showPage("LOGIN");
                return;
            }

            if (fullName.getText().isEmpty()
                    || staffId.getText().isEmpty()
                    || department.getText().isEmpty()
                    || phone.getText().isEmpty()
                    || school.getText().isEmpty()
                    || email.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields (*)");
                return;
            }

            JsonUtil.updateMOProfile(
                    user,
                    fullName.getText(),
                    staffId.getText(),
                    department.getText(),
                    phone.getText(),
                    school.getText(),
                    email.getText(),
                    campus.getText(),
                    courseTypes.getText()
            );

            JOptionPane.showMessageDialog(this, "Profile saved!");
            app.showPage("MO");
        });

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);
        card.add(Box.createVerticalStrut(20));
        card.add(fullName);
        card.add(Box.createVerticalStrut(10));
        card.add(staffId);
        card.add(Box.createVerticalStrut(10));
        card.add(department);
        card.add(Box.createVerticalStrut(10));
        card.add(phone);
        card.add(Box.createVerticalStrut(10));
        card.add(school);
        card.add(Box.createVerticalStrut(10));
        card.add(email);
        card.add(Box.createVerticalStrut(10));
        card.add(campus);
        card.add(Box.createVerticalStrut(10));
        card.add(courseTypes);
        card.add(Box.createVerticalStrut(20));
        card.add(submit);

        wrapper.add(card);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JTextField createField(String title) {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(0, 44));
        field.setMinimumSize(new Dimension(0, 44));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setBorder(BorderFactory.createTitledBorder(title));
        return field;
    }
}
