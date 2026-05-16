package view;

import model.User;
import util.JsonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MOFirstLoginPagePanel extends JPanel {



    public MOFirstLoginPagePanel(AppFrame app) {

        User user = app.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(25, 40, 25, 40)
        ));
        card.setPreferredSize(new Dimension(420, 520));

        // ===== 标题 =====
        JLabel title = new JLabel("Complete Your Profile (MO)");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Please fill in your information");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(new Color(120, 120, 120));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== 输入框 =====
        JTextField fullName = createField("Full Name *");
        JTextField staffId = createField("Staff ID *");
        JTextField department = createField("Department *");
        JTextField phone = createField("Phone Number *");
        JTextField school = createField("School *");
        JTextField email = createField("Campus Email *");
        JTextField campus = createField("Teaching Campus");
        JTextField courseTypes = createField("Course Types (comma separated)");

        // ===== 按钮 =====
        JButton submit = new JButton("Save & Continue");
        submit.setAlignmentX(Component.CENTER_ALIGNMENT);
        submit.setBackground(Color.BLACK);
        submit.setForeground(Color.WHITE);
        submit.setFocusPainted(false);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // ===== 提交逻辑 =====
        submit.addActionListener(e -> {

            if (fullName.getText().isEmpty() ||
                    staffId.getText().isEmpty() ||
                    department.getText().isEmpty() ||
                    phone.getText().isEmpty() ||
                    school.getText().isEmpty() ||
                    email.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Please fill all required fields (*)");
                return;
            }

            //  写入 JSON
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

            //  跳转 MO 首页
            app.showPage("MO");
        });

        // ===== 组装 =====
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
        add(wrapper, BorderLayout.CENTER);
    }

    private JTextField createField(String title) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setBorder(BorderFactory.createTitledBorder(title));
        return field;
    }
}