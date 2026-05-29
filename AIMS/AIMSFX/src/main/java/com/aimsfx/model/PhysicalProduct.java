package com.aimsfx.model;

/**
 * PhysicalProduct - Abstract class for products that can be physically shipped
 * 
 * DESIGN PATTERN: Template Method Pattern (extends Product)
 * PURPOSE: Separate physical/shippable products from digital products
 *
 * COHESION: Functional 
 * - Groups physical product specific behaviors and attributes
 * - Inherits common attributes from Product
 * 
 * COUPLING: Data Coupling 
 * - Extends Product base class
 * - No additional external dependencies
 */
public abstract class PhysicalProduct extends Product {

    private Double weight; // Physical weight in kg
    private String dimensions; // Physical dimensions (e.g., "20x15x3 cm")

    public PhysicalProduct() {
        super();
    }

    /**
     * Full constructor with all attributes including physical attributes
     * 
     * @param productId     Unique identifier
     * @param barcode       External identifier (UPC/ISBN)
     * @param title         Product name
     * @param category      Classification
     * @param originalPrice Base price before VAT
     * @param currentPrice  Current selling price
     * @param description   Detailed description
     * @param weight        Physical weight in kg
     * @param dimensions    Physical dimensions
     * @param stock         Stock level
     * @param status        Availability status
     * @param vatRate       VAT rate
     */
    public PhysicalProduct(Long productId, String barcode, String title, String category,
                          Double originalPrice, Double currentPrice,
                          String description, Double weight, String dimensions,
                          Integer stock, String status, Double vatRate) {
        super(productId, barcode, title, category, originalPrice, currentPrice,
              description, stock, status, vatRate);
        this.weight = weight;
        this.dimensions = dimensions;
    }

    // ==================== Physical Attribute Getters/Setters ====================

    @Override
    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public String getDimensions() {
        return dimensions;
    }

    @Override
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    // ==================== Physical Product Behaviors ====================

    @Override
    public boolean requiresShipping() {
        return true;
    }

    public boolean isPhysicalProduct() {
        return true;
    }
}
