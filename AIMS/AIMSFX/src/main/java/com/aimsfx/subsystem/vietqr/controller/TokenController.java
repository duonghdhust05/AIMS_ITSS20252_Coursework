package com.aimsfx.subsystem.vietqr.controller;

import com.aimsfx.subsystem.vietqr.model.VietQRResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class TokenController {

    // Inject values from application.properties
    @Value("${vietqr.client.username}")
    private String validUsername;

    @Value("${vietqr.client.password}")
    private String validPassword;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @PostMapping("/token_generate")
    public ResponseEntity<VietQRResponse> generateToken(@RequestHeader("Authorization") String authHeader) {

        // 1. Validate Basic Auth (Username/Password)
        if (!isBasicAuthValid(authHeader)) {
            // FIX: Use the static factory method for a clean error return
            VietQRResponse error = VietQRResponse.createTokenError("401", "Invalid Credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // 2. Generate Real JWT Token
        try {
            String token = generateJwtToken();

            // FIX: Use the static factory method for a clean success return
            VietQRResponse success = VietQRResponse.createTokenSuccess(token, 300);

            return ResponseEntity.ok(success);

        } catch (Exception e) {
            System.err.println("JWT Generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Internal Helper Methods ---

    private boolean isBasicAuthValid(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic "))
            return false;
        try {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

            // Print received and expected credentials for debugging
            String[] values = credentials.split(":", 2);

            return values.length == 2 && values[0].equals(validUsername) && values[1].equals(validPassword);
        } catch (Exception e) {
            return false;
        }
    }

    private String generateJwtToken() {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 300000); // 5 minutes expiry
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject("VietQR-Partner")
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }
}