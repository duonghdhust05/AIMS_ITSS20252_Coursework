package com.aimsfx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TransactionInfo Class
 * Purpose: Represents a payment transaction with all details and metadata.
 * 
 * ✅ SOLID COMPLIANCE - Week 11 Audit ✅
 * 
 * ✅ SRP COMPLIANT: Single responsibility - Track payment transaction state
 * ✅ OCP COMPLIANT: Can extend TransactionStatus enum or add new fields
 * ✅ LSP COMPLIANT: No inheritance hierarchy (N/A)
 * ✅ ISP COMPLIANT: No interfaces imposed (N/A)
 * ✅ DIP COMPLIANT: Only stores IDs (not object references) - minimal coupling
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods relate to transaction management: creation, status updates, persistence
 * - Single responsibility: Track payment transaction state and metadata
 * - Helper methods (isSuccessful, isFailed) support main transaction operations
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with Order/Invoice (orderId, invoiceId attributes)
 *    - Stores: Integer orderId, Integer invoiceId (just IDs, not objects)
 *    - Type: Data coupling - only primitive/simple data (IDs)
 *    - Justification: Needs to link transaction to order/invoice for tracking
 *    - Note: Does NOT hold object references, only IDs → Minimal coupling
 * 
 * 2. NO Coupling with other domain classes
 *    - Independent transaction record
 *    - Uses only built-in Java types (BigDecimal, String, LocalDateTime, Map)
 * 
 * Overall: VERY LOW COUPLING - Only stores IDs for reference, no object dependencies
 */
public class TransactionInfo {
    
    // Enum for Transaction Status
    public enum TransactionStatus {
        PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED, CANCELLED
    }
    
    // Attributes (Table 1)
    private String transactionId;               // #1: Internal unique ID (UUID)
    private Integer orderId;                    // #2: Associated order identifier
    private Integer invoiceId;                  // #3: Associated invoice identifier
    private BigDecimal amount;                  // #4: Captured/authorized amount (default 0)
    private String currency;                    // #5: ISO currency code (default "VND")
    private String paymentMethod;               // #6: Credit Card, PayPal, VietQR, etc.
    private TransactionStatus status;           // #7: Transaction status (default PENDING)
    private String failureReason;               // #8: Reason when status = FAILED
    private LocalDateTime createdAt;            // #9: Creation timestamp (default now)
    private LocalDateTime updatedAt;            // #10: Last update timestamp (default now)
    private Map<String, String> meta;           // #12: Optional metadata
    
    // Additional legacy fields
    private String transactionCode;
    private String cardNumber;
    private String bankCode;

    // ==================== Constructors ====================
    
    /**
     * Default constructor
     */
    public TransactionInfo() {
        this.transactionId = UUID.randomUUID().toString();
        this.amount = BigDecimal.ZERO;
        this.currency = "VND";
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.meta = new HashMap<>();
    }
    
    /**
     * #1: transactionInfo(orderId, invoiceId, paymentMethod, amount, currency) - Constructor
     * Purpose: Initializes a new transaction with PENDING status, sets timestamps, 
     * and copies core identifiers (order/invoice).
     * 
     * @param orderId Associated order identifier
     * @param invoiceId Associated invoice identifier (can be null)
     * @param paymentMethod Payment method (Credit Card, PayPal, VietQR, etc.)
     * @param amount Transaction amount
     * @param currency ISO currency code (default "VND")
     */
    public TransactionInfo(Integer orderId, Integer invoiceId, String paymentMethod, 
                          BigDecimal amount, String currency) {
        this();  // Call default constructor for defaults
        this.orderId = orderId;
        this.invoiceId = invoiceId;
        this.paymentMethod = paymentMethod;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "VND";
    }
    
    /**
     * Constructor with default currency (VND)
     */
    public TransactionInfo(Integer orderId, Integer invoiceId, String paymentMethod, 
                          BigDecimal amount) {
        this(orderId, invoiceId, paymentMethod, amount, "VND");
    }
    
    /**
     * Legacy constructor for compatibility
     */
    public TransactionInfo(int transactionId, int orderId, BigDecimal amount, String paymentMethod) {
        this();
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    
    // ==================== Getters and Setters ====================

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    // Legacy support - return hash code as int
    public int getTransactionIdAsInt() {
        return transactionId != null ? transactionId.hashCode() : 0;
    }
    
    public void setTransactionId(int id) {
        // For legacy support - not recommended
        this.transactionId = String.valueOf(id);
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.updatedAt = LocalDateTime.now();
    }

    public TransactionStatus getStatus() {
        return status;
    }
    
    public String getStatusString() {
        return status != null ? status.name() : null;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setStatus(String status) {
        try {
            this.status = TransactionStatus.valueOf(status.toUpperCase());
            this.updatedAt = LocalDateTime.now();
        } catch (IllegalArgumentException e) {
            // Handle legacy status values
            if ("SUCCESS".equals(status) || "COMPLETED".equals(status)) {
                this.status = TransactionStatus.CAPTURED;
            } else {
                this.status = TransactionStatus.FAILED;
            }
        }
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addMeta(String key, String value) {
        if (this.meta == null) {
            this.meta = new HashMap<>();
        }
        this.meta.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    // ==================== Business Methods (Table 2) ====================
    
    /**
     * #2: save(): void
     * Purpose: Persist the transaction into storage.
     * Throws on failure.
     */
    public void save() {
        try {
            // Update timestamp
            this.updatedAt = LocalDateTime.now();
            boolean success = com.aimsfx.repository.TransactionRepository.getInstance().save(this);
            
            if (!success) {
                throw new RuntimeException("TransactionRepository failed to save the transaction to the database.");
            }
            
            System.out.println("Transaction saved: " + this.transactionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save transaction: " + e.getMessage(), e);
        }
    }
    
    // ==================== Additional Helper Methods ====================
    
    public boolean isSuccessful() {
        return status == TransactionStatus.CAPTURED || status == TransactionStatus.AUTHORIZED;
    }

    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED || status == TransactionStatus.CANCELLED;
    }
    
    public boolean isRefunded() {
        return status == TransactionStatus.REFUNDED;
    }

    public void markAsSuccess() {
        this.status = TransactionStatus.CAPTURED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsFailed() {
        markAsFailed("Transaction failed");
    }
    
    public void markAsAuthorized() {
        this.status = TransactionStatus.AUTHORIZED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsCancelled(String reason) {
        this.status = TransactionStatus.CANCELLED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsRefunded(String reason) {
        this.status = TransactionStatus.REFUNDED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "TransactionInfo{" +
                "transactionId='" + transactionId + '\'' +
                ", orderId=" + orderId +
                ", invoiceId=" + invoiceId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status=" + status +
                ", failureReason='" + failureReason + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
