package com.aimsfx.exception;

/**
 * Exception thrown when an order has no invoice but payment was called.
 * Used in payOrder operation.
 */
public class InvoiceNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public InvoiceNotFoundException(String message) {
        super(message);
    }
    
    public InvoiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
