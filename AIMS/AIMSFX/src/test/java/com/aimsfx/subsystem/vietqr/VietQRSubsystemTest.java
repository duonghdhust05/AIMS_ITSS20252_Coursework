package com.aimsfx.subsystem.vietqr;

import com.aimsfx.exception.PaymentException;
import com.aimsfx.subsystem.vietqr.model.VietQRRequest;
import com.aimsfx.subsystem.vietqr.model.VietQRResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VietQRSubsystem
 * UC1: Pay Order (VietQR)
 */
@ExtendWith(MockitoExtension.class)
class VietQRSubsystemTest {

    @Mock
    private VietQRInteraction mockInteraction;

    @Mock
    private VietQRConfig mockConfig;

    private VietQRSubsystem subsystem;

    @BeforeEach
    void setUp() {
        subsystem = new VietQRSubsystem(mockInteraction, mockConfig);
    }

    @Test
    @DisplayName("Pay Order – VietQR generates QR code successfully")
    void Pay_Order_VietQR_generates_QR_code_successfully() throws Exception {
        // Arrange: Mock token and QR response
        VietQRResponse tokenResponse = VietQRResponse.createTokenSuccess("test-token", 300);
        when(mockInteraction.postTokenRequest()).thenReturn(tokenResponse);
        when(mockConfig.getBankBin()).thenReturn("970422");
        when(mockConfig.getBankAccount()).thenReturn("123456789");
        when(mockConfig.getAccountName()).thenReturn("Test Account");

        VietQRResponse qrResponse = new VietQRResponse(
                "success", null, null, null, null,
                "base64QRCodeData", "https://qrlink.com", "TXN123",
                null, null, null, null, null, null, null, null,
                "00", "Success");
        when(mockInteraction.postQrRequest(any(VietQRRequest.class), eq("test-token")))
                .thenReturn(qrResponse);

        // Act
        String result = subsystem.generateQRCode("ORDER001", 100000L, "Payment");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("base64QRCodeData"));
        verify(mockInteraction).postQrRequest(any(), eq("test-token"));
    }

    @Test
    @DisplayName("Pay Order – VietQR authentication error (E74)")
    void Pay_Order_VietQR_authentication_error() throws Exception {
        // Arrange: Mock token and error response
        VietQRResponse tokenResponse = VietQRResponse.createTokenSuccess("test-token", 300);
        when(mockInteraction.postTokenRequest()).thenReturn(tokenResponse);
        when(mockConfig.getBankBin()).thenReturn("970422");
        when(mockConfig.getBankAccount()).thenReturn("123456789");
        when(mockConfig.getAccountName()).thenReturn("Test");

        VietQRResponse errorResponse = VietQRResponse.createTokenError("E76", "Invalid request");
        when(mockInteraction.postQrRequest(any(), any())).thenReturn(errorResponse);

        // Act & Assert
        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> subsystem.generateQRCode("ORDER001", 100000L, "Payment"));

        assertEquals("VIETQR", exception.getProvider());
    }
}
