package com.aimsfx.controller.PlaceOrderController;

import com.aimsfx.model.CartManager;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.model.OrderItem;
import com.aimsfx.model.TransactionInfo;
import com.aimsfx.utils.UIUtils;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OrderSuccessController {

    @FXML
    private Label orderIdLabel;
    @FXML
    private Label customerNameLabel;
    @FXML
    private Label phoneNumberLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label provinceLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label transactionIdLabel;
    @FXML
    private Label paymentMethodLabel;
    @FXML
    private Label transactionDateLabel;
    @FXML
    private Label statusLabel;

    private Order currentOrder;
    private Invoice currentInvoice;
    private TransactionInfo transactionInfo;

    public void setSuccessData(Order order, Invoice invoice, TransactionInfo info, DeliveryInfo delivery) {
        this.currentOrder = order;
        this.currentInvoice = invoice;
        this.transactionInfo = info;

        if (orderIdLabel != null && order != null) {
            orderIdLabel.setText("#" + order.getOrderId());
        }
        if (customerNameLabel != null && delivery != null) {
            customerNameLabel.setText(delivery.getRecipientName());
        }
        if (phoneNumberLabel != null && delivery != null) {
            phoneNumberLabel.setText(delivery.getPhoneNumber());
        }
        if (addressLabel != null && delivery != null) {
            addressLabel.setText(delivery.getAddress());
        }
        if (provinceLabel != null && delivery != null) {
            provinceLabel.setText(delivery.getProvince());
        }
        if (totalAmountLabel != null && invoice != null) {
            totalAmountLabel.setText(UIUtils.formatPrice(invoice.getTotalAmount()) + " VND");
        }
        if (transactionIdLabel != null && info != null) {
            transactionIdLabel.setText(info.getTransactionId());
        }
        if (paymentMethodLabel != null && info != null) {
            paymentMethodLabel.setText(info.getPaymentMethod());
        }
        if (transactionDateLabel != null && info != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm:ss");
            transactionDateLabel.setText(info.getCreatedAt().format(formatter));
        }
        if (statusLabel != null && info != null) {
            String status = info.getStatus() != null ? info.getStatus().toString() : "UNKNOWN";
            statusLabel.setText(status);

            if ("PENDING".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            } else if ("PROCESSING".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            } else if ("COMPLETED".equals(status) || "SUCCESS".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        }
    }

    @FXML
    public void backToHomepage() {
        CartManager.getInstance().clearCart();
        Stage stage = (Stage) orderIdLabel.getScene().getWindow();
        UIUtils.navigate(stage, "/com/aimsfx/homepage-view.fxml", "AIMS - Homepage");
    }

    @FXML
    public void viewOrderDetails() {
        if (currentOrder == null || currentInvoice == null || transactionInfo == null) {
            UIUtils.showAlert("No Information", "Order information not found.");
            return;
        }
        showOrderDetailsDialog(currentOrder, currentInvoice, transactionInfo);
    }

    private void showOrderDetailsDialog(Order order, Invoice invoice, TransactionInfo txn) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detail Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");
        mainContainer.setPrefWidth(550);

        mainContainer.getChildren().add(createDialogHeader(order, invoice));
        mainContainer.getChildren().add(createProductsSection(invoice));
        mainContainer.getChildren().add(createPaymentSection(invoice));
        mainContainer.getChildren().add(createTransactionSection(txn));

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f5f5; -fx-border-color: transparent;");
        scrollPane.setPrefHeight(600);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setStyle("-fx-background-color: #f5f5f5;");

        dialog.showAndWait();
    }

    private VBox createDialogHeader(Order order, Invoice invoice) {
        VBox header = new VBox(8);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); -fx-padding: 20; -fx-background-radius: 8 8 0 0;");
        Label titleLabel = new Label("📋 ORDER INFORMATION");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label idLabel = new Label("Order #" + order.getOrderId() + " | Invoice #" + invoice.getInvoiceId());
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");
        header.getChildren().addAll(titleLabel, idLabel);
        return header;
    }

    private VBox createProductsSection(Invoice invoice) {
        VBox section = new VBox(10);
        section.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        Label title = new Label("🛍️ PRODUCTS");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        section.getChildren().addAll(title, new Separator());
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
                section.getChildren().add(row);
            }
        }
        return section;
    }

    private VBox createPaymentSection(Invoice invoice) {
        VBox section = new VBox(8);
        section.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        Label title = new Label("💰 PAYMENT");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        section.getChildren().addAll(title, new Separator());
        section.getChildren().addAll(
                createSummaryRow("Subtotal:", UIUtils.formatPrice(invoice.getSubtotal()) + " ₫", false, null),
                createSummaryRow("VAT (10%):", UIUtils.formatPrice(invoice.getVat()) + " ₫", false, null),
                createSummaryRow("Shipping Fee:", UIUtils.formatPrice(invoice.getDeliveryFee()) + " ₫", false, null));
        if (invoice.getDiscount() > 0) {
            section.getChildren().add(createSummaryRow("Discount:",
                    "-" + UIUtils.formatPrice(invoice.getDiscount()) + " ₫", false, "#27ae60"));
        }
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #667eea; -fx-pref-height: 2;");
        section.getChildren().addAll(sep,
                createSummaryRow("TOTAL:", UIUtils.formatPrice(invoice.getTotalAmount()) + " ₫", true, null));
        return section;
    }

    private VBox createTransactionSection(TransactionInfo txn) {
        VBox section = new VBox(8);
        section.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        Label title = new Label("🔐 TRANSACTION");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        section.getChildren().addAll(title, new Separator());
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm:ss");

        String statusColor = "#666";
        String status = txn.getStatus() != null ? txn.getStatus().toString().toUpperCase() : "UNKNOWN";
        if (status.equals("SUCCESS") || status.equals("COMPLETED"))
            statusColor = "#27ae60";
        else if (status.equals("PENDING"))
            statusColor = "#f39c12";
        else if (status.equals("FAILED"))
            statusColor = "#e74c3c";

        section.getChildren().addAll(
                createInfoRow("Transaction Code:", txn.getTransactionId(), "#667eea"),
                createInfoRow("Payment Method:", txn.getPaymentMethod(), "#667eea"),
                createInfoRow("Status:", status, statusColor),
                createInfoRow("Time:", txn.getCreatedAt().format(formatter), "#667eea"));
        return section;
    }

    private HBox createInfoRow(String label, String value, String valueColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-min-width: 130;");
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 13px; -fx-text-fill: " + valueColor + "; -fx-font-weight: bold;");
        valueNode.setWrapText(true);
        valueNode.setMaxWidth(350);
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private HBox createSummaryRow(String label, String value, boolean isBold, String customColor) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;"
                + (isBold ? " -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #333;" : ""));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label valueNode = new Label(value);
        String color = customColor != null ? customColor : (isBold ? "#667eea" : "#333");
        valueNode.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";"
                + (isBold ? " -fx-font-weight: bold; -fx-font-size: 16px;" : " -fx-font-weight: bold;"));
        valueNode.setAlignment(Pos.CENTER_RIGHT);
        valueNode.setMinWidth(120);
        row.getChildren().addAll(labelNode, spacer, valueNode);
        return row;
    }
}
