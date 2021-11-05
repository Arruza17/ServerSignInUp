package model;

import enumerations.UserPrivilege;
import enumerations.UserStatus;
import exceptions.LoginFoundException;

import exceptions.ServerDownException;
import exceptions.UserNotFoundException;
import interfaces.Connectable;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.User;

/**
 * DaoImplementation for MYSQL Singleton class
 *
 * @author Yeray Sampedro
 */
public class DAOImplementation implements Connectable {

    private static DAOImplementation dao;
    private Pool pool;
    private BDConnection bdc;
    private Connection con;
    private PreparedStatement stmt;

    //Insert to sign up
    //Null = Autogenerated ID // now() = Current date
    final String SIGN_UP = "insert into user values(null, ?,?, ?, ?, ?, ?, now())";
    //Sentence used to check if the user already exists
    final String FIND_LOGIN = "Select * from user where login = ?";

    //Sentences to update the SignIns table
    final String INSERT_LAST_SIGN_INS = "Insert into signin values(null, now(), ?)";
    final String CHECK_LAST_SIGN_INS = "Select count(lastSignIn) as \"count\" from signin where userid = ?";
    final String UPDATEABLE_SIGNIN = "Select * from signin where userid = ? order by lastSignIn";

    //Sentences to SignIn 
    final String SIGN_IN = "Select * from user where login = ? and password=?";

    private static final ResourceBundle CONFIGFILE = ResourceBundle.getBundle("resources.config");
    private static final int MAX_USERS = Integer.parseInt(CONFIGFILE.getString("MaxUsers"));

    private DAOImplementation() {
        pool = Pool.getPool();
        bdc = new BDConnection();

    }

    /**
     * Method that returns the implementation
     *
     * @return
     */
    public static DAOImplementation getDAO() {
        if (dao == null) {
            dao = new DAOImplementation();
        }
        return dao;
    }

    /**
     * Method that allows a user to be signed in
     *
     * @param user the user to sing in
     * @return de the object used to transfer data between client and server
     * @throws ServerDownException
     */
    @Override
    public DataEncapsulator signIn(User user) throws ServerDownException {
        DataEncapsulator de = new DataEncapsulator();
        try {
            //Get connection from the pool
            getConnection();
            //Prepare the ResultSet and the statement
            ResultSet rs = null;
            stmt = con.prepareStatement(SIGN_IN);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPassword());
            rs = stmt.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("login"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("fullName"));
                user.setPrivilege(UserPrivilege.values()[rs.getInt("privilege")]);
                user.setStatus(UserStatus.values()[rs.getInt("status")]);
                user.setLastPasswordChange(rs.getTimestamp("lastPasswordChange"));
                //Update the sign in table
                updateSignIns(user, rs);
            } else {
                user = null;
            }
            rs.close();
            stmt.close();
            releaseConnection();
            if (user == null) {
                throw new UserNotFoundException();
            }

        } catch (UserNotFoundException ex) {
            de.setException(ex);
            throw new ServerDownException(ex.getMessage());
        } catch (SQLException ex) {
            de.setException(ex);
            throw new ServerDownException(ex.getMessage());

        } finally {
            if (de.getException() == null) {
                de.setException(new Exception("OK"));
            }
            de.setUser(user);
            return de;
        }
    }

    /**
     * Method that allows a user to be SignedUp
     *
     * @param user the user to SignUp
     * @throws ServerDownException
     * @throws LoginFoundException
     */
    @Override
    public void signUp(User user) throws ServerDownException, LoginFoundException {
        try {
            getConnection();
            ResultSet rs = null;
            stmt = con.prepareStatement(FIND_LOGIN);
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

    /**
     * Method that updates the oldest sign in to the newest one
     *
     * @param user the user to change the sign in
     * @param rs
     * @throws SQLException
     */
    private void updateSignIns(User user, ResultSet rs) throws SQLException {
        int cant = 0;
        //Search the ammount of SignIns
        stmt = con.prepareStatement(CHECK_LAST_SIGN_INS);
        stmt.setInt(1, user.getId());
        rs = stmt.executeQuery();
        if (rs.next()) {
            cant = rs.getInt("count");
            //In case they're 10
            if (cant == 10) {
                //Prepare updateable ResultSet 
                stmt = con.prepareStatement(UPDATEABLE_SIGNIN, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, user.getId());           
                rs = stmt.executeQuery();
                //First position because they are ordered by date
                rs.first();
                //Add the current date
                rs.updateTimestamp("lastSignIn", Timestamp.valueOf(LocalDateTime.now()));
                //Update
                rs.updateRow();
            } else {
                //Insert in case its less than 10
                stmt = con.prepareStatement(INSERT_LAST_SIGN_INS);
                stmt.setInt(1, user.getId());
                stmt.executeUpdate();
            }
        } else {
            //Insert if they do not have any sign ins yet
            stmt = con.prepareStatement(INSERT_LAST_SIGN_INS);
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        }

    }

    /**
     * Method that releases the connection from the pool
     */
    public void releaseConnection() {
        pool.returnConnection(con);
    }

    /**
     * Method that gets a connection form the pool
     */
    public void getConnection() {
        if (pool.getConnections() < MAX_USERS) {
            con = bdc.openConnection();
        } else {
            con = pool.getConnection();
        }

    }

    public void closePool() {
        for (int i = 0; i < pool.getConnections(); i++) {
            Connection con = pool.getConnection();
            try {
                bdc.closeConnection(null, con);
            } catch (SQLException ex) {
                Logger.getLogger(DAOImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
