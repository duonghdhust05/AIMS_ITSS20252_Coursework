package com.aimsfx.exception;

/**
 * PaymentCancelledException
 * Thrown when user cancels the payment
 * 
 * Maps from:
 * - PayPal: User closes popup or clicks cancel
 * - VietQR: User cancels QR payment
 * - VNPay: (future) User cancels at bank page
 */
public class PaymentCancelledException extends PaymentException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Payment was cancelled.";

    public PaymentCancelledException(String provider) {
        super(DEFAULT_MESSAGE, "CANCELLED", provider);
    }

    public PaymentCancelledException(String errorCode, String provider) {
        super(DEFAULT_MESSAGE, errorCode, provider);
    }

    public PaymentCancelledException(String customMessage, String errorCode, String provider) {
        super(customMessage, errorCode, provider);
    }
}
