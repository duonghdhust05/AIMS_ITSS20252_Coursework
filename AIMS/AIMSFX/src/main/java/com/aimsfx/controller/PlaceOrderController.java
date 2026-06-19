package com.aimsfx.controller;

import com.aimsfx.exception.EmptyCartException;
import com.aimsfx.exception.InvalidDeliveryInfoException;
import com.aimsfx.model.Cart;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.PlaceOrderService;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.PaymentUI.PaymentUI;
import com.aimsfx.service.PaymentControllerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Pure MVC Logic Controller for Place Order operations.
 */
public class PlaceOrderController {

    private final PlaceOrderService placeOrderService;
    private final OrderRepository orderRepository;

    public PlaceOrderController() {
        this.placeOrderService = new PlaceOrderService();
        this.orderRepository = new OrderRepository();
    }

    public void navigateBackToCart(Stage currentStage) {
        if (currentStage != null) {
            UIUtils.navigate(currentStage, "/com/aimsfx/cart-view.fxml", "AIMS - Shopping Cart");
        }
    }

    public Order processOrderCreation(Cart cart, DeliveryInfo info) throws Exception {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty. Cannot place order.");
        }

        String stockError = placeOrderService.checkProductAvailability(cart);
        if (stockError != null) {
            throw new IllegalStateException("Out of Stock: " + stockError);
        }

        return placeOrderService.createAndSaveOrder(cart, info, orderRepository);
    }

    public Map<String, Object> submitDeliveryInfo(Order order, DeliveryInfo deliveryInfo) throws Exception {
        if (!deliveryInfo.checkValidityOfDeliveryInfo()) {
            throw new InvalidDeliveryInfoException("Invalid delivery information provided");
        }
        if (order == null) {
            throw new IllegalStateException("No current order. Please place an order first.");
        }

        return placeOrderService.processDeliveryAndCreateInvoice(order, deliveryInfo);
    }

    public void navigateToPayment(Order currentOrder, Invoice currentInvoice, Stage currentStage) {
        try {
            if (currentOrder == null) {
                UIUtils.showAlert("Error", "Please place an order first.");
                return;
            }
            if (currentInvoice != null) {
                currentOrder.setTotalAmount(currentInvoice.getTotalAmount());
            }

            PayOrderController paymentController = PaymentControllerFactory.getPayOrderController();
            if (paymentController == null) {
                UIUtils.showAlert("Payment Error", "Failed to initialize payment system.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/payment-view.fxml"));
            loader.setControllerFactory(c -> new PaymentUI(paymentController));
            Parent root = loader.load();

            PaymentUI paymentUI = loader.getController();
            paymentUI.initializeData(currentOrder, currentInvoice);

            if (currentStage.getScene() != null) {
                currentStage.getScene().setRoot(root);
            } else {
                currentStage.setScene(new Scene(root));
            }
            currentStage.setTitle("AIMS - Payment");

        } catch (Exception e) {
            UIUtils.showAlert("System Error", "Could not process payment: " + e.getMessage());
        }
    }
}
