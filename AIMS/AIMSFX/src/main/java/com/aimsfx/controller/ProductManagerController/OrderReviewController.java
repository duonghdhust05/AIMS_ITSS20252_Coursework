package com.aimsfx.controller;

import com.aimsfx.model.OrderDetail;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.service.OrderReviewService;
import com.aimsfx.utils.SessionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Controller responsible for managing order review business logic.
 * Enforces MVC pattern by handling data fetching, authorization, and background tasks.
 */
public class OrderReviewController {

    private final OrderReviewService orderReviewService;
    private ScheduledExecutorService scheduler;
    private int lastLoadedCount = -1;

    public OrderReviewController() {
        this.orderReviewService = new OrderReviewService();
    }

    /**
     * Checks if the current session has permission to manage orders.
     */
    public boolean canManageOrders() {
        return SessionManager.getInstance().canManageOrders();
    }

    /**
     * Retrieves the total count of orders.
     */
    public int countAllOrders() throws SQLException {
        int count = orderReviewService.countAllOrders();
        lastLoadedCount = count;
        return count;
    }

    /**
     * Retrieves a paginated list of orders.
     */
    public List<OrderSummary> loadOrders(int pageIndex, int pageSize) throws SQLException {
        return orderReviewService.listAllOrders(pageIndex, pageSize);
    }

    /**
     * Retrieves the details of a specific order.
     */
    public OrderDetail getOrderDetail(int orderId) throws SQLException {
        return orderReviewService.getOrderDetail(orderId);
    }

    /**
     * Approves the specified order.
     */
    public void approveOrder(int orderId) throws SQLException {
        orderReviewService.approve(orderId);
    }

    /**
     * Rejects the specified order with a given reason.
     */
    public void rejectOrder(int orderId, String reason) throws SQLException {
        orderReviewService.reject(orderId, reason);
    }

    /**
     * Starts background polling for new orders and invokes the callback when a change is detected.
     * @param onOrderCountChanged Callback to execute when new orders are found.
     */
    public void startBackgroundPolling(Consumer<Integer> onOrderCountChanged) {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                int currentCount = orderReviewService.countAllOrders();
                if (lastLoadedCount != -1 && currentCount != lastLoadedCount) {
                    onOrderCountChanged.accept(currentCount);
                }
            } catch (SQLException e) {
                // Ignore background polling errors
            }
        }, 15, 15, TimeUnit.SECONDS);
    }

    /**
     * Shuts down the background polling scheduler.
     */
    public void stopBackgroundPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
