package com.aimsfx.exception;

/**
 * Exception thrown when payment processing fails.
 * Used in payOrder operation to indicate payment was unsuccessful.
 */
public class PaymentFailedException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public PaymentFailedException(String message) {
        super(message);
    }
    
    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
