package com.aimsfx.exception;

/**
 * Exception thrown when password validation or verification fails
 */
public class InvalidPasswordException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

	public enum Reason {
        INCORRECT_PASSWORD("Current password is incorrect"),
        TOO_SHORT("Password must be at least 6 characters"),
        PASSWORDS_NOT_MATCH("Passwords do not match"),
        SAME_AS_OLD("New password must be different from current password"),
        EMPTY("Password cannot be empty");
        
        private final String message;
        
        Reason(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private final Reason reason;
    
    public InvalidPasswordException(Reason reason) {
        super(reason.getMessage());
        this.reason = reason;
    }
    
    public InvalidPasswordException(String message) {
        super(message);
        this.reason = null;
    }
    
    public Reason getReason() {
        return reason;
    }
}
