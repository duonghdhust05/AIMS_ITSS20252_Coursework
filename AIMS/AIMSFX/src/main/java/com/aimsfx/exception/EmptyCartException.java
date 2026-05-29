package com.aimsfx.exception;

/**
 * EmptyCartException
 * Thrown by Order(cart) when cart is empty.
 */
public class EmptyCartException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public EmptyCartException(String message) {
        super(message);
    }
    
    public EmptyCartException(String message, Throwable cause) {
        super(message, cause);
    }
}
