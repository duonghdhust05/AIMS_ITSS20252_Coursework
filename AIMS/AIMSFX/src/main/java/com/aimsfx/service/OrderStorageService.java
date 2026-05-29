package com.aimsfx.service;

import com.aimsfx.model.Order;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * OrderStorageService - In-memory order storage implementation
 * 
 * RESPONSIBILITIES:
 * - CRUD operations for orders
 * - ID generation
 * - Thread-safe storage
 * 
 * COHESION: Functional (5/5)
 * COUPLING: Data (4/5) - Only shares data structures
 */
public class OrderStorageService implements IOrderStorage {
    
    private static final Logger LOGGER = Logger.getLogger(OrderStorageService.class.getName());
    private final Map<Integer, Order> storage = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1000);
    
    @Override
    public int save(Order order) {
        if (order == null) {
            throw new java.lang.IllegalArgumentException("Order cannot be null");
        }
        
        int orderId = idGenerator.incrementAndGet();
        order.setOrderId(orderId);
        storage.put(orderId, order);
        
        LOGGER.info("Order saved: ID=" + orderId);
        return orderId;
    }
    
    @Override
    public Order findById(int orderId) {
        Order order = storage.get(orderId);
        if (order == null) {
            LOGGER.warning("Order not found: ID=" + orderId);
        }
        return order;
    }
    
    @Override
    public void update(Order order) {
        if (order == null || order.getOrderId() == 0) {
            throw new java.lang.IllegalArgumentException("Invalid order");
        }
        
        if (!storage.containsKey(order.getOrderId())) {
            throw new IllegalStateException("Order not found: ID=" + order.getOrderId());
        }
        
        storage.put(order.getOrderId(), order);
        LOGGER.info("Order updated: ID=" + order.getOrderId());
    }
    
    @Override
    public void delete(int orderId) {
        Order removed = storage.remove(orderId);
        if (removed != null) {
            LOGGER.info("Order deleted: ID=" + orderId);
        } else {
            LOGGER.warning("Order not found for deletion: ID=" + orderId);
        }
    }
    
    /**
     * Get all orders (for testing/debugging)
     */
    public Collection<Order> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    /**
     * Clear all orders (for testing)
     */
    public void clear() {
        storage.clear();
        idGenerator.set(1000);
        LOGGER.info("All orders cleared");
    }
    
    /**
     * Get order count
     */
    public int count() {
        return storage.size();
    }
}
