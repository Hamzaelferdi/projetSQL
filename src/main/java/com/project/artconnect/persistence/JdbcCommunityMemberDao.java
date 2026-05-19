package com.project.artconnect.persistence;

import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation JDBC du CommunityMemberDao.
 */
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public Optional<CommunityMember> findById(Long id) {
        String sql = "SELECT * FROM community_member WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du membre par id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<CommunityMember> findByName(String name) {
        String sql = "SELECT * FROM community_member WHERE name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du membre par nom", e);
        }
        return Optional.empty();
    }

    @Override
    public List<CommunityMember> findAll() {
        String sql = "SELECT * FROM community_member ORDER BY name";
        List<CommunityMember> members = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des membres", e);
        }
        return members;
    }

    @Override
    public void save(CommunityMember m) {
        String sql = "INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getEmail());
            pstmt.setObject(3, m.getBirthYear(), Types.INTEGER);
            pstmt.setString(4, m.getPhone());
            pstmt.setString(5, m.getCity());
            pstmt.setString(6, m.getMembershipType() != null ? m.getMembershipType() : "free");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du membre '" + m.getName() + "'", e);
        }
    }

    @Override
    public void update(CommunityMember m) {
        String sql = "UPDATE community_member SET email = ?, birth_year = ?, phone = ?, city = ?, "
                + "membership_type = ? WHERE name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getEmail());
            pstmt.setObject(2, m.getBirthYear(), Types.INTEGER);
            pstmt.setString(3, m.getPhone());
            pstmt.setString(4, m.getCity());
            pstmt.setString(5, m.getMembershipType() != null ? m.getMembershipType() : "free");
            pstmt.setString(6, m.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour du membre '" + m.getName() + "'", e);
        }
    }

    @Override
    public void delete(String name) {
        String sql = "DELETE FROM community_member WHERE name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du membre '" + name + "'", e);
        }
    }

    // -- Helpers -----------------------------------------------------------

    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember m = new CommunityMember();
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        m.setBirthYear(rs.getObject("birth_year", Integer.class));
        m.setPhone(rs.getString("phone"));
        m.setCity(rs.getString("city"));
        m.setMembershipType(rs.getString("membership_type"));
        return m;
    }
}
