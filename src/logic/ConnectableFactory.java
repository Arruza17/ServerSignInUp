package logic;

import interfaces.Connectable;
import model.DAOImplementation;

/**
 * Factory of the connectable factory
 * 
 * @author Yeray Sampedro
 */
public class ConnectableFactory {

    /**
     *  Method that returns an instance of the Connectable implementation
     * @return conn the implementation itself
     */
    public static Connectable getConnectable() {
        Connectable conn = null;
        
        conn = DAOImplementation.getDAO(); 
       
        return conn;
    }


  

    

}
