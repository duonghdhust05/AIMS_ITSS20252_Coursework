package com.aimsfx.view.PaymentView;

import com.aimsfx.controller.PayOrderController;
import com.aimsfx.exception.PaymentException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import com.aimsfx.utils.UIUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * VietQRPaymentHandler Class
 * Purpose: Strategy implementation for VietQR payment method
 * 
 * SOLID Compliance:
 * - SRP: Handles only VietQR-specific payment UI operations
 * - OCP: Implements IPaymentMethodHandler without modifying it
 * - LSP: Properly substitutable for IPaymentMethodHandler
 * - DIP: Depends on PayOrderController (not repositories directly)
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward VietQR payment UI
 * 
 * COUPLING: LOW - Data Coupling
 * - Uses PayOrderController through method calls with primitive parameters
 * - NO DIRECT repository access (proper layered architecture)
 * 
 * LAYERED ARCHITECTURE:
 * View (this) → Controller (PayOrderController) → Service → Repository
 */
public class VietQRPaymentHandler implements IPaymentMethodHandler {

    private final PayOrderController payOrderController;

    // UI Components (set via setupView)
    private ImageView qrCodeImageView;
    private ProgressIndicator loadingIndicator;

    public VietQRPaymentHandler(PayOrderController payOrderController) {
        this.payOrderController = payOrderController;
    }

    @Override
    public String getMethodName() {
        return "VietQR";
    }

    @Override
    public String getConfirmButtonText() {
        return "Pay with VietQR";
    }

    @Override
    public void setupView(ImageView qrCodeImageView, VBox qrCodeContainer, ProgressIndicator loadingIndicator) {
        this.qrCodeImageView = qrCodeImageView;
        this.loadingIndicator = loadingIndicator;

        // Show QR-related elements for VietQR
        if (qrCodeImageView != null)
            qrCodeImageView.setVisible(true);
        if (qrCodeContainer != null)
            qrCodeContainer.setVisible(true);
    }

    @Override
    public void initializePayment(String orderId, double amount, String content) {
        // Request QR code when payment is initialized
        requestQRCode(orderId, amount, content);
    }

    @Override
    public void handlePayment(String orderId, double amount, String content,
            Runnable onSuccess, Runnable onShowLoading, Runnable onHideLoading) {
        onShowLoading.run();

        new Thread(() -> {
            boolean paymentSuccess = false;
            String errorMessage = null;
            try {
                // Step 1: Simulate payment via VietQR (can throw PaymentException)
                payOrderController.simulatePaymentSuccess(orderId, amount, content);

                // Step 2: Process payment in database (only if simulation succeeds)
                int orderIdInt = Integer.parseInt(orderId);
                String transactionId = "VIETQR_SIM_" + System.currentTimeMillis();
                paymentSuccess = payOrderController.processPayment(orderIdInt, amount, "VIETQR", transactionId);

                if (paymentSuccess) {
                    System.out.println("[VietQRPaymentHandler] Payment processed: " + transactionId);
                } else {
                    errorMessage = "Failed to save transaction to database";
                }

            } catch (PaymentException e) {
                // VietQR error (E74, E76, E222, etc.) - NO saving should happen
                System.err.println("[VietQRPaymentHandler] VietQR Error [" + e.getErrorCode() + "]: " + e.getMessage());
                e.printStackTrace();
                errorMessage = e.getMessage();
                paymentSuccess = false;

            } catch (Exception e) {
                System.err.println("[VietQRPaymentHandler] Unexpected error: " + e.getMessage());
                e.printStackTrace();
                errorMessage = e.getMessage();
                paymentSuccess = false;
            }

            final boolean finalSuccess = paymentSuccess;
            final String finalErrorMsg = errorMessage;
            Platform.runLater(() -> {
                onHideLoading.run();

                if (finalSuccess) {
                    UIUtils.showAlert("Success", "VietQR Payment Successful!");
                    onSuccess.run();
                } else {
                    // Show clean error message (error code logged to console for debugging)
                    UIUtils.showError("Payment Failed", finalErrorMsg);
                }
            });
        }).start();
    }

    /**
     * Request QR code from VietQR API
     * Handles PaymentException for errors like E74 (invalid token), E76 (account
     * not registered)
     */
    private void requestQRCode(String orderId, double amount, String content) {
        showLoading();

        new Thread(() -> {
            try {
                String jsonResponse = payOrderController.requestPayment(orderId, amount, content);

                Platform.runLater(() -> {
                    hideLoading();
                    displayQRCode(jsonResponse);
                });

            } catch (PaymentException e) {
                // VietQR error - show clean error to user (code logged for debugging)
                String errorCode = e.getErrorCode() != null ? e.getErrorCode() : "UNKNOWN";
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Payment failed. Please try again.";
                System.err.println("[VietQRPaymentHandler] QR Error [" + errorCode + "]: " + errorMsg);
                Platform.runLater(() -> {
                    hideLoading();
                    UIUtils.showError("Payment Error", errorMsg);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    hideLoading();
                    UIUtils.showError("Error", "Failed to generate QR code: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Display QR code from API response
     */
    private void displayQRCode(String response) {
        if (response == null || response.isEmpty() || qrCodeImageView == null)
            return;

        try {
            if (response.trim().startsWith("{")) {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                String bankCode = json.has("bankCode") ? json.get("bankCode").getAsString() : "MB";
                String account = "";
                if (json.has("bankAccount"))
                    account = json.get("bankAccount").getAsString();
                else if (json.has("accountNo"))
                    account = json.get("accountNo").getAsString();

                String amountStr = json.has("amount") ? json.get("amount").getAsString() : "0";
                String contentStr = json.has("content") ? json.get("content").getAsString() : "";
                String encodedContent = URLEncoder.encode(contentStr, StandardCharsets.UTF_8);

                String accountNameStr = json.has("accountName") ? json.get("accountName").getAsString() : "";
                String encodedAccountName = URLEncoder.encode(accountNameStr, StandardCharsets.UTF_8);

                String imageUrl = String.format(
                        "https://img.vietqr.io/image/%s-%s-compact.png?amount=%s&addInfo=%s&accountName=%s",
                        bankCode, account, amountStr, encodedContent, encodedAccountName);

                qrCodeImageView.setImage(new Image(imageUrl, true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading() {
        if (loadingIndicator != null)
            loadingIndicator.setVisible(true);
        if (qrCodeImageView != null)
            qrCodeImageView.setOpacity(0.3);
    }

    private void hideLoading() {
        if (loadingIndicator != null)
            loadingIndicator.setVisible(false);
        if (qrCodeImageView != null)
            qrCodeImageView.setOpacity(1.0);
    }
}
