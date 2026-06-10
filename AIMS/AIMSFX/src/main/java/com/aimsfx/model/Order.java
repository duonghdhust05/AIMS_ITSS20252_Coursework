package com.aimsfx.model;

import com.aimsfx.exception.EmptyCartException;
import com.aimsfx.exception.InvalidDeliveryInfoException;
import com.aimsfx.exception.OrderSaveFailedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Class
 * Purpose: Represents an order created from a cart with delivery info and
 * payment.
 * 
 * ✅ SOLID COMPLIANCE - Week 11 Audit ✅
 * 
 * ✅ SRP COMPLIANT: Single responsibility - Order lifecycle management
 * ✅ OCP COMPLIANT: Can extend through inheritance if needed
 * ✅ LSP COMPLIANT: No inheritance hierarchy (N/A)
 * ✅ ISP COMPLIANT: No unnecessary interfaces (N/A)
 * ✅ DIP COMPLIANT: Depends on abstractions where appropriate
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods relate to managing order lifecycle: creation, calculation,
 * validation
 * - Single responsibility: Business entity for order management
 * - All attributes and methods work together to maintain order state
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with Cart (createOrder method)
 * - Uses: cart.getItems() to get product list
 * - Type: Data coupling - only uses necessary data (product list)
 * - Justification: Needs product data to create order items
 * 
 * 2. Data Coupling with DeliveryInfo (setDeliveryInfo method)
 * - Uses: deliveryInfo.calculateDeliveryFee(weight),
 * checkValidityOfDeliveryInfo()
 * - Type: Data coupling - uses specific methods for validation and calculation
 * - Justification: Needs delivery validation and fee calculation
 * 
 * 3. Data Coupling with Invoice (saveOrder method)
 * - Uses: invoice to link order with invoice
 * - Type: Data coupling - only stores reference for persistence
 * - Justification: Business rule requires order-invoice linkage
 * 
 * Overall: LOW COUPLING - Only depends on necessary data/methods from other
 * objects
 */
public class Order {

    // Constants
    private static final float VAT_RATE = 0.10f; // 10% VAT on products only

    // Attributes (Table 1)
    private int orderId; // #1: Unique identifier (auto/null)
    private String status; // #2: Current status: new, pending, paid, canceled
    private List<OrderItem> orderItems; // #3: Items (product + quantity + price) from cart
    private LocalDateTime createdDate; // #4: Timestamp when order was created
    private DeliveryInfo deliveryInfo; // #5: Delivery information (step 2.2.4)
    private int invoiceId; // #6: Reference to invoice (step 3.1.3)
    private float subtotal; // #7: Sum of all order item prices (excluding VAT)
    private float vat; // #8: VAT amount (10% of subtotal)
    private float deliveryFee; // #9: Delivery fee from delivery info (2.2.3)
    private float totalAmount; // #10: Final amount = subtotal + vat + deliveryFee
    private String paymentTransactionId; // #11: Reference to payment transaction (UUID String)
    private String cancelReason;

    // Additional relationships
    private Invoice invoice;
    private TransactionInfo transactionInfo;

    // ==================== Constructors ====================

    /**
     * Default constructor
     */
    public Order() {
        this.status = "new";
        this.orderItems = new ArrayList<>();
        this.createdDate = LocalDateTime.now();
        this.subtotal = 0.0f;
        this.vat = 0.0f;
        this.deliveryFee = 0.0f;
        this.totalAmount = 0.0f;
    }

    public Order(int id, float totalAmount) {
        this(); // Call list/date initialization logic of the default constructor
        this.orderId = id;
        this.totalAmount = totalAmount;
    }

    /**
     * Constructor: Create a new order from cart (sequence 1.1.3)
     * Purpose: create order from cart.
     * Steps: check cart not empty; copy items; set status="new"; set
     * createdDate=now; calculateSubtotal().
     * 
     * @param cart Cart to create order from
     * @throws EmptyCartException when cart is empty
     */
    public Order(Cart cart) throws EmptyCartException {
        // Step 1: Check cart not empty
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot create order from empty cart");
        }

