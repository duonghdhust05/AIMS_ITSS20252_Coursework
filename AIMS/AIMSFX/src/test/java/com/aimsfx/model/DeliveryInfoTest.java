package com.aimsfx.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1: Models Layer
 * Test Suite: TS_PLO_MODELS
 */
public class DeliveryInfoTest {

    @Test
    void testUT_PLO_01_DeliveryInfoValidation() {
        // Arrange
        DeliveryInfo info = new DeliveryInfo();
        
        // Act & Assert
        // 1. Invalid (empty fields)
        assertFalse(info.checkValidityOfDeliveryInfo());
        
        // 2. Add required fields
        info.setRecipientName("Nguyen Van A");
        info.setPhoneNumber("0123456789");
        info.setEmail("test@example.com");
        info.setProvince("Hanoi");
        info.setAddress("123 Street");
        info.setWard("Ward 1");
        
        // Assert valid
        assertTrue(info.checkValidityOfDeliveryInfo());
        
        // 3. Test calculation for Fee
        // weight <= 3kg -> 22000 in Hanoi
        assertEquals(22000.0f, info.calculateDeliveryFee(2.5f), 0.01f);
        
        // weight > 3kg -> 22000 + 2500 per 0.5kg
        assertEquals(24500.0f, info.calculateDeliveryFee(3.5f), 0.01f);
    }
}
