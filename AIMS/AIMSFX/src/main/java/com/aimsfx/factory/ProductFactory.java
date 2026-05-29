package com.aimsfx.factory;

import com.aimsfx.factory.meta.FieldSpec;
import com.aimsfx.model.Product;
import com.aimsfx.model.ProductType;
import com.aimsfx.model.Track;
import com.aimsfx.model.meta.AttributeMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProductFactory - Abstract Factory Pattern (Base Interface)
 * 
 * PURPOSE: Eliminates switch statements for product creation
 * 
 * BENEFITS:
 * - Open/Closed Principle: Can add new product types without modifying existing code
 * - Single Responsibility: Each factory handles one product type
 * - Dependency Inversion: Clients depend on abstract factory, not concrete implementations
 * - Type Safety: Uses ProductType enum instead of String
 * 
 * NOTE: This base interface does NOT include weight/dimensions
 * PhysicalProductFactory extends this to add physical attributes
 * 
 * USAGE:
 * ProductFactory factory = ProductFactoryRegistry.getFactory(ProductType.BOOK);
 * Product product = factory.createProduct(...);
 */
public interface ProductFactory {
    
    /**
     * Factory method to create a product of specific type
     * 
     * UNIFIED INTERFACE: All product factories implement this single method.
     * - PhysicalProductFactory implementations: use weight/dimensions
     * - DigitalProductFactory implementations: ignore weight/dimensions (can be null)
     * 
     * OCP BENEFIT: ProductService only knows ProductFactory, not subinterfaces.
     * Adding new factory types (DigitalProductFactory) won't require changes to ProductService.
     * 
     * @param productId     Unique identifier (null for new products)
     * @param barcode       External identifier (UPC/ISBN)
     * @param title         Product name
     * @param category      Classification
     * @param originalPrice Base price before VAT
     * @param currentPrice  Current selling price
     * @param description   Detailed description
     * @param weight        Physical weight in kg (null for digital products)
     * @param dimensions    Physical dimensions (null for digital products)
     * @param stock         Stock level
     * @param status        Availability status
     * @param vatRate       VAT rate
     * @param attributes    Type-specific attributes (varargs)
     * @return Product instance
     */
    Product createProduct(Long productId, String barcode, String title, String category,
                         Double originalPrice, Double currentPrice,
                         String description, Double weight, String dimensions,
                         Integer stock, String status, Double vatRate,
                         String... attributes);
    
    /**
     * Get the product type this factory creates
     * @return Product type enum (compile-time safe)
     */
    ProductType getProductType();
    
    /**
     * Get attribute labels for this product type
     * Used by UI to display input fields
     * 
     * @return Array of attribute labels
     * @deprecated Use getAttributeConfig() instead for full metadata
     */
    @Deprecated
    String[] getAttributeLabels();
    
    /**
     * Get attribute keys for this product type
     * Used for mapping between DTO maps and legacy arrays
     * The order MUST match getAttributeLabels() and the createProduct attributes array
     * 
     * OCP PRINCIPLE: This metadata belongs in Factory, not in DTO
     * Adding new product types only requires creating new Factory implementation
     * 
     * @return Array of attribute keys (e.g., "author", "publisher")
     * @deprecated Use getAttributeConfig() instead for full metadata
     */
    @Deprecated
    String[] getAttributeKeys();
    
    /**
     * Get complete attribute configuration with metadata
     * 
     * OCP SOLUTION: Replaces getAttributeLabels() and getAttributeKeys()
     * with rich metadata including input types, options, etc.
     * 
     * BENEFITS:
     * - View doesn't need to know product types (no "BOOK", "CD" checks)
     * - All metadata in one place (key, label, inputType, options)
     * - Adding new product types = new Factory only
     * 
     * @return List of AttributeMeta describing each attribute
     */
    List<AttributeMeta> getAttributeConfig();
    
    /**
     * Create an empty product instance for reconstruction from database
     * Used by Repository to create object before populating from ResultSet
     * 
     * OCP PRINCIPLE: Repository doesn't need to know about specific product types
     * Each factory creates its own type, eliminating switch-case in Repository
     * 
     * @return Empty product instance of the appropriate type
     */
    Product createEmptyProduct();
    
    // ==================== Input Schema Methods  ====================
    
    /**
     * Get field specifications for common product fields
     * 
     * OCP PRINCIPLE: Factory owns its input schema - Controller becomes generic
     * - Base ProductFactory defines baseline common fields
     * - PhysicalProductFactory overrides to add weight/dimensions
     * - DigitalProductFactory (future) uses base specs only
     * 
     * USAGE:
     * ProductFactory factory = ProductFactoryRegistry.getFactory(type);
     * List<FieldSpec> specs = factory.commonFieldSpecs();
     * Map<String, Object> parsed = controller.parseCommonFieldsBySpecs(specs, rawInputs);
     * 
     * @return List of FieldSpec for common fields (barcode, title, price, etc.)
     */
    default List<FieldSpec> commonFieldSpecs() {
        List<FieldSpec> specs = new ArrayList<>();
        
        // Base common fields for ALL products (physical and digital)
        specs.add(FieldSpec.string("barcode", "Barcode", true));
        specs.add(FieldSpec.string("title", "Title", true));
        specs.add(FieldSpec.doubleField("price", "Price", true));
        specs.add(FieldSpec.string("category", "Category", true));
        specs.add(FieldSpec.intField("stock", "Stock", true)
                .withConstraint(v -> (Integer) v >= 0, "Stock cannot be negative!"));
        specs.add(FieldSpec.doubleFieldWithDefault("vatRate", "VAT Rate", false, 10.0));
        specs.add(FieldSpec.string("description", "Description", false));
        
        // NOTE: weight and dimensions are NOT included here
        // PhysicalProductFactory overrides this method to add them
        
        return specs;
    }
    
    /**
     * Get field specifications for update operation
     * 
     * Update has slightly different required fields than add:
     * - barcode is optional (may not change)
     * - stock is NOT updated through this path
     * - originalPrice and currentPrice instead of single price
     * 
     * @return List of FieldSpec for update common fields
     */
    default List<FieldSpec> commonFieldSpecsForUpdate() {
        List<FieldSpec> specs = new ArrayList<>();
        
        // Base common fields for update
        specs.add(FieldSpec.string("barcode", "Barcode", false)); // Optional in update
        specs.add(FieldSpec.string("title", "Title", true));
        specs.add(FieldSpec.doubleField("originalPrice", "Original Price", true));
        specs.add(FieldSpec.doubleField("currentPrice", "Current Price", false)); // Defaults to originalPrice
        specs.add(FieldSpec.string("category", "Category", true));
        specs.add(FieldSpec.doubleFieldWithDefault("vatRate", "VAT Rate", false, 10.0));
        specs.add(FieldSpec.string("description", "Description", false));
        specs.add(FieldSpec.stringWithDefault("status", "Status", false, "available"));
        
        // NOTE: stock is NOT included - updated through separate path
        // NOTE: weight and dimensions NOT included - PhysicalProductFactory adds them
        
        return specs;
    }
    
    /**
     * Cross-field validation hook
     * 
     * OCP PRINCIPLE: Type-specific cross-field rules belong in Factory, not Controller
     * - CDFactory overrides to require tracks
     * - Other factories can add their own cross-field rules
     * - Controller calls this generically without knowing product types
     * 
     * @param parsedCommon Parsed common fields from parseCommonFieldsBySpecs()
     * @param specificAttributes Type-specific attributes (raw strings)
     * @param tracks List of tracks (for CD products, null for others)
     * @throws IllegalArgumentException if cross-field validation fails
     */
    default void validateCrossFieldRules(
            Map<String, Object> parsedCommon,
            Map<String, String> specificAttributes,
            List<Track> tracks) {
        // No-op by default - subclasses override as needed
    }
}

