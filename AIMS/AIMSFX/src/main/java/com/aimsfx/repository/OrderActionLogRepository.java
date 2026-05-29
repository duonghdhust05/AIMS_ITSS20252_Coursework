package com.aimsfx.repository;

import com.aimsfx.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderActionLogRepository {

    public void logAction(int orderId, String action, String reason) throws SQLException {
        String sql = """
            INSERT INTO order_action_logs (order_id, action, reason)
            VALUES (?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setString(2, action);
            stmt.setString(3, reason);
            stmt.executeUpdate();
        }
    }
}
