package com.aimsfx.view;

import com.aimsfx.model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * PlaceOrderView - View Layer for Place Order Use Case
 * 
 * PURPOSE: Handle all UI rendering, display logic, and visual components
 * 
 * RESPONSIBILITIES (Layer 1 - VIEW):
 * 1. Create UI components (cards, boxes, dialogs)
 * 2. Format data for display (formatPrice)
 * 3. Show alerts and dialogs
 * 4. Bind FXML components
 * 5. Update UI labels and displays
 * 
 * NO business logic, NO validation, NO persistence
 * 
 * SOLID COMPLIANCE:
 * - SRP: Only handles UI rendering
 * - OCP: Can extend for new UI components
 * - DIP: No dependencies on concrete business classes
 */
public class PlaceOrderView {

    // ==================== UI COMPONENT REFERENCES ====================
    private VBox cartItemsContainer;
    private Label subtotalLabel;
    private Label vatLabel;
    private Label totalLabel;
    private Label deliveryFeeLabel;
    
    // Display labels for delivery info
    private Label displayNameLabel;
    private Label displayPhoneLabel;
    private Label displayEmailLabel;
    private Label displayProvinceLabel;
    private Label displaySubDistrictLabel;
    private Label displayAddressLabel;
    private Label displayInstructionsLabel;
    
    // Order success view labels
    private Label orderIdLabel;
    private Label customerNameLabel;
    private Label phoneNumberLabel;
    private Label addressLabel;
    private Label provinceLabel;
    private Label totalAmountLabel;
    private Label transactionIdLabel;
    private Label paymentMethodLabel;
    private Label transactionDateLabel;
    private Label statusLabel;
    
    // Form components
    private GridPane deliveryFormGrid;
    private VBox deliveryInfoDisplay;
    private Button addDeliveryInfoButton;
    private ComboBox<String> provinceComboBox;

    // ==================== CONSTRUCTOR ====================
    
    public PlaceOrderView() {
        // Default constructor
    }

    // ==================== COMPONENT BINDING ====================
    
    /**
     * Bind FXML components to this view
     * Called from Controller after FXML loading
     */
    public void bindFXMLComponents(
            VBox cartItemsContainer,
            Label subtotalLabel,
            Label vatLabel,
            Label totalLabel,
            Label deliveryFeeLabel,
            ComboBox<String> provinceComboBox) {
        
        this.cartItemsContainer = cartItemsContainer;
        this.subtotalLabel = subtotalLabel;
        this.vatLabel = vatLabel;
        this.totalLabel = totalLabel;
        this.deliveryFeeLabel = deliveryFeeLabel;
        this.provinceComboBox = provinceComboBox;
    }

    /**
     * Bind delivery display labels
     */
    public void bindDeliveryDisplayLabels(
            Label displayNameLabel,
            Label displayPhoneLabel,
            Label displayEmailLabel,
            Label displayProvinceLabel,
            Label displaySubDistrictLabel,
            Label displayAddressLabel,
            Label displayInstructionsLabel,
            GridPane deliveryFormGrid,
            VBox deliveryInfoDisplay,
            Button addDeliveryInfoButton) {
        
        this.displayNameLabel = displayNameLabel;
        this.displayPhoneLabel = displayPhoneLabel;
        this.displayEmailLabel = displayEmailLabel;
        this.displayProvinceLabel = displayProvinceLabel;
        this.displaySubDistrictLabel = displaySubDistrictLabel;
        this.displayAddressLabel = displayAddressLabel;
        this.displayInstructionsLabel = displayInstructionsLabel;
        this.deliveryFormGrid = deliveryFormGrid;
        this.deliveryInfoDisplay = deliveryInfoDisplay;
        this.addDeliveryInfoButton = addDeliveryInfoButton;
    }

