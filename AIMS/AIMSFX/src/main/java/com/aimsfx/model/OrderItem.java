package com.aimsfx.model;

/**
 * OrderItem Class
 * Purpose: Represents a line item in an order (product + quantity + price snapshot)
 * 
 * ✅ SOLID COMPLIANCE - Refactoring from nested class ✅
 * 
 * ✅ SRP COMPLIANT: Single responsibility - Manage order line item data and calculations
 * ✅ OCP COMPLIANT: Can extend through inheritance if needed (e.g., DiscountedOrderItem)
 * ✅ LSP COMPLIANT: No inheritance hierarchy (N/A)
 * ✅ ISP COMPLIANT: No unnecessary interfaces (N/A)
 * ✅ DIP COMPLIANT: Depends on Product abstraction
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods relate to managing a single order line item
 * - Single responsibility: Represent and calculate order item details
 * - All attributes work together to maintain order item state
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with Product
 *    - Uses: Product reference to store item details
 *    - Type: Data coupling - only stores reference and uses basic getters
 *    - Justification: Need product information for order fulfillment
 * 
 * Overall: LOW COUPLING - Minimal dependencies, only requires Product reference
 * 
 * REFACTORING NOTES:
 * - Extracted from Order as nested class to improve separation of concerns
 * - Now reusable across multiple contexts (Order, Invoice, Email templates)
 * - Easier to test independently
 * - Follows Entity pattern as standalone business object
 */
public class OrderItem {
    
    private Product product;
    private int quantity;
    private double price;        // Price snapshot at time of order
    
    // ==================== Constructors ====================
    
    /**
     * Default constructor
     */
    public OrderItem() {
    }
    
    /**
     * Constructor with all fields
     * @param product Product reference
     * @param quantity Quantity ordered
     * @param price Price snapshot at order time
     */
    public OrderItem(Product product, int quantity, double price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
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
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    // ==================== Business Methods ====================
    
    /**
     * Calculate line total (quantity * price)
     * @return Total amount for this line item
     */
    public double getLineTotal() {
        return quantity * price;
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "product=" + (product != null ? product.getTitle() : "null") +
                ", quantity=" + quantity +
                ", price=" + price +
                ", lineTotal=" + getLineTotal() +
                '}';
    }
}
