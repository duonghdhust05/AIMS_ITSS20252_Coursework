package com.aimsfx.dto;

import com.aimsfx.model.TransactionInfo;
import java.math.BigDecimal;

/**
 * TransactionResponseDTO - Data Transfer Object for transaction results
 * Purpose: Returns transaction status to the UI while hiding internal fields 
 * like gateway metadata, bank codes, or internal error stack traces.
 */
public class TransactionResponseDTO {
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String displayMessage;

    public TransactionResponseDTO() {
    }

    public TransactionResponseDTO(TransactionInfo transaction) {
        if (transaction != null) {
            this.transactionId = transaction.getTransactionId();
            this.amount = transaction.getAmount();
            this.currency = transaction.getCurrency();
            this.status = transaction.getStatusString();
            this.paymentMethod = transaction.getPaymentMethod();
            
            if (transaction.isSuccessful()) {
                this.displayMessage = "Payment successful";
            } else if (transaction.isFailed()) {
                this.displayMessage = "Payment failed. Please try again.";
            } else {
                this.displayMessage = "Payment is " + this.status.toLowerCase();
            }
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }
}
