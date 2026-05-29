package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.factory.PhysicalProductFactory;
import com.aimsfx.factory.ProductFactory;
import com.aimsfx.factory.ProductFactoryRegistry;

/**
 * CommonProductValidator - Validates common product fields
 * 
 * SRP: Handles validation of fields common to all product types
 * Used by ProductService before delegating to type-specific validators
 * 
 * OCP: Weight and dimensions validation is conditional based on factory type:
 * - PhysicalProductFactory: weight and dimensions are required
 * - Non-physical factories (future digital products): weight/dimensions can be null
 */
public class CommonProductValidator {
    
    // Price range constants for update validation
    private static final double MIN_PRICE_RATIO = 0.30; // 30% of originalPrice
    private static final double MAX_PRICE_RATIO = 1.50; // 150% of originalPrice
    
    /**
     * Validate common product fields that all products must have
     * 
     * NOTE: Weight and dimensions are validated conditionally:
     * - Required for physical products (PhysicalProductFactory)
     * - Optional (can be null) for digital products
     * 
     * @throws InvalidProductDataException if any validation fails
     */
    public void validateCommonFields(String type, String barcode, String title,
                                     Double originalPrice, Double currentPrice,
                                     String category, Double weight,
                                     String dimensions, Integer stock) throws InvalidProductDataException {
        
        if (type == null || type.isBlank()) {
            throw new InvalidProductDataException("Product type is required");
        }
        
        if (title == null || title.isBlank()) {
            throw new InvalidProductDataException("Product title is required");
        }
        
        if (barcode == null || barcode.isBlank()) {
            throw new InvalidProductDataException("Product barcode is required");
        }
        
        // Validate barcode length (8-15 characters)
        if (barcode.length() < 8 || barcode.length() > 15) {
            throw new InvalidProductDataException("Barcode must be between 8 and 15 characters");
        }
        
        if (originalPrice == null || originalPrice < 0) {
            throw new InvalidProductDataException("Original price must be non-negative");
        }
        
        if (currentPrice != null && currentPrice < 0) {
            throw new InvalidProductDataException("Current price must be non-negative");
        }
        
        // Validate category
        if (category == null || category.isBlank()) {
            throw new InvalidProductDataException("Category is required");
        }
        
        // Determine if this is a physical product type
        boolean isPhysical = isPhysicalProductType(type);
        
        // Validate weight - ONLY for physical products
        if (isPhysical) {
            if (weight == null) {
                throw new InvalidProductDataException("Weight is required for physical products");
            }
            if (weight < 0) {
                throw new InvalidProductDataException("Weight must be non-negative");
            }
            
            // Validate dimensions - ONLY for physical products
            if (dimensions == null || dimensions.isBlank()) {
                throw new InvalidProductDataException("Dimensions is required for physical products");
            }
            
            // Validate dimensions format (LxWxH, e.g., 10x20x5 or 10.5x20.5x5.5)
            if (!dimensions.matches("^\\d+(\\.\\d+)?x\\d+(\\.\\d+)?x\\d+(\\.\\d+)?$")) {
                throw new InvalidProductDataException("Dimensions must be in format LxWxH (e.g., 10x20x5 or 10.5x20.5x5.5)");
            }
        }
        // For non-physical products: weight and dimensions can be null - no validation needed
        
        // Validate stock
        if (stock == null) {
            throw new InvalidProductDataException("Stock is required");
        }
        if (stock < 0) {
            throw new InvalidProductDataException("Stock must be non-negative");
        }
    }
    
    /**
     * Check if the product type is a physical product
     * Uses factory registry to determine if factory is PhysicalProductFactory
     * 
     * @param type Product type string
     * @return true if physical product, false otherwise
     */
    private boolean isPhysicalProductType(String type) {
        try {
            ProductFactory factory = ProductFactoryRegistry.getFactory(type);
            return factory instanceof PhysicalProductFactory;
        } catch (Exception e) {
            // If factory not found, assume physical for backward compatibility
            return true;
        }
    }
    
    /**
     * Validate price range for product add/update
     * 
     * BUSINESS RULE: When adding/updating product price, currentPrice must be
     * between 30% and 150% of originalPrice to prevent unreasonable pricing.
     * 
     * @param originalPrice The original/base price of the product
     * @param currentPrice  The new current/selling price
     * @throws InvalidProductDataException if currentPrice is outside valid range
     */
    public void validatePriceRange(Double originalPrice, Double currentPrice) 
            throws InvalidProductDataException {
        
        if (originalPrice == null || originalPrice <= 0) {
            throw new InvalidProductDataException("Original price must be positive for price validation");
        }
        
        if (currentPrice == null) {
            throw new InvalidProductDataException("Current price is required");
        }
        
        double minAllowedPrice = originalPrice * MIN_PRICE_RATIO;
        double maxAllowedPrice = originalPrice * MAX_PRICE_RATIO;
        
        if (currentPrice < minAllowedPrice || currentPrice > maxAllowedPrice) {
            throw new InvalidProductDataException(
                String.format("Current price must be between %.0f%% and %.0f%% of original price. " +
                              "Allowed range: %.2f - %.2f (Original: %.2f, Provided: %.2f)",
                              MIN_PRICE_RATIO * 100, MAX_PRICE_RATIO * 100,
                              minAllowedPrice, maxAllowedPrice, originalPrice, currentPrice)
            );
        }
    }
}
