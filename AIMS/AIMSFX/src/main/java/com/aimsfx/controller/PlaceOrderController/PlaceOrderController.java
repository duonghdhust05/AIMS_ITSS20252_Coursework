package com.aimsfx.controller.PlaceOrderController;

import com.aimsfx.controller.PayOrderController;
import com.aimsfx.exception.*;
import com.aimsfx.model.*;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.*;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.InvoiceUI;
import com.aimsfx.view.ViewCartUI;
import com.aimsfx.view.PaymentUI.PaymentUI;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class PlaceOrderController implements Initializable {

    private Cart currentCart;
    private Order currentOrder;
    private DeliveryInfo deliveryInfo;
    private Invoice currentInvoice;

    private float originalDeliveryFee = 0f;

    private final InvoiceUI invoiceUI;
    private final ViewCartUI viewCartUI;
    private final PlaceOrderService placeOrderService;
    private final OrderRepository orderRepository;

    // FXML Elements
    @FXML
    private VBox cartItemsContainer;
    @FXML
    private ScrollPane cartItemsScrollPane;

    @FXML
    private Label subtotalLabel, vatLabel, totalLabel, deliveryFeeLabel;

    @FXML
    private TextField nameField, phoneField, emailField, subDistrictField;
    @FXML
    private ComboBox<String> provinceComboBox;
    @FXML
    private TextArea addressArea, deliveryInstructionsArea;
    @FXML
    private GridPane deliveryFormGrid;
    @FXML
    private VBox deliveryInfoDisplay;

    @FXML
    private Label displayNameLabel, displayPhoneLabel, displayEmailLabel;
    @FXML
    private Label displayProvinceLabel, displaySubDistrictLabel, displayAddressLabel, displayInstructionsLabel;

    @FXML
    private Button placeOrderButton, addDeliveryInfoButton, payOrderButton;

    // Handlers
    private PlaceOrderCartHandler cartDisplayHandler;
    private PlaceOrderDeliveryFormHandler deliveryFormHandler;
    private PlaceOrderSummaryHandler orderSummaryHandler;

    public PlaceOrderController() {
        this(new PlaceOrderService(), new InvoiceUI(), new ViewCartUI(), new OrderRepository());
    }

    public PlaceOrderController(PlaceOrderService placeOrderService, InvoiceUI invoiceUI, ViewCartUI viewCartUI,
            OrderRepository orderRepository) {
        this.placeOrderService = placeOrderService;
        this.invoiceUI = invoiceUI;
        this.viewCartUI = viewCartUI;
        this.orderRepository = orderRepository;
    }

    public void setCart(Cart cart) {
        this.currentCart = cart;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            new EmailService();
        } catch (Exception e) {
        }

        this.cartDisplayHandler = new PlaceOrderCartHandler(cartItemsContainer);
        this.deliveryFormHandler = new PlaceOrderDeliveryFormHandler(nameField, phoneField, emailField,
                subDistrictField,
                provinceComboBox, addressArea, deliveryInstructionsArea, deliveryFormGrid, deliveryInfoDisplay,
                displayNameLabel, displayPhoneLabel, displayEmailLabel, displayProvinceLabel, displaySubDistrictLabel,
                displayAddressLabel, displayInstructionsLabel, addDeliveryInfoButton);
        this.orderSummaryHandler = new PlaceOrderSummaryHandler(subtotalLabel, vatLabel, totalLabel, deliveryFeeLabel);

        if (provinceComboBox != null) {
            deliveryFormHandler.initProvinceComboBox();
            if (currentCart == null)
                currentCart = CartManager.getInstance().getCart();
            deliveryInfo = SessionManager.getInstance().getDeliveryInfo();

            if (deliveryInfo != null) {
                deliveryFormHandler.restoreDeliveryInfoToForm(deliveryInfo);
            }
            if (currentCart != null && !currentCart.getItems().isEmpty()) {
                cartDisplayHandler.loadCartIntoTableView(currentCart);
                orderSummaryHandler.updateTotals(placeOrderService, currentCart);
            }
        }
    }

    public boolean placeOrder(Cart cart) throws EmptyCartException {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty. Cannot place order.");
        }
        this.currentCart = cart;

        List<Map<String, Object>> insufficientItems = placeOrderService.getInsufficientStockItems(cart);
        if (!insufficientItems.isEmpty()) {
            viewCartUI.showInvalidQuantityException(
                    "Some products do not have sufficient stock. Please update your cart.", insufficientItems);
            return false;
        }

        this.currentOrder = placeOrderService.createOrder(cart);
        return true;
    }

    public Invoice submitDeliveryInfo(DeliveryInfo deliveryInfo)
            throws InvalidDeliveryInfoException, UnsupportedLocationException {
        this.deliveryInfo = deliveryInfo;
        if (!deliveryInfo.checkValidityOfDeliveryInfo()) {
            throw new InvalidDeliveryInfoException("Invalid delivery information provided");
        }
        if (currentOrder == null) {
            throw new IllegalStateException("No current order. Please place an order first.");
        }

        Map<String, Object> result = placeOrderService.processDeliveryAndCreateInvoice(currentOrder, deliveryInfo);
        this.currentInvoice = (Invoice) result.get("invoice");
        this.originalDeliveryFee = ((Number) result.get("originalFee")).floatValue();

        invoiceUI.displayInvoice(currentInvoice);
        return currentInvoice;
    }

    @FXML
    public void onBackToCart() {
        Stage stage = cartItemsContainer != null ? (Stage) cartItemsContainer.getScene().getWindow() : null;
        if (stage != null)
            UIUtils.navigate(stage, "/com/aimsfx/cart-view.fxml", "AIMS - Shopping Cart");
    }

    @FXML
    public void onAddDeliveryInfo() {
        showDeliveryInfoDialog();
        if (deliveryFormHandler.getName() != null && !deliveryFormHandler.getName().trim().isEmpty()) {
            try {
                DeliveryInfo displayInfo = placeOrderService.createDeliveryInfoFromForm(
                        deliveryFormHandler.getName(), deliveryFormHandler.getPhone(), deliveryFormHandler.getEmail(),
                        deliveryFormHandler.getProvince(), deliveryFormHandler.getSubDistrict(),
                        deliveryFormHandler.getAddress(), deliveryFormHandler.getDeliveryInstructions());
                deliveryFormHandler.updateDeliveryDisplayAndSwitchMode(displayInfo);
            } catch (InvalidDeliveryInfoException e) {
                UIUtils.showAlert("Invalid Delivery Info", e.getMessage());
            }
        }
    }

    private void showDeliveryInfoDialog() {
        try {
            double totalWeight = placeOrderService.calculateTotalWeight(currentCart);
            double totalAmount = placeOrderService.calculateSubtotal(
                    currentCart != null ? currentCart.getItems() : java.util.Collections.emptyList());

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/delivery-info-dialog.fxml"));
            VBox dialogRoot = loader.load();
            DeliveryInfoDialogController controller = loader.getController();
            controller.setOrderData(totalWeight, totalAmount);

            if (deliveryFormHandler.getName() != null && !deliveryFormHandler.getName().trim().isEmpty()) {
                controller.setExistingData(deliveryFormHandler.getName(), deliveryFormHandler.getPhone(),
                        deliveryFormHandler.getEmail(),
                        deliveryFormHandler.getProvince(), deliveryFormHandler.getSubDistrict(),
                        deliveryFormHandler.getAddress(),
                        deliveryFormHandler.getDeliveryInstructions());
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(nameField.getScene().getWindow());
            dialogStage.setTitle("Delivery Information");
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                deliveryFormHandler.updateFormFromDialog(controller);
                orderSummaryHandler.setDeliveryFeeText(controller.getDeliveryFee());

                DeliveryInfo info = placeOrderService.createDeliveryInfoFromForm(
                        controller.getName(), controller.getPhone(), controller.getEmail(),
                        controller.getProvince(), controller.getSubDistrict(),
                        controller.getAddress(), controller.getDeliveryInstructions());
                SessionManager.getInstance().setDeliveryInfo(info);
                this.deliveryInfo = info;
                orderSummaryHandler.updateTotals(placeOrderService, currentCart);
            }
        } catch (Exception ex) {
            UIUtils.showAlert("Error", "Cannot display delivery information form: " + ex.getMessage());
        }
    }

    @FXML
    public void placeOrder() {
        try {
            if (!deliveryFormHandler.validateDeliveryInfo(placeOrderService, originalDeliveryFee,
                    orderSummaryHandler.getDeliveryFeeText()))
                return;

            if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
                UIUtils.showAlert("Empty Cart", "No products in cart to place order.");
                return;
            }

            String stockError = placeOrderService.checkProductAvailability(currentCart);
            if (stockError != null) {
                UIUtils.showAlert("Out of Stock", stockError);
                return;
            }

            DeliveryInfo info = placeOrderService.createDeliveryInfoFromForm(
                    deliveryFormHandler.getName().trim(),
                    deliveryFormHandler.getPhone().trim().replaceAll("\\s+", ""),
                    deliveryFormHandler.getEmail() != null ? deliveryFormHandler.getEmail().trim() : null,
                    deliveryFormHandler.getProvince(), null, deliveryFormHandler.getAddress().trim(),
                    deliveryFormHandler.getDeliveryInstructions() != null
                            ? deliveryFormHandler.getDeliveryInstructions().trim()
                            : null);

            if (currentOrder == null) {
                currentOrder = placeOrderService.createAndSaveOrder(currentCart, info, orderRepository);
            }
            submitDeliveryInfo(info);

            if (payOrderButton != null)
                payOrderButton.setDisable(false);
            if (placeOrderButton != null)
                placeOrderButton.setDisable(true);

        } catch (InvalidDeliveryInfoException e) {
            UIUtils.showAlert("Invalid Information", "Invalid delivery information:\n\n" + e.getMessage());
        } catch (Exception e) {
            UIUtils.showAlert("Error", "Cannot place order:\n\n" + e.getMessage());
        }
    }

    @FXML
    public void payOrder() {
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

            Stage stage = (Stage) payOrderButton.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/payment-view.fxml"));
            loader.setControllerFactory(c -> new PaymentUI(paymentController));
            Parent root = loader.load();

            PaymentUI paymentUI = loader.getController();
            paymentUI.initializeData(currentOrder, currentInvoice);

            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }
            stage.setTitle("AIMS - Payment");

        } catch (Exception e) {
            UIUtils.showAlert("System Error", "Could not process payment: " + e.getMessage());
        }
    }
}
