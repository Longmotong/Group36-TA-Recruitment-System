import java.util.Scanner;
import controller.AuthController;

public class LoginPage {

    public static void show(AuthController controller) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n===== LOGIN PAGE =====");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        controller.handleLogin(username, password);
    }
}
