package com.aimsfx.validator;

import com.aimsfx.exception.InvalidPasswordException;
import com.aimsfx.exception.UserValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1: Models & Utils Layer Tests
 * Test Suite: TS_USR_MODELS
 */
public class UserValidatorTest {

    private UserValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserValidator();
    }

    @Test
    void testUT_USR_02_ValidationRules_WeakPassword() {
        // Arrange
        String weakPassword = "123";

        // Act & Assert
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            validator.validatePassword(weakPassword);
        });
        
        assertTrue(exception.getMessage().contains("Password must be at least 6 characters"));
    }

    @Test
    void testUT_USR_02_ValidationRules_InvalidUsername() {
        // Arrange
        String invalidUsername = "us"; // Too short

        // Act & Assert
        UserValidationException exception = assertThrows(UserValidationException.class, () -> {
            validator.validateUsername(invalidUsername);
        });

        assertTrue(exception.getMessage().contains("Username must be at least 3 characters"));
    }
    
    @Test
    void testUT_USR_02_ValidationRules_ValidInput() {
        // Arrange
        String validPassword = "securePassword123";
        String validUsername = "johndoe";

        // Act & Assert
        assertDoesNotThrow(() -> {
            validator.validatePassword(validPassword);
            validator.validateUsername(validUsername);
        });
    }
}
