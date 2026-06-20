package com.aimsfx.controller.api;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderStatus;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.subsystem.vietqr.model.VietQRCallbackRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRCallbackResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final OrderRepository orderRepository = new OrderRepository();

    @PostMapping(value = "/vietqr-callback", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VietQRCallbackResponse> transactionSync(
            @RequestBody VietQRCallbackRequest payload,
            HttpServletRequest request) {
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
            System.out.println("[PaymentWebhookController] Received VietQR callback for order: " + payload.orderId());

            if (payload.orderId() != null && !payload.orderId().isEmpty()) {
                int orderId = Integer.parseInt(payload.orderId());
                Order order = orderRepository.findById(orderId);
                
                if (order != null) {
                    boolean success = orderRepository.updateOrderStatusWithCheck(
                        orderId, 
                        OrderStatus.PENDING_REVIEW.toDbValue(), 
                        order.getStatus(), 
                        "Paid via VietQR"
                    );
                    if (success) {
                        System.out.println("[PaymentWebhookController] Successfully updated Order " + orderId + " to PENDING_REVIEW");
                    } else {
                        System.err.println("[PaymentWebhookController] Failed to update Order " + orderId + " from " + order.getStatus());
                    }
                } else {
                    System.err.println("[PaymentWebhookController] Order " + orderId + " not found!");
                }
            }

            // Return Success Response
            return ResponseEntity.ok(VietQRCallbackResponse.success(payload.transactionid()));

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