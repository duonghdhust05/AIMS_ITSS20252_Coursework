package com.aimsfx.controller;

import com.aimsfx.exception.*;
import com.aimsfx.model.*;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.*;
import com.aimsfx.view.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

/**
 * PlaceOrderController - Controller for Place Order Use Case
 * 
 * ARCHITECTURE:
 * ┌─────────────────────────────────────────────────────────────┐
 * │  PlaceOrderController (This class - Orchestration)         │
 * ├─────────────────────────────────────────────────────────────┤
 * │  PlaceOrderService    (Business logic)                     │
 * │  PlaceOrderView       (UI rendering)                       │
 * │  OrderRepository      (Persistence)                        │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * RESPONSIBILITIES:
 * 1. Handle FXML events (@FXML methods)
 * 2. Orchestrate workflow between Service and View layers
 * 3. Manage controller state (currentOrder, currentInvoice, etc.)
 * 
 * SOLID COMPLIANCE:
 * - SRP: Delegates business logic to PlaceOrderService, UI to PlaceOrderView
 * - DIP: Uses DI constructor for testability
 * 
 * @see PlaceOrderService for business logic
 * @see PlaceOrderView for UI rendering
 */
public class PlaceOrderController implements Initializable {

    // ==================== ATTRIBUTES - Business State ====================
    
    private Cart currentCart;
    private Order currentOrder;
    private DeliveryInfo deliveryInfo;          // #3: Delivery info entered by customer
    private Invoice currentInvoice;             // #4: Invoice created after delivery accepted
    private TransactionInfo transactionInfo;    // #5: Payment/transaction result
    
    // Delivery fee tracking
    private float originalDeliveryFee = 0f;
    

    // ==================== ATTRIBUTES - Dependencies (Injected) ====================
    
    // View layer
    private final PlaceOrderView placeOrderView;
    private final InvoiceUI invoiceUI;
    private final ViewCartUI viewCartUI;
    
    // Service layer
    private final PlaceOrderService placeOrderService;
    // Repository layer
    private final OrderRepository orderRepository;

    // ==================== ATTRIBUTES - FXML Components ====================
    // Note: Required by JavaFX - must be in Controller class
    
    // Cart display
    @FXML private VBox cartItemsContainer;
    @FXML private ScrollPane cartItemsScrollPane;
    @FXML private TableView<Product> cartTableView;
    
    // Price labels
    @FXML private Label subtotalLabel, vatLabel, totalLabel, deliveryFeeLabel;
    
    // Delivery form inputs
    @FXML private TextField nameField, phoneField, emailField, subDistrictField;
    @FXML private ComboBox<String> provinceComboBox;
    @FXML private TextArea addressArea, deliveryInstructionsArea;
    @FXML private GridPane deliveryFormGrid;
    @FXML private VBox deliveryInfoDisplay;
    
    // Delivery display labels
    @FXML private Label displayNameLabel, displayPhoneLabel, displayEmailLabel;
    @FXML private Label displayProvinceLabel, displaySubDistrictLabel, displayAddressLabel, displayInstructionsLabel;
    
    // Action buttons
    @FXML private Button placeOrderButton, addDeliveryInfoButton, payOrderButton;
       
    // Success page labels
    @FXML private Label orderIdLabel, customerNameLabel, phoneNumberLabel, addressLabel, provinceLabel;
    @FXML private Label totalAmountLabel, transactionIdLabel, paymentMethodLabel, transactionDateLabel, statusLabel;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor - Uses default implementations
     * For JavaFX FXML instantiation
     */
    public PlaceOrderController() {
        this(new PlaceOrderService(), 
             new PlaceOrderView(), 
             new InvoiceUI(), 
             new ViewCartUI(),
             new OrderRepository());
    }

    /**
     * Dependency Injection constructor - For testing
     * All dependencies can be mocked
     * 
     * @param placeOrderService Business logic service
     * @param placeOrderView View layer for UI rendering
     * @param invoiceUI Invoice display component
     * @param viewCartUI Cart view component  
     * @param orderRepository Order persistence
     */
    public PlaceOrderController(PlaceOrderService placeOrderService,
                                PlaceOrderView placeOrderView,
                                InvoiceUI invoiceUI,
                                ViewCartUI viewCartUI,
                                OrderRepository orderRepository) {
        this.placeOrderService = placeOrderService;
        this.placeOrderView = placeOrderView;
        this.invoiceUI = invoiceUI;
        this.viewCartUI = viewCartUI;
        this.orderRepository = orderRepository;
    }

