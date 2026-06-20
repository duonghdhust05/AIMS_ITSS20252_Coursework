package com.aimsfx.router;

import com.aimsfx.controller.PayOrderController;
import com.aimsfx.factory.PaymentControllerFactory;
import com.aimsfx.model.Cart;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.model.TransactionInfo;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.PaymentView.PaymentUI;
import com.aimsfx.view.PlaceOrderUI.DeliveryInfoDialogUI;
import com.aimsfx.view.PlaceOrderUI.InvoiceDialogUI;
import com.aimsfx.view.PlaceOrderUI.PlaceOrderScreen;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * PlaceOrderRouter handles navigation and dialog creation for the Place Order
 * module.
 * Implements the Singleton pattern.
 */
public class PlaceOrderRouter {

    private static PlaceOrderRouter instance;

    private PlaceOrderRouter() {
    }

    public static PlaceOrderRouter getInstance() {
        if (instance == null) {
            instance = new PlaceOrderRouter();
        }
        return instance;
    }

    /**
     * Navigates to the Homepage.
     */
    public void navigateToHomepage(Stage currentStage) {
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

    /**
     * Navigates to the Place Order screen.
     */
    public void navigateToPlaceOrder(Cart cart, Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/place-order-view.fxml"));
            Parent placeOrderViewRoot = loader.load();

            PlaceOrderScreen placeOrderScreen = loader.getController();
            placeOrderScreen.setCart(cart);

            if (currentStage.getScene() != null) {
                currentStage.getScene().setRoot(placeOrderViewRoot);
            } else {
                currentStage.setScene(new Scene(placeOrderViewRoot));
            }
            currentStage.setTitle("AIMS - Place Order");
            new animatefx.animation.FadeIn(placeOrderViewRoot).play();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not proceed to checkout: " + e.getMessage());
        }
    }

    /**
     * Navigates back to the Cart screen.
     */
    public void navigateToCart(Stage currentStage) {
        if (currentStage != null) {
            UIUtils.navigate(currentStage, "/com/aimsfx/cart-view.fxml", "AIMS - Shopping Cart");
        }
    }

    /**
     * Navigates to the Payment screen.
     */
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
            new animatefx.animation.FadeIn(root).play();

        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showAlert("System Error", "Could not process payment: " + e.getMessage());
        }
    }

    /**
     * Shows the Delivery Information Dialog.
     * Returns an Optional containing the dialog controller if the user saved the
     * info.
     */
    public Optional<DeliveryInfoDialogUI> showDeliveryInfoDialog(Stage owner, double totalWeight, double totalAmount,
            DeliveryInfo existingInfo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/delivery-info-dialog.fxml"));
            VBox dialogRoot = loader.load();
            DeliveryInfoDialogUI controller = loader.getController();
            controller.setOrderData(totalWeight, totalAmount);

            if (existingInfo != null && existingInfo.getRecipientName() != null
                    && !existingInfo.getRecipientName().trim().isEmpty()) {
                controller.setExistingData(
                        existingInfo.getRecipientName(),
                        existingInfo.getPhoneNumber(),
                        existingInfo.getEmail(),
                        existingInfo.getProvince(),
                        existingInfo.getWard(),
                        existingInfo.getAddress(),
                        existingInfo.getDeliveryInstructions());
            }

            Stage dialogStage = new Stage();
            UIUtils.applyAppIcon(dialogStage);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                dialogStage.initOwner(owner);
            }
            dialogStage.setTitle("Delivery Information");
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setResizable(false);
            new animatefx.animation.FadeIn(dialogRoot).play();
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                return Optional.of(controller);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            UIUtils.showAlert("Error", "Cannot display delivery information form: " + ex.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Shows the Delivery Information Dialog using individual fields (useful for
     * restoring from form state).
     */
    public Optional<DeliveryInfoDialogUI> showDeliveryInfoDialog(Stage owner, double totalWeight, double totalAmount,
            String name, String phone, String email,
            String province, String subDistrict, String address, String instructions) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/delivery-info-dialog.fxml"));
            VBox dialogRoot = loader.load();
            DeliveryInfoDialogUI controller = loader.getController();
            controller.setOrderData(totalWeight, totalAmount);

            if (name != null && !name.trim().isEmpty()) {
                controller.setExistingData(name, phone, email, province, subDistrict, address, instructions);
            }

            Stage dialogStage = new Stage();
            UIUtils.applyAppIcon(dialogStage);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                dialogStage.initOwner(owner);
            }
            dialogStage.setTitle("Delivery Information");
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setResizable(false);
            new animatefx.animation.FadeIn(dialogRoot).play();
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                return Optional.of(controller);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            UIUtils.showAlert("Error", "Cannot display delivery information form: " + ex.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Shows the Invoice Dialog.
     */
    public void showInvoiceDialog(Order order, Invoice invoice, TransactionInfo txn) {
        if (invoice == null)
            return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Invoice Details");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        UIUtils.applyAppDialogIcon(dialog);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/invoice-dialog.fxml"));
            ScrollPane scrollPane = loader.load();
            InvoiceDialogUI controller = loader.getController();

            // Pass the provided order, invoice, and transaction info
            controller.setInvoiceData(order, invoice, txn);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().setStyle("-fx-background-color: #f5f5f5;");

            // Add OK button styling
            javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            if (okButton instanceof Button) {
                okButton.setStyle(
                        "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
            }

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not load invoice view: " + e.getMessage());
        }
    }
}
