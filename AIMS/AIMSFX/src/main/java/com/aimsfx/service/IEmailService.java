package com.aimsfx.service;

import com.aimsfx.exception.EmailException;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Order;
import com.aimsfx.model.TransactionInfo;

/**
 * Email service interface for sending order notifications
 * 
 * DESIGN PRINCIPLES:
 * - Interface Segregation: Single focused method
 * - Dependency Inversion: Controller depends on abstraction
 */
public interface IEmailService {
    
    /**
     * Send order confirmation email to customer
     * 
     * @param order Order details with products and total amount
     * @param deliveryInfo Customer delivery information with email address
     * @param transaction Payment transaction details
     * @throws EmailException if email sending fails
     */
    void sendOrderConfirmation(Order order, 
                              DeliveryInfo deliveryInfo,
                              TransactionInfo transaction) 
            throws EmailException;
}
