package com.aimsfx.view;

import com.aimsfx.model.Invoice;
import com.aimsfx.utils.UIUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * View for displaying invoice information
 * Contains UI fields for invoice display
 */
public class InvoiceUI extends BaseView {

    // FXML UI Components - These belong in View layer
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label vatLabel;
    @FXML
    private Label deliveryFeeLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label invoiceIdLabel;
    @FXML
    private Label orderIdLabel;

    /**
     * Update invoice display on UI labels
     * 
     * @param invoice Invoice to display
     */
    public void updateInvoiceDisplay(Invoice invoice) {
        if (invoice == null)
            return;

        if (invoiceIdLabel != null) {
            invoiceIdLabel.setText("Invoice #" + invoice.getInvoiceId());
        }
        if (orderIdLabel != null) {
            orderIdLabel.setText("Order #" + invoice.getOrderId());
        }
        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("%.2f VND", invoice.getSubtotal()));
        }
        if (vatLabel != null) {
            vatLabel.setText(String.format("%.2f VND", invoice.getVat()));
        }
        if (deliveryFeeLabel != null) {
            deliveryFeeLabel.setText(String.format("%.2f VND", invoice.getDeliveryFee()));
        }
        if (totalLabel != null) {
            totalLabel.setText(String.format("%.2f VND", invoice.getTotalAmount()));
        }
    }

    public void displayInvoice(Invoice invoice) {
        if (invoice == null)
            return;

        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Invoice Details");
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);
        UIUtils.applyAppDialogIcon(dialog);

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/invoice-dialog.fxml"));
            javafx.scene.control.ScrollPane scrollPane = loader.load();
            com.aimsfx.view.InvoiceDialogUI controller = loader
                    .getController();

            // Pass null for order (it will use invoice.getOrderId()) and null for
            // transaction
            controller.setInvoiceData(null, invoice, null);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().setStyle("-fx-background-color: #f5f5f5;");

            // Add OK button styling
            javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
            if (okButton instanceof javafx.scene.control.Button) {
                okButton.setStyle(
                        "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
            }

            dialog.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            displayError("Could not load invoice view.");
        }
    }

    /**
     * Request invoice creation/update
     * 
     * @param invoice Invoice to request
     * @return Updated invoice
     */
    public Invoice requestInvoice(Invoice invoice) {
        displayInfo("Invoice requested");
        return invoice;
    }
}
