package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.model.ProductType;

/**
 * ProductValidator - Strategy Pattern Interface
 * 
 * DESIGN PATTERN: Strategy Pattern
 * PURPOSE: Encapsulates validation rules for different product types
 * 
 * SOLID PRINCIPLES:
 * - SRP: Each validator handles validation for one product type
 * - OCP: Can add new validators without modifying existing code
 * - DIP: Business logic depends on abstraction, not concrete validators
 * - ISP: Single focused method
 * - Type Safety: Uses ProductType enum instead of String
 * 
 * BENEFITS:
 * - Separates validation logic from creation logic
 * - Each product type can have different validation rules
 * - Easy to test validators independently
 * - Compile-time type safety with enum
 */
public interface ProductValidator {
    
    /**
     * Validate product-specific attributes
     * 
     * @param attributes Product-specific attributes to validate
     * @throws InvalidProductDataException if validation fails
     */
    void validateSpecificAttributes(String... attributes) throws InvalidProductDataException;
    
    /**
     * Get the product type this validator handles
     * @return Product type enum (compile-time safe)
     */
    ProductType getProductType();
}
