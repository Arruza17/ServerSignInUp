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
     *
     */
    public BDConnection() {

        configFile = ResourceBundle.getBundle("resources.config");
        conn = configFile.getString("Conn");
        dbUser = configFile.getString("DBUser");
        dbPass = configFile.getString("DBPass");
    }

    /**
     *
     * @return
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
     *
     * @param stmt
     * @param con
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
