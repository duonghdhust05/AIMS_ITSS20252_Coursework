package com.aimsfx.exception;

/**
 * Exception thrown when attempting to create or update a user with a username that already exists
 */
public class DuplicateUsernameException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
	private final String username;
    
    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
}
