package model;

import java.sql.Connection;
import java.util.Stack;

/**
 * Pool singleton class
 *
 * @author Yeray Sampedro
 */
public class Pool {

    private static Pool pool;
    private static Stack connections;

    private Pool() {
        connections = new Stack();
    }

    /**
     * Method that gets a Pool
     *
     * @return
     */
    public static Pool getPool() {
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }

    /**
     * Method that returns back the connection into the collection
     *
     * @param con the connection to be saved
     */
    public void returnConnection(Connection con) {
        connections.push(con);
    }

    /**
     * Method that gets back the connection from the collection
     *
     * @return con the connection to be returned
     */
    public Connection getConnection() {
        Connection con = (Connection) connections.pop();
        return con;
    }

    /**
     * Method that gets the size of the pool
     * @return size the pool size
     */
    public int getConnections() {
        return connections.size();
    }

   

}
