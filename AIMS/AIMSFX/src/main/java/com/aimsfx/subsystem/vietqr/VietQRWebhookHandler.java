package com.aimsfx.subsystem.vietqr;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderStatus;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.webhook.IPaymentWebhookHandler;
import com.aimsfx.subsystem.vietqr.model.VietQRCallbackRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRCallbackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class VietQRWebhookHandler implements IPaymentWebhookHandler {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public VietQRWebhookHandler() {
        this.orderRepository = new OrderRepository();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(String gatewayName) {
        return "vietqr".equalsIgnoreCase(gatewayName);
    }

    @Override
    public ResponseEntity<?> handleWebhook(String payload, HttpServletRequest request) {
        // 1. Validate Auth Header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(VietQRCallbackResponse.error("INVALID_AUTH_HEADER", "Authorization header is missing"));
        }

        String token = authHeader.substring(7).trim();

        // 2. Validate JWT
        if (!isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(VietQRCallbackResponse.error("INVALID_TOKEN", "Token is invalid or expired"));
        }

        try {
            // 3. Parse Payload
            VietQRCallbackRequest callbackRequest = objectMapper.readValue(payload, VietQRCallbackRequest.class);
            System.out.println("[VietQRWebhookHandler] Received VietQR callback for order: " + callbackRequest.orderId());

            if (callbackRequest.orderId() != null && !callbackRequest.orderId().isEmpty()) {
                int orderId = Integer.parseInt(callbackRequest.orderId());
                Order order = orderRepository.findById(orderId);

                if (order != null) {
                    boolean success = orderRepository.updateOrderStatusWithCheck(
                            orderId,
                            OrderStatus.PENDING_REVIEW.toDbValue(),
                            order.getStatus(),
                            "Paid via VietQR");
                    if (success) {
                        System.out.println("[VietQRWebhookHandler] Successfully updated Order " + orderId
                                + " to PENDING_REVIEW");
                    } else {
                        System.err.println("[VietQRWebhookHandler] Failed to update Order " + orderId + " from "
                                + order.getStatus());
                    }
                } else {
                    System.err.println("[VietQRWebhookHandler] Order " + orderId + " not found!");
                }
            }

            // Return Success Response
            return ResponseEntity.ok(VietQRCallbackResponse.success(callbackRequest.transactionid()));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VietQRCallbackResponse.error("PROCESSING_FAILED", ex.getMessage()));
        }
    }

    // --- JWT Validation Logic ---
    private boolean isTokenValid(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            System.err.println("JWT Validation Failed: " + e.getMessage());
            return false;
        }
    }
}
