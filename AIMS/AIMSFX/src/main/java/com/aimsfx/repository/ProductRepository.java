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
 * - OCP: Can swap implementations (in-memory, database, file) without changing clients
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

    boolean updateStock(String barcode, Integer newStock, String reason);

    List<StockChangeLog> getStockChangeHistory(String barcode);

    Map<String, Object> getProductDetails(Long productId);
}
