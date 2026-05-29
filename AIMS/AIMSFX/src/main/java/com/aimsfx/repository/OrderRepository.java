package com.aimsfx.repository;

import com.aimsfx.utils.DatabaseConnection;
import com.aimsfx.model.Order;
import com.aimsfx.model.OrderItem;
import com.aimsfx.model.DeliveryInfo;

import java.sql.*;

/**
 * Repository for Order persistence.
 * Handles saving orders to the database and retrieving the auto-generated order_id.
 */
public class OrderRepository {

    /**
     * Save an order to the database and return the auto-generated order_id.
     * This should be called BEFORE payment processing to get a real order ID.
     *
     * @param order The order to save
     * @return The auto-generated order_id from the database
     * @throws SQLException if database operation fails
     */
    public int saveOrder(Order order) throws SQLException {
        String insertOrderSQL = """
            INSERT INTO orders (
                user_id, delivery_name, delivery_phone, delivery_email,
                delivery_address, delivery_province, delivery_ward, 
                delivery_instructions, shipping_fee, rush_delivery, rush_fee,
                subtotal, total_amount, payment_method, payment_status, order_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING order_id
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertOrderSQL)) {

            // user_id - nullable, set to null if not logged in
            stmt.setNull(1, Types.INTEGER);

            // Delivery info - save ALL fields
            if (order.getDeliveryInfo() != null) {
                DeliveryInfo info = order.getDeliveryInfo();
                stmt.setString(2, info.getRecipientName());
                stmt.setString(3, info.getPhoneNumber());
                stmt.setString(4, info.getEmail());
                stmt.setString(5, info.getAddress());
                stmt.setString(6, info.getProvince());
                stmt.setString(7, info.getWard());
                stmt.setString(8, info.getDeliveryInstructions());
            } else {
                stmt.setNull(2, Types.VARCHAR);
                stmt.setNull(3, Types.VARCHAR);
                stmt.setNull(4, Types.VARCHAR);
                stmt.setString(5, ""); // address is NOT NULL
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
            }

            // Fees - Order may not have rush delivery fields, use defaults
            stmt.setDouble(9, order.getDeliveryFee());
            stmt.setBoolean(10, false);  // rush_delivery - default false
            stmt.setDouble(11, 0.0);     // rush_fee - default 0

            // Amounts
            stmt.setDouble(12, order.getSubtotal());
            stmt.setDouble(13, order.getTotalAmount());

            // Payment info - initially pending
            stmt.setString(14, null); // payment_method - set later when user chooses
            stmt.setString(15, "PENDING");
            stmt.setString(16, "PROCESSING");

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int orderId = rs.getInt("order_id");
                order.setOrderId(orderId);

                // Save order items
                saveOrderItems(conn, orderId, order);

                return orderId;
            } else {
                throw new SQLException("Failed to get generated order_id");
            }
        }
    }

    /**
     * Save order items to the order_items table.
     */
    private void saveOrderItems(Connection conn, int orderId, Order order) throws SQLException {
        String insertItemSQL = """
            INSERT INTO order_items (
                order_id, product_id, product_title, product_type, 
                quantity, unit_price, total_price
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(insertItemSQL)) {
            for (OrderItem item : order.getOrderItems()) {
                stmt.setInt(1, orderId);
                stmt.setLong(2, item.getProduct().getProductId());
                stmt.setString(3, item.getProduct().getTitle());
                stmt.setString(4, item.getProduct().getCategory());  // Use category instead of type
                stmt.setInt(5, item.getQuantity());
                stmt.setDouble(6, item.getPrice());
                stmt.setDouble(7, item.getPrice() * item.getQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Update order payment method and status after payment.
     *
     * @param orderId The order ID to update
     * @param paymentMethod "VIETQR" or "PAYPAL"
     * @param paymentStatus "COMPLETED", "FAILED", "CANCELLED"
     */
    public void updatePaymentStatus(int orderId, String paymentMethod, String paymentStatus) throws SQLException {
        String sql = """
            UPDATE orders 
            SET payment_method = ?, 
                payment_status = ?, 
                order_status = CASE WHEN ? = 'COMPLETED' THEN 'PENDING' ELSE order_status END,
                updated_at = CURRENT_TIMESTAMP
            WHERE order_id = ?
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paymentMethod);
            stmt.setString(2, paymentStatus);
            stmt.setString(3, paymentStatus);
            stmt.setInt(4, orderId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get an order by its ID with full delivery info restored.
     */
    public Order findById(int orderId) throws SQLException {
        String sql = """
            SELECT * FROM orders WHERE order_id = ?
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setSubtotal(rs.getFloat("subtotal"));
                order.setTotalAmount(rs.getFloat("total_amount"));
                order.setDeliveryFee(rs.getFloat("shipping_fee"));
                order.setStatus(rs.getString("order_status"));
                
                // Restore DeliveryInfo from database
                DeliveryInfo deliveryInfo = new DeliveryInfo();
                deliveryInfo.setRecipientName(rs.getString("delivery_name"));
                deliveryInfo.setPhoneNumber(rs.getString("delivery_phone"));
                deliveryInfo.setEmail(rs.getString("delivery_email"));
                deliveryInfo.setAddress(rs.getString("delivery_address"));
                deliveryInfo.setProvince(rs.getString("delivery_province"));
                deliveryInfo.setWard(rs.getString("delivery_ward"));
                deliveryInfo.setDeliveryInstructions(rs.getString("delivery_instructions"));
                
                // Set delivery info - wrap in try-catch since it may validate
                try {
                    order.setDeliveryInfo(deliveryInfo);
                } catch (Exception e) {
                    // If validation fails, still set the info (data from DB should be valid)
                    System.err.println("Warning: DeliveryInfo validation failed for order " + orderId + ": " + e.getMessage());
                }
                
                return order;
            }
            return null;
        }
    }

    /**
     * Check if an order exists.
     */
    public boolean exists(int orderId) throws SQLException {
        String sql = "SELECT 1 FROM orders WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeQuery().next();
        }
    }
    

    /**
     * Update order_status for Product Manager review actions (approve/reject) or customer cancellation.
     */
    public void updateOrderStatus(int orderId, String orderStatus) throws SQLException {
        updateOrderStatus(orderId, orderStatus, null);
    }

    /**
     * Update order_status and optionally cancel_reason.
     */
    public void updateOrderStatus(int orderId, String orderStatus, String cancelReason) throws SQLException {
        String sql = """
            UPDATE orders
            SET order_status = ?, cancel_reason = ?, updated_at = CURRENT_TIMESTAMP
            WHERE order_id = ?
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderStatus);
            stmt.setString(2, cancelReason);
            stmt.setInt(3, orderId);
            stmt.executeUpdate();
        }
    }
}
