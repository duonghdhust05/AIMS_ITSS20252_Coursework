package com.aimsfx.dto;

import java.math.BigDecimal;

/**
 * PaymentRequestDTO - Data Transfer Object for initiating a payment
 * Purpose: Ensures only safe inputs are accepted from the client, preventing tampering
 * with internal transaction IDs or statuses.
 */
public class PaymentRequestDTO {
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String currency;

    public PaymentRequestDTO() {
    }

    public PaymentRequestDTO(Integer orderId, BigDecimal amount, String paymentMethod, String currency) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
