package com.aimsfx.service;

import com.aimsfx.exception.*;
import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.model.UserStatus;
import com.aimsfx.repository.DatabaseUserRepository;
import com.aimsfx.repository.UserRepository;
import com.aimsfx.utils.SessionManager;
import com.aimsfx.validator.UserValidator;
import com.aimsfx.dto.UserLoginDTO;
import com.aimsfx.dto.UserProfileDTO;
import com.aimsfx.dto.UserRegisterDTO;
import javafx.collections.ObservableList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;

/**
 * UserService - Business logic for user management
 * 
 * RESPONSIBILITIES:
 * - User CRUD operations with business rules
 * - Password management (hashing, validation)
 * - User status management (block/unblock)
 * - Authorization checks
 * 
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Only handles user business logic
 * - Dependency Inversion: Depends on abstractions (UserRepository interface)
 * - Separation of Concerns: Delegates validation to UserValidator
 */
public class UserService {
    
    private static UserService instance;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final UserValidator userValidator;
    
    public UserService() {
        this.userRepository = new DatabaseUserRepository();
        this.sessionManager = SessionManager.getInstance();
        this.userValidator = new UserValidator();
    }
    
    // Constructor for dependency injection (testing)
    public UserService(UserRepository userRepository, SessionManager sessionManager, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.userValidator = userValidator;
    }
    
    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    // ==================== AUTHENTICATION ====================
    
    /**
     * Authenticate user with username and password
     * @param username Username
     * @param password Plain text password
     * @return Authenticated user
     * @throws UserNotFoundException if user not found or credentials invalid
     * @throws UnauthorizedAccessException if user is blocked
     */
    public User authenticate(String username, String password) {
        String hashedPassword = hashPassword(password);
        Optional<User> userOpt = userRepository.authenticate(username, hashedPassword);
        
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Invalid username or password");
        }
        
        User user = userOpt.get();
        if (!user.isActive()) {
            throw new UnauthorizedAccessException("Account is blocked");
        }
        
