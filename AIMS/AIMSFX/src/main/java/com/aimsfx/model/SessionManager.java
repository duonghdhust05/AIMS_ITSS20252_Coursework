package com.aimsfx.model;

/**
 * SessionManager - Singleton for managing session-related data
 * 
 * PURPOSE: Store session data that doesn't belong to Cart management
 * - DeliveryInfo: Delivery information across navigation
 * - Other session data in the future
 * 
 * DESIGN PATTERN: Singleton Pattern (Thread-Safe with Holder)
 * - Uses Bill Pugh Singleton Design (static inner class holder)
 * - Lazy initialization
 * - Thread-safe without synchronization overhead
 * 
 * SOLID COMPLIANCE:
 * SRP: Only manages session-related data (NOT cart data)
 * OCP: Can extend with new session data types
 * 
 * COHESION: HIGH - All methods relate to session data management
 */
public class SessionManager {
    
    // Session data
    private DeliveryInfo deliveryInfo;
    
    // Private constructor for Singleton
    private SessionManager() {
        this.deliveryInfo = null;
    }
    
    /**
     * Static inner class holder for thread-safe lazy initialization
     * The inner class is not loaded until getInstance() is called
     */
    private static class Holder {
        private static final SessionManager INSTANCE = new SessionManager();
    }
    
    /**
     * Get singleton instance (thread-safe)
     * @return The singleton SessionManager instance
     */
    public static SessionManager getInstance() {
        return Holder.INSTANCE;
    }
    
    // ==================== DELIVERY INFO ====================
    
    /**
     * Get delivery info
     * @return Current delivery info or null
     */
    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }
    
    /**
     * Set delivery info
     * @param deliveryInfo DeliveryInfo to store
     */
    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }
    
    /**
     * Clear delivery info
     */
    public void clearDeliveryInfo() {
        this.deliveryInfo = null;
    }
    
    /**
     * Check if delivery info is set
     * @return true if delivery info exists
     */
    public boolean hasDeliveryInfo() {
        return deliveryInfo != null;
    }
    
    // ==================== SESSION MANAGEMENT ====================
    
    /**
     * Clear all session data
     * Call this on logout or session reset
     */
    public void clearSession() {
        this.deliveryInfo = null;
        // Add more session data clearing here as needed
    }
}
