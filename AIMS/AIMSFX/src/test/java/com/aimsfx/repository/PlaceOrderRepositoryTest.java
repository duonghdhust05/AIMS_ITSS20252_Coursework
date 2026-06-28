package com.aimsfx.repository;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderItem;
import com.aimsfx.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 3: Repository Layer
 * Test Suite: TS_PLO_REPO
 */
public class PlaceOrderRepositoryTest {

    private OrderRepository orderRepository;
    private int generatedOrderId = -1;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (generatedOrderId != -1) {
            // Delete the inserted order_items and orders
            try (java.sql.Connection conn = com.aimsfx.utils.DatabaseConnection.getInstance().getConnection()) {
                String deleteItems = "DELETE FROM order_items WHERE order_id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(deleteItems)) {
                    stmt.setInt(1, generatedOrderId);
                    stmt.executeUpdate();
                }

                String deleteOrder = "DELETE FROM orders WHERE order_id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(deleteOrder)) {
                    stmt.setInt(1, generatedOrderId);
                    stmt.executeUpdate();
                }
            }
        }
    }

    @Test
    void testIT_PLO_01_SaveOrderTransactional() throws SQLException {
        // Arrange
        Order order = new Order();
        order.setStatus("new");
        order.setSubtotal(100.0f);
        order.setDeliveryFee(20.0f);
        order.setTotalAmount(120.0f);

        com.aimsfx.model.DeliveryInfo info = new com.aimsfx.model.DeliveryInfo();
        info.setRecipientName("Test Name");
        info.setPhoneNumber("0123456789");
        info.setEmail("test@email.com");
        info.setAddress("Test Address");
        info.setProvince("Hanoi");
        try {
            order.setDeliveryInfo(info);
        } catch (Exception e) {}

        Product product = new com.aimsfx.model.Book();
        product.setProductId(1L);
        product.setTitle("Test Book");
        product.setCategory("Book");
        product.setCurrentPrice(100.0);
        
        OrderItem item = new OrderItem(product, 1, 100.0);
        java.util.List<OrderItem> items = new ArrayList<>();
        items.add(item);
        order.setOrderItems(items);

        // Act
        generatedOrderId = orderRepository.saveOrder(order);

        // Assert
        assertTrue(generatedOrderId > 0, "Generated order ID should be greater than 0");
    }
}
