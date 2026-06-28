package com.aimsfx.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Phase 1: Models & Utils Layer Tests
 * Test Suite: TS_MNO_MODELS
 */
public class OrderStatusTest {

    @Test
    void testUT_MNO_01_OrderStatusMapping() {
        // Arrange
        String pendingDbValue = "PENDING";
        String invalidDbValue = "UNKNOWN_STATUS";
        String processingDbValue = "PROCESSING";

        // Act & Assert
        // 1. Valid mapping
        assertEquals(OrderStatus.PENDING_REVIEW, OrderStatus.fromDbValue(pendingDbValue));
        
        // 2. Fallback for unknown status
        assertEquals(OrderStatus.PROCESSING, OrderStatus.fromDbValue(invalidDbValue));
        
        // 3. Null handling
        assertEquals(OrderStatus.PROCESSING, OrderStatus.fromDbValue(null));
        
        // 4. toDbValue check
        assertEquals(processingDbValue, OrderStatus.PROCESSING.toDbValue());
    }
}
