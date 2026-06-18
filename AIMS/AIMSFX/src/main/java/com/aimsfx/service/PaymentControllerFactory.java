package com.aimsfx.service;

import com.aimsfx.controller.PayOrderController;
import com.aimsfx.subsystem.paypal.*;
import com.aimsfx.subsystem.vietqr.*;
import com.aimsfx.view.PaymentUI.PayPalWebView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PaymentControllerFactory - Factory Pattern for creating PayOrderController
 * 
 * PURPOSE: Encapsulate the complex creation of PayOrderController with all its
 * dependencies
 * 
 * DESIGN PATTERNS:
 * - Factory Pattern: Creates complex object (PayOrderController)
 * - Singleton Pattern: Caches single instance for reuse
 * 
 * SOLID COMPLIANCE:
 * - SRP: Single responsibility - create and manage PayOrderController lifecycle
 * - DIP: PlaceOrderController depends on this factory (abstraction) instead of
 * creating subsystems directly
 * 
 * BENEFITS:
 * - PlaceOrderController no longer needs to know how to create payment
 * subsystems
 * - Easy to mock for testing
 * - Centralized payment system initialization
 * - Clean separation of concerns
 * 
 * @author ISD-20252-01
 * @version 1.0
 */
public class PaymentControllerFactory {

    private static final Logger LOGGER = Logger.getLogger(PaymentControllerFactory.class.getName());

    // Singleton instance (cached)
    private static PayOrderController cachedController;
    private static IPaymentGateway cachedPayPalGateway;

    // Private constructor to prevent instantiation
    private PaymentControllerFactory() {
        // Factory class - use static methods only
    }

    /**
     * Get or create PayOrderController with all payment subsystems
     * 
     * Creates:
     * - VietQR subsystem (QR code payment)
     * - PayPal subsystem (international payment)
     * - PayPal WebView (approval popup)
     * 
     * @return PayOrderController instance, or null if initialization fails
     */
    public static PayOrderController getPayOrderController() {
        if (cachedController != null) {
            LOGGER.info("Returning cached PayOrderController instance");
            return cachedController;
        }

        try {
            LOGGER.info("Creating new PayOrderController with payment subsystems...");

            // ==================== VietQR Subsystem Setup ====================
            VietQRConfig qrConfig = new VietQRConfig();
            VietQRInteraction interaction = new VietQRInteraction(qrConfig);
            IPaymentQRCode vietQRSystem = new VietQRSubsystem(interaction, qrConfig);
            LOGGER.info("VietQR subsystem initialized");

            // ==================== PayPal Subsystem Setup ====================
            PayPalConfig payPalConfig = new PayPalConfig();
            CurrencyConverter currencyConverter = new CurrencyConverter();
            cachedPayPalGateway = new PayPalSubsystem(
                    payPalConfig.paypalClient(),
                    currencyConverter);
            IPaymentGateway payPalSubsystem = cachedPayPalGateway;
            LOGGER.info("PayPal subsystem initialized");

            // ==================== PayPal View Setup ====================
            IPayPalView payPalView = new PayPalWebView();
            LOGGER.info("PayPal WebView initialized");

            // ==================== Create PayOrderController ====================
            cachedController = new PayOrderController(
                    vietQRSystem,
                    payPalSubsystem,
                    payPalView);

            LOGGER.info("PayOrderController created successfully");
            return cachedController;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create PayOrderController: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Reset cached controller
     * Useful for testing or when payment configuration changes
     */
    public static void reset() {
        LOGGER.info("Resetting cached PayOrderController");
        cachedController = null;
        cachedPayPalGateway = null;
    }

    /**
     * Get the configured PayPal gateway instance
     * 
     * @return IPaymentGateway instance for PayPal
     */
    public static IPaymentGateway getPayPalGateway() {
        if (cachedPayPalGateway == null) {
            getPayOrderController();
        }
        return cachedPayPalGateway;
    }

    /**
     * Check if controller is initialized
     * 
     * @return true if PayOrderController is available
     */
    public static boolean isInitialized() {
        return cachedController != null;
    }

    /**
     * Create PayOrderController for testing with mock dependencies
     * 
     * @param vietQRSubsystem Mock VietQR subsystem
     * @param payPalSubsystem Mock PayPal subsystem
     * @param payPalView      Mock PayPal view
     * @return PayOrderController with injected mocks
     */
    public static PayOrderController createForTesting(
            IPaymentQRCode vietQRSubsystem,
            IPaymentGateway payPalSubsystem,
            IPayPalView payPalView) {

        LOGGER.info("Creating PayOrderController for testing with mock dependencies");
        return new PayOrderController(vietQRSubsystem, payPalSubsystem, payPalView);
    }
}