    /**
     * Bind order success page labels
     */
    public void bindSuccessPageLabels(
            Label orderIdLabel,
            Label customerNameLabel,
            Label phoneNumberLabel,
            Label addressLabel,
            Label provinceLabel,
            Label totalAmountLabel,
            Label transactionIdLabel,
            Label paymentMethodLabel,
            Label transactionDateLabel,
            Label statusLabel) {
        
        this.orderIdLabel = orderIdLabel;
        this.customerNameLabel = customerNameLabel;
        this.phoneNumberLabel = phoneNumberLabel;
        this.addressLabel = addressLabel;
        this.provinceLabel = provinceLabel;
        this.totalAmountLabel = totalAmountLabel;
        this.transactionIdLabel = transactionIdLabel;
        this.paymentMethodLabel = paymentMethodLabel;
        this.transactionDateLabel = transactionDateLabel;
        this.statusLabel = statusLabel;
    }

    // ==================== UI RENDERING METHODS ====================

    /**
     * Create a beautiful product card with image, details, and quantity
     * 
     * @param cartItem CartItem to display
     * @return HBox containing the product card UI
     */
    public HBox createProductCard(CartItem cartItem) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 8;");

        Product product = cartItem.getProduct();

        // Product Icon/Placeholder
        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 40px;");

