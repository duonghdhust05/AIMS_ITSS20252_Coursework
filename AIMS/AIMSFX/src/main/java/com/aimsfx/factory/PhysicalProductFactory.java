package com.aimsfx.factory;

import com.aimsfx.factory.meta.FieldSpec;
import com.aimsfx.model.Product;

import java.util.List;

/**
 * PhysicalProductFactory - Factory interface for physical/shippable products
 * PURPOSE: Separate physical product creation from digital product creation
 * 
 * USAGE:
 * PhysicalProductFactory factory = (PhysicalProductFactory) ProductFactoryRegistry.getFactory(ProductType.BOOK);
 * Product book = factory.createPhysicalProduct(..., weight, dimensions, ...);
 */
public interface PhysicalProductFactory extends ProductFactory {
    
    /**
     * Factory method to create a physical product with weight and dimensions
     * 
     * @param productId     Unique identifier
     * @param barcode       External identifier (UPC/ISBN)
     * @param title         Product name
     * @param category      Classification
     * @param originalPrice Base price before VAT
     * @param currentPrice  Current selling price
     * @param description   Detailed description
     * @param weight        Physical weight in kg
     * @param dimensions    Physical dimensions (e.g., "20x15x3 cm")
     * @param stock         Stock level
     * @param status        Availability status
     * @param vatRate       VAT rate
     * @param attributes    Type-specific attributes (varargs)
     * @return Physical product instance (Book, CD, DVD, Newspaper, etc.)
     */
    Product createPhysicalProduct(Long productId, String barcode, String title, String category,
                                  Double originalPrice, Double currentPrice,
                                  String description, Double weight, String dimensions,
                                  Integer stock, String status, Double vatRate,
                                  String... attributes);
    
    /**
     * Implementation of ProductFactory.createProduct() - delegates to createPhysicalProduct
     * This ensures a single unified interface for ProductService
     */
    @Override
    default Product createProduct(Long productId, String barcode, String title, String category,
                                  Double originalPrice, Double currentPrice,
                                  String description, Double weight, String dimensions,
                                  Integer stock, String status, Double vatRate,
                                  String... attributes) {
        // Delegate to createPhysicalProduct (which is the actual implementation)
        return createPhysicalProduct(productId, barcode, title, category,
                                     originalPrice, currentPrice, description,
                                     weight, dimensions,
                                     stock, status, vatRate, attributes);
    }
    
    // ==================== Physical Product Input Schema ====================
    
    /**
     * Override commonFieldSpecs to add physical-only fields: weight and dimensions
     * 
     * OCP PRINCIPLE: Physical products have additional required fields
     * - Builds on top of base ProductFactory specs
     * - Controller doesn't need to know about physical vs digital
     * - Just calls factory.commonFieldSpecs() and gets the right specs
     * 
     * @return List of FieldSpec including physical fields (weight, dimensions)
     */
    @Override
    default List<FieldSpec> commonFieldSpecs() {
        // Start with base specs from ProductFactory
        List<FieldSpec> specs = ProductFactory.super.commonFieldSpecs();
        
        // Add physical-only fields
        specs.add(FieldSpec.doubleField("weight", "Weight", true)
                .withConstraint(v -> (Double) v >= 0, "Weight cannot be negative!"));
        specs.add(FieldSpec.string("dimensions", "Dimensions", true));
        
        return specs;
    }
    
    /**
     * Override commonFieldSpecsForUpdate to add physical-only fields
     * 
     * @return List of FieldSpec for update including physical fields
     */
    @Override
    default List<FieldSpec> commonFieldSpecsForUpdate() {
        // Start with base specs from ProductFactory
        List<FieldSpec> specs = ProductFactory.super.commonFieldSpecsForUpdate();
        
        // Add physical-only fields
        specs.add(FieldSpec.doubleField("weight", "Weight", true)
                .withConstraint(v -> (Double) v >= 0, "Weight cannot be negative!"));
        specs.add(FieldSpec.string("dimensions", "Dimensions", true));
        
        return specs;
    }
}
