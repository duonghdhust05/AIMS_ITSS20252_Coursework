package com.aimsfx.factory;

import com.aimsfx.exception.UnsupportedProductTypeException;
import com.aimsfx.model.ProductType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ProductFactoryRegistry - Registry Pattern
 * 
 * DESIGN PATTERN: Registry + Factory
 * PURPOSE: Centralized factory lookup and management
 * 
 * ARCHITECTURE IMPROVEMENT: Uses ProductType enum as map key
 * - Compile-time type safety (no String typos)
 * - IDE autocomplete support
 * - Exhaustive switch checking possible
 * 
 * BENEFITS:
 * - OCP: Adding new product types only requires registering a new factory
 * - No switch statements or if-else chains
 * - Easy to extend with new product types
 * 
 * TO ADD NEW PRODUCT TYPE:
 * 1. Add enum value to ProductType.java
 * 2. Create new Product subclass (e.g., Magazine.java)
 * 3. Create new Factory class (e.g., MagazineFactory.java)
 * 4. Register in static initializer: registerFactory(new MagazineFactory())
 * 
 * NO MODIFICATION to existing code required!
 */
public class ProductFactoryRegistry {
    
    private static final Map<ProductType, ProductFactory> factories = new HashMap<>();
    
    // Static initializer - registers all available factories
    static {
        registerFactory(new BookFactory());
        registerFactory(new CDFactory());
        registerFactory(new DVDFactory());
        registerFactory(new NewspaperFactory());
    }
    
    /**
     * Register a factory for a product type
     * Can be called at runtime to add new product types dynamically
     */
    public static void registerFactory(ProductFactory factory) {
        factories.put(factory.getProductType(), factory);
    }
    
    /**
     * Get factory for a specific product type (enum-based, type-safe)
     * 
     * @param type Product type enum
     * @return Corresponding ProductFactory
     * @throws UnsupportedProductTypeException if type not registered
     */
    public static ProductFactory getFactory(ProductType type) throws UnsupportedProductTypeException {
        if (type == null) {
            throw new UnsupportedProductTypeException("Product type cannot be null");
        }
        
        ProductFactory factory = factories.get(type);
        if (factory == null) {
            throw new UnsupportedProductTypeException("Unsupported product type: " + type);
        }
        
        return factory;
    }
    
    /**
     * Get factory for a specific product type (backward compatibility with String)
     * 
     * @param type Product type string (BOOK, CD, DVD, NEWSPAPER)
     * @return Corresponding ProductFactory
     * @throws UnsupportedProductTypeException if type not registered
     */
    public static ProductFactory getFactory(String type) throws UnsupportedProductTypeException {
        if (type == null || type.isBlank()) {
            throw new UnsupportedProductTypeException("Product type cannot be null or empty");
        }
        
        return getFactory(ProductType.fromString(type));
    }
    
    /**
     * Get all supported product types
     * @return List of registered product type names
     */
    public static List<String> getSupportedTypes() {
        return factories.keySet().stream()
                .map(ProductType::toString)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a product type is supported
     * @param type Product type to check
     * @return true if factory is registered for this type
     */
    public static boolean isTypeSupported(String type) {
        if (type == null) return false;
        try {
            return factories.containsKey(ProductType.fromString(type));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Check if a product type is supported (enum version)
     * @param type Product type enum to check
     * @return true if factory is registered for this type
     */
    public static boolean isTypeSupported(ProductType type) {
        return type != null && factories.containsKey(type);
    }
    
    /**
     * Get factory as PhysicalProductFactory (for products with weight/dimensions)
     * 
     * @param type Product type enum
     * @return PhysicalProductFactory if the factory creates physical products
     * @throws UnsupportedProductTypeException if type not registered
     * @throws ClassCastException if factory is not a PhysicalProductFactory
     */
    public static PhysicalProductFactory getPhysicalFactory(ProductType type) throws UnsupportedProductTypeException {
        ProductFactory factory = getFactory(type);
        if (factory instanceof PhysicalProductFactory) {
            return (PhysicalProductFactory) factory;
        }
        throw new ClassCastException("Factory for " + type + " is not a PhysicalProductFactory");
    }
    
    /**
     * Get factory as PhysicalProductFactory (String version)
     * 
     * @param type Product type string
     * @return PhysicalProductFactory if the factory creates physical products
     * @throws UnsupportedProductTypeException if type not registered
     * @throws ClassCastException if factory is not a PhysicalProductFactory
     */
    public static PhysicalProductFactory getPhysicalFactory(String type) throws UnsupportedProductTypeException {
        return getPhysicalFactory(ProductType.fromString(type));
    }
}
