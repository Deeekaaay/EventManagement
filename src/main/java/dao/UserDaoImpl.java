package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.User;

/**
 * Implementation of the UserDao interface for user-related database operations.
 * Handles user creation, authentication, and password management.
 */
public class UserDaoImpl implements UserDao {
    private static final String TABLE_NAME = "users";

    public UserDaoImpl() {}

    /**
     * Creates the users table if it does not exist.
     */
    @Override
    public void setup() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + "username VARCHAR(10) NOT NULL,"
                    + "password VARCHAR(8) NOT NULL,"
                    + "preferred_name VARCHAR(20) NOT NULL,"
                    + "role VARCHAR(10) NOT NULL DEFAULT 'user',"
                    + "PRIMARY KEY (username))";
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Encrypts a password using a Caesar cipher (shift by 3).
     * @param password the plain text password
     * @return the encrypted password
     */
    private String encryptPassword(String password) {
        StringBuilder sb = new StringBuilder();
        for (char c : password.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (Character.isLowerCase(c)) {
                    sb.append((char) ('a' + (c - 'a' + 3) % 26));
                } else if (Character.isUpperCase(c)) {
                    sb.append((char) ('A' + (c - 'A' + 3) % 26));
                } else if (Character.isDigit(c)) {
                    sb.append((char) ('0' + (c - '0' + 3) % 10));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves a user by username and password.
     * @param username the username
     * @param password the plain text password
     * @return the User object if found, otherwise null
     */
    @Override
    public User getUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, encryptPassword(password));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setPreferredName(rs.getString("preferred_name"));
                    user.setRole(rs.getString("role"));
                    // Set userId if column exists, otherwise skip
                    try {
                        user.setUserId(rs.getInt("user_id"));
                    } catch (SQLException | NullPointerException e) {
                        // Ignore if user_id column does not exist or is null
                    }
                    return user;
                }
                return null;
            }
        }
    }

    /**
     * Creates a new user in the database.
     * @param username the username
     * @param password the plain text password
     * @param preferredName the preferred name
     * @return the created User object
     */
    @Override
    public User createUser(String username, String password, String preferredName) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (username, password, preferred_name) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, encryptPassword(password));
            stmt.setString(3, preferredName);
            stmt.executeUpdate();
            return new User(username, encryptPassword(password), preferredName, "user"); // default role
        }
    }

    /**
     * Changes the password for a user.
     * @param username the username
     * @param newPassword the new plain text password
     * @return true if the password was changed, false otherwise
     */
    @Override
    public boolean changePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET password = ? WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, encryptPassword(newPassword));
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        }
    }
}
