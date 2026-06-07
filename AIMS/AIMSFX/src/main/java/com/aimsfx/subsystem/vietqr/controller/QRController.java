package com.aimsfx.subsystem.vietqr.controller;

import com.aimsfx.subsystem.vietqr.model.VietQRRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
public class QRController {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @PostMapping("/generate-customer")
    public ResponseEntity<VietQRResponse> generateQR(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VietQRRequest request) {

        // 1. Validate Auth Header (Bearer Token)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(VietQRResponse.createTokenError("401", "Unauthorized: Missing or invalid Bearer token"));
        }

        String token = authHeader.substring(7).trim();

        // 2. Validate JWT Token
        if (!isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(VietQRResponse.createTokenError("401", "Unauthorized: Token is invalid or expired"));
        }

        try {
            // 3. Generate a Mock QR Response
            String mockTransactionId = "VIETQR_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            VietQRResponse response = new VietQRResponse(
                    "success", "Success",
                    null, null, null,
                    "MOCK_QR_CODE_DATA", "https://mock.qr.link", mockTransactionId,
                    request.bankaccount(), request.userbankname(), "Mock Bank", "Mock User Bank",
                    String.valueOf(request.amount()), request.content(), null, null,
                    "00", "Success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VietQRResponse.createTokenError("500", "Internal Server Error during QR generation"));
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
            System.err.println("JWT Validation Failed in QRController: " + e.getMessage());
            return false;
        }
    }
}
