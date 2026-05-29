package com.aimsfx.exception;

import java.util.List;

/**
 * Exception thrown when bulk delete validation fails
 * 
 * Contains information about:
 * - Products with stock > 0 (cannot delete)
 * - Deletion quota exceeded
 */
public class BulkDeleteValidationException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public enum ErrorType {
        STOCK_NOT_ZERO,      // Some products have stock > 0
        QUOTA_EXCEEDED       // Daily deletion quota exceeded
    }
    
    private final ErrorType errorType;
    private final List<String> productsWithStock;
    private final int remainingQuota;
    private final int requestedCount;
    
    /**
     * Constructor for STOCK_NOT_ZERO error
     */
    public BulkDeleteValidationException(List<String> productsWithStock) {
        super("Cannot delete products with stock > 0");
        this.errorType = ErrorType.STOCK_NOT_ZERO;
        this.productsWithStock = productsWithStock;
        this.remainingQuota = 0;
        this.requestedCount = 0;
    }
    
    /**
     * Constructor for QUOTA_EXCEEDED error
     */
    public BulkDeleteValidationException(int remainingQuota, int requestedCount) {
        super("Daily deletion quota exceeded");
        this.errorType = ErrorType.QUOTA_EXCEEDED;
        this.productsWithStock = null;
        this.remainingQuota = remainingQuota;
        this.requestedCount = requestedCount;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public List<String> getProductsWithStock() {
        return productsWithStock;
    }
    
    public int getRemainingQuota() {
        return remainingQuota;
    }
    
    public int getRequestedCount() {
        return requestedCount;
    }
}
