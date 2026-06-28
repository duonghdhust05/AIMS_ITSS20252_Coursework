package com.aimsfx.service;

import com.aimsfx.dto.ProductDTO;
import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.exception.UnsupportedProductTypeException;
import com.aimsfx.factory.ProductFactory;
import com.aimsfx.factory.ProductFactoryRegistry;
import com.aimsfx.model.Product;
import com.aimsfx.model.ProductType;
import com.aimsfx.model.StockChangeLog;
import com.aimsfx.model.Track;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.repository.ProductRepository;

import com.aimsfx.repository.TrackRepository;
import com.aimsfx.validator.CommonProductValidator;
import com.aimsfx.validator.ProductValidator;
import com.aimsfx.validator.ValidatorRegistry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductService - Service Layer for Product business logic
 * 
 * PURPOSE: Encapsulates business logic and coordinates between layers
 * 
 * MICROSERVICES PREPARATION - BOUNDED CONTEXT ISOLATION:
 * - This service acts as the core entry point for the 'Product/Catalog' Bounded Context.
 * - In a future distributed architecture, this module is prepared to be extracted into 
 *   a standalone Spring Boot Microservice repository.
 * - We enforce strict module boundaries here: ProductService does NOT directly depend 
 *   on Orders, Carts, or Users entities. It only deals with Catalog concerns.
 * - This strict separation allows us to easily lift-and-shift this logic into a new 
 *   service with its own dedicated database (as prepared with the JSONB schema).
 * 
 * SOLID PRINCIPLES APPLIED:
 * SRP: Single responsibility = Product business operations
 * - No longer manages data storage (delegated to Repository)
 * - No longer handles UI concerns (delegated to Factory)
 * - No longer contains validation logic (delegated to Validators)
 * 
 * OCP: Open for extension, closed for modification
 * - New product types don't require changes here
 * - Uses Factory and Validator registries
 * 
 * LSP: Not applicable (not part of inheritance hierarchy)
 * 
 * ISP: Interface (if created) would be focused on product operations
 * 
 * DIP: Depends on abstractions (Repository, Factory, Validator interfaces)
 * - Uses constructor injection (no Singleton!)
 * - Easy to test with mocks
 * 
 * DEPENDENCIES (all injected):
 * - ProductRepository: Data access
 * - CommonProductValidator: Common field validation
 * - UserDeleteLimitService: Daily deletion limit tracking
 * 
 * BEFORE vs AFTER:
 * BEFORE: ProductController did everything (validation, creation, storage, UI)
 * AFTER: ProductService orchestrates specialized components
 */
public class ProductService {

    private static ProductService instance;

    private final ProductRepository repository;
    private final CommonProductValidator commonValidator;
    private final UserDeleteLimitService deleteLimitService;
    private final TrackRepository trackRepository;

    /**
     * Static factory method - provides default instance
     * 
     * COMPOSITION ROOT: This is the only place that knows about concrete
     * repository.
     * This is acceptable because:
     * - Constructor still depends on abstraction (ProductRepository)
     * - Other methods don't cast or use instanceof
     * - Testing is easy via constructor injection
     */
    public static ProductService getInstance() {
        if (instance == null) {
            ProductRepository repository = new com.aimsfx.repository.DatabaseProductRepository();
            CommonProductValidator commonValidator = new CommonProductValidator();
            instance = new ProductService(repository, commonValidator);
        }
        return instance;
    }

