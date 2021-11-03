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
import model.User;

/**
 *
 * @author Yeray Sampedro
 */
public class ConnectionThread extends Thread {

    private Socket clientSocket;
    private Connectable dataReceiver;

    public ConnectionThread(Socket clientSocket, Connectable dataReceiver) {
        this.clientSocket = clientSocket;
        this.dataReceiver = dataReceiver;
        this.start();
    }

    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            DataEncapsulator de = (DataEncapsulator) ois.readObject();

            User user = de.getUser();
            if (user.getLogin() != null && user.getPassword() != null && user.getEmail() == null) {
                System.out.println(user.getLogin() + user.getPassword());
                DataEncapsulator dataSender;
                dataSender = dataReceiver.signIn(user);
                oos.writeObject(dataSender);
                oos.flush();
                Application.removeClient(this);
            } else {
                dataReceiver.signUp(user);
                de.setUser(user);
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
