package application;

import java.io.IOException;
import static java.lang.System.exit;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SignIn SignUp application server main class
 *
 * @author Yeray Sampedro
 */
public class Application extends Thread {

    private static boolean cont = true;

    /**
     * Method that runs the server
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        while (cont) {
            System.out.println("Write EXIT to close the server");
            if (new Scanner(System.in).next().trim().equalsIgnoreCase("EXIT")) {
                server.setServerOn(false);
                server.interrupt();
                cont = false;
            }
        }
        System.out.println("Server closed");
        exit(0);
    }
}
