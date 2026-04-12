package TA_Job_Application_Module;

import com.taapp.ui.UI;

import profile_module.ui.AppFrame;
import profile_module.ui.TaTopNavigationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class TAPortalApp extends JFrame {
    
    private DataService dataService;
    private TAUser currentUser;
    
    private Consumer<String> returnToMainCallback;
   
    private Runnable onLogoutCallback;
    
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    
    private Page_Jobs jobsPage;
    private Page_JobDetail jobDetailPage;
    private JScrollPane jobDetailScroll;
    private JScrollPane applicationStatusScroll;
    private JScrollPane applyScroll;
    private Page_Apply applyPage;
    private Page_MyApplications myApplicationsPage;
    private Page_ApplicationStatus applicationStatusPage;
    private Page_Profile profilePage;
    
    private Job selectedJob;

    private TaTopNavigationPanel topNav;
    
    public TAPortalApp() {
        this(null, "jobs");
    }

    
    public TAPortalApp(String initialPage) {
        this(null, initialPage);
    }

    public void setReturnCallback(Consumer<String> callback) {
        this.returnToMainCallback = callback;
    }

    
    public void setLogoutCallback(Runnable callback) {
        this.onLogoutCallback = callback;
    }

    public TAPortalApp(Runnable onReturn) {
        this(r -> {
            if (onReturn != null) {
                onReturn.run();
            }
        }, "jobs");
    }

   
    public TAPortalApp(java.util.function.Consumer<String> onReturn) {
        this(onReturn, "jobs");
    }

    private String pendingInitialPage;
    private boolean pendingShowPage;

    private TAPortalApp(Consumer<String> onReturn, String initialPage) {
        this.returnToMainCallback = onReturn;
        this.pendingInitialPage = initialPage != null && !initialPage.isBlank() ? initialPage : "jobs";
        this.pendingShowPage = true;
        dataService = DataService.getInstance();
        currentUser = dataService.getCurrentUser();

        initFrame();
        initNavigation();
        initPages();
    }

    public void finishInit() {
        if (pendingShowPage) {
            showPage(pendingInitialPage);
            pendingShowPage = false;
        }
    }
    
    private void initFrame() {
        setTitle("TA System - Job Applications");
        setMinimumSize(new Dimension(980, 680));
        setSize(1080, 760);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (returnToMainCallback != null) {
                    Consumer<String> cb = returnToMainCallback;
                    returnToMainCallback = null;
                    cb.accept(AppFrame.ROUTE_DASHBOARD);
                }
            }
        });
        
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UI.palette().appBg());
        
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private void initNavigation() {
        TaTopNavigationPanel.Actions actions = new TaTopNavigationPanel.Actions() {
            @Override
            public void onHome() {
                returnToMainApp(AppFrame.ROUTE_DASHBOARD);
            }

            @Override
            public void onProfileModule() {
                returnToMainApp(AppFrame.ROUTE_PROFILE);
            }

            @Override
            public void onJobApplicationModule() {
                showPage("jobs");
            }

            @Override
            public void onLogout() {
                int ok = JOptionPane.showConfirmDialog(TAPortalApp.this,
                        "Are you sure you want to logout?", "Logout", JOptionPane.OK_CANCEL_OPTION);
                if (ok == JOptionPane.OK_OPTION) {
                    if (onLogoutCallback != null) {
                        dispose();
                        onLogoutCallback.run();
                    } else {
                        returnToMainApp(null);
                    }
                }
            }
        };
        topNav = new TaTopNavigationPanel(actions, this::jobPortalUserLine, TaTopNavigationPanel.Active.JOBS);
        add(topNav, BorderLayout.NORTH);
    }

    private String jobPortalUserLine() {
        TAUser u = dataService != null ? dataService.getCurrentUser() : currentUser;
        if (u == null) {
            return "";
        }
        if (u.getLoginId() != null && !u.getLoginId().isBlank()) {
            return "Logged in: " + u.getLoginId();
        }
        if (u.getProfile() != null && u.getProfile().getStudentId() != null
                && !u.getProfile().getStudentId().isBlank()) {
            return "Logged in: " + u.getProfile().getStudentId();
        }
        return "";
    }

    
    private void returnToMainApp(String route) {
        Consumer<String> cb = returnToMainCallback;
        returnToMainCallback = null;
        if (cb != null) {
            String targetRoute = (route == null || route.isBlank()) ? AppFrame.ROUTE_DASHBOARD : route;
            cb.accept(targetRoute);
        }
        dispose();
    }

    private void updateNavHighlight(String pageName) {
        if (topNav == null) {
            return;
        }
        if ("profile".equals(pageName)) {
            topNav.setActive(TaTopNavigationPanel.Active.PROFILE);
        } else {
            topNav.setActive(TaTopNavigationPanel.Active.JOBS);
        }
        topNav.refreshUserLabel();
    }
    
    private void initPages() {
        jobsPage = new Page_Jobs(dataService, new Page_Jobs.JobsCallback() {
            @Override
            public void onViewJobDetail(Job job) {
                selectedJob = job;
                jobDetailPage.showJob(job);
                showPage("job-detail");
            }
            @Override
            public void onGoToApplications() { showPage("applications"); }
            @Override
            public void onGoToHome() { returnToMainApp(AppFrame.ROUTE_DASHBOARD); }
        });
        mainContentPanel.add(jobsPage.getPanel(), "jobs");
        
        jobDetailPage = new Page_JobDetail(dataService, new Page_JobDetail.JobDetailCallback() {
            @Override
            public void onBackToJobs() { showPage("jobs"); }
            @Override
            public void onApply(Job job) {
                selectedJob = job;
                applyPage.showJob(job);
                showPage("apply");
            }
        });
        jobDetailScroll = wrapContentInScrollPane(jobDetailPage.getPanel());
        mainContentPanel.add(jobDetailScroll, "job-detail");
        
        applyPage = new Page_Apply(currentUser, dataService, new Page_Apply.ApplyCallback() {
            @Override
            public void onBackToJobDetail(Job job) {
                jobDetailPage.showJob(job);
                showPage("job-detail");
            }
            @Override
            public void onSubmitSuccess() {
                myApplicationsPage.refreshTable();
                showPage("applications");
            }

            @Override
            public void onDraftSaved() {
                myApplicationsPage.refreshTable();
                SwingUtilities.invokeLater(() -> {
                    showPage("applications");
                    myApplicationsPage.selectDraftsTab();
                });
            }
        });
        applyScroll = wrapContentInScrollPane(applyPage.getPanel());
        mainContentPanel.add(applyScroll, "apply");
        
        myApplicationsPage = new Page_MyApplications(dataService, new Page_MyApplications.MyApplicationsCallback() {
            @Override
            public void onViewStatus(Application application) {
                applicationStatusPage.showApplication(application);
                showPage("status");
            }

            @Override
            public void onBackToHome() {
                returnToMainApp(AppFrame.ROUTE_DASHBOARD);
            }

            @Override
            public void onBrowseJobs() {
                showPage("jobs");
            }

            @Override
            public void onCancelApplication(Application application) {
                dataService.cancelApplication(application.getApplicationId());
                myApplicationsPage.refreshTable();
            }

            @Override
            public void onEditDraft(Application draft) {
                Job job = dataService.getJobById(draft.getJobId());
                if (job != null) {
                    applyPage.showJobForDraft(job, draft);
                    showPage("apply");
                } else {
                    JOptionPane.showMessageDialog(null,
                        "The job for this draft is no longer available.",
                        "Draft Unavailable", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        mainContentPanel.add(myApplicationsPage.getPanel(), "applications");
        
        applicationStatusPage = new Page_ApplicationStatus(new Page_ApplicationStatus.StatusCallback() {
            @Override
            public void onBackToApplications() { showPage("applications"); }

            @Override
            public void onCancelled() {
                String appId = applicationStatusPage.getCurrentApplicationId();
                if (appId != null) {
                    dataService.cancelApplication(appId);
                }
                myApplicationsPage.refreshTable();
                showPage("applications");
            }
        });
        applicationStatusScroll = wrapContentInScrollPane(applicationStatusPage.getPanel());
        mainContentPanel.add(applicationStatusScroll, "status");
        
        profilePage = new Page_Profile(currentUser);
        mainContentPanel.add(profilePage.getPanel(), "profile");
    }
    
    private static JScrollPane wrapContentInScrollPane(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(UI.palette().appBg());
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void showPage(String pageName) {
        if (pageName.equals("applications")) {
            myApplicationsPage.refreshTable();
        } else if (pageName.equals("jobs")) {
            jobsPage.refreshJobs();
        }
        
        cardLayout.show(mainContentPanel, pageName);
        if ("job-detail".equals(pageName) && jobDetailScroll != null) {
            SwingUtilities.invokeLater(() -> jobDetailScroll.getVerticalScrollBar().setValue(0));
        }
        if ("status".equals(pageName) && applicationStatusScroll != null) {
            SwingUtilities.invokeLater(() -> applicationStatusScroll.getVerticalScrollBar().setValue(0));
        }
        if ("apply".equals(pageName) && applyScroll != null) {
            SwingUtilities.invokeLater(() -> applyScroll.getVerticalScrollBar().setValue(0));
        }
        updateNavHighlight(pageName);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        SwingUtilities.invokeLater(() -> {
            TAPortalApp app = new TAPortalApp();
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            app.setVisible(true);
            app.finishInit();
        });
    }
}
