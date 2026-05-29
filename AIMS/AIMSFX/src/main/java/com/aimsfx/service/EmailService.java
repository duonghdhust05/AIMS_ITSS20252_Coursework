package com.aimsfx.service;

import com.aimsfx.exception.EmailException;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Order;
import com.aimsfx.model.TransactionInfo;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Email service implementation using Gmail SMTP
 * 
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Each method has one clear purpose
 * - High Cohesion: All methods relate to email sending
 * - Low Coupling: Only depends on entities + Jakarta Mail
 * 
 * TECHNICAL STACK:
 * - Jakarta Mail 2.0.1: SMTP protocol implementation
 * - Gmail SMTP: Mail server (smtp.gmail.com:587)
 * - TLS 1.2: Secure connection
 * 
 * @author AIMS Team
 * @version 1.0
 */
public class EmailService implements IEmailService {
    
    // Configuration fields (loaded once at construction)
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromName;
    
    /**
     * Constructor: Initialize email service and load configuration
     * 
     * Configuration is loaded from application.properties:
     * - email.smtp.host
     * - email.smtp.port
     * - email.username
     * - email.password
     * - email.from.name
     * 
     * @throws RuntimeException if configuration loading fails
     */
    public EmailService() {
        try {
            Properties config = loadConfig();
            this.host = config.getProperty("email.smtp.host");
            this.port = Integer.parseInt(config.getProperty("email.smtp.port"));
            this.username = config.getProperty("email.username");
            this.password = config.getProperty("email.password");
            this.fromName = config.getProperty("email.from.name", "DELETED_CODE");
            
            System.out.println("✅ Email service initialized successfully");
            System.out.println("   SMTP: " + host + ":" + port);
            System.out.println("   From: " + fromName + " <" + username + ">");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize email service: " + e.getMessage());
            throw new RuntimeException("Failed to initialize email service", e);
        }
    }
    
    /**
     * Send order confirmation email to customer
     * 
     * PROCESS:
     * 1. Validate recipient email
     * 2. Create SMTP session with authentication
     * 3. Generate HTML content from order data
     * 4. Build MIME message
     * 5. Send via Gmail SMTP
     * 
     * @param order Order details
     * @param deliveryInfo Customer delivery information
     * @param transaction Payment transaction details
     * @throws EmailException if email sending fails
     */
    @Override
    public void sendOrderConfirmation(Order order, 
                                     DeliveryInfo deliveryInfo,
                                     TransactionInfo transaction) 
            throws EmailException {
        
        // Validate recipient email
        String recipientEmail = deliveryInfo.getEmail();
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new EmailException("Recipient email is empty");
        }
        
        System.out.println("📧 Sending order confirmation email to: " + recipientEmail);
        
