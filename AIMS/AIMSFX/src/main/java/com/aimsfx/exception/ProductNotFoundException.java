package com.aimsfx.exception;

/**
 * ProductNotFoundException
 * Thrown when a product is not found or product info is null
 */
public class ProductNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
