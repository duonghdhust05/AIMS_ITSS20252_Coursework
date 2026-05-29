package com.aimsfx.service;

import com.aimsfx.model.Cart;
import com.aimsfx.model.CartItem;
import com.aimsfx.repository.ProductRepository;

import java.util.List;
import java.util.Map;

/**
 * ICartService - Interface for Cart Business Logic Operations
 * 
 * DESIGN PRINCIPLE: Dependency Inversion Principle (DIP)
 * - High-level modules (Controllers) depend on this abstraction
 * - Low-level module (CartService) implements this interface
 * 
 * RESPONSIBILITIES:
 * - Cart calculations (subtotal, weight, VAT)
 * - Stock validation
 * - Price formatting
 * 
 * SOLID COMPLIANCE:
 * - SRP: Only cart-related business logic
 * - ISP: Focused interface for cart operations
 */
public interface ICartService {
    
    // ==================== CALCULATIONS ====================
    
    /**
     * Calculate subtotal from cart items (excluding VAT)
     * @param items List of cart items
     * @return Subtotal amount
     */
    double calculateSubtotal(List<CartItem> items);
    
    /**
     * Calculate subtotal from cart
     * @param cart Shopping cart
     * @return Subtotal amount
     */
    double calculateSubtotal(Cart cart);
    
    /**
     * Calculate VAT (10% on products only, NOT on shipping)
     * @param subtotal Product subtotal
     * @return VAT amount
     */
    double calculateVAT(double subtotal);
    
    /**
     * Calculate total weight of cart items
     * @param cart Cart with products
     * @return Total weight in kg
     */
    float calculateTotalWeight(Cart cart);
    
    // ==================== STOCK VALIDATION ====================
    
    /**
     * Check product availability for all items in cart
     * @param cart The shopping cart
     * @return Error message if any product is out of stock, null if all available
     */
    String checkProductAvailability(Cart cart);
    
    /**
     * Get detailed list of products with insufficient stock
     * Used by Controller to display detailed error UI
     * @param cart The shopping cart to check
     * @return List of maps with product details (empty if all products available)
     */
    List<Map<String, Object>> getInsufficientStockItems(Cart cart);
    
    /**
     * Check stock availability for all cart items with database refresh
     * Uses internally managed repository (DIP COMPLIANT)
     * 
     * @param cart Cart to check
     * @return List of insufficient items with details (empty if all available)
     */
    List<Map<String, Object>> checkCartStockWithDatabaseRefresh(Cart cart);
    
    /**
     * Check stock availability for all cart items with database refresh
     * Fetches latest stock from database to prevent race conditions
     * @param cart Cart to check
     * @param productRepository Repository to fetch latest stock
     * @return List of insufficient items with details (empty if all available)
     * @deprecated Use checkCartStockWithDatabaseRefresh(Cart cart) instead for DIP compliance
     */
    @Deprecated
    List<Map<String, Object>> checkCartStockWithDatabaseRefresh(Cart cart, ProductRepository productRepository);
    
    // ==================== STOCK UPDATE ====================
    
    /**
     * Update product stock from cart after order completion
     * @param cart The shopping cart containing products
     */
    void updateProductStockFromCart(Cart cart);
    
    // ==================== FORMATTING ====================
    
    /**
     * Format price for display
     * @param price Price value
     * @return Formatted price string
     */
    String formatPrice(double price);
}
