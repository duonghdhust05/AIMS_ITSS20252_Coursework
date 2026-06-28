package com.aimsfx.controller;

import com.aimsfx.exception.PaymentException;
import com.aimsfx.model.Order;
import com.aimsfx.service.PaymentService;
import com.aimsfx.service.payment.IPaymentGateway;
import com.aimsfx.subsystem.paypal.IPayPalView;
import com.aimsfx.service.payment.IPaymentQRCode;
import javafx.application.Platform;

import java.util.Map;
import java.util.function.Consumer;

/**
 * PayOrderController Class
 * Purpose: Controller for handling payment operations (VietQR + PayPal)
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward payment processing
 * 
 * COUPLING: LOW - Data Coupling
 * - Uses interfaces for all subsystem dependencies
 * - Uses PaymentService for persistence
 */
public class PayOrderController {

    private final IPaymentQRCode vietQRSubsystem;
    private final IPaymentGateway payPalSubsystem;
    private final IPayPalView payPalView;
    private final PaymentService paymentService;

    /**
     * Constructor with default PaymentService
     * Used in production - creates PaymentService internally
     */
    public PayOrderController(IPaymentQRCode vietQRSubsystem,
            IPaymentGateway payPalSubsystem,
            IPayPalView payPalView) {
        this(vietQRSubsystem, payPalSubsystem, payPalView, new PaymentService());
    }

    /**
     * Constructor with dependency injection for all dependencies
     * DIP COMPLIANT: Allows injecting mock PaymentService for testing
     * 
     * @param vietQRSubsystem VietQR payment subsystem
     * @param payPalSubsystem PayPal payment subsystem
     * @param payPalView PayPal view interface
     * @param paymentService Payment service for persistence
     */
    public PayOrderController(IPaymentQRCode vietQRSubsystem,
            IPaymentGateway payPalSubsystem,
            IPayPalView payPalView,
            PaymentService paymentService) {
        this.vietQRSubsystem = vietQRSubsystem;
        this.payPalSubsystem = payPalSubsystem;
        this.payPalView = payPalView;
        this.paymentService = paymentService;
    }

    /**
     * Process payment and save transaction via service layer
     * 
     * @param orderId               Internal order ID
     * @param amount                Payment amount
     * @param paymentMethod         "VIETQR" or "PAYPAL"
     * @param externalTransactionId Transaction ID from payment provider
     * @return true if successful
     */
    public boolean processPayment(int orderId, double amount, String paymentMethod, String externalTransactionId) {
        return paymentService.processPayment(orderId, amount, paymentMethod, externalTransactionId);
    }

    /**
     * Request VietQR payment - generates QR code
     * 
     * @throws PaymentException if QR generation fails (E74, E76, E222)
     */
    public String requestPayment(String orderId, double amount, String content) throws PaymentException {
        long amountLong = (long) amount;
        return vietQRSubsystem.generateQRCode(orderId, amountLong, content);
    }

    /**
     * Simulate VietQR payment success (for testing)
     * 
     * @throws PaymentException if simulation fails (E222: ngrok not running)
     */
    public void simulatePaymentSuccess(String orderId, double amount, String content) throws PaymentException {
        long amountLong = (long) amount;
        vietQRSubsystem.simulatePayment(orderId, amountLong, content);
    }

