package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommonProductValidator
 * Maps to Phase 1 of Test Plan
 */
class CommonProductValidatorTest {

    private CommonProductValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CommonProductValidator();
    }

    @Test
    @DisplayName("[UT_PROD_002] Validator Rejects Negative Price")
    void testValidateCommonFields_NegativePrice() {
        // Arrange
        String type = "Book";
        String barcode = "123456789";
        String title = "Valid Title";
        Double originalPrice = -10.0; // Negative price
        Double currentPrice = 50.0;
        String category = "Books";
        Double weight = 1.0;
        String dimensions = "10x20x5";
        Integer stock = 10;

        // Act & Assert
        InvalidProductDataException exception = assertThrows(InvalidProductDataException.class, () -> {
            validator.validateCommonFields(type, barcode, title, originalPrice, currentPrice, category, weight, dimensions, stock);
        });

        assertEquals("Original price must be non-negative", exception.getMessage());
    }

    @Test
    @DisplayName("Validator Rejects Negative Current Price")
    void testValidateCommonFields_NegativeCurrentPrice() {
        // Arrange
        Double originalPrice = 100.0; 
        Double currentPrice = -50.0; // Negative current price

        // Act & Assert
        InvalidProductDataException exception = assertThrows(InvalidProductDataException.class, () -> {
            validator.validateCommonFields("Book", "123456789", "Title", originalPrice, currentPrice, "Books", 1.0, "10x20x5", 10);
        });

        assertEquals("Current price must be non-negative", exception.getMessage());
    }
}
