package com.aimsfx.view.PlaceOrderUI;

import com.aimsfx.model.Cart;
import com.aimsfx.model.CartItem;
import com.aimsfx.utils.UIUtils;
import javafx.scene.layout.HBox;
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

    private javafx.scene.Node createProductCard(CartItem cartItem) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/place-order-item.fxml"));
            HBox card = loader.load();
            PlaceOrderItemUI controller = loader.getController();
            controller.setItemData(cartItem);
            return card;
        } catch (java.io.IOException e) {

            e.printStackTrace();
            UIUtils.showAlert("Error", "Could not load cart item UI.");
            return new HBox();
        }
    }
}
