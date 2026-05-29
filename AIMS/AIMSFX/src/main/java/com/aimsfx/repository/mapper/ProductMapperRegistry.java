package com.aimsfx.repository.mapper;

import com.aimsfx.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ProductMapperRegistry - Registry for ProductJdbcMapper implementations
 * 
 * DESIGN PATTERN: Registry Pattern (similar to ProductFactoryRegistry)
 * PURPOSE: Provides centralized access to type-specific mappers
 * 
 * BENEFITS:
 * - Open/Closed Principle: Add new mappers by registering them, no code changes needed
 * - Decoupling: DatabaseProductRepository doesn't need to know about specific mappers
 * - Single Source of Truth: All mapper registrations in one place
 * - Thread-safe: Static initialization is thread-safe in Java
 * 
 * HOW TO ADD A NEW PRODUCT TYPE:
 * 1. Create NewTypeJdbcMapper implements ProductJdbcMapper<NewType>
 * 2. Add: register(ProductType.NEW_TYPE, new NewTypeJdbcMapper()) in static block
 * 3. No changes to DatabaseProductRepository needed!
 */
public class ProductMapperRegistry {
    
    // Registry map: ProductType enum -> Mapper instance
    private static final Map<ProductType, ProductJdbcMapper<?>> mappers = new HashMap<>();
    
    // Class type -> Mapper instance (for runtime type lookup)
    private static final Map<Class<? extends Product>, ProductJdbcMapper<?>> mappersByClass = new HashMap<>();
    
    // Static initialization - register all mappers
    static {
        // Register all product type mappers
        register(ProductType.BOOK, Book.class, new BookJdbcMapper());
        register(ProductType.CD, CD.class, new CDJdbcMapper());
        register(ProductType.DVD, DVD.class, new DVDJdbcMapper());
        register(ProductType.NEWSPAPER, Newspaper.class, new NewspaperJdbcMapper());
    }
    
    /**
     * Register a mapper for a product type
     * 
     * @param type The product type enum
     * @param clazz The product class
     * @param mapper The mapper implementation
     */
    private static <T extends Product> void register(ProductType type, Class<T> clazz, ProductJdbcMapper<T> mapper) {
        mappers.put(type, mapper);
        mappersByClass.put(clazz, mapper);
    }
    
    /**
     * Get mapper for a ProductType enum
     * 
     * @param type The product type
     * @return The corresponding mapper
     * @throws IllegalArgumentException if no mapper is registered for the type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Product> ProductJdbcMapper<T> getMapper(ProductType type) {
        ProductJdbcMapper<?> mapper = mappers.get(type);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper registered for type: " + type);
        }
        return (ProductJdbcMapper<T>) mapper;
    }
    
    /**
     * Get mapper for a ProductType string
     * 
     * @param typeString The product type as string (e.g., "BOOK")
     * @return The corresponding mapper
     * @throws IllegalArgumentException if no mapper is registered for the type
     */
    public static <T extends Product> ProductJdbcMapper<T> getMapper(String typeString) {
        ProductType type = ProductType.fromString(typeString);
        return getMapper(type);
    }
    
    /**
     * Get mapper by product class type (for runtime type lookup)
     * 
     * @param clazz The product class
     * @return The corresponding mapper
     * @throws IllegalArgumentException if no mapper is registered for the class
     */
    @SuppressWarnings("unchecked")
    public static <T extends Product> ProductJdbcMapper<T> getMapperByClass(Class<T> clazz) {
        ProductJdbcMapper<?> mapper = mappersByClass.get(clazz);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper registered for class: " + clazz.getName());
        }
        return (ProductJdbcMapper<T>) mapper;
    }
    
    /**
     * Get mapper for a product instance (uses runtime type)
     * 
     * @param product The product instance
     * @return The corresponding mapper
     * @throws IllegalArgumentException if no mapper is registered for the product type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Product> ProductJdbcMapper<T> getMapperForProduct(T product) {
        return (ProductJdbcMapper<T>) mappersByClass.get(product.getClass());
    }
    
    /**
     * Check if a mapper is registered for a type
     * 
     * @param type The product type
     * @return true if a mapper is registered
     */
    public static boolean hasMapper(ProductType type) {
        return mappers.containsKey(type);
    }
    
    /**
     * Check if a mapper is registered for a class
     * 
     * @param clazz The product class
     * @return true if a mapper is registered
     */
    public static boolean hasMapper(Class<? extends Product> clazz) {
        return mappersByClass.containsKey(clazz);
    }
}
