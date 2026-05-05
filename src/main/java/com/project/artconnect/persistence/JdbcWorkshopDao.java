package com.project.artconnect.persistence;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Workshop;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for WorkshopDao.
 * Handles database operations for Workshop entities using JDBC.
 */
public class JdbcWorkshopDao implements WorkshopDao {

    /**
     * Finds a workshop by ID.
     */
    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = "SELECT * FROM workshop WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Workshop workshop = mapRow(rs);
                    return Optional.of(workshop);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding workshop by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    /**
     * Retrieves all workshops from the database.
     */
    @Override
    public List<Workshop> findAll() {
        String sql = "SELECT * FROM workshop";
        List<Workshop> workshops = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Workshop workshop = mapRow(rs);
                workshops.add(workshop);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all workshops: " + e.getMessage());
            e.printStackTrace();
        }
        
        return workshops;
    }

    /**
     * Maps a database row (ResultSet) to a Workshop object.
     */
    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setTitle(rs.getString("title"));
        workshop.setDate(rs.getObject("date", LocalDateTime.class));
        workshop.setDurationMinutes(rs.getInt("duration_minutes"));
        workshop.setMaxParticipants(rs.getInt("max_participants"));
        workshop.setPrice(rs.getDouble("price"));
        workshop.setLocation(rs.getString("location"));
        workshop.setDescription(rs.getString("description"));
        workshop.setLevel(rs.getString("level"));
        // Note: instructor_name is stored but the full Artist object needs to be loaded separately
        return workshop;
    }
}
