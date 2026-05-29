package com.aimsfx.service;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderDetail;
import com.aimsfx.model.OrderStatus;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.repository.OrderQueryRepository;
import com.aimsfx.repository.OrderRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * OrderReviewService - business service for Product Manager order review.
 *
 * Scope (per your requirement, refund excluded):
 * - Paging pending orders (30/page)
 * - View order detail
 * - Approve / Reject order (even if stock is sufficient)
 * - Trigger email notification on decision (hook only; content handled by EmailSenderService)
 */
public class OrderReviewService {
    public static final int DEFAULT_PAGE_SIZE = 30;

    private final OrderQueryRepository queryRepository;
    private final OrderRepository commandRepository;
    private final EmailSenderService emailSender;

    public OrderReviewService() {
        this(new OrderQueryRepository(), new OrderRepository(), new EmailSenderService());
    }

    public OrderReviewService(OrderQueryRepository queryRepository,
                              OrderRepository commandRepository,
                              EmailSenderService emailSender) {
        this.queryRepository = queryRepository;
        this.commandRepository = commandRepository;
        this.emailSender = emailSender;
    }

    public int countPendingReviewOrders() throws SQLException {
        return queryRepository.countByStatus(OrderStatus.PENDING_REVIEW);
    }

    public List<OrderSummary> listPendingReviewOrders(int pageIndex, int pageSize) throws SQLException {
        int safePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        int offset = Math.max(0, pageIndex) * safePageSize;
        return queryRepository.findByStatus(OrderStatus.PENDING_REVIEW, safePageSize, offset);
    }

    public OrderDetail getOrderDetail(int orderId) throws SQLException {
        return queryRepository.findDetailById(orderId);
    }

    public void approve(int orderId) throws SQLException {
        commandRepository.updateOrderStatus(orderId, OrderStatus.APPROVED.toDbValue());
        
        Order order = commandRepository.findById(orderId);
        if (order != null) {
            logAction(orderId, "APPROVE", "Approved by Product Manager");
            if (order.getDeliveryInfo() != null) {
                emailSender.sendUpdateNotification(order, order.getDeliveryInfo().getEmail());
            }
        }
    }

    public void reject(int orderId, String reason) throws SQLException {
        commandRepository.updateOrderStatus(orderId, OrderStatus.REJECTED.toDbValue(), reason);
        
        Order order = commandRepository.findById(orderId);
        if (order != null) {
            logAction(orderId, "REJECT", reason);
            if (order.getDeliveryInfo() != null) {
                emailSender.sendUpdateNotification(order, order.getDeliveryInfo().getEmail());
            }
        }
        // Refund intentionally excluded.
    }

    public void cancelByCustomer(int orderId) throws SQLException {
        commandRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED.toDbValue());
        // Refund intentionally excluded.
    }
    
    private void logAction(int orderId, String action, String reason) {
        try {
            new com.aimsfx.repository.OrderActionLogRepository().logAction(orderId, action, reason);
        } catch (SQLException e) {
            System.err.println("Failed to log action: " + e.getMessage());
        }
    }
}