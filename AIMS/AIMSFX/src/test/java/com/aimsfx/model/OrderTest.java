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
    @DisplayName("[UT-PO-01] Create new Order successfully with valid data")
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

    @Test
    @DisplayName("[UT_PAY_001] Validate Order Total Calculation")
    void testCalculateTotalAmount() {
        // Arrange
        Order order = new Order();
        
        Product p1 = org.mockito.Mockito.mock(Product.class);
        org.mockito.Mockito.when(p1.getCurrentPrice()).thenReturn(50.0);
        Product p2 = org.mockito.Mockito.mock(Product.class);
        org.mockito.Mockito.when(p2.getCurrentPrice()).thenReturn(30.0);
        
        OrderItem item1 = new OrderItem(p1, 1, 50.0);
        OrderItem item2 = new OrderItem(p2, 1, 30.0);
        
        order.addOrderItem(item1);
        order.addOrderItem(item2);
        
        // Subtotal should be 80.0
        assertEquals(80.0f, order.calculateSubtotal());
        
        // Set delivery fee to 10.0
        order.setDeliveryFee(10.0f);
        
        // Calculate total amount
        // Total = 80.0 + (80.0 * 0.1) + 10.0 = 80.0 + 8.0 + 10.0 = 98.0
        float total = order.calculateTotalAmount();
        
        // Assert
        assertEquals(98.0f, total, 0.001f);
    }
}
