package com.aimsfx.validator;

import com.aimsfx.exception.InvalidPasswordException;
import com.aimsfx.exception.UserValidationException;
import com.aimsfx.model.UserRole;

import java.util.Set;

/**
 * UserValidator - Validates user input data
 * 
 * RESPONSIBILITIES:
 * - Validate username format and length
 * - Validate password requirements
 * - Validate full name
 * - Validate roles selection
 * 
 * DESIGN PRINCIPLE: Single Responsibility
 * - This class only handles validation logic
 * - Throws specific exceptions for different validation failures
 */
public class UserValidator {
    
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final int MAX_FULLNAME_LENGTH = 100;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
    
    /**
     * Validate username
     * @param username Username to validate
     * @throws UserValidationException if validation fails
     */
    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new UserValidationException("username", "Username is required");
        }
        
        String trimmedUsername = username.trim();
        
        if (trimmedUsername.length() < MIN_USERNAME_LENGTH) {
            throw new UserValidationException("username", 
                "Username must be at least " + MIN_USERNAME_LENGTH + " characters");
        }
        
        if (trimmedUsername.length() > MAX_USERNAME_LENGTH) {
            throw new UserValidationException("username", 
                "Username must not exceed " + MAX_USERNAME_LENGTH + " characters");
        }
        
        if (!trimmedUsername.matches(USERNAME_PATTERN)) {
            throw new UserValidationException("username", 
                "Username can only contain letters, numbers, and underscores");
        }
    }
    
    /**
     * Validate password for new user creation
     * @param password Password to validate
     * @throws InvalidPasswordException if validation fails
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException(InvalidPasswordException.Reason.EMPTY);
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException(InvalidPasswordException.Reason.TOO_SHORT);
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must not exceed " + MAX_PASSWORD_LENGTH + " characters");
        }
    }
    
    /**
     * Validate password confirmation
     * @param password Original password
     * @param confirmPassword Confirmation password
     * @throws InvalidPasswordException if passwords don't match
     */
    public void validatePasswordConfirmation(String password, String confirmPassword) {
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            throw new InvalidPasswordException(InvalidPasswordException.Reason.PASSWORDS_NOT_MATCH);
        }
    }
    
    /**
     * Validate password change (old vs new)
     * @param oldPassword Old password
     * @param newPassword New password
     * @throws InvalidPasswordException if new password is same as old
     */
    public void validatePasswordChange(String oldPassword, String newPassword) {
        validatePassword(newPassword);
        
        if (oldPassword != null && oldPassword.equals(newPassword)) {
            throw new InvalidPasswordException(InvalidPasswordException.Reason.SAME_AS_OLD);
        }
    }
    
    /**
     * Validate full name
     * @param fullName Full name to validate
     * @throws UserValidationException if validation fails
     */
    public void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new UserValidationException("fullName", "Full name is required");
        }
        
        if (fullName.trim().length() > MAX_FULLNAME_LENGTH) {
            throw new UserValidationException("fullName", 
                "Full name must not exceed " + MAX_FULLNAME_LENGTH + " characters");
        }
    }
    
    /**
     * Validate roles selection
     * @param roles Set of roles to validate
     * @throws UserValidationException if no roles selected
     */
    public void validateRoles(Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new UserValidationException("roles", "At least one role must be selected");
        }
    }
    
    /**
     * Validate all fields for user creation
     * @param username Username
     * @param password Password
     * @param confirmPassword Password confirmation
     * @param fullName Full name
     * @param roles User roles
     * @throws UserValidationException if any validation fails
     * @throws InvalidPasswordException if password validation fails
     */
    public void validateCreateUser(String username, String password, String confirmPassword, 
                                   String fullName, Set<UserRole> roles) {
        validateUsername(username);
        validatePassword(password);
        validatePasswordConfirmation(password, confirmPassword);
        validateFullName(fullName);
        validateRoles(roles);
    }
    
    /**
     * Validate all fields for user update (no password)
     * @param username Username
     * @param fullName Full name
     * @param roles User roles
     * @throws UserValidationException if any validation fails
     */
    public void validateUpdateUser(String username, String fullName, Set<UserRole> roles) {
        validateUsername(username);
        validateFullName(fullName);
        validateRoles(roles);
    }
}