    /**
     * Request PayPal payment
     * 
     * Flow per sequence diagram:
     * 1. createOrder() → securePaymentURL
     * 2. displayPopUp() with approval page
     * 3. [Customer cancels] → paymentCancelled() → View handles display
     * 4. [Customer approves] → callback → captureOrder()
     * 5. [API fail] → onError callback → View calls displayErrorMessage()
     * 
     * MVC: Controller returns error info via callback, View displays it
     * 
     * @param orderId          Internal order ID
     * @param amount           Payment amount in VND
     * @param onPaymentSuccess Callback with PayPal order ID on success
     * @param onPaymentError   Callback with error message on failure
     * @param onPaymentCancel  Callback when user cancels
     */
    public void requestPayPalPayment(String orderId, double amount,
            Consumer<String> onPaymentSuccess,
            Consumer<String> onPaymentError,
            Runnable onPaymentCancel) {
        new Thread(() -> {
            try {
                Map<String, String> response = payPalSubsystem.createOrder(orderId, amount);

                if (response != null) {
                    String approvalUrl = response.get("approveUrl");
                    String paypalOrderId = response.get("orderId");

                    Platform.runLater(() -> {
                        payPalView.displayApprovalPage(approvalUrl,
                                (successUrl) -> handleCapture(paypalOrderId, orderId, amount,
                                        onPaymentSuccess, onPaymentError),
                                () -> {
                                    // Per sequence diagram: Customer cancels → paymentCancelled()
                                    System.out.println("[PayOrderController] Payment cancelled by user");
                                    onPaymentCancel.run();
                                });
                    });
                }
            } catch (PaymentException e) {
                // Per sequence diagram: API call fail → return error to View
                System.err.println("PayPal Error [" + e.getErrorCode() + "]: " + e.getMessage());
                Platform.runLater(() -> onPaymentError.accept(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> onPaymentError.accept("Unexpected error: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Request PayPal payment using PaymentRequestDTO (Security and Payload Optimized)
     */
    public void requestPayPalPaymentWithDTO(com.aimsfx.dto.PaymentRequestDTO requestDTO,
            Consumer<com.aimsfx.dto.TransactionResponseDTO> onPaymentSuccess,
            Consumer<String> onPaymentError,
            Runnable onPaymentCancel) {
        new Thread(() -> {
            try {
                Map<String, String> response = payPalSubsystem.createOrder(requestDTO);
                
                if (response != null) {
                    String approvalUrl = response.get("approveUrl");
                    String paypalOrderId = response.get("orderId");

                    Platform.runLater(() -> {
                        payPalView.displayApprovalPage(approvalUrl,
                                (successUrl) -> handleCaptureWithDTO(paypalOrderId, requestDTO,
                                        onPaymentSuccess, onPaymentError),
                                () -> {
                                    System.out.println("[PayOrderController] Payment cancelled by user");
                                    onPaymentCancel.run();
                                });
                    });
                }
            } catch (PaymentException e) {
                System.err.println("PayPal Error [" + e.getErrorCode() + "]: " + e.getMessage());
                Platform.runLater(() -> onPaymentError.accept(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> onPaymentError.accept("Unexpected error: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Handle PayPal order capture after user approval
     * 
     * Flow per sequence diagram:
     * 1. captureOrder(token) → transactionResult
     * 2. [API call successful] → onSuccess callback → paymentSuccess()
     * 3. [API call fail] → onError callback → View calls displayErrorMessage()
     */
    private void handleCapture(String paypalOrderId, String orderId, double amount,
            Consumer<String> onPaymentSuccess,
            Consumer<String> onPaymentError) {
        new Thread(() -> {
            try {
                boolean success = payPalSubsystem.captureOrder(paypalOrderId);

                Platform.runLater(() -> {
                    if (success) {
                        // Per sequence diagram: API call successful → paymentSuccess()
                        onPaymentSuccess.accept(paypalOrderId);
                    } else {
                        // Capture returned false but no exception
                        onPaymentError.accept("PayPal payment capture was not completed.");
                    }
                });
            } catch (PaymentException e) {
                // Per sequence diagram: API call fail → return error to View
                System.err.println("PayPal Capture Error [" + e.getErrorCode() + "]: " + e.getMessage());
                Platform.runLater(() -> onPaymentError.accept(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> onPaymentError.accept("PayPal API Error: " + e.getMessage()));
            }
        }).start();
    }

    private void handleCaptureWithDTO(String paypalOrderId, com.aimsfx.dto.PaymentRequestDTO requestDTO,
            Consumer<com.aimsfx.dto.TransactionResponseDTO> onPaymentSuccess,
            Consumer<String> onPaymentError) {
        new Thread(() -> {
            try {
                boolean success = payPalSubsystem.captureOrder(paypalOrderId);

                Platform.runLater(() -> {
                    if (success) {
                        com.aimsfx.model.TransactionInfo transaction = new com.aimsfx.model.TransactionInfo();
                        transaction.setTransactionId(paypalOrderId);
                        transaction.setAmount(requestDTO.getAmount());
                        transaction.setCurrency("VND");
                        transaction.setPaymentMethod("PAYPAL");
                        transaction.setStatus(com.aimsfx.model.TransactionInfo.TransactionStatus.CAPTURED);
                        onPaymentSuccess.accept(new com.aimsfx.dto.TransactionResponseDTO(transaction));
                    } else {
                        onPaymentError.accept("PayPal payment capture was not completed.");
                    }
                });
            } catch (PaymentException e) {
                System.err.println("PayPal Capture Error [" + e.getErrorCode() + "]: " + e.getMessage());
                Platform.runLater(() -> onPaymentError.accept(e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> onPaymentError.accept("PayPal API Error: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Complete payment after successful transaction
     * Updates product stock and order status
     * 
     * SOLID Compliance:
     * - SRP: Controller delegates to Service layer
     * - DIP: Depends on PaymentService abstraction
     * 
     * LAYERED ARCHITECTURE:
     * View → Controller (this) → Service → Repository
     * 
     * @param order         The order that was successfully paid
     * @param paymentMethod "VIETQR" or "PAYPAL"
     */
    public void completePayment(Order order, String paymentMethod) {
        paymentService.completePayment(order, paymentMethod);
    }
}