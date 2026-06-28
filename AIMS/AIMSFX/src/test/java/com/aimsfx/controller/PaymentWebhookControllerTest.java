package com.aimsfx.controller;

import com.aimsfx.service.webhook.IPaymentWebhookHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentWebhookController
 * Maps to Phase 4 of Test Plan
 */
@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

    @Mock
    private IPaymentWebhookHandler mockHandler;

    @Mock
    private HttpServletRequest mockRequest;

    private PaymentWebhookController controller;

    @BeforeEach
    void setUp() {
        List<IPaymentWebhookHandler> handlers = Collections.singletonList(mockHandler);
        controller = new PaymentWebhookController(handlers);
    }

    @Test
    @DisplayName("[UT_PAY_005] Webhook - Successful Payment Confirmation")
    void testHandlePaymentCallback_Success() {
        // Arrange
        String gatewayName = "paypal";
        String payload = "{\"status\":\"COMPLETED\"}";
        
        when(mockHandler.supports("paypal")).thenReturn(true);
        doReturn(ResponseEntity.ok("Success"))
                .when(mockHandler).handleWebhook(payload, mockRequest);

        // Act
        ResponseEntity<?> response = controller.handlePaymentCallback(gatewayName, payload, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());
        verify(mockHandler).supports("paypal");
        verify(mockHandler).handleWebhook(payload, mockRequest);
    }

    @Test
    @DisplayName("[UT_PAY_006] Webhook - Invalid Signature / Fraudulent Callback")
    void testHandlePaymentCallback_InvalidOrUnsupported() {
        // Arrange
        String gatewayName = "unknownGateway";
        String payload = "{\"status\":\"INVALID\"}";
        
        when(mockHandler.supports("unknownGateway")).thenReturn(false);
        // Note: For unsupported gateway, the controller returns badRequest
        // For invalid signature but supported gateway, the handler would return 400 or 401. Let's test unsupported.

        // Act
        ResponseEntity<?> response = controller.handlePaymentCallback(gatewayName, payload, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body);
        assertTrue(body.toString().contains("Unsupported payment gateway"));
        verify(mockHandler).supports("unknownGateway");
        verify(mockHandler, never()).handleWebhook(anyString(), any(HttpServletRequest.class));
    }
    
    @Test
    @DisplayName("[UT_PAY_006] Webhook - Supported Gateway but Invalid Signature")
    void testHandlePaymentCallback_SupportedButInvalid() {
        // Arrange
        String gatewayName = "paypal";
        String payload = "{\"status\":\"COMPLETED\"}"; // but invalid signature
        
        when(mockHandler.supports("paypal")).thenReturn(true);
        doReturn(ResponseEntity.badRequest().body("Invalid signature"))
                .when(mockHandler).handleWebhook(payload, mockRequest);

        // Act
        ResponseEntity<?> response = controller.handlePaymentCallback(gatewayName, payload, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid signature", response.getBody());
        verify(mockHandler).supports("paypal");
        verify(mockHandler).handleWebhook(payload, mockRequest);
    }
}
