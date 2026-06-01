package com.aimsfx.controller.PlaceOrderController;

import com.aimsfx.model.Cart;
import com.aimsfx.service.PlaceOrderService;
import com.aimsfx.utils.UIUtils;
import javafx.scene.control.Label;

public class PlaceOrderSummaryHandler {
    private final Label subtotalLabel, vatLabel, totalLabel, deliveryFeeLabel;

    public PlaceOrderSummaryHandler(Label subtotalLabel, Label vatLabel, Label totalLabel, Label deliveryFeeLabel) {
        this.subtotalLabel = subtotalLabel;
        this.vatLabel = vatLabel;
        this.totalLabel = totalLabel;
        this.deliveryFeeLabel = deliveryFeeLabel;
    }

    public void updateTotals(PlaceOrderService placeOrderService, Cart currentCart) {
        if (currentCart == null || subtotalLabel == null)
            return;
        double subtotal = placeOrderService.calculateSubtotal(currentCart.getItems());
        double vat = placeOrderService.calculateVAT(subtotal);
        double deliveryFee = placeOrderService.parseDeliveryFee(deliveryFeeLabel.getText());
        double total = subtotal + vat + deliveryFee;

        subtotalLabel.setText(UIUtils.formatPrice(subtotal) + " VND");
        vatLabel.setText(UIUtils.formatPrice(vat) + " VND");
        totalLabel.setText(UIUtils.formatPrice(total) + " VND");
    }

    public String getDeliveryFeeText() {
        return deliveryFeeLabel != null ? deliveryFeeLabel.getText() : "0.00 VND";
    }

    public void setDeliveryFeeText(String text) {
        if (deliveryFeeLabel != null) {
            deliveryFeeLabel.setText(text);
        }
    }
}
