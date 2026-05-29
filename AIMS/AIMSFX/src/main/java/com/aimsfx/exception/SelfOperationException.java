package com.aimsfx.exception;

/**
 * Exception thrown when a user attempts to perform a restricted operation on themselves
 * (e.g., delete own account, block own account)
 */
public class SelfOperationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

	public enum Operation {
        DELETE("Cannot delete your own account"),
        BLOCK("Cannot block your own account");
        
        private final String message;
        
        Operation(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private final Operation operation;
    
    public SelfOperationException(Operation operation) {
        super(operation.getMessage());
        this.operation = operation;
    }
    
    public Operation getOperation() {
        return operation;
    }
}
