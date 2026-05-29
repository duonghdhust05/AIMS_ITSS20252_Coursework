package com.aimsfx.exception;

/**
 * InvalidDeliveryInfoException
 * Thrown by setDeliveryInfo(...) when delivery info is null or invalid.
 */
public class InvalidDeliveryInfoException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public InvalidDeliveryInfoException(String message) {
        super(message);
    }
    
    public InvalidDeliveryInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
