package com.aimsfx.controller;

import com.aimsfx.exception.*;
import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.service.UserService;
import com.aimsfx.utils.SessionManager;
import javafx.collections.ObservableList;

import java.util.Optional;
import java.util.Set;

/**
 * UserController - Coordinates between View and Service layers
 * 
 * RESPONSIBILITIES:
 * - Receive requests from Views
 * - Delegate business logic to UserService
 * - Manage user session (login/logout)
 * 
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Only handles coordination
 * - Dependency Inversion: Uses service abstraction
 * - Thin Controller: Most logic delegated to Service layer
 */
public class UserController {
    private static UserController instance;
    private final UserService userService;
    private final SessionManager sessionManager;

    private UserController() {
        this.userService = UserService.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }

    // Constructor for dependency injection (testing)
    public UserController(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
    }

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    // For testing - reset instance
    public static void resetInstance() {
        instance = null;
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Login user with username and password
     * 
     * @param username Username
     * @param password Plain text password
     * @return true if login successful
     */
    public boolean login(String username, String password) {
        try {
            User user = userService.authenticate(username, password);
            sessionManager.login(user);
            return true;
        } catch (UserNotFoundException | UnauthorizedAccessException e) {
            return false;
        }
    }

    /**
     * Logout current user
     */
    public void logout() {
        sessionManager.logout();
    }

    /**
     * Get currently logged-in user
     * 
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    /**
     * Check if user is logged in
     * 
     * @return true if logged in
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    // ==================== USER CRUD ====================

    /**
     * Create new user (administrators only)
     * 
     * @param username Username
     * @param password Plain text password
     * @param roles    User roles (can be multiple)
     * @param fullName Full name
     * @return Created user
     * @throws DuplicateUsernameException  if username exists
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserValidationException     if validation fails
     * @throws InvalidPasswordException    if password validation fails
     */
    public User createUser(String username, String password, Set<UserRole> roles, String fullName) {
        return userService.createUser(username, password, roles, fullName);
    }

    /**
     * Update user information (administrators only)
     * 
     * @param userId      User ID
     * @param newPassword New password (optional, null to keep current)
     * @param roles       New roles (can be multiple)
     * @param fullName    New full name
     * @return Updated user
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserNotFoundException       if user not found
     * @throws UserValidationException     if validation fails
     * @throws InvalidPasswordException    if password validation fails
     */
    public User updateUser(Long userId, String newPassword, Set<UserRole> roles, String fullName) {
        return userService.updateUser(userId, newPassword, roles, fullName);
    }

    /**
     * Delete user (administrators only)
     * 
     * @param userId User ID
     * @return true if deleted successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws SelfOperationException      if trying to delete own account
     * @throws UserNotFoundException       if user not found
     */
    public boolean deleteUser(Long userId) {
        return userService.deleteUser(userId);
    }

    /**
     * Get user by ID (administrators only)
     * 
     * @param userId User ID
     * @return Optional containing user if found
     * @throws UnauthorizedAccessException if current user is not administrator
     */
    public Optional<User> getUserById(Long userId) {
        try {
            return Optional.of(userService.getUserById(userId));
        } catch (UserNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all users (administrators only)
     * 
     * @return Observable list of all users
     * @throws UnauthorizedAccessException if current user is not administrator
     */
    public ObservableList<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Get users by role (administrators only)
     * 
     * @param role User role
     * @return Observable list of users with specified role
     * @throws UnauthorizedAccessException if current user is not administrator
     */
    public ObservableList<User> getUsersByRole(UserRole role) {
        return userService.getUsersByRole(role);
    }

    // ==================== PASSWORD MANAGEMENT ====================

    /**
     * Change password for current user
     * 
     * @param oldPassword Old password (plain text)
     * @param newPassword New password (plain text)
     * @return true if password changed successfully
     * @throws UnauthorizedAccessException if not logged in
     * @throws InvalidPasswordException    if old password incorrect or validation
     *                                     fails
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        return userService.changePassword(oldPassword, newPassword);
    }

    /**
     * Reset user password (administrators only)
     * 
     * @param userId      User ID
     * @param newPassword New password (plain text)
     * @return true if password reset successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserNotFoundException       if user not found
     * @throws InvalidPasswordException    if password validation fails
     */
    public boolean resetPassword(Long userId, String newPassword) {
        return userService.resetPassword(userId, newPassword);
    }

    // ==================== USER STATUS MANAGEMENT ====================

    /**
     * Block user (administrators only)
     * 
     * @param userId User ID
     * @return true if blocked successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws SelfOperationException      if trying to block own account
     * @throws UserNotFoundException       if user not found
     */
    public boolean blockUser(Long userId) {
        return userService.blockUser(userId);
    }

    /**
     * Unblock user (administrators only)
     * 
     * @param userId User ID
     * @return true if unblocked successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserNotFoundException       if user not found
     */
    public boolean unblockUser(Long userId) {
        return userService.unblockUser(userId);
    }

    // ==================== UTILITY ====================

    /**
     * Hash password using SHA-256 (delegates to service)
     * 
     * @param password Plain text password
     * @return Hashed password (hex string)
     */
    public String hashPassword(String password) {
        return userService.hashPassword(password);
    }
}
