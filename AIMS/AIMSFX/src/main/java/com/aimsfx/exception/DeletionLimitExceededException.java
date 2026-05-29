package com.aimsfx.exception;

/**
 * DeletionLimitExceededException - Exception thrown when user exceeds daily deletion limit
 * 
 * BUSINESS RULE: Users can only delete 20 products per day
 */
public class DeletionLimitExceededException extends Exception {
    
    private static final long serialVersionUID = 1L;
	private final int currentCount;
    private final int maxLimit;
    
    public DeletionLimitExceededException(String message, int currentCount, int maxLimit) {
        super(message);
        this.currentCount = currentCount;
        this.maxLimit = maxLimit;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public int getMaxLimit() {
        return maxLimit;
    }
}
