package com.aimsfx.model;

/**
 * ProductType - Type-safe enumeration for product categories
 * 
 * USAGE:
 * Instead of: String type = "BOOK";
 * Use: ProductType type = ProductType.BOOK;
 */
public enum ProductType {
    BOOK,
    CD,
    DVD,
    NEWSPAPER;
    
    /**
     * Convert string to ProductType (for backward compatibility with database/UI)
     * @param type String representation of product type
     * @return ProductType enum value
     * @throws IllegalArgumentException if type is invalid
     */
    public static ProductType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }
        try {
            return ProductType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid product type: " + type + 
                ". Valid types are: BOOK, CD, DVD, NEWSPAPER");
        }
    }
    
    /**
     * Get string representation (for database/UI compatibility)
     * @return String representation
     */
    @Override
    public String toString() {
        return this.name();
    }
}
