package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database class using the Singleton design pattern.
 * Ensures only one instance of the database connection exists throughout the application.
 */
public class Database {
    private static final String DB_URL = "jdbc:sqlite:application.db";
    private static Database instance; // Singleton instance
    private Connection connection;

    // Private constructor prevents instantiation from other classes
    private Database() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL);
    }

    /**
     * Returns the singleton instance of Database.
     * @return Database instance
     * @throws SQLException if a database access error occurs
     */
    public static synchronized Database getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Returns the single Connection object.
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }
}
