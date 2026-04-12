package Authentication_Module.view;

import Authentication_Module.session.SessionManager;
import Authentication_Module.model.User;
import profile_module.data.JsonStore;
import profile_module.data.ProfileData;
import TA_Job_Application_Module.DataService;
import TA_Job_Application_Module.TAUser;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;


public class TAHomePagePanel extends JPanel {

    private final AppFrame app;
    private final JLabel label;

    public TAHomePagePanel(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        label = new JLabel("TA Portal - Loading...", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            SwingUtilities.invokeLater(this::tryOpenProfilePortal);
        }
    }

    private void tryOpenProfilePortal() {
        // 如果用户已经登出，不再打开门户
        if (SessionManager.getCurrentUser() == null) {
            return;
        }
        if (!isShowing()) {
            return;
        }
        // 再次检查登出状态（双重检查）
        if (SessionManager.getCurrentUser() == null) {
            return;
        }

        User authUser = SessionManager.getCurrentUser();
        if (authUser == null) {
            label.setText("No user logged in. Please login first.");
            return;
        }

        TAUser taUser = DataService.getInstance().loadUserData(authUser.getUsername(), authUser.getRole());
        ProfileData profile = buildProfileFromAuthUser(authUser, taUser);

        
        JsonStore store = new JsonStore(authUser.getUsername());

        
        SwingUtilities.invokeLater(() -> {
            
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }

           
            String initialRoute = (taUser != null && taUser.isOnboardingCompleted())
                    ? profile_module.ui.AppFrame.ROUTE_DASHBOARD
                    : profile_module.ui.AppFrame.ROUTE_ONBOARDING;

            profile_module.ui.AppFrame profileFrame = new profile_module.ui.AppFrame(store, profile, initialRoute);
            profileFrame.setAuthenticatedContext(authUser.getUsername(), authUser.getRole());
            profileFrame.setOnLogout(() -> {
                profileFrame.dispose();
                SessionManager.logout();
                app.setVisible(true);
                app.showPage("HOME");
            });
            profileFrame.setVisible(true);
        });

        
        app.setVisible(false);
    }

    
    private ProfileData buildProfileFromAuthUser(User user, TAUser taUser) {
        ProfileData p = new ProfileData();
        if (taUser != null) {
            p.fullName = taUser.getProfile() != null ? taUser.getProfile().getFullName() : user.getUsername();
            p.studentId = taUser.getProfile() != null ? taUser.getProfile().getStudentId() : "";
            p.year = taUser.getProfile() != null ? taUser.getProfile().getYear() : "";
            p.programMajor = taUser.getProfile() != null ? taUser.getProfile().getProgramMajor() : "";
            p.email = taUser.getAccount() != null ? taUser.getAccount().getEmail() : user.getUsername() + "@university.edu";
            p.phoneNumber = taUser.getProfile() != null ? taUser.getProfile().getPhoneNumber() : "";
            p.address = taUser.getProfile() != null ? taUser.getProfile().getAddress() : "";
            p.shortBio = taUser.getProfile() != null ? taUser.getProfile().getShortBio() : "";
            
            if (taUser.getSkills() != null) {
                p.skills = new java.util.ArrayList<>();
                copySkills(p.skills, taUser.getSkills().getProgramming());
                copySkills(p.skills, taUser.getSkills().getTeaching());
                copySkills(p.skills, taUser.getSkills().getCommunication());
                copySkills(p.skills, taUser.getSkills().getOther());
            }
           
            if (taUser.getCv() != null) {
                p.cv = new profile_module.data.CvInfo();
                p.cv.fileName = taUser.getCv().getOriginalFileName() != null ? taUser.getCv().getOriginalFileName() : "";
                p.cv.status = taUser.getCv().isUploaded() ? "Uploaded" : "";
                p.cv.lastUpdated = taUser.getCv().getUploadedAt() != null ? taUser.getCv().getUploadedAt() : "";
                p.cv.sizeLabel = "";
            }
            p.profileCompletionPercent = taUser.getProfileCompletion();
            p.numberOfApplications = taUser.getApplicationSummary() != null ? taUser.getApplicationSummary().getTotalApplications() : 0;
        } else {
            p.fullName = user.getUsername();
            p.studentId = "";
            p.email = user.getUsername() + "@university.edu";
            p.profileCompletionPercent = 0;
            p.numberOfApplications = 0;
        }
        return p;
    }

   
    private void copySkills(java.util.List<profile_module.data.SkillItem> target, java.util.List<TAUser.Skill> source) {
        if (source != null) {
            for (TAUser.Skill s : source) {
                profile_module.data.SkillItem item = new profile_module.data.SkillItem();
                item.name = s.getName();
                item.category = determineCategory(s);
                item.proficiency = s.getProficiency();
                target.add(item);
            }
        }
    }

   
    private String determineCategory(TAUser.Skill s) {
        
        return "Other Skills";
    }
}
