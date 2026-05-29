package com.aimsfx.model;

import java.time.LocalDateTime;

/**
 * StockChangeLog - Model class representing a stock change history record
 * 
 * PURPOSE: Track all changes to product stock levels with reasons
 * 
 * BUSINESS RULE: Every stock change must be logged with a reason
 */
public class StockChangeLog {
    private Long id;
    private String barcode;
    private Integer fromStock;
    private Integer toStock;
    private String changeReason;
    private LocalDateTime changedAt;
    
    // Constructors
    public StockChangeLog() {
        this.changedAt = LocalDateTime.now();
    }
    
    public StockChangeLog(String barcode, Integer fromStock, Integer toStock, String changeReason) {
        this();
        this.barcode = barcode;
        this.fromStock = fromStock;
        this.toStock = toStock;
        this.changeReason = changeReason;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public Integer getFromStock() {
        return fromStock;
    }
    
    public void setFromStock(Integer fromStock) {
        this.fromStock = fromStock;
    }
    
    public Integer getToStock() {
        return toStock;
    }
    
    public void setToStock(Integer toStock) {
        this.toStock = toStock;
    }
    
    public String getChangeReason() {
        return changeReason;
    }
    
    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
    public Integer getStockChange() {
        return toStock - fromStock;
    }
    
    @Override
    public String toString() {
        return "StockChangeLog{" +
                "id=" + id +
                ", barcode='" + barcode + '\'' +
                ", fromStock=" + fromStock +
                ", toStock=" + toStock +
                ", change=" + getStockChange() +
                ", reason='" + changeReason + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
