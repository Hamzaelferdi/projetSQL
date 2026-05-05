package com.project.artconnect.persistence;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ExhibitionDao.
 * Handles database operations for Exhibition entities using JDBC.
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    /**
     * Retrieves all exhibitions from the database.
     */
    @Override
    public List<Exhibition> findAll() {
        String sql = "SELECT * FROM exhibition";
        List<Exhibition> exhibitions = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Exhibition exhibition = mapRow(rs);
                exhibitions.add(exhibition);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all exhibitions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return exhibitions;
    }

    /**
     * Inserts a new exhibition into the database.
     */
    @Override
    public void save(Exhibition exhibition) {
        String sql = "INSERT INTO exhibition (title, start_date, end_date, description, gallery_name, curator_name, theme) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, exhibition.getTitle());
            pstmt.setObject(2, exhibition.getStartDate(), Types.DATE);
            pstmt.setObject(3, exhibition.getEndDate(), Types.DATE);
            pstmt.setString(4, exhibition.getDescription());
            pstmt.setString(5, exhibition.getGallery() != null ? exhibition.getGallery().getName() : null);
            pstmt.setString(6, exhibition.getCuratorName());
            pstmt.setString(7, exhibition.getTheme());
            
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Exhibition '" + exhibition.getTitle() + "' inserted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error saving exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing exhibition in the database.
     */
    @Override
    public void update(Exhibition exhibition) {
        String sql = "UPDATE exhibition SET start_date = ?, end_date = ?, description = ?, "
                   + "gallery_name = ?, curator_name = ?, theme = ? WHERE title = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, exhibition.getStartDate(), Types.DATE);
            pstmt.setObject(2, exhibition.getEndDate(), Types.DATE);
            pstmt.setString(3, exhibition.getDescription());
            pstmt.setString(4, exhibition.getGallery() != null ? exhibition.getGallery().getName() : null);
            pstmt.setString(5, exhibition.getCuratorName());
            pstmt.setString(6, exhibition.getTheme());
            pstmt.setString(7, exhibition.getTitle());
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Exhibition '" + exhibition.getTitle() + "' updated successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes an exhibition from the database by title.
     */
    @Override
    public void delete(String title) {
        String sql = "DELETE FROM exhibition WHERE title = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Exhibition '" + title + "' deleted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maps a database row (ResultSet) to an Exhibition object.
     */
    private Exhibition mapRow(ResultSet rs) throws SQLException {
        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(rs.getString("title"));
        exhibition.setStartDate(rs.getObject("start_date", LocalDate.class));
        exhibition.setEndDate(rs.getObject("end_date", LocalDate.class));
        exhibition.setDescription(rs.getString("description"));
        exhibition.setCuratorName(rs.getString("curator_name"));
        exhibition.setTheme(rs.getString("theme"));
        // Note: gallery_name is stored but the full Gallery object needs to be loaded separately
        return exhibition;
    }
}
