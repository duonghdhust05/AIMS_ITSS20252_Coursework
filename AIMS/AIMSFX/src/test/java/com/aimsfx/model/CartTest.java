package com.aimsfx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Cart Model
 * Tests: View Cart, Update Cart, Add to Cart use cases
 * 
 * SOLID VERIFICATION:
 * - Tests ensure Cart maintains SRP (only cart operations)
 * - Tests verify data coupling with CartItem (no tight coupling)
 */
@DisplayName("Cart Model Tests")
class CartTest {

    private Cart cart;
    private Book testBook;
    private CD testCD;
    private DVD testDVD;

    @BeforeEach
    void setUp() {
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
        
        // Setup test DVD
        testDVD = new DVD();
        testDVD.setProductId(3L);
        testDVD.setBarcode("DVD-001");
        testDVD.setTitle("Test DVD");
        testDVD.setCurrentPrice(150000.0);
        testDVD.setStock(3);
        testDVD.setWeight(0.15);
    }

    // ==================== ADD TO CART TESTS ====================

    @Test
    @DisplayName("Add product to empty cart - success")
    void testAddProduct_ToEmptyCart_Success() {
        boolean result = cart.addProduct(testBook, 1);
        
        assertTrue(result, "Should add product successfully");
        assertEquals(1, cart.getItemCount(), "Cart should have 1 item");
        assertEquals(1, cart.getItems().get(0).getQuantity(), "Quantity should be 1");
    }

    @Test
    @DisplayName("Add product with quantity > 1")
    void testAddProduct_WithQuantityGreaterThanOne() {
        cart.addProduct(testBook, 3);
        
        assertEquals(1, cart.getItemCount(), "Cart should have 1 unique item");
        assertEquals(3, cart.getItems().get(0).getQuantity(), "Quantity should be 3");
    }

    @Test
    @DisplayName("Add same product (same barcode) increases quantity")
    void testAddProduct_SameBarcode_IncreasesQuantity() {
        cart.addProduct(testBook, 2);
        
        // Create another book with same barcode
        Book sameBook = new Book();
        sameBook.setProductId(100L); // Different ID but same barcode
        sameBook.setBarcode("BOOK-001"); // Same barcode
        sameBook.setTitle("Test Book");
        sameBook.setCurrentPrice(100000.0);
        sameBook.setStock(10);
        
        cart.addProduct(sameBook, 3);
        
        assertEquals(1, cart.getItemCount(), "Should still have 1 unique item");
        assertEquals(5, cart.getItems().get(0).getQuantity(), "Quantity should be 2+3=5");
    }

    @Test
    @DisplayName("Add different products creates separate items")
    void testAddProduct_DifferentProducts_CreatesSeparateItems() {
        cart.addProduct(testBook, 1);
        cart.addProduct(testCD, 2);
        cart.addProduct(testDVD, 1);
        
        assertEquals(3, cart.getItemCount(), "Cart should have 3 different items");
        assertEquals(4, cart.getTotalQuantity(), "Total quantity should be 1+2+1=4");
    }

    @Test
    @DisplayName("Add null product returns false")
    void testAddProduct_NullProduct_ReturnsFalse() {
        boolean result = cart.addProduct(null, 1);
        
        assertFalse(result, "Should return false for null product");
        assertTrue(cart.isEmpty(), "Cart should remain empty");
    }

    @Test
    @DisplayName("Add product with zero quantity returns false")
    void testAddProduct_ZeroQuantity_ReturnsFalse() {
        boolean result = cart.addProduct(testBook, 0);
        
        assertFalse(result, "Should return false for zero quantity");
        assertTrue(cart.isEmpty(), "Cart should remain empty");
    }

    @Test
    @DisplayName("Add product with negative quantity returns false")
    void testAddProduct_NegativeQuantity_ReturnsFalse() {
        boolean result = cart.addProduct(testBook, -5);
        
        assertFalse(result, "Should return false for negative quantity");
        assertTrue(cart.isEmpty(), "Cart should remain empty");
    }

    // ==================== VIEW CART TESTS ====================

    @Test
    @DisplayName("Get items returns all cart items")
    void testGetItems_ReturnsAllItems() {
        cart.addProduct(testBook, 1);
        cart.addProduct(testCD, 2);
        
        assertEquals(2, cart.getItems().size(), "Should return 2 items");
    }

    @Test
    @DisplayName("Get item count returns correct count")
    void testGetItemCount_ReturnsCorrectCount() {
        assertEquals(0, cart.getItemCount(), "Empty cart should have 0 items");
        
        cart.addProduct(testBook, 1);
        assertEquals(1, cart.getItemCount(), "Should have 1 item");
        
        cart.addProduct(testCD, 2);
        assertEquals(2, cart.getItemCount(), "Should have 2 items");
    }

    @Test
    @DisplayName("Get total quantity sums all quantities")
    void testGetTotalQuantity_SumsAllQuantities() {
        cart.addProduct(testBook, 2);
        cart.addProduct(testCD, 3);
        cart.addProduct(testDVD, 1);
        
        assertEquals(6, cart.getTotalQuantity(), "Total quantity should be 2+3+1=6");
    }

    @Test
    @DisplayName("Calculate subtotal correctly sums line totals")
    void testCalculateSubtotal_CorrectSum() {
        cart.addProduct(testBook, 2);  // 100000 * 2 = 200000
        cart.addProduct(testCD, 1);    // 80000 * 1 = 80000
        
        double expected = 200000.0 + 80000.0;
        assertEquals(expected, cart.calculateSubtotal(), 0.01, "Subtotal should be 280000");
    }

    @Test
    @DisplayName("isEmpty returns true for empty cart")
    void testIsEmpty_EmptyCart_ReturnsTrue() {
        assertTrue(cart.isEmpty(), "Empty cart should return true");
    }

    @Test
    @DisplayName("isEmpty returns false for non-empty cart")
    void testIsEmpty_NonEmptyCart_ReturnsFalse() {
        cart.addProduct(testBook, 1);
        assertFalse(cart.isEmpty(), "Non-empty cart should return false");
    }

    // ==================== UPDATE CART TESTS ====================

    @Test
    @DisplayName("Update quantity successfully")
    void testUpdateQuantity_Success() {
        cart.addProduct(testBook, 1);
        
        boolean result = cart.updateQuantity(testBook.getProductId(), 5);
        
        assertTrue(result, "Update should succeed");
        assertEquals(5, cart.getItems().get(0).getQuantity(), "Quantity should be updated to 5");
    }

    @Test
    @DisplayName("Update quantity to zero removes item")
    void testUpdateQuantity_ToZero_RemovesItem() {
        cart.addProduct(testBook, 1);
        
        boolean result = cart.updateQuantity(testBook.getProductId(), 0);
        
        assertTrue(result, "Update to 0 should succeed (removes item)");
        assertTrue(cart.isEmpty(), "Cart should be empty after removing item");
    }

    @Test
    @DisplayName("Update quantity for non-existent product returns false")
    void testUpdateQuantity_NonExistentProduct_ReturnsFalse() {
        cart.addProduct(testBook, 1);
        
        boolean result = cart.updateQuantity(999L, 5);
        
        assertFalse(result, "Should return false for non-existent product");
    }

    @Test
    @DisplayName("Update quantity by barcode works correctly")
    void testUpdateQuantityByBarcode_Success() {
        cart.addProduct(testBook, 2);
        
        boolean result = cart.updateQuantityByBarcode("BOOK-001", 10);
        
        assertTrue(result, "Update by barcode should succeed");
        assertEquals(10, cart.getItems().get(0).getQuantity(), "Quantity should be 10");
    }

    // ==================== REMOVE FROM CART TESTS ====================

    @Test
    @DisplayName("Remove product by ID successfully")
    void testRemoveProduct_ByProductId_Success() {
        cart.addProduct(testBook, 1);
        cart.addProduct(testCD, 1);
        
        boolean result = cart.removeProduct(testBook.getProductId());
        
        assertTrue(result, "Remove should succeed");
        assertEquals(1, cart.getItemCount(), "Cart should have 1 item left");
        assertEquals("Test CD", cart.getItems().get(0).getTitle(), "Remaining item should be CD");
    }

    @Test
    @DisplayName("Remove product by barcode successfully")
    void testRemoveProduct_ByBarcode_Success() {
        cart.addProduct(testBook, 1);
        cart.addProduct(testCD, 1);
        
        boolean result = cart.removeProductByBarcode("BOOK-001");
        
        assertTrue(result, "Remove by barcode should succeed");
        assertEquals(1, cart.getItemCount(), "Cart should have 1 item left");
    }

    @Test
    @DisplayName("Remove non-existent product returns false")
    void testRemoveProduct_NonExistent_ReturnsFalse() {
        cart.addProduct(testBook, 1);
        
        boolean result = cart.removeProduct(999L);
        
        assertFalse(result, "Should return false for non-existent product");
        assertEquals(1, cart.getItemCount(), "Cart should still have 1 item");
    }

    @Test
    @DisplayName("Clear cart removes all items")
    void testClear_RemovesAllItems() {
        cart.addProduct(testBook, 1);
        cart.addProduct(testCD, 2);
        cart.addProduct(testDVD, 3);
        
        cart.clear();
        
        assertTrue(cart.isEmpty(), "Cart should be empty after clear");
        assertEquals(0, cart.getItemCount(), "Item count should be 0");
    }

    // ==================== CART ITEM RETRIEVAL TESTS ====================

    @Test
    @DisplayName("Get cart item by product ID")
    void testGetCartItem_ByProductId_ReturnsCorrectItem() {
        cart.addProduct(testBook, 2);
        cart.addProduct(testCD, 1);
        
        CartItem item = cart.getCartItem(testBook.getProductId());
        
        assertNotNull(item, "Should return the cart item");
        assertEquals("Test Book", item.getTitle(), "Should be the correct product");
        assertEquals(2, item.getQuantity(), "Should have correct quantity");
    }

    @Test
    @DisplayName("Get cart item by barcode")
    void testGetCartItemByBarcode_ReturnsCorrectItem() {
        cart.addProduct(testBook, 3);
        
        CartItem item = cart.getCartItemByBarcode("BOOK-001");
        
        assertNotNull(item, "Should return the cart item");
        assertEquals(3, item.getQuantity(), "Should have correct quantity");
    }

    @Test
    @DisplayName("Get product by ID returns product")
    void testGetProduct_ByProductId_ReturnsProduct() {
        cart.addProduct(testBook, 1);
        
        Product product = cart.getProduct(testBook.getProductId());
        
        assertNotNull(product, "Should return the product");
        assertEquals("Test Book", product.getTitle(), "Should be the correct product");
    }

    // ==================== AVAILABILITY TESTS ====================

    @Test
    @DisplayName("Check availability returns true when all items in stock")
    void testCheckAvailability_AllInStock_ReturnsTrue() {
        cart.addProduct(testBook, 5);  // Stock is 10, requesting 5
        cart.addProduct(testCD, 3);    // Stock is 5, requesting 3
        
        assertTrue(cart.checkAvailability(), "All items should be available");
    }

    @Test
    @DisplayName("Check availability returns false when item exceeds stock")
    void testCheckAvailability_ExceedsStock_ReturnsFalse() {
        cart.addProduct(testBook, 15); // Stock is 10, requesting 15 - exceeds!
        
        assertFalse(cart.checkAvailability(), "Should return false when exceeding stock");
    }
}
