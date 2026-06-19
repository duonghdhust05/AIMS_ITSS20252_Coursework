package com.aimsfx.controller;

import com.aimsfx.model.Cart;
import com.aimsfx.service.CartService;
import com.aimsfx.service.ICartService;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.PlaceOrderUI.PlaceOrderScreen;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/homepage-view.fxml"));
            Parent homepageView = loader.load();

            if (currentStage.getScene() != null) {
                currentStage.getScene().setRoot(homepageView);
            } else {
                currentStage.setScene(new Scene(homepageView));
            }
            currentStage.setTitle("AIMS - Product Management System");
            new animatefx.animation.FadeIn(homepageView).play();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not load homepage: " + e.getMessage());
        }
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/place-order-view.fxml"));
            Parent placeOrderViewRoot = loader.load();

            PlaceOrderScreen placeOrderScreen = loader.getController();
            placeOrderScreen.setCart(currentCart);

            if (currentStage.getScene() != null) {
                currentStage.getScene().setRoot(placeOrderViewRoot);
            } else {
                currentStage.setScene(new Scene(placeOrderViewRoot));
            }
            currentStage.setTitle("AIMS - Place Order");
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not proceed to checkout: " + e.getMessage());
        }
    }
}
