package com.aimsfx.controller;

import com.aimsfx.exception.EmptyCartException;
import com.aimsfx.exception.InvalidDeliveryInfoException;
import com.aimsfx.model.Cart;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.PlaceOrderService;
import com.aimsfx.router.PlaceOrderRouter;

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
        PlaceOrderRouter.getInstance().navigateToCart(currentStage);
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
        PlaceOrderRouter.getInstance().navigateToPayment(currentOrder, currentInvoice, currentStage);
    }
}
