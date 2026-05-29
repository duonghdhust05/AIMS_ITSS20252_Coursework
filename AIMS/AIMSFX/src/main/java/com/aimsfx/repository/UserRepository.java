package com.aimsfx.repository;

import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import javafx.collections.ObservableList;

import java.util.Optional;

/**
 * UserRepository - Interface for user data access operations
 * 
 * RESPONSIBILITIES:
 * - User CRUD operations
 * - Authentication (login)
 * - User management (block/unblock, password reset)
 */
public interface UserRepository {

    Optional<User> authenticate(String username, String hashedPassword);

    Optional<User> findById(Long userId);

    Optional<User> findByUsername(String username);

    ObservableList<User> findAll();

    ObservableList<User> findByRole(UserRole role);

    User save(User user);

    User update(User user);

    boolean delete(Long userId);

    boolean changePassword(Long userId, String newHashedPassword);
    
    boolean blockUser(Long userId);

    boolean unblockUser(Long userId);

    boolean usernameExists(String username);
}
