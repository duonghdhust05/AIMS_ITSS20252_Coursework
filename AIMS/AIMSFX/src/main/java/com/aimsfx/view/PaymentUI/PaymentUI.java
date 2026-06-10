package com.aimsfx.view.PaymentUI;

import com.aimsfx.controller.PayOrderController;
import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.model.TransactionInfo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * PaymentUI Class
 * Purpose: UI for payment method selection and processing
 * 
 * REFACTORED: Now uses Strategy Pattern for payment method handling
 * 
 * SOLID Compliance:
 * SRP: UI responsibility only - delegates payment logic to handlers
 * OCP: Adding new payment methods requires NO modification to this class
 * - Just add new handler implementation and register in initialize()
 * DIP: Depends on IPaymentMethodHandler abstraction, not concrete handlers
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward payment UI orchestration
 * - Payment-specific logic delegated to handlers
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with IPaymentMethodHandler (abstraction)
 * - Uses: handlePayment(), setupView(), getMethodName()
 * - Type: Data coupling - passes primitives and callbacks
 * 
 * 2. Stamp Coupling with Order/Invoice (unchanged)
 * - Uses: order.getOrderId(), order.getTotalAmount()
 * - Type: Stamp coupling - acceptable for data transfer
 * 
 * 3. Control Coupling with PlaceOrderController (navigation)
 * - Uses: setSuccessData() to pass data to success screen
 * - Type: Control coupling - controls navigation flow
 */
public class PaymentUI {

    // --- FXML Links ---
    @FXML
    private ImageView qrCodeImageView;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button btnConfirmPayment;

    @FXML
    private ToggleGroup paymentMethodGroup;
    @FXML
    private RadioButton vietqrRadio;
    @FXML
    private RadioButton paypalRadio;

    @FXML
    private VBox vietqrBox;
    @FXML
    private VBox paypalBox;
    @FXML
    private VBox qrCodeContainer;

    @FXML
    private Label totalLabel;
    @FXML
    private Label transactionIdLabel;
    @FXML
    private Label totalLabelInQR;

    // --- Strategy Pattern: Payment Method Handlers ---
    private final Map<String, IPaymentMethodHandler> paymentHandlers = new HashMap<>();
    private IPaymentMethodHandler activeHandler;

    private final PayOrderController payOrderController;
    private Order currentOrder;
    private Invoice currentInvoice;

    public PaymentUI(PayOrderController payOrderController) {
        this.payOrderController = payOrderController;
    }

