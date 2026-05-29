package com.aimsfx.exception;

/**
 * Exception thrown when a user is not found in the system
 */
public class UserNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
	private final Long userId;
    private final String username;
    
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
        this.username = null;
    }
    
    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
        this.userId = null;
        this.username = username;
    }
    
    public UserNotFoundException(String message, Long userId) {
        super(message);
        this.userId = userId;
        this.username = null;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
}
