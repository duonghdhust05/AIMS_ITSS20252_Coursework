package com.aimsfx.service;

import com.aimsfx.model.Book;
import com.aimsfx.model.Cart;
import com.aimsfx.model.CD;
import com.aimsfx.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for CartService
 * Tests business logic: calculations, stock validation
 * 
 * SOLID VERIFICATION:
 * - Tests verify DIP compliance with mock repository injection
 * - Tests ensure SRP (business logic only)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock
    private ProductRepository mockProductRepository;

    private CartService cartService;
    private Cart cart;
    private Book testBook;
    private CD testCD;

    @BeforeEach
    void setUp() {
        // Use DI constructor to inject mock repository
        cartService = new CartService(mockProductRepository);

        cart = new Cart(1, 1);

        // Setup test book
        testBook = new Book();
        testBook.setProductId(1L);
        testBook.setBarcode("BOOK-001");
        testBook.setTitle("Test Book");
        testBook.setCurrentPrice(100000.0);
        testBook.setStock(10);
        testBook.setWeight(0.5);

        // Setup test CD
        testCD = new CD();
        testCD.setProductId(2L);
        testCD.setBarcode("CD-001");
        testCD.setTitle("Test CD");
        testCD.setCurrentPrice(80000.0);
        testCD.setStock(5);
        testCD.setWeight(0.1);
    }

    // ==================== CALCULATION TESTS ====================

    @Test
    @DisplayName("Calculate subtotal from cart")
    void testCalculateSubtotal_FromCart() {
        cart.addProduct(testBook, 2);  // 100000 * 2 = 200000
        cart.addProduct(testCD, 3);    // 80000 * 3 = 240000

        double subtotal = cartService.calculateSubtotal(cart);

        assertEquals(440000.0, subtotal, 0.01, "Subtotal should be 440000");
    }

    @Test
    @DisplayName("Calculate subtotal from cart items list")
    void testCalculateSubtotal_FromItemsList() {
        cart.addProduct(testBook, 1);
        cart.addProduct(testCD, 2);

        double subtotal = cartService.calculateSubtotal(cart.getItems());

        double expected = 100000.0 + (80000.0 * 2);
        assertEquals(expected, subtotal, 0.01, "Subtotal should be correct");
    }

    @Test
    @DisplayName("Calculate subtotal for empty cart returns 0")
    void testCalculateSubtotal_EmptyCart_ReturnsZero() {
        assertEquals(0, cartService.calculateSubtotal(cart), 0.01, "Empty cart subtotal should be 0");
    }

    @Test
    @DisplayName("Calculate subtotal for null cart returns 0")
    void testCalculateSubtotal_NullCart_ReturnsZero() {
        assertEquals(0, cartService.calculateSubtotal((Cart) null), 0.01, "Null cart subtotal should be 0");
    }

    @Test
    @DisplayName("Calculate VAT at 10%")
    void testCalculateVAT_TenPercent() {
        double subtotal = 100000.0;
        double vat = cartService.calculateVAT(subtotal);

        assertEquals(10000.0, vat, 0.01, "VAT should be 10% of subtotal");
    }

    @Test
    @DisplayName("Calculate total weight from cart")
    void testCalculateTotalWeight_FromCart() {
        cart.addProduct(testBook, 2);  // 0.5 * 2 = 1.0 kg
        cart.addProduct(testCD, 3);    // 0.1 * 3 = 0.3 kg

        float totalWeight = cartService.calculateTotalWeight(cart);

        assertEquals(1.3f, totalWeight, 0.01, "Total weight should be 1.3 kg");
    }

    // ==================== STOCK VALIDATION TESTS ====================

    @Test
    @DisplayName("Check product availability - all in stock")
    void testCheckProductAvailability_AllInStock_ReturnsNull() {
        cart.addProduct(testBook, 5);  // Stock 10, requesting 5
        cart.addProduct(testCD, 3);    // Stock 5, requesting 3

        String result = cartService.checkProductAvailability(cart);

        assertNull(result, "Should return null when all products available");
    }

    @Test
    @DisplayName("Check product availability - insufficient stock")
    void testCheckProductAvailability_InsufficientStock_ReturnsMessage() {
        cart.addProduct(testBook, 15); // Stock 10, requesting 15 - exceeds!

        String result = cartService.checkProductAvailability(cart);

        assertNotNull(result, "Should return error message");
        assertTrue(result.contains("Test Book"), "Message should contain product name");
    }

    @Test
    @DisplayName("Get insufficient stock items returns details")
    void testGetInsufficientStockItems_ReturnsDetails() {
        cart.addProduct(testBook, 15); // Exceeds stock of 10
        cart.addProduct(testCD, 3);    // Within stock

        List<Map<String, Object>> result = cartService.getInsufficientStockItems(cart);

        assertEquals(1, result.size(), "Should have 1 insufficient item");
        assertEquals("Test Book", result.get(0).get("title"), "Should be the book");
        assertEquals(15, result.get(0).get("requestedQty"), "Requested qty should be 15");
        assertEquals(10, result.get(0).get("availableQty"), "Available qty should be 10");
    }

    @Test
    @DisplayName("Get insufficient stock items - all available returns empty list")
    void testGetInsufficientStockItems_AllAvailable_ReturnsEmptyList() {
        cart.addProduct(testBook, 5);  // Within stock
        cart.addProduct(testCD, 3);    // Within stock

        List<Map<String, Object>> result = cartService.getInsufficientStockItems(cart);

        assertTrue(result.isEmpty(), "Should return empty list when all available");
    }

    // ==================== DATABASE REFRESH TESTS ====================

    @Test
    @DisplayName("Check stock with database refresh - uses injected repository")
    void testCheckCartStockWithDatabaseRefresh_UsesInjectedRepository() {
        cart.addProduct(testBook, 5);

        // Mock repository to return updated product
        when(mockProductRepository.findById(1L)).thenReturn(Optional.of(testBook));

        List<Map<String, Object>> result = cartService.checkCartStockWithDatabaseRefresh(cart);

        assertTrue(result.isEmpty(), "Should return empty when stock sufficient");
        verify(mockProductRepository).findById(1L);
    }

    @Test
    @DisplayName("Check stock with database refresh - product not found")
    void testCheckCartStockWithDatabaseRefresh_ProductNotFound() {
        cart.addProduct(testBook, 5);

        // Mock repository to return empty (product deleted)
        when(mockProductRepository.findById(1L)).thenReturn(Optional.empty());

        List<Map<String, Object>> result = cartService.checkCartStockWithDatabaseRefresh(cart);

        assertEquals(1, result.size(), "Should have 1 item");
        assertEquals("Product no longer available", result.get(0).get("error"));
    }

    @Test
    @DisplayName("Check stock with database refresh - stock changed")
    void testCheckCartStockWithDatabaseRefresh_StockChanged() {
        cart.addProduct(testBook, 8);

        // Mock repository to return product with reduced stock
        Book updatedBook = new Book();
        updatedBook.setProductId(1L);
        updatedBook.setTitle("Test Book");
        updatedBook.setStock(3); // Only 3 left now!
        when(mockProductRepository.findById(1L)).thenReturn(Optional.of(updatedBook));

        List<Map<String, Object>> result = cartService.checkCartStockWithDatabaseRefresh(cart);

        assertEquals(1, result.size(), "Should have 1 insufficient item");
        assertEquals(3, result.get(0).get("availableQty"), "Should show updated stock");
    }

    // ==================== FORMATTING TESTS ====================

    @Test
    @DisplayName("Format price with Vietnamese locale")
    void testFormatPrice_VietnameseFormat() {
        String formatted = cartService.formatPrice(1234567.89);

        assertNotNull(formatted, "Should return formatted string");
        // Vietnamese format uses dot or comma as thousand separator
        assertTrue(formatted.contains("1") && formatted.contains("234"), 
                "Should format with thousand separators");
    }

    // ==================== HELPER METHOD TESTS ====================

    @Test
    @DisplayName("Get item count from cart")
    void testGetItemCount() {
        cart.addProduct(testBook, 1);
        cart.addProduct(testCD, 2);

        assertEquals(2, cartService.getItemCount(cart), "Should have 2 items");
    }

    @Test
    @DisplayName("Get total quantity from cart")
    void testGetTotalQuantity() {
        cart.addProduct(testBook, 3);
        cart.addProduct(testCD, 2);

        assertEquals(5, cartService.getTotalQuantity(cart), "Total quantity should be 5");
    }

    @Test
    @DisplayName("Is cart empty check")
    void testIsCartEmpty() {
        assertTrue(cartService.isCartEmpty(cart), "Empty cart should return true");

        cart.addProduct(testBook, 1);
        assertFalse(cartService.isCartEmpty(cart), "Non-empty cart should return false");
    }

    @Test
    @DisplayName("Calculate total with VAT")
    void testCalculateTotalWithVAT() {
        cart.addProduct(testBook, 1);  // 100000

        double total = cartService.calculateTotalWithVAT(cart);

        double expected = 100000.0 + (100000.0 * 0.1); // subtotal + 10% VAT
        assertEquals(expected, total, 0.01, "Total should include 10% VAT");
    }
}
