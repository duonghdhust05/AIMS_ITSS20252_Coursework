package com.aimsfx.subsystem.vietqr.controller;

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
@RequestMapping("/bank/api/test")
public class TransactionSyncController {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @PostMapping(value = "/transaction-callback", consumes = MediaType.APPLICATION_JSON_VALUE)
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
            // Transaction saving is handled by VietQRPaymentHandler
            // This webhook just validates the callback and returns success

            // Log the callback for debugging
            System.out.println(
                    "[TransactionSyncController] Received VietQR callback for transaction: " + payload.transactionid());

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