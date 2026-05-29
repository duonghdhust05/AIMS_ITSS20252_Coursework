package com.aimsfx.service;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderItem;
import com.aimsfx.model.Product;
import com.aimsfx.repository.DatabaseProductRepository;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.repository.ProductRepository;
import com.aimsfx.repository.TransactionRepository;

import java.sql.SQLException;

/**
 * PaymentService Class
 * 
 * SOLID Compliance:
 * - SRP: Single responsibility - handle payment transaction persistence
 * - DIP: Repositories injected via constructor
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward payment transaction management
 * 
 * COUPLING: LOW - Data Coupling
 * - Uses repositories through method calls with primitive parameters
 */
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    public PaymentService() {
        this.transactionRepository = TransactionRepository.getInstance();
        this.orderRepository = new OrderRepository();
    }

    public PaymentService(TransactionRepository transactionRepository, OrderRepository orderRepository) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Process payment: create transaction and update order status
     */
    public boolean processPayment(int orderId, double amount, String paymentMethod, String externalTransactionId) {
        int transactionId = transactionRepository.createPendingTransaction(
                orderId, amount, paymentMethod, externalTransactionId);

        if (transactionId > 0) {
            updateOrderPaymentStatus(orderId, paymentMethod, "PENDING");
            return true;
        }
        return false;
    }

    /**
     * Complete payment: update stock and order status
     * 
     * SOLID: SRP - All payment completion logic in Service layer
     */
    public void completePayment(Order order, String paymentMethod) {
        if (order == null || order.getOrderItems() == null) {
            return;
        }

        ProductRepository productRepo = new DatabaseProductRepository();

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product == null || product.getProductId() == null) {
                continue;
            }

            Long productId = product.getProductId();
            int quantitySold = item.getQuantity();

            productRepo.findById(productId).ifPresent(dbProduct -> {
                int newStock = Math.max(0, dbProduct.getStock() - quantitySold);
                productRepo.updateStock(productId, newStock);
            });
        }

        // Mark payment completed. OrderRepository.updatePaymentStatus will move order_status to PENDING for PM review.
        updateOrderPaymentStatus(order.getOrderId(), paymentMethod, "COMPLETED");
    }

    public void updateOrderPaymentStatus(int orderId, String paymentMethod, String status) {
        try {
            orderRepository.updatePaymentStatus(orderId, paymentMethod, status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
