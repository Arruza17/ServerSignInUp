package logic;

import interfaces.Connectable;
import model.DAOImplementation;

/**
 *
 * @author Yeray Sampedro
 */
public class ConnectableFactory {

    public static Connectable getConnectable() {
        Connectable conn = null;
        
        conn = DAOImplementation.getDAO(); 
       
        return conn;
    }


  

    

}
