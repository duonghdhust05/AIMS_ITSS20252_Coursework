package com.aimsfx.subsystem.vietqr.controller;

import com.aimsfx.subsystem.vietqr.model.VietQRCallbackRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRCallbackResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aimsfx.subsystem.vietqr.util.JwtUtil;

@RestController
@RequestMapping("/bank/api")
public class TransactionSyncController {

    private final JwtUtil jwtUtil;

    public TransactionSyncController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

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
        if (!jwtUtil.isTokenValid(token)) {
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

}