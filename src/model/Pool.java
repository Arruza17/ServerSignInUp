package model;

import java.sql.Connection;
import java.sql.SQLException;
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
     * @return Pool the connection pool
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
    public static void returnConnection(Connection con) {
        connections.push(con);
    }

    /**
     * Method that gets back the connection from the collection
     *
     * @return con the connection to be returned
     */
    public static Connection getConnection() {
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

    /**
     * This method is used to close the Pool
     * @throws SQLException if any SQLException is thrown
     */
    public static void closePool() throws SQLException {
         for (int i = 0; i < connections.size(); i++) {
             Connection con = getConnection();
             con.close();
         }
    }

}
