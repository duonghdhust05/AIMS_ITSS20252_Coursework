package com.aimsfx.service;

import com.aimsfx.model.Order;

/**
 * Interface for email sending operations
 * Enables Dependency Injection and mocking for unit tests
 */
public interface IEmailSender {
    /**
     * Send order confirmation email
     */
    void sendConfirmation(Order order, String email);
    
    /**
     * Send order update notification
     */
    void sendUpdateNotification(Order order, String email);
}
