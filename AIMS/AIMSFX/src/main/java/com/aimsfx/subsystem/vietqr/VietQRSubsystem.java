package com.aimsfx.subsystem.vietqr;

import com.aimsfx.exception.*;
import com.aimsfx.subsystem.vietqr.exception.VietQRApiException;
import com.aimsfx.subsystem.vietqr.exception.VietQRAuthException;
import com.aimsfx.subsystem.vietqr.exception.VietQRNetworkException;
import com.aimsfx.subsystem.vietqr.model.VietQRRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String cachedToken;
    private long tokenExpiryTime = 0;

    public VietQRSubsystem(VietQRInteraction interaction, VietQRConfig config) {
        this.interaction = interaction;
        this.config = config;
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
        } catch (VietQRNetworkException e) {
            throw new PaymentTimeoutException("E222", "VIETQR", e);
        } catch (VietQRAuthException e) {
            throw new PaymentAuthenticationException("E74", "VIETQR", e);
        } catch (VietQRApiException e) {
            throw new PaymentProcessingException("E05", "VIETQR", e);
        } catch (Exception e) {
            throw new PaymentProcessingException("UNKNOWN_ERROR", "VIETQR", e);
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

            ObjectNode jsonResponse = objectMapper.valueToTree(qrResponse);
            jsonResponse.put("bankCode", config.getBankCode());
            jsonResponse.put("bankAccount", config.getBankAccount());
            jsonResponse.put("accountName", config.getAccountName());
            jsonResponse.put("amount", String.valueOf(amount));
            jsonResponse.put("content", content);

            return objectMapper.writeValueAsString(jsonResponse);

        } catch (PaymentException e) {
            throw e;
        } catch (VietQRNetworkException e) {
            throw new PaymentTimeoutException("E222", "VIETQR", e);
        } catch (VietQRApiException e) {
            throw new PaymentProcessingException("QR_GENERATION_FAILED", "VIETQR", e);
        } catch (Exception e) {
            throw new PaymentProcessingException("QR_GENERATION_FAILED", "VIETQR", e);
        }
    }

    @Override
    public void simulatePayment(String orderId, long amount, String content) throws PaymentException {
        try {
            String token = getAccessToken();
            interaction.postSimulationRequest(
                    "TRANSFER", amount, content,
                    config.getBankCode(),
                    config.getBankAccount(),
                    token);
        } catch (PaymentException e) {
            throw e;
        } catch (VietQRNetworkException e) {
            throw new PaymentTimeoutException(
                    "Payment callback server is offline or unreachable. Please check your connection.",
                    "E222", "VIETQR");
        } catch (Exception e) {
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