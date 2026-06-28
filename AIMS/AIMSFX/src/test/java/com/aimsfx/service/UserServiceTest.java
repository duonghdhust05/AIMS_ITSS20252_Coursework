package com.aimsfx.service;

import com.aimsfx.exception.DuplicateUsernameException;
import com.aimsfx.exception.SelfOperationException;
import com.aimsfx.exception.UnauthorizedAccessException;
import com.aimsfx.exception.UserNotFoundException;
import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.model.UserStatus;
import com.aimsfx.repository.UserRepository;
import com.aimsfx.utils.SessionManager;
import com.aimsfx.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2: Business Logic / Service Layer Tests
 * Test Suite: TS_USR_SERVICE
 */
public class UserServiceTest {

    private UserService userService;
    private MockUserRepository mockRepo;
    private SessionManager mockSession;
    private UserValidator validator;

    @BeforeEach
    void setUp() {
        mockRepo = new MockUserRepository();
        mockSession = Mockito.mock(SessionManager.class);
        validator = new UserValidator();
        userService = new UserService(mockRepo, mockSession, validator);
    }

    @Test
    void testUT_USR_03_AuthSuccess() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(userService.hashPassword("password123"));
        user.setStatus(UserStatus.ACTIVE);
        mockRepo.setMockUserToReturn(user);

        // Act
        User authenticated = userService.authenticate("testuser", "password123");

        // Assert
        assertNotNull(authenticated);
        assertEquals("testuser", authenticated.getUsername());
    }

    @Test
    void testUT_USR_04_AuthBlocked() {
        // Arrange
        User user = new User();
        user.setUsername("blockeduser");
        user.setPassword(userService.hashPassword("password123"));
        user.setStatus(UserStatus.BLOCKED);
        mockRepo.setMockUserToReturn(user);

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            userService.authenticate("blockeduser", "password123");
        });
    }

    @Test
    void testUT_USR_05_CreateUserSuccess() {
        // Arrange
        Mockito.when(mockSession.isAdministrator()).thenReturn(true);
        mockRepo.setMockUsernameExists(false);
        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUsername("newuser");
        mockRepo.setMockSavedUser(savedUser);

        // Act
        User created = userService.createUser("newuser", "password123", Set.of(UserRole.PRODUCT_MANAGER), "New User");

        // Assert
        assertNotNull(created);
        assertEquals("newuser", created.getUsername());
    }

    @Test
    void testUT_USR_06_CreateUserUnauthorized() {
        // Arrange
        Mockito.when(mockSession.isAdministrator()).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            userService.createUser("newuser", "password123", Set.of(UserRole.PRODUCT_MANAGER), "New User");
        });
    }

    @Test
    void testUT_USR_07_UpdateUser() {
        // Arrange
        Mockito.when(mockSession.isAdministrator()).thenReturn(true);
        User existingUser = new User();
        existingUser.setUserId(1L);
        mockRepo.setMockUserToReturn(existingUser);
        mockRepo.setMockSavedUser(existingUser); // For update return

        // Act
        User updated = userService.updateUser(1L, "newPass123", Set.of(UserRole.ADMINISTRATOR), "Updated Name");

        // Assert
        assertNotNull(updated);
        assertEquals(Set.of(UserRole.ADMINISTRATOR), updated.getRoles());
        assertEquals("Updated Name", updated.getFullName());
    }

    @Test
    void testUT_USR_08_SelfDeletePrevent() {
        // Arrange
        User selfUser = new User();
        selfUser.setUserId(1L);
        Mockito.when(mockSession.isAdministrator()).thenReturn(true);
        Mockito.when(mockSession.getCurrentUser()).thenReturn(selfUser);

        // Act & Assert
        assertThrows(SelfOperationException.class, () -> {
            userService.deleteUser(1L);
        });
    }

    @Test
    void testUT_USR_09_BlockUser() {
        // Arrange
        User adminUser = new User();
        adminUser.setUserId(1L);
        Mockito.when(mockSession.isAdministrator()).thenReturn(true);
        Mockito.when(mockSession.getCurrentUser()).thenReturn(adminUser);
        
        User targetUser = new User();
        targetUser.setUserId(2L);
        mockRepo.setMockUserToReturn(targetUser);
        mockRepo.setMockBlockSuccess(true);

        // Act
        boolean result = userService.blockUser(2L);

        // Assert
        assertTrue(result);
    }

    // ==========================================
    // Mock Implementations for Isolation
    // ==========================================

    private static class MockUserRepository implements UserRepository {
        private User mockUserToReturn;
        private User mockSavedUser;
        private boolean mockUsernameExists = false;
        private boolean mockBlockSuccess = false;

        public void setMockUserToReturn(User user) { this.mockUserToReturn = user; }
        public void setMockSavedUser(User user) { this.mockSavedUser = user; }
        public void setMockUsernameExists(boolean exists) { this.mockUsernameExists = exists; }
        public void setMockBlockSuccess(boolean success) { this.mockBlockSuccess = success; }

        @Override
        public Optional<User> authenticate(String username, String hashedPassword) {
            if (mockUserToReturn != null && mockUserToReturn.getUsername().equals(username) && mockUserToReturn.getPassword().equals(hashedPassword)) {
                return Optional.of(mockUserToReturn);
            }
            return Optional.empty();
        }

        @Override public Optional<User> findById(Long id) { return Optional.ofNullable(mockUserToReturn); }
        @Override public Optional<User> findByUsername(String username) { return Optional.ofNullable(mockUserToReturn); }
        @Override public boolean usernameExists(String username) { return mockUsernameExists; }
        @Override public User save(User user) { return mockSavedUser != null ? mockSavedUser : user; }
        @Override public User update(User user) { return mockSavedUser != null ? mockSavedUser : user; }
        @Override public boolean delete(Long id) { return true; }
        @Override public boolean changePassword(Long id, String newPasswordHash) { return true; }
        @Override public boolean blockUser(Long id) { return mockBlockSuccess; }
        @Override public boolean unblockUser(Long id) { return true; }
        @Override public javafx.collections.ObservableList<User> findAll() { return null; }
        @Override public javafx.collections.ObservableList<User> findByRole(UserRole role) { return null; }
    }

    // Remove MockSessionManager class entirely
}
