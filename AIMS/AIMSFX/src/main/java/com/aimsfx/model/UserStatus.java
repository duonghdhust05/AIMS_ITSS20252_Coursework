package com.aimsfx.model;

/**
 * UserStatus - Enum representing user account status
 */
public enum UserStatus {
    ACTIVE,
    BLOCKED;
    
    @Override
    public String toString() {
        return name();
    }
    
    public static UserStatus fromString(String status) {
        if (status == null) return ACTIVE;
        try {
            return UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}