        // Step 2: Copy items from cart to order items
        this.orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            OrderItem item = new OrderItem(product, cartItem.getQuantity(), product.getCurrentPrice());
            this.orderItems.add(item);
        }

        // Step 3: Set status = "new"
        this.status = "new";

        // Step 4: Set createdDate = now
        this.createdDate = LocalDateTime.now();

        // Step 5: Calculate subtotal
        this.subtotal = calculateSubtotal();
        this.deliveryFee = 0.0f;
        this.totalAmount = this.subtotal;
    }

    // ==================== Getters and Setters ====================

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public float getVat() {
        return vat;
    }

    public void setVat(float vat) {
        this.vat = vat;
    }

    public float getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(float deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        if (invoice != null) {
            this.invoiceId = invoice.getInvoiceId();
        }
    }

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
        if (transactionInfo != null) {
            this.paymentTransactionId = transactionInfo.getTransactionId();
        }
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    // ==================== Business Methods (Table 2) ====================

    /**
     * #2: setDeliveryInfo(deliveryInfo: DeliveryInfo): void
     * Purpose: Attach delivery info to the order (sequence 2.2.4).
     * Steps: validate not null; set to field; read delivery fee; call
     * calculateTotalAmount().
     * 
     * @param deliveryInfo validated delivery info from the delivery form
     * @throws InvalidDeliveryInfoException when delivery info is null or invalid
     */
    public void setDeliveryInfo(DeliveryInfo deliveryInfo) throws InvalidDeliveryInfoException {
        // Step 1: Validate not null
        if (deliveryInfo == null) {
            throw new InvalidDeliveryInfoException("Delivery info cannot be null");
        }

        // Validate delivery info
        if (!deliveryInfo.checkValidityOfDeliveryInfo()) {
            throw new InvalidDeliveryInfoException("Delivery info is invalid");
        }

        // Step 2: Set to field
        this.deliveryInfo = deliveryInfo;

        // Step 3: Calculate total weight and delivery fee
        float totalWeight = calculateTotalWeight();
        this.deliveryFee = deliveryInfo.calculateDeliveryFee(totalWeight);

        // Step 4: Call calculateTotalAmount()
        calculateTotalAmount();
    }

    /**
     * #3: saveOrder(invoice: Invoice): void
     * Purpose: Persist this order together with invoice after payment (sequence
     * 3.1.3).
     * Steps: link invoice; call repository/DAO; throw OrderSaveFailedException on
     * error.
     * 
     * @param invoice invoice created by controller
     * @throws OrderSaveFailedException when persisting fails
     */
    public void saveOrder(Invoice invoice) throws OrderSaveFailedException {
        try {
            // Step 1: Link invoice
            if (invoice == null) {
                throw new OrderSaveFailedException("Invoice cannot be null");
            }

            this.invoice = invoice;
            this.invoiceId = invoice.getInvoiceId();

            // Step 2: Call repository/DAO to persist
            // TODO: Implement persistence logic
            // orderRepository.save(this);

            System.out.println("Order saved successfully with invoice ID: " + invoiceId);

        } catch (Exception e) {
            // Step 3: Throw OrderSaveFailedException on error
            throw new OrderSaveFailedException("Failed to save order: " + e.getMessage(), e);
        }
    }

    /**
     * #4: setPendingStatus(): void
     * Purpose: Set the order status to "pending" (sequence 5.1).
     * Steps: this.status = "pending".
     */
    public void setPendingStatus() {
        this.status = "pending";
    }

    /**
     * #5: calculateSubtotal(): float
     * Purpose: Calculate subtotal from orderItems.
     * 
     * @return subtotal amount
     */
    public float calculateSubtotal() {
        if (orderItems == null || orderItems.isEmpty()) {
            this.subtotal = 0.0f;
            return this.subtotal;
        }

        float total = 0.0f;
        for (OrderItem item : orderItems) {
            total += item.getLineTotal();
        }

        this.subtotal = total;
        return this.subtotal;
    }

    /**
     * #6: calculateTotalAmount(): float
     * Purpose: Calculate final amount = subtotal + VAT + deliveryFee.
     * Note: VAT (10%) is calculated on subtotal only, NOT on delivery fee
     * 
     * @return total amount
     */
    public float calculateTotalAmount() {
        // Calculate VAT (10% on products only)
        this.vat = this.subtotal * VAT_RATE;

        // Total = subtotal + VAT + delivery fee
        this.totalAmount = this.subtotal + this.vat + this.deliveryFee;
        return this.totalAmount;
    }

    /**
     * #7: calculateTotalWeight(): float
     * Purpose: Calculate total weight from all order items for delivery fee
     * calculation.
     * 
     * @return total weight in kg
     */
    public float calculateTotalWeight() {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0.0f;
        }

        float totalWeight = 0.0f;
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            if (product != null && product.getWeight() != null) {
                // Weight per product * quantity
                totalWeight += product.getWeight() * item.getQuantity();
            }
        }

        return totalWeight;
    }

    // ==================== Additional Helper Methods ====================

    /**
     * Add an order item
     */
    public void addOrderItem(OrderItem item) {
        if (item != null) {
            this.orderItems.add(item);
            calculateSubtotal();
            calculateTotalAmount();
        }
    }

    /**
     * Remove an order item
     */
    public void removeOrderItem(OrderItem item) {
        if (this.orderItems.remove(item)) {
            calculateSubtotal();
            calculateTotalAmount();
        }
    }

    /**
     * Cancel order
     */
    public boolean cancelOrder() {
        if ("new".equals(status) || "pending".equals(status)) {
            this.status = "canceled";
            return true;
        }
        return false;
    }

    /**
     * Validate order is ready for payment
     */
    public boolean validateOrder() {
        // Check if order has items
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }

        // Check if delivery info is set
        if (deliveryInfo == null) {
            return false;
        }

        // Validate delivery info
        if (!deliveryInfo.checkValidityOfDeliveryInfo()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                ", subtotal=" + subtotal +
                ", deliveryFee=" + deliveryFee +
                ", totalAmount=" + totalAmount +
                ", orderItemsCount=" + (orderItems != null ? orderItems.size() : 0) +
                ", hasDeliveryInfo=" + (deliveryInfo != null) +
                ", invoiceId=" + invoiceId +
                ", paymentTransactionId=" + paymentTransactionId +
                '}';
    }
}
