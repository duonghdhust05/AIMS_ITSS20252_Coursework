package com.aimsfx.subsystem.paypal;

import com.aimsfx.exception.PaymentDeclinedException;
import com.aimsfx.exception.PaymentException;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayPalSubsystem
 * UC2: Pay by Credit Card (PayPal)
 */
@ExtendWith(MockitoExtension.class)
class PayPalSubsystemTest {

    @Mock
    private PaypalServerSdkClient mockClient;

    @Mock
    private OrdersController mockOrdersController;

    @Mock
    private CurrencyConverter mockConverter;

    @Mock
    private ApiResponse<Order> mockApiResponse;

    private PayPalSubsystem subsystem;

    @BeforeEach
    void setUp() {
        when(mockClient.getOrdersController()).thenReturn(mockOrdersController);
        subsystem = new PayPalSubsystem(mockClient, mockConverter);
    }

    @Test
    @DisplayName("Pay by Credit Card – PayPal creates order successfully")
    void Pay_by_Credit_Card_PayPal_creates_order_successfully() throws Exception {
        // Arrange
        when(mockConverter.convertVndToUsd(500000.0)).thenReturn(20.0);

        Order mockOrder = mock(Order.class);
        when(mockOrder.getId()).thenReturn("PAYPAL-ORDER-123");

        LinkDescription approveLink = mock(LinkDescription.class);
        when(approveLink.getRel()).thenReturn("approve");
        when(approveLink.getHref()).thenReturn("https://www.sandbox.paypal.com/approve");

        when(mockOrder.getLinks()).thenReturn(List.of(approveLink));
        when(mockApiResponse.getResult()).thenReturn(mockOrder);
        when(mockOrdersController.createOrder(any(CreateOrderInput.class))).thenReturn(mockApiResponse);

        // Act
        Map<String, String> result = subsystem.createOrder("ORDER-001", 500000.0);

        // Assert
        assertNotNull(result);
        assertEquals("PAYPAL-ORDER-123", result.get("orderId"));
        assertTrue(result.get("approveUrl").contains("paypal.com"));
    }

    @Test
    @DisplayName("Pay by Credit Card – PayPal payment declined")
    void Pay_by_Credit_Card_PayPal_payment_declined() throws Exception {
        // Arrange
        when(mockConverter.convertVndToUsd(anyDouble())).thenReturn(20.0);
        when(mockOrdersController.createOrder(any(CreateOrderInput.class)))
                .thenThrow(new RuntimeException("declined_by_payment_method"));

        // Act & Assert
        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> subsystem.createOrder("ORDER-001", 500000.0));

        assertEquals("PAYPAL", exception.getProvider());
        assertTrue(exception instanceof PaymentDeclinedException);
    }
}
