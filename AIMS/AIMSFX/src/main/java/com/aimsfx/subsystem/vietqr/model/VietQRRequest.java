package com.aimsfx.subsystem.vietqr.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List; // Required for List<Object> type

/**
 * STRICTLY UPDATED Request Model based on VietQR documentation.
 * Uses @JsonInclude(NON_NULL) to send only the fields that are set.
 */
@JsonInclude(Include.NON_NULL)
public record VietQRRequest(
        @JsonProperty("bankCode") String bankCode,
        @JsonProperty("bankAccount") String bankAccount,
        @JsonProperty("userBankName") String userBankName,
        @JsonProperty("content") String content,
        @JsonProperty("qrType") Integer qrType, // Type: Integer

        // --- DYNAMIC QR (qrType=0) MANDATORY FIELDS ---
        @JsonProperty("amount") Long amount,               // Type: Long
        @JsonProperty("orderId") String orderId,          // Type: String
        @JsonProperty("transType") String transType,        // Type: String (Bắt buộc nếu qrType=0, Mặc định 'C')

        // --- OTHER OPTIONAL FIELDS ---
        @JsonProperty("terminalCode") String terminalCode,
        @JsonProperty("serviceCode") String serviceCode,
        @JsonProperty("subTerminalCode") String subTerminalCode,
        @JsonProperty("sign") String sign,
        @JsonProperty("urlLink") String urlLink,
        @JsonProperty("note") String note,
        @JsonProperty("additionalData") List<Object> additionalData

) {
    /**
     * Factory method for creating a complete Dynamic QR Request (qrType=0).
     */
    /**
     * Factory method now accepts Bank Details dynamically.
     */
    public static VietQRRequest createForQR(
            String orderId, long amount,
            String bankBin, String bankAccount, String accountName
    ) {
        return new VietQRRequest(
                bankBin,        // 1. bankCode (BIN)
                bankAccount,    // 2. bankAccount
                accountName,    // 3. userBankName
                "AIMS-" + orderId,
                0,
                amount,
                orderId,
                "C",
                null, null, null, null, null, null, null
        );
    }
}