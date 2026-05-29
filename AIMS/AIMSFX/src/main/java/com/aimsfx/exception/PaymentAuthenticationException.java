package com.aimsfx.exception;

/**
 * PaymentAuthenticationException
 * Thrown when authentication/credentials fail
 * 
 * Maps from:
 * - PayPal: system_config_error, payee_not_enabled_for_payment_method
 * - VietQR: E74 (Invalid credentials)
 * - VNPay: (future) 02
 */
public class PaymentAuthenticationException extends PaymentException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Authentication failed. Please check payment credentials.";

    public PaymentAuthenticationException(String errorCode, String provider) {
        super(DEFAULT_MESSAGE, errorCode, provider);
    }

    public PaymentAuthenticationException(String errorCode, String provider, Throwable cause) {
        super(DEFAULT_MESSAGE, cause, errorCode, provider);
    }

    public PaymentAuthenticationException(String customMessage, String errorCode, String provider) {
        super(customMessage, errorCode, provider);
    }
}
