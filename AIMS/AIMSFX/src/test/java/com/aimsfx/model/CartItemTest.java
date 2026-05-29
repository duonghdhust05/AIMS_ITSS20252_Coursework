package com.aimsfx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for CartItem Model
 * Verifies wrapper functionality and line total calculations
 */
@DisplayName("CartItem Model Tests")
class CartItemTest {

    private CartItem cartItem;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setProductId(1L);
        testBook.setBarcode("BOOK-001");
        testBook.setTitle("Test Book");
        testBook.setCurrentPrice(150000.0);
        testBook.setStock(20);
        testBook.setWeight(0.5);
        
        cartItem = new CartItem(testBook, 3);
    }

    @Test
    @DisplayName("Constructor initializes correctly")
    void testConstructor_InitializesCorrectly() {
        assertEquals(testBook, cartItem.getProduct(), "Product should be set");
        assertEquals(3, cartItem.getQuantity(), "Quantity should be 3");
    }

    @Test
    @DisplayName("Get product ID delegates to product")
    void testGetProductId_DelegatesToProduct() {
        assertEquals(1L, cartItem.getProductId(), "Should return product ID");
    }

    @Test
    @DisplayName("Get barcode delegates to product")
    void testGetBarcode_DelegatesToProduct() {
        assertEquals("BOOK-001", cartItem.getBarcode(), "Should return barcode");
    }

    @Test
    @DisplayName("Get title delegates to product")
    void testGetTitle_DelegatesToProduct() {
        assertEquals("Test Book", cartItem.getTitle(), "Should return title");
    }

    @Test
    @DisplayName("Get current price delegates to product")
    void testGetCurrentPrice_DelegatesToProduct() {
        assertEquals(150000.0, cartItem.getCurrentPrice(), 0.01, "Should return price");
    }

    @Test
    @DisplayName("Get weight delegates to product")
    void testGetWeight_DelegatesToProduct() {
        assertEquals(0.5, cartItem.getWeight(), 0.01, "Should return weight");
    }

    @Test
    @DisplayName("Get stock delegates to product")
    void testGetStock_DelegatesToProduct() {
        assertEquals(20, cartItem.getStock(), "Should return stock");
    }

    @Test
    @DisplayName("Calculate line total correctly")
    void testGetLineTotal_CalculatesCorrectly() {
        // Price 150000 * quantity 3 = 450000
        double expected = 150000.0 * 3;
        assertEquals(expected, cartItem.getLineTotal(), 0.01, "Line total should be 450000");
    }

    @Test
    @DisplayName("Update quantity and recalculate line total")
    void testSetQuantity_UpdatesLineTotal() {
        cartItem.setQuantity(5);
        
        assertEquals(5, cartItem.getQuantity(), "Quantity should be updated");
        double expected = 150000.0 * 5;
        assertEquals(expected, cartItem.getLineTotal(), 0.01, "Line total should recalculate");
    }

    @Test
    @DisplayName("Is available returns true when stock sufficient")
    void testIsAvailable_StockSufficient_ReturnsTrue() {
        // Stock is 20, quantity is 3
        assertTrue(cartItem.isAvailable(), "Should be available");
    }

    @Test
    @DisplayName("Is available returns false when stock insufficient")
    void testIsAvailable_StockInsufficient_ReturnsFalse() {
        cartItem.setQuantity(25); // Exceeds stock of 20
        assertFalse(cartItem.isAvailable(), "Should not be available");
    }

    @Test
    @DisplayName("Null product returns safe defaults")
    void testNullProduct_ReturnsSafeDefaults() {
        CartItem emptyItem = new CartItem(null, 1);
        
        assertNull(emptyItem.getProductId(), "ProductId should be null");
        assertNull(emptyItem.getBarcode(), "Barcode should be null");
        assertNull(emptyItem.getTitle(), "Title should be null");
        assertEquals(0, emptyItem.getCurrentPrice(), 0.01, "Price should be 0");
        assertEquals(0, emptyItem.getWeight(), 0.01, "Weight should be 0");
        assertEquals(0, emptyItem.getStock(), "Stock should be 0");
        assertEquals(0, emptyItem.getLineTotal(), 0.01, "Line total should be 0");
    }
}
