package logic;

import application.Server;

import exceptions.LoginFoundException;
import exceptions.ServerDownException;
import interfaces.Connectable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DataEncapsulator;

import model.User;

/**
 *
 * @author Yeray Sampedro
 */
public class ConnectionThread extends Thread {

    private static Socket clientSocket;
    private Connectable dataReceiver;
    private static ObjectOutputStream oos = null;
    private static ObjectInputStream ois = null;

    /**
     * Thread that starts the communication between client and server
     *
     * @param clientSocket the socket where the client is being served with
     * @param dataReceiver the implementation of Connectable
     */
    public ConnectionThread(Socket clientSocket, Connectable dataReceiver) {
        this.clientSocket = clientSocket;
        this.dataReceiver = dataReceiver;
        this.start();
    }

    /**
     * Method that starts the thread and both reads the users petitions and
     * sends them back the results
     */
    public void run() {
        oos = null;
        ois = null;
        try {
            //We open the receival and sendal of data
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            //We read the petition
            DataEncapsulator de = (DataEncapsulator) ois.readObject();

            User user = de.getUser();
            //If they only have user and password, its a login
            if (user.getLogin() != null && user.getPassword() != null && user.getEmail() == null) {

                DataEncapsulator dataSender;
                //We sign in the user and send back all the data
                dataSender = dataReceiver.signIn(user);
                oos.writeObject(dataSender);
                oos.flush();
                //We disconnect the client
                Server.removeClient(this);
                //If they have all the data is a register
            } else {
                //We sign up a user
                dataReceiver.signUp(user);
                de.setUser(user);
                //Send them the confirmation message
                de.setException(new Exception("OK"));
                oos.writeObject(de);
                oos.flush();
                Server.removeClient(this);
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServerDownException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoginFoundException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void close() throws IOException {
        if (!clientSocket.isClosed()) {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            clientSocket.close();

        }
    }

}
