package com.aimsfx.exception;

/**
 * Exception thrown when a user attempts to perform an action they are not authorized to do
 */
public class UnauthorizedAccessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
	private final String requiredRole;
    private final String action;
    
    public UnauthorizedAccessException(String action) {
        super("Unauthorized access: " + action);
        this.requiredRole = null;
        this.action = action;
    }
    
    public UnauthorizedAccessException(String action, String requiredRole) {
        super("Unauthorized access: " + action + ". Required role: " + requiredRole);
        this.requiredRole = requiredRole;
        this.action = action;
    }
    
    public String getRequiredRole() {
        return requiredRole;
    }
    
    public String getAction() {
        return action;
    }
}
