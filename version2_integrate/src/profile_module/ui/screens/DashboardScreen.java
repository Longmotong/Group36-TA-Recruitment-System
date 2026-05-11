package profile_module.ui.screens;

import com.taapp.ui.AppLayout;
import com.taapp.ui.DashboardModuleIcons;
import com.taapp.ui.UI;
import com.taapp.ui.components.RoundedActionButton;

import profile_module.ui.AppFrame;
import profile_module.ui.TaTopNavigationPanel;
import TA_Job_Application_Module.DataService;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * TA dashboard — layout and visual language aligned with {@link com.taapp.ui.pages.AdminDashboardPanel}
 * (max width, welcome banner, module cards, quick overview stat boxes).
 */
public final class DashboardScreen extends JPanel {
    private final AppFrame app;

    private final JLabel completionValue = new JLabel("0%", SwingConstants.CENTER);
    private final JLabel cvStatusValue = new JLabel("—", SwingConstants.CENTER);
    private final JLabel appsValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel welcomeMessage = new JLabel();

    private final TaTopNavigationPanel topNav;

    public DashboardScreen(AppFrame app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(UI.palette().appBg());

        loadJarsFromLib();

        topNav = TaTopNavigationPanel.forAppFrame(app, TaTopNavigationPanel.Active.HOME);
        add(topNav, BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildCenter() {
        JPanel host = new JPanel(new BorderLayout());
        host.setOpaque(false);
        host.setBackground(UI.palette().appBg());
        host.add(AppLayout.wrapCentered(buildDashboardRoot()), BorderLayout.CENTER);
        return host;
    }

    private JPanel buildDashboardRoot() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(28, 0, 40, 0));

        root.add(welcomeBanner());
        root.add(Box.createVerticalStrut(22));

        JPanel cards = new JPanel(new GridLayout(1, 2, 24, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 420));
        final int iconPx = 40;
        cards.add(moduleCard(
                DashboardModuleIcons.userProfile(iconPx),
                ModuleCardStyle.PROFILE,
                "Profile Module",
                "Manage your TA profile",
                new String[]{
                        "Edit personal information and academic details",
                        "Record skills and proficiency for matching",
                        "Upload and maintain your CV"
                },
                "Go to Profile",
                () -> app.showRoute(AppFrame.ROUTE_PROFILE),
                false,
                null
        ));
        cards.add(moduleCard(
                DashboardModuleIcons.briefcase(iconPx),
                ModuleCardStyle.JOBS,
                "Job Application Module",
                "Browse positions and track applications",
                new String[]{
                        "Explore open TA positions and requirements",
                        "Submit and manage your applications",
                        "Monitor status updates and outcomes"
                },
                "Browse Jobs",
                () -> app.openJobApplicationPortal("jobs"),
                true,
                () -> app.openJobApplicationPortal("applications")
        ));
        root.add(cards);
        root.add(Box.createVerticalStrut(22));

        root.add(quickOverviewPanel());
        root.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, Integer.MAX_VALUE));
        return root;
    }

    private enum ModuleCardStyle {
        PROFILE(new Color(0x1A1A1A), new Color(0xF0F0F0), new Color(0xCCCCCC)),
        JOBS(new Color(0x404040), new Color(0xF0F0F0), new Color(0xCCCCCC));

        final Color accent;
        final Color iconBg;
        final Color iconBorder;

        ModuleCardStyle(Color accent, Color iconBg, Color iconBorder) {
            this.accent = accent;
            this.iconBg = iconBg;
            this.iconBorder = iconBorder;
        }
    }

    private JPanel welcomeBanner() {
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setOpaque(true);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 200));
        banner.setBackground(Color.WHITE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x1A1A1A)),
                        BorderFactory.createLineBorder(UI.palette().border())
                ),
                new EmptyBorder(22, 24, 24, 24)
        ));

        JLabel title = new JLabel("TA Dashboard");
        title.setFont(UI.moFontBold(30));
        title.setForeground(new Color(0x0F172A));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomeMessage.setAlignmentX(Component.LEFT_ALIGNMENT);

        banner.add(title);
        banner.add(Box.createVerticalStrut(10));
        banner.add(welcomeMessage);
        return banner;
    }

    private JPanel moduleCard(Icon moduleIcon,
                              ModuleCardStyle style,
                              String title,
                              String intro,
                              String[] bulletItems,
                              String primaryCta,
                              Runnable primaryAction,
                              boolean withSecondary,
                              Runnable secondaryAction) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, style.accent),
                        BorderFactory.createLineBorder(UI.palette().border())
                ),
                new EmptyBorder(22, 22, 20, 24)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setBackground(style.iconBg);
        iconWrap.setBorder(BorderFactory.createLineBorder(style.iconBorder));
        iconWrap.setPreferredSize(new Dimension(56, 56));
        JLabel ic = new JLabel(moduleIcon, SwingConstants.CENTER);
        iconWrap.add(ic, BorderLayout.CENTER);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UI.moFontBold(18));
        t.setForeground(new Color(0x0F172A));
        JLabel introL = new JLabel("<html><div style='width:360px;line-height:1.45;font-family:sans-serif;color:#525252;font-size:14px'>" + intro + "</div></html>");
        titles.add(t);
        titles.add(Box.createVerticalStrut(8));
        titles.add(introL);

        header.add(iconWrap, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel bulletPanel = new JPanel();
        bulletPanel.setLayout(new BoxLayout(bulletPanel, BoxLayout.Y_AXIS));
        bulletPanel.setOpaque(false);
        bulletPanel.setBorder(new EmptyBorder(18, 0, 0, 0));
        for (String b : bulletItems) {
            JLabel line = new JLabel("\u2022  " + b);
            line.setFont(UI.moFontPlain(14));
            line.setForeground(new Color(0x262626));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            line.setBorder(new EmptyBorder(0, 0, 6, 0));
            bulletPanel.add(line);
        }

        JPanel upper = new JPanel(new BorderLayout(0, 0));
        upper.setOpaque(false);
        upper.add(header, BorderLayout.NORTH);
        upper.add(bulletPanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(18, 0, 0, 0));

        RoundedActionButton primary = new RoundedActionButton(primaryCta, RoundedActionButton.Scheme.PRIMARY_BLACK);
        primary.setFont(UI.moFontBold(13));
        primary.addActionListener(e -> primaryAction.run());
        int minW = withSecondary ? 200 : 268;
        int h = Math.max(48, primary.getPreferredSize().height);
        primary.setPreferredSize(new Dimension(Math.max(minW, primary.getPreferredSize().width), h));
        primary.setMinimumSize(new Dimension(minW, h));
        btnRow.add(primary);

        if (withSecondary && secondaryAction != null) {
            JButton secondary = new JButton("My Applications");
            UI.styleSecondaryButton(secondary);
            secondary.addActionListener(e -> secondaryAction.run());
            int sw = Math.max(168, secondary.getPreferredSize().width);
            secondary.setPreferredSize(new Dimension(sw, h));
            secondary.setMinimumSize(new Dimension(168, h));
            btnRow.add(secondary);
        }

        card.add(upper, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quickOverviewPanel() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(true);
        outer.setBackground(Color.WHITE);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border()),
                new EmptyBorder(20, 24, 24, 24)
        ));
        outer.setMaximumSize(new Dimension(AppLayout.CONTENT_MAX_W, 320));

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x1A1A1A)),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JLabel h = new JLabel("Quick Overview");
        h.setFont(UI.moFontBold(17));
        h.setForeground(new Color(0x0F172A));
        head.add(h, BorderLayout.CENTER);
        outer.add(head);
        outer.add(Box.createVerticalStrut(18));

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);

        Color statBg = new Color(0xF5F5F5);
        Color statBorder = new Color(0xD4D4D4);
        Color statValue = new Color(0x111111);

        completionValue.setFont(UI.moFontBold(34));
        completionValue.setForeground(statValue);
        cvStatusValue.setFont(UI.moFontBold(34));
        cvStatusValue.setForeground(statValue);
        appsValue.setFont(UI.moFontBold(34));
        appsValue.setForeground(statValue);

        grid.add(statBox(completionValue, "Profile Completion", statBg, statBorder, statValue));
        grid.add(statBox(cvStatusValue, "CV Upload Status", statBg, statBorder, statValue));
        grid.add(statBox(appsValue, "Applications Submitted", statBg, statBorder, statValue));

        outer.add(grid);
        return outer;
    }

    private JPanel statBox(JLabel valueLabel, String caption, Color bg, Color border, Color valueColor) {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(bg);
        box.setOpaque(true);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(14, 12, 16, 12)
        ));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(UI.moFontBold(12));
        cap.setForeground(new Color(0x404040));
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(Box.createVerticalGlue());
        inner.add(valueLabel);
        inner.add(Box.createVerticalStrut(6));
        inner.add(cap);
        inner.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        box.add(inner, gbc);
        return box;
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * 从 lib 目录动态加载所有 JAR 文件到类路径
     */
    private void loadJarsFromLib() {
        File libDir = new File("lib");
        if (!libDir.exists() || !libDir.isDirectory()) {
            libDir = new File("../lib");
        }
        if (!libDir.exists() || !libDir.isDirectory()) {
            libDir = new File(System.getProperty("user.dir"), "lib");
        }

        File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            return;
        }

        try {
            ClassLoader loader = getClass().getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }

            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            for (File jar : jarFiles) {
                try {
                    URL url = jar.toURI().toURL();
                    method.invoke(loader, url);
                } catch (Exception e) {
                    System.err.println("[DashboardScreen] Failed to load JAR: " + jar.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("[DashboardScreen] Failed to load JARs: " + e.getMessage());
        }
    }

    public void refresh() {
        topNav.refresh(TaTopNavigationPanel.Active.HOME);

        int pct = app.profile().profileCompletionPercent;
        completionValue.setText(pct + "%");
        completionValue.setFont(UI.moFontBold(34));

        String cvText = app.profile().cv != null && !app.profile().cv.fileName.isBlank() ? "Uploaded" : "Not uploaded";
        cvStatusValue.setText(cvText);
        cvStatusValue.setFont(UI.moFontBold(cvText.length() > 8 ? 20 : 34));

        int appCount = 0;
        try {
            DataService ds = DataService.getInstance();
            if (ds != null) {
                appCount = ds.getUserApplications().size();
            }
        } catch (Exception e) {
            appCount = app.profile().numberOfApplications;
        }
        appsValue.setText(String.valueOf(appCount));
        appsValue.setFont(UI.moFontBold(34));

        String name = app.profile().fullName;
        boolean hasName = name != null && !name.isBlank();
        boolean hasCv = app.profile().cv != null && !app.profile().cv.fileName.isBlank();
        boolean hasSkills = app.profile().skills != null && !app.profile().skills.isEmpty();

        String msg;
        if (!hasName && !hasCv && !hasSkills) {
            msg = "Complete your profile to get started with TA applications.";
        } else if (hasName && !hasCv && !hasSkills) {
            msg = "Your basic info is ready! Add skills and upload your CV to apply for TA positions.";
        } else if (hasName && hasCv && !hasSkills) {
            msg = "Almost there! Add your skills to make your profile stand out.";
        } else if (hasName && hasSkills && !hasCv) {
            msg = "Looking good! Upload your CV to start applying for TA positions.";
        } else if (hasName && hasCv && hasSkills) {
            if (app.profile().numberOfApplications > 0) {
                msg = "Your profile is complete! Track your " + app.profile().numberOfApplications + " application(s) below.";
            } else {
                msg = "Your profile is complete! Browse TA positions and start applying.";
            }
        } else {
            msg = "Welcome! Pick a module below to manage your profile or explore TA positions.";
        }

        welcomeMessage.setText("<html><div style='width:720px;line-height:1.5'><span style='color:#525252;font-size:15px;font-family:sans-serif'>"
                + escapeHtml(msg) + "</span></div></html>");

        revalidate();
        repaint();
    }
}
