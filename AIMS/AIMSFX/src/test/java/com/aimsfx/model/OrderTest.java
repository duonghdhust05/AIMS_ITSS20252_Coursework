package com.aimsfx.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Order Model
 * Maps to Phase 1 of Test Plan (Models & Utils Layer)
 */
class OrderTest {

    @Test
    @DisplayName("[TC-PO-01] Create new Order successfully with valid data")
    void testCreateOrder_Success() {
        // Arrange
        Order order = new Order();
        order.setOrderId(1);
        order.setInvoiceId(10);
        order.setTotalAmount(150000.0f);
        order.setStatus("PENDING");

        List<OrderItem> items = new ArrayList<>();
        Product p1 = org.mockito.Mockito.mock(Product.class);
        org.mockito.Mockito.when(p1.getCurrentPrice()).thenReturn(50000.0);
        Product p2 = org.mockito.Mockito.mock(Product.class);
        org.mockito.Mockito.when(p2.getCurrentPrice()).thenReturn(50000.0);
        
        OrderItem item1 = new OrderItem(p1, 2, 50000.0);
        OrderItem item2 = new OrderItem(p2, 1, 50000.0);
        items.add(item1);
        items.add(item2);
        order.setOrderItems(items);

        // Act & Assert
        assertEquals(1, order.getOrderId());
        assertEquals(10, order.getInvoiceId());
        assertEquals(150000.0f, order.getTotalAmount());
        assertEquals("PENDING", order.getStatus());
        assertEquals(2, order.getOrderItems().size());
    }

    @Test
    @DisplayName("Order Default Initialization")
    void testOrder_DefaultInitialization() {
        Order order = new Order();
        assertEquals(0, order.getOrderId());
        assertNotNull(order.getOrderItems(), "OrderItems should be initialized");
        assertTrue(order.getOrderItems().isEmpty(), "OrderItems should be empty");
    }
}
