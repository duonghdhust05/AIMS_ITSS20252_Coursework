package com.aimsfx.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User - Model class representing a user in the system
 * 
 * AUTHENTICATION & AUTHORIZATION:
 * - username: Unique identifier for login
 * - password: Hashed password (SHA-256)
 * - roles: Set of UserRole enums (PRODUCT_MANAGER, ADMINISTRATOR)
 * - status: UserStatus enum (ACTIVE, BLOCKED)
 */
public class User {
    private Long userId;
    private String username;
    private String password;  // Hashed
    private Set<UserRole> roles;
    private UserStatus status;
    private String fullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public User() {
        this.roles = new HashSet<>();
        this.roles.add(UserRole.PRODUCT_MANAGER);
        this.status = UserStatus.ACTIVE;
    }
    
    public User(String username, String password, Set<UserRole> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles != null ? roles : new HashSet<>();
        this.status = UserStatus.ACTIVE;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Set<UserRole> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }
    
    public boolean hasRole(UserRole role) {
        return this.roles != null && this.roles.contains(role);
    }
    
    public boolean isAdministrator() {
        return hasRole(UserRole.ADMINISTRATOR);
    }
    
    public boolean isProductManager() {
        return hasRole(UserRole.PRODUCT_MANAGER);
    }
    
    public void addRole(UserRole role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }
    
    public void removeRole(UserRole role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", status=" + status +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}

