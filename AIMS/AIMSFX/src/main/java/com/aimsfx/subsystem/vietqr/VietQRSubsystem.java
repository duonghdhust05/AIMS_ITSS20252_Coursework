package com.aimsfx.subsystem.vietqr;

import com.aimsfx.exception.*;
import com.aimsfx.subsystem.vietqr.model.VietQRRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRResponse;
import com.google.gson.Gson;

/**
 * VietQRSubsystem Class
 * Purpose: Implementation of IPaymentQRCode for VietQR payment processing
 * 
 * SOLID Compliance:
 * - SRP: Handles only VietQR API operations
 * - OCP: Maps provider errors to semantic exception types
 * - LSP: Properly substitutable for IPaymentQRCode
 * - DIP: Throws abstract PaymentException subclasses
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward VietQR payment operations
 * 
 * COUPLING: LOW - Data Coupling
 * - Throws semantic exceptions (no provider-specific coupling)
 */
public class VietQRSubsystem implements IPaymentQRCode {

    private final VietQRInteraction interaction;
    private final VietQRConfig config;
    private final Gson gson;

    private String cachedToken;
    private long tokenExpiryTime = 0;

    public VietQRSubsystem(VietQRInteraction interaction, VietQRConfig config) {
        this.interaction = interaction;
        this.config = config;
        this.gson = new Gson();
    }

    private String getAccessToken() throws PaymentException {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedToken;
        }

        try {
            VietQRResponse response = interaction.postTokenRequest();

            if (response != null && response.accessToken() != null) {
                this.cachedToken = response.accessToken();
                this.tokenExpiryTime = System.currentTimeMillis() + (4 * 60 * 1000);
                return cachedToken;
            }

            String errorCode = response != null ? response.code() : "UNKNOWN";
            throw mapToSemanticException(errorCode, null, null);

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentAuthenticationException("E74", "VIETQR", e);
        }
    }

    @Override
    public String generateQRCode(String orderId, long amount, String content) throws PaymentException {
        try {
            String token = getAccessToken();

            VietQRRequest qrRequest = VietQRRequest.createForQR(
                    orderId, amount,
                    config.getBankBin(),
                    config.getBankAccount(),
                    config.getAccountName());

            VietQRResponse qrResponse = interaction.postQrRequest(qrRequest, token);

            if ("FAILED".equalsIgnoreCase(qrResponse.status())) {
                String errorCode = qrResponse.code() != null ? qrResponse.code() : "FAILED";
                throw mapToSemanticException(errorCode, qrResponse.message(), null);
            }

            if (qrResponse.code() != null && !"00".equals(qrResponse.code())) {
                throw mapToSemanticException(qrResponse.code(), qrResponse.desc(), null);
            }

            return gson.toJson(qrResponse);

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentProcessingException("QR_GENERATION_FAILED", "VIETQR", e);
        }
    }

    @Override
    public void simulatePayment(String orderId, long amount, String content) throws PaymentException {
        try {
            String token = getAccessToken();
            interaction.postSimulationRequest(
                    orderId, amount, content,
                    config.getBankCode(),
                    config.getBankAccount(),
                    token);
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            String rawError = e.getMessage();
            if (rawError != null && (rawError.contains("ngrok") || rawError.contains("offline"))) {
                throw new PaymentTimeoutException(
                        "Payment callback server is offline. Please start ngrok and try again.",
                        "E222", "VIETQR");
            }
            throw new PaymentProcessingException("SIMULATION_FAILED", "VIETQR", e);
        }
    }

    private PaymentException mapToSemanticException(String errorCode, String desc, Throwable cause) {
        System.err.println("[VietQRSubsystem] Error [" + errorCode + "]: " + desc);

        if (errorCode == null) {
            return new PaymentProcessingException("UNKNOWN", "VIETQR");
        }

        return switch (errorCode.toUpperCase()) {
            case "E74" -> cause != null
                    ? new PaymentAuthenticationException(errorCode, "VIETQR", cause)
                    : new PaymentAuthenticationException(errorCode, "VIETQR");
            case "E222" -> cause != null
                    ? new PaymentTimeoutException(errorCode, "VIETQR", cause)
                    : new PaymentTimeoutException(errorCode, "VIETQR");
            case "E05", "E76" -> cause != null
                    ? new PaymentProcessingException(errorCode, "VIETQR", cause)
                    : new PaymentProcessingException(errorCode, "VIETQR");
            default -> cause != null
                    ? new PaymentProcessingException(errorCode, "VIETQR", cause)
                    : new PaymentProcessingException(errorCode, "VIETQR");
        };
    }
}
