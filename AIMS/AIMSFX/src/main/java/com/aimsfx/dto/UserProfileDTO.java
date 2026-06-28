package com.aimsfx.dto;

import com.aimsfx.model.UserRole;
import com.aimsfx.model.UserStatus;
import com.aimsfx.model.User;
import java.util.Set;

/**
 * UserProfileDTO - Data Transfer Object for user information
 * Purpose: Returns user details without exposing sensitive data like password hashes.
 */
public class UserProfileDTO {
    private Long userId;
    private String username;
    private String fullName;
    private Set<UserRole> roles;
    private UserStatus status;

    public UserProfileDTO() {
    }

    public UserProfileDTO(User user) {
        if (user != null) {
            this.userId = user.getUserId();
            this.username = user.getUsername();
            this.fullName = user.getFullName();
            this.roles = user.getRoles();
            this.status = user.getStatus();
        }
    }

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
}
