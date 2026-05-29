package com.aimsfx.model;

/**
 * ICartManager - Interface for Cart Management Operations
 * 
 * DESIGN PRINCIPLE: Dependency Inversion Principle (DIP)
 * - High-level modules (Controllers, Views) depend on this abstraction
 * - Low-level module (CartManager) implements this interface
 * 
 * BENEFITS:
 * - Enables unit testing with mock implementations
 * - Decouples cart operations from concrete implementation
 * - Supports future alternative cart implementations (e.g., persistent cart)
 */
public interface ICartManager {
    
    /**
     * Get current cart
     * @return Current Cart instance
     */
    Cart getCart();
    
    /**
     * Set cart
     * @param cart Cart to set
     */
    void setCart(Cart cart);
    
    /**
     * Add product to cart with specified quantity
     * @param product Product to add
     * @param quantity Quantity to add
     * @return true if added successfully
     */
    boolean addProduct(Product product, int quantity);
    
    /**
     * Add CartItem directly to cart
     * @param cartItem CartItem to add
     * @return true if added successfully
     */
    boolean addCartItem(CartItem cartItem);
    
    /**
     * Remove product from cart
     * @param productId Product ID to remove
     * @return true if removed successfully
     */
    boolean removeProduct(Long productId);
    
    /**
     * Update product quantity in cart
     * @param productId Product ID
     * @param quantity New quantity
     * @return true if updated successfully
     */
    boolean updateQuantity(Long productId, int quantity);
    
    /**
     * Clear all items from cart
     */
    void clearCart();
    
    /**
     * Refresh cart products with latest data from database
     * Updates price, stock, and other product info for each cart item
     * Removes items if product no longer exists in database
     * @return Number of items that were updated
     */
    int refreshCartProducts();
}
