package com.aimsfx.exception;

/**
 * Exception thrown when user input validation fails
 */
public class UserValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
	private final String field;
    private final String validationMessage;
    
    public UserValidationException(String field, String validationMessage) {
        super(validationMessage);
        this.field = field;
        this.validationMessage = validationMessage;
    }
    
    public UserValidationException(String validationMessage) {
        super(validationMessage);
        this.field = null;
        this.validationMessage = validationMessage;
    }
    
    public String getField() {
        return field;
    }
    
    public String getValidationMessage() {
        return validationMessage;
    }
}
