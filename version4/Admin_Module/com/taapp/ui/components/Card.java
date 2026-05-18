package Admin_Module.com.taapp.ui.components;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import Admin_Module.com.taapp.ui.UI;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class Card extends JPanel {
    public Card() {
        setOpaque(true);
        setBackground(UI.palette().cardBg());
        setBorder(BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1));
    }
}