        // Style icon container with rounded corners and shadow
        StackPane imageContainer = new StackPane(iconLabel);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 6; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);");
        imageContainer.setPrefSize(70, 70);
        imageContainer.setMaxSize(70, 70);

        // Product Details VBox
        VBox details = new VBox(6);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, Priority.ALWAYS);

        // Product Name (null-safe)
        String title = product.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Product #" + product.getProductId();
        }
        Label nameLabel = new Label(title);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(400);

        // Price row
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(formatPrice(product.getCurrentPrice()) + " đ");
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        // Quantity badge
        Label qtyBadge = new Label("× " + cartItem.getQuantity());
        qtyBadge.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 12; " +
                "-fx-font-size: 12px;");

        priceRow.getChildren().addAll(priceLabel, qtyBadge);
        details.getChildren().addAll(nameLabel, priceRow);

        // Subtotal VBox (right side)
        VBox subtotalBox = new VBox(4);
        subtotalBox.setAlignment(Pos.CENTER_RIGHT);
        subtotalBox.setMinWidth(150);

        Label subtotalTitleLabel = new Label("Subtotal");
        subtotalTitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        double subtotal = cartItem.getLineTotal();
        Label subtotalValueLabel = new Label(formatPrice(subtotal) + " đ");
        subtotalValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");

        subtotalBox.getChildren().addAll(subtotalTitleLabel, subtotalValueLabel);

        // Add all components to card
        card.getChildren().addAll(imageContainer, details, subtotalBox);

        return card;
    }

    /**
     * Create UI box for a cart item with quantity controls
     * 
     * @param cartItem CartItem to display
     * @param onQuantityChange Callback when quantity changes
     * @param onRemove Callback when remove button clicked
     * @return HBox containing the cart item UI
     */
    public HBox createCartItemBox(CartItem cartItem, Runnable onQuantityChange, Runnable onRemove) {
        HBox itemBox = new HBox(15);
        itemBox.setStyle(
                "-fx-padding: 15; -fx-background-color: white; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Product product = cartItem.getProduct();

        // Product image placeholder
        Label imageLabel = new Label("📦");
        imageLabel.setStyle(
                "-fx-font-size: 40px; -fx-pref-width: 80; -fx-pref-height: 80; -fx-alignment: center; " +
                "-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // Product info
        VBox infoBox = new VBox(5);
        infoBox.setPrefWidth(300);

        // Null-safe title display
        String title = product.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Product #" + product.getProductId();
        }
        Label nameLabel = new Label(title);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label priceLabel = new Label(formatPrice(product.getCurrentPrice()) + " đ");
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        // Quantity controls
        HBox quantityBox = new HBox(5);
        quantityBox.setAlignment(Pos.CENTER_LEFT);

        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-background-color: #f5f5f5; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 2 8;");
        minusBtn.setOnAction(e -> {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                onQuantityChange.run();
            }
        });

        Label quantityLabel = new Label(String.valueOf(cartItem.getQuantity()));
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 2 10; -fx-text-fill: #333;");

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #f5f5f5; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 2 8;");
        plusBtn.setOnAction(e -> {
            if (cartItem.getQuantity() < product.getStock()) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                onQuantityChange.run();
            } else {
                showAlert("Out of Stock", "Maximum available quantity: " + product.getStock());
            }
        });

        quantityBox.getChildren().addAll(minusBtn, quantityLabel, plusBtn);
        infoBox.getChildren().addAll(nameLabel, priceLabel, quantityBox);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Total price for this item
        double itemTotal = cartItem.getLineTotal();
        Label totalPriceLabel = new Label(formatPrice(itemTotal) + " đ");
        totalPriceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Remove button
        Button removeBtn = new Button("✕");
        removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 18px; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> onRemove.run());

        itemBox.getChildren().addAll(imageLabel, infoBox, spacer, totalPriceLabel, removeBtn);

        return itemBox;
    }

    /**
     * Load cart items into the container
     * 
     * @param items List of cart items in cart
     * @param onQuantityChange Callback when any quantity changes
     */
    public void loadCartItems(List<CartItem> items, Runnable onQuantityChange) {
        if (cartItemsContainer == null) return;
        
        cartItemsContainer.getChildren().clear();

        if (items == null || items.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 40;");
            cartItemsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (CartItem cartItem : items) {
            HBox productCard = createProductCard(cartItem);
            cartItemsContainer.getChildren().add(productCard);
        }
    }

    /**
     * Update totals display
     * 
     * @param subtotal Subtotal amount
     * @param vat VAT amount
     * @param total Total amount
     */
    public void updateTotals(double subtotal, double vat, double total) {
        if (subtotalLabel != null) {
            subtotalLabel.setText(formatPrice(subtotal) + " VND");
        }
        if (vatLabel != null) {
            vatLabel.setText(formatPrice(vat) + " VND");
        }
        if (totalLabel != null) {
            totalLabel.setText(formatPrice(total) + " VND");
        }
    }

    /**
     * Update delivery fee display
     * 
     * @param fee Delivery fee amount
     */
    public void updateDeliveryFee(double fee) {
        if (deliveryFeeLabel != null) {
            deliveryFeeLabel.setText(formatPrice(fee) + " VND");
        }
    }

    /**
     * Update delivery info display labels
     */
    public void updateDeliveryInfoDisplay(DeliveryInfo info) {
        if (info == null) return;

        if (displayNameLabel != null) {
            displayNameLabel.setText(info.getRecipientName());
        }
        if (displayPhoneLabel != null) {
            displayPhoneLabel.setText(info.getPhoneNumber());
        }
        if (displayEmailLabel != null) {
            displayEmailLabel.setText(
                info.getEmail() == null || info.getEmail().trim().isEmpty() ? "-" : info.getEmail());
        }
        if (displayProvinceLabel != null) {
            displayProvinceLabel.setText(info.getProvince());
        }
        if (displaySubDistrictLabel != null) {
            displaySubDistrictLabel.setText(
                info.getWard() == null || info.getWard().trim().isEmpty() ? "-" : info.getWard());
        }
        if (displayAddressLabel != null) {
            displayAddressLabel.setText(info.getAddress());
        }
        if (displayInstructionsLabel != null) {
            displayInstructionsLabel.setText(
                info.getDeliveryInstructions() == null || info.getDeliveryInstructions().trim().isEmpty() 
                    ? "-" : info.getDeliveryInstructions());
        }

        // Switch to display mode
        if (deliveryInfoDisplay != null) {
            deliveryInfoDisplay.setVisible(true);
            deliveryInfoDisplay.setManaged(true);
        }
        if (deliveryFormGrid != null) {
            deliveryFormGrid.setVisible(false);
            deliveryFormGrid.setManaged(false);
        }
        if (addDeliveryInfoButton != null) {
            addDeliveryInfoButton.setText("✏️ Edit Delivery Information");
        }
    }

    /**
     * Populate order success page with data
     */
    public void populateSuccessPage(Order order, Invoice invoice, TransactionInfo txn, DeliveryInfo delivery) {
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
            totalAmountLabel.setText(formatPrice(invoice.getTotalAmount()) + " VND");
        }

        if (transactionIdLabel != null && txn != null) {
            transactionIdLabel.setText(txn.getTransactionId());
        }

        if (paymentMethodLabel != null && txn != null) {
            paymentMethodLabel.setText(txn.getPaymentMethod());
        }

        if (transactionDateLabel != null && txn != null) {
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            transactionDateLabel.setText(txn.getCreatedAt().format(formatter));
        }

        if (statusLabel != null && txn != null) {
            String status = txn.getStatus() != null ? txn.getStatus().toString() : "UNKNOWN";
            statusLabel.setText(status);

            // Style status based on type
            if ("PENDING".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            } else if ("PROCESSING".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            } else if ("COMPLETED".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            }
        }
    }

    // ==================== DIALOG METHODS ====================

    /**
     * Show alert dialog
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error alert
     */
    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning alert
     */
    public void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Non-blocking
    }

    /**
     * Show confirmation dialog
     * 
     * @return true if user confirmed
     */
    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ==================== ORDER DETAILS DIALOG ====================

    /**
     * Show order details dialog
     */
    public void showOrderDetailsDialog(Order order, Invoice invoice, TransactionInfo txn) {
        if (order == null || invoice == null || txn == null) {
            showAlert("No Information", "Order information not found.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");
        mainContainer.setPrefWidth(550);

        // Header
        VBox header = createDialogHeader(order, invoice);
        mainContainer.getChildren().add(header);

        // Products Section
        VBox productsSection = createProductsSection(invoice);
        mainContainer.getChildren().add(productsSection);

        // Payment Section
        VBox paymentSection = createPaymentSection(invoice);
        mainContainer.getChildren().add(paymentSection);

        // Transaction Section
        VBox transactionSection = createTransactionSection(txn);
        mainContainer.getChildren().add(transactionSection);

        // Wrap in ScrollPane
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
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                "-fx-padding: 20; -fx-background-radius: 8 8 0 0;");
        
        Label titleLabel = new Label("📋 CHI TIẾT ĐƠN HÀNG");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label orderIdLabel = new Label(
                "Đơn hàng #" + order.getOrderId() + " | Hóa đơn #" + invoice.getInvoiceId());
        orderIdLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        header.getChildren().addAll(titleLabel, orderIdLabel);
        return header;
    }

    private VBox createProductsSection(Invoice invoice) {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label title = new Label("🛍️ SẢN PHẨM");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        section.getChildren().add(title);

        section.getChildren().add(new Separator());

        if (invoice.getOrderItems() != null) {
            for (OrderItem item : invoice.getOrderItems()) {
                HBox itemRow = createProductRow(item);
                section.getChildren().add(itemRow);
            }
        }

        return section;
    }

    private HBox createProductRow(OrderItem item) {
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

        Label priceLabel = new Label(String.format("%,.0f ₫", (double) item.getLineTotal()));
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333; -fx-font-weight: bold;");
        priceLabel.setMinWidth(120);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(productLabel, spacer, qtyLabel, priceLabel);
        return row;
    }

    private VBox createPaymentSection(Invoice invoice) {
        VBox section = new VBox(8);
        section.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label title = new Label("💰 THANH TOÁN");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        section.getChildren().add(title);

        section.getChildren().add(new Separator());

        section.getChildren().addAll(
                createSummaryRow("Tạm tính:", String.format("%,.0f ₫", (double) invoice.getSubtotal()), false),
                createSummaryRow("VAT (10%):", String.format("%,.0f ₫", (double) invoice.getVat()), false),
                createSummaryRow("Phí vận chuyển:", String.format("%,.0f ₫", (double) invoice.getDeliveryFee()), false));

        if (invoice.getDiscount() > 0) {
            section.getChildren().add(
                    createSummaryRow("Giảm giá:", String.format("-%,.0f ₫", (double) invoice.getDiscount()), 
                            false, "#27ae60"));
        }

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #667eea; -fx-pref-height: 2;");
        section.getChildren().add(sep);

        section.getChildren().add(
                createSummaryRow("TỔNG CỘNG:", String.format("%,.0f ₫", (double) invoice.getTotalAmount()), true));

        return section;
    }

    private VBox createTransactionSection(TransactionInfo txn) {
        VBox section = new VBox(8);
        section.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label title = new Label("🔐 GIAO DỊCH");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        section.getChildren().add(title);

        section.getChildren().add(new Separator());

        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        section.getChildren().addAll(
                createInfoRow("Mã giao dịch:", txn.getTransactionId()),
                createInfoRow("Phương thức:", txn.getPaymentMethod()),
                createInfoRow("Trạng thái:", txn.getStatus().toString().toUpperCase(), 
                        getStatusColor(txn.getStatus().toString())),
                createInfoRow("Thời gian:", txn.getCreatedAt().format(formatter)));

        return section;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Format price with thousand separators (Vietnamese locale)
     */
    public String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.of("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price);
    }

    private String getStatusColor(String status) {
        if (status == null) return "#666";
        switch (status.toUpperCase()) {
            case "SUCCESS":
            case "COMPLETED":
                return "#27ae60";
            case "PENDING":
                return "#f39c12";
            case "FAILED":
                return "#e74c3c";
            default:
                return "#666";
        }
    }

    private HBox createInfoRow(String label, String value) {
        return createInfoRow(label, value, "#667eea");
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

    private HBox createSummaryRow(String label, String value, boolean isBold) {
        return createSummaryRow(label, value, isBold, null);
    }

    private HBox createSummaryRow(String label, String value, boolean isBold, String customColor) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        String labelStyle = "-fx-font-size: 13px; -fx-text-fill: #666;";
        if (isBold) {
            labelStyle += " -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #333;";
        }
        labelNode.setStyle(labelStyle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueNode = new Label(value);
        String color = customColor != null ? customColor : (isBold ? "#667eea" : "#333");
        String valueStyle = "-fx-font-size: 13px; -fx-text-fill: " + color + ";";
        if (isBold) {
            valueStyle += " -fx-font-weight: bold; -fx-font-size: 16px;";
        } else {
            valueStyle += " -fx-font-weight: bold;";
        }
        valueNode.setStyle(valueStyle);
        valueNode.setAlignment(Pos.CENTER_RIGHT);
        valueNode.setMinWidth(120);

        row.getChildren().addAll(labelNode, spacer, valueNode);
        return row;
    }

    // ==================== GETTERS ====================

    public ComboBox<String> getProvinceComboBox() {
        return provinceComboBox;
    }
    
    // ==================== ADDITIONAL UI METHODS ====================
    
    /**
     * Show empty cart message in cart container
     * @param container The container to add message to
     */
    public void showEmptyCartMessage(VBox container) {
        if (container == null) return;
        
        container.getChildren().clear();
        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 40;");
        container.getChildren().add(emptyLabel);
    }

    // ==================== DELIVERY INFO UI METHODS ====================

    /**
     * Restore delivery info to form fields
     * @param deliveryInfo The delivery info to restore
     * @param nameField Name text field
     * @param phoneField Phone text field
     * @param emailField Email text field
     * @param provinceComboBox Province combo box
     * @param subDistrictField Sub-district text field
     * @param addressArea Address text area
     * @param deliveryInstructionsArea Delivery instructions text area
     */
    public void restoreDeliveryInfoToForm(
            DeliveryInfo deliveryInfo,
            TextField nameField,
            TextField phoneField,
            TextField emailField,
            ComboBox<String> provinceComboBox,
            TextField subDistrictField,
            TextArea addressArea,
            TextArea deliveryInstructionsArea) {
        
        if (deliveryInfo == null) return;

        // Restore basic fields
        if (deliveryInfo.getRecipientName() != null && nameField != null) {
            nameField.setText(deliveryInfo.getRecipientName());
        }
        if (deliveryInfo.getPhoneNumber() != null && phoneField != null) {
            phoneField.setText(deliveryInfo.getPhoneNumber());
        }
        if (deliveryInfo.getEmail() != null && emailField != null) {
            emailField.setText(deliveryInfo.getEmail());
        }
        if (deliveryInfo.getProvince() != null && provinceComboBox != null) {
            provinceComboBox.setValue(deliveryInfo.getProvince());
        }
        if (deliveryInfo.getWard() != null && subDistrictField != null) {
            subDistrictField.setText(deliveryInfo.getWard());
        }
        if (deliveryInfo.getAddress() != null && addressArea != null) {
            addressArea.setText(deliveryInfo.getAddress());
        }
        if (deliveryInfo.getDeliveryInstructions() != null && deliveryInstructionsArea != null) {
            deliveryInstructionsArea.setText(deliveryInfo.getDeliveryInstructions());
        }
    }

    /**
     * Update delivery display labels and switch to display mode
     * @param deliveryInfo The delivery info to display
     */
    public void updateDeliveryDisplayAndSwitchMode(DeliveryInfo deliveryInfo) {
        if (deliveryInfo == null) return;
        
        // Update display labels
        if (displayNameLabel != null) {
            displayNameLabel.setText(deliveryInfo.getRecipientName() != null ? deliveryInfo.getRecipientName() : "-");
        }
        if (displayPhoneLabel != null) {
            displayPhoneLabel.setText(deliveryInfo.getPhoneNumber() != null ? deliveryInfo.getPhoneNumber() : "-");
        }
        if (displayEmailLabel != null) {
            displayEmailLabel.setText(deliveryInfo.getEmail() != null && !deliveryInfo.getEmail().trim().isEmpty() 
                    ? deliveryInfo.getEmail() : "-");
        }
        if (displayProvinceLabel != null) {
            displayProvinceLabel.setText(deliveryInfo.getProvince() != null ? deliveryInfo.getProvince() : "-");
        }
        if (displaySubDistrictLabel != null) {
            displaySubDistrictLabel.setText(deliveryInfo.getWard() != null && !deliveryInfo.getWard().trim().isEmpty() 
                    ? deliveryInfo.getWard() : "-");
        }
        if (displayAddressLabel != null) {
            displayAddressLabel.setText(deliveryInfo.getAddress() != null ? deliveryInfo.getAddress() : "-");
        }
        if (displayInstructionsLabel != null) {
            displayInstructionsLabel.setText(deliveryInfo.getDeliveryInstructions() != null 
                    && !deliveryInfo.getDeliveryInstructions().trim().isEmpty() 
                    ? deliveryInfo.getDeliveryInstructions() : "-");
        }

        // Switch to display mode, hide form
        if (deliveryInfoDisplay != null) {
            deliveryInfoDisplay.setVisible(true);
            deliveryInfoDisplay.setManaged(true);
        }
        if (deliveryFormGrid != null) {
            deliveryFormGrid.setVisible(false);
            deliveryFormGrid.setManaged(false);
        }
        if (addDeliveryInfoButton != null) {
            addDeliveryInfoButton.setText("✏️ Edit Delivery Information");
        }
    }

    /**
     * Initialize province combo box with Vietnamese provinces
     */
    public void initializeProvinceComboBox() {
        if (provinceComboBox == null) return;
        
        provinceComboBox.getItems().addAll(
                "Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng",
                "Cần Thơ", "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang",
                "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định");
    }

    // ==================== SUCCESS PAGE UI (LAYER 1) ====================

    /**
     * Populate success page with order details
     * @param order Order information
     * @param invoice Invoice information
     * @param txn Transaction information
     * @param delivery Delivery information
     * @param labels Label references from Controller
     */
    public void populateSuccessPage(
            com.aimsfx.model.Order order, 
            com.aimsfx.model.Invoice invoice, 
            com.aimsfx.model.TransactionInfo txn, 
            com.aimsfx.model.DeliveryInfo delivery,
            Label orderIdLabel, Label customerNameLabel, Label phoneNumberLabel,
            Label addressLabel, Label provinceLabel, Label totalAmountLabel,
            Label transactionIdLabel, Label paymentMethodLabel, Label transactionDateLabel,
            Label statusLabel) {
        
        if (orderIdLabel != null) {
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
            totalAmountLabel.setText(formatPrice(invoice.getTotalAmount()) + " VND");
        }

        if (transactionIdLabel != null && txn != null) {
            transactionIdLabel.setText(txn.getTransactionId());
        }

        if (paymentMethodLabel != null && txn != null) {
            paymentMethodLabel.setText(txn.getPaymentMethod());
        }

        if (transactionDateLabel != null && txn != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm:ss");
            transactionDateLabel.setText(txn.getCreatedAt().format(formatter));
        }

        if (statusLabel != null && txn != null) {
            String status = txn.getStatus() != null ? txn.getStatus().toString() : "UNKNOWN";
            statusLabel.setText(status);

            // Style status based on type
            if ("PENDING".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            } else if ("PROCESSING".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            } else if ("COMPLETED".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            }
        }
    }

    // ==================== TOTAL LABELS UPDATE (LAYER 1) ====================

    /**
     * Update total labels with calculated values
     * @param subtotalLabel Subtotal label reference
     * @param vatLabel VAT label reference
     * @param totalLabel Total label reference
     * @param subtotal Subtotal value
     * @param vat VAT value
     * @param total Total value
     * @param currency Currency suffix
     */
    public void updateTotalLabels(Label subtotalLabel, Label vatLabel, Label totalLabel,
                                   double subtotal, double vat, double total, String currency) {
        if (subtotalLabel != null) {
            subtotalLabel.setText(formatPrice(subtotal) + " " + currency);
        }
        if (vatLabel != null) {
            vatLabel.setText(formatPrice(vat) + " " + currency);
        }
        if (totalLabel != null) {
            totalLabel.setText(formatPrice(total) + " " + currency);
        }
    }

    // ==================== MESSAGE BUILDING (LAYER 1 - UI STRINGS) ====================

    /**
     * Build delivery fee calculation message for display
     * @param province Selected province
     * @param totalWeight Total weight in kg
     * @param originalFee Original delivery fee
     * @param discount Discount amount
     * @param finalFee Final delivery fee after discount
     * @return Formatted message string for display
     */
    public String buildDeliveryFeeMessage(String province, float totalWeight,
                                           float originalFee, float discount, float finalFee) {
        if (discount > 0) {
            return String.format(
                    "✓ Delivery fee calculated successfully!\n\n" +
                            "Location: %s\n" +
                            "Total weight: %.2f kg\n" +
                            "Original fee: %s VND\n\n" +
                            "🎉 FREE SHIPPING!\n" +
                            "Order > 100,000 VND\n" +
                            "Discount: %s VND\n" +
                            "Final fee: %s VND",
                    province, totalWeight,
                    formatPrice(originalFee),
                    formatPrice(discount),
                    formatPrice(finalFee));
        } else {
            return String.format(
                    "✓ Delivery fee calculated successfully!\n\n" +
                            "Location: %s\n" +
                            "Total weight: %.2f kg\n" +
                            "Delivery fee: %s VND",
                    province, totalWeight, formatPrice(finalFee));
        }
    }

    // ==================== DIALOG DISPLAY (LAYER 1 - UI) ====================

    /**
     * Show delivery information dialog and return user input
     * LAYER 1 responsibility: UI dialog display and user interaction
     * 
     * @param ownerWindow The owner window for modal dialog
     * @param totalWeight Total order weight for fee calculation
     * @param totalAmount Total order amount
     * @param existingName Existing name if editing
     * @param existingPhone Existing phone if editing
     * @param existingEmail Existing email if editing
     * @param existingProvince Existing province if editing
     * @param existingSubDistrict Existing sub-district if editing
     * @param existingAddress Existing address if editing
     * @param existingInstructions Existing instructions if editing
     * @return Map with dialog results or null if cancelled
     */
    public java.util.Map<String, String> showDeliveryInfoDialog(
            javafx.stage.Window ownerWindow,
            double totalWeight, double totalAmount,
            String existingName, String existingPhone, String existingEmail,
            String existingProvince, String existingSubDistrict,
            String existingAddress, String existingInstructions) {
        
        try {
            // Load the dialog FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/aimsfx/delivery-info-dialog.fxml"));
            javafx.scene.layout.VBox dialogRoot = loader.load();

            // Get the controller
            com.aimsfx.controller.DeliveryInfoDialogController controller = loader.getController();

            // Set order data for fee calculation
            controller.setOrderData(totalWeight, totalAmount);

            // If editing, set existing data
            if (existingName != null && !existingName.trim().isEmpty()) {
                controller.setExistingData(
                        existingName, existingPhone, existingEmail,
                        existingProvince, existingSubDistrict,
                        existingAddress, existingInstructions);
            }

            // Create dialog stage
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(ownerWindow);
            dialogStage.setTitle("Delivery Information");
            javafx.scene.Scene dialogScene = new javafx.scene.Scene(dialogRoot);
            dialogStage.setScene(dialogScene);
            dialogStage.setResizable(false);

            // Show dialog and wait for user action
            dialogStage.showAndWait();

            // If user saved data, return results
            if (controller.isSaved()) {
                java.util.Map<String, String> result = new java.util.HashMap<>();
                result.put("name", controller.getName());
                result.put("phone", controller.getPhone());
                result.put("email", controller.getEmail());
                result.put("province", controller.getProvince());
                result.put("subDistrict", controller.getSubDistrict());
                result.put("address", controller.getAddress());
                result.put("instructions", controller.getDeliveryInstructions());
                result.put("deliveryFee", controller.getDeliveryFee());
                return result;
            }
            
            return null; // User cancelled

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Cannot display delivery information form: " + ex.getMessage());
            return null;
        }
    }

    // ==================== NAVIGATION METHODS ====================
    // View handles navigation to other views (View creates View - acceptable pattern)

    /**
     * Navigate to Payment screen
     * 
     * PURPOSE: Controller delegates navigation to View (correct layering)
     * PATTERN: View creates View - acceptable, avoids Controller creating UI
     * 
     * @param order Current order to pay
     * @param invoice Current invoice with amounts
     * @param payOrderController Controller for payment processing
     * @param currentStage Stage to switch scene
     */
    public void navigateToPaymentScreen(Order order, Invoice invoice,
                                         com.aimsfx.controller.PayOrderController payOrderController,
                                         javafx.stage.Stage currentStage) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/payment-view.fxml"));
            
            // View creates PaymentUI (View creates View - acceptable)
            loader.setControllerFactory(c -> new PaymentUI(payOrderController));
            
            javafx.scene.Parent root = loader.load();
            
            // Pass data to Payment screen
            PaymentUI paymentUI = loader.getController();
            paymentUI.initializeData(order, invoice);
            
            // Switch scene
            currentStage.setScene(new javafx.scene.Scene(root));
            currentStage.setTitle("AIMS - Payment");
            currentStage.centerOnScreen();
            
            System.out.println("✅ Navigated to Payment screen for Order #" + order.getOrderId());
            
        } catch (java.io.IOException e) {
            showAlert("Error", "Could not load Payment Screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to Order Success screen
     * 
     * @param order Completed order
     * @param invoice Invoice with payment details
     * @param transaction Transaction info
     * @param delivery Delivery information
     * @param currentStage Stage to switch scene
     */
    public void navigateToSuccessScreen(Order order, Invoice invoice,
                                         TransactionInfo transaction, DeliveryInfo delivery,
                                         javafx.stage.Stage currentStage) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/order-success-view.fxml"));
            javafx.scene.Parent successView = loader.load();
            
            // Get controller and pass data
            com.aimsfx.controller.PlaceOrderController controller = loader.getController();
            controller.setSuccessData(order, invoice, transaction, delivery);
            
            // Switch scene
            currentStage.setScene(new javafx.scene.Scene(successView));
            currentStage.setTitle("AIMS - Order Success");
            
            System.out.println("✅ Navigated to Success screen for Order #" + order.getOrderId());
            
        } catch (java.io.IOException e) {
            showAlert("Error", "Could not load success page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate back to Cart view
     * 
     * @param currentStage Stage to switch scene
     */
    public void navigateToCartScreen(javafx.stage.Stage currentStage) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/cart-view.fxml"));
            javafx.scene.Parent cartView = loader.load();
            
            currentStage.setScene(new javafx.scene.Scene(cartView));
            currentStage.setTitle("AIMS - Shopping Cart");
            
        } catch (java.io.IOException e) {
            showAlert("Error", "Could not load cart view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to Homepage
     * 
     * @param currentStage Stage to switch scene
     */
    public void navigateToHomepage(javafx.stage.Stage currentStage) {
        try {
            // Clear cart on successful order completion
            CartManager.getInstance().clearCart();
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/homepage-view.fxml"));
            javafx.scene.Parent homepageView = loader.load();
            
            currentStage.setScene(new javafx.scene.Scene(homepageView));
            currentStage.setTitle("AIMS - Homepage");
            
        } catch (java.io.IOException e) {
            showAlert("Error", "Could not load homepage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
