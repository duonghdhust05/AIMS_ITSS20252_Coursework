package com.aimsfx.subsystem.vietqr;

import com.aimsfx.subsystem.vietqr.model.VietQRRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class VietQRInteraction {

    private static final String TOKEN_URL = "https://172.21.128.157/vqr/api/token_generate";
    private static final String QR_URL = "https://172.21.128.157/vqr/api/qr/generate-customer";
    private static final String SIMULATE_URL = "https://172.21.128.157/vqr/bank/api/test/transaction-callback";

    private final VietQRConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public VietQRInteraction(VietQRConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public VietQRResponse postTokenRequest() throws Exception {
        String username = config.getClientUsername();
        String password = config.getClientPassword();

        if (username == null || password == null) {
            throw new RuntimeException("VietQR Credentials not found in application.properties");
        }

        String authString = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Authorization", "Basic " + encodedAuth)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new RuntimeException("E74: Invalid VietQR credentials");
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("VietQR API Error: HTTP " + response.statusCode());
        }

        VietQRResponse vietQRResponse = objectMapper.readValue(response.body(), VietQRResponse.class);

        if ("FAILED".equalsIgnoreCase(vietQRResponse.status())) {
            throw new RuntimeException(
                    "E74: " + (vietQRResponse.message() != null ? vietQRResponse.message() : "Authentication failed"));
        }

        return vietQRResponse;
    }

    public VietQRResponse postQrRequest(VietQRRequest request, String accessToken) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(QR_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), VietQRResponse.class);
    }

    public void postSimulationRequest(String orderId, long amount, String content,
            String bankCode, String bankAccount,
            String token) throws Exception {

        String jsonBody = String.format("""
                {
                    "bankAccount": "%s",
                    "content": "%s",
                    "amount": %d,
                    "transType": "C",
                    "bankCode": "%s",
                    "orderId": "%s"
                }
                """, bankAccount, content, amount, bankCode, orderId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SIMULATE_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 || response.body().contains("FAILED")) {
            throw new RuntimeException("Simulation Failed: " + response.body());
        }
    }
}