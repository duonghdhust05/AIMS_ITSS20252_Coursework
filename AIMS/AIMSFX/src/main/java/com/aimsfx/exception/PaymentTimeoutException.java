package com.aimsfx.exception;

/**
 * PaymentTimeoutException
 * Thrown on server errors, timeouts, or service unavailable
 * 
 * Maps from:
 * - PayPal: internal_server_error, service_unavailable
 * - VietQR: E222 (callback server offline), TIMEOUT
 * - VNPay: (future) 99
 */
public class PaymentTimeoutException extends PaymentException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Payment service is temporarily unavailable. Please try again later.";

    public PaymentTimeoutException(String errorCode, String provider) {
        super(DEFAULT_MESSAGE, errorCode, provider);
    }

    public PaymentTimeoutException(String errorCode, String provider, Throwable cause) {
        super(DEFAULT_MESSAGE, cause, errorCode, provider);
    }

    public PaymentTimeoutException(String customMessage, String errorCode, String provider) {
        super(customMessage, errorCode, provider);
    }
}
