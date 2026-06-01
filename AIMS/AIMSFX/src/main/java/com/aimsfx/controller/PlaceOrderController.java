package com.aimsfx.controller;

import com.aimsfx.exception.*;
import com.aimsfx.model.*;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.*;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.InvoiceUI;
import com.aimsfx.view.PaymentUI;
import com.aimsfx.view.ViewCartUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            new EmailService();
        } catch (Exception e) {
        }

        if (provinceComboBox != null) {
            provinceComboBox.getItems().addAll("Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
                    "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre",
                    "Bình Định");
            if (currentCart == null)
                currentCart = CartManager.getInstance().getCart();
            deliveryInfo = SessionManager.getInstance().getDeliveryInfo();
            if (deliveryInfo != null)
                restoreDeliveryInfoToForm();
            if (currentCart != null && !currentCart.getItems().isEmpty()) {
                loadCartIntoTableView();
                updatePlaceOrderTotals();
            }
        }
    }

    private void restoreDeliveryInfoToForm() {
        if (deliveryInfo == null)
            return;
        if (deliveryInfo.getRecipientName() != null && nameField != null)
            nameField.setText(deliveryInfo.getRecipientName());
        if (deliveryInfo.getPhoneNumber() != null && phoneField != null)
            phoneField.setText(deliveryInfo.getPhoneNumber());
        if (deliveryInfo.getEmail() != null && emailField != null)
            emailField.setText(deliveryInfo.getEmail());
        if (deliveryInfo.getProvince() != null && provinceComboBox != null)
            provinceComboBox.setValue(deliveryInfo.getProvince());
        if (deliveryInfo.getWard() != null && subDistrictField != null)
            subDistrictField.setText(deliveryInfo.getWard());
        if (deliveryInfo.getAddress() != null && addressArea != null)
            addressArea.setText(deliveryInfo.getAddress());
        if (deliveryInfo.getDeliveryInstructions() != null && deliveryInstructionsArea != null)
            deliveryInstructionsArea.setText(deliveryInfo.getDeliveryInstructions());

        if (deliveryInfo.getRecipientName() != null && !deliveryInfo.getRecipientName().trim().isEmpty()) {
            updateDeliveryDisplayAndSwitchMode(deliveryInfo);
        }
    }

    private void updateDeliveryDisplayAndSwitchMode(DeliveryInfo info) {
        if (info == null)
            return;
        if (displayNameLabel != null)
            displayNameLabel.setText(info.getRecipientName() != null ? info.getRecipientName() : "-");
        if (displayPhoneLabel != null)
            displayPhoneLabel.setText(info.getPhoneNumber() != null ? info.getPhoneNumber() : "-");
        if (displayEmailLabel != null)
            displayEmailLabel
                    .setText(info.getEmail() != null && !info.getEmail().trim().isEmpty() ? info.getEmail() : "-");
        if (displayProvinceLabel != null)
            displayProvinceLabel.setText(info.getProvince() != null ? info.getProvince() : "-");
        if (displaySubDistrictLabel != null)
            displaySubDistrictLabel
                    .setText(info.getWard() != null && !info.getWard().trim().isEmpty() ? info.getWard() : "-");
        if (displayAddressLabel != null)
            displayAddressLabel.setText(info.getAddress() != null ? info.getAddress() : "-");
        if (displayInstructionsLabel != null)
            displayInstructionsLabel
                    .setText(info.getDeliveryInstructions() != null && !info.getDeliveryInstructions().trim().isEmpty()
                            ? info.getDeliveryInstructions()
                            : "-");

        if (deliveryInfoDisplay != null) {
            deliveryInfoDisplay.setVisible(true);
            deliveryInfoDisplay.setManaged(true);
        }
        if (deliveryFormGrid != null) {
            deliveryFormGrid.setVisible(false);
            deliveryFormGrid.setManaged(false);
        }
        if (addDeliveryInfoButton != null) {
            addDeliveryInfoButton.setText("✏️ Edit Delivery Information");
        }
    }

    private void loadCartIntoTableView() {
        if (cartItemsContainer == null || currentCart == null)
            return;
        cartItemsContainer.getChildren().clear();
        for (CartItem cartItem : currentCart.getItems()) {
            cartItemsContainer.getChildren().add(createProductCard(cartItem));
        }
    }

    private HBox createProductCard(CartItem cartItem) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 8;");

        Product product = cartItem.getProduct();
        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 40px;");
        StackPane imageContainer = new StackPane(iconLabel);
        imageContainer.setStyle(
                "-fx-background-color: #f5f5f5; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);");
        imageContainer.setPrefSize(70, 70);
        imageContainer.setMaxSize(70, 70);

        VBox details = new VBox(6);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, Priority.ALWAYS);

        String title = product.getTitle();
        if (title == null || title.trim().isEmpty())
            title = "Product #" + product.getProductId();
        Label nameLabel = new Label(title);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(400);

        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(UIUtils.formatPrice(product.getCurrentPrice()) + " đ");
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        Label qtyBadge = new Label("× " + cartItem.getQuantity());
        qtyBadge.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 12px;");
        priceRow.getChildren().addAll(priceLabel, qtyBadge);
        details.getChildren().addAll(nameLabel, priceRow);

        VBox subtotalBox = new VBox(4);
        subtotalBox.setAlignment(Pos.CENTER_RIGHT);
        subtotalBox.setMinWidth(150);
        Label subtotalTitleLabel = new Label("Subtotal");
        subtotalTitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        Label subtotalValueLabel = new Label(UIUtils.formatPrice(cartItem.getLineTotal()) + " đ");
        subtotalValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");
        subtotalBox.getChildren().addAll(subtotalTitleLabel, subtotalValueLabel);

        card.getChildren().addAll(imageContainer, details, subtotalBox);
        return card;
    }

    @FXML
    public void onAddDeliveryInfo() {
        showDeliveryInfoDialog();
        if (nameField.getText() != null && !nameField.getText().trim().isEmpty()) {
            try {
                DeliveryInfo displayInfo = placeOrderService.createDeliveryInfoFromForm(
                        nameField.getText(), phoneField.getText(), emailField.getText(),
                        provinceComboBox.getValue(), subDistrictField.getText(),
                        addressArea.getText(), deliveryInstructionsArea.getText());
                updateDeliveryDisplayAndSwitchMode(displayInfo);
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

            if (nameField.getText() != null && !nameField.getText().trim().isEmpty()) {
                controller.setExistingData(nameField.getText(), phoneField.getText(), emailField.getText(),
                        provinceComboBox.getValue(), subDistrictField.getText(), addressArea.getText(),
                        deliveryInstructionsArea.getText());
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(nameField.getScene().getWindow());
            dialogStage.setTitle("Delivery Information");
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                nameField.setText(controller.getName());
                phoneField.setText(controller.getPhone());
                emailField.setText(controller.getEmail());
                provinceComboBox.setValue(controller.getProvince());
                subDistrictField.setText(controller.getSubDistrict());
                addressArea.setText(controller.getAddress());
                deliveryInstructionsArea.setText(controller.getDeliveryInstructions());
                deliveryFeeLabel.setText(controller.getDeliveryFee());

                DeliveryInfo info = placeOrderService.createDeliveryInfoFromForm(
                        controller.getName(), controller.getPhone(), controller.getEmail(),
                        controller.getProvince(), controller.getSubDistrict(),
                        controller.getAddress(), controller.getDeliveryInstructions());
                SessionManager.getInstance().setDeliveryInfo(info);
                this.deliveryInfo = info;
                updatePlaceOrderTotals();
            }
        } catch (Exception ex) {
            UIUtils.showAlert("Error", "Cannot display delivery information form: " + ex.getMessage());
        }
    }

    private void updatePlaceOrderTotals() {
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

    private boolean validateDeliveryInfo() {
        List<String> errors = placeOrderService.validateDeliveryInfo(
                nameField.getText(), phoneField.getText(), emailField.getText(),
                provinceComboBox.getValue(), addressArea.getText());

        if ((deliveryFeeLabel.getText() == null || deliveryFeeLabel.getText().equals("0.00 VND"))
                && originalDeliveryFee == 0) {
            errors.add("Please click 'Calculate Delivery Fee' before placing order.");
        }

        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            for (String error : errors)
                errorMsg.append("• ").append(error).append("\n");
            UIUtils.showAlert("Invalid Information", "Please check delivery information:\n\n" + errorMsg);
            return false;
        }
        return true;
    }

    @FXML
    public void placeOrder() {
        try {
            if (!validateDeliveryInfo())
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
                    nameField.getText().trim(),
                    phoneField.getText().trim().replaceAll("\\s+", ""),
                    emailField.getText() != null ? emailField.getText().trim() : null,
                    provinceComboBox.getValue(), null, addressArea.getText().trim(),
                    deliveryInstructionsArea.getText() != null ? deliveryInstructionsArea.getText().trim() : null);

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

            stage.setScene(new Scene(root));
            stage.setTitle("AIMS - Payment");
            stage.centerOnScreen();

        } catch (Exception e) {
            UIUtils.showAlert("System Error", "Could not process payment: " + e.getMessage());
        }
    }
}
