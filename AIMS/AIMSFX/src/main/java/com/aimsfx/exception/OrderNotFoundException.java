package com.aimsfx.exception;

/**
 * Exception thrown when an order cannot be found in the system.
 * Used in payOrder operation.
 */
public class OrderNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public OrderNotFoundException(String message) {
        super(message);
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
