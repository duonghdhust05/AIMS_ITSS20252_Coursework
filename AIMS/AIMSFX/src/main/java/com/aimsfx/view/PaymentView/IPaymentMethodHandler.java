package com.aimsfx.view.PaymentView;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * IPaymentMethodHandler Interface
 * Purpose: Strategy interface for payment method handling
 * 
 * SOLID Compliance:
 * SRP: Single responsibility - define payment method handler contract
 * OCP: Open for extension (new payment methods), closed for modification
 * ISP: Minimal interface with only essential operations
 * DIP: High-level modules depend on this abstraction
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work together for payment method lifecycle
 * 
 * COUPLING: LOW - Data Coupling
 * - Uses only primitive types and standard JavaFX components
 */
public interface IPaymentMethodHandler {

    /**
     * Get the display name of the payment method
     * 
     * @return Payment method name (e.g., "VietQR", "PayPal")
     */
    String getMethodName();

    /**
     * Get the text to display on the confirm button
     * 
     * @return Button text
     */
    String getConfirmButtonText();

    /**
     * Configure the UI elements for this payment method
     * Called when user selects this payment method
     * 
     * @param qrCodeImageView  QR code display (may be null)
     * @param qrCodeContainer  Container for QR elements (may be null)
     * @param loadingIndicator Loading spinner (may be null)
     */
    void setupView(ImageView qrCodeImageView, VBox qrCodeContainer, ProgressIndicator loadingIndicator);

    /**
     * Initialize payment display (e.g., load QR code)
     * Called when order data is available
     * 
     * @param orderId Order ID
     * @param amount  Total amount
     * @param content Payment description
     */
    void initializePayment(String orderId, double amount, String content);

    /**
     * Execute the payment process
     * 
     * @param orderId       Order ID
     * @param amount        Total amount
     * @param content       Payment description
     * @param onSuccess     Callback when payment succeeds
     * @param onShowLoading Callback to show loading indicator
     * @param onHideLoading Callback to hide loading indicator
     */
    void handlePayment(String orderId, double amount, String content,
            Runnable onSuccess, Runnable onShowLoading, Runnable onHideLoading);
}
