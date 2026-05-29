package com.aimsfx.exception;

/**
 * PaymentProcessingException
 * General processing errors that don't fit other categories
 * 
 * Maps from:
 * - PayPal: processing_error, invalid_payment_method, order_not_confirmed
 * - VietQR: E05 (unknown error), QR generation failed
 * - VNPay: (future) general errors
 */
public class PaymentProcessingException extends PaymentException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Unable to process payment. Please try again.";

    public PaymentProcessingException(String errorCode, String provider) {
        super(DEFAULT_MESSAGE, errorCode, provider);
    }

    public PaymentProcessingException(String errorCode, String provider, Throwable cause) {
        super(DEFAULT_MESSAGE, cause, errorCode, provider);
    }

    public PaymentProcessingException(String customMessage, String errorCode, String provider) {
        super(customMessage, errorCode, provider);
    }
}
