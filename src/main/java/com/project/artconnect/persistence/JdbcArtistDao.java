package com.project.artconnect.persistence;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
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
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Artist artist = mapRow(rs);
                artists.add(artist);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all artists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return artists;
    }

    /**
     * Inserts a new artist into the database.
     */
    @Override
    public void save(Artist artist) {
        String sql = "INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artist.getName());
            pstmt.setString(2, artist.getBio());
            pstmt.setObject(3, artist.getBirthYear(), Types.INTEGER);
            pstmt.setString(4, artist.getContactEmail());
            pstmt.setString(5, artist.getPhone());
            pstmt.setString(6, artist.getCity());
            pstmt.setString(7, artist.getWebsite());
            pstmt.setString(8, artist.getSocialMedia());
            pstmt.setBoolean(9, artist.isActive());
            
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Artist '" + artist.getName() + "' inserted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error saving artist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing artist in the database.
     */
    @Override
    public void update(Artist artist) {
        String sql = "UPDATE artist SET bio = ?, birth_year = ?, contact_email = ?, phone = ?, "
                   + "city = ?, website = ?, social_media = ?, is_active = ? WHERE name = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artist.getBio());
            pstmt.setObject(2, artist.getBirthYear(), Types.INTEGER);
            pstmt.setString(3, artist.getContactEmail());
            pstmt.setString(4, artist.getPhone());
            pstmt.setString(5, artist.getCity());
            pstmt.setString(6, artist.getWebsite());
            pstmt.setString(7, artist.getSocialMedia());
            pstmt.setBoolean(8, artist.isActive());
            pstmt.setString(9, artist.getName());
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Artist '" + artist.getName() + "' updated successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating artist: " + e.getMessage());
            e.printStackTrace();
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
            
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Artist '" + artistName + "' deleted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting artist: " + e.getMessage());
            e.printStackTrace();
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
                    Artist artist = mapRow(rs);
                    artists.add(artist);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding artists by city: " + e.getMessage());
            e.printStackTrace();
        }
        
        return artists;
    }

    /**
     * Maps a database row (ResultSet) to an Artist object.
     */
    private Artist mapRow(ResultSet rs) throws SQLException {
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
        return artist;
    }
}
