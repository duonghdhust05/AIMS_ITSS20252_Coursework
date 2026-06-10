package com.aimsfx.controller.PlaceOrderController;

import com.aimsfx.model.CartItem;
import com.aimsfx.model.Product;
import com.aimsfx.utils.UIUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PlaceOrderItemController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label qtyBadge;

    @FXML
    private Label subtotalValueLabel;

    public void setItemData(CartItem cartItem) {
        Product product = cartItem.getProduct();
        String title = product.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Product #" + product.getProductId();
        }
        nameLabel.setText(title);
        priceLabel.setText(UIUtils.formatPrice(product.getCurrentPrice()) + " VND");
        qtyBadge.setText("× " + cartItem.getQuantity());
        subtotalValueLabel.setText(UIUtils.formatPrice(cartItem.getLineTotal()) + " VND");
    }
}
