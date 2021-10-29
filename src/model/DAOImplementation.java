package model;

import com.sun.istack.internal.logging.Logger;
import enumerations.UserPrivilege;
import enumerations.UserStatus;
import exceptions.LoginFoundException;

import exceptions.ServerDownException;
import interfaces.Connectable;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ResourceBundle;

import model.User;

public class DAOImplementation implements Connectable {

    private static DAOImplementation dao;
    private Pool pool;
    private BDConnection bdc;
    private Connection con;
    private static final ResourceBundle CONFIGFILE = ResourceBundle.getBundle("resources.config");
    private static final int MAX_USERS = Integer.parseInt(CONFIGFILE.getString("MaxUsers"));
    private PreparedStatement stmt;

    final String SIGN_UP = "insert into user values(null, ?,?, ?, ?, ?, ?, now())";
    final String SIGN_IN = "Select * from user where login = ?";

    private DAOImplementation() {
        pool = Pool.getPool();
        bdc = new BDConnection();

    }

    public static DAOImplementation getDAO() {
        if (dao == null) {
            dao = new DAOImplementation();
        }
        return dao;
    }

    @Override
    public DataEncapsulator signIn(User user) throws ServerDownException {
        DataEncapsulator de = new DataEncapsulator();

        try {
            getConnection();
            ResultSet rs = null;
            stmt = con.prepareStatement(SIGN_IN);
            stmt.setString(1, user.getLogin());
            rs = stmt.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("login"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("fullName"));
                user.setPrivilege(UserPrivilege.values()[rs.getInt("privilege")]);
                user.setStatus(UserStatus.values()[rs.getInt("status")]);
                user.setLastPasswordChange(rs.getTimestamp("lastPasswordChange"));

            }
            stmt.close();
            releaseConnection();
        } catch (SQLException e) {
            throw new ServerDownException(e.getMessage());
        }
        de.setUser(user);
        return de;
    }

    @Override
    public void signUp(User user) throws ServerDownException, LoginFoundException {
        try {
            getConnection();
            ResultSet rs = null;
            stmt = con.prepareStatement(SIGN_IN);
            stmt.setString(1, user.getLogin());
            rs = stmt.executeQuery();
            if (rs.next()) {
                throw new LoginFoundException();
            }
            stmt = con.prepareStatement(SIGN_UP);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            Enum status = user.getStatus();
            stmt.setInt(4, status.ordinal());
            Enum privilege = user.getPrivilege();
            stmt.setInt(5, privilege.ordinal());
            stmt.setString(6, user.getPassword());
            stmt.executeUpdate();

            stmt.close();
            releaseConnection();
        } catch (SQLException e) {
            throw new ServerDownException(e.getMessage());
        }

    }

    public void releaseConnection() {
        pool.returnConnection(con);
    }

    public void getConnection() {
        if (pool.getConnections() < 2) {
            con = bdc.openConnection();
        } else {
            con = pool.getConnection();
        }

    }

   

}
