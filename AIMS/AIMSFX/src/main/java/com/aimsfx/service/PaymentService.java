package com.aimsfx.service;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderItem;
import com.aimsfx.model.Product;
import com.aimsfx.repository.DatabaseProductRepository;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.repository.ProductRepository;
import com.aimsfx.repository.TransactionRepository;

// Spring imports for @Transactional
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final ProductRepository productRepo;

    // Executor for background cron job
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public PaymentService() {
        this.transactionRepository = TransactionRepository.getInstance();
        this.orderRepository = new OrderRepository();
        this.productRepo = new DatabaseProductRepository();
        startPendingTransactionCronJob();
    }

    public PaymentService(TransactionRepository transactionRepository, OrderRepository orderRepository,
            ProductRepository productRepo) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
        this.productRepo = productRepo;
        startPendingTransactionCronJob();
    }

    /**
     * Start a background Cron Job to check PENDING transactions.
     * 
     * HOW IT WORKS (Cron Job):
     * - We use a ScheduledExecutorService to run a task every 5 minutes.
     * - The task finds all transactions in the database with status = 'PENDING'.
     * - For each PENDING transaction, it could call the PayPal API to check the
     * real status.
     * - If PayPal says "FAILED" or "NOT FOUND", it triggers the Compensating
     * Transaction
     * (restoring the stock) and marks the transaction as 'FAILED'.
     * - This ensures Data Consistency even if the initial PayPal call timed out.
     * 
     * Documentation: Search for "Saga Pattern", "Outbox Pattern", and
     * "ScheduledExecutorService in Java".
     */
    private void startPendingTransactionCronJob() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // In a real scenario, fetch PENDING transactions from DB here:
                // List<Transaction> pendingTxns =
                // transactionRepository.findPendingTransactions();
                // for (Transaction txn : pendingTxns) {
                // String status = paypalApi.checkStatus(txn.getExternalId());
                // if ("FAILED".equals(status)) {
                // productRepo.restoreStock(txn.getProductId(), txn.getQuantity());
                // transactionRepository.updateStatus(txn.getId(), "FAILED");
                // } else if ("COMPLETED".equals(status)) {
                // transactionRepository.updateStatus(txn.getId(), "COMPLETED");
                // }
                // }
                System.out.println("Cron Job: Checking pending transactions...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.MINUTES);
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
     * Complete payment: update stock and order status using Saga Pattern.
     * 
     * SOLID: SRP - All payment completion logic in Service layer
     */
    public void completePayment(Order order, String paymentMethod) {
        if (order == null || order.getOrderItems() == null) {
            return;
        }

        // STEP 1: Deduct Stock FIRST (Transactional Update to prevent Race Condition and Partial Deduction)
        boolean success = productRepo.deductStockForOrder(order.getOrderItems());
        if (!success) {
            throw new RuntimeException("Out of stock for one or more products in the order. Transaction rolled back.");
        }

        // STEP 2: Call External API (Simulated)
        try {
            // Simulated PayPal API call
            // paypalApi.charge(order.getTotalAmount());
            System.out.println("Calling PayPal API...");

            // If the API call fails due to a network timeout, an exception is thrown.
            // if (networkError) throw new NetworkException("Timeout");

        } catch (Exception e) { // Catch NetworkException
            // STEP 3: Handle Network Failure (Saga Pattern)
            // Because we don't know if PayPal actually charged the customer or not
            // (Timeout),
            // we DO NOT rollback the transaction immediately. Instead, we keep the
            // transaction
            // in PENDING state and let the background Cron Job verify it later.
            System.err.println("Network error calling PayPal API. Keeping order in PENDING state.");
            throw e;
        }

        // STEP 4: If API call was successful, mark payment as COMPLETED
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
