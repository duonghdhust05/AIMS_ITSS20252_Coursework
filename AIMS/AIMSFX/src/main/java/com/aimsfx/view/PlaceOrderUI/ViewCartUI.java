package com.aimsfx.view.PlaceOrderUI;

import com.aimsfx.model.CartItem;
import com.aimsfx.model.Product;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.BaseView;

import java.util.List;
import java.util.Map;

/**
 * ViewCartUI - Handle cart validation dialogs and confirmations
 */
public class ViewCartUI extends BaseView {

    public void requestToPlaceOrder() {
        displayInfo("Proceeding to place order...");
    }

    public void showInvalidQuantityException(String message, List<Map<String, Object>> invalidItems) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("⚠️ INSUFFICIENT STOCK\n");
        errorMsg.append("=".repeat(60)).append("\n\n");
        errorMsg.append(message).append("\n\n");

        if (invalidItems != null && !invalidItems.isEmpty()) {
            errorMsg.append("Affected Products:\n");
            errorMsg.append("-".repeat(60)).append("\n");

            for (int i = 0; i < invalidItems.size(); i++) {
                Map<String, Object> item = invalidItems.get(i);
                errorMsg.append(String.format("\n%d. ", i + 1));

                Object title = item.get("title");
                if (title != null) {
                    errorMsg.append(title).append("\n");
                } else {
                    errorMsg.append("Product ID: ").append(item.get("productId")).append("\n");
                }

                errorMsg.append("   Requested Quantity: ").append(item.get("requestedQty")).append("\n");
                errorMsg.append("   Available Quantity: ").append(item.get("availableQty")).append("\n");
            }

            errorMsg.append("\n").append("=".repeat(60)).append("\n");
            errorMsg.append("📝 Please update your cart with available quantities and try again.\n");
        }

        displayError(errorMsg.toString());
    }

    public void showInvalidQuantityException(String message) {
        showInvalidQuantityException(message, null);
    }

    public void displayCart(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            displayInfo("Your cart is empty");
            return;
        }

        StringBuilder cartContent = new StringBuilder();
        cartContent.append("Shopping Cart\n");
        cartContent.append("=".repeat(50)).append("\n\n");

        double total = 0;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            cartContent.append("%d. %s\n".formatted(i + 1, item.getTitle()));
            cartContent.append("   Price: %.2f VND x %d\n".formatted(item.getCurrentPrice(), item.getQuantity()));
            cartContent.append("   Subtotal: %.2f VND\n\n".formatted(item.getLineTotal()));
            total += item.getLineTotal();
        }

        cartContent.append("=".repeat(50)).append("\n");
        cartContent.append("Total: %.2f VND\n".formatted(total));

        displayInfo(cartContent.toString());
    }

    public boolean confirmRemoveProduct(Product product) {
        return UIUtils.showConfirmation("Remove Product", "Remove " + product.getTitle() + " from your cart?");
    }

    public void requestToAddBookToCart(int productId) {
        displayInfo("Adding product #" + productId + " to cart...");
    }

    public void displayCartSummary(int itemCount, double totalAmount) {
        String summary = String.format("Cart: %d items | Total: %.2f VND", itemCount, totalAmount);
        displayInfo(summary);
    }

    public void showEmptyCartMessage() {
        displayInfo("Your cart is empty. Please add items to continue.");
    }

    public boolean confirmProceedToCheckout() {
        return UIUtils.showConfirmation("Proceed to Checkout", "Do you want to proceed with this order?");
    }
}
