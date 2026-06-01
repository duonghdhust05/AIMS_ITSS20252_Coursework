package com.aimsfx.controller;

import com.aimsfx.dto.ProductDTO;
import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.exception.UnsupportedProductTypeException;
import com.aimsfx.factory.ProductFactory;
import com.aimsfx.factory.ProductFactoryRegistry;
import com.aimsfx.factory.meta.FieldSpec;
import com.aimsfx.model.Product;
import com.aimsfx.model.StockChangeLog;
import com.aimsfx.model.Track;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProductController - REFACTORED with SOLID principles (Generic Pipeline)
 * 
 * ============================================================
 * REFACTORING OBJECTIVES ACHIEVED:
 * ============================================================
 * Controller is now a GENERIC PIPELINE:
 *    - ZERO hardcoded per-field validation (barcode/title/price/weight/etc.)
 *    - ZERO type-specific if/else (no "CD".equals(type), no instanceof checks)
 *    - Factory provides input schema via commonFieldSpecs()
 *    - Factory provides cross-field rules via validateCrossFieldRules()
 * 
 * OCP-Compliant Architecture:
 *    - Adding new product types requires ONLY: new Factory + register it
 *    - Controller requires NO changes
 *    - PhysicalProductFactory adds weight/dimensions automatically
 *    - CDFactory enforces tracks rule automatically
 * 
 * Views remain UI-only:
 *    - Views collect raw strings and call ONE controller method
 *    - Views don't parse numbers or construct ProductDTO
 * 
 * ObservableList conversion happens here (UI layer):
 *    - Service returns List<Product> (standard Java)
 *    - Controller converts to ObservableList for JavaFX binding
 * 
 * ============================================================
 * GENERIC PIPELINE FLOW:
 * ============================================================
 * 1. Get factory for product type
 * 2. Get input schema from factory (commonFieldSpecs)
 * 3. Parse + validate all fields generically via parseCommonFieldsBySpecs()
 * 4. Call factory.validateCrossFieldRules() for type-specific rules
 * 5. Build ProductDTO from parsed values
 * 6. Delegate to ProductService for business logic + persistence
 * 
 * ============================================================
 * INTERFACES IMPLEMENTED:
 * ============================================================
 * - IProductDataProvider: For DIP compliance in ViewProductController
 */
public class ProductController implements IProductDataProvider {
    
    // Singleton for backward compatibility with existing views
    private static ProductController instance;
    
    private final ProductService productService;
    
    /**
     * Private constructor with Dependency Injection
     */
    private ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Singleton accessor for backward compatibility
     * Controller only knows about Service layer - not Repository!
     */
    public static ProductController getInstance() {
        if (instance == null) {
            ProductService service = ProductService.getInstance();
            instance = new ProductController(service);
        }
        return instance;
    }
    
    /**
     * Alternative constructor for testing
     * Allows injection of mock service
     */
    public ProductController(ProductService productService, boolean isTest) {
        this.productService = productService;
    }
    
