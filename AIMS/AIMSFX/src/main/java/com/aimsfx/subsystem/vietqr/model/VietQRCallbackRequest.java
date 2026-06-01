package com.aimsfx.subsystem.vietqr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the JSON body sent by VietQR during the Transaction Sync callback.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VietQRCallbackRequest(
                @JsonProperty("transactionid") String transactionid,
                @JsonProperty("transactiontime") Long transactiontime,
                @JsonProperty("referencenumber") String referencenumber,
                @JsonProperty("amount") Long amount,
                @JsonProperty("content") String content,
                @JsonProperty("bankAccount") String bankAccount,
                @JsonProperty("orderId") String orderId,
                @JsonProperty("transType") String transType,
                @JsonProperty("terminalCode") String terminalCode,
                @JsonProperty("sign") String sign,
                @JsonProperty("bankCode") String bankCode) {
}