package com.aimsfx.controller;

import com.aimsfx.model.Cart;
import com.aimsfx.service.CartService;
import com.aimsfx.service.ICartService;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.router.PlaceOrderRouter;

import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * Pure MVC Logic Controller for Cart operations.
 * Handles user input intent, business logic delegation, and view selection/navigation.
 */
public class CartController {

    private final ICartService cartService;

    public CartController() {
        this.cartService = new CartService();
    }

    public void handleContinueShopping(Stage currentStage) {
        PlaceOrderRouter.getInstance().navigateToHomepage(currentStage);
    }

    public void handleCheckoutRequest(Cart currentCart, Stage currentStage, com.aimsfx.view.PlaceOrderUI.PlaceOrderCartSection cartView) {
        if (currentCart == null || currentCart.getItems().isEmpty()) {
            UIUtils.showWarning("Empty Cart", "Your cart is empty. Please add items before checkout.");
            return;
        }

        List<Map<String, Object>> insufficientItems = cartService.checkCartStockWithDatabaseRefresh(currentCart);

        if (!insufficientItems.isEmpty()) {
            String errorMessage = cartView.buildInsufficientStockMessage(insufficientItems);
            UIUtils.showError("Insufficient Stock", errorMessage);
            return;
        }

        // Navigate to Place Order View
        PlaceOrderRouter.getInstance().navigateToPlaceOrder(currentCart, currentStage);
    }
}
