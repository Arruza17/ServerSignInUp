package application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import model.DataEncapsulator;

/**
 *
 * @author Yeray Sampedro
 */
public class TestClient {

    private static final int PORT = 5000;
    private static final String IP = "localhost";

    public static void main(String[] args) {
        Socket cliente;
        try {
            cliente = new Socket(IP, PORT);
            System.out.println("CLIENT: START");
            ObjectInputStream ois = new ObjectInputStream(cliente.getInputStream());
            DataEncapsulator de = (DataEncapsulator) ois.readObject();
            throw de.getException();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
