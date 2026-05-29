package com.aimsfx.subsystem.vietqr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the JSON body sent by VietQR during the Transaction Sync callback.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VietQRCallbackRequest(
        @JsonProperty("transactionid") String transactionId,
        @JsonProperty("transactiontime") Long transactionTime,
        @JsonProperty("referencenumber") String referenceNumber,
        @JsonProperty("amount") Long amount,
        @JsonProperty("content") String content,
        @JsonProperty("bankaccount") String bankAccount,
        @JsonProperty("orderId") String orderId,
        @JsonProperty("transType") String transType,
        @JsonProperty("terminalCode") String terminalCode,
        @JsonProperty("sign") String sign
) {}