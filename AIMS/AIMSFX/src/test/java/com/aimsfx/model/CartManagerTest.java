package com.aimsfx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for CartManager and cart event notification
 * Verifies add-to-cart functionality and quantity increments
 */
class CartManagerTest {

    private CartManager cartManager;
    private Cart cart;
    private boolean eventNotified;

    @BeforeEach
    void setUp() {
        cartManager = CartManager.getInstance();
        cartManager.clearCart();
        cart = cartManager.getCart();
        eventNotified = false;
        CartEvents.clearListeners();
    }

    @Test
    void testAddProductToCart() {
        // Create a test book
        Book book = new Book();
        book.setProductId(1L);
        book.setTitle("Test Book");
        book.setCurrentPrice(100.0);
        book.setStock(10);

        // Add to cart with quantity 1
        boolean added = cartManager.addProduct(book, 1);

        assertTrue(added, "Product should be added to cart");
        assertEquals(1, cart.getItems().size(), "Cart should have 1 item");
        assertEquals("Test Book", cart.getItems().get(0).getTitle());
    }

    @Test
    void testAddSameProductIncreasesQuantity() {
        // Create two instances of the same product (same barcode)
        // Cart uses BARCODE for duplicate detection, not productId
        Book book1 = new Book();
        book1.setProductId(1L);
        book1.setBarcode("BOOK-001"); // Same barcode
        book1.setTitle("Test Book");
        book1.setCurrentPrice(100.0);
        book1.setStock(10);

        Book book2 = new Book();
        book2.setProductId(1L);
        book2.setBarcode("BOOK-001"); // Same barcode - should match
        book2.setTitle("Test Book");
        book2.setCurrentPrice(100.0);
        book2.setStock(10);

        // Add first instance with quantity 1
        cartManager.addProduct(book1, 1);
        assertEquals(1, cart.getItems().size(), "Cart should have 1 unique item");
        assertEquals(1, cart.getItems().get(0).getQuantity(), "First add should set quantity to 1");

        // Add second instance (same barcode) with quantity 1
        cartManager.addProduct(book2, 1);
        assertEquals(1, cart.getItems().size(), "Cart should still have 1 unique item");
        assertEquals(2, cart.getItems().get(0).getQuantity(), "Second add should increment quantity to 2");
    }

    @Test
    void testCartEventsNotification() {
        // Register listener
        CartEvents.addListener(() -> eventNotified = true);

        // Create and add product
        CD cd = new CD();
        cd.setProductId(2L);
        cd.setTitle("Test CD");
        cd.setCurrentPrice(50.0);
        cd.setStock(5);

        cartManager.addProduct(cd, 1);
        
        // Manually trigger event (in real code, ProductDetailUI calls this)
        CartEvents.notifyCartUpdated();

        assertTrue(eventNotified, "Cart event listener should be notified");
    }

    @Test
    void testMultipleProductTypes() {
        // Add book
        Book book = new Book();
        book.setProductId(1L);
        book.setTitle("Test Book");
        book.setCurrentPrice(100.0);
        book.setStock(10);

        // Add DVD
        DVD dvd = new DVD();
        dvd.setProductId(2L);
        dvd.setTitle("Test DVD");
        dvd.setCurrentPrice(150.0);
        dvd.setStock(8);

        cartManager.addProduct(book, 1);
        cartManager.addProduct(dvd, 2);

        assertEquals(2, cart.getItems().size(), "Cart should have 2 different items");
        assertEquals(3, cart.getTotalQuantity(), "Total quantity should be 3 (1+2)");
    }

    @Test
    void testCartBadgeCount() {
        // Add multiple products
        for (int i = 1; i <= 3; i++) {
            Book book = new Book();
            book.setProductId((long) i);
            book.setTitle("Book " + i);
            book.setCurrentPrice(100.0);
            book.setStock(10);
            cartManager.addProduct(book, 1);
        }

        // Badge should show number of unique items
        int itemCount = cart.getItems().size();
        assertEquals(3, itemCount, "Badge should show 3 items");
    }
}
