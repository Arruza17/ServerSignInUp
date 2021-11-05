package application;

import exceptions.MaxUsersException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;

import java.util.logging.Logger;

import logic.ConnectableFactory;
import logic.ConnectionThread;

import model.DataEncapsulator;

/**
 * SignIn SignUp application server main class
 *
 * @author Yeray Sampedro
 */
public class Application extends Thread {

    private static final ResourceBundle CONFIGFILE = ResourceBundle.getBundle("resources.config");
    private static final int MAX_USERS = Integer.parseInt(CONFIGFILE.getString("MaxUsers"));
    private static final int PORT = Integer.parseInt(CONFIGFILE.getString("PORT"));
    
    private static ArrayList<ConnectionThread> receiveClients = new ArrayList<>();
    
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    private static Boolean serverOn = true;
    private static ServerSocket serverSocket;

    /**
     * Method use to start the server
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //Server Socket instance
            serverSocket = new ServerSocket(PORT);
            System.out.println("SERVER: SERVER STARTED");
            Socket clientSocket;
            //Read petitions
            while (serverOn) {
                //If the last user is more than the maximum, send message
                if (receiveClients.size() > MAX_USERS) {
                    //We accept the petition
                    clientSocket = serverSocket.accept();
                    //We prepare the sending
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    DataEncapsulator de = new DataEncapsulator();
                    //We send them the error saying that they should wait
                    de.setException(new MaxUsersException());
                    oos.writeObject(de);

                } else {
                    //We accept the petition
                    clientSocket = serverSocket.accept();
                    //We open a new thread for working
                    ConnectionThread receive = new ConnectionThread(clientSocket, ConnectableFactory.getConnectable());
                    //We ad a client to the list
                    addClient(receive);
                    System.out.println("Client #" + receiveClients.size());
                }
            }

        } catch (IOException ex) {
            LOGGER.warning(ex.getMessage());
        }

    }

    /**
     * Method that adds a client from the list
     *
     * @param receive a client thread
     */
    private static synchronized void addClient(ConnectionThread receive) {
        receiveClients.add(receive);
    }

    /**
     * Method that removes a client from the list
     *
     * @param client a client thread
     */
    public static synchronized void removeClient(ConnectionThread client) {
        receiveClients.remove(client);
        System.out.println("Client disconnected");
    }

    /**
     * Method that enables or disables the server
     *
     * @param serverOn
     */
    public static void setServerOn(Boolean serverOn) {
        Application.serverOn = serverOn;
    }

}
