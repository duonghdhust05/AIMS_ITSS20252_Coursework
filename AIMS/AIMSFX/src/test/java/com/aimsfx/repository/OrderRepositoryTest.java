package com.aimsfx.repository;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderStatus;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.utils.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3: Data Access / Repository Layer Tests
 * Test Suite: TS_MNO_REPO
 */
public class OrderRepositoryTest {

    private OrderRepository orderRepository;
    private OrderQueryRepository orderQueryRepository;
    private int testOrderId = -1;

    @BeforeEach
    void setUp() throws SQLException {
        orderRepository = new OrderRepository();
        orderQueryRepository = new OrderQueryRepository();

        // Create a test order directly in the database
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // First ensure we have a valid user (assuming user id 1 exists or create one)
            // For simplicity, we just insert into orders. The foreign key for user_id might be needed.
            // Let's check if we can insert without user_id or use a dummy.
            String sql = "INSERT INTO orders (order_status, subtotal, shipping_fee, total_amount, delivery_email, delivery_address, delivery_phone, delivery_name, delivery_province) " +
                    "VALUES (?, 100, 0, 100, 'test@example.com', '123 Test St', '0123456789', 'Test User', 'Hanoi') RETURNING order_id";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, OrderStatus.PENDING_REVIEW.toDbValue());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    testOrderId = rs.getInt(1);
                }
            }
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testOrderId != -1) {
            try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                // Delete the test order
                String sql = "DELETE FROM orders WHERE order_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, testOrderId);
                    pstmt.executeUpdate();
                }
            }
        }
    }

    @Test
    void testIT_MNO_01_RepoQueryPendingOrders() throws SQLException {
        // Act
        List<OrderSummary> pendingOrders = orderQueryRepository.findByStatus(OrderStatus.PENDING_REVIEW, 10, 0);

        // Assert
        assertNotNull(pendingOrders);
        boolean foundTestOrder = pendingOrders.stream()
                .anyMatch(summary -> summary.getOrderId() == testOrderId);
        assertTrue(foundTestOrder, "The test order should be found in pending orders");
    }

    @Test
    void testIT_MNO_02_RepoUpdateOrderStatusWithCheck() throws SQLException {
        // Act
        boolean success = orderRepository.updateOrderStatusWithCheck(
                testOrderId,
                OrderStatus.APPROVED.toDbValue(),
                OrderStatus.PENDING_REVIEW.toDbValue(),
                "Approving test order"
        );

        // Assert
        assertTrue(success, "Status update should succeed");

        // Verify it was updated
        Order updatedOrder = orderRepository.findById(testOrderId);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.APPROVED.toDbValue(), updatedOrder.getStatus());
        assertEquals("Approving test order", updatedOrder.getCancelReason()); // Note: cancel_reason is used for approval notes too
    }
}
