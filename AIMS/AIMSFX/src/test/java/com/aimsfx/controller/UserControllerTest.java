package com.aimsfx.controller;

import com.aimsfx.exception.UnauthorizedAccessException;
import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.service.UserService;
import com.aimsfx.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4: Controller / API Layer Tests
 * Test Suite: TS_USR_CTRL
 */
public class UserControllerTest {

    private UserController userController;
    private MockUserService mockUserService;
    private SessionManager mockSessionManager;

    @BeforeEach
    void setUp() {
        mockUserService = new MockUserService();
        mockSessionManager = SessionManager.getInstance();
        userController = new UserController(mockUserService, mockSessionManager);
    }

    @Test
    void testIT_USR_04_CtrlCreateUser() {
        // Arrange
        String username = "ctrl_user";
        String password = "password";
        Set<UserRole> roles = Set.of(UserRole.PRODUCT_MANAGER);
        String fullName = "Ctrl User";

        // Act
        User created = userController.createUser(username, password, roles, fullName);

        // Assert
        assertNotNull(created);
        assertEquals(username, created.getUsername());
        assertTrue(mockUserService.wasCreateCalled);
    }

    @Test
    void testIT_USR_05_CtrlDeleteUser() {
        // Arrange
        Long userIdToDelete = 55L;
        mockUserService.deleteSuccess = true;

        // Act
        boolean result = userController.deleteUser(userIdToDelete);

        // Assert
        assertTrue(result);
        assertEquals(userIdToDelete, mockUserService.lastDeletedId);
    }
    
    @Test
    void testIT_USR_05_CtrlDeleteUser_Unauthorized() {
        // Arrange
        Long userIdToDelete = 55L;
        mockUserService.shouldThrowUnauthorizedOnDelete = true;

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            userController.deleteUser(userIdToDelete);
        });
    }

    // ==========================================
    // Mock Implementations for Isolation
    // ==========================================

    private static class MockUserService extends UserService {
        public boolean wasCreateCalled = false;
        public boolean deleteSuccess = false;
        public Long lastDeletedId = null;
        public boolean shouldThrowUnauthorizedOnDelete = false;

        public MockUserService() {
            super(null, null, null); // Avoid triggering real dependencies
        }

        @Override
        public User createUser(String username, String password, Set<UserRole> roles, String fullName) {
            wasCreateCalled = true;
            User u = new User();
            u.setUsername(username);
            return u;
        }

        @Override
        public boolean deleteUser(Long userId) {
            if (shouldThrowUnauthorizedOnDelete) {
                throw new UnauthorizedAccessException("delete user");
            }
            lastDeletedId = userId;
            return deleteSuccess;
        }
    }
}
