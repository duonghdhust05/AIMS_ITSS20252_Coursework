package com.aimsfx.controller;

import com.aimsfx.exception.PaymentException;
import com.aimsfx.exception.PaymentProcessingException;
import com.aimsfx.service.PaymentService;
import com.aimsfx.service.payment.IPaymentGateway;
import com.aimsfx.subsystem.paypal.IPayPalView;
import com.aimsfx.service.payment.IPaymentQRCode;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayOrderController
 * Maps to Phase 4 of Test Plan
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

    @BeforeAll
    static void initJfxRuntime() {
        // Platform.startup is needed for Platform.runLater in controller
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @BeforeEach
    void setUp() {
        controller = new PayOrderController(
                mockVietQRSubsystem,
                mockPayPalSubsystem,
                mockPayPalView,
                mockPaymentService);
    }

    @Test
    @DisplayName("[UT-PP-01] Initiate PayPal Success")
    void testRequestPayPalPayment_Success() throws PaymentException, InterruptedException {
        // Arrange
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("orderId", "PAYPAL123");
        mockResponse.put("approveUrl", "https://paypal.com/approve");
        when(mockPayPalSubsystem.createOrder(eq("ORDER001"), eq(50000.0)))
                .thenReturn(mockResponse);

        @SuppressWarnings("unchecked")
        Consumer<String> onSuccess = (Consumer<String>) mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<String> onError = (Consumer<String>) mock(Consumer.class);
        Runnable onCancel = mock(Runnable.class);

        // Act
        controller.requestPayPalPayment("ORDER001", 50000.0, onSuccess, onError, onCancel);

        // Wait for thread to finish
        Thread.sleep(200);

        // Assert
        verify(mockPayPalSubsystem).createOrder("ORDER001", 50000.0);
        // Verify Platform.runLater triggered view
        verify(mockPayPalView, timeout(500)).displayApprovalPage(eq("https://paypal.com/approve"), any(), any());
    }

    @Test
    @DisplayName("[UT-PP-02] Initiate PayPal API Error")
    void testRequestPayPalPayment_ApiError() throws PaymentException, InterruptedException {
        // Arrange
        when(mockPayPalSubsystem.createOrder(anyString(), anyDouble()))
                .thenThrow(new PaymentProcessingException("Bad Request Error", "400", "PAYPAL"));

        @SuppressWarnings("unchecked")
        Consumer<String> onSuccess = (Consumer<String>) mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<String> onError = (Consumer<String>) mock(Consumer.class);
        Runnable onCancel = mock(Runnable.class);

        // Act
        controller.requestPayPalPayment("ORDER001", 50000.0, onSuccess, onError, onCancel);

        // Wait for thread to finish
        Thread.sleep(200);

        // Assert
        verify(mockPayPalSubsystem).createOrder("ORDER001", 50000.0);
        // Error callback should be triggered
        verify(onError, timeout(500)).accept(contains("Bad Request Error"));
    }

    @Test
    @DisplayName("[UT_PAY_008] User Cancels Payment on PayPal Screen")
    void testRequestPayPalPayment_UserCancel() throws PaymentException, InterruptedException {
        // Arrange
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("orderId", "PAYPAL123");
        mockResponse.put("approveUrl", "https://paypal.com/approve");
        when(mockPayPalSubsystem.createOrder(anyString(), anyDouble()))
                .thenReturn(mockResponse);

        // Mock the displayApprovalPage to immediately trigger the cancel callback
        // (Runnable)
        doAnswer(invocation -> {
            Runnable cancelCallback = invocation.getArgument(2);
            cancelCallback.run();
            return null;
        }).when(mockPayPalView).displayApprovalPage(anyString(), any(), any());

        @SuppressWarnings("unchecked")
        Consumer<String> onSuccess = (Consumer<String>) mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<String> onError = (Consumer<String>) mock(Consumer.class);
        Runnable onCancel = mock(Runnable.class);

        // Act
        controller.requestPayPalPayment("ORDER001", 50000.0, onSuccess, onError, onCancel);

        // Wait for thread to finish
        Thread.sleep(200);

        // Assert
        verify(onCancel, timeout(500)).run();
    }
}
