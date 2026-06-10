package com.aimsfx.controller;

import com.aimsfx.model.CartItem;
import com.aimsfx.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import java.text.NumberFormat;
import java.util.Locale;

public class CartItemController {

    @SuppressWarnings("deprecation")
    private static final Locale VN_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance(VN_LOCALE);

    @FXML
    private Label imageLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label stockLabel;

    @FXML
    private Label quantityLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Button minusBtn;

    @FXML
    private Button plusBtn;

    @FXML
    private Button removeBtn;

    private CartItem cartItem;
    private Runnable onQuantityChange;
    private Runnable onRemove;

    public void setItemData(CartItem cartItem, Runnable onQuantityChange, Runnable onRemove) {
        this.cartItem = cartItem;
        this.onQuantityChange = onQuantityChange;
        this.onRemove = onRemove;

        Product product = cartItem.getProduct();

        String title = product.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Product #" + product.getProductId();
        }
        nameLabel.setText(title);

        priceLabel.setText(formatPrice(product.getCurrentPrice()) + " VND");
        stockLabel.setText("Available: " + product.getStock());
        quantityLabel.setText(String.valueOf(cartItem.getQuantity()));
        subtotalLabel.setText(formatPrice(cartItem.getLineTotal()) + " VND");
    }

    @FXML
    private void handleMinus() {
        if (cartItem.getQuantity() > 1) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            if (onQuantityChange != null) onQuantityChange.run();
        }
    }

    @FXML
    private void handlePlus() {
        Product product = cartItem.getProduct();
        if (cartItem.getQuantity() < product.getStock()) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            if (onQuantityChange != null) onQuantityChange.run();
        } else {
            showAlert("Out of Stock", "Maximum available quantity: " + product.getStock());
        }
    }

    @FXML
    private void handleRemove() {
        if (onRemove != null) onRemove.run();
    }

    private String formatPrice(double price) {
        return PRICE_FORMATTER.format(price);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
