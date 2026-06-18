package com.aimsfx.controller.ProductManagerController;

import com.aimsfx.model.Product;
import javafx.collections.ObservableList;

/**
 * IProductDataProvider - Interface for Product Data Access
 * 
 * DESIGN PRINCIPLE: Dependency Inversion Principle (DIP)
 * - High-level modules (ViewProductController) depend on this abstraction
 * - Low-level modules (ProductController) implement this interface
 * 
 * BENEFITS:
 * - Enables unit testing with mock implementations
 * - Decouples product viewing logic from data source
 * - Supports future alternative data sources (e.g., API, cache)
 */
public interface IProductDataProvider {
    
    /**
     * Get all products
     * @return ObservableList of all products
     */
    ObservableList<Product> getProducts();
    
    /**
     * Find product by ID
     * @param productId Product ID
     * @return Product if found, null otherwise
     */
    Product findById(Long productId);
}