    /**
     * Constructor Injection
     * For testing or custom configurations
     * Dependencies are injected, making testing easy
     */
    public ProductService(ProductRepository repository, CommonProductValidator commonValidator) {
        this.repository = repository;
        this.commonValidator = commonValidator;
        this.deleteLimitService = new UserDeleteLimitService();
        this.trackRepository = new TrackRepository();
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    /**
     * Search products by title, category, or barcode with a limit.
     * Used for autocomplete suggestions and filtered search.
     * 
     * CHANGELOG: Added minPrice and maxPrice parameters for DB-level price filtering.
     */
    public List<Product> searchProducts(String query, Double minPrice, Double maxPrice, int limit) {
        return repository.searchProducts(query, minPrice, maxPrice, limit);
    }

    /**
     * Get a random list of products with a limit.
     * Used for Homepage display to avoid loading all products into memory.
     * 
     * CHANGELOG: Added for memory-optimization in main homepage display.
     */
    public List<Product> getRandomProducts(int limit) {
        return repository.getRandomProducts(limit);
    }

    public Product getProductById(Long id) throws ProductNotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    public List<String> getSupportedTypes() {
        return ProductFactoryRegistry.getSupportedTypes();
    }

    @SuppressWarnings("deprecation")
    public String[] getAttributeLabels(String type) throws UnsupportedProductTypeException {
        ProductFactory factory = ProductFactoryRegistry.getFactory(type);
        return factory.getAttributeLabels();
    }

    public List<AttributeMeta> getAttributeConfig(String type) throws UnsupportedProductTypeException {
        ProductFactory factory = ProductFactoryRegistry.getFactory(type);
        return factory.getAttributeConfig();
    }

    /**
     * 
     * REFACTORED: Uses ProductDTO instead of long parameter list
     * 
     * WORKFLOW:
     * 1. Validate common fields (CommonValidator)
     * 2. Validate type-specific fields (ProductValidator from registry)
     * 3. Create product (ProductFactory from registry)
     * 4. Save to repository
     */
    public Product addProduct(ProductDTO dto)
            throws InvalidProductDataException, UnsupportedProductTypeException {

        String type = dto.getType();

        // Get Factory and attribute keys (OCP: Factory owns the metadata)
        ProductFactory factory = ProductFactoryRegistry.getFactory(type);
        @SuppressWarnings("deprecation")
        String[] keys = factory.getAttributeKeys();

        // Extract attributes array using keys from Factory
        String[] attributes = dto.getAttributesAsArray(keys);

        // Step 1: Validate common fields
        commonValidator.validateCommonFields(type, dto.getBarcode(), dto.getTitle(),
                dto.getOriginalPrice(), dto.getCurrentPrice(),
                dto.getCategory(), dto.getWeight(), dto.getDimensions(), dto.getStock());

        // Step 1.1: Validate price range
        commonValidator.validatePriceRange(dto.getOriginalPrice(), dto.getCurrentPrice());

        // Step 1.2: Check barcode duplication
        if (repository.findCurrentByBarcode(dto.getBarcode()).isPresent()) {
            throw new InvalidProductDataException(
                    "Barcode '" + dto.getBarcode() + "' is already in use by another active product.");
        }

        // Step 2: Validate type-specific attributes
        ProductValidator validator = ValidatorRegistry.getValidator(type);
        validator.validateSpecificAttributes(attributes);

        // Step 3: Create product using factory
        // Pass null as ID for new products - database will auto-generate the ID
        // Status is always set to "available" for new products (handled in Product
        // constructor)
        // OCP: Single unified call - factory knows how to handle weight/dimensions
        // Physical factories use them, digital factories ignore them
        Product product = factory.createProduct(
                null, dto.getBarcode(), dto.getTitle(), dto.getCategory(),
                dto.getOriginalPrice(), dto.getCurrentPrice(),
                dto.getDescription(), dto.getWeight(), dto.getDimensions(),
                dto.getStock(), "available", dto.getVatRate(),
                attributes);

        // Set creation timestamp
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // Step 4: Save to repository
        return repository.save(product);
    }

    /**
     * WORKFLOW:
     * 1. Find existing product
     * 2. Validate common fields
     * 3. Validate type-specific fields
     * 4. Verify type hasn't changed (business rule)
     * 5. Create updated product
     * 6. Save to repository
     */
    public Product updateProduct(ProductDTO dto)
            throws InvalidProductDataException, UnsupportedProductTypeException, ProductNotFoundException {

        Long productId = dto.getProductId();
        String type = dto.getType();

        // Get Factory and attribute keys (OCP: Factory owns the metadata)
        ProductFactory factory = ProductFactoryRegistry.getFactory(type);
        @SuppressWarnings("deprecation")
        String[] keys = factory.getAttributeKeys();

        // Extract attributes array using keys from Factory
        String[] attributes = dto.getAttributesAsArray(keys);

        // Step 1: Find existing product
        Product existingProduct = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Step 2: Validate common fields
        commonValidator.validateCommonFields(type, dto.getBarcode(), dto.getTitle(),
                dto.getOriginalPrice(), dto.getCurrentPrice(),
                dto.getCategory(), dto.getWeight(), dto.getDimensions(), existingProduct.getStock());

        // Step 2.1: Validate price range for update
        commonValidator.validatePriceRange(dto.getOriginalPrice(), dto.getCurrentPrice());

        // Step 2.2: Check barcode duplication if changed
        if (!existingProduct.getBarcode().equals(dto.getBarcode())) {
            if (repository.findCurrentByBarcode(dto.getBarcode()).isPresent()) {
                throw new InvalidProductDataException(
                        "Barcode '" + dto.getBarcode() + "' is already in use by another active product.");
            }
        }

        // Step 3: Validate type-specific attributes
        ProductValidator validator = ValidatorRegistry.getValidator(type);
        validator.validateSpecificAttributes(attributes);

        // Step 4: Verify type hasn't changed (business rule)
        ProductType existingType = getProductType(existingProduct);
        ProductType newType = ProductType.fromString(type);
        if (existingType != newType) {
            throw new InvalidProductDataException(
                    "Cannot change product type from " + existingType + " to " + newType);
        }

        // Step 5: Create updated product with same ID
        // OCP: Single unified call - factory knows how to handle weight/dimensions
        Product updatedProduct = factory.createProduct(
                productId, dto.getBarcode(), dto.getTitle(), dto.getCategory(),
                dto.getOriginalPrice(), dto.getCurrentPrice(),
                dto.getDescription(), dto.getWeight(), dto.getDimensions(),
                existingProduct.getStock(), dto.getStatus(), dto.getVatRate(),
                attributes);

        updatedProduct.setCreatedAt(existingProduct.getCreatedAt());
        updatedProduct.setUpdatedAt(LocalDateTime.now());

        // Step 6: Save to repository
        return repository.save(updatedProduct);
    }

    public boolean updateStock(String barcode, Integer newStock, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason for stock change is required");
        }

        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        return repository.updateStock(barcode, newStock, reason);
    }

