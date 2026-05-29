package com.aimsfx.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Product - Abstract base class for all product types
 * 
 * HIERARCHY:
 * Product (abstract) - THIS CLASS - Core attributes (no physical attributes)
 *   └── PhysicalProduct (abstract) - adds weight/dimensions for shippable products
 *         ├── Book
 *         ├── CD
 *         ├── DVD
 *         └── Newspaper
 *         └── [Future: SportProduct, ElectronicProduct, etc.]
 *   └── [Future: DigitalProduct (abstract) - no weight/dimensions needed]
 *         └── [EBook, DigitalMusic, etc.]
 * 
 * COHESION: Functional/Informational
 * - All fields and methods relate to product entity
 * - Represents a cohesive domain concept: a product in inventory
 * 
 * COUPLING: Data Coupling
 * - LOW COUPLING: Pure data model with minimal dependencies
 * - No coupling to View or Controller layers
 * - Only uses Java standard library (LocalDateTime, Map)
 * 
 * NOTE ON EXTENSIBILITY:
 * - Physical attributes (weight, dimensions) are in PhysicalProduct
 * - DigitalProduct won't need these attributes at all
 */
public abstract class Product {

    // Identity fields
    private Long productId; // Unique identifier
    private String barcode; // External identifier (UPC/ISBN)

    // Content fields
    private String title; // Product name
    private String category; // Classification
    private String description; // Detailed description

    // Pricing fields
    private Double originalPrice; // Base price before VAT
    private Double currentPrice; // Current selling price
    private Double vatRate; // VAT rate (e.g., 0.1 for 10%)

    // Inventory fields
    private Integer stock; // Stock level (stored in DB)
    private ProductStatus status; // Availability status (AVAILABLE or DEACTIVATED)

    // NOTE: weight and dimensions moved to PhysicalProduct
    // DigitalProduct doesn't need these attributes

    // Audit fields
    private LocalDateTime createdAt; // Creation timestamp
    private LocalDateTime updatedAt; // Last update timestamp

    // History tracking fields (for temporal data pattern)
    private Boolean isCurrent = true; // Is this the current version?
    private LocalDateTime expiredDate; // When this version expired (null for current version)

