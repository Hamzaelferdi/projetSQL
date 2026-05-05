package com.project.artconnect.persistence;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for CommunityMemberDao.
 * Handles database operations for CommunityMember entities using JDBC.
 */
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    /**
     * Finds a community member by ID.
     */
    @Override
    public Optional<CommunityMember> findById(Long id) {
        String sql = "SELECT * FROM community_member WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CommunityMember member = mapRow(rs);
                    return Optional.of(member);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding community member by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    /**
     * Retrieves all community members from the database.
     */
    @Override
    public List<CommunityMember> findAll() {
        String sql = "SELECT * FROM community_member";
        List<CommunityMember> members = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, 
                DatabaseConfig.USER, 
                DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                CommunityMember member = mapRow(rs);
                members.add(member);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all community members: " + e.getMessage());
            e.printStackTrace();
        }
        
        return members;
    }

    /**
     * Maps a database row (ResultSet) to a CommunityMember object.
     */
    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember member = new CommunityMember();
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setBirthYear(rs.getObject("birth_year", Integer.class));
        member.setPhone(rs.getString("phone"));
        member.setCity(rs.getString("city"));
        member.setMembershipType(rs.getString("membership_type"));
        return member;
    }
}
