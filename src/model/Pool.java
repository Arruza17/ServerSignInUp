package model;

import java.sql.Connection;
import java.util.Stack;

/**
 *
 * @author Yeray Sampedro
 */
public class Pool {

    private static Pool pool;
    private static Stack connections;

    private Pool() {
        connections = new Stack();
    }

    public static Pool getPool() {
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }

    public void returnConnection(Connection con) {
        connections.push(con);
    }

    public Connection getConnection() {
        Connection con = (Connection) connections.pop();
        return con;
    }

    public int getConnections() {
        return connections.size();
    }

}
