package com.project.artconnect.config;

/**
 * Database configuration constants.
 * TODO: Students should update these with their own MySQL credentials.
 */
public class DatabaseConfig {

    public static final String URL = "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String USER = "root";
    public static final String PASSWORD = "tonMotDePasse"; // CHANGE ME

    /**
     * Retourne une connexion JDBC active vers la base ArtConnect.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
