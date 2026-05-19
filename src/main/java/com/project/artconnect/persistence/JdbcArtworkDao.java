package com.project.artconnect.persistence;

import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
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
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Artwork artwork = mapRow(rs, conn);
                artworks.add(artwork);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all artworks", e);
        }
        
        return artworks;
    }

    /**
     * Inserts a new artwork into the database.
     */
    @Override
    public void save(Artwork artwork) {
        String insertArtworkSql = "INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertTagSql = "INSERT INTO artwork_tag (artwork_title, tag_name) VALUES (?, ?)";

        try (Connection conn = ConnectionManager.getConnection()) {

            conn.setAutoCommit(false);
            try (PreparedStatement insertArtworkStmt = conn.prepareStatement(insertArtworkSql);
                 PreparedStatement insertTagStmt = conn.prepareStatement(insertTagSql)) {

                insertArtworkStmt.setString(1, artwork.getTitle());
                insertArtworkStmt.setObject(2, artwork.getCreationYear(), Types.INTEGER);
                insertArtworkStmt.setString(3, artwork.getType());
                insertArtworkStmt.setString(4, artwork.getMedium());
                insertArtworkStmt.setString(5, artwork.getDimensions());
                insertArtworkStmt.setString(6, artwork.getDescription());
                insertArtworkStmt.setDouble(7, artwork.getPrice());
                insertArtworkStmt.setString(8, artwork.getStatus() != null ? artwork.getStatus().name() : null);
                insertArtworkStmt.setString(9, artwork.getArtist() != null ? artwork.getArtist().getName() : null);
                insertArtworkStmt.executeUpdate();

                insertTags(artwork, insertTagStmt);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving artwork", e);
        }
    }

    /**
     * Updates an existing artwork in the database.
     */
    @Override
    public void update(Artwork artwork) {
        String updateArtworkSql = "UPDATE artwork SET creation_year = ?, type = ?, medium = ?, dimensions = ?, "
                + "description = ?, price = ?, status = ?, artist_name = ? WHERE title = ?";
        String deleteTagsSql = "DELETE FROM artwork_tag WHERE artwork_title = ?";
        String insertTagSql = "INSERT INTO artwork_tag (artwork_title, tag_name) VALUES (?, ?)";

        try (Connection conn = ConnectionManager.getConnection()) {

            conn.setAutoCommit(false);
            try (PreparedStatement updateArtworkStmt = conn.prepareStatement(updateArtworkSql);
                 PreparedStatement deleteTagsStmt = conn.prepareStatement(deleteTagsSql);
                 PreparedStatement insertTagStmt = conn.prepareStatement(insertTagSql)) {

                updateArtworkStmt.setObject(1, artwork.getCreationYear(), Types.INTEGER);
                updateArtworkStmt.setString(2, artwork.getType());
                updateArtworkStmt.setString(3, artwork.getMedium());
                updateArtworkStmt.setString(4, artwork.getDimensions());
                updateArtworkStmt.setString(5, artwork.getDescription());
                updateArtworkStmt.setDouble(6, artwork.getPrice());
                updateArtworkStmt.setString(7, artwork.getStatus() != null ? artwork.getStatus().name() : null);
                updateArtworkStmt.setString(8, artwork.getArtist() != null ? artwork.getArtist().getName() : null);
                updateArtworkStmt.setString(9, artwork.getTitle());
                updateArtworkStmt.executeUpdate();

                deleteTagsStmt.setString(1, artwork.getTitle());
                deleteTagsStmt.executeUpdate();

                insertTags(artwork, insertTagStmt);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating artwork", e);
        }
    }

    /**
     * Deletes an artwork from the database by title.
     */
    @Override
    public void delete(String title) {
        String sql = "DELETE FROM artwork WHERE title = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting artwork", e);
        }
    }

    /**
     * Finds all artworks by a specific artist.
     */
    @Override
    public List<Artwork> findByArtistName(String artistName) {
        String sql = "SELECT * FROM artwork WHERE artist_name = ?";
        List<Artwork> artworks = new ArrayList<>();
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artistName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Artwork artwork = mapRow(rs, conn);
                    artworks.add(artwork);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding artworks by artist", e);
        }
        
        return artworks;
    }

    /**
     * Maps a database row (ResultSet) to an Artwork object.
     */
    private Artwork mapRow(ResultSet rs, Connection conn) throws SQLException {
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
        artwork.setTags(findTagsByArtworkTitle(conn, artwork.getTitle()));
        
        return artwork;
    }

    private List<ArtworkTag> findTagsByArtworkTitle(Connection conn, String artworkTitle) throws SQLException {
        String sql = "SELECT tag_name FROM artwork_tag WHERE artwork_title = ?";
        List<ArtworkTag> tags = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artworkTitle);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(new ArtworkTag(rs.getString("tag_name")));
                }
            }
        }

        return tags;
    }

    private void insertTags(Artwork artwork, PreparedStatement insertTagStmt) throws SQLException {
        if (artwork.getTags() == null) {
            return;
        }

        for (ArtworkTag tag : artwork.getTags()) {
            if (tag == null || tag.getName() == null || tag.getName().isBlank()) {
                continue;
            }

            insertTagStmt.setString(1, artwork.getTitle());
            insertTagStmt.setString(2, tag.getName());
            insertTagStmt.executeUpdate();
        }
    }
}
