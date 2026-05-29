package com.aimsfx.service;

/**
 * Interface for price formatting operations
 * Enables Dependency Injection and mocking for unit tests
 */
public interface IPriceHelper {
    /**
     * Format price to display string
     */
    String format(double price);
    
    /**
     * Parse price string to double
     */
    double parse(String priceString);
}
