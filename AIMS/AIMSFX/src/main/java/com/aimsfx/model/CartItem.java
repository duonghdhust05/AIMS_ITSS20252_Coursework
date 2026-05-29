package com.aimsfx.model;

/**
 * CartItem Class
 * Purpose: Wraps a Product with quantity information for shopping cart.
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All attributes relate to a single cart item: product reference and quantity
 * - Single responsibility: Associate quantity with a product in cart context
 * 
 * COUPLING: LOW - Data Coupling
 * - Only depends on Product for product data
 * - Simple wrapper class with minimal dependencies
 */
public class CartItem {
    
    private Product product;
    private int quantity;
    
    // ==================== Constructors ====================
    
    public CartItem() {
        this.quantity = 0;
    }
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    
    // ==================== Getters and Setters ====================
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    // ==================== Convenience Methods (delegate to Product) ====================
    
    /**
     * Get product ID from wrapped product
     * @return Product ID or null if product is null
     */
    public Long getProductId() {
        return product != null ? product.getProductId() : null;
    }
    
    /**
     * Get product barcode from wrapped product
     * @return Product barcode or null if product is null
     */
    public String getBarcode() {
        return product != null ? product.getBarcode() : null;
    }
    
    /**
     * Get product title from wrapped product
     * @return Product title or null if product is null
     */
    public String getTitle() {
        return product != null ? product.getTitle() : null;
    }
    
    /**
     * Get current price from wrapped product
     * @return Current price or 0 if product is null
     */
    public double getCurrentPrice() {
        return product != null ? product.getCurrentPrice() : 0;
    }
    
    /**
     * Get weight from wrapped product
     * @return Weight or 0 if product is null
     */
    public double getWeight() {
        return product != null ? product.getWeight() : 0;
    }
    
    /**
     * Get stock from wrapped product
     * @return Stock or 0 if product is null
     */
    public int getStock() {
        return product != null ? product.getStock() : 0;
    }
    
    /**
     * Check if requested quantity is available in stock
     * @return true if stock >= quantity
     */
    public boolean isAvailable() {
        return product != null && product.checkAvailability(quantity);
    }
    
    /**
     * Calculate line total (quantity * price)
     * @return Line total amount
     */
    public double getLineTotal() {
        return getCurrentPrice() * quantity;
    }
    
    @Override
    public String toString() {
        return "CartItem{" +
                "product=" + (product != null ? product.getTitle() : "null") +
                ", quantity=" + quantity +
                ", lineTotal=" + getLineTotal() +
                '}';
    }
}