    /**
     * Handle Add Product from UI - GENERIC PIPELINE
     * 
     * ZERO HARDCODED LOGIC:
     * - Field specs come from factory.commonFieldSpecs()
     * - Cross-field rules come from factory.validateCrossFieldRules()
     * - Controller just orchestrates the pipeline
     * 
     * @param type Product type (BOOK, CD, DVD, NEWSPAPER, or future types)
     * @param commonFieldsRaw Map containing common fields as raw strings
     * @param specificAttributes Map containing type-specific attributes
     * @param tracks List of tracks (for CD only, can be null/empty for others)
     * @return Created product
     * @throws IllegalArgumentException if validation fails (with user-friendly message)
     */
    public Product handleAddProduct(
            String type,
            Map<String, String> commonFieldsRaw,
            Map<String, String> specificAttributes,
            List<Track> tracks) {
        
        try {
            // Step 1: Get factory - single point where type is resolved
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("Product Type is required!");
            }
            ProductFactory factory = ProductFactoryRegistry.getFactory(type);
            
            // Step 2: Get input schema from factory (OCP - factory owns its requirements)
            List<FieldSpec> commonSpecs = factory.commonFieldSpecs();
            
            // Step 3: Generic parse + validate all fields by spec
            Map<String, Object> parsedCommon = parseCommonFieldsBySpecs(commonSpecs, commonFieldsRaw);
            
            // Step 4: Cross-field validation hook (e.g., CD requires tracks)
            factory.validateCrossFieldRules(parsedCommon, specificAttributes, tracks);
            
            // Step 5: Normalize defaults for add operation
            Double price = (Double) parsedCommon.get("price");
            String status = "available"; // Always "available" for new products
            
            // Step 6: Build ProductDTO from parsed values
            ProductDTO productDTO = ProductDTO.builder()
                    .type(type)
                    .barcode(getStringOrEmpty(parsedCommon, "barcode"))
                    .title(getStringOrEmpty(parsedCommon, "title"))
                    .category(getStringOrEmpty(parsedCommon, "category"))
                    .originalPrice(price)
                    .currentPrice(price)
                    .description(getStringOrEmpty(parsedCommon, "description"))
                    .weight((Double) parsedCommon.get("weight"))       // null for non-physical
                    .dimensions((String) parsedCommon.get("dimensions")) // null for non-physical
                    .stock((Integer) parsedCommon.get("stock"))
                    .status(status)
                    .vatRate((Double) parsedCommon.get("vatRate"))
                    .specificAttributes(specificAttributes)
                    .build();
            
            // Step 7: Delegate to service for business validation + persistence
            Product product = productService.addProduct(productDTO);
            
            // Step 8: Post-persist hook for tracks (CD-specific but handled generically)
            if (tracks != null && !tracks.isEmpty()) {
                String barcode = product.getBarcode();
                for (Track track : tracks) {
                    track.setProductBarcode(barcode);
                }
                boolean tracksSaved = productService.saveTracks(tracks);
                if (!tracksSaved) {
                    System.err.println("Warning: Product created but some tracks failed to save");
                }
            }
            
            return product;
            
        } catch (InvalidProductDataException | UnsupportedProductTypeException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    
    /**
     * Handle Update Product from UI - GENERIC PIPELINE
     * 
     * ZERO HARDCODED LOGIC:
     * - Field specs come from factory.commonFieldSpecsForUpdate()
     * - Controller just orchestrates the pipeline
     * 
     * @param productIdRaw Product ID as string
     * @param productType Product type (cannot be changed)
     * @param commonFieldsRaw Map containing common fields as raw strings
     * @param specificAttributes Map containing type-specific attributes
     * @return Updated product
     * @throws IllegalArgumentException if validation fails
     */
    public Product handleUpdateProduct(
            String productIdRaw,
            String productType,
            Map<String, String> commonFieldsRaw,
            Map<String, String> specificAttributes) {
        
        try {
            // Step 1: Parse product ID (this is always required for update)
            Long productId = parseLongField("Product ID", productIdRaw);
            
            // Step 2: Get factory for the product type
            ProductFactory factory = ProductFactoryRegistry.getFactory(productType);
            
            // Step 3: Get input schema from factory for update operation
            List<FieldSpec> commonSpecs = factory.commonFieldSpecsForUpdate();
            
            // Step 4: Generic parse + validate all fields by spec
            Map<String, Object> parsedCommon = parseCommonFieldsBySpecs(commonSpecs, commonFieldsRaw);
            
            // Step 5: Handle currentPrice default (defaults to originalPrice if blank)
            Double originalPrice = (Double) parsedCommon.get("originalPrice");
            Double currentPrice = (Double) parsedCommon.get("currentPrice");
            if (currentPrice == null) {
                currentPrice = originalPrice;
            }
            
            // Step 6: Build ProductDTO from parsed values
            ProductDTO productDTO = ProductDTO.builder()
                    .productId(productId)
                    .type(productType)
                    .barcode(getStringOrEmpty(parsedCommon, "barcode"))
                    .title(getStringOrEmpty(parsedCommon, "title"))
                    .category(getStringOrEmpty(parsedCommon, "category"))
                    .originalPrice(originalPrice)
                    .currentPrice(currentPrice)
                    .description(getStringOrEmpty(parsedCommon, "description"))
                    .weight((Double) parsedCommon.get("weight"))       // null for non-physical
                    .dimensions((String) parsedCommon.get("dimensions")) // null for non-physical
                    .stock(null) // Stock is not updated through this method
                    .status(getStringOrDefault(parsedCommon, "status", "available"))
                    .vatRate((Double) parsedCommon.get("vatRate"))
                    .specificAttributes(specificAttributes)
                    .build();
            
            // Step 7: Delegate to service for business validation + persistence
            return productService.updateProduct(productDTO);
            
        } catch (InvalidProductDataException | UnsupportedProductTypeException | ProductNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    
    /**
     * Handle Update Stock from UI
     * 
     * RESPONSIBILITIES:
     * - Parse stock value
     * - Validate reason is non-empty
     * - Call service to update stock
     * - Return user-friendly error messages
     * 
     * @param barcode Product barcode
     * @param newStockRaw New stock value as string
     * @param reason Reason for stock change
     * @throws IllegalArgumentException if validation fails
     */
    public void handleUpdateStock(String barcode, String newStockRaw, String reason) {
        // Step 1: Validate required fields
        validateRequiredField("Barcode", barcode);
        validateRequiredField("New Stock", newStockRaw);
        validateRequiredField("Reason", reason);
        
        // Step 2: Parse stock
        Integer newStock = parseIntegerField("New Stock", newStockRaw);
        
        // Step 3: Validate constraints
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative!");
        }
        
        // Step 4: Call service
        try {
            boolean success = productService.updateStock(barcode.trim(), newStock, reason.trim());
            if (!success) {
                throw new IllegalArgumentException("Failed to update stock. Product may not exist.");
            }
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation errors from service
        }
    }
    
    /**
     * Parse and validate common fields based on factory-provided specs
     * 
     * GENERIC RULES (no hardcoded field names):
     * - For each spec in the list:
     *   - Get raw string by spec.key
     *   - If missing/blank and required => throw error with spec.label
     *   - If missing/blank and has default => use default
     *   - If present => parse based on spec.kind (STRING/INT/DOUBLE)
     *   - If constraint exists => validate and throw error if fails
     * 
     * @param specs List of FieldSpec from factory.commonFieldSpecs()
     * @param raw Raw input map from UI (key -> raw string)
     * @return Parsed map (key -> parsed object: String/Integer/Double)
     * @throws IllegalArgumentException if validation fails
     */
    private Map<String, Object> parseCommonFieldsBySpecs(
            List<FieldSpec> specs,
            Map<String, String> raw) {
        
        Map<String, Object> parsed = new HashMap<>();
        
        for (FieldSpec spec : specs) {
            String rawValue = raw.get(spec.getKey());
            
            if (rawValue == null || rawValue.trim().isEmpty()) {
                // Missing or blank value
                if (spec.isRequired()) {
                    throw new IllegalArgumentException(spec.getLabel() + " is required!");
                } else if (spec.getDefaultValue() != null) {
                    parsed.put(spec.getKey(), spec.getDefaultValue());
                }
                // else: optional with no default => don't put in map (will be null)
            } else {
                // Value present - parse based on kind
                Object parsedValue = parseValueByKind(spec, rawValue.trim());
                
                // Check constraint if present
                if (spec.hasConstraint() && !spec.getConstraint().test(parsedValue)) {
                    throw new IllegalArgumentException(spec.getConstraintMessage());
                }
                
                parsed.put(spec.getKey(), parsedValue);
            }
        }
        
        return parsed;
    }
    
    private Object parseValueByKind(FieldSpec spec, String value) {
        switch (spec.getKind()) {
            case STRING:
                return value;
            case INT:
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(spec.getLabel() + " must be a valid integer!");
                }
            case DOUBLE:
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(spec.getLabel() + " must be a valid number!");
                }
            default:
                throw new IllegalArgumentException("Unknown field kind: " + spec.getKind());
        }
    }
    
