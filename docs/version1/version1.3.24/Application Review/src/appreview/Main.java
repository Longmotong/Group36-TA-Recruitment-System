package appreview;

import appreview.data.DataRepository;
import appreview.service.ApplicationReviewService;
import appreview.ui.DesktopApp;

/**
 * Main entry point of Application Review desktop app.
 */
public class Main {
    /**
     * App startup.
     *
     * @param args command arguments
     */
    public static void main(String[] args) {
        String dataPath = "..\\data";
        if (args != null && args.length > 0) {
            dataPath = args[0];
        }
        DataRepository repository = new DataRepository(dataPath);
        ApplicationReviewService service = new ApplicationReviewService(repository);
        try {
            service.initialize();
            DesktopApp.launch(service);
        } catch (java.io.IOException ex) {
            System.out.println("Program startup failed: " + ex.getMessage());
        }
    }
}
