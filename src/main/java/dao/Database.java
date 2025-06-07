package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing SQLite database connections.
 * <p>
 * Always returns a new connection for each call to getConnection().
 * The caller is responsible for closing the connection after use.
 * </p>
 */
public final class Database {
    private static final String DB_URL = "jdbc:sqlite:application.db";

    // Private constructor to prevent instantiation
    private Database() {}

    /**
     * Returns a new database connection.
     *
     * @return a new Connection to the SQLite database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
