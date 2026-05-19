package com.project.artconnect.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration JDBC de la base de donnees ArtConnect.
 *
 * Les parametres (URL, utilisateur, mot de passe) sont charges au demarrage
 * depuis le fichier {@code db.properties} place dans {@code src/main/resources}.
 * Si le fichier est introuvable ou incomplet, des valeurs par defaut
 * (utiles pour le developpement local) sont utilisees en repli.
 *
 * <p>Cette approche evite de coder en dur des identifiants dans le code source
 * et permet de modifier la configuration sans recompiler l'application.</p>
 */
public final class DatabaseConfig {

    /** Nom du fichier de configuration recherche dans le classpath. */
    private static final String CONFIG_FILE = "db.properties";

    /** URL JDBC complete (jdbc:mysql://host:port/database?options). */
    public static final String URL;

    /** Utilisateur MySQL. */
    public static final String USER;

    /** Mot de passe MySQL. */
    public static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in =
                     DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("[DatabaseConfig] " + CONFIG_FILE
                        + " introuvable dans le classpath, utilisation des valeurs par defaut.");
            }
        } catch (IOException e) {
            System.err.println("[DatabaseConfig] Echec du chargement de "
                    + CONFIG_FILE + " : " + e.getMessage());
        }

        URL = props.getProperty("db.url",
                "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        USER = props.getProperty("db.user", "root");
        PASSWORD = props.getProperty("db.password", "");
    }

    /** Classe utilitaire : empeche l'instanciation. */
    private DatabaseConfig() {
        throw new AssertionError("DatabaseConfig est une classe utilitaire, ne pas instancier.");
    }
}
