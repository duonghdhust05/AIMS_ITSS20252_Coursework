package com.aimsfx.view;

import com.aimsfx.view.PlaceOrderUI.PlaceOrderCartSection;
import com.aimsfx.model.*;
import com.aimsfx.service.ICartService;
import com.aimsfx.service.CartService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * CartScreen - Controller for Cart View (cart-view.fxml)
 */
public class CartView implements Initializable {

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
    private PlaceOrderCartSection cartView;
    private Cart currentCart;
    private com.aimsfx.controller.CartController logicController;

    public CartView() {
        this.cartManager = CartManager.getInstance();
        this.cartService = new CartService();
        this.cartView = new PlaceOrderCartSection();
        this.logicController = new com.aimsfx.controller.CartController();
    }

    public void setCartManager(ICartManager cartManager) {
        this.cartManager = cartManager;
    }

    public void setCartService(ICartService cartService) {
        this.cartService = cartService;
    }

    public void setCartView(PlaceOrderCartSection cartView) {
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
        Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
        logicController.handleContinueShopping(stage);
    }

    @FXML
    public void onCheckout() {
        Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
        logicController.handleCheckoutRequest(currentCart, stage, cartView);
        // Reload data in case it was rejected due to insufficient stock and cartView
        // displayed a message
        loadCartData();
    }

    public Cart getCurrentCart() {
        return currentCart;
    }
}
