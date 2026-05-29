package com.aimsfx.subsystem.paypal;

import com.aimsfx.exception.PaymentException;
import java.util.Map;

/**
 * IPaymentGateway Interface
 * Purpose: Abstraction for payment gateway operations (PayPal, Credit Card,
 * etc.)
 * 
 * SOLID Compliance:
 * - SRP: Single responsibility - define payment gateway contract
 * - OCP: Open for extension (new providers), closed for modification
 * Uses semantic PaymentException hierarchy for all providers
 * - LSP: Implementations must fulfill the contract completely
 * - ISP: Minimal interface with only essential payment operations
 * - DIP: High-level modules depend on this abstraction
 * 
 * COHESION: HIGH - Functional Cohesion
 * - createOrder() and captureOrder() work together for payment lifecycle
 * 
 * COUPLING: LOW - Data Coupling
 * - Uses only primitive types (String, double) and standard Map
 * - No dependencies on external SDKs in the interface
 */
public interface IPaymentGateway {

    /**
     * Create a payment order with the gateway
     * 
     * @param orderId Internal order ID from AIMS system
     * @param amount  Amount in VND (implementation handles currency conversion)
     * @return Map containing:
     *         - "orderId": Gateway's order/transaction ID
     *         - "approveUrl": URL for user approval (if applicable)
     * @throws PaymentException if order creation fails
     */
    Map<String, String> createOrder(String orderId, double amount) throws PaymentException;

    /**
     * Capture/confirm a previously approved payment
     * 
     * @param gatewayOrderId The gateway's order ID (returned from createOrder)
     * @return true if capture successful, false otherwise
     * @throws PaymentException if capture operation fails
     */
    boolean captureOrder(String gatewayOrderId) throws PaymentException;
}
