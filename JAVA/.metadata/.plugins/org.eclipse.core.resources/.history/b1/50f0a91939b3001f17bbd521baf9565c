package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Static block to load database configuration
    static {
        try (FileInputStream fis = new FileInputStream("dbconfig.properties")) {
            Properties properties = new Properties();
            properties.load(fis);
            URL = properties.getProperty("db.url");
            USER = properties.getProperty("db.user");
            PASSWORD = properties.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Establishes and returns a connection to the database.
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            if (connection.isValid(2)) { // 2 seconds timeout for validation
                logger.info("Database connection established successfully.");
                return connection;
            } else {
                throw new SQLException("Failed to validate the database connection.");
            }
        } catch (SQLException e) {
            logger.severe("Database connection failed: " + e.getMessage());
            throw e; // Rethrow the exception for the caller
        }
    }

    /**
     * Closes the database connection safely.
     * 
     * @param connection the Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed successfully.");
            } catch (SQLException e) {
                logger.warning("Failed to close the database connection: " + e.getMessage());
            }
        }
    }
}
