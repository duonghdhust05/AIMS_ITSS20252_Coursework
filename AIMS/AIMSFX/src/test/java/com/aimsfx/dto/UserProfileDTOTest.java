package com.aimsfx.dto;

import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.model.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1: Models & Utils Layer Tests
 * Test Suite: TS_USR_MODELS
 */
public class UserProfileDTOTest {

    @Test
    void testUT_USR_01_ModelMapping() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setPassword("secret_hash_password_should_be_hidden");
        user.setFullName("Test User");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(UserRole.PRODUCT_MANAGER));
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Act
        UserProfileDTO dto = new UserProfileDTO(user);

        // Assert
        assertEquals(user.getUserId(), dto.getUserId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getFullName(), dto.getFullName());
        assertEquals(user.getStatus(), dto.getStatus());
        assertEquals(user.getRoles(), dto.getRoles());
        
        // Ensure password is not exposed in toString or directly
        assertFalse(dto.toString().contains("secret_hash_password_should_be_hidden"));
    }
}
