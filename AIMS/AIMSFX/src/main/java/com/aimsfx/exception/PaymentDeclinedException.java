package com.aimsfx.exception;

/**
 * PaymentDeclinedException
 * Thrown when payment is declined by provider (card declined, insufficient
 * funds, etc.)
 * 
 * Maps from:
 * - PayPal: declined_by_payment_method, payment_method_error
 * - VietQR: DECLINED, INSUFFICIENT_FUNDS
 * - VNPay: (future) 07, 12
 */
public class PaymentDeclinedException extends PaymentException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Your payment was declined. Please try a different payment method.";

    public PaymentDeclinedException(String errorCode, String provider) {
        super(DEFAULT_MESSAGE, errorCode, provider);
    }

    public PaymentDeclinedException(String errorCode, String provider, Throwable cause) {
        super(DEFAULT_MESSAGE, cause, errorCode, provider);
    }

    public PaymentDeclinedException(String customMessage, String errorCode, String provider) {
        super(customMessage, errorCode, provider);
    }
}
