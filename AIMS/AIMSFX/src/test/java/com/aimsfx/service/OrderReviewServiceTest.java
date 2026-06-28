package com.aimsfx.service;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.repository.OrderQueryRepository;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Phase 2: Business Logic / Service Layer Tests
 * Test Suite: TS_MNO_SERVICE
 */
public class OrderReviewServiceTest {

    private OrderReviewService orderReviewService;
    private OrderQueryRepository mockQueryRepo;
    private OrderRepository mockCommandRepo;
    private ProductRepository mockProductRepo;
    private EmailSenderService mockEmailSender;
    private RefundService mockRefundService;

    @BeforeEach
    void setUp() {
        mockQueryRepo = Mockito.mock(OrderQueryRepository.class);
        mockCommandRepo = Mockito.mock(OrderRepository.class);
        mockProductRepo = Mockito.mock(ProductRepository.class);
        mockEmailSender = Mockito.mock(EmailSenderService.class);
        mockRefundService = Mockito.mock(RefundService.class);

        orderReviewService = new OrderReviewService(
                mockQueryRepo,
                mockCommandRepo,
                mockProductRepo,
                mockEmailSender,
                mockRefundService
        );
    }

    @Test
    void testUT_MNO_02_ServiceLoadPendingOrders() throws SQLException {
        // Arrange
        List<OrderSummary> mockList = List.of(new OrderSummary(), new OrderSummary());
        when(mockQueryRepo.findByStatus(eq(com.aimsfx.model.OrderStatus.PENDING_REVIEW), anyInt(), anyInt()))
                .thenReturn(mockList);

        // Act
        List<OrderSummary> result = orderReviewService.listPendingReviewOrders(0, 30);

        // Assert
        assertEquals(2, result.size());
        verify(mockQueryRepo, times(1)).findByStatus(eq(com.aimsfx.model.OrderStatus.PENDING_REVIEW), eq(30), eq(0));
    }

    @Test
    void testUT_MNO_03_ServiceApproveOrderSuccess() throws SQLException {
        // Arrange
        int orderId = 1;
        Order mockOrder = new Order();
        mockOrder.setOrderId(orderId);
        
        when(mockCommandRepo.updateOrderStatusWithCheck(eq(orderId), eq("APPROVED"), eq("PENDING"), isNull()))
                .thenReturn(true);
        when(mockCommandRepo.findById(orderId)).thenReturn(mockOrder);

        // Act
        orderReviewService.approve(orderId);

        // Assert
        verify(mockCommandRepo, times(1)).updateOrderStatusWithCheck(eq(orderId), eq("APPROVED"), eq("PENDING"), isNull());
        verify(mockCommandRepo, times(1)).findById(orderId);
    }

    @Test
    void testUT_MNO_04_ServiceRejectOrderSuccess() throws SQLException {
        // Arrange
        int orderId = 1;
        String reason = "Out of stock";
        Order mockOrder = new Order();
        mockOrder.setOrderId(orderId);
        mockOrder.setStatus("PENDING");
        
        when(mockCommandRepo.findById(orderId)).thenReturn(mockOrder);
        when(mockCommandRepo.updateOrderStatusWithCheck(eq(orderId), eq("REJECTED"), eq("PENDING"), eq(reason)))
                .thenReturn(true);

        // Act
        orderReviewService.reject(orderId, reason);

        // Assert
        verify(mockCommandRepo, times(1)).updateOrderStatusWithCheck(eq(orderId), eq("REJECTED"), eq("PENDING"), eq(reason));
        verify(mockProductRepo, times(1)).restoreStockForOrder(any());
        verify(mockRefundService, times(1)).processRefundIfPaid(orderId);
    }

    @Test
    void testUT_MNO_05_ServiceRejectOrderNotFound() throws SQLException {
        // Arrange
        int orderId = 999;
        when(mockCommandRepo.findById(orderId)).thenReturn(null);

        // Act
        orderReviewService.reject(orderId, "Reason");

        // Assert
        verify(mockCommandRepo, never()).updateOrderStatusWithCheck(anyInt(), anyString(), anyString(), anyString());
    }
}