    public List<StockChangeLog> getStockChangeHistory(String barcode) {
        return repository.getStockChangeHistory(barcode);
    }

    private ProductType getProductType(Product product) {
        String simpleClassName = product.getClass().getSimpleName();
        return ProductType.fromString(simpleClassName);
    }

    public List<Product> getProductHistory(Long productId) {
        return repository.findHistoryByProductId(productId);
    }

    /**
     * BUSINESS RULES:
     * 1. If stock = 0: Soft delete (set is_current = false and expired_date =
     * NOW())
     * 2. If stock > 0: Deactivate (set status = "deactivated")
     * 3. Users can only delete 20 products per day
     * 
     * @param productId Product ID to delete
     * @param userId    User ID performing the deletion
     * @return true if deleted/deactivated successfully
     * @throws ProductNotFoundException                            if product not
     *                                                             found
     * @throws com.aimsfx.exception.DeletionLimitExceededException if user exceeded
     *                                                             daily limit
     */
    public boolean deleteProduct(Long productId, Long userId)
            throws ProductNotFoundException, com.aimsfx.exception.DeletionLimitExceededException {
        // Verify product exists first
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Check if user can delete (hasn't exceeded daily limit)
        if (!deleteLimitService.canDeleteProduct(userId)) {
            int currentCount = deleteLimitService.getDeleteCount(userId, java.time.LocalDate.now());
            int maxLimit = deleteLimitService.getMaxDeletionsPerDay();
            throw new com.aimsfx.exception.DeletionLimitExceededException(
                    "Daily deletion limit exceeded. You can only delete " + maxLimit + " products per day.",
                    currentCount,
                    maxLimit);
        }

        boolean result;

        // Check stock level to determine action
        if (product.getStock() == null || product.getStock() == 0) {
            // Stock = 0: Perform soft delete
            result = repository.deleteById(productId);
        } else {
            // Stock > 0: Deactivate product (change status to "deactivated")
            product.setStatus("deactivated");
            product.setUpdatedAt(LocalDateTime.now());
            Product updatedProduct = repository.save(product);
            result = updatedProduct != null;
        }

        // If operation successful, increment deletion count
        if (result) {
            deleteLimitService.incrementDeleteCount(userId);
        }

        return result;
    }

    public int getRemainingDeletionQuota(Long userId) {
        return deleteLimitService.getRemainingQuota(userId);
    }

    /**
     * Validate and delete multiple products
     * 
     * BUSINESS RULES:
     * 1. If any product has stock > 0, throw exception with list of barcodes (don't
     * delete anything)
     * 2. Check if deletion count would exceed daily limit
     * 3. Only perform deletion if all validations pass
     * 
     * @param products List of products to delete
     * @param userId   User ID performing the deletion
     * @return Number of successfully deleted products
     * @throws com.aimsfx.exception.BulkDeleteValidationException if validation
     *                                                            fails
     */
    public int deleteMultipleProducts(List<Product> products, Long userId)
            throws com.aimsfx.exception.BulkDeleteValidationException {

        // Step 1: Check daily deletion limit
        int remainingQuota = deleteLimitService.getRemainingQuota(userId);
        int requestedCount = products.size();

        if (requestedCount > remainingQuota) {
            throw new com.aimsfx.exception.BulkDeleteValidationException(remainingQuota, requestedCount);
        }

        // Step 2: Perform deletion or deactivation
        int successCount = 0;

        for (Product product : products) {
            boolean result = false;
            if (product.getStock() == null || product.getStock() == 0) {
                // Stock = 0: Perform soft delete
                result = repository.deleteById(product.getProductId());
            } else {
                // Stock > 0: Deactivate product
                product.setStatus("deactivated");
                product.setUpdatedAt(LocalDateTime.now());
                Product updatedProduct = repository.save(product);
                result = updatedProduct != null;
            }

            if (result) {
                deleteLimitService.incrementDeleteCount(userId);
                successCount++;
            }
        }

        return successCount;
    }

    public boolean saveTracks(List<Track> tracks) {
        return trackRepository.saveTracks(tracks);
    }

    public List<Track> getTracksByBarcode(String productBarcode) {
        return trackRepository.findByProductBarcode(productBarcode);
    }

    public boolean deleteTracksByBarcode(String productBarcode) {
        return trackRepository.deleteByProductBarcode(productBarcode);
    }

    public java.util.Map<String, Object> getProductDetails(Long productId) {
        return repository.getProductDetails(productId);
    }
}
