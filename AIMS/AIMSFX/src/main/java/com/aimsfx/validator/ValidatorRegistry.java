package com.aimsfx.validator;

import com.aimsfx.exception.UnsupportedProductTypeException;
import com.aimsfx.model.ProductType;

import java.util.HashMap;
import java.util.Map;

/**
 * ValidatorRegistry - Registry for product validators
 * 
 * DESIGN PATTERN: Registry Pattern (similar to FactoryRegistry)
 * PURPOSE: Centralized validator lookup
 * 
 * ARCHITECTURE IMPROVEMENT: Uses ProductType enum as map key
 * - Compile-time type safety
 * - No String matching errors
 * 
 * OCP: Adding new validators doesn't require modifying existing code
 */
public class ValidatorRegistry {
    
    private static final Map<ProductType, ProductValidator> validators = new HashMap<>();
    
    static {
        registerValidator(new BookValidator());
        registerValidator(new CDValidator());
        registerValidator(new DVDValidator());
        registerValidator(new NewspaperValidator());
    }
    
    public static void registerValidator(ProductValidator validator) {
        validators.put(validator.getProductType(), validator);
    }
    
    /**
     * Get validator for a product type (enum-based, type-safe)
     */
    public static ProductValidator getValidator(ProductType type) throws UnsupportedProductTypeException {
        if (type == null) {
            throw new UnsupportedProductTypeException("Product type cannot be null");
        }
        
        ProductValidator validator = validators.get(type);
        if (validator == null) {
            throw new UnsupportedProductTypeException("No validator for product type: " + type);
        }
        
        return validator;
    }
    
    /**
     * Get validator for a product type (backward compatibility with String)
     */
    public static ProductValidator getValidator(String type) throws UnsupportedProductTypeException {
        if (type == null || type.isBlank()) {
            throw new UnsupportedProductTypeException("Product type cannot be null or empty");
        }
        
        return getValidator(ProductType.fromString(type));
    }
}
