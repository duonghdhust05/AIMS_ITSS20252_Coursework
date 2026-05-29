package com.aimsfx.exception;

/**
 * Exception thrown when the delivery address is not supported
 * and delivery fee cannot be calculated.
 * Used in submitDeliveryInfo operation.
 */
public class UnsupportedLocationException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public UnsupportedLocationException(String message) {
        super(message);
    }
    
    public UnsupportedLocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