    public Product() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.status = ProductStatus.AVAILABLE; // Default status
    }

    /**
     * Constructor with core product attributes (no physical attributes)
     * Physical attributes (weight, dimensions) are handled by PhysicalProduct
     * 
     * @param productId     Unique identifier
     * @param barcode       External identifier (UPC/ISBN)
     * @param title         Product name
     * @param category      Classification
     * @param originalPrice Base price before VAT
     * @param currentPrice  Current selling price
     * @param description   Detailed description
     * @param stock         Stock level
     * @param status        Availability status
     * @param vatRate       VAT rate
     */
    public Product(Long productId, String barcode, String title, String category,
            Double originalPrice, Double currentPrice,
            String description, Integer stock, String status, Double vatRate) {
        this();
        this.productId = productId;
        this.barcode = barcode;
        this.title = title;
        this.category = category;
        this.originalPrice = originalPrice;
        this.currentPrice = currentPrice;
        this.description = description;
        this.stock = stock;
        this.status = status != null ? ProductStatus.fromString(status) : ProductStatus.AVAILABLE;
        this.vatRate = vatRate;
    }

    /**
     * @deprecated Use PhysicalProduct constructor for products with weight/dimensions
     *             Kept for backward compatibility
     */
    @Deprecated
    public Product(Long productId, String barcode, String title, String category,
            Double originalPrice, Double currentPrice,
            String description, Double weight, String dimensions,
            Integer stock, String status, Double vatRate) {
        this(productId, barcode, title, category, originalPrice, currentPrice,
             description, stock, status, vatRate);
        // weight and dimensions are ignored - handled by PhysicalProduct
    }

    // -------- Getter and Setter Methods --------

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWeight() {
        return null; // Digital products don't have weight
    }

    public void setWeight(Double weight) {
        // Default: ignored for non-physical products
    }

    public String getDimensions() {
        return null; // Digital products don't have dimensions
    }

    public void setDimensions(String dimensions) {
        // Default: ignored for non-physical products
    }

    /**
     * Check if this product requires physical shipping
     * Default: false - PhysicalProduct overrides to return true
     * 
     * @return true if product needs shipping
     */
    public boolean requiresShipping() {
        return false; // Digital products don't require shipping
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status != null ? status.getValue() : "available";
    }
    
    public ProductStatus getStatusEnum() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? ProductStatus.fromString(status) : ProductStatus.AVAILABLE;
    }
    
    public void setStatusEnum(ProductStatus status) {
        this.status = status != null ? status : ProductStatus.AVAILABLE;
    }

    public Double getVatRate() {
        return vatRate;
    }

    public void setVatRate(Double vatRate) {
        this.vatRate = vatRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public LocalDateTime getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDateTime expiredDate) {
        this.expiredDate = expiredDate;
    }

    public boolean checkAvailability(Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            return false;
        }
        if (this.stock == null) {
            return false;
        }
        return this.stock >= requestedQuantity;
    }

    public boolean isSufficient(Integer requestedQuantity) {
        return checkAvailability(requestedQuantity);
    }

    /**
     * COHESION: FUNCTIONAL COHESION - Exports common data as Map
     * All code serves purpose of converting object to Map format
     * 
     * COUPLING: LOW COUPLING - Returns generic Map interface
     * Controller can consume this without knowing Product internals
     * Decouples data structure from consumers
     * 
     * DATA TRANSFER:
     * - Converts domain object to data transfer format
     * - Suitable for serialization, API responses, UI binding
     * - Allows adding/removing fields without breaking contracts
     * 
     * Returns common product information as a map
     * NOTE: weight and dimensions are included for backward compatibility
     *       but will be null for digital products
     * 
     * @return Map containing common product attributes
     */
    public Map<String, Object> getCommonProductInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("productId", this.productId);
        info.put("barcode", this.barcode);
        info.put("title", this.title);
        info.put("category", this.category);
        info.put("originalPrice", this.originalPrice);
        info.put("currentPrice", this.currentPrice);
        info.put("description", this.description);
        // Physical attributes - use getters (will be null for digital products)
        info.put("weight", getWeight());
        info.put("dimensions", getDimensions());
        info.put("stock", this.stock);
        info.put("status", this.status);
        info.put("vatRate", this.vatRate);
        info.put("createdAt", this.createdAt);
        info.put("isCurrent", this.isCurrent);
        info.put("expiredDate", this.expiredDate);
        return info;
    }

    /**
     * OCP SOLUTION: Polymorphic type identification
     * WHY: Eliminates instanceof checks in ViewProductController
     * Each subclass returns its own ProductType - no modification needed
     * when adding new product types
     * 
     * @return ProductType enum value identifying this product's type
     */
    public abstract ProductType getProductType();

    /**
     * DESIGN PATTERN: Template Method Pattern
     * Subclasses export their specific attributes as Map
     * - Book: author, publisher, pages
     * - CD: artist, genre, trackCount
     * - DVD: director, runtime, studio
     * - Newspaper: issueNumber, frequency, publisher
     * 
     * POLYMORPHISM: Controller can call without knowing specific type
     * 
     * @return Map containing type-specific product attributes
     */
    public abstract Map<String, Object> getSpecificDetail();

    /**
     * OCP SOLUTION: Polymorphic equality comparison
     * WHY: Eliminates instanceof chains in areProductsEqual()
     * 
     * Base equals() compares common product attributes.
     * Subclasses override to add type-specific field comparisons.
     * 
     * @param obj The object to compare
     * @return true if products are equal (same common + specific attributes)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Product other = (Product) obj;
        return Objects.equals(barcode, other.barcode) &&
               Objects.equals(title, other.title) &&
               Objects.equals(category, other.category) &&
               Objects.equals(originalPrice, other.originalPrice) &&
               Objects.equals(currentPrice, other.currentPrice) &&
               Objects.equals(description, other.description) &&
               Objects.equals(getWeight(), other.getWeight()) &&
               Objects.equals(getDimensions(), other.getDimensions()) &&
               Objects.equals(stock, other.stock) &&
               Objects.equals(status, other.status) &&
               Objects.equals(vatRate, other.vatRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode, title, category, originalPrice, currentPrice,
                           description, getWeight(), getDimensions(), stock, status, vatRate);
    }

    /**
     * OCP SOLUTION: Abstract copy method for polymorphic cloning
     * WHY: Eliminates instanceof checks in HomepageController.addToCart()
     * Each subclass implements its own copy logic - no modification needed
     * when adding new product types
     * 
     * DESIGN PATTERN: Prototype Pattern
     * - Allows creating new objects by copying existing ones
     * - Client code doesn't need to know concrete type
     * 
     * @return A deep copy of this product with all attributes copied
     */
    public abstract Product copy();

    /**
     * Helper method to copy common attributes from this product to another
     * Used by subclasses in their copy() implementations
     * 
     * @param target The product to copy common attributes to
     */
    protected void copyCommonAttributesTo(Product target) {
        target.setProductId(this.productId);
        target.setBarcode(this.barcode);
        target.setTitle(this.title);
        target.setCategory(this.category);
        target.setOriginalPrice(this.originalPrice);
        target.setCurrentPrice(this.currentPrice);
        target.setDescription(this.description);
        // Physical attributes - use getters (PhysicalProduct will handle these)
        target.setWeight(getWeight());
        target.setDimensions(getDimensions());
        target.setStock(this.stock);
        target.setStatusEnum(this.status);
        target.setVatRate(this.vatRate);
        target.setCreatedAt(this.createdAt);
        target.setUpdatedAt(this.updatedAt);
        target.setIsCurrent(this.isCurrent);
        target.setExpiredDate(this.expiredDate);
    }

}
