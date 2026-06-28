package com.aimsfx.dto;

/**
 * UserRegisterDTO - Data Transfer Object for user registration
 * Purpose: Encapsulates fields required to create a new user account.
 */
public class UserRegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;
    private String fullName;

    public UserRegisterDTO() {
    }

    public UserRegisterDTO(String username, String password, String confirmPassword, String fullName) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.fullName = fullName;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
