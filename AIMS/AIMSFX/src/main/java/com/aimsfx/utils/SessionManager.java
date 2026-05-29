package com.aimsfx.utils;

import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;

import java.util.Set;

/**
 * SessionManager - Singleton for managing user session
 * 
 * RESPONSIBILITIES:
 * - Track currently logged-in user
 * - Provide session state (logged in / logged out)
 * - Role-based access control checks
 * 
 * AUTHORIZATION RULES:
 * - Product Management: Requires PRODUCT_MANAGER role explicitly
 * - User Management: Requires ADMINISTRATOR role explicitly
 * - Users can have multiple roles
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Login user
     * @param user User to login
     */
    public void login(User user) {
        this.currentUser = user;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Check if user is logged in
     * @return true if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current logged-in user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get current user's roles
     * @return Set of UserRoles or null if not logged in
     */
    public Set<UserRole> getCurrentRoles() {
        return currentUser != null ? currentUser.getRoles() : null;
    }
    
    /**
     * Check if current user has specified role
     * @param role Role to check
     * @return true if user has role
     */
    public boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.hasRole(role);
    }
    
    /**
     * Check if current user is administrator
     * @return true if administrator
     */
    public boolean isAdministrator() {
        return hasRole(UserRole.ADMINISTRATOR);
    }
    
    /**
     * Check if current user is product manager
     * @return true if product manager
     */
    public boolean isProductManager() {
        return hasRole(UserRole.PRODUCT_MANAGER);
    }
    
    /**
     * Check if current user can manage products
     * IMPORTANT: Requires PRODUCT_MANAGER role explicitly
     * Administrator role alone is NOT sufficient
     * @return true if user has PRODUCT_MANAGER role
     */
    public boolean canManageProducts() {
        return isProductManager();
    }
    

    /**
     * Check if current user can manage orders (review/approve/reject).
     * Minimal rule: Product Manager role.
     */
    public boolean canManageOrders() {
        return isProductManager();
    }
    
    /**
     * Check if current user can manage users
     * @return true if user is administrator
     */
    public boolean canManageUsers() {
        return isAdministrator();
    }
}
