package com.aimsfx.service;

import com.aimsfx.model.Order;

/**
 * Interface for order storage operations
 * Enables Dependency Injection and mocking for unit tests
 */
public interface IOrderStorage {
    /**
     * Save new order
     * @return Generated order ID
     */
    int save(Order order);
    
    /**
     * Find order by ID
     */
    Order findById(int orderId);
    
    /**
     * Update existing order
     */
    void update(Order order);
    
    /**
     * Delete order
     */
    void delete(int orderId);
}
