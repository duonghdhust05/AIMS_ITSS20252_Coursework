package com.aimsfx.model;

import java.util.ArrayList;
import java.util.List;

/**
 * CartEvents - Simple observer pattern for cart updates
 * Allows views to listen for cart changes and update UI accordingly
 */
public class CartEvents {
    
    private static final List<Runnable> listeners = new ArrayList<>();
    
    /**
     * Register a listener to be notified when cart changes
     * @param listener Runnable to execute on cart update
     */
    public static void addListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener
     * @param listener Runnable to remove
     */
    public static void removeListener(Runnable listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners that the cart has been updated
     * Call this after adding, removing, or updating cart items
     */
    public static void notifyCartUpdated() {
        for (Runnable listener : new ArrayList<>(listeners)) {
            try {
                listener.run();
            } catch (Exception e) {
                // Log but don't break other listeners
                System.err.println("Error in cart listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clear all listeners (useful for testing)
     */
    public static void clearListeners() {
        listeners.clear();
    }
}