    // ============================================================
    // HELPER METHODS for DTO Building
    // ============================================================

    private String getStringOrEmpty(Map<String, Object> parsed, String key) {
        Object value = parsed.get(key);
        return (value != null) ? value.toString() : "";
    }

    private String getStringOrDefault(Map<String, Object> parsed, String key, String defaultValue) {
        Object value = parsed.get(key);
        return (value != null) ? value.toString() : defaultValue;
    }

    private Long parseLongField(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required!");
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number!");
        }
    }

    private void validateRequiredField(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required!");
        }
    }

    private Integer parseIntegerField(String fieldName, String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer!");
        }
    }
    
    // ============================================================
    // PUBLIC API - Delegates to ProductService with ObservableList conversion
    // ============================================================

    public ObservableList<Product> getProducts() {
        return FXCollections.observableArrayList(productService.getAllProducts());
    }

    public List<String> getSupportedTypes() {
        return productService.getSupportedTypes();
    }

    public String[] getAttributeLabels(String type) throws UnsupportedProductTypeException {
        return productService.getAttributeLabels(type);
    }
    
    public List<AttributeMeta> getAttributeConfig(String type) throws UnsupportedProductTypeException {
        return productService.getAttributeConfig(type);
    }

    @Deprecated
    public Product addProduct(ProductDTO productDto) 
            throws InvalidProductDataException, UnsupportedProductTypeException {
        return productService.addProduct(productDto);
    }

    @Deprecated
    public Product updateProduct(ProductDTO productDto)
            throws InvalidProductDataException, UnsupportedProductTypeException, ProductNotFoundException {
        return productService.updateProduct(productDto);
    }

    public Product getProductById(Long id) throws ProductNotFoundException {
        return productService.getProductById(id);
    }

    public ObservableList<Product> getProductHistory(Long productId) {
        return FXCollections.observableArrayList(productService.getProductHistory(productId));
    }

    @Override
    public Product findById(Long productId) {
        try {
            return productService.getProductById(productId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Update product stock with reason logging
     * @deprecated Use handleUpdateStock() instead for proper parsing
     */
    @Deprecated
    public boolean updateStock(String barcode, Integer newStock, String reason) {
        return productService.updateStock(barcode, newStock, reason);
    }
    
    /**
     * Get stock change history for a product
     * @param barcode Product barcode
     * @return ObservableList of stock change logs for JavaFX binding
     */
    public ObservableList<StockChangeLog> getStockChangeHistory(String barcode) {
        return FXCollections.observableArrayList(productService.getStockChangeHistory(barcode));
    }
    
    /**
     * Delete product (soft delete)
     */
    public boolean deleteProduct(Long productId, Long userId) throws ProductNotFoundException, com.aimsfx.exception.DeletionLimitExceededException {
        return productService.deleteProduct(productId, userId);
    }

    public int getRemainingDeletionQuota(Long userId) {
        return productService.getRemainingDeletionQuota(userId);
    }

    public int deleteMultipleProducts(java.util.List<com.aimsfx.model.Product> products, Long userId) 
            throws com.aimsfx.exception.BulkDeleteValidationException {
        return productService.deleteMultipleProducts(products, userId);
    }
    
    // ============================================================
    // TRACK OPERATIONS
    // ============================================================

    public boolean saveTracks(List<Track> tracks) {
        return productService.saveTracks(tracks);
    }

    public List<Track> getTracksByBarcode(String productBarcode) {
        return productService.getTracksByBarcode(productBarcode);
    }

    public boolean deleteTracksByBarcode(String productBarcode) {
        return productService.deleteTracksByBarcode(productBarcode);
    }
    
    // ============================================================
    // PRODUCT DETAILS - For Update Form
    // ============================================================

    public Map<String, Object> getProductDetails(Long productId) {
        return productService.getProductDetails(productId);
    }
}
