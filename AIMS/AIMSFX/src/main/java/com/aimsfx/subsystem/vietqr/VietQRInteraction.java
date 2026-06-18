package com.aimsfx.subsystem.vietqr;

import com.aimsfx.subsystem.vietqr.exception.VietQRApiException;
import com.aimsfx.subsystem.vietqr.exception.VietQRAuthException;
import com.aimsfx.subsystem.vietqr.exception.VietQRNetworkException;
import com.aimsfx.subsystem.vietqr.model.VietQRRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class VietQRInteraction {// khởi tạo và cấu hình

    private final VietQRConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public VietQRInteraction(VietQRConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public VietQRResponse postTokenRequest() throws VietQRApiException {// lấy token xác thực
        String username = config.getClientUsername();
        String password = config.getClientPassword();

        if (username == null || password == null) {
            throw new VietQRAuthException("VietQR Credentials not found in application.properties");
        }

        String authString = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(config.getTokenUrl()))
                .header("Authorization", "Basic " + encodedAuth)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new VietQRNetworkException("Network error while requesting token: " + e.getMessage(), e);
        }

        if (response.statusCode() == 401) {
            throw new VietQRAuthException("E74: Invalid VietQR credentials");
        }

        if (response.statusCode() != 200) {
            throw new VietQRApiException("VietQR API Error: HTTP " + response.statusCode());
        }

        try {
            VietQRResponse vietQRResponse = objectMapper.readValue(response.body(), VietQRResponse.class);

            if ("FAILED".equalsIgnoreCase(vietQRResponse.status())) {
                throw new VietQRAuthException(
                        "E74: " + (vietQRResponse.message() != null ? vietQRResponse.message()
                                : "Authentication failed"));
            }

            return vietQRResponse;
        } catch (VietQRApiException e) {
            throw e;
        } catch (Exception e) {
            throw new VietQRApiException("Error parsing token response", e);
        }
    }

    public VietQRResponse postQrRequest(VietQRRequest request, String accessToken) throws VietQRApiException {// tạo mã
                                                                                                              // QR code
        try {
            String jsonBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getQrUrl()))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body(), VietQRResponse.class);
        } catch (java.io.IOException | InterruptedException e) {
            throw new VietQRNetworkException("Network error while generating QR code: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new VietQRApiException("Error processing QR request", e);
        }
    }

    public void postSimulationRequest(String transType, long amount, String content,
            String bankCode, String bankAccount, String token) throws VietQRApiException {

        String jsonBody = String.format("""
                {
                    "bankAccount": "%s",
                    "content": "%s",
                    "amount": %d,
                    "transType": "%s",
                    "bankCode": "%s"
                }
                """, bankAccount, content, amount, transType, bankCode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getSimulateUrl()))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new VietQRNetworkException("Network error during payment simulation: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200 || response.body().contains("FAILED")) {
            throw new VietQRApiException("Simulation Failed: " + response.body());
        }
    }
}