package com.aimsfx.model;

/**
 * UserRole - Enum representing user roles in the system
 * 
 * ROLES:
 * - PRODUCT_MANAGER: Can add, view, edit, delete products
 * - ADMINISTRATOR: Can manage users, reset passwords, block/unblock users
 * 
 * Note: Customers don't need to log in, so there's no CUSTOMER role
 */
public enum UserRole {
    PRODUCT_MANAGER,
    ADMINISTRATOR;
    
    @Override
    public String toString() {
        return name();
    }
    
    public static UserRole fromString(String role) {
        if (role == null) return PRODUCT_MANAGER;
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PRODUCT_MANAGER;
        }
    }
}
