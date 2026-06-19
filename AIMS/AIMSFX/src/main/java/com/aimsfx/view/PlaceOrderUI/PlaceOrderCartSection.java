package com.aimsfx.view;

import com.aimsfx.model.CartItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import com.aimsfx.utils.UIUtils;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * CartView - View Layer for Cart Use Case
 */
public class PlaceOrderCartSection {

    @SuppressWarnings("deprecation")
    private static final Locale VN_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance(VN_LOCALE);

    public PlaceOrderCartSection() {
    }

    public String formatPrice(double price) {
        return PRICE_FORMATTER.format(price);
    }

    public void showEmptyCartMessage(VBox container) {
        if (container == null)
            return;

        container.getChildren().clear();

        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));

        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");

        Label subLabel = new Label("Add some products to get started!");
        subLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");

        emptyBox.getChildren().addAll(emptyLabel, subLabel);
        container.getChildren().add(emptyBox);
    }

    public HBox createCartItemBox(CartItem cartItem, Runnable onQuantityChange, Runnable onRemove) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/cart-item.fxml"));
            HBox itemBox = loader.load();
            com.aimsfx.view.CartItemUI controller = loader
                    .getController();
            controller.setItemData(cartItem, onQuantityChange, onRemove);
            return itemBox;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not load cart item UI.");
            return new HBox();
        }
    }

    public void updateTotalLabels(Label subtotalLabel, Label vatLabel, Label totalLabel,
            double subtotal, double vat, double total) {
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

    public void updateCartCountLabel(Label cartCountLabel, int count) {
        if (cartCountLabel != null) {
            cartCountLabel.setText("Cart (" + count + ")");
        }
    }

    public String buildInsufficientStockMessage(java.util.List<java.util.Map<String, Object>> insufficientItems) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("INSUFFICIENT STOCK\n\n");
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
