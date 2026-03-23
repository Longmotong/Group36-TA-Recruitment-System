package com.ebuko.moapp.ui;

import com.ebuko.moapp.data.DataRepository;
import com.ebuko.moapp.data.StatusConfig;
import com.ebuko.moapp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class MoAppFrame extends JFrame {
  private final DataRepository repo;
  private final StatusConfig statusConfig;

  private String currentMoUserId = "u_mo_001";

  private final CardLayout cardLayout = new CardLayout();
  private final JPanel contentPanel = new JPanel(cardLayout);

  private DashboardPanelV2 dashboardPanel;
  private ApplicationReviewPanelV2 reviewPanel;

  public MoAppFrame() {
    super("MO Desktop Application");
    this.repo = new DataRepository();
    this.statusConfig = repo.getStatusConfig();

    List<String> moUserIds = repo.listMoUserIds();
    moUserIds.sort(Comparator.naturalOrder());
    if (!moUserIds.contains(currentMoUserId) && !moUserIds.isEmpty()) {
      currentMoUserId = moUserIds.get(0);
    }

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(1200, 720);
    setLocationRelativeTo(null);

    // Header: MO System + Tabs + MO switcher + Logout
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Color.WHITE);
    header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_BORDER));

    JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
    headerLeft.setOpaque(false);
    JLabel brand = new JLabel("MO System");
    brand.setFont(brand.getFont().deriveFont(Font.BOLD, 18f));
    headerLeft.add(brand);

    JButton tabHome = new JButton("Home");
    JButton tabJob = new JButton("Job Management");
    JButton tabReview = new JButton("Application Review");
    styleTab(tabHome);
    styleTab(tabJob);
    styleTab(tabReview);

    JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
    tabs.setOpaque(false);
    tabs.add(tabHome);
    tabs.add(tabJob);
    tabs.add(tabReview);

    JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
    headerRight.setOpaque(false);
    JComboBox<String> moCombo = new JComboBox<>(moUserIds.toArray(new String[0]));
    moCombo.setSelectedItem(currentMoUserId);
    JLabel logout = new JLabel("Logout");
    logout.setForeground(new Color(90, 90, 90));
    logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logout.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        System.exit(0);
      }
    });
    headerRight.add(moCombo);
    headerRight.add(logout);

    header.add(headerLeft, BorderLayout.WEST);
    header.add(tabs, BorderLayout.CENTER);
    header.add(headerRight, BorderLayout.EAST);

    // Panels
    dashboardPanel =
        new DashboardPanelV2(
            repo,
            statusConfig,
            currentMoUserId,
            () -> cardLayout.show(contentPanel, "review"),
            () -> cardLayout.show(contentPanel, "jobMgmt")
        );

    reviewPanel = new ApplicationReviewPanelV2(repo, statusConfig, currentMoUserId, v -> cardLayout.show(contentPanel, "home"));

    contentPanel.add(dashboardPanel, "home");
    contentPanel.add(reviewPanel, "review");
    JPanel jobMgmtPlaceholder = makeJobMgmtPlaceholder();
    contentPanel.add(jobMgmtPlaceholder, "jobMgmt");

    tabHome.addActionListener(e -> {
      cardLayout.show(contentPanel, "home");
      setActiveTab(tabHome, tabJob, tabReview);
    });
    tabJob.addActionListener(e -> {
      cardLayout.show(contentPanel, "jobMgmt");
      setActiveTab(tabHome, tabJob, tabReview);
    });
    tabReview.addActionListener(e -> {
      cardLayout.show(contentPanel, "review");
      setActiveTab(tabHome, tabJob, tabReview);
    });

    moCombo.addActionListener(e -> {
      String newId = (String) moCombo.getSelectedItem();
      if (newId == null || newId.equals(currentMoUserId)) return;
      currentMoUserId = newId;
      dashboardPanel.setCurrentMoUserId(currentMoUserId);
      dashboardPanel.refresh();
      reviewPanel.setCurrentMoUserId(currentMoUserId);
      reviewPanel.refresh();
    });

    // Compose
    setLayout(new BorderLayout());
    add(header, BorderLayout.NORTH);
    add(contentPanel, BorderLayout.CENTER);

    dashboardPanel.refresh();
    reviewPanel.refresh();
    cardLayout.show(contentPanel, "home");
    setActiveTab(tabHome, tabJob, tabReview);
  }

  private JPanel makeJobMgmtPlaceholder() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBackground(Theme.APP_BG);
    JLabel label = new JLabel("Job Management module is not implemented in this version.");
    label.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    p.add(label, BorderLayout.CENTER);
    return p;
  }

  private static void styleTab(JButton btn) {
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setForeground(new Color(80, 80, 80));
  }

  private static void setActiveTab(JButton active, JButton other1, JButton other2) {
    styleTab(active);
    active.setContentAreaFilled(true);
    active.setOpaque(true);
    active.setBackground(new Color(230, 232, 237));
    styleTab(other1);
    styleTab(other2);
  }
}

