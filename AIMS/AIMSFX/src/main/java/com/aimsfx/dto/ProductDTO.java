package com.aimsfx.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * ProductDTO - Data Transfer Object for Product creation/update
 * 
 * DESIGN PATTERN: Builder Pattern
 * PURPOSE: Solves "Long Parameter List" anti-pattern
 * 
 * BENEFITS:
 * - Readable code: Named parameters instead of positional
 * - Safe construction: Required vs optional fields are clear
 * - Immutable: Once built, cannot be modified
 * - Extensible: Easy to add new fields without breaking existing code
 * 
 * USAGE:
 * ProductDTO dto = ProductDTO.builder()
 *     .type("BOOK")
 *     .barcode("123456")
 *     .title("Java Programming")
 *     .originalPrice(100.0)
 *     .attribute("author", "John Doe")
 *     .attribute("publisher", "Tech Press")
 *     .build();
 */
public class ProductDTO {
    
    // Identity fields
    private final Long productId;
    private final String type;
    private final String barcode;
    
    // Content fields
    private final String title;
    private final String category;
    private final String description;
    
    // Pricing fields
    private final Double originalPrice;
    private final Double currentPrice;
    private final Double vatRate;
    
    private final Integer stock;
    private final String status;
    
    // Physical attributes
    private final Double weight;
    private final String dimensions;
    
    // Type-specific attributes (replaces String... attributes)
    private final Map<String, String> specificAttributes;
    
    /**
     * Private constructor - use Builder to create instances
     */
    private ProductDTO(Builder builder) {
        this.productId = builder.productId;
        this.type = builder.type;
        this.barcode = builder.barcode;
        this.title = builder.title;
        this.category = builder.category;
        this.description = builder.description;
        this.originalPrice = builder.originalPrice;
        this.currentPrice = builder.currentPrice;
        this.vatRate = builder.vatRate;
        this.stock = builder.stock;
        this.status = builder.status;
        this.weight = builder.weight;
        this.dimensions = builder.dimensions;
        this.specificAttributes = new HashMap<>(builder.specificAttributes);
    }
    
    /**
     * Create a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // ==================== Getters ====================
    
    public Long getProductId() {
        return productId;
    }
    
    public String getType() {
        return type;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Double getOriginalPrice() {
        return originalPrice;
    }
    
    public Double getCurrentPrice() {
        return currentPrice;
    }
    
    public Double getVatRate() {
        return vatRate;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public String getDimensions() {
        return dimensions;
    }
    
    public Map<String, String> getSpecificAttributes() {
        return new HashMap<>(specificAttributes);
    }
    
    /**
     * Get a specific attribute by key
     */
    public String getAttribute(String key) {
        return specificAttributes.get(key);
    }
    
    /**
     * Convert specific attributes to array format using provided keys
     * OCP COMPLIANT: No hardcoded product types - keys come from Factory
     * 
     * @param keys The ordered list of keys provided by the Factory
     * @return Array of attribute values in the same order as keys
     */
    public String[] getAttributesAsArray(String[] keys) {
        if (keys == null) return new String[0];
        
        String[] attributes = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            attributes[i] = specificAttributes.getOrDefault(keys[i], "");
        }
        return attributes;
    }
    
    // ==================== Builder Class ====================
    
    /**
     * Builder for ProductDTO
     * Implements Fluent Interface pattern for readable construction
     */
    public static class Builder {
        private Long productId;
        private String type;
        private String barcode;
        private String title;
        private String category;
        private String description;
        private Double originalPrice;
        private Double currentPrice;
        private Double vatRate;
        private Integer stock;
        private String status;
        private Double weight;
        private String dimensions;
        private Map<String, String> specificAttributes = new HashMap<>();
        
        private Builder() {
            // Private constructor
        }
        
        // Identity fields
        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder barcode(String barcode) {
            this.barcode = barcode;
            return this;
        }
        
        // Content fields
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        // Pricing fields
        public Builder originalPrice(Double originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }
        
        public Builder currentPrice(Double currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }
        
        public Builder vatRate(Double vatRate) {
            this.vatRate = vatRate;
            return this;
        }
        
        public Builder stock(Integer stock) {
            this.stock = stock;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        // Physical attributes
        public Builder weight(Double weight) {
            this.weight = weight;
            return this;
        }
        
        public Builder dimensions(String dimensions) {
            this.dimensions = dimensions;
            return this;
        }
        
        // Type-specific attributes
        public Builder attribute(String key, String value) {
            this.specificAttributes.put(key, value);
            return this;
        }
        
        public Builder attributes(Map<String, String> attributes) {
            this.specificAttributes.putAll(attributes);
            return this;
        }
        
        /**
         * Set all specific attributes at once from a Map
         * OCP SOLUTION: View collects data by key, no index-based logic needed
         * 
         * @param attributes Map of attribute key -> value
         * @return this builder for method chaining
         */
        public Builder specificAttributes(Map<String, String> attributes) {
            if (attributes != null) {
                this.specificAttributes.putAll(attributes);
            }
            return this;
        }
        
        /**
         * Generic method to map array values to map using keys from Factory
         * OCP COMPLIANT: No hardcoded product types - keys come from external source (Factory)
         * 
         * @param keys Array of attribute keys (from Factory.getAttributeKeys())
         * @param values Array of attribute values (from UI or legacy code)
         * @return this builder for method chaining
         * @deprecated Use specificAttributes(Map) instead for cleaner code
         */
        @Deprecated
        public Builder attributesFromKeys(String[] keys, String[] values) {
            if (keys == null || values == null) return this;
            
            // Map keys to values by index
            for (int i = 0; i < Math.min(keys.length, values.length); i++) {
                if (keys[i] != null && values[i] != null) {
                    this.specificAttributes.put(keys[i], values[i]);
                }
            }
            return this;
        }
        
        /**
         * Build the ProductDTO instance
         */
        public ProductDTO build() {
            // Validation can be added here
            if (type == null || type.isEmpty()) {
                throw new IllegalStateException("Product type is required");
            }
            if (barcode == null || barcode.isEmpty()) {
                throw new IllegalStateException("Barcode is required");
            }
            if (title == null || title.isEmpty()) {
                throw new IllegalStateException("Title is required");
            }
            return new ProductDTO(this);
        }
    }
    
    @Override
    public String toString() {
        return "ProductDTO{" +
                "productId=" + productId +
                ", type='" + type + '\'' +
                ", barcode='" + barcode + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", originalPrice=" + originalPrice +
                ", currentPrice=" + currentPrice +
                ", specificAttributes=" + specificAttributes +
                '}';
    }
}
