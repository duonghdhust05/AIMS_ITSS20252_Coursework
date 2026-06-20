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
 * - Trigger email notification on decision (hook only; content handled by
 * EmailSenderService)
 */
public class OrderReviewService {
    public static final int DEFAULT_PAGE_SIZE = 30;

    private final OrderQueryRepository queryRepository;
    private final OrderRepository commandRepository;
    private final com.aimsfx.repository.ProductRepository productRepository;
    private final EmailSenderService emailSender;
    private final RefundService refundService;

    public OrderReviewService() {
        this(new OrderQueryRepository(), new OrderRepository(), new com.aimsfx.repository.DatabaseProductRepository(),
                new EmailSenderService(), new RefundService());
    }

    public OrderReviewService(OrderQueryRepository queryRepository,
            OrderRepository commandRepository,
            com.aimsfx.repository.ProductRepository productRepository,
            EmailSenderService emailSender,
            RefundService refundService) {
        this.queryRepository = queryRepository;
        this.commandRepository = commandRepository;
        this.productRepository = productRepository;
        this.emailSender = emailSender;
        this.refundService = refundService;
    }

    public int countPendingReviewOrders() throws SQLException {
        return queryRepository.countByStatus(OrderStatus.PENDING_REVIEW);
    }

    public List<OrderSummary> listPendingReviewOrders(int pageIndex, int pageSize) throws SQLException {
        int safePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        int offset = Math.max(0, pageIndex) * safePageSize;
        return queryRepository.findByStatus(OrderStatus.PENDING_REVIEW, safePageSize, offset);
    }

    public int countAllOrders() throws SQLException {
        return queryRepository.countAll();
    }

    public List<OrderSummary> listAllOrders(int pageIndex, int pageSize) throws SQLException {
        int safePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        int offset = Math.max(0, pageIndex) * safePageSize;
        return queryRepository.findAll(safePageSize, offset);
    }

    public OrderDetail getOrderDetail(int orderId) throws SQLException {
        return queryRepository.findDetailById(orderId);
    }

    public Order getOrderById(int orderId) throws SQLException {
        return commandRepository.findById(orderId);
    }

    public void approve(int orderId) throws SQLException {
        boolean success = commandRepository.updateOrderStatusWithCheck(orderId, OrderStatus.APPROVED.toDbValue(),
                OrderStatus.PENDING_REVIEW.toDbValue(), null);

        if (success) {
            Order order = commandRepository.findById(orderId);
            if (order != null) {
                logAction(orderId, "APPROVE", "Approved by Product Manager");
                if (order.getDeliveryInfo() != null) {
                    emailSender.sendUpdateNotification(order, order.getDeliveryInfo().getEmail());
                }
            }
        }
    }

    public void reject(int orderId, String reason) throws SQLException {
        Order tempOrder = commandRepository.findById(orderId);
        if (tempOrder == null)
            return;
        String currentStatus = tempOrder.getStatus();
        boolean success = commandRepository.updateOrderStatusWithCheck(orderId, OrderStatus.REJECTED.toDbValue(),
                currentStatus, reason);

        if (success) {
            Order order = commandRepository.findById(orderId);
            if (order != null) {
                // Restore stock safely
                productRepository.restoreStockForOrder(order.getOrderItems());
                logAction(orderId, "REJECT", reason);
                if (order.getDeliveryInfo() != null) {
                    emailSender.sendUpdateNotification(order, order.getDeliveryInfo().getEmail());
                }

                // Attempt automated refund if paid
                refundService.processRefundIfPaid(orderId);
            }
        }
    }

    public void cancelByCustomer(int orderId) throws SQLException {
        boolean success = commandRepository.updateOrderStatusWithCheck(orderId, OrderStatus.CANCELLED.toDbValue(),
                OrderStatus.PENDING_REVIEW.toDbValue(), "Cancelled by customer");
        if (success) {
            Order order = commandRepository.findById(orderId);
            if (order != null) {
                // Restore stock safely
                productRepository.restoreStockForOrder(order.getOrderItems());
            }
        }
        // Refund intentionally excluded.
    }

    public void requestRefund(int orderId) throws SQLException {
        // Customer request refund from PENDING or PENDING_REVIEW
        Order tempOrder = commandRepository.findById(orderId);
        if (tempOrder == null)
            return;

        String currentStatus = tempOrder.getStatus();
        boolean success = commandRepository.updateOrderStatusWithCheck(orderId, OrderStatus.REFUND_REQUEST.toDbValue(),
                currentStatus, "Customer requested order cancellation/refund");

        if (success) {
            // Restore stock safely because order will not be delivered
            productRepository.restoreStockForOrder(tempOrder.getOrderItems());
            logAction(orderId, "REFUND_REQUEST", "Customer requested order cancellation/refund");
        }
    }

    private void logAction(int orderId, String action, String reason) {
        try {
            new com.aimsfx.repository.OrderActionLogRepository().logAction(orderId, action, reason);
        } catch (SQLException e) {
            System.err.println("Failed to log action: " + e.getMessage());
        }
    }
}