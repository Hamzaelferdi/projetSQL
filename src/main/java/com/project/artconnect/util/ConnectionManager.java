package com.project.artconnect.util;

import com.project.artconnect.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestionnaire centralise des connexions JDBC vers la base ArtConnect.
 *
 * <p>Tous les DAO doivent passer par {@link #getConnection()} pour obtenir
 * une nouvelle connexion. Cela permet :</p>
 * <ul>
 *   <li>de centraliser le chargement du driver MySQL,</li>
 *   <li>de modifier la strategie de connexion (pool, etc.) en un seul endroit,</li>
 *   <li>de garantir l'utilisation des parametres de {@link DatabaseConfig}.</li>
 * </ul>
 */
public final class ConnectionManager {

    private ConnectionManager() {
        throw new AssertionError("Classe utilitaire, ne pas instancier.");
    }

    static {
        // Forcer le chargement explicite du driver MySQL.
        // Optionnel depuis JDBC 4, mais securise les environnements ou
        // l'auto-detection echoue (anciens classloaders, etc.).
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Driver MySQL (com.mysql.cj.jdbc.Driver) introuvable dans le classpath.", e);
        }
    }

    /**
     * Ouvre une nouvelle connexion vers la base ArtConnect.
     *
     * @return une connexion JDBC ouverte (a fermer par l'appelant, idealement
     *         via try-with-resources).
     * @throws SQLException si la connexion ne peut etre etablie.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD);
    }
}
