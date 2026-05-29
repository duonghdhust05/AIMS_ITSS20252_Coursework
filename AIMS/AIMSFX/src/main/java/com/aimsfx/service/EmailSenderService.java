package com.aimsfx.service;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderItem;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EmailSenderService - Asynchronous email sending implementation
 * 
 * RESPONSIBILITIES:
 * - Send order confirmation emails
 * - Send order update notifications
 * - Retry logic for failed sends
 * - Async execution to avoid blocking UI
 * 
 * COHESION: Functional (5/5)
 * COUPLING: Data (4/5) - Only shares data structures
 */
public class EmailSenderService implements IEmailSender {
    
    private static final Logger LOGGER = Logger.getLogger(EmailSenderService.class.getName());
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    // SMTP Configuration - loaded from application.properties
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String fromName;
    
    public EmailSenderService() {
        loadEmailConfig();
    }
    
    /**
     * Load email configuration from application.properties
     */
    private void loadEmailConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            Properties props = new Properties();
            if (input != null) {
                props.load(input);
                smtpHost = props.getProperty("email.smtp.host", "smtp.gmail.com");
                smtpPort = Integer.parseInt(props.getProperty("email.smtp.port", "587"));
                smtpUsername = props.getProperty("email.username", "");
                smtpPassword = props.getProperty("email.password", "");
                fromName = props.getProperty("email.from.name", "DELETED_CODE");
                LOGGER.info("Email config loaded: host=" + smtpHost + ", port=" + smtpPort + ", from=" + smtpUsername);
            } else {
                LOGGER.warning("application.properties not found - using defaults");
                smtpHost = "smtp.gmail.com";
                smtpPort = 587;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load email config", e);
            smtpHost = "smtp.gmail.com";
            smtpPort = 587;
        }
    }
    
    @Override
    public void sendConfirmation(Order order, String email) {
        LOGGER.info("Sending confirmation email to: " + email);
        
        String subject = "Order Confirmation - Order #" + order.getOrderId();
        String body = buildConfirmationEmailBody(order);
        
        sendEmailAsync(email, subject, body);
    }
    
    @Override
    public void sendUpdateNotification(Order order, String email) {
        LOGGER.info("Sending update notification to: " + email);
        
        String subject = "Order Update - Order #" + order.getOrderId();
        String body = buildUpdateEmailBody(order);
        
        sendEmailAsync(email, subject, body);
    }
    
    /**
     * Send email asynchronously with retry logic
     */
    private void sendEmailAsync(String to, String subject, String body) {
        executor.submit(() -> {
            int attempt = 0;
            boolean sent = false;
            
            while (attempt < MAX_RETRIES && !sent) {
                try {
                    attempt++;
                    LOGGER.info("Email send attempt " + attempt + "/" + MAX_RETRIES);
                    
                    // Simulate email sending (replace with actual SMTP logic)
                    sendEmailViaSmtp(to, subject, body);
                    
                    sent = true;
                    LOGGER.info("Email sent successfully to: " + to);
                    
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Email send attempt " + attempt + " failed", e);
                    
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            LOGGER.log(Level.SEVERE, "Email retry interrupted", ie);
                            return;
                        }
                    }
                }
            }
            
            if (!sent) {
                LOGGER.severe("Failed to send email after " + MAX_RETRIES + " attempts");
            }
        });
    }
    
    /**
     * Build confirmation email body with HTML template
     */
    private String buildConfirmationEmailBody(Order order) {
        StringBuilder html = new StringBuilder();
        
        // Start HTML document
        html.append("<!DOCTYPE html>");
        html.append("<html lang='vi'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("</head>");
        html.append("<body style='margin:0; padding:0; font-family: Arial, Helvetica, sans-serif; background-color:#f4f4f4;'>");
        
        // Container
        html.append("<div style='max-width:600px; margin:0 auto; background-color:#ffffff;'>");
        
        // Header with gradient
        html.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding:30px; text-align:center;'>");
        html.append("<h1 style='color:#ffffff; margin:0; font-size:28px;'>🎉 Order Confirmed!</h1>");
        html.append("<p style='color:#e8e8e8; margin:10px 0 0 0; font-size:16px;'>Thank you for shopping with AIMS</p>");
        html.append("</div>");
        
        // Order ID badge
        html.append("<div style='background-color:#f8f9fa; padding:20px; text-align:center; border-bottom:1px solid #e9ecef;'>");
        html.append("<span style='background-color:#28a745; color:#fff; padding:8px 20px; border-radius:20px; font-size:14px; font-weight:bold;'>");
        html.append("Order #").append(order.getOrderId()).append("</span>");
        html.append("<p style='color:#6c757d; margin:15px 0 0 0; font-size:13px;'>📅 ").append(order.getCreatedDate()).append("</p>");
        html.append("</div>");
        
        // Products section
        html.append("<div style='padding:25px;'>");
        html.append("<h2 style='color:#333; font-size:18px; margin:0 0 20px 0; border-bottom:2px solid #667eea; padding-bottom:10px;'>🛒 Order Items</h2>");
        
        // Products table
        html.append("<table style='width:100%; border-collapse:collapse;'>");
        for (OrderItem orderItem : order.getOrderItems()) {
            html.append("<tr style='border-bottom:1px solid #eee;'>");
            html.append("<td style='padding:15px 0;'>");
            html.append("<div style='font-weight:bold; color:#333;'>").append(orderItem.getProduct().getTitle()).append("</div>");
            html.append("<div style='color:#888; font-size:13px; margin-top:5px;'>Quantity: ").append(orderItem.getQuantity()).append("</div>");
            html.append("</td>");
            html.append("<td style='text-align:right; padding:15px 0; color:#667eea; font-weight:bold;'>");
            html.append(formatCurrency(orderItem.getPrice())).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("</div>");
        
        // Price summary
        html.append("<div style='background-color:#f8f9fa; padding:25px; margin:0 25px 25px 25px; border-radius:10px;'>");
        html.append("<table style='width:100%;'>");
        html.append("<tr><td style='padding:8px 0; color:#666;'>Subtotal:</td>");
        html.append("<td style='text-align:right; padding:8px 0;'>").append(formatCurrency(order.getSubtotal())).append("</td></tr>");
        html.append("<tr><td style='padding:8px 0; color:#666;'>VAT (10%):</td>");
        html.append("<td style='text-align:right; padding:8px 0;'>").append(formatCurrency(order.getVat())).append("</td></tr>");
        html.append("<tr><td style='padding:8px 0; color:#666;'>🚚 Delivery Fee:</td>");
        html.append("<td style='text-align:right; padding:8px 0;'>").append(formatCurrency(order.getDeliveryFee())).append("</td></tr>");
        html.append("<tr style='border-top:2px solid #667eea;'>");
        html.append("<td style='padding:15px 0 0 0; font-size:18px; font-weight:bold; color:#333;'>Total:</td>");
        html.append("<td style='text-align:right; padding:15px 0 0 0; font-size:20px; font-weight:bold; color:#28a745;'>");
        html.append(formatCurrency(order.getTotalAmount())).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Delivery address
        if (order.getDeliveryInfo() != null) {
            html.append("<div style='padding:0 25px 25px 25px;'>");
            html.append("<h2 style='color:#333; font-size:18px; margin:0 0 15px 0; border-bottom:2px solid #667eea; padding-bottom:10px;'>📍 Delivery Address</h2>");
            html.append("<div style='background-color:#fff3cd; border-left:4px solid #ffc107; padding:15px; border-radius:0 8px 8px 0;'>");
            html.append("<div style='font-weight:bold; color:#333; font-size:15px;'>").append(order.getDeliveryInfo().getRecipientName()).append("</div>");
            html.append("<div style='color:#666; margin-top:8px;'>📱 ").append(order.getDeliveryInfo().getPhoneNumber()).append("</div>");
            html.append("<div style='color:#666; margin-top:5px;'>🏠 ").append(order.getDeliveryInfo().getAddress()).append("</div>");
            html.append("<div style='color:#666; margin-top:5px;'>📮 ").append(order.getDeliveryInfo().getProvince()).append("</div>");
            html.append("</div>");
            html.append("</div>");
        }
        
        // Footer
        html.append("<div style='background-color:#333; padding:25px; text-align:center;'>");
        html.append("<p style='color:#fff; margin:0 0 10px 0; font-size:14px;'>🚚 We will notify you when your order is shipped!</p>");
        html.append("<p style='color:#aaa; margin:0; font-size:12px;'>Thank you for shopping with DELETED_CODE</p>");
        html.append("<div style='margin-top:15px;'>");
        html.append("<span style='color:#667eea; font-size:20px;'>💜</span>");
        html.append("</div>");
        html.append("</div>");
        
        html.append("</div>"); // End container
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Format currency with Vietnamese format
     */
    private String formatCurrency(double amount) {
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        return formatter.format(amount) + " VND";
    }
    
    /**
     * Build update email body with HTML template
     */
    private String buildUpdateEmailBody(Order order) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang='vi'>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='margin:0; padding:0; font-family: Arial, sans-serif; background-color:#f4f4f4;'>");
        
        html.append("<div style='max-width:600px; margin:0 auto; background-color:#ffffff;'>");
        
        // Header
        html.append("<div style='background: linear-gradient(135deg, #17a2b8 0%, #138496 100%); padding:30px; text-align:center;'>");
        html.append("<h1 style='color:#ffffff; margin:0; font-size:26px;'>📦 Order Update</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div style='padding:30px; text-align:center;'>");
        html.append("<div style='background-color:#d4edda; border:1px solid #c3e6cb; padding:20px; border-radius:10px; margin-bottom:20px;'>");
        html.append("<h2 style='color:#155724; margin:0;'>Order #").append(order.getOrderId()).append("</h2>");
        html.append("</div>");
        
        html.append("<div style='background-color:#fff3cd; padding:15px; border-radius:8px; display:inline-block;'>");
        html.append("<span style='font-size:16px; color:#856404;'>Status: <strong>").append(order.getStatus()).append("</strong></span>");
        html.append("</div>");
        
        html.append("<p style='color:#666; margin-top:25px; font-size:18px;'>Total: <strong style='color:#28a745;'>");
        html.append(formatCurrency(order.getTotalAmount())).append("</strong></p>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='background-color:#333; padding:20px; text-align:center;'>");
        html.append("<p style='color:#aaa; margin:0; font-size:13px;'>Thank you for your patience! 💜</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Send email via SMTP using Jakarta Mail with HTML support
     * Configured via application.properties
     */
    private void sendEmailViaSmtp(String to, String subject, String body) throws Exception {
        if (smtpUsername == null || smtpUsername.isEmpty()) {
            LOGGER.warning("SMTP username not configured - skipping email send");
            return;
        }
        
        LOGGER.info("SMTP: Sending to " + to + " | Subject: " + subject);
        
        // Setup mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.ssl.trust", smtpHost);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        // Create session with authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });
        
        // Create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpUsername, fromName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        
        // Set HTML content type
        message.setContent(body, "text/html; charset=UTF-8");
        
        // Send
        Transport.send(message);
        LOGGER.info("SMTP: Email sent successfully to " + to);
    }
    
    /**
     * Shutdown executor gracefully
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("EmailSenderService shutdown complete");
    }
}
