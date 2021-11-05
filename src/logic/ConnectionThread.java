package logic;

import application.Application;
import enumerations.UserPrivilege;
import enumerations.UserStatus;
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
import model.Pool;
import model.User;

/**
 *
 * @author Yeray Sampedro
 */
public class ConnectionThread extends Thread {

    private Socket clientSocket;
    private Connectable dataReceiver;

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
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
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
                Application.removeClient(this);
            //If they have all the data is a register
            } else {
                //We sign up a user
                dataReceiver.signUp(user);
                de.setUser(user);
                //Send them the confirmation message
                de.setException(new Exception("OK"));             
                oos.writeObject(de);
                oos.flush();
                Application.removeClient(this);
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

}
