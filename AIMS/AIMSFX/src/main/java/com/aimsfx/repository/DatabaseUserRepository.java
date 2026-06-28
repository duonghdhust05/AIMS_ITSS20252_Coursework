package com.aimsfx.repository;

import com.aimsfx.utils.DatabaseConnection;
import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.model.UserStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * DatabaseUserRepository - PostgreSQL implementation of UserRepository
 * 
 * RESPONSIBILITIES:
 * - User authentication and management
 * - Password operations (change, reset)
 * - User status management (block/unblock)
 * 
 * MULTI-ROLE SUPPORT:
 * - Roles stored as comma-separated string in database
 * - Parsed into Set<UserRole> in Java objects
 */
public class DatabaseUserRepository implements UserRepository {

    private final DatabaseConnection dbConnection;

    public DatabaseUserRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<User> authenticate(String username, String hashedPassword) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE'";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(createUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Authentication failed: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(createUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to find user by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(createUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to find user by username: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public ObservableList<User> findAll() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to fetch all users: " + e.getMessage());
        }

        return users;
    }

    @Override
    public ObservableList<User> findByRole(UserRole role) {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT * FROM users WHERE roles LIKE ? ORDER BY created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Use LIKE to find role in comma-separated list
            stmt.setString(1, "%" + role.toString() + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = createUserFromResultSet(rs);
                // Double-check user actually has the role (to avoid false matches)
                if (user.hasRole(role)) {
                    users.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to fetch users by role: " + e.getMessage());
        }

        return users;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, password, roles, status, full_name) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING user_id";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, rolesToString(user.getRoles()));
            stmt.setString(4, user.getStatus().toString());
            stmt.setString(5, user.getFullName());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setUserId(rs.getLong("user_id"));
                return user;
            } else {
                throw new RuntimeException("Failed to create user: No ID returned");
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to save user: " + e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET username = ?, roles = ?, status = ?, full_name = ?, " +
                "updated_at = NOW() WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, rolesToString(user.getRoles()));
            stmt.setString(3, user.getStatus().toString());
            stmt.setString(4, user.getFullName());
            stmt.setLong(5, user.getUserId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return user;
            } else {
                throw new RuntimeException("Failed to update user: User not found");
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to update user: " + e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public boolean delete(Long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to delete user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean changePassword(Long userId, String newHashedPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = NOW() WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newHashedPassword);
            stmt.setLong(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to change password: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean blockUser(Long userId) {
        String sql = "UPDATE users SET status = 'BLOCKED', updated_at = NOW() WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to block user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean unblockUser(Long userId) {
        String sql = "UPDATE users SET status = 'ACTIVE', updated_at = NOW() WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to unblock user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to check username existence: " + e.getMessage());
        }

        return false;
    }

    /**
     * Helper method to create User object from ResultSet
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRoles(parseRoles(rs.getString("roles")));
        user.setStatus(UserStatus.fromString(rs.getString("status")));
        user.setFullName(rs.getString("full_name"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null)
            user.setCreatedAt(createdAt.toLocalDateTime());
        if (updatedAt != null)
            user.setUpdatedAt(updatedAt.toLocalDateTime());

        return user;
    }

    /**
     * Parse comma-separated roles string into Set<UserRole>
     */
    private Set<UserRole> parseRoles(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(rolesString.split(","))
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .map(UserRole::fromString)
                .collect(Collectors.toSet());
    }

    /**
     * Convert Set<UserRole> to comma-separated string
     */
    private String rolesToString(Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }

        return roles.stream()
                .map(role -> role.toString())
                .collect(Collectors.joining(","));
    }
}
