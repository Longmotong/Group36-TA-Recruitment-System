package com.mojobsystem.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Sprint 1: job-detail spec links here; full review module is out of MO sprint scope.
 */
public class ApplicationReviewPlaceholderFrame extends JFrame {

    private final MyJobsFrame moJobsFrame;

    public ApplicationReviewPlaceholderFrame(String fromJobId) {
        this(null, fromJobId);
    }

    public ApplicationReviewPlaceholderFrame(MyJobsFrame moJobsFrame, String fromJobId) {
        this.moJobsFrame = moJobsFrame;
        setTitle("MO System - Application Review");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        if (moJobsFrame != null) {
            MoFrameGeometry.applyMatching(moJobsFrame, this);
        } else {
            MoFrameGeometry.apply(this);
        }
        getContentPane().setBackground(MoUiTheme.PAGE_BG);
        setLayout(new BorderLayout());

        add(NavigationPanel.create(NavigationPanel.Tab.APPLICATION_REVIEW, navActions()), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBackground(MoUiTheme.PAGE_BG);
        main.add(buildPageHeaderStrip(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(26, MoUiTheme.GUTTER, 40, MoUiTheme.GUTTER));

        String extra = fromJobId == null || fromJobId.isBlank()
                ? ""
                : "<br/><br/>Context from job record: <b>" + fromJobId + "</b>";
        JLabel msg = new JLabel("<html><div style='width:560px;color:#666666'>This area will host applicant queues, CV access, "
                + "and accept/reject actions per your backlog (MO-02 / MO-03). "
                + "Data for applications already lives under <code>data/applications/</code>."
                + extra + "</div></html>");
        msg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(Box.createVerticalStrut(6));
        body.add(msg);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MoUiTheme.BORDER));
        wrap.add(body, BorderLayout.CENTER);
        main.add(wrap, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        if (moJobsFrame == null) {
            MoFrameGeometry.finishTopLevelFrame(this);
        }
    }

    private JPanel buildPageHeaderStrip() {
        JPanel strip = new JPanel(new BorderLayout(20, 0));
        strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MoUiTheme.BORDER),
                new EmptyBorder(18, MoUiTheme.GUTTER, 20, MoUiTheme.GUTTER)
        ));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JButton back = new JButton("Back");
        back.setFocusPainted(false);
        back.setContentAreaFilled(false);
        back.setBorder(new EmptyBorder(6, 4, 6, 4));
        back.setForeground(MoUiTheme.TEXT_SECONDARY);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> {
            dispose();
            if (moJobsFrame != null) {
                moJobsFrame.reloadJobsFromRepository();
                moJobsFrame.setVisible(true);
            } else {
                new MyJobsFrame().setVisible(true);
            }
        });
        leftCol.add(back);
        leftCol.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("Application Review");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(MoUiTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(title);

        leftCol.add(Box.createVerticalStrut(6));
        JLabel sub = new JLabel("Review applicant queues and manage decisions");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        sub.setForeground(MoUiTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(sub);

        strip.add(leftCol, BorderLayout.CENTER);
        return strip;
    }

    private NavigationPanel.Actions navActions() {
        return new NavigationPanel.Actions(
                () -> MoFrameGeometry.navigateReplace(this, () -> new MoDashboardFrame().setVisible(true)),
                () -> MoFrameGeometry.navigateReplace(this, () -> new MyJobsFrame().setVisible(true)),
                () -> { },
                () -> System.exit(0)
        );
    }

}
