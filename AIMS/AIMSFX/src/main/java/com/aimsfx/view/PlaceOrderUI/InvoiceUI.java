package com.aimsfx.view.PlaceOrderUI;

import com.aimsfx.model.Invoice;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.BaseView;

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
