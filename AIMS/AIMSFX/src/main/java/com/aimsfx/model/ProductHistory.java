package com.aimsfx.model;

import java.time.LocalDateTime;

public class ProductHistory {
    private Long historyId;
    private Long productId;
    private String action;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changedAt;
    private String reason;
    
    // Constructors
    public ProductHistory() {
        this.changedAt = LocalDateTime.now();
    }
    
    public ProductHistory(Long productId, String action, String changedBy) {
        this();
        this.productId = productId;
        this.action = action;
        this.changedBy = changedBy;
    }
    
    public ProductHistory(Long productId, String action, String fieldChanged, 
                         String oldValue, String newValue, String changedBy, String reason) {
        this(productId, action, changedBy);
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getFieldChanged() {
        return fieldChanged;
    }
    
    public void setFieldChanged(String fieldChanged) {
        this.fieldChanged = fieldChanged;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    @Override
    public String toString() {
        return String.format("ProductHistory{historyId=%d, productId=%d, action='%s', fieldChanged='%s', changedBy='%s', changedAt=%s}",
                historyId, productId, action, fieldChanged, changedBy, changedAt);
    }
}
