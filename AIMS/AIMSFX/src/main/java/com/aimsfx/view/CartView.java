package com.aimsfx.view;

import com.aimsfx.model.CartItem;
import com.aimsfx.model.Product;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * CartView - View Layer for Cart Use Case
 */
public class CartView {

    @SuppressWarnings("deprecation")
	private static final Locale VN_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance(VN_LOCALE);

    public CartView() {
    }

    public String formatPrice(double price) {
        return PRICE_FORMATTER.format(price);
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public void showEmptyCartMessage(VBox container) {
        if (container == null) return;
        
        container.getChildren().clear();
        
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));
        
        Label emptyIcon = new Label("🛒");
        emptyIcon.setStyle("-fx-font-size: 48px;");
        
        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
        
        Label subLabel = new Label("Add some products to get started!");
        subLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
        
        emptyBox.getChildren().addAll(emptyIcon, emptyLabel, subLabel);
        container.getChildren().add(emptyBox);
    }

    public HBox createCartItemBox(CartItem cartItem, Runnable onQuantityChange, Runnable onRemove) {
        HBox itemBox = new HBox(15);
        itemBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");

        Product product = cartItem.getProduct();

        Label imageLabel = new Label("📦");
        imageLabel.setStyle("-fx-font-size: 40px; -fx-pref-width: 80; -fx-pref-height: 80; -fx-alignment: center; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        VBox infoBox = new VBox(5);
        infoBox.setPrefWidth(300);

        String title = product.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Product #" + product.getProductId();
        }
        Label nameLabel = new Label(title);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label priceLabel = new Label(formatPrice(product.getCurrentPrice()) + " đ");
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label stockLabel = new Label("Available: " + product.getStock());
        stockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

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
        infoBox.getChildren().addAll(nameLabel, priceLabel, stockLabel, quantityBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightBox = new VBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        double subtotal = cartItem.getLineTotal();
        Label subtotalLabel = new Label(formatPrice(subtotal) + " đ");
        subtotalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff5722;");

        Button removeBtn = new Button("🗑 Remove");
        removeBtn.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-cursor: hand; -fx-background-radius: 3; -fx-font-size: 12px;");
        removeBtn.setOnAction(e -> onRemove.run());

        rightBox.getChildren().addAll(subtotalLabel, removeBtn);

        itemBox.getChildren().addAll(imageLabel, infoBox, spacer, rightBox);
        return itemBox;
    }

    public void updateTotalLabels(Label subtotalLabel, Label vatLabel, Label totalLabel,
                                   double subtotal, double vat, double total) {
        if (subtotalLabel != null) {
            subtotalLabel.setText(formatPrice(subtotal) + " đ");
        }
        if (vatLabel != null) {
            vatLabel.setText(formatPrice(vat) + " đ");
        }
        if (totalLabel != null) {
            totalLabel.setText(formatPrice(total) + " đ");
        }
    }

    public void updateCartCountLabel(Label cartCountLabel, int count) {
        if (cartCountLabel != null) {
            cartCountLabel.setText("Cart (" + count + ")");
        }
    }

    public String buildInsufficientStockMessage(java.util.List<java.util.Map<String, Object>> insufficientItems) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("⚠️ INSUFFICIENT STOCK\n\n");
        errorMsg.append("Stock availability has been updated from database.\n");
        errorMsg.append("Some products do not have sufficient stock:\n\n");

        for (java.util.Map<String, Object> item : insufficientItems) {
            errorMsg.append(String.format("• %s\n", item.get("title")));

            if (item.containsKey("error")) {
                errorMsg.append(String.format("  %s\n\n", item.get("error")));
            } else {
                errorMsg.append(String.format("  Requested: %d | Available: %d\n\n",
                        item.get("requestedQty"), item.get("availableQty")));
            }
        }

        errorMsg.append("Please update your cart quantities using +/- buttons and try again.");
        return errorMsg.toString();
    }
}
