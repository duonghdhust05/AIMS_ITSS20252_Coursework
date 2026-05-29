package com.aimsfx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for SessionManager Singleton
 * Verifies thread-safe singleton and session data management
 * 
 * SOLID VERIFICATION:
 * - Tests verify SRP (only session data, not cart)
 */
@DisplayName("SessionManager Tests")
class SessionManagerTest {

    @BeforeEach
    void setUp() {
        // Clear session before each test
        SessionManager.getInstance().clearSession();
    }

    @Test
    @DisplayName("Singleton returns same instance")
    void testGetInstance_ReturnsSameInstance() {
        SessionManager instance1 = SessionManager.getInstance();
        SessionManager instance2 = SessionManager.getInstance();

        assertSame(instance1, instance2, "Should return the same singleton instance");
    }

    @Test
    @DisplayName("Set and get delivery info")
    void testSetAndGetDeliveryInfo() {
        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setRecipientName("Test User");
        deliveryInfo.setProvince("Hà Nội");

        SessionManager.getInstance().setDeliveryInfo(deliveryInfo);

        DeliveryInfo retrieved = SessionManager.getInstance().getDeliveryInfo();

        assertNotNull(retrieved, "Should return delivery info");
        assertEquals("Test User", retrieved.getRecipientName(), "Name should match");
        assertEquals("Hà Nội", retrieved.getProvince(), "Province should match");
    }

    @Test
    @DisplayName("Has delivery info returns true when set")
    void testHasDeliveryInfo_WhenSet_ReturnsTrue() {
        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setRecipientName("Test");

        SessionManager.getInstance().setDeliveryInfo(deliveryInfo);

        assertTrue(SessionManager.getInstance().hasDeliveryInfo(), "Should return true");
    }

    @Test
    @DisplayName("Has delivery info returns false when not set")
    void testHasDeliveryInfo_WhenNotSet_ReturnsFalse() {
        assertFalse(SessionManager.getInstance().hasDeliveryInfo(), "Should return false");
    }

    @Test
    @DisplayName("Clear delivery info removes it")
    void testClearDeliveryInfo_RemovesIt() {
        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setRecipientName("Test");
        SessionManager.getInstance().setDeliveryInfo(deliveryInfo);

        SessionManager.getInstance().clearDeliveryInfo();

        assertNull(SessionManager.getInstance().getDeliveryInfo(), "Should be null after clear");
        assertFalse(SessionManager.getInstance().hasDeliveryInfo(), "Has delivery info should be false");
    }

    @Test
    @DisplayName("Clear session removes all session data")
    void testClearSession_RemovesAllData() {
        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setRecipientName("Test");
        SessionManager.getInstance().setDeliveryInfo(deliveryInfo);

        SessionManager.getInstance().clearSession();

        assertNull(SessionManager.getInstance().getDeliveryInfo(), "Delivery info should be null");
    }
}
