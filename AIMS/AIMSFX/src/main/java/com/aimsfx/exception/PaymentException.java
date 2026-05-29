package com.aimsfx.exception;

/**
 * PaymentException Class (Abstract Base)
 * Purpose: Base exception for all payment gateway operations
 * 
 * DESIGN PATTERN: Exception Hierarchy Pattern
 * All specific payment exceptions extend this class.
 * 
 * SOLID Compliance:
 * - OCP: New payment types use existing exception subclasses
 * - LSP: All subclasses are substitutable for this base
 * - DIP: Controllers/Views depend on this abstraction
 * 
 * USAGE:
 * - View layer catches PaymentException to get user-friendly message
 * - errorCode and provider fields are for logging/debugging only
 */
public abstract class PaymentException extends Exception {

    private static final long serialVersionUID = 1L;
	private final String errorCode;
    private final String provider;

    protected PaymentException(String userMessage, String errorCode, String provider) {
        super(userMessage);
        this.errorCode = errorCode;
        this.provider = provider;
    }

    protected PaymentException(String userMessage, Throwable cause, String errorCode, String provider) {
        super(userMessage, cause);
        this.errorCode = errorCode;
        this.provider = provider;
    }

    /**
     * Get provider-specific error code (for debugging)
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Get payment provider name (e.g., "PAYPAL", "VIETQR")
     */
    public String getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return String.format("%s[provider=%s, code=%s]: %s",
                getClass().getSimpleName(), provider, errorCode, getMessage());
    }
}
