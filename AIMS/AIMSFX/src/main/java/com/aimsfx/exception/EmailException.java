package com.aimsfx.exception;

/**
 * Custom exception for email service errors
 * 
 * This exception is thrown when email sending fails due to:
 * - SMTP connection failure
 * - Authentication failure
 * - Invalid recipient email
 * - Network timeout
 * 
 * @author AIMS Team
 * @version 1.0
 */
public class EmailException extends Exception {
    
    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new EmailException with the specified detail message
     * 
     * @param message the detail message
     */
    public EmailException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new EmailException with the specified detail message and cause
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
