package com.aimsfx.model;

import com.aimsfx.exception.EmptyCartException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1: Models Layer
 * Test Suite: TS_PLO_MODELS
 */
public class OrderModelTest {

    @Test
    void testUT_PLO_02_OrderCreationFromCart() throws EmptyCartException {
        // Arrange
        Cart cart = new Cart(1, 1);
        
        Book book = new Book();
        book.setProductId(1L);
        book.setTitle("Test Book");
        book.setCurrentPrice(100000.0);
        book.setWeight(0.5);
        
        CD cd = new CD();
        cd.setProductId(2L);
        cd.setTitle("Test CD");
        cd.setCurrentPrice(50000.0);
        cd.setWeight(0.1);
        
        cart.addProduct(book, 2); // 200,000 | 1kg
        cart.addProduct(cd, 1);   // 50,000  | 0.1kg
        
        // Act
        Order order = new Order(cart);
        
        // Assert
        assertEquals(2, order.getOrderItems().size());
        assertEquals("new", order.getStatus());
        assertNotNull(order.getCreatedDate());
        assertEquals(250000.0f, order.getSubtotal(), 0.01f);
        assertEquals(250000.0f, order.getTotalAmount(), 0.01f);
        assertEquals(1.1f, order.calculateTotalWeight(), 0.01f);
    }
}