    @FXML
    public void initialize() {
        // Register payment method handlers (Strategy Pattern)
        registerPaymentHandlers();

        // Listen for payment method selection changes
        paymentMethodGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == vietqrRadio) {
                setActiveHandler("vietqr");
            } else if (newToggle == paypalRadio) {
                setActiveHandler("paypal");
            }
        });

        if (vietqrBox != null) {
            vietqrBox.setOnMouseClicked(e -> vietqrRadio.setSelected(true));
        }
        if (paypalBox != null) {
            paypalBox.setOnMouseClicked(e -> paypalRadio.setSelected(true));
        }
        if (vietqrRadio.isSelected()) {
            setActiveHandler("vietqr");
        } else {
            setActiveHandler("paypal");
        }
    }

    /**
     * Register all payment method handlers
     * 
     * OCP: To add new payment method (e.g., MoMo, ZaloPay):
     * 1. Create new class implementing IPaymentMethodHandler
     * 2. Add one line here: paymentHandlers.put("momo", new
     * MoMoPaymentHandler(...));
     * 3. Add radio button in FXML
     * 
     * No modification needed in handleConfirmPayment() or other methods!
     */
    private void registerPaymentHandlers() {
        paymentHandlers.put("vietqr", new VietQRPaymentHandler(payOrderController));
        paymentHandlers.put("paypal", new PayPalPaymentHandler(payOrderController));
    }

    /**
     * Switch to a payment method handler
     */
    private void setActiveHandler(String handlerKey) {
        activeHandler = paymentHandlers.get(handlerKey);

        if (activeHandler != null) {
            // Let handler configure UI
            activeHandler.setupView(qrCodeImageView, qrCodeContainer, loadingIndicator);

            // Update button text
            if (btnConfirmPayment != null) {
                btnConfirmPayment.setText(activeHandler.getConfirmButtonText());
            }

            // Style the selection boxes
            styleSelectedBox(vietqrBox, "vietqr".equals(handlerKey));
            styleSelectedBox(paypalBox, "paypal".equals(handlerKey));

            // Initialize payment display if order data is available
            if (currentOrder != null) {
                String orderIdStr = String.valueOf(currentOrder.getOrderId());
                double totalAmount = currentOrder.getTotalAmount();
                String content = "AIMS " + orderIdStr;
                activeHandler.initializePayment(orderIdStr, totalAmount, content);
            }
        }
    }

    public void initializeData(Order order, Invoice invoice) {
        this.currentOrder = order;
        this.currentInvoice = invoice;

        // Update labels
        if (totalLabel != null)
            totalLabel.setText(String.format("%,.0f VND", order.getTotalAmount()));
        if (transactionIdLabel != null)
            transactionIdLabel.setText("ORDER_" + order.getOrderId());
        if (totalLabelInQR != null)
            totalLabelInQR.setText(String.format("%,.0f VND", order.getTotalAmount()));

        // Initialize the active handler with order data
        if (activeHandler != null) {
            String orderIdStr = String.valueOf(order.getOrderId());
            double totalAmount = order.getTotalAmount();
            String content = "AIMS " + orderIdStr;
            activeHandler.initializePayment(orderIdStr, totalAmount, content);
        }
    }

    // --- Navigation Handlers ---

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/place-order-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnConfirmPayment.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }
            stage.setTitle("AIMS - Place Order");
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot go back: " + e.getMessage()).showAndWait();
        }
    }

    private void navigateToSuccess(TransactionInfo transactionInfo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-success-view.fxml"));
            Parent root = loader.load();

            com.aimsfx.controller.PlaceOrderController.OrderSuccessController controller = loader.getController();
            controller.setSuccessData(currentOrder, currentInvoice, transactionInfo, currentOrder.getDeliveryInfo());

            Stage stage = (Stage) btnConfirmPayment.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }
            stage.setTitle("AIMS - Order Success");

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load success screen: " + e.getMessage()).showAndWait();
        }
    }

    // --- Payment Logic (Strategy Pattern) ---

    /**
     * Handle payment confirmation
     * 
     * OCP COMPLIANT: No if/else branching based on payment method!
     * The active handler (VietQR, PayPal, or any future method) handles the
     * payment.
     */
    @FXML
    void handleConfirmPayment(ActionEvent event) {
        if (currentOrder == null || activeHandler == null)
            return;

        String orderIdStr = String.valueOf(currentOrder.getOrderId());
        double totalAmount = currentOrder.getTotalAmount();
        String content = "AIMS " + orderIdStr;

        // Strategy Pattern: Delegate to active handler
        activeHandler.handlePayment(
                orderIdStr,
                totalAmount,
                content,
                () -> {
                    payOrderController.completePayment(currentOrder, activeHandler.getMethodName().toUpperCase());

                    TransactionInfo info = createTransactionInfo(activeHandler.getMethodName());
                    
                    // Send order confirmation email
                    new Thread(() -> {
                        try {
                            new com.aimsfx.service.EmailService().sendOrderConfirmation(currentOrder, currentOrder.getDeliveryInfo(), info);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                    navigateToSuccess(info);
                },
                this::showLoading,
                this::hideLoading);
    }

    private void styleSelectedBox(VBox box, boolean isSelected) {
        if (box == null)
            return;
        String style = isSelected
                ? "-fx-border-color: #0088ff; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 20; -fx-cursor: hand;"
                : "";
        box.setStyle(style);
    }

    private TransactionInfo createTransactionInfo(String method) {
        TransactionInfo info = new TransactionInfo(
                currentOrder.getOrderId(),
                currentInvoice != null ? currentInvoice.getInvoiceId() : null,
                method,
                java.math.BigDecimal.valueOf(currentOrder.getTotalAmount()),
                "VND");
        info.setTransactionId(method.toUpperCase() + "_" + System.currentTimeMillis());
        info.setStatus("PENDING");
        return info;
    }

    public void showLoading() {
        if (loadingIndicator != null)
            loadingIndicator.setVisible(true);
        if (qrCodeImageView != null)
            qrCodeImageView.setOpacity(0.3);
    }

    public void hideLoading() {
        if (loadingIndicator != null)
            loadingIndicator.setVisible(false);
        if (qrCodeImageView != null)
            qrCodeImageView.setOpacity(1.0);
    }
}