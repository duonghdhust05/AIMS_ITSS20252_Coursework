package com.aimsfx.service;

import com.aimsfx.factory.PaymentControllerFactory;
import com.aimsfx.model.Order;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.repository.TransactionRepository;
import com.aimsfx.subsystem.paypal.IPaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefundServiceTest {

    private RefundService refundService;
    private OrderRepository orderRepositoryMock;
    private TransactionRepository transactionRepositoryMock;
    private IPaymentGateway payPalGatewayMock;

    @BeforeEach
    void setUp() {
        // Reset static factory
        PaymentControllerFactory.reset();

        orderRepositoryMock = mock(OrderRepository.class);
        transactionRepositoryMock = mock(TransactionRepository.class);
        payPalGatewayMock = mock(IPaymentGateway.class);
    }

    // A small helper to inject mocks into the service using reflection
    // Since RefundService creates its own repositories in the constructor,
    // we would normally refactor it to accept them via DI, but for now we use
    // reflection.
    private void injectMocks() throws Exception {
        refundService = new RefundService();
        java.lang.reflect.Field txRepoField = RefundService.class.getDeclaredField("transactionRepository");
        txRepoField.setAccessible(true);
        txRepoField.set(refundService, transactionRepositoryMock);

        java.lang.reflect.Field orderRepoField = RefundService.class.getDeclaredField("orderRepository");
        orderRepoField.setAccessible(true);
        orderRepoField.set(refundService, orderRepositoryMock);
    }

    @Test
    void processRefundIfPaid_OrderNotFound_ReturnsFalse() throws Exception {
        injectMocks();
        when(orderRepositoryMock.findById(1)).thenReturn(null);

        boolean result = refundService.processRefundIfPaid(1);

        assertFalse(result);
        verify(orderRepositoryMock, times(1)).findById(1);
    }

    @Test
    void processRefundIfPaid_VietQROrder_ReturnsTrue_NoExternalCall() throws Exception {
        injectMocks();
        Order order = new Order();
        order.setOrderId(1);
        order.setStatus("REJECTED"); // status might be rejected

        // We have to mock the private methods or we can just mock the
        // connection/statement
        // to return VietQR. But it's easier to mock the whole RefundService using a spy
        // if we didn't want to deal with DB in private methods.
        // Let's create a partial mock of RefundService to avoid DB calls in
        // getOrderPaymentMethod.
        RefundService spyService = spy(refundService);
        doReturn("VIETQR").when(spyService).getOrderPaymentMethod(1);
        doReturn("COMPLETED").when(spyService).getOrderPaymentStatus(1);
        doReturn(order).when(orderRepositoryMock).findById(1);

        boolean result = spyService.processRefundIfPaid(1);

        assertTrue(result);
    }

    @Test
    void processRefundIfPaid_PayPalCompletedOrder_CallsRefundOrder() throws Exception {
        injectMocks();
        Order order = new Order();
        order.setOrderId(2);

        RefundService spyService = spy(refundService);
        doReturn("PAYPAL").when(spyService).getOrderPaymentMethod(2);
        doReturn("COMPLETED").when(spyService).getOrderPaymentStatus(2);
        doReturn(order).when(orderRepositoryMock).findById(2);

        // Mock getExternalTransactionIdByOrderId
        when(transactionRepositoryMock.getExternalTransactionIdByOrderId(2)).thenReturn("PAYPAL_ORD_123");
        when(payPalGatewayMock.refundOrder("PAYPAL_ORD_123")).thenReturn(true);
        when(transactionRepositoryMock.findByOrderId(2)).thenReturn(99);

        try (MockedStatic<PaymentControllerFactory> factoryMock = mockStatic(PaymentControllerFactory.class)) {
            factoryMock.when(PaymentControllerFactory::getPayPalGateway).thenReturn(payPalGatewayMock);

            boolean result = spyService.processRefundIfPaid(2);

            assertTrue(result);
            verify(payPalGatewayMock, times(1)).refundOrder("PAYPAL_ORD_123");
            verify(transactionRepositoryMock, times(1)).updateStatus(99, "REFUNDED");
        }
    }
}
