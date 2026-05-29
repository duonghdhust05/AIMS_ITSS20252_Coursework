package com.aimsfx.exception;

/**
 * OrderSaveFailedException
 * Thrown by saveOrder(...) when persisting fails.
 */
public class OrderSaveFailedException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public OrderSaveFailedException(String message) {
        super(message);
    }
    
    public OrderSaveFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
