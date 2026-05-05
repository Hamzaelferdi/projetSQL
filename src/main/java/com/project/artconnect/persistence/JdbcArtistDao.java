package com.project.artconnect.persistence;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtistDao.
 * Handles database operations for Artist entities using JDBC.
 */
public class JdbcArtistDao implements ArtistDao {

    /**
     * Retrieves all artists from the database.
     */
    @Override
    public List<Artist> findAll() {
        String sql = "SELECT * FROM artist";
        List<Artist> artists = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Artist artist = mapRow(rs, conn);
                artists.add(artist);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all artists", e);
        }
        
        return artists;
    }

    /**
     * Inserts a new artist into the database.
     */
    @Override
    public void save(Artist artist) {
        String insertArtistSql = "INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String upsertDisciplineSql = "INSERT INTO discipline (name) VALUES (?) ON DUPLICATE KEY UPDATE name = VALUES(name)";
        String linkDisciplineSql = "INSERT INTO artist_discipline (artist_name, discipline_id) "
                + "SELECT ?, d.id FROM discipline d WHERE d.name = ?";

        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD)) {

            conn.setAutoCommit(false);
            try (PreparedStatement insertArtistStmt = conn.prepareStatement(insertArtistSql);
                 PreparedStatement upsertDisciplineStmt = conn.prepareStatement(upsertDisciplineSql);
                 PreparedStatement linkDisciplineStmt = conn.prepareStatement(linkDisciplineSql)) {

                insertArtistStmt.setString(1, artist.getName());
                insertArtistStmt.setString(2, artist.getBio());
                insertArtistStmt.setObject(3, artist.getBirthYear(), Types.INTEGER);
                insertArtistStmt.setString(4, artist.getContactEmail());
                insertArtistStmt.setString(5, artist.getPhone());
                insertArtistStmt.setString(6, artist.getCity());
                insertArtistStmt.setString(7, artist.getWebsite());
                insertArtistStmt.setString(8, artist.getSocialMedia());
                insertArtistStmt.setBoolean(9, artist.isActive());
                insertArtistStmt.executeUpdate();

                insertDisciplines(conn, artist, upsertDisciplineStmt, linkDisciplineStmt);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving artist", e);
        }
    }

    /**
     * Updates an existing artist in the database.
     */
    @Override
    public void update(Artist artist) {
        String updateArtistSql = "UPDATE artist SET bio = ?, birth_year = ?, contact_email = ?, phone = ?, "
                + "city = ?, website = ?, social_media = ?, is_active = ? WHERE name = ?";
        String deleteLinksSql = "DELETE FROM artist_discipline WHERE artist_name = ?";
        String upsertDisciplineSql = "INSERT INTO discipline (name) VALUES (?) ON DUPLICATE KEY UPDATE name = VALUES(name)";
        String linkDisciplineSql = "INSERT INTO artist_discipline (artist_name, discipline_id) "
                + "SELECT ?, d.id FROM discipline d WHERE d.name = ?";

        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD)) {

            conn.setAutoCommit(false);
            try (PreparedStatement updateArtistStmt = conn.prepareStatement(updateArtistSql);
                 PreparedStatement deleteLinksStmt = conn.prepareStatement(deleteLinksSql);
                 PreparedStatement upsertDisciplineStmt = conn.prepareStatement(upsertDisciplineSql);
                 PreparedStatement linkDisciplineStmt = conn.prepareStatement(linkDisciplineSql)) {

                updateArtistStmt.setString(1, artist.getBio());
                updateArtistStmt.setObject(2, artist.getBirthYear(), Types.INTEGER);
                updateArtistStmt.setString(3, artist.getContactEmail());
                updateArtistStmt.setString(4, artist.getPhone());
                updateArtistStmt.setString(5, artist.getCity());
                updateArtistStmt.setString(6, artist.getWebsite());
                updateArtistStmt.setString(7, artist.getSocialMedia());
                updateArtistStmt.setBoolean(8, artist.isActive());
                updateArtistStmt.setString(9, artist.getName());
                updateArtistStmt.executeUpdate();

                deleteLinksStmt.setString(1, artist.getName());
                deleteLinksStmt.executeUpdate();

                insertDisciplines(conn, artist, upsertDisciplineStmt, linkDisciplineStmt);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating artist", e);
        }
    }

    /**
     * Deletes an artist from the database by name.
     */
    @Override
    public void delete(String artistName) {
        String sql = "DELETE FROM artist WHERE name = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artistName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting artist", e);
        }
    }

    /**
     * Finds all artists in a specific city.
     */
    @Override
    public List<Artist> findByCity(String city) {
        String sql = "SELECT * FROM artist WHERE city = ?";
        List<Artist> artists = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, city);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Artist artist = mapRow(rs, conn);
                    artists.add(artist);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding artists by city", e);
        }
        
        return artists;
    }

    /**
     * Maps a database row (ResultSet) to an Artist object.
     */
    private Artist mapRow(ResultSet rs, Connection conn) throws SQLException {
        Artist artist = new Artist();
        artist.setName(rs.getString("name"));
        artist.setBio(rs.getString("bio"));
        artist.setBirthYear(rs.getObject("birth_year", Integer.class));
        artist.setContactEmail(rs.getString("contact_email"));
        artist.setPhone(rs.getString("phone"));
        artist.setCity(rs.getString("city"));
        artist.setWebsite(rs.getString("website"));
        artist.setSocialMedia(rs.getString("social_media"));
        artist.setActive(rs.getBoolean("is_active"));
        artist.setDisciplines(findDisciplinesByArtistName(conn, artist.getName()));
        return artist;
    }

    private List<Discipline> findDisciplinesByArtistName(Connection conn, String artistName) throws SQLException {
        String sql = "SELECT d.name FROM discipline d "
                + "INNER JOIN artist_discipline ad ON ad.discipline_id = d.id "
                + "WHERE ad.artist_name = ?";
        List<Discipline> disciplines = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artistName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    disciplines.add(new Discipline(rs.getString("name")));
                }
            }
        }

        return disciplines;
    }

    private void insertDisciplines(Connection conn,
                                   Artist artist,
                                   PreparedStatement upsertDisciplineStmt,
                                   PreparedStatement linkDisciplineStmt) throws SQLException {
        if (artist.getDisciplines() == null) {
            return;
        }

        for (Discipline discipline : artist.getDisciplines()) {
            if (discipline == null || discipline.getName() == null || discipline.getName().isBlank()) {
                continue;
            }

            upsertDisciplineStmt.setString(1, discipline.getName());
            upsertDisciplineStmt.executeUpdate();

            linkDisciplineStmt.setString(1, artist.getName());
            linkDisciplineStmt.setString(2, discipline.getName());
            linkDisciplineStmt.executeUpdate();
        }
    }
}
