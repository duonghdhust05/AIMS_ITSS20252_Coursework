package com.aimsfx.model;

/**
 * ProductStatus - Enum representing product status
 * 
 * PURPOSE: Type-safe representation of product status values
 * 
 * VALUES:
 * - AVAILABLE: Product is active and available for sale
 * - DEACTIVATED: Product is deactivated (stock > 0 but cannot be sold)
 */
public enum ProductStatus {
    AVAILABLE("available"),
    DEACTIVATED("deactivated");
    
    private final String value;
    
    ProductStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get ProductStatus from string value
     * @param value String value (case-insensitive)
     * @return ProductStatus enum
     * @throws IllegalArgumentException if value is invalid
     */
    public static ProductStatus fromString(String value) {
        if (value == null) {
            return AVAILABLE; // Default value
        }
        
        for (ProductStatus status : ProductStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        
        // For backward compatibility with old values
        String lowerValue = value.toLowerCase();
        if (lowerValue.contains("available") || lowerValue.equals("in stock")) {
            return AVAILABLE;
        }
        if (lowerValue.contains("deactivat") || lowerValue.contains("discontinued")) {
            return DEACTIVATED;
        }
        
        throw new IllegalArgumentException("Invalid product status: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