    /**
     * Set cart from CartController
     * Called when navigating from cart-view to place-order-view
     * 
     * @param cart The cart passed from CartController
     */
    public void setCart(Cart cart) {
        this.currentCart = cart;
    }

    // ==================== BUSINESS OPERATIONS ====================

    /**
     * placeOrder - Main entry point for order placement flow
     * Start the "place order" flow from the cart: check stock for every item,
     * ONLY proceed to delivery form if stock is sufficient
     * 
     * As per specification:
     * Flow: Validate cart → Check stock → Create order
     * 
     * @param cart Shopping cart with items
     * @return true if order created successfully, false if stock insufficient
     * @throws EmptyCartException if cart is empty
     */
    public boolean placeOrder(Cart cart) throws EmptyCartException {
        // Validate cart
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty. Cannot place order.");
        }
        this.currentCart = cart;

        // Check stock (delegated to Service)
        List<Map<String, Object>> insufficientItems = placeOrderService.getInsufficientStockItems(cart);
        if (!insufficientItems.isEmpty()) {
            viewCartUI.showInvalidQuantityException(
                "Some products do not have sufficient stock. Please update your cart.",
                insufficientItems);
            return false;
        }

        // Create order (delegated to Service)
        this.currentOrder = placeOrderService.createOrder(cart);
        return true;
    }

    /**
     * submitDeliveryInfo - Apply delivery info and create invoice
     * 
     * Flow: Validate → Set to order → Create invoice → Display
     * 
     * @param deliveryInfo Customer delivery information
     * @return Invoice with calculated fees
     * @throws InvalidDeliveryInfoException if delivery info invalid
     * @throws UnsupportedLocationException if location not supported
     */
    public Invoice submitDeliveryInfo(DeliveryInfo deliveryInfo)
            throws InvalidDeliveryInfoException, UnsupportedLocationException {

        // Save and validate
        this.deliveryInfo = deliveryInfo;
        if (!deliveryInfo.checkValidityOfDeliveryInfo()) {
            throw new InvalidDeliveryInfoException("Invalid delivery information provided");
        }
        if (currentOrder == null) {
            throw new IllegalStateException("No current order. Please place an order first.");
        }

        // Delegate to Service
        Map<String, Object> result = placeOrderService.processDeliveryAndCreateInvoice(
            currentOrder, deliveryInfo);
        
        this.currentInvoice = (Invoice) result.get("invoice");
        this.originalDeliveryFee = ((Number) result.get("originalFee")).floatValue();
        ((Number) result.get("discount")).floatValue();

        // Step 5: Display invoice (View layer)
        invoiceUI.displayInvoice(currentInvoice);

        return currentInvoice;
    }

    // ==================== FXML EVENT HANDLERS ====================

    /**
     * FXML handler for Back to Cart button
     */
    @FXML
    public void onBackToCart() {
        Stage stage = cartTableView != null ? (Stage) cartTableView.getScene().getWindow() 
                     : nameField != null ? (Stage) nameField.getScene().getWindow() : null;
        if (stage != null) placeOrderView.navigateToCartScreen(stage);
    }

    
    // ==================== LIFECYCLE - Initializable ====================

    @SuppressWarnings("deprecation")
	@Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize email service (non-critical)
        try {
            new EmailService();
        } catch (Exception e) {
            System.err.println("⚠️ Email service init failed: " + e.getMessage());
        }

        // Bind FXML components to View layer
        if (cartItemsContainer != null && subtotalLabel != null) {
            placeOrderView.bindFXMLComponents(cartItemsContainer, subtotalLabel, vatLabel, totalLabel, deliveryFeeLabel, provinceComboBox);
        }
        if (displayNameLabel != null) {
            placeOrderView.bindDeliveryDisplayLabels(displayNameLabel, displayPhoneLabel, displayEmailLabel,
                displayProvinceLabel, displaySubDistrictLabel, displayAddressLabel, displayInstructionsLabel, 
                deliveryFormGrid, deliveryInfoDisplay, addDeliveryInfoButton);
        }
        if (orderIdLabel != null) {
            placeOrderView.bindSuccessPageLabels(orderIdLabel, customerNameLabel, phoneNumberLabel,
                addressLabel, provinceLabel, totalAmountLabel, transactionIdLabel, paymentMethodLabel, 
                transactionDateLabel, statusLabel);
        }

        // Initialize place order view if province combo is present
        if (provinceComboBox != null) {
            placeOrderView.initializeProvinceComboBox();
            if (currentCart == null) currentCart = CartManager.getInstance().getCart();
            deliveryInfo = CartManager.getInstance().getDeliveryInfo();
            if (deliveryInfo != null) restoreDeliveryInfoToForm();
            if (currentCart != null && !currentCart.getItems().isEmpty()) {
                loadCartIntoTableView();
                updatePlaceOrderTotals();
            }
        }
    }

    private void showAlert(String title, String message) {
        placeOrderView.showAlert(title, message);
    }

    private void restoreDeliveryInfoToForm() {
        if (deliveryInfo == null) return;
        placeOrderView.restoreDeliveryInfoToForm(deliveryInfo, nameField, phoneField, emailField,
            provinceComboBox, subDistrictField, addressArea, deliveryInstructionsArea);
        if (deliveryInfo.getRecipientName() != null && !deliveryInfo.getRecipientName().trim().isEmpty()) {
            placeOrderView.updateDeliveryDisplayAndSwitchMode(deliveryInfo);
        }
    }

    private void loadCartIntoTableView() {
        if (cartItemsContainer == null || currentCart == null) return;
        cartItemsContainer.getChildren().clear();
        for (CartItem cartItem : currentCart.getItems()) {
            cartItemsContainer.getChildren().add(placeOrderView.createProductCard(cartItem));
        }
    }

    // ==================== PLACE ORDER VIEW EVENT HANDLERS ====================

    /** Show delivery information dialog */
    @FXML
    public void onAddDeliveryInfo() {
        showDeliveryInfoDialog();
        if (nameField.getText() != null && !nameField.getText().trim().isEmpty()) {
            try {
                DeliveryInfo displayInfo = placeOrderService.createDeliveryInfoFromForm(
                    nameField.getText(), phoneField.getText(), emailField.getText(),
                    provinceComboBox.getValue(), subDistrictField.getText(),
                    addressArea.getText(), deliveryInstructionsArea.getText());
                placeOrderView.updateDeliveryDisplayAndSwitchMode(displayInfo);
            } catch (InvalidDeliveryInfoException e) {
                showAlert("Invalid Delivery Info", e.getMessage());
            }
        }
    }

    /** Calculate delivery fee based on location and weight
    @FXML
    public void onCalculateDeliveryFee() {
        try {
            if (provinceComboBox.getValue() == null || provinceComboBox.getValue().isEmpty()) {
                showAlert("Missing Information", "Please select Province/City to calculate delivery fee.");
                return;
            }
            if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
                showAlert("Empty Cart", "No products in cart to calculate delivery fee.");
                return;
            }

            float totalWeight = placeOrderService.calculateTotalWeight(currentCart);
            if (totalWeight <= 0) {
                showAlert("Calculation Error", "Cannot calculate delivery fee because products have no weight.");
                return;
            }

            DeliveryInfo tempInfo = placeOrderService.createDeliveryInfoFromForm(
                null, null, null, provinceComboBox.getValue(), null, null, null);
            double subtotal = placeOrderService.calculateSubtotal(currentCart.getItems());
            Map<String, Object> feeResult = placeOrderService.processDeliveryFeeWithDiscount(tempInfo, currentCart, subtotal);
            
            originalDeliveryFee = ((Number) feeResult.get("originalFee")).floatValue();
            deliveryDiscount = ((Number) feeResult.get("discount")).floatValue();
            float deliveryFee = ((Number) feeResult.get("deliveryFee")).floatValue();

            deliveryFeeLabel.setText(placeOrderView.formatPrice(deliveryFee) + " VND");
            updatePlaceOrderTotals();
            showAlert("Fee Calculated", placeOrderView.buildDeliveryFeeMessage(
                provinceComboBox.getValue(), totalWeight, originalDeliveryFee, deliveryDiscount, deliveryFee));

        } catch (Exception e) {
            showAlert("Error", "Cannot calculate delivery fee: " + e.getMessage());
        }
    }
    */

    /** Place order button handler */
    @FXML
    public void placeOrder() {
        try {
            if (!validateDeliveryInfo()) return;
            if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
                showAlert("Empty Cart", "No products in cart to place order.");
                return;
            }

            String stockError = placeOrderService.checkProductAvailability(currentCart);
            if (stockError != null) {
                showAlert("Out of Stock", stockError);
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

            if (payOrderButton != null) payOrderButton.setDisable(false);
            if (placeOrderButton != null) placeOrderButton.setDisable(true);

        } catch (InvalidDeliveryInfoException e) {
            showAlert("Invalid Information", "Invalid delivery information:\n\n" + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Cannot place order:\n\n" + e.getMessage());
        }
    }

    /** Pay Order button handler - uses Factory pattern for payment controller */
    @FXML
    public void payOrder() {
        try {
            if (currentOrder == null) {
                showAlert("Error", "Please place an order first.");
                return;
            }
            if (currentInvoice != null) {
                currentOrder.setTotalAmount(currentInvoice.getTotalAmount());
            }

            PayOrderController paymentController = PaymentControllerFactory.getPayOrderController();
            if (paymentController == null) {
                showAlert("Payment Error", "Failed to initialize payment system.");
                return;
            }

            placeOrderView.navigateToPaymentScreen(currentOrder, currentInvoice, paymentController, 
                (Stage) payOrderButton.getScene().getWindow());

        } catch (Exception e) {
            showAlert("System Error", "Could not process payment: " + e.getMessage());
        }
    }

    public void setSuccessData(Order order, Invoice invoice, TransactionInfo info, DeliveryInfo delivery) {
        this.currentOrder = order;
        this.currentInvoice = invoice;
        this.transactionInfo = info;
        this.deliveryInfo = delivery;
        placeOrderService.sendOrderConfirmationEmail(order, delivery, info);
        placeOrderView.populateSuccessPage(order, invoice, info, delivery,
            orderIdLabel, customerNameLabel, phoneNumberLabel, addressLabel,
            provinceLabel, totalAmountLabel, transactionIdLabel, paymentMethodLabel,
            transactionDateLabel, statusLabel);
    }

    @FXML
    public void backToHomepage() {
        placeOrderView.navigateToHomepage((Stage) orderIdLabel.getScene().getWindow());
    }

    @FXML
    public void viewOrderDetails() {
        if (currentOrder == null || currentInvoice == null || transactionInfo == null) {
            showAlert("No Information", "Order information not found.");
            return;
        }
        placeOrderView.showOrderDetailsDialog(currentOrder, currentInvoice, transactionInfo);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private boolean validateDeliveryInfo() {
        List<String> errors = placeOrderService.validateDeliveryInfo(
            nameField.getText(), phoneField.getText(), emailField.getText(),
            provinceComboBox.getValue(), addressArea.getText());
        
        if ((deliveryFeeLabel.getText() == null || deliveryFeeLabel.getText().equals("0.00 VND")) && originalDeliveryFee == 0) {
            errors.add("Please click 'Calculate Delivery Fee' before placing order.");
        }

        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            for (String error : errors) {
                errorMsg.append("• ").append(error).append("\n");
            }
            showAlert("Invalid Information", "Please check delivery information:\n\n" + errorMsg);
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
	private void showDeliveryInfoDialog() {
        double totalWeight = placeOrderService.calculateTotalWeight(currentCart);
        double totalAmount = placeOrderService.calculateSubtotal(
            currentCart != null ? currentCart.getItems() : java.util.Collections.emptyList());

        java.util.Map<String, String> result = placeOrderView.showDeliveryInfoDialog(
            nameField.getScene().getWindow(), totalWeight, totalAmount,
            nameField.getText(), phoneField.getText(), emailField.getText(),
            provinceComboBox.getValue(), subDistrictField.getText(),
            addressArea.getText(), deliveryInstructionsArea.getText());

        if (result != null) {
            nameField.setText(result.get("name"));
            phoneField.setText(result.get("phone"));
            emailField.setText(result.get("email"));
            provinceComboBox.setValue(result.get("province"));
            subDistrictField.setText(result.get("subDistrict"));
            addressArea.setText(result.get("address"));
            deliveryInstructionsArea.setText(result.get("instructions"));
            deliveryFeeLabel.setText(result.get("deliveryFee"));

            try {
                DeliveryInfo info = placeOrderService.createDeliveryInfoFromForm(
                    result.get("name"), result.get("phone"), result.get("email"),
                    result.get("province"), result.get("subDistrict"),
                    result.get("address"), result.get("instructions"));
                CartManager.getInstance().setDeliveryInfo(info);
                this.deliveryInfo = info;
            } catch (InvalidDeliveryInfoException e) {
                showAlert("Invalid Delivery Info", e.getMessage());
                return;
            }
            updatePlaceOrderTotals();
        }
    }

    private void updatePlaceOrderTotals() {
        if (currentCart == null || subtotalLabel == null) return;
        double subtotal = placeOrderService.calculateSubtotal(currentCart.getItems());
        double vat = placeOrderService.calculateVAT(subtotal);
        double deliveryFee = placeOrderService.parseDeliveryFee(deliveryFeeLabel.getText());
        placeOrderView.updateTotalLabels(subtotalLabel, vatLabel, totalLabel, subtotal, vat, subtotal + vat + deliveryFee, "VND");
    }
}
