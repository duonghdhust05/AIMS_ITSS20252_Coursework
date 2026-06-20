package com.aimsfx.view.OrderView;

import com.aimsfx.model.CartManager;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.model.TransactionInfo;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.router.PlaceOrderRouter;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class OrderSuccessScreen {

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
        PlaceOrderRouter.getInstance().showInvoiceDialog(order, invoice, txn);
    }
}
