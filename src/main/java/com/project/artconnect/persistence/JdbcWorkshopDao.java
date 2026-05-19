package com.project.artconnect.persistence;

import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation JDBC du WorkshopDao.
 *
 * <p>Operations supportees : findAll, findById, findByTitle, save, update, delete.</p>
 */
public class JdbcWorkshopDao implements WorkshopDao {

    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = "SELECT * FROM workshop WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du workshop par id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Workshop> findByTitle(String title) {
        String sql = "SELECT * FROM workshop WHERE title = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du workshop par titre", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        String sql = "SELECT * FROM workshop";
        List<Workshop> workshops = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                workshops.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des workshops", e);
        }
        return workshops;
    }

    @Override
    public void save(Workshop w) {
        String sql = "INSERT INTO workshop (title, date, duration_minutes, max_participants, price, "
                + "instructor_name, location, description, level) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bindCommonFields(pstmt, w);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du workshop '" + w.getTitle() + "'", e);
        }
    }

    @Override
    public void update(Workshop w) {
        // On met a jour le premier workshop dont le titre correspond
        // (le titre n'est pas UNIQUE en BD mais sert d'identifiant cote application).
        String sql = "UPDATE workshop SET date = ?, duration_minutes = ?, max_participants = ?, price = ?, "
                + "instructor_name = ?, location = ?, description = ?, level = ? WHERE title = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, w.getDate(), Types.TIMESTAMP);
            pstmt.setInt(2, w.getDurationMinutes());
            pstmt.setInt(3, w.getMaxParticipants());
            pstmt.setDouble(4, w.getPrice());
            pstmt.setString(5, w.getInstructor() != null ? w.getInstructor().getName() : null);
            pstmt.setString(6, w.getLocation());
            pstmt.setString(7, w.getDescription());
            pstmt.setString(8, w.getLevel());
            pstmt.setString(9, w.getTitle());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour du workshop '" + w.getTitle() + "'", e);
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM workshop WHERE title = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du workshop '" + title + "'", e);
        }
    }

    // -- Helpers -----------------------------------------------------------

    private void bindCommonFields(PreparedStatement pstmt, Workshop w) throws SQLException {
        pstmt.setString(1, w.getTitle());
        pstmt.setObject(2, w.getDate(), Types.TIMESTAMP);
        pstmt.setInt(3, w.getDurationMinutes());
        pstmt.setInt(4, w.getMaxParticipants());
        pstmt.setDouble(5, w.getPrice());
        pstmt.setString(6, w.getInstructor() != null ? w.getInstructor().getName() : null);
        pstmt.setString(7, w.getLocation());
        pstmt.setString(8, w.getDescription());
        pstmt.setString(9, w.getLevel());
    }

    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop w = new Workshop();
        w.setTitle(rs.getString("title"));
        w.setDate(rs.getObject("date", LocalDateTime.class));
        w.setDurationMinutes(rs.getInt("duration_minutes"));
        w.setMaxParticipants(rs.getInt("max_participants"));
        w.setPrice(rs.getDouble("price"));
        w.setLocation(rs.getString("location"));
        w.setDescription(rs.getString("description"));
        w.setLevel(rs.getString("level"));
        String instructorName = rs.getString("instructor_name");
        if (instructorName != null) {
            // On cree un Artist minimal pour porter le nom ; l'objet complet
            // est charge ailleurs si besoin.
            Artist a = new Artist();
            a.setName(instructorName);
            w.setInstructor(a);
        }
        return w;
    }
}
