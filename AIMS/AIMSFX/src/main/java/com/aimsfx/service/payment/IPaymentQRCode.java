package com.aimsfx.service.payment;

import com.aimsfx.exception.PaymentException;

/**
 * IPaymentQRCode Interface
 * Purpose: Abstraction for QR-based payment operations
 * 
 * SOLID Compliance:
 * - OCP: PaymentException is generic - new providers don't require interface
 * changes
 * - LSP: Any implementation can throw PaymentException without breaking
 * contract
 * - DIP: Callers depend on this abstraction, not concrete implementations
 * - ISP: Minimal interface with only essential QR payment operations
 */
public interface IPaymentQRCode {

    /**
     * Generate QR code for payment
     * 
     * @param orderId Order identifier
     * @param amount  Payment amount
     * @param content Payment description
     * @return JSON response with QR code data
     * @throws PaymentException if QR generation fails (E74, E76, E222, etc.)
     */
    String generateQRCode(String orderId, long amount, String content) throws PaymentException;

    /**
     * Simulate payment for testing
     * 
     * @param orderId Order identifier
     * @param amount  Payment amount
     * @param content Payment description
     * @throws PaymentException if simulation fails
     */
    void simulatePayment(String orderId, long amount, String content) throws PaymentException;
}