package com.aimsfx.controller.PlaceOrderController;

import com.aimsfx.model.Cart;
import com.aimsfx.model.CartItem;
import com.aimsfx.model.Product;
import com.aimsfx.utils.UIUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PlaceOrderCartHandler {
    private final VBox cartItemsContainer;

    public PlaceOrderCartHandler(VBox cartItemsContainer) {
        this.cartItemsContainer = cartItemsContainer;
    }

    public void loadCartIntoTableView(Cart currentCart) {
        if (cartItemsContainer == null || currentCart == null)
            return;
        cartItemsContainer.getChildren().clear();
        for (CartItem cartItem : currentCart.getItems()) {
            cartItemsContainer.getChildren().add(createProductCard(cartItem));
        }
    }

    private HBox createProductCard(CartItem cartItem) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 8;");

        Product product = cartItem.getProduct();
        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 40px;");
        StackPane imageContainer = new StackPane(iconLabel);
        imageContainer.setStyle(
                "-fx-background-color: #f5f5f5; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);");
        imageContainer.setPrefSize(70, 70);
        imageContainer.setMaxSize(70, 70);

        VBox details = new VBox(6);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, Priority.ALWAYS);

        String title = product.getTitle();
        if (title == null || title.trim().isEmpty())
            title = "Product #" + product.getProductId();
        Label nameLabel = new Label(title);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(400);

        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(UIUtils.formatPrice(product.getCurrentPrice()) + " đ");
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        Label qtyBadge = new Label("× " + cartItem.getQuantity());
        qtyBadge.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 12px;");
        priceRow.getChildren().addAll(priceLabel, qtyBadge);
        details.getChildren().addAll(nameLabel, priceRow);

        VBox subtotalBox = new VBox(4);
        subtotalBox.setAlignment(Pos.CENTER_RIGHT);
        subtotalBox.setMinWidth(150);
        Label subtotalTitleLabel = new Label("Subtotal");
        subtotalTitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        Label subtotalValueLabel = new Label(UIUtils.formatPrice(cartItem.getLineTotal()) + " đ");
        subtotalValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");
        subtotalBox.getChildren().addAll(subtotalTitleLabel, subtotalValueLabel);

        card.getChildren().addAll(imageContainer, details, subtotalBox);
        return card;
    }
}
