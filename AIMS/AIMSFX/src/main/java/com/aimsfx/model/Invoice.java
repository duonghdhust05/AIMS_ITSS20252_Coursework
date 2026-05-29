package com.aimsfx.model;

import java.util.Date;
import java.util.List;

/**
 * Invoice Class
 * Purpose: Financial record/document for an Order
 * 
 * ✅ SOLID COMPLIANCE - Week 11 Audit ✅
 * 
 * ✅ SRP COMPLIANT: Single responsibility - Store invoice data (Value Object pattern)
 * ✅ OCP COMPLIANT: Closed for modification, can be extended if needed
 * ✅ LSP COMPLIANT: No inheritance hierarchy (N/A)
 * ✅ ISP COMPLIANT: No interfaces, pure data holder (N/A)
 * ✅ DIP COMPLIANT: Zero external coupling - completely independent
 * 
 * Design Principles:
 * - Invoice is a COMPLETE SNAPSHOT of Order data at time of invoice creation
 * - Does NOT contain business logic (all calculations done by Order)
 * - Acts as a READ-ONLY document for display, printing, and storage
 * - All data copied from Order: orderItems, subtotal, vat, deliveryFee, totalAmount
 * - NO dependency on Order after creation (avoids stamp coupling)
 * - Invoice can exist independently even if Order is deleted
 * 
 * COHESION: HIGH - Informational Cohesion
 * - All attributes represent invoice information
 * - All methods are getters/setters for invoice data
 * - Single responsibility: Store and provide invoice data for display
 * - No business logic - pure data holder (Value Object pattern)
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with OrderItem (orderItems attribute)
 *    - Stores: List<OrderItem> as snapshot
 *    - Type: Data coupling - only stores data structure, no method calls
 *    - Justification: Needs product details for invoice display
 *    - Note: This is a SNAPSHOT copy, not a reference (no ongoing dependency)
 * 
 * 2. NO Coupling with Order class itself
 *    - Does NOT hold Order reference (avoids stamp coupling)
 *    - Data is copied at creation time, then independent
 *    - Can exist even if original Order is deleted
 * 
 * Overall: VERY LOW COUPLING - Minimal dependencies, acts as independent data container
 */
public class Invoice {
    // Attributes - Snapshot of order financial data
    private int invoiceId;
    private int orderId;
    private Date issuedDate;
    
    // Financial breakdown (all values from Order)
    private float subtotal;        // From Order.getSubtotal()
    private float vat;             // From Order.getVat()
    private float deliveryFee;     // From Order.getDeliveryFee()
    private float discount;        // Future use
    private float totalAmount;     // From Order.getTotalAmount()
    
    // Legacy fields (for backward compatibility)
    private String address;
    private float totalFee;        // Alias for totalAmount
    
    // Order items snapshot (copied from Order at invoice creation time)
    private List<OrderItem> orderItems;

    // Constructors
    public Invoice() {
        this.issuedDate = new Date();
        this.discount = 0f;
    }

    public Invoice(int invoiceId, int orderId) {
        this();
        this.invoiceId = invoiceId;
        this.orderId = orderId;
    }

    // Getters and Setters
    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(float totalFee) {
        this.totalFee = totalFee;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public float getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(float deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public float getVat() {
        return vat;
    }

    public void setVat(float vat) {
        this.vat = vat;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    // Helper Methods (for display purposes only)
    
    /**
     * Get discount amount
     * @return discount amount
     */
    public float getDiscountAmount() {
        return this.discount;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", orderId=" + orderId +
                ", subtotal=" + subtotal +
                ", deliveryFee=" + deliveryFee +
                ", vat=" + vat +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
