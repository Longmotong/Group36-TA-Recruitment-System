import java.util.Scanner;
import controller.AuthController;

public class MainMenu {

    public static void main(String[] args) {

        AuthController controller = new AuthController();
        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println("\n===== TA Recruitment System =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");

            System.out.print("Select option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // clear buffer

            switch (choice) {
                case 1:
                    LoginPage.show(controller);
                    break;
                case 2:
                    RegisterPage.show(controller);
                    break;
                case 3:
                    System.out.println("Goodbye.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
