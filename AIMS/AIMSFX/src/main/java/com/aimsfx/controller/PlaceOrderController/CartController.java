package com.aimsfx.controller.PlaceOrderController;

import com.aimsfx.model.*;
import com.aimsfx.service.ICartService;
import com.aimsfx.service.CartService;
import com.aimsfx.view.CartView;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * CartController - Controller for Cart View (cart-view.fxml)
 */
public class CartController implements Initializable {

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label vatLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label cartCountLabel;

    private ICartManager cartManager;
    private ICartService cartService;
    private CartView cartView;
    private Cart currentCart;

    public CartController() {
        this.cartManager = CartManager.getInstance();
        this.cartService = new CartService();
        this.cartView = new CartView();
    }

    public void setCartManager(ICartManager cartManager) {
        this.cartManager = cartManager;
    }

    public void setCartService(ICartService cartService) {
        this.cartService = cartService;
    }

    public void setCartView(CartView cartView) {
        this.cartView = cartView;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCartData();
    }

    private void loadCartData() {
        currentCart = cartManager.getCart();

        if (currentCart == null || currentCart.getItems().isEmpty()) {
            cartView.showEmptyCartMessage(cartItemsContainer);
            updateTotals(0);
            cartView.updateCartCountLabel(cartCountLabel, 0);
            return;
        }

        int updatedCount = cartManager.refreshCartProducts();
        if (updatedCount > 0) {
            System.out.println("✅ Cart synced with database: " + updatedCount + " item(s) updated");
        }

        cartView.updateCartCountLabel(cartCountLabel, currentCart.getItems().size());

        cartItemsContainer.getChildren().clear();

        for (CartItem cartItem : currentCart.getItems()) {
            HBox itemBox = cartView.createCartItemBox(
                    cartItem,
                    () -> {
                        CartEvents.notifyCartUpdated();
                        loadCartData();
                    },
                    () -> {
                        cartManager.removeProduct(cartItem.getProductId());
                        CartEvents.notifyCartUpdated();
                        loadCartData();
                    });
            cartItemsContainer.getChildren().add(itemBox);
        }

        double subtotal = cartService.calculateSubtotal(currentCart);
        updateTotals(subtotal);
    }

    private void updateTotals(double subtotal) {
        double vat = cartService.calculateVAT(subtotal);
        double total = subtotal + vat;
        cartView.updateTotalLabels(subtotalLabel, vatLabel, totalLabel, subtotal, vat, total);
    }

    @FXML
    public void onContinueShopping() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/homepage-view.fxml"));
            Parent homepageView = loader.load();

            Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(homepageView);
            } else {
                stage.setScene(new Scene(homepageView));
            }
            stage.setTitle("AIMS - Product Management System");
            new animatefx.animation.FadeIn(homepageView).play();

        } catch (IOException e) {
            e.printStackTrace();
            cartView.showError("Error", "Could not load homepage: " + e.getMessage());
        }
    }

    @FXML
    public void onCheckout() {
        try {
            if (currentCart == null || currentCart.getItems().isEmpty()) {
                cartView.showAlert("Empty Cart", "Your cart is empty. Please add items before checkout.");
                return;
            }

            List<Map<String, Object>> insufficientItems = cartService.checkCartStockWithDatabaseRefresh(currentCart);

            if (!insufficientItems.isEmpty()) {
                String errorMessage = cartView.buildInsufficientStockMessage(insufficientItems);
                cartView.showAlert("Insufficient Stock", errorMessage);
                loadCartData();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/place-order-view.fxml"));
            Parent placeOrderViewRoot = loader.load();

            PlaceOrderController placeOrderController = loader.getController();
            placeOrderController.setCart(currentCart);

            Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(placeOrderViewRoot);
            } else {
                stage.setScene(new Scene(placeOrderViewRoot));
            }
            stage.setTitle("AIMS - Place Order");

        } catch (IOException e) {
            e.printStackTrace();
            cartView.showError("Error", "Could not proceed to checkout: " + e.getMessage());
        }
    }

    public Cart getCurrentCart() {
        return currentCart;
    }
}
