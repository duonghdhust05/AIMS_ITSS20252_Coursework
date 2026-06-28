package com.aimsfx.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product Model
 * Maps to Phase 1 of Test Plan (Models & Utils Layer)
 */
class ProductTest {

    @Test
    @DisplayName("[UT_PROD_001] Product Initialization")
    void testProductInitialization() {
        // Arrange
        Product product = new Product() {
            @Override
            public ProductType getProductType() {
                return ProductType.BOOK;
            }

            @Override
            public Map<String, Object> getSpecificDetail() {
                return new java.util.HashMap<>();
            }

            @Override
            public Product copy() {
                return this; // mock implementation
            }
        };

        // Act
        product.setProductId(1L);
        product.setTitle("Test Product");
        product.setCurrentPrice(100.0);
        product.setStock(10);
        product.setCategory("Books");
        product.setStatus("available");

        // Assert
        assertEquals(1L, product.getProductId());
        assertEquals("Test Product", product.getTitle());
        assertEquals(100.0, product.getCurrentPrice());
        assertEquals(10, product.getStock());
        assertEquals("Books", product.getCategory());
        assertEquals("available", product.getStatus());
        assertEquals(ProductType.BOOK, product.getProductType());
    }
}
