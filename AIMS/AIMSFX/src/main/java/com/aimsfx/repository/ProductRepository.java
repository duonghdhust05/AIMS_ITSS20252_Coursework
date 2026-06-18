package com.aimsfx.repository;

import com.aimsfx.model.Product;
import com.aimsfx.model.StockChangeLog;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ProductRepository - Repository Pattern Interface
 * 
 * DESIGN PATTERN: Repository Pattern
 * PURPOSE: Abstracts data access layer from business logic
 * 
 * SOLID PRINCIPLES:
 * - SRP: Single responsibility = data persistence and retrieval
 * - OCP: Can swap implementations (in-memory, database, file) without changing
 * clients
 * - DIP: Clients depend on abstraction, not concrete implementation
 * - ISP: Minimal interface with only necessary operations
 * 
 * BENEFITS:
 * - Separates data access from business logic
 * - Easy to test (can mock repository)
 * - Can switch storage mechanism without affecting business layer
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    Optional<Product> findCurrentByBarcode(String barcode);

    List<Product> findAll();

    boolean deleteById(Long id);

    boolean existsById(Long id);

    List<Product> findHistoryByProductId(Long productId);

    boolean updateStock(Long productId, Integer newStock);

    /**
     * Atomically deducts stock from the database (solves Race Condition).
     * 
     * @param productId The ID of the product
     * @param quantity  The amount to deduct
     * @return true if successful, false if out of stock or product not found
     */
    boolean deductStockAtomically(Long productId, int quantity);

    /**
     * Atomically restores stock to the database (used for Compensating
     * Transactions).
     * 
     * @param productId The ID of the product
     * @param quantity  The amount to restore
     * @return true if successful, false otherwise
     */

    boolean restoreStock(Long productId, int quantity);

    /**
     * Atomically deducts stock for an entire order using a single database transaction.
     * Prevents partial stock deduction if one item is out of stock.
     */
    boolean deductStockForOrder(List<com.aimsfx.model.OrderItem> items);

    /**
     * Atomically restores stock for an entire order using a single database transaction.
     * Used when an order is cancelled or rejected.
     */
    boolean restoreStockForOrder(List<com.aimsfx.model.OrderItem> items);

    boolean updateStock(String barcode, Integer newStock, String reason);

    List<StockChangeLog> getStockChangeHistory(String barcode);

    Map<String, Object> getProductDetails(Long productId);

    /**
     * Search products by title, category, or barcode with a limit.
     * Used for autocomplete suggestions and filtered search.
     * 
     * CHANGELOG: Added minPrice and maxPrice parameters for DB-level price filtering.
     */
    List<Product> searchProducts(String query, Double minPrice, Double maxPrice, int limit);

    /**
     * Get a random list of products with a limit.
     * Used for Homepage display to avoid loading all products into memory.
     */
    List<Product> getRandomProducts(int limit);
}
