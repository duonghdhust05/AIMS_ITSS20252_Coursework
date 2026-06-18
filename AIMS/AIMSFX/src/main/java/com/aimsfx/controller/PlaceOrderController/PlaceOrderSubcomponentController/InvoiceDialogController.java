package com.aimsfx.controller.PlaceOrderController.PlaceOrderSubcomponentController;

import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.model.OrderItem;
import com.aimsfx.model.TransactionInfo;
import com.aimsfx.utils.UIUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class InvoiceDialogController {

    @FXML
    private Label headerIdLabel;
    @FXML
    private VBox productsContainer;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label vatLabel;
    @FXML
    private Label shippingFeeLabel;
    @FXML
    private HBox discountBox;
    @FXML
    private Label discountLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label txnCodeLabel;
    @FXML
    private Label paymentMethodLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label timeLabel;

    @FXML
    private VBox transactionContainer;

    public void setInvoiceData(Order order, Invoice invoice, TransactionInfo txn) {
        String orderIdStr = (order != null) ? String.valueOf(order.getOrderId()) : String.valueOf(invoice.getOrderId());
        headerIdLabel.setText("Order #" + orderIdStr + " | Invoice #" + invoice.getInvoiceId());

        if (invoice.getOrderItems() != null) {
            for (OrderItem item : invoice.getOrderItems()) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label productLabel = new Label(item.getProduct().getTitle());
                productLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                productLabel.setMaxWidth(250);
                productLabel.setWrapText(true);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label qtyLabel = new Label("x" + item.getQuantity());
                qtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-font-weight: bold;");
                Label priceLabel = new Label(UIUtils.formatPrice(item.getLineTotal()) + " ₫");
                priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333; -fx-font-weight: bold;");
                priceLabel.setMinWidth(120);
                priceLabel.setAlignment(Pos.CENTER_RIGHT);
                row.getChildren().addAll(productLabel, spacer, qtyLabel, priceLabel);
                productsContainer.getChildren().add(row);
            }
        }

        subtotalLabel.setText(UIUtils.formatPrice(invoice.getSubtotal()) + " ₫");
        vatLabel.setText(UIUtils.formatPrice(invoice.getVat()) + " ₫");
        shippingFeeLabel.setText(UIUtils.formatPrice(invoice.getDeliveryFee()) + " ₫");

        if (invoice.getDiscount() > 0) {
            discountLabel.setText("-" + UIUtils.formatPrice(invoice.getDiscount()) + " ₫");
        } else {
            discountBox.setVisible(false);
            discountBox.setManaged(false);
        }

        totalLabel.setText(UIUtils.formatPrice(invoice.getTotalAmount()) + " ₫");

        if (txn != null) {
            transactionContainer.setVisible(true);
            transactionContainer.setManaged(true);
            txnCodeLabel.setText(txn.getTransactionId());
            paymentMethodLabel.setText(txn.getPaymentMethod());

            String status = txn.getStatus() != null ? txn.getStatus().toString().toUpperCase() : "UNKNOWN";
            statusLabel.setText(status);
            if (status.equals("SUCCESS") || status.equals("COMPLETED")) {
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else if (status.equals("PENDING")) {
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    
                    .ofPattern("dd/MM/yyyy HH:mm:ss");
            timeLabel.setText(txn.getCreatedAt().format(formatter));
        } else {
            transactionContainer.setVisible(false);
            transactionContainer.setManaged(false);
        }
    }
}
