package ui;

import data.ProfileData;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

/**
 * Top navigation for the TA profile shell. {@link NavStyle#STANDARD} matches the original light bar;
 * {@link NavStyle#PORTAL_PURPLE_GRADIENT} matches the Teaching Assistant portal mockup.
 */
public final class TaTopNavigationPanel extends JPanel {

    private static volatile Image cachedSchoolLogo;
    /** If the PNG on disk is replaced, reload so the bar shows the new asset without restarting the JVM. */
    private static volatile long cachedSchoolLogoMtime = Long.MIN_VALUE;

    private static Image getSchoolLogoImage() {
        Path p = Path.of(System.getProperty("user.dir"), "resources", "bupt_intl_school_logo.png");
        try {
            if (!Files.isRegularFile(p)) {
                return null;
            }
            long mtime = Files.getLastModifiedTime(p).toMillis();
            if (cachedSchoolLogo != null && mtime == cachedSchoolLogoMtime) {
                return cachedSchoolLogo;
            }
            synchronized (TaTopNavigationPanel.class) {
                if (cachedSchoolLogo != null && mtime == cachedSchoolLogoMtime) {
                    return cachedSchoolLogo;
                }
                cachedSchoolLogo = ImageIO.read(p.toFile());
                cachedSchoolLogoMtime = mtime;
                return cachedSchoolLogo;
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    public enum Active {
        HOME,
        PROFILE,
        JOBS,
        /** Admin shell (four centered portal pills). */
        ADMIN_HOME,
        ADMIN_WORKLOAD,
        ADMIN_STATISTICS,
        ADMIN_AI,
        /** MO operator shell (three centered portal pills). */
        MO_HOME,
        MO_JOBS,
        MO_REVIEW
    }

    public enum NavStyle {
        STANDARD,
        PORTAL_PURPLE_GRADIENT
    }

    /**
     * Which portal center strip to build when {@link NavStyle#PORTAL_PURPLE_GRADIENT} is selected.
     */
    public enum PortalChromeVariant {
        /** Default TA shell: Home, Profile, Job Applications. */
        STANDARD_TA_THREE,
        ADMIN_FOUR,
        MO_THREE
    }

    public interface Actions {
        void onHome();

        void onProfileModule();

        void onJobApplicationModule();

        /** Fourth portal tab (Admin: AI Analysis). Default no-op for TA shells. */
        default void onFourthModule() {
        }

        void onLogout();
    }

    private static final Color PORTAL_PRIMARY = new Color(109, 77, 235);   // #6D4DEB
    private static final Color PORTAL_DEEP = new Color(79, 53, 217);       // #4F35D9
    private static final Color PORTAL_RIGHT = new Color(142, 107, 245);
    private static final Color PORTAL_PILL_TEXT = PORTAL_PRIMARY;
    private static final Color PORTAL_TEXT_WHITE = Color.WHITE;
    private static final Color PORTAL_TEXT_SOFT = new Color(255, 255, 255, 222);

    /** Shorter bar; school block uses larger type (see HTML font-size). */
    private static final int PORTAL_BAR_H = 72;
    private static final int PORTAL_NAV_FONT_PT = 12;
    private static final int PORTAL_LINK_FONT_PT = 12;
    private static final int PORTAL_TITLE_MAX_W = 260;

    private final Actions actions;
    private final Supplier<String> userLineSupplier;
    private final NavStyle navStyle;
    private final PortalChromeVariant portalChrome;

    private JButton navHome;
    private JButton navProfile;
    private JButton navJobs;
    /** Non-null only when {@link #portalChrome} is {@link PortalChromeVariant#ADMIN_FOUR}. */
    private JButton navFourth;
    private JButton logoutBtn;
    private JLabel userLabel;

    private static final String SHORTCUT_HOME = "TaTopNavigationPanel.shortcutHome";
    private static final String SHORTCUT_PROFILE = "TaTopNavigationPanel.shortcutProfile";
    private static final String SHORTCUT_JOBS = "TaTopNavigationPanel.shortcutJobs";
    private static final String SHORTCUT_FOURTH = "TaTopNavigationPanel.shortcutFourth";

    private JRootPane shortcutRoot;

    public TaTopNavigationPanel(Actions actions, Supplier<String> userLineSupplier, Active initialActive) {
        this(actions, userLineSupplier, initialActive, NavStyle.STANDARD);
    }

    public TaTopNavigationPanel(Actions actions, Supplier<String> userLineSupplier, Active initialActive, NavStyle navStyle) {
        this(actions, userLineSupplier, initialActive, navStyle, PortalChromeVariant.STANDARD_TA_THREE);
    }

    /**
     * @param portalChrome layout of the purple portal bar when {@link NavStyle#PORTAL_PURPLE_GRADIENT} is used.
     */
    public TaTopNavigationPanel(Actions actions, Supplier<String> userLineSupplier, Active initialActive,
                                NavStyle navStyle, PortalChromeVariant portalChrome) {
        this.actions = actions;
        this.userLineSupplier = userLineSupplier != null ? userLineSupplier : () -> "";
        this.navStyle = navStyle != null ? navStyle : NavStyle.STANDARD;
        this.portalChrome = portalChrome != null ? portalChrome : PortalChromeVariant.STANDARD_TA_THREE;

        if (this.navStyle == NavStyle.PORTAL_PURPLE_GRADIENT) {
            switch (this.portalChrome) {
                case ADMIN_FOUR -> initPortalPurpleAdmin(initialActive);
                case MO_THREE -> initPortalPurpleMo(initialActive);
                case STANDARD_TA_THREE -> initPortalPurple(initialActive);
            }
        } else {
            initStandard(initialActive);
        }
    }

    private void initStandard(Active initialActive) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(0, 8, 0, 8)
        ));
        setPreferredSize(new Dimension(10, 52));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel title = new JLabel("TA System");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(Color.BLACK);
        brand.add(title);
        add(brand);

        add(spacer(18));

        navHome = addStandardNavButton("Home", actions::onHome);
        navProfile = addStandardNavButton("Profile Module", actions::onProfileModule);
        navJobs = addStandardNavButton("Job Application Module", actions::onJobApplicationModule);

        add(Box.createHorizontalStrut(28));

        userLabel = new JLabel();
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        userLabel.setForeground(Theme.NAV_TEXT_SECONDARY);
        add(userLabel);

        logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(false);
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setBorder(new EmptyBorder(4, 10, 4, 10));
        logoutBtn.setForeground(Theme.NAV_TEXT_MUTED);
        logoutBtn.setHorizontalAlignment(SwingConstants.RIGHT);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> actions.onLogout());
        add(logoutBtn);

        bindStandardMnemonics();
        refreshUserLabel();
        setActive(initialActive);
    }

    private void initPortalPurple(Active initialActive) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 18, 4, 18));
        setPreferredSize(new Dimension(10, PORTAL_BAR_H));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        west.setOpaque(false);
        west.add(new LogoBadgePanel());

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.setBorder(new EmptyBorder(0, 0, 0, 0));

        String schoolPlain = "BUPT International School";
        JLabel school = new JLabel("<html><body style='margin:0;padding:0;'><div style=\"width:" + PORTAL_TITLE_MAX_W
                + "px;color:#ffffff;font-family:'Segoe UI';font-weight:bold;font-size:15px;line-height:1.2;\">"
                + escapeHtml(schoolPlain) + "</div></body></html>");

        JLabel portal = new JLabel("TEACHING ASSISTANT PORTAL");
        portal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        portal.setForeground(PORTAL_TEXT_SOFT);

        titles.add(school);
        titles.add(Box.createVerticalStrut(1));
        titles.add(portal);
        west.add(titles);

        navHome = portalNavButton("Home", PortalNavGlyphs.home(PORTAL_TEXT_WHITE), actions::onHome);
        navProfile = portalNavButton("Profile", PortalNavGlyphs.user(PORTAL_TEXT_WHITE), actions::onProfileModule);
        navJobs = portalNavButton("Job Applications", PortalNavGlyphs.briefcase(PORTAL_TEXT_WHITE), actions::onJobApplicationModule);
        navProfile.setToolTipText("My Profile");
        navJobs.setToolTipText("Job Applications");

        // Single horizontal row; min width = pref so BoxLayout does not squeeze pill text to "J…".
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.add(Box.createHorizontalGlue());
        center.add(navHome);
        center.add(Box.createHorizontalStrut(8));
        center.add(navProfile);
        center.add(Box.createHorizontalStrut(8));
        center.add(navJobs);
        center.add(Box.createHorizontalGlue());

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);

        userLabel = new JLabel();
        userLabel.setForeground(PORTAL_TEXT_SOFT);
        userLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 70));
                int x = getWidth() / 2;
                g2.drawLine(x, 2, x, getHeight() - 2);
                g2.dispose();
            }
        };
        separator.setOpaque(false);
        separator.setPreferredSize(new Dimension(1, 22));

        logoutBtn = portalLinkButton("Sign Out", PortalNavGlyphs.signOut(PORTAL_TEXT_WHITE), actions::onLogout);

        east.add(userLabel);
        east.add(separator);
        east.add(logoutBtn);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        add(west, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 8, 0, 8);
        add(center, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.LINE_END;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 0, 0, 0);
        add(east, gc);

        bindPortalMnemonics();
        refreshUserLabel();
        setActive(initialActive);
        portalLockPreferredNavSizes();
    }

    private void initPortalPurpleAdmin(Active initialActive) {
        Active initial = normalizeAdminActive(initialActive);

        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 18, 4, 18));
        setPreferredSize(new Dimension(10, PORTAL_BAR_H));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        west.setOpaque(false);
        west.add(new LogoBadgePanel());

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.setBorder(new EmptyBorder(0, 0, 0, 0));

        String schoolPlain = "BUPT International School";
        JLabel school = new JLabel("<html><body style='margin:0;padding:0;'><div style=\"width:" + PORTAL_TITLE_MAX_W
                + "px;color:#ffffff;font-family:'Segoe UI';font-weight:bold;font-size:15px;line-height:1.2;\">"
                + escapeHtml(schoolPlain) + "</div></body></html>");

        JLabel portal = new JLabel("TEACHING ASSISTANT PORTAL");
        portal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        portal.setForeground(PORTAL_TEXT_SOFT);

        titles.add(school);
        titles.add(Box.createVerticalStrut(1));
        titles.add(portal);
        west.add(titles);

        navHome = portalNavButton("Home", PortalNavGlyphs.home(PORTAL_TEXT_WHITE), actions::onHome);
        navProfile = portalNavButton("TA Workload", PortalNavGlyphs.listStack(PORTAL_TEXT_WHITE), actions::onProfileModule);
        navJobs = portalNavButton("Statistics", PortalNavGlyphs.barChart(PORTAL_TEXT_WHITE), actions::onJobApplicationModule);
        navFourth = portalNavButton("AI Analysis", PortalNavGlyphs.sparkle(PORTAL_TEXT_WHITE), actions::onFourthModule);
        navProfile.setToolTipText("TA Workload");
        navJobs.setToolTipText("Statistics");
        navFourth.setToolTipText("AI Analysis");

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.add(Box.createHorizontalGlue());
        center.add(navHome);
        center.add(Box.createHorizontalStrut(8));
        center.add(navProfile);
        center.add(Box.createHorizontalStrut(8));
        center.add(navJobs);
        center.add(Box.createHorizontalStrut(8));
        center.add(navFourth);
        center.add(Box.createHorizontalGlue());

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);

        userLabel = new JLabel();
        userLabel.setForeground(PORTAL_TEXT_SOFT);
        userLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 70));
                int x = getWidth() / 2;
                g2.drawLine(x, 2, x, getHeight() - 2);
                g2.dispose();
            }
        };
        separator.setOpaque(false);
        separator.setPreferredSize(new Dimension(1, 22));

        logoutBtn = portalLinkButton("Sign Out", PortalNavGlyphs.signOut(PORTAL_TEXT_WHITE), actions::onLogout);

        east.add(userLabel);
        east.add(separator);
        east.add(logoutBtn);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        add(west, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 8, 0, 8);
        add(center, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.LINE_END;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 0, 0, 0);
        add(east, gc);

        bindPortalMnemonicsAdmin();
        refreshUserLabel();
        setActive(initial);
        portalLockPreferredNavSizes();
    }

    private void initPortalPurpleMo(Active initialActive) {
        Active initial = normalizeMoActive(initialActive);

        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 18, 4, 18));
        setPreferredSize(new Dimension(10, PORTAL_BAR_H));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        west.setOpaque(false);
        west.add(new LogoBadgePanel());

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.setBorder(new EmptyBorder(0, 0, 0, 0));

        String schoolPlain = "BUPT International School";
        JLabel school = new JLabel("<html><body style='margin:0;padding:0;'><div style=\"width:" + PORTAL_TITLE_MAX_W
                + "px;color:#ffffff;font-family:'Segoe UI';font-weight:bold;font-size:15px;line-height:1.2;\">"
                + escapeHtml(schoolPlain) + "</div></body></html>");

        JLabel portal = new JLabel("JOB & APPLICATIONS PORTAL");
        portal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        portal.setForeground(PORTAL_TEXT_SOFT);

        titles.add(school);
        titles.add(Box.createVerticalStrut(1));
        titles.add(portal);
        west.add(titles);

        navHome = portalNavButton("Home", PortalNavGlyphs.home(PORTAL_TEXT_WHITE), actions::onHome);
        navProfile = portalNavButton("Job Management", PortalNavGlyphs.briefcase(PORTAL_TEXT_WHITE), actions::onProfileModule);
        navJobs = portalNavButton("Application Review", PortalNavGlyphs.listStack(PORTAL_TEXT_WHITE), actions::onJobApplicationModule);
        navFourth = null;
        navProfile.setToolTipText("Job Management");
        navJobs.setToolTipText("Application Review");

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.add(Box.createHorizontalGlue());
        center.add(navHome);
        center.add(Box.createHorizontalStrut(8));
        center.add(navProfile);
        center.add(Box.createHorizontalStrut(8));
        center.add(navJobs);
        center.add(Box.createHorizontalGlue());

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);

        userLabel = new JLabel();
        userLabel.setForeground(PORTAL_TEXT_SOFT);
        userLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 70));
                int x = getWidth() / 2;
                g2.drawLine(x, 2, x, getHeight() - 2);
                g2.dispose();
            }
        };
        separator.setOpaque(false);
        separator.setPreferredSize(new Dimension(1, 22));

        logoutBtn = portalLinkButton("Sign Out", PortalNavGlyphs.signOut(PORTAL_TEXT_WHITE), actions::onLogout);

        east.add(userLabel);
        east.add(separator);
        east.add(logoutBtn);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        add(west, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 8, 0, 8);
        add(center, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.LINE_END;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 0, 0, 0);
        add(east, gc);

        bindPortalMnemonicsMo();
        refreshUserLabel();
        setActive(initial);
        portalLockPreferredNavSizes();
    }

    private static Active normalizeMoActive(Active initialActive) {
        return switch (initialActive) {
            case MO_HOME, MO_JOBS, MO_REVIEW -> initialActive;
            default -> Active.MO_HOME;
        };
    }

    private static Active normalizeAdminActive(Active initialActive) {
        return switch (initialActive) {
            case ADMIN_HOME, ADMIN_WORKLOAD, ADMIN_STATISTICS, ADMIN_AI -> initialActive;
            default -> Active.ADMIN_HOME;
        };
    }

    /** After fonts/icons settle, lock min=max=pref so BoxLayout cannot clip pill labels. */
    private void portalLockPreferredNavSizes() {
        JButton[] buttons = portalChrome == PortalChromeVariant.ADMIN_FOUR
                ? new JButton[] { navHome, navProfile, navJobs, navFourth }
                : new JButton[] { navHome, navProfile, navJobs };
        for (JButton b : buttons) {
            if (b == null) {
                continue;
            }
            Dimension p = b.getPreferredSize();
            b.setMinimumSize(new Dimension(p.width, p.height));
            b.setMaximumSize(new Dimension(p.width, p.height));
        }
        revalidate();
    }

    private void bindStandardMnemonics() {
        navHome.setMnemonic(KeyEvent.VK_H);
        navHome.setDisplayedMnemonicIndex(0);
        navProfile.setMnemonic(KeyEvent.VK_P);
        navProfile.setDisplayedMnemonicIndex(0);
        navJobs.setMnemonic(KeyEvent.VK_J);
        navJobs.setDisplayedMnemonicIndex(0);
        logoutBtn.setMnemonic(KeyEvent.VK_L);
        logoutBtn.setDisplayedMnemonicIndex(0);
    }

    private void bindPortalMnemonics() {
        navHome.setMnemonic(KeyEvent.VK_H);
        navHome.setDisplayedMnemonicIndex(0);
        navProfile.setMnemonic(KeyEvent.VK_P);
        navProfile.setDisplayedMnemonicIndex(0);
        navJobs.setMnemonic(KeyEvent.VK_J);
        navJobs.setDisplayedMnemonicIndex(0);
        logoutBtn.setMnemonic(KeyEvent.VK_O);
        logoutBtn.setDisplayedMnemonicIndex(5);
    }

    private void bindPortalMnemonicsAdmin() {
        navHome.setMnemonic(KeyEvent.VK_H);
        navHome.setDisplayedMnemonicIndex(0);
        navProfile.setMnemonic(KeyEvent.VK_W);
        navProfile.setDisplayedMnemonicIndex(3);
        navJobs.setMnemonic(KeyEvent.VK_S);
        navJobs.setDisplayedMnemonicIndex(0);
        navFourth.setMnemonic(KeyEvent.VK_A);
        navFourth.setDisplayedMnemonicIndex(0);
        logoutBtn.setMnemonic(KeyEvent.VK_O);
        logoutBtn.setDisplayedMnemonicIndex(5);
    }

    private void bindPortalMnemonicsMo() {
        navHome.setMnemonic(KeyEvent.VK_H);
        navHome.setDisplayedMnemonicIndex(0);
        navProfile.setMnemonic(KeyEvent.VK_M);
        navProfile.setDisplayedMnemonicIndex(4);
        navJobs.setMnemonic(KeyEvent.VK_A);
        navJobs.setDisplayedMnemonicIndex(0);
        logoutBtn.setMnemonic(KeyEvent.VK_O);
        logoutBtn.setDisplayedMnemonicIndex(5);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> {
            if (!isDisplayable()) {
                return;
            }
            JRootPane root = getRootPane();
            if (root != null && shortcutRoot != root) {
                unregisterGlobalShortcuts(shortcutRoot);
                shortcutRoot = root;
                registerGlobalShortcuts(root);
            }
        });
    }

    @Override
    public void removeNotify() {
        unregisterGlobalShortcuts(shortcutRoot);
        shortcutRoot = null;
        super.removeNotify();
    }

    private void registerGlobalShortcuts(JRootPane root) {
        if (root == null) {
            return;
        }
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, menuMask), SHORTCUT_HOME);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, menuMask), SHORTCUT_PROFILE);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, menuMask), SHORTCUT_JOBS);
        am.put(SHORTCUT_HOME, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actions.onHome();
            }
        });
        am.put(SHORTCUT_PROFILE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actions.onProfileModule();
            }
        });
        am.put(SHORTCUT_JOBS, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actions.onJobApplicationModule();
            }
        });
        KeyStroke alt1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK);
        KeyStroke alt2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK);
        KeyStroke alt3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK);
        im.put(alt1, SHORTCUT_HOME);
        im.put(alt2, SHORTCUT_PROFILE);
        im.put(alt3, SHORTCUT_JOBS);
        if (portalChrome == PortalChromeVariant.ADMIN_FOUR) {
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, menuMask), SHORTCUT_FOURTH);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_DOWN_MASK), SHORTCUT_FOURTH);
            am.put(SHORTCUT_FOURTH, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actions.onFourthModule();
                }
            });
        }
    }

    private void unregisterGlobalShortcuts(JRootPane root) {
        if (root == null) {
            return;
        }
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_1, menuMask));
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_2, menuMask));
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_3, menuMask));
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK));
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK));
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK));
        if (portalChrome == PortalChromeVariant.ADMIN_FOUR) {
            im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_4, menuMask));
            im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_DOWN_MASK));
            am.remove(SHORTCUT_FOURTH);
        }
        am.remove(SHORTCUT_HOME);
        am.remove(SHORTCUT_PROFILE);
        am.remove(SHORTCUT_JOBS);
    }

    public static TaTopNavigationPanel forAppFrame(AppFrame app, Active initialActive) {
        Actions actions = new Actions() {
            @Override
            public void onHome() {
                app.showRoute(AppFrame.ROUTE_DASHBOARD);
            }

            @Override
            public void onProfileModule() {
                app.showRoute(AppFrame.ROUTE_PROFILE);
            }

            @Override
            public void onJobApplicationModule() {
                app.openJobApplicationPortal("jobs");
            }

            @Override
            public void onLogout() {
                app.confirmLogout();
            }
        };
        return new TaTopNavigationPanel(actions, () -> userLineForAppFrame(app), initialActive,
                NavStyle.PORTAL_PURPLE_GRADIENT);
    }

    private static String userLineForAppFrame(AppFrame app) {
        String u = app.authenticatedUsername();
        if (u != null && !u.isBlank()) {
            return "Logged in: " + u;
        }
        ProfileData p = app.profile();
        String name = p != null ? p.fullName : null;
        if (name != null && !name.isBlank()) {
            return "Logged in: " + name;
        }
        return "";
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (navStyle != NavStyle.PORTAL_PURPLE_GRADIENT) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, PORTAL_DEEP, w, 0, PORTAL_RIGHT);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // Soft top highlight to avoid a flat block while keeping the bar saturated.
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 30), 0, h, new Color(255, 255, 255, 0)));
        g2.fillRect(0, 0, w, h);

        // Subtle right-side glow, similar to the reference mockup.
        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillOval(w - 260, -120, 380, 260);

        g2.dispose();
        super.paintComponent(g);
    }

    public void setActive(Active active) {
        if (navStyle == NavStyle.PORTAL_PURPLE_GRADIENT) {
            switch (portalChrome) {
                case ADMIN_FOUR -> stylePortalNavAdmin(active);
                case MO_THREE -> stylePortalNavMo(active);
                case STANDARD_TA_THREE -> stylePortalNav(active);
            }
            portalLockPreferredNavSizes();
        } else {
            styleStandardNavButton(navHome, active == Active.HOME);
            styleStandardNavButton(navProfile, active == Active.PROFILE);
            styleStandardNavButton(navJobs, active == Active.JOBS);
        }
    }

    private void stylePortalNav(Active active) {
        boolean home = active == Active.HOME;
        boolean profile = active == Active.PROFILE;
        boolean jobs = active == Active.JOBS;

        Color activeInk = PORTAL_PILL_TEXT;
        Color inactiveInk = PORTAL_TEXT_WHITE;

        PortalNavButton homeBtn = (PortalNavButton) navHome;
        PortalNavButton profileBtn = (PortalNavButton) navProfile;
        PortalNavButton jobsBtn = (PortalNavButton) navJobs;

        homeBtn.setPillSelected(home);
        profileBtn.setPillSelected(profile);
        jobsBtn.setPillSelected(jobs);

        homeBtn.setIcon(PortalNavGlyphs.home(home ? activeInk : inactiveInk));
        profileBtn.setIcon(PortalNavGlyphs.user(profile ? activeInk : inactiveInk));
        jobsBtn.setIcon(PortalNavGlyphs.briefcase(jobs ? activeInk : inactiveInk));
    }

    private void stylePortalNavAdmin(Active active) {
        Active a = normalizeAdminActive(active);
        boolean home = a == Active.ADMIN_HOME;
        boolean workload = a == Active.ADMIN_WORKLOAD;
        boolean statistics = a == Active.ADMIN_STATISTICS;
        boolean ai = a == Active.ADMIN_AI;

        Color activeInk = PORTAL_PILL_TEXT;
        Color inactiveInk = PORTAL_TEXT_WHITE;

        PortalNavButton homeBtn = (PortalNavButton) navHome;
        PortalNavButton workloadBtn = (PortalNavButton) navProfile;
        PortalNavButton statsBtn = (PortalNavButton) navJobs;
        PortalNavButton aiBtn = (PortalNavButton) navFourth;

        homeBtn.setPillSelected(home);
        workloadBtn.setPillSelected(workload);
        statsBtn.setPillSelected(statistics);
        aiBtn.setPillSelected(ai);

        homeBtn.setIcon(PortalNavGlyphs.home(home ? activeInk : inactiveInk));
        workloadBtn.setIcon(PortalNavGlyphs.listStack(workload ? activeInk : inactiveInk));
        statsBtn.setIcon(PortalNavGlyphs.barChart(statistics ? activeInk : inactiveInk));
        aiBtn.setIcon(PortalNavGlyphs.sparkle(ai ? activeInk : inactiveInk));
    }

    private void stylePortalNavMo(Active active) {
        Active a = normalizeMoActive(active);
        boolean home = a == Active.MO_HOME;
        boolean jobs = a == Active.MO_JOBS;
        boolean review = a == Active.MO_REVIEW;

        Color activeInk = PORTAL_PILL_TEXT;
        Color inactiveInk = PORTAL_TEXT_WHITE;

        PortalNavButton homeBtn = (PortalNavButton) navHome;
        PortalNavButton jobBtn = (PortalNavButton) navProfile;
        PortalNavButton reviewBtn = (PortalNavButton) navJobs;

        homeBtn.setPillSelected(home);
        jobBtn.setPillSelected(jobs);
        reviewBtn.setPillSelected(review);

        homeBtn.setIcon(PortalNavGlyphs.home(home ? activeInk : inactiveInk));
        jobBtn.setIcon(PortalNavGlyphs.briefcase(jobs ? activeInk : inactiveInk));
        reviewBtn.setIcon(PortalNavGlyphs.listStack(review ? activeInk : inactiveInk));
    }

    public void refreshUserLabel() {
        if (navStyle == NavStyle.PORTAL_PURPLE_GRADIENT) {
            userLabel.setText(formatPortalUserLine(userLineSupplier.get()));
        } else {
            userLabel.setText(userLineSupplier.get());
        }
    }

    public void refresh(Active active) {
        refreshUserLabel();
        setActive(active);
    }

    private static String formatPortalUserLine(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String value = raw.trim();
        String user = value;
        String lower = value.toLowerCase();
        if (lower.startsWith("logged in:")) {
            user = value.substring(value.indexOf(':') + 1).trim();
        } else if (lower.startsWith("logged in as")) {
            user = value.substring("logged in as".length()).trim();
        }
        if (user.isBlank()) {
            user = value;
        }
        return "<html><body width=\"132\"><div style='text-align:right; line-height:112%;'>"
                + "<span style='font-size:9px; font-weight:500;'>Logged in as</span><br>"
                + "<span style='font-size:11px; font-weight:700;'>" + escapeHtml(user) + "</span>"
                + "</div></body></html>";
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static void styleStandardNavButton(JButton b, boolean active) {
        b.setForeground(active ? Color.BLACK : Theme.NAV_TEXT_SECONDARY);
        b.setFont(active ? new Font(Font.SANS_SERIF, Font.BOLD, 14) : new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    }

    private static JPanel spacer(int width) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(width, 1));
        return p;
    }

    private JButton addStandardNavButton(String label, Runnable action) {
        JButton b = new JButton(label);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(4, 10, 4, 10));
        b.setBorderPainted(false);
        b.setForeground(Theme.NAV_TEXT_SECONDARY);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        add(b);
        return b;
    }

    private static JButton portalNavButton(String text, javax.swing.Icon icon, Runnable action) {
        PortalNavButton b = new PortalNavButton(text, icon);
        b.setFont(new Font("Segoe UI", Font.PLAIN, PORTAL_NAV_FONT_PT));
        b.setForeground(PORTAL_TEXT_WHITE);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(3, 8, 3, 8));
        b.setHorizontalTextPosition(SwingConstants.RIGHT);
        b.setIconTextGap(6);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    private static JButton portalLinkButton(String text, javax.swing.Icon icon, Runnable action) {
        PortalNavButton b = new PortalNavButton(text, icon);
        b.setFont(new Font("Segoe UI", Font.PLAIN, PORTAL_LINK_FONT_PT));
        b.setForeground(PORTAL_TEXT_WHITE);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(4, 8, 4, 8));
        b.setHorizontalTextPosition(SwingConstants.RIGHT);
        b.setIconTextGap(6);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    private static final class LogoBadgePanel extends JPanel {
        LogoBadgePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(40, 40));
            setMinimumSize(new Dimension(40, 40));
            setMaximumSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(0, 0, 0, 28));
            g2.fillRoundRect(2, 4, w - 5, h - 5, 12, 12);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 3, h - 3, 12, 12);

            Image logo = getSchoolLogoImage();
            if (logo != null) {
                int pad = 2;
                int boxW = w - 3;
                int boxH = h - 3;
                Shape clip = g2.getClip();
                g2.setClip(new RoundRectangle2D.Float(0, 0, boxW, boxH, 12, 12));
                g2.drawImage(logo, pad, pad, boxW - 2 * pad, boxH - 2 * pad, null);
                g2.setClip(clip);
            } else {
                PortalNavGlyphs.paintGraduationCap(g2, w / 2, h / 2 + 1, 20, PORTAL_PRIMARY);
            }
            g2.dispose();
        }
    }

    private static final class PortalNavButton extends JButton {
        private boolean pillSelected;
        private boolean hover;

        PortalNavButton(String text, javax.swing.Icon icon) {
            super(text, icon);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setPillSelected(boolean selected) {
            this.pillSelected = selected;
            setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, PORTAL_NAV_FONT_PT));
            setForeground(selected ? PORTAL_PILL_TEXT : PORTAL_TEXT_WHITE);
            invalidate();
            Dimension p = getPreferredSize();
            setMinimumSize(new Dimension(p.width, p.height));
            setMaximumSize(new Dimension(p.width, p.height));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            if (pillSelected) {
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(2, 4, w - 4, h - 4, 18, 18);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 1, w - 2, h - 3, 18, 18);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 28));
                g2.fillRoundRect(1, 3, w - 2, h - 6, 16, 16);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class PortalNavGlyphs {
        private PortalNavGlyphs() {
        }

        static javax.swing.Icon home(Color c) {
            return glyph(18, (g2, s) -> {
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 3;
                int roofY = pad + 6;
                g2.drawLine(s / 2, pad + 2, pad, roofY);
                g2.drawLine(s / 2, pad + 2, s - pad, roofY);
                g2.drawRoundRect(pad + 2, roofY, s - 2 * pad - 4, s - roofY - pad, 2, 2);
            });
        }

        static javax.swing.Icon user(Color c) {
            return glyph(18, (g2, s) -> {
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Keep head + shoulders inside the 18×18 box (old arc extended past the bottom and was clipped).
                g2.drawOval(s / 2 - 4, 2, 8, 7);
                g2.drawArc(s / 2 - 7, 9, 14, 8, 200, 140);
            });
        }

        static javax.swing.Icon briefcase(Color c) {
            return glyph(18, (g2, s) -> {
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(4, 7, s - 8, s - 10, 3, 3);
                g2.drawLine(4, 11, s - 4, 11);
                g2.drawLine(s / 2 - 4, 7, s / 2 - 4, 5);
                g2.drawLine(s / 2 + 4, 7, s / 2 + 4, 5);
                g2.drawLine(s / 2 - 4, 5, s / 2 + 4, 5);
            });
        }

        static javax.swing.Icon listStack(Color c) {
            return glyph(18, (g2, s) -> {
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int left = 4;
                int right = s - 4;
                g2.drawLine(left, 5, right, 5);
                g2.drawLine(left, 9, right - 3, 9);
                g2.drawLine(left, 13, right - 5, 13);
            });
        }

        static javax.swing.Icon barChart(Color c) {
            return glyph(18, (g2, s) -> {
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int base = s - 4;
                g2.drawLine(3, base, s - 3, base);
                g2.fillRect(5, base - 6, 3, 6);
                g2.fillRect(9, base - 10, 3, 10);
                g2.fillRect(13, base - 4, 3, 4);
            });
        }

        static javax.swing.Icon sparkle(Color c) {
            return glyph(18, (g2, s) -> {
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.55f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = s / 2;
                int cy = s / 2;
                g2.drawLine(cx, cy - 6, cx, cy + 6);
                g2.drawLine(cx - 6, cy, cx + 6, cy);
                g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                g2.drawLine(cx - 4, cy + 4, cx + 4, cy - 4);
            });
        }

        static javax.swing.Icon signOut(Color c) {
            return glyph(18, (g2, s) -> {
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(7, s / 2, s - 4, s / 2);
                g2.drawLine(s - 4, s / 2, s - 8, s / 2 - 4);
                g2.drawLine(s - 4, s / 2, s - 8, s / 2 + 4);
                g2.drawLine(4, 4, 4, s - 4);
                g2.drawLine(4, 4, 9, 4);
                g2.drawLine(4, s - 4, 9, s - 4);
            });
        }

        static void paintGraduationCap(Graphics2D g2, int cx, int cy, int size, Color c) {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int half = size / 2;
            int top = cy - size / 4;
            int mid = cy;

            java.awt.Polygon cap = new java.awt.Polygon();
            cap.addPoint(cx, top - 6);
            cap.addPoint(cx + half, top + 2);
            cap.addPoint(cx, top + 10);
            cap.addPoint(cx - half, top + 2);
            g2.fillPolygon(cap);

            g2.fillRoundRect(cx - size / 4, mid + 1, size / 2, size / 5, 5, 5);
            g2.drawLine(cx + half - 3, top + 3, cx + half - 3, mid + 13);
            g2.fillOval(cx + half - 6, mid + 12, 6, 6);
        }

        private interface Painter {
            void paint(Graphics2D g2, int size);
        }

        private static javax.swing.Icon glyph(int size, Painter painter) {
            return new javax.swing.Icon() {
                @Override
                public void paintIcon(Component comp, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.translate(x, y);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    painter.paint(g2, size);
                    g2.dispose();
                }

                @Override
                public int getIconWidth() {
                    return size;
                }

                @Override
                public int getIconHeight() {
                    return size;
                }
            };
        }
    }
}