        return user;
    }
    
    /**
     * Authenticate user via DTO (Optimized for network/API payload)
     * @param loginDTO Contains username and password
     * @return UserProfileDTO containing non-sensitive user info
     */
    public UserProfileDTO authenticateWithDTO(UserLoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new java.lang.IllegalArgumentException("Login credentials cannot be null");
        }
        User authenticatedUser = authenticate(loginDTO.getUsername(), loginDTO.getPassword());
        return new UserProfileDTO(authenticatedUser);
    }
    
    // ==================== USER CRUD ====================
    
    /**
     * Create new user
     * @param username Username
     * @param password Plain text password
     * @param roles User roles
     * @param fullName Full name
     * @return Created user
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws DuplicateUsernameException if username already exists
     * @throws UserValidationException if validation fails
     */
    public User createUser(String username, String password, Set<UserRole> roles, String fullName) {
        checkAdminAuthorization("create users");
        
        // Validate input
        userValidator.validateUsername(username);
        userValidator.validatePassword(password);
        userValidator.validateFullName(fullName);
        userValidator.validateRoles(roles);
        
        // Check username uniqueness
        if (userRepository.usernameExists(username.trim())) {
            throw new DuplicateUsernameException(username);
        }
        
        // Create user
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(hashPassword(password));
        user.setRoles(roles);
        user.setStatus(UserStatus.ACTIVE);
        user.setFullName(fullName.trim());
        
        return userRepository.save(user);
    }
    
    /**
     * Register new user via DTO (API/Network Optimized)
     * @param registerDTO Contains registration info
     * @param roles User roles to assign
     * @return UserProfileDTO with non-sensitive info
     */
    public UserProfileDTO registerUserWithDTO(UserRegisterDTO registerDTO, Set<UserRole> roles) {
        if (registerDTO == null) throw new java.lang.IllegalArgumentException("Registration data cannot be null");
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new UserValidationException("Passwords do not match");
        }
        User createdUser = createUser(
            registerDTO.getUsername(), 
            registerDTO.getPassword(), 
            roles, 
            registerDTO.getFullName()
        );
        return new UserProfileDTO(createdUser);
    }
    
    /**
     * Update user information
     * @param userId User ID
     * @param newPassword New password (optional, null to keep current)
     * @param roles New roles
     * @param fullName New full name
     * @return Updated user
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserNotFoundException if user not found
     * @throws UserValidationException if validation fails
     * @throws InvalidPasswordException if password validation fails
     */
    public User updateUser(Long userId, String newPassword, Set<UserRole> roles, String fullName) {
        checkAdminAuthorization("update users");
        
        // Validate input
        userValidator.validateFullName(fullName);
        userValidator.validateRoles(roles);
        
        // Validate password only if provided
        if (newPassword != null && !newPassword.isEmpty()) {
            userValidator.validatePassword(newPassword);
        }
        
        // Find user
        User user = findUserById(userId);
        
        // Update user fields
        user.setRoles(roles);
        user.setFullName(fullName.trim());
        
        // Update password only if provided
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(hashPassword(newPassword));
        }
        
        return userRepository.update(user);
    }
    
    /**
     * Delete user
     * @param userId User ID
     * @return true if deleted successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws SelfOperationException if trying to delete own account
     * @throws UserNotFoundException if user not found
     */
    public boolean deleteUser(Long userId) {
        checkAdminAuthorization("delete users");
        checkNotSelfOperation(userId, SelfOperationException.Operation.DELETE);
        
        // Verify user exists
        findUserById(userId);
        
        return userRepository.delete(userId);
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserNotFoundException if user not found
     */
    public User getUserById(Long userId) {
        checkAdminAuthorization("view user details");
        return findUserById(userId);
    }
    
    /**
     * Get all users
     * @return Observable list of all users
     * @throws UnauthorizedAccessException if current user is not administrator
     */
    public ObservableList<User> getAllUsers() {
        checkAdminAuthorization("view all users");
        return userRepository.findAll();
    }
    
    /**
     * Get users by role
     * @param role User role
     * @return Observable list of users with specified role
     * @throws UnauthorizedAccessException if current user is not administrator
     */
    public ObservableList<User> getUsersByRole(UserRole role) {
        checkAdminAuthorization("view users by role");
        return userRepository.findByRole(role);
    }
    
    // ==================== PASSWORD MANAGEMENT ====================
    
    /**
     * Change password for current user
     * @param oldPassword Old password (plain text)
     * @param newPassword New password (plain text)
     * @return true if password changed successfully
     * @throws UnauthorizedAccessException if user is not logged in
     * @throws InvalidPasswordException if old password is incorrect or validation fails
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!sessionManager.isLoggedIn()) {
            throw new UnauthorizedAccessException("User must be logged in to change password");
        }
        
        // Validate new password
        userValidator.validatePassword(newPassword);
        userValidator.validatePasswordChange(oldPassword, newPassword);
        
        User currentUser = sessionManager.getCurrentUser();
        String oldHashed = hashPassword(oldPassword);
        
        // Verify old password
        if (!currentUser.getPassword().equals(oldHashed)) {
            throw new InvalidPasswordException(InvalidPasswordException.Reason.INCORRECT_PASSWORD);
        }
        
        String newHashed = hashPassword(newPassword);
        return userRepository.changePassword(currentUser.getUserId(), newHashed);
    }
    
    /**
     * Reset user password (administrators only)
     * @param userId User ID
     * @param newPassword New password (plain text)
     * @return true if password reset successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserNotFoundException if user not found
     * @throws InvalidPasswordException if password validation fails
     */
    public boolean resetPassword(Long userId, String newPassword) {
        checkAdminAuthorization("reset passwords");
        
        // Validate password
        userValidator.validatePassword(newPassword);
        
        // Verify user exists
        findUserById(userId);
        
        String hashedPassword = hashPassword(newPassword);
        return userRepository.changePassword(userId, hashedPassword);
    }
    
    // ==================== USER STATUS MANAGEMENT ====================
    
    /**
     * Block user
     * @param userId User ID
     * @return true if blocked successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws SelfOperationException if trying to block own account
     * @throws UserNotFoundException if user not found
     */
    public boolean blockUser(Long userId) {
        checkAdminAuthorization("block users");
        checkNotSelfOperation(userId, SelfOperationException.Operation.BLOCK);
        
        // Verify user exists
        findUserById(userId);
        
        return userRepository.blockUser(userId);
    }
    
    /**
     * Unblock user
     * @param userId User ID
     * @return true if unblocked successfully
     * @throws UnauthorizedAccessException if current user is not administrator
     * @throws UserNotFoundException if user not found
     */
    public boolean unblockUser(Long userId) {
        checkAdminAuthorization("unblock users");
        
        // Verify user exists
        findUserById(userId);
        
        return userRepository.unblockUser(userId);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return true if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.usernameExists(username);
    }
    
    /**
     * Hash password using SHA-256
     * @param password Plain text password
     * @return Hashed password (hex string)
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Check if current user is administrator
     * @param action Action being performed (for error message)
     * @throws UnauthorizedAccessException if not administrator
     */
    private void checkAdminAuthorization(String action) {
        if (!sessionManager.isAdministrator()) {
            throw new UnauthorizedAccessException(action, "ADMINISTRATOR");
        }
    }
    
    /**
     * Check if operation is not on self
     * @param userId User ID being operated on
     * @param operation Operation type
     * @throws SelfOperationException if operating on self
     */
    private void checkNotSelfOperation(Long userId, SelfOperationException.Operation operation) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            throw new SelfOperationException(operation);
        }
    }
    
    /**
     * Find user by ID or throw exception
     * @param userId User ID
     * @return User
     * @throws UserNotFoundException if not found
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
