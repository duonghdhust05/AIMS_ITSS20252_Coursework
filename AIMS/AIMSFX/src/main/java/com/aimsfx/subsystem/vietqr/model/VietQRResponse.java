package com.aimsfx.subsystem.vietqr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VietQRResponse(
        // --- Token Response Fields ---
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Integer expiresIn,

        // --- QR Code Data Fields ---
        @JsonProperty("qrCode") String qrCode,
        @JsonProperty("qrLink") String qrLink,
        @JsonProperty("transactionId") String transactionId,

        // --- MISSING FIELDS ADDED HERE ---
        @JsonProperty("bankCode") String bankCode,
        @JsonProperty("bankAccount") String bankAccount,
        @JsonProperty("bankName") String bankName,
        @JsonProperty("userBankName") String userBankName,

        @JsonProperty("amount") String amount,
        @JsonProperty("content") String content,
        @JsonProperty("imgId") String imgId,
        @JsonProperty("orderId") String orderId,

        // --- Error / Status Codes ---
        @JsonProperty("code") String code,
        @JsonProperty("desc") String desc
) {
    /**
     * Helper to create a success response for Token requests (Internal use)
     */
    public static VietQRResponse createTokenSuccess(String token, Integer expirySeconds) {
        return new VietQRResponse(
                "success", null,
                token, "Bearer", expirySeconds,
                null, null, null,
                null, null, null, null, // bank details
                null, null, null, null, // amount, content, imgId, orderId
                "00", "Success"
        );
    }

    /**
     * Helper to create an error response (Internal use)
     */
    public static VietQRResponse createTokenError(String errorCode, String errorDesc) {
        return new VietQRResponse(
                errorCode, errorDesc,
                null, null, null,
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                errorCode, errorDesc
        );
    }

    public String getErrorMessage() {
        if (message != null && !message.isEmpty()) return message;
        if (desc != null && !desc.isEmpty()) return desc;
        if (code != null) return "Error Code: " + code;
        return "Unknown Error";
    }
}