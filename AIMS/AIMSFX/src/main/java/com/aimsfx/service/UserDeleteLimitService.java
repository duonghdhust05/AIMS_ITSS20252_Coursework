package com.aimsfx.service;

import com.aimsfx.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

/**
 * UserDeleteLimitService - Manages daily product deletion limits per user
 * 
 * BUSINESS RULE:
 * - Each user can delete a maximum of 20 products per day
 * - Deletion count is tracked in user_delete_count table
 * - Count resets daily (tracked by day_id)
 * 
 * WORKFLOW:
 * 1. Check if user has reached daily limit (20 deletions)
 * 2. If within limit, allow deletion
 * 3. After successful deletion, increment count for that user/day
 * 4. If no record exists for user/day, create new record with count=1
 */
public class UserDeleteLimitService {
    
    private static final int MAX_DELETIONS_PER_DAY = 20;
    private final DatabaseConnection dbConnection;
    
    public UserDeleteLimitService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public boolean canDeleteProduct(Long userId) {
        LocalDate today = LocalDate.now();
        int currentCount = getDeleteCount(userId, today);
        return currentCount < MAX_DELETIONS_PER_DAY;
    }

    public int getRemainingQuota(Long userId) {
        LocalDate today = LocalDate.now();
        int currentCount = getDeleteCount(userId, today);
        return Math.max(0, MAX_DELETIONS_PER_DAY - currentCount);
    }

    public int getDeleteCount(Long userId, LocalDate date) {
        String sql = "SELECT delete_count FROM user_delete_count WHERE user_id = ? AND day_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("delete_count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting delete count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0; // No record exists, count is 0
    }

    public boolean incrementDeleteCount(Long userId) {
        LocalDate today = LocalDate.now();
        
        // Try to update existing record first (using PostgreSQL UPSERT)
        String upsertSql = 
            "INSERT INTO user_delete_count (user_id, day_id, delete_count, created_at, updated_at) " +
            "VALUES (?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (user_id, day_id) " +
            "DO UPDATE SET delete_count = user_delete_count.delete_count + 1, " +
            "              updated_at = CURRENT_TIMESTAMP";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
            
            stmt.setLong(1, userId);
            stmt.setDate(2, Date.valueOf(today));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error incrementing delete count: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getMaxDeletionsPerDay() {
        return MAX_DELETIONS_PER_DAY;
    }
}
