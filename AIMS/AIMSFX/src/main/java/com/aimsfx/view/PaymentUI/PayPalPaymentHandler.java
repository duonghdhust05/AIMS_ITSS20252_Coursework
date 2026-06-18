package com.aimsfx.view.PaymentUI;

import com.aimsfx.controller.PayOrderController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * PayPalPaymentHandler Class
 * Purpose: Strategy implementation for PayPal payment method
 * 
 * SOLID Compliance:
 * - SRP: Handles only PayPal-specific payment UI operations
 * - OCP: Implements IPaymentMethodHandler without modifying it
 * - LSP: Properly substitutable for IPaymentMethodHandler
 * - DIP: Depends on PayOrderController (not repositories directly)
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward PayPal payment UI
 * 
 * COUPLING: LOW - Data Coupling
 * - Uses PayOrderController through method calls with primitive parameters
 * - NO DIRECT repository access (proper layered architecture)
 * 
 * LAYERED ARCHITECTURE:
 * View (this) → Controller (PayOrderController) → Service → Repository
 */
public class PayPalPaymentHandler implements IPaymentMethodHandler {

    private final PayOrderController payOrderController;

    public PayPalPaymentHandler(PayOrderController payOrderController) {
        this.payOrderController = payOrderController;
    }

    @Override
    public String getMethodName() {
        return "PayPal";
    }

    @Override
    public String getConfirmButtonText() {
        return "Pay with PayPal";
    }

    @Override
    public void setupView(ImageView qrCodeImageView, VBox qrCodeContainer, ProgressIndicator loadingIndicator) {
    }

    @Override
    public void initializePayment(String orderId, double amount, String content) {
        // PayPal doesn't need pre-initialization - payment starts when user clicks
    }

    @Override
    public void handlePayment(String orderId, double amount, String content,
            Runnable onSuccess, Runnable onShowLoading, Runnable onHideLoading) {
        onShowLoading.run();

        // PayPal flow: opens WebView for user approval, then captures payment
        // MVC: Controller returns result via callbacks, View handles display
        payOrderController.requestPayPalPayment(orderId, amount,
                // onPaymentSuccess callback
                (paypalOrderId) -> {
                    onHideLoading.run();

                    // Use controller to process payment (Controller → Service → Repository)
                    boolean paymentSuccess = false;
                    try {
                        int orderIdInt = Integer.parseInt(orderId);

                        // Delegate to controller, which uses PaymentService
                        paymentSuccess = payOrderController.processPayment(orderIdInt, amount, "PAYPAL", paypalOrderId);

                        if (paymentSuccess) {
                            System.out.println(
                                    "[PayPalPaymentHandler] Payment processed via service layer: " + paypalOrderId);
                        } else {
                            System.err.println("[PayPalPaymentHandler] Payment processing failed");
                        }
                    } catch (Exception e) {
                        System.err.println("[PayPalPaymentHandler] Failed to process payment: " + e.getMessage());
                        e.printStackTrace();
                        paymentSuccess = false;
                    }

                    if (paymentSuccess) {
                        onSuccess.run();
                    } else {
                        displayErrorMessage("Failed to save payment transaction. Please contact support.");
                    }
                },
                // onPaymentError callback - Controller returns error, View displays
                (errorMessage) -> {
                    onHideLoading.run();
                    displayErrorMessage(errorMessage);
                },
                // onPaymentCancel callback
                () -> {
                    onHideLoading.run();
                    displayCancelMessage();
                });
    }

    /**
     * Display error message to user
     * Per sequence diagram: displayErrorMessage() → void
     * MVC: View handles all UI display
     */
    private void displayErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Payment Failed");
            alert.setHeaderText("PayPal Payment Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Display cancellation message to user
     */
    private void displayCancelMessage() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Cancelled");
            alert.setHeaderText(null);
            alert.setContentText("You cancelled the PayPal payment.");
            alert.showAndWait();
        });
    }
}
