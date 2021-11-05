package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 *
 * @author Adrián Perez
 */
public class BDConnection {

    private final ResourceBundle configFile;
    private final String conn;
    private final String dbUser;
    private final String dbPass;

    /**
     * BDConnection constructor where it receives all the data from resources.config file
     */
    public BDConnection() {
       
        configFile = ResourceBundle.getBundle("resources.config");
        conn = configFile.getString("Conn");
        dbUser = configFile.getString("DBUser");
        dbPass = configFile.getString("DBPass");
    }

    /**
     *  Method that opens a connection to the database
     * @return con the connection object
     */
    public Connection openConnection() {

        Connection con = null;

        try {
            con = DriverManager.getConnection(conn, dbUser, dbPass);

        } catch (SQLException e) {

        }
        return con;
    }

    /**
     * Method that closes the connection and the statements
     * @param stmt the prepared statement 
     * @param con the connection
     * @throws SQLException
     */
    public void closeConnection(PreparedStatement stmt, Connection con) throws SQLException {

        if (stmt != null) {
            stmt.close();
        }
        if (con != null) {
            con.close();
        }

    }

}
