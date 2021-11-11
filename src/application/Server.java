package application;

import exceptions.MaxUsersException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static java.lang.System.exit;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;

import java.util.logging.Logger;

import logic.ConnectableFactory;
import logic.ConnectionThread;

import model.DataEncapsulator;
import model.Pool;

/**
 * SignIn SignUp application server main class
 *
 * @author Yeray Sampedro
 */
public class Server extends Thread {

    private static final ResourceBundle CONFIGFILE = ResourceBundle.getBundle("resources.config");
    private static final int MAX_USERS = Integer.parseInt(CONFIGFILE.getString("MaxUsers"));
    private static final int PORT = Integer.parseInt(CONFIGFILE.getString("PORT"));

    private static ArrayList<ConnectionThread> receiveClients = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static boolean serverOn = true;
    private static ServerSocket serverSocket;

    /**
     * Method use to start the server
     *
     */
    public void run() {
        try {
            //Server Socket instance
            serverSocket = new ServerSocket(PORT);
            System.out.println("SERVER: SERVER STARTED");
            Socket clientSocket;
            //Read petitions
            while (serverOn) {
                //We accept the petition              
                if (!serverSocket.isClosed()) {
                    //If the last user is more than the maximum, send message if (serverSocket.isClosed()) {
                    if (receiveClients.size() > MAX_USERS) {
                        //We prepare the sending 
                        clientSocket = serverSocket.accept();
                        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        DataEncapsulator de = new DataEncapsulator();
                        //We send them the error saying that they should wait
                        de.setException(new MaxUsersException());
                        oos.writeObject(de);
                    } else {
                         clientSocket = serverSocket.accept();
                        //We open a new thread for working
                       
                        ConnectionThread receive = new ConnectionThread(clientSocket, ConnectableFactory.getConnectable());
                        //We ad a client to the list
                        addClient(receive);
                        System.out.println("Client #" + receiveClients.size());
                    }
                }
            }

            LOGGER.info("Closing server");
            closeServer();
           
         
        } catch (IOException ex) {
            LOGGER.warning(ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
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
     * @param serverOn boolean true if the user is on
     */
    public static void setServerOn(boolean serverOn) {
        Server.serverOn = serverOn;
    }

    
    /**
     * Method that is used to close the whole server (connections and threads included)
     * @throws SQLException if any SQL exception is thrown
     * @throws IOException if any IO exception is thrown
     */
    public static void closeServer() throws SQLException, IOException {
        if (receiveClients.size() > 0) {
            Pool pool = Pool.getPool();
            pool.closePool();

            for (ConnectionThread t : receiveClients) {
                t.close();
                t.interrupt();
                removeClient(t);
            }
        }
        serverSocket.close();
    }

}
