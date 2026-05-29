package com.aimsfx.repository;

import com.aimsfx.model.OrderDetail;
import com.aimsfx.model.OrderLine;
import com.aimsfx.model.OrderStatus;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Read model repository for Order review screens (paging + detail).
 */
public class OrderQueryRepository {

    public int countByStatus(OrderStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM orders WHERE order_status = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.toDbValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    public List<OrderSummary> findByStatus(OrderStatus status, int limit, int offset) throws SQLException {
        String sql = """
                SELECT
                    order_id,
                    created_at,
                    delivery_name,
                    total_amount,
                    payment_method,
                    payment_status,
                    order_status
                FROM orders
                WHERE order_status = ?
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        List<OrderSummary> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.toDbValue());
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapSummary(rs));
                }
            }
        }
        return results;
    }

    public OrderDetail findDetailById(int orderId) throws SQLException {
        OrderDetail detail = new OrderDetail();

        String headerSql = """
                SELECT
                    order_id,
                    created_at,
                    updated_at,
                    delivery_name,
                    delivery_phone,
                    delivery_email,
                    delivery_address,
                    delivery_province,
                    delivery_ward,
                    delivery_instructions,
                    total_amount,
                    payment_method,
                    payment_status,
                    order_status,
                    transaction_id
                FROM orders
                WHERE order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement headerStmt = conn.prepareStatement(headerSql)) {
            headerStmt.setInt(1, orderId);
            try (ResultSet rs = headerStmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                OrderSummary summary = mapSummary(rs);
                detail.setSummary(summary);
                detail.setDeliveryPhone(rs.getString("delivery_phone"));
                detail.setDeliveryEmail(rs.getString("delivery_email"));
                detail.setDeliveryAddress(rs.getString("delivery_address"));
                detail.setDeliveryProvince(rs.getString("delivery_province"));
                detail.setDeliveryWard(rs.getString("delivery_ward"));
                detail.setDeliveryInstructions(rs.getString("delivery_instructions"));
                detail.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
                int txnId = rs.getInt("transaction_id");
                detail.setTransactionId(rs.wasNull() ? null : txnId);
            }

            String itemsSql = """
                    SELECT
                        product_id,
                        product_title,
                        product_type,
                        quantity,
                        unit_price,
                        total_price
                    FROM order_items
                    WHERE order_id = ?
                    ORDER BY product_title ASC
                    """;
            try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                itemsStmt.setInt(1, orderId);
                try (ResultSet rs = itemsStmt.executeQuery()) {
                    while (rs.next()) {
                        OrderLine line = new OrderLine();
                        line.setProductId(rs.getLong("product_id"));
                        line.setProductTitle(rs.getString("product_title"));
                        line.setProductType(rs.getString("product_type"));
                        line.setQuantity(rs.getInt("quantity"));
                        line.setUnitPrice(rs.getDouble("unit_price"));
                        line.setTotalPrice(rs.getDouble("total_price"));
                        detail.getLines().add(line);
                    }
                }
            }
        }

        return detail;
    }

    private static OrderSummary mapSummary(ResultSet rs) throws SQLException {
        OrderSummary summary = new OrderSummary();
        summary.setOrderId(rs.getInt("order_id"));
        summary.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        summary.setCustomerName(rs.getString("delivery_name"));
        summary.setTotalAmount(rs.getDouble("total_amount"));
        summary.setPaymentMethod(rs.getString("payment_method"));
        summary.setPaymentStatus(rs.getString("payment_status"));
        summary.setOrderStatus(OrderStatus.fromDbValue(rs.getString("order_status")));
        return summary;
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}