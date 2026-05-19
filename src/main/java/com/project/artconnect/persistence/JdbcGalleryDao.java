package com.project.artconnect.persistence;

import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for GalleryDao.
 * Handles database operations for Gallery entities using JDBC.
 */
public class JdbcGalleryDao implements GalleryDao {

    /**
     * Finds a gallery by ID.
     */
    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT * FROM gallery WHERE id = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Gallery gallery = mapRow(rs);
                    return Optional.of(gallery);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding gallery by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    /**
     * Retrieves all galleries from the database.
     */
    @Override
    public List<Gallery> findAll() {
        String sql = "SELECT * FROM gallery";
        List<Gallery> galleries = new ArrayList<>();
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Gallery gallery = mapRow(rs);
                galleries.add(gallery);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all galleries: " + e.getMessage());
            e.printStackTrace();
        }
        
        return galleries;
    }

    /**
     * Maps a database row (ResultSet) to a Gallery object.
     */
    private Gallery mapRow(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("name"));
        gallery.setAddress(rs.getString("address"));
        gallery.setOwnerName(rs.getString("owner_name"));
        gallery.setOpeningHours(rs.getString("opening_hours"));
        gallery.setContactPhone(rs.getString("contact_phone"));
        gallery.setRating(rs.getDouble("rating"));
        gallery.setWebsite(rs.getString("website"));
        return gallery;
    }
}
