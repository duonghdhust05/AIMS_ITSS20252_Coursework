package com.aimsfx.view;

import com.aimsfx.model.Invoice;
import com.aimsfx.model.OrderItem;
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
     * @param invoice Invoice to display
     */
    public void updateInvoiceDisplay(Invoice invoice) {
        if (invoice == null) return;
        
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
     * Display invoice details with beautiful formatting
     * @param invoice Invoice to display
     */
    public void displayInvoice(Invoice invoice) {
        if (invoice == null) {
            displayError("No invoice available");
            return;
        }
        
        // Create custom dialog
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Hóa Đơn Tạm Tính");
        dialog.setHeaderText(null);
        
        // Main container
        javafx.scene.layout.VBox mainContainer = new javafx.scene.layout.VBox(15);
        mainContainer.setStyle("-fx-padding: 20; -fx-background-color: #f8f9fa;");
        
        // Header
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(5);
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                       "-fx-padding: 20; -fx-background-radius: 10;");
        
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("🧾 SUBTOTAL INVOICE");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        javafx.scene.control.Label subtitleLabel = new javafx.scene.control.Label("Please review your order details before proceeding to payment");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-opacity: 0.9;");
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        
        // Invoice Info Section
        javafx.scene.layout.VBox infoSection = new javafx.scene.layout.VBox(8);
        infoSection.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateStr = java.time.LocalDateTime.now().format(formatter);
        
        infoSection.getChildren().addAll(
        		createInfoRow("Invoice ID:", "#" + invoice.getInvoiceId(), "#667eea"),
                createInfoRow("Order ID:", "#" + invoice.getOrderId(), "#667eea"),
                createInfoRow("Created At:", dateStr, "#666")
        );
        
        // Items Section
        javafx.scene.layout.VBox itemsSection = new javafx.scene.layout.VBox(5);
        itemsSection.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        javafx.scene.control.Label itemsTitle = new javafx.scene.control.Label("📦 SẢN PHẨM");
        itemsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        itemsSection.getChildren().add(itemsTitle);
        
        javafx.scene.control.Separator sep1 = new javafx.scene.control.Separator();
        sep1.setStyle("-fx-background-color: #e0e0e0;");
        itemsSection.getChildren().add(sep1);
        
        if (invoice.getOrderItems() != null && !invoice.getOrderItems().isEmpty()) {
            for (OrderItem item : invoice.getOrderItems()) {
                javafx.scene.layout.HBox itemRow = new javafx.scene.layout.HBox(10);
                itemRow.setStyle("-fx-padding: 8 0;");
                
                javafx.scene.control.Label productLabel = new javafx.scene.control.Label(item.getProduct().getTitle());
                productLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                productLabel.setMaxWidth(300);
                productLabel.setWrapText(true);
                
                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                
                javafx.scene.control.Label qtyLabel = new javafx.scene.control.Label("x" + item.getQuantity());
                qtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-font-weight: bold;");
                
                javafx.scene.control.Label priceLabel = new javafx.scene.control.Label(
                    String.format("%,.0f đ", item.getPrice() * item.getQuantity()));
                priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333; -fx-font-weight: bold;");
                priceLabel.setMinWidth(120);
                priceLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                
                itemRow.getChildren().addAll(productLabel, spacer, qtyLabel, priceLabel);
                itemsSection.getChildren().add(itemRow);
            }
        }
        
        // Financial Summary Section
        javafx.scene.layout.VBox summarySection = new javafx.scene.layout.VBox(8);
        summarySection.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        javafx.scene.control.Label summaryTitle = new javafx.scene.control.Label("💰 THANH TOÁN");
        summaryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        summarySection.getChildren().add(summaryTitle);
        
        javafx.scene.control.Separator sep2 = new javafx.scene.control.Separator();
        sep2.setStyle("-fx-background-color: #e0e0e0;");
        summarySection.getChildren().add(sep2);
        
        summarySection.getChildren().addAll(
            createSummaryRow("Subtotal:", String.format("%,.0f đ", invoice.getSubtotal()), false),
            createSummaryRow("VAT (10%):", String.format("%,.0f đ", invoice.getVat()), false),
            createSummaryRow("Delivery Fee:", String.format("%,.0f đ", invoice.getDeliveryFee()), false)
        );
        
        if (invoice.getDiscount() > 0) {
            summarySection.getChildren().add(
                createSummaryRow("Discount:", String.format("-%,.0f đ", invoice.getDiscount()), false, "#27ae60")
            );
        }
        
        javafx.scene.control.Separator sep3 = new javafx.scene.control.Separator();
        sep3.setStyle("-fx-background-color: #667eea; -fx-pref-height: 2;");
        summarySection.getChildren().add(sep3);
        
        summarySection.getChildren().add(
            createSummaryRow("TỔNG CỘNG:", String.format("%,.0f đ", invoice.getTotalAmount()), true)
        );
        
        // Footer
        javafx.scene.layout.VBox footer = new javafx.scene.layout.VBox(5);
        footer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 15; -fx-background-radius: 8; -fx-alignment: center;");
        
        javafx.scene.control.Label footerLabel = new javafx.scene.control.Label("✨ Thanks for shopping with us! Please proceed to payment to complete your order.");
        footerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-font-style: italic;");
        footer.getChildren().add(footerLabel);
        
        // Add all sections
        mainContainer.getChildren().addAll(header, infoSection, itemsSection, summarySection, footer);
        
        // Set content
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(700);
        
        // Add OK button
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);
        
        // Style the OK button
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; " +
                         "-fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 5;");
        
        dialog.showAndWait();
    }
    
    /**
     * Create an info row with label and value
     */
    private javafx.scene.layout.HBox createInfoRow(String label, String value, String valueColor) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        javafx.scene.control.Label labelText = new javafx.scene.control.Label(label);
        labelText.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-min-width: 120;");
        
        javafx.scene.control.Label valueText = new javafx.scene.control.Label(value);
        valueText.setStyle("-fx-font-size: 13px; -fx-text-fill: " + valueColor + "; -fx-font-weight: bold;");
        
        row.getChildren().addAll(labelText, valueText);
        return row;
    }
    
    /**
     * Create a summary row for financial details
     */
    private javafx.scene.layout.HBox createSummaryRow(String label, String value, boolean isTotal) {
        return createSummaryRow(label, value, isTotal, null);
    }
    
    private javafx.scene.layout.HBox createSummaryRow(String label, String value, boolean isTotal, String customColor) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox();
        row.setStyle("-fx-padding: 5 0;");
        
        javafx.scene.control.Label labelText = new javafx.scene.control.Label(label);
        String labelStyle = isTotal 
            ? "-fx-font-size: 16px; -fx-text-fill: #667eea; -fx-font-weight: bold;"
            : "-fx-font-size: 13px; -fx-text-fill: #666;";
        labelText.setStyle(labelStyle);
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        javafx.scene.control.Label valueText = new javafx.scene.control.Label(value);
        String valueColor = customColor != null ? customColor : (isTotal ? "#667eea" : "#333");
        String valueStyle = isTotal
            ? "-fx-font-size: 18px; -fx-text-fill: " + valueColor + "; -fx-font-weight: bold;"
            : "-fx-font-size: 13px; -fx-text-fill: " + valueColor + "; -fx-font-weight: bold;";
        valueText.setStyle(valueStyle);
        valueText.setMinWidth(120);
        valueText.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        row.getChildren().addAll(labelText, spacer, valueText);
        return row;
    }
    
    /**
     * Request invoice creation/update
     * @param invoice Invoice to request
     * @return Updated invoice
     */
    public Invoice requestInvoice(Invoice invoice) {
        displayInfo("Invoice requested");
        return invoice;
    }
}
