package com.project.artconnect.persistence;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artwork;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtworkDao.
 * Handles database operations for Artwork entities using JDBC.
 */
public class JdbcArtworkDao implements ArtworkDao {

    /**
     * Retrieves all artworks from the database.
     */
    @Override
    public List<Artwork> findAll() {
        String sql = "SELECT * FROM artwork";
        List<Artwork> artworks = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Artwork artwork = mapRow(rs);
                artworks.add(artwork);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all artworks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return artworks;
    }

    /**
     * Inserts a new artwork into the database.
     */
    @Override
    public void save(Artwork artwork) {
        String sql = "INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_name) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artwork.getTitle());
            pstmt.setObject(2, artwork.getCreationYear(), Types.INTEGER);
            pstmt.setString(3, artwork.getType());
            pstmt.setString(4, artwork.getMedium());
            pstmt.setString(5, artwork.getDimensions());
            pstmt.setString(6, artwork.getDescription());
            pstmt.setDouble(7, artwork.getPrice());
            pstmt.setString(8, artwork.getStatus().toString());
            pstmt.setString(9, artwork.getArtist() != null ? artwork.getArtist().getName() : null);
            
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Artwork '" + artwork.getTitle() + "' inserted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error saving artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing artwork in the database.
     */
    @Override
    public void update(Artwork artwork) {
        String sql = "UPDATE artwork SET creation_year = ?, type = ?, medium = ?, dimensions = ?, "
                   + "description = ?, price = ?, status = ?, artist_name = ? WHERE title = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, artwork.getCreationYear(), Types.INTEGER);
            pstmt.setString(2, artwork.getType());
            pstmt.setString(3, artwork.getMedium());
            pstmt.setString(4, artwork.getDimensions());
            pstmt.setString(5, artwork.getDescription());
            pstmt.setDouble(6, artwork.getPrice());
            pstmt.setString(7, artwork.getStatus().toString());
            pstmt.setString(8, artwork.getArtist() != null ? artwork.getArtist().getName() : null);
            pstmt.setString(9, artwork.getTitle());
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Artwork '" + artwork.getTitle() + "' updated successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes an artwork from the database by title.
     */
    @Override
    public void delete(String title) {
        String sql = "DELETE FROM artwork WHERE title = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Artwork '" + title + "' deleted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds all artworks by a specific artist.
     */
    @Override
    public List<Artwork> findByArtistName(String artistName) {
        String sql = "SELECT * FROM artwork WHERE artist_name = ?";
        List<Artwork> artworks = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artistName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Artwork artwork = mapRow(rs);
                    artworks.add(artwork);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding artworks by artist: " + e.getMessage());
            e.printStackTrace();
        }
        
        return artworks;
    }

    /**
     * Maps a database row (ResultSet) to an Artwork object.
     */
    private Artwork mapRow(ResultSet rs) throws SQLException {
        Artwork artwork = new Artwork();
        artwork.setTitle(rs.getString("title"));
        artwork.setCreationYear(rs.getObject("creation_year", Integer.class));
        artwork.setType(rs.getString("type"));
        artwork.setMedium(rs.getString("medium"));
        artwork.setDimensions(rs.getString("dimensions"));
        artwork.setDescription(rs.getString("description"));
        artwork.setPrice(rs.getDouble("price"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            artwork.setStatus(Artwork.Status.valueOf(statusStr));
        }
        
        // Note: artist_name is stored but the full Artist object needs to be loaded separately
        // This is a simplified approach - in production, you might use JOINs or lazy loading
        
        return artwork;
    }
}