        try {
            // 1. Create SMTP session
            Session session = createSession();
            
            // 2. Build email message
            Message message = buildMessage(session, order, deliveryInfo, transaction);
            
            // 3. Send email
            Transport.send(message);
            
            System.out.println("✅ Email sent successfully to: " + recipientEmail);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            String errorMsg = "Failed to send order confirmation email: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            throw new EmailException(errorMsg, e);
        }
    }
    
    // ============ PRIVATE HELPER METHODS ============
    
    /**
     * Load email configuration from application.properties
     * 
     * SINGLE RESPONSIBILITY: Only read and parse properties
     * 
     * @return Properties object with email configuration
     * @throws IOException if properties file not found or cannot be read
     */
    private Properties loadConfig() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                throw new IOException("application.properties not found in classpath");
            }
            
            props.load(input);
            
            // Validate required properties
            String[] requiredProps = {
                "email.smtp.host", 
                "email.smtp.port", 
                "email.username", 
                "email.password"
            };
            
            for (String prop : requiredProps) {
                if (!props.containsKey(prop)) {
                    throw new IOException("Missing required property: " + prop);
                }
            }
            
        }
        return props;
    }
    
    /**
     * Create authenticated SMTP session
     * 
     * SINGLE RESPONSIBILITY: Only create and configure session
     * 
     * SMTP PROPERTIES:
     * - mail.smtp.auth=true: Enable authentication
     * - mail.smtp.starttls.enable=true: Enable TLS encryption
     * - mail.smtp.host: SMTP server hostname
     * - mail.smtp.port: SMTP server port
     * - mail.smtp.ssl.protocols=TLSv1.2: Force TLS 1.2
     * 
     * @return Configured SMTP session
     */
    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
    
    /**
     * Build email message with all components
     * 
     * SINGLE RESPONSIBILITY: Only construct message object
     * 
     * @param session SMTP session
     * @param order Order details
     * @param deliveryInfo Customer delivery information
     * @param transaction Payment transaction details
     * @return Constructed MIME message
     * @throws MessagingException if message building fails
     */
    private Message buildMessage(Session session, 
                                 Order order,
                                 DeliveryInfo deliveryInfo, 
                                 TransactionInfo transaction) 
            throws MessagingException, UnsupportedEncodingException {
        
        Message message = new MimeMessage(session);
        
        // Set sender
        message.setFrom(new InternetAddress(username, fromName));
        
        // Set recipient
        message.setRecipients(
            Message.RecipientType.TO, 
            InternetAddress.parse(deliveryInfo.getEmail())
        );
        
        // Set subject
        message.setSubject("AIMS - Order Confirmation #" + order.getOrderId());
        
        // Set HTML content
        String htmlContent = generateHtml(order, deliveryInfo, transaction);
        message.setContent(htmlContent, "text/html; charset=utf-8");
        
        return message;
    }
    
    /**
     * Generate HTML email content
     * 
     * SINGLE RESPONSIBILITY: Only generate HTML string
     * 
     * HTML STRUCTURE:
     * 1. Header section with title
     * 2. Greeting
     * 3. Order information box
     * 4. Product table
     * 5. Total amount
     * 6. Delivery information
     * 7. Footer
     * 
     * @param order Order details
     * @param deliveryInfo Customer delivery information
     * @param transaction Payment transaction details
     * @return HTML string for email body
     */
    private String generateHtml(Order order, 
                               DeliveryInfo deliveryInfo,
                               TransactionInfo transaction) {
        
        StringBuilder html = new StringBuilder();
        
        // DOCTYPE and head with styles
        html.append("<!DOCTYPE html><html><head><meta charset='utf-8'>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;padding:20px;color:#333;background:#f9f9f9;}");
        html.append(".container{max-width:600px;margin:0 auto;background:#fff;border-radius:8px;overflow:hidden;}");
        html.append(".header{background:#4CAF50;color:white;padding:30px 20px;text-align:center;}");
        html.append(".header h1{margin:0;font-size:24px;}");
        html.append(".content{padding:30px 20px;}");
        html.append(".info-box{background:#f5f5f5;padding:15px;margin:20px 0;border-radius:5px;border-left:4px solid #4CAF50;}");
        html.append("table{width:100%;border-collapse:collapse;margin:20px 0;}");
        html.append("th{background:#4CAF50;color:white;padding:12px;text-align:left;font-weight:600;}");
        html.append("td{border:1px solid #ddd;padding:12px;}");
        html.append("tr:nth-child(even){background:#f9f9f9;}");
        html.append(".total{font-size:20px;font-weight:bold;color:#4CAF50;text-align:right;padding:15px 0;}");
        html.append(".footer{text-align:center;padding:20px;background:#f5f5f5;color:#777;font-size:12px;}");
        html.append(".footer p{margin:5px 0;}");
        html.append("</style></head><body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>🎉 Order Confirmation</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        
        // Greeting
        html.append("<p>Dear <strong>").append(deliveryInfo.getRecipientName()).append("</strong>,</p>");
        html.append("<p>Thank you for your order! Your order has been successfully placed and confirmed.</p>");
        
        // Order info box
        html.append("<div class='info-box'>");
        html.append("<strong>📦 Order ID:</strong> ").append(order.getOrderId()).append("<br>");
        html.append("<strong>💳 Payment Method:</strong> ").append(transaction.getPaymentMethod()).append("<br>");
        html.append("<strong>📅 Order Date:</strong> ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        html.append("</div>");
        
        // Product table
        html.append("<h3 style='color:#4CAF50;border-bottom:2px solid #4CAF50;padding-bottom:10px;'>📋 Order Details</h3>");
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Product</th>");
        html.append("<th style='text-align:center;'>Quantity</th>");
        html.append("<th style='text-align:right;'>Price</th>");
        html.append("<th style='text-align:right;'>Subtotal</th>");
        html.append("</tr></thead>");
        html.append("<tbody>");
        
        // Product rows
        for (var item : order.getOrderItems()) {
            html.append("<tr>");
            html.append("<td>").append(item.getProduct().getTitle()).append("</td>");
            html.append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>");
            html.append("<td style='text-align:right;'>")
                .append(String.format("%,d", (int)item.getProduct().getCurrentPrice().doubleValue()))
                .append(" VND</td>");
            html.append("<td style='text-align:right;'>")
                .append(String.format("%,d", (int)(item.getProduct().getCurrentPrice() * item.getQuantity())))
                .append(" VND</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody></table>");
        
        // Total
        html.append("<div class='total'>");
        html.append("💰 Total Amount: ")
            .append(String.format("%,d", (int)order.getTotalAmount()))
            .append(" VND");
        html.append("</div>");
        
        // Delivery info
        html.append("<h3 style='color:#4CAF50;border-bottom:2px solid #4CAF50;padding-bottom:10px;'>🚚 Delivery Information</h3>");
        html.append("<div class='info-box'>");
        html.append("<strong>Name:</strong> ").append(deliveryInfo.getRecipientName()).append("<br>");
        html.append("<strong>Phone:</strong> ").append(deliveryInfo.getPhoneNumber()).append("<br>");
        html.append("<strong>Address:</strong> ").append(deliveryInfo.getAddress())
            .append(", ").append(deliveryInfo.getProvince()).append("<br>");
        
        if (deliveryInfo.getDeliveryInstructions() != null && 
            !deliveryInfo.getDeliveryInstructions().trim().isEmpty()) {
            html.append("<strong>Delivery Instructions:</strong> ")
                .append(deliveryInfo.getDeliveryInstructions());
        }
        html.append("</div>");
        
        // Closing message
        html.append("<p>We will process your order shortly and keep you updated on the delivery status.</p>");
        html.append("<p>If you have any questions, please don't hesitate to contact us.</p>");
        
        html.append("</div>"); // End content
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p><strong>Thank you for shopping with DELETED_CODE!</strong> 🛒</p>");
        html.append("<p>© 2026 DELETED_CODE. All rights reserved.</p>");
        html.append("</div>");
        
        html.append("</div>"); // End container
        html.append("</body></html>");
        
        return html.toString();
    }
}
