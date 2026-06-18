package com.aimsfx.subsystem.vietqr.exception;

public class VietQRNetworkException extends VietQRApiException {
    public VietQRNetworkException(String message) {
        super(message);
    }

    public VietQRNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
