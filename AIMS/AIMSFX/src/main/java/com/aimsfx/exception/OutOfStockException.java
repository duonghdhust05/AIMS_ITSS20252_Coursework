package com.aimsfx.exception;

/**
 * Exception thrown when a product does not have enough stock for the requested quantity.
 * Used in placeOrder operation when checking product availability.
 */
public class OutOfStockException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public OutOfStockException(String message) {
        super(message);
    }
    
    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
