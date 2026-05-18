package TA_Job_Application_Module.portal;

import TA_Job_Application_Module.pages.applications.ApplicationsTabController;
import TA_Job_Application_Module.pages.applications.Page_MyApplications;
import TA_Job_Application_Module.pages.apply.ApplyDraftAdapter;
import TA_Job_Application_Module.pages.apply.ApplyFormPanel;
import TA_Job_Application_Module.pages.apply.Page_Apply;
import TA_Job_Application_Module.pages.jobs.JobsPageController;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;
import TA_Job_Application_Module.pages.jobs.Page_JobDetail;
import TA_Job_Application_Module.pages.jobs.Page_Jobs;
import TA_Job_Application_Module.pages.profile.Page_Profile;
import TA_Job_Application_Module.pages.status.Page_ApplicationStatus;
import TA_Job_Application_Module.portal.callbacks.PortalFlowCallbacks;
import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.model.TAUser;
import TA_Job_Application_Module.service.DataFacade;
import TA_Job_Application_Module.service.DataService;

import profile_module.ui.AppFrame;
import profile_module.ui.TaTopNavigationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class TAPortalApp extends JFrame {
    
    private DataService dataService;
    private DataFacade dataFacade;
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
    private JobsPageController jobsController;
    private ApplyFormPanel applyFormPanel;
    private ApplyDraftAdapter applyDraftAdapter;
    private ApplicationsTabController applicationsController;
    
    private Job selectedJob;

    private TaTopNavigationPanel topNav;
    
    private String pendingInitialPage;
    private boolean pendingShowPage;
    private PortalNavigator navigator;

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

    private TAPortalApp(Consumer<String> onReturn, String initialPage) {
        this.returnToMainCallback = onReturn;
        this.pendingInitialPage = initialPage != null && !initialPage.isBlank() ? initialPage : PortalRoutes.JOBS;
        this.pendingShowPage = true;
        dataService = DataService.getInstance();
        dataFacade = new DataFacade(dataService);
        currentUser = dataFacade.currentUser();

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
                // 清除 AI 分析结果
                dataService.clearCachedAIResults();
                
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
        mainContentPanel.setBackground(JobsPortalUi.PAGE_BG);
        
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
                showPage(PortalRoutes.JOBS);
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
        topNav = new TaTopNavigationPanel(actions, this::jobPortalUserLine, TaTopNavigationPanel.Active.JOBS,
                TaTopNavigationPanel.NavStyle.PORTAL_PURPLE_GRADIENT);
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
        if (PortalRoutes.PROFILE.equals(pageName)) {
            topNav.setActive(TaTopNavigationPanel.Active.PROFILE);
        } else {
            topNav.setActive(TaTopNavigationPanel.Active.JOBS);
        }
        topNav.refreshUserLabel();
    }
    
    private void initPages() {
        PortalFlowCallbacks.JobsFlow jobsFlow = new PortalFlowCallbacks.JobsFlow() {
            @Override
            public void openJobDetail(Job job) {
                selectedJob = job;
                jobDetailPage.showJob(job);
                showPage(PortalRoutes.JOB_DETAIL);
            }
            @Override
            public void openApplications() { showPage(PortalRoutes.APPLICATIONS); }
            @Override
            public void returnHome() { returnToMainApp(AppFrame.ROUTE_DASHBOARD); }
        };
        jobsPage = new Page_Jobs(dataService, new Page_Jobs.JobsCallback() {
            @Override public void onViewJobDetail(Job job) { jobsFlow.openJobDetail(job); }
            @Override public void onGoToApplications() { jobsFlow.openApplications(); }
            @Override public void onGoToHome() { jobsFlow.returnHome(); }
        });
        jobsController = new JobsPageController(jobsPage);
        mainContentPanel.add(jobsController.view(), PortalRoutes.JOBS);
        
        PortalFlowCallbacks.JobDetailFlow jobDetailFlow = new PortalFlowCallbacks.JobDetailFlow() {
            @Override
            public void backToJobs() { showPage(PortalRoutes.JOBS); }
            @Override
            public void openApply(Job job) {
                selectedJob = job;
                applyFormPanel.openForJob(job);
                showPage(PortalRoutes.APPLY);
            }
        };
        jobDetailPage = new Page_JobDetail(dataService, new Page_JobDetail.JobDetailCallback() {
            @Override public void onBackToJobs() { jobDetailFlow.backToJobs(); }
            @Override public void onApply(Job job) { jobDetailFlow.openApply(job); }
        });
        jobDetailScroll = wrapContentInScrollPane(jobDetailPage.getPanel());
        mainContentPanel.add(jobDetailScroll, PortalRoutes.JOB_DETAIL);
        
        PortalFlowCallbacks.ApplyFlow applyFlow = new PortalFlowCallbacks.ApplyFlow() {
            @Override
            public void backToJobDetail(Job job) {
                jobDetailPage.showJob(job);
                showPage(PortalRoutes.JOB_DETAIL);
            }
            @Override
            public void submitSuccess() {
                applicationsController.refreshAll();
                SwingUtilities.invokeLater(() -> {
                    showPage(PortalRoutes.APPLICATIONS);
                    applicationsController.openApplicationsTab();
                });
            }

            @Override
            public void draftSaved() {
                applicationsController.refreshAll();
                SwingUtilities.invokeLater(() -> {
                    showPage(PortalRoutes.APPLICATIONS);
                    applicationsController.openDraftsTab();
                });
            }
        };
        applyPage = new Page_Apply(currentUser, dataService, new Page_Apply.ApplyCallback() {
            @Override public void onBackToJobDetail(Job job) { applyFlow.backToJobDetail(job); }
            @Override public void onSubmitSuccess() { applyFlow.submitSuccess(); }
            @Override public void onDraftSaved() { applyFlow.draftSaved(); }
        });
        applyFormPanel = new ApplyFormPanel(applyPage);
        applyDraftAdapter = new ApplyDraftAdapter(applyFormPanel);
        applyScroll = wrapContentInScrollPane(applyPage.getPanel());
        mainContentPanel.add(applyScroll, PortalRoutes.APPLY);
        
        PortalFlowCallbacks.ApplicationsFlow applicationsFlow = new PortalFlowCallbacks.ApplicationsFlow() {
            @Override
            public void openStatus(Application application) {
                applicationStatusPage.showApplication(application);
                showPage(PortalRoutes.STATUS);
            }

            @Override
            public void backToHome() {
                returnToMainApp(AppFrame.ROUTE_DASHBOARD);
            }

            @Override
            public void browseJobs() {
                showPage(PortalRoutes.JOBS);
            }

            @Override
            public void cancelApplication(Application application) {
                dataFacade.cancelApplication(application.getApplicationId());
                applicationsController.refreshAll();
            }

            @Override
            public void editDraft(Application draft) {
                Job job = dataFacade.findJob(draft.getJobId());
                if (job != null) {
                    applyDraftAdapter.open(job, draft);
                    showPage(PortalRoutes.APPLY);
                } else {
                    JOptionPane.showMessageDialog(null,
                        "The job for this draft is no longer available.",
                        "Draft Unavailable", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        myApplicationsPage = new Page_MyApplications(dataService, new Page_MyApplications.MyApplicationsCallback() {
            @Override public void onViewStatus(Application application) { applicationsFlow.openStatus(application); }
            @Override public void onBackToHome() { applicationsFlow.backToHome(); }
            @Override public void onBrowseJobs() { applicationsFlow.browseJobs(); }
            @Override public void onCancelApplication(Application application) { applicationsFlow.cancelApplication(application); }
            @Override public void onEditDraft(Application draft) { applicationsFlow.editDraft(draft); }
        });
        applicationsController = new ApplicationsTabController(myApplicationsPage);
        mainContentPanel.add(applicationsController.view(), PortalRoutes.APPLICATIONS);
        
        applicationStatusPage = new Page_ApplicationStatus(new Page_ApplicationStatus.StatusCallback() {
            @Override
            public void onBackToApplications() { showPage(PortalRoutes.APPLICATIONS); }

            @Override
            public void onCancelled() {
                String appId = applicationStatusPage.getCurrentApplicationId();
                if (appId != null) {
                    dataFacade.cancelApplication(appId);
                }
                applicationsController.refreshAll();
                showPage(PortalRoutes.APPLICATIONS);
            }

            @Override
            public void onAcceptOffer(String applicationId) {
                dataFacade.acceptOffer(applicationId);
                applicationsController.refreshAll();
                showPage(PortalRoutes.APPLICATIONS);
            }

            @Override
            public void onDeclineOffer(String applicationId) {
                dataFacade.declineOffer(applicationId);
                applicationsController.refreshAll();
                showPage(PortalRoutes.APPLICATIONS);
            }
        }, dataService);
        applicationStatusScroll = wrapContentInScrollPane(applicationStatusPage.getPanel());
        mainContentPanel.add(applicationStatusScroll, PortalRoutes.STATUS);
        
        profilePage = new Page_Profile(currentUser);
        mainContentPanel.add(profilePage.getPanel(), PortalRoutes.PROFILE);

        navigator = new PortalNavigator(
                cardLayout,
                mainContentPanel,
                jobsPage,
                myApplicationsPage,
                jobDetailScroll,
                applicationStatusScroll,
                applyScroll,
                null
        );
    }
    
    private static JScrollPane wrapContentInScrollPane(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(JobsPortalUi.PAGE_BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void showPage(String pageName) {
        if (navigator != null) {
            navigator.showPage(pageName);
        } else {
            cardLayout.show(mainContentPanel, pageName);
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
