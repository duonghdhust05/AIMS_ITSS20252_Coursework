package com.aimsfx.service;

import com.aimsfx.factory.PaymentControllerFactory;
import com.aimsfx.model.Order;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.repository.TransactionRepository;
import com.aimsfx.service.payment.IPaymentGateway;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RefundService
 * Handles automated refunds for applicable payment methods (e.g. PayPal)
 * when orders are rejected or cancelled.
 */
public class RefundService {

    private static final Logger LOGGER = Logger.getLogger(RefundService.class.getName());

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    public RefundService() {
        this.transactionRepository = TransactionRepository.getInstance();
        this.orderRepository = new OrderRepository();
    }

    /**
     * Attempts to automatically refund an order if it was paid via a supported
     * gateway.
     * 
     * @param orderId The order ID to process refund for
     * @return true if refund was successful or not needed, false if refund failed
     */
    public boolean processRefundIfPaid(int orderId) {
        try {
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                LOGGER.warning("Order not found: " + orderId);
                return false;
            }

            // In AIMS, order status might be REJECTED/REFUND_REQUEST but payment_status is
            // 'Completed'
            // We just need to check if payment_status is Completed and method is PayPal
            String paymentStatus = getOrderPaymentStatus(orderId);
            String paymentMethod = getOrderPaymentMethod(orderId);

            if ("PAYPAL".equalsIgnoreCase(paymentMethod) && "COMPLETED".equalsIgnoreCase(paymentStatus)) {
                LOGGER.info("Initiating automated refund for PayPal order: " + orderId);
                return executePayPalRefund(orderId);
            } else if ("VIETQR".equalsIgnoreCase(paymentMethod) && "COMPLETED".equalsIgnoreCase(paymentStatus)) {
                LOGGER.info(
                        "Manual refund required for VietQR order: " + orderId + ". Please process via banking app.");
                // Simply log manual task for VietQR as per user request
                return true;
            } else {
                LOGGER.info("No refund necessary for order: " + orderId + " (Status: " + paymentStatus + ", Method: "
                        + paymentMethod + ")");
                return true;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing refund for order " + orderId, e);
            return false;
        }
    }

    private boolean executePayPalRefund(int orderId) {
        IPaymentGateway payPalGateway = PaymentControllerFactory.getPayPalGateway();
        if (payPalGateway == null) {
            LOGGER.severe("PayPal gateway is not initialized!");
            return false;
        }

        String externalTransactionId = transactionRepository.getExternalTransactionIdByOrderId(orderId);
        if (externalTransactionId == null || externalTransactionId.isEmpty()) {
            LOGGER.warning("No external transaction ID found for order: " + orderId);
            return false;
        }

        try {
            boolean success = payPalGateway.refundOrder(externalTransactionId);
            if (success) {
                LOGGER.info("Successfully refunded PayPal transaction: " + externalTransactionId);
                // Mark transaction as refunded
                int transactionId = transactionRepository.findByOrderId(orderId);
                if (transactionId > 0) {
                    transactionRepository.updateStatus(transactionId, "REFUNDED");
                }
                // Update Order status to REFUNDED
                orderRepository.updateOrderStatus(orderId, "REFUNDED", "Automated PayPal Refund Successful");
                return true;
            } else {
                LOGGER.warning("Refund failed for PayPal transaction: " + externalTransactionId);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during PayPal refund for order " + orderId, e);
            return false;
        }
    }

    protected String getOrderPaymentStatus(int orderId) {
        String sql = "SELECT payment_status FROM orders WHERE order_id = ?";
        try (java.sql.Connection conn = com.aimsfx.utils.DatabaseConnection.getInstance().getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("payment_status");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get payment_status for order " + orderId + ": " + e.getMessage());
        }
        return null;
    }

    protected String getOrderPaymentMethod(int orderId) {
        String sql = "SELECT payment_method FROM orders WHERE order_id = ?";
        try (java.sql.Connection conn = com.aimsfx.utils.DatabaseConnection.getInstance().getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("payment_method");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get payment_method for order " + orderId + ": " + e.getMessage());
        }
        return null;
    }
}
