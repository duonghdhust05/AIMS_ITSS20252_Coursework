package com.aimsfx.subsystem.vietqr.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response structure for the Transaction Sync callback.
 * Matches VietQR documentation requirements.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VietQRCallbackResponse(
                @JsonProperty("error") boolean error,
                @JsonProperty("errorReason") String errorReason,
                @JsonProperty("toastMessage") String toastMessage,
                @JsonProperty("object") TransactionResponseObject object) {
        // Nested record for the 'object' field
        public record TransactionResponseObject(
                        @JsonProperty("reftransactionid") String reftransactionid) {
        }

        // --- Factory Methods ---

        public static VietQRCallbackResponse success(String transactionId) {
                return new VietQRCallbackResponse(
                                false,
                                null,
                                "Success",
                                new TransactionResponseObject(transactionId));
        }

        public static VietQRCallbackResponse error(String code, String message) {
                return new VietQRCallbackResponse(
                                true,
                                code,
                                message,
                                null);
        }
}