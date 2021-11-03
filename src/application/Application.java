package application;

import exceptions.MaxUsersException;
import exceptions.ServerDownException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import logic.ConnectableFactory;
import logic.ConnectionThread;

import model.DataEncapsulator;

/**
 *
 * @author Yeray Sampedro
 */
public class Application {

    private static final ResourceBundle CONFIGFILE = ResourceBundle.getBundle("resources.config");
    private static final int PORT = Integer.parseInt(CONFIGFILE.getString("PORT"));
    private static ArrayList<ConnectionThread> receiveClients = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    private static final int MAX_USERS = Integer.parseInt(CONFIGFILE.getString("MaxUsers"));

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("SERVER: SERVER STARTED");
            Socket clientSocket;

            while (true) {
                if (receiveClients.size() > MAX_USERS) {
                    clientSocket = serverSocket.accept();
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    DataEncapsulator de = new DataEncapsulator();
                    de.setException(new MaxUsersException());
                    oos.writeObject(de);

                } else {
                    clientSocket = serverSocket.accept();
                    ConnectionThread receive = new ConnectionThread(clientSocket, ConnectableFactory.getConnectable());
                    addClient(receive);
                    System.out.println("Client #" + receiveClients.size());

                }

             

            }
        } catch (IOException ex) {
            System.err.println(new Exception("This address is already in use, try again later"));
        }
    }

    private static synchronized void addClient(ConnectionThread receive) {
        receiveClients.add(receive);
    }

    public static synchronized void removeClient(ConnectionThread client) {
        receiveClients.remove(client);
        System.out.println("Client disconnected");
    }

}
