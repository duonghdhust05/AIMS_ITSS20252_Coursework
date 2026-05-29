package com.aimsfx.controller;

import com.aimsfx.exception.PaymentException;
import com.aimsfx.service.PaymentService;
import com.aimsfx.subsystem.paypal.IPaymentGateway;
import com.aimsfx.subsystem.paypal.IPayPalView;
import com.aimsfx.subsystem.vietqr.IPaymentQRCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayOrderController
 * Tests controller delegation to subsystems
 */
@ExtendWith(MockitoExtension.class)
class PayOrderControllerTest {

    @Mock
    private IPaymentQRCode mockVietQRSubsystem;

    @Mock
    private IPaymentGateway mockPayPalSubsystem;

    @Mock
    private IPayPalView mockPayPalView;

    @Mock
    private PaymentService mockPaymentService;

    private PayOrderController controller;

    @BeforeEach
    void setUp() {
        controller = new PayOrderController(
                mockVietQRSubsystem,
                mockPayPalSubsystem,
                mockPayPalView,
                mockPaymentService);
    }

    @Test
    @DisplayName("Pay Order – Controller delegates to VietQR subsystem")
    void Pay_Order_Controller_delegates_to_VietQR() throws PaymentException {
        // Arrange
        String expectedJson = "{\"qrData\":\"base64...\"}";
        when(mockVietQRSubsystem.generateQRCode(eq("ORDER001"), eq(100000L), eq("Payment")))
                .thenReturn(expectedJson);

        // Act
        String result = controller.requestPayment("ORDER001", 100000.0, "Payment");

        // Assert
        assertEquals(expectedJson, result);
        verify(mockVietQRSubsystem).generateQRCode("ORDER001", 100000L, "Payment");
    }

    @Test
    @DisplayName("Pay by Credit Card – Controller delegates to PayPal subsystem")
    void Pay_by_Credit_Card_Controller_delegates_to_PayPal() throws PaymentException {
        // Arrange
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("orderId", "PAYPAL123");
        mockResponse.put("approveUrl", "https://paypal.com/approve");
        when(mockPayPalSubsystem.createOrder(eq("ORDER001"), eq(100000.0)))
                .thenReturn(mockResponse);

        // Act & Assert
        assertDoesNotThrow(() -> {
            controller.requestPayPalPayment(
                    "ORDER001", 100000.0,
                    (id) -> {
                    }, (err) -> {
                    }, () -> {
                    });
        });

        // Verify PayPal was called
        verify(mockPayPalSubsystem, timeout(1000)).createOrder("ORDER001", 100000.0);
    }
}
