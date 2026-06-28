package com.aimsfx.repository;

import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.model.UserStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3: Data Access / Repository Layer Tests
 * Test Suite: TS_USR_REPO
 * Integration tests connecting to the real database.
 */
public class DatabaseUserRepositoryTest {

    private static DatabaseUserRepository repository;
    private static User testUser;

    @BeforeAll
    static void setUp() {
        repository = new DatabaseUserRepository();
        // Create a unique test user to avoid conflicts
        testUser = new User();
        testUser.setUsername("testuser_db_" + UUID.randomUUID().toString().substring(0, 8));
        testUser.setPassword("dummy_hash");
        testUser.setRoles(Set.of(UserRole.PRODUCT_MANAGER));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setFullName("DB Test User");
    }

    @AfterAll
    static void tearDown() {
        // Clean up the test user if it still exists
        if (testUser != null && testUser.getUserId() != null) {
            repository.delete(testUser.getUserId());
        }
    }

    @Test
    void testIT_USR_01_DBSaveAndRetrieveUser() {
        // Act: Save user
        User savedUser = repository.save(testUser);
        
        // Assert: Saved user has an ID
        assertNotNull(savedUser.getUserId());
        testUser.setUserId(savedUser.getUserId()); // Update for future tests

        // Act: Retrieve user by ID
        Optional<User> retrieved = repository.findById(savedUser.getUserId());

        // Assert: User retrieved correctly
        assertTrue(retrieved.isPresent());
        assertEquals(testUser.getUsername(), retrieved.get().getUsername());
        assertEquals(testUser.getFullName(), retrieved.get().getFullName());
    }

    @Test
    void testIT_USR_02_DBUpdateUserInfoAndPassword() {
        // Ensure user exists
        if (testUser.getUserId() == null) {
            testIT_USR_01_DBSaveAndRetrieveUser();
        }

        // Arrange: Update info
        testUser.setFullName("Updated DB Name");
        testUser.setRoles(Set.of(UserRole.ADMINISTRATOR));

        // Act: Update user
        User updatedUser = repository.update(testUser);
        
        // Assert: Update returned updated instance
        assertEquals("Updated DB Name", updatedUser.getFullName());
        assertTrue(updatedUser.getRoles().contains(UserRole.ADMINISTRATOR));

        // Act: Change password
        boolean pwdChanged = repository.changePassword(testUser.getUserId(), "new_dummy_hash");

        // Assert: Password change reported success
        assertTrue(pwdChanged);
        
        // Verify changes in DB
        Optional<User> fromDb = repository.findById(testUser.getUserId());
        assertTrue(fromDb.isPresent());
        assertEquals("new_dummy_hash", fromDb.get().getPassword());
        assertEquals("Updated DB Name", fromDb.get().getFullName());
    }

    @Test
    void testIT_USR_03_DBDeleteAndBlockUser() {
        // Ensure user exists
        if (testUser.getUserId() == null) {
            testIT_USR_01_DBSaveAndRetrieveUser();
        }

        // Act: Block user
        boolean blocked = repository.blockUser(testUser.getUserId());
        
        // Assert: Block success
        assertTrue(blocked);
        Optional<User> blockedDb = repository.findById(testUser.getUserId());
        assertTrue(blockedDb.isPresent());
        assertEquals(UserStatus.BLOCKED, blockedDb.get().getStatus());

        // Act: Delete user
        boolean deleted = repository.delete(testUser.getUserId());

        // Assert: Delete success
        assertTrue(deleted);
        Optional<User> deletedDb = repository.findById(testUser.getUserId());
        assertFalse(deletedDb.isPresent());
        
        // Prevent tearDown from failing
        testUser.setUserId(null); 
    }
}
