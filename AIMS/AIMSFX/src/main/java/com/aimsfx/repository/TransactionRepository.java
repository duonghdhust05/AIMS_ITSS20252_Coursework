package com.aimsfx.repository;

import com.aimsfx.utils.DatabaseConnection;

import java.sql.*;

/**
 * TransactionRepository Class
 * Purpose: Repository for payment transactions (VietQR + PayPal)
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward one goal: transaction data persistence
 * - Single responsibility: CRUD operations for transactions table
 * 
 * COUPLING ANALYSIS:
 * - Data Coupling with DatabaseConnection: uses getConnection()
 * - Data Coupling with Transaction model: only primitive data passed
 */
public class TransactionRepository {

    private static TransactionRepository instance;

    private TransactionRepository() {
        // Private constructor for singleton
    }

    public static TransactionRepository getInstance() {
        if (instance == null) {
            synchronized (TransactionRepository.class) {
                if (instance == null) {
                    instance = new TransactionRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Create a new pending transaction with external transaction ID
     * Used when we have the payment provider's transaction ID (VietQR/PayPal)
     * 
     * @param orderId               The numeric order ID from database
     * @param amount                Amount in VND
     * @param paymentMethod         'VIETQR' or 'PAYPAL'
     * @param externalTransactionId Transaction ID from payment provider
     * @return The generated transaction_id, or -1 if failed
     */
    public int createPendingTransaction(int orderId, double amount, String paymentMethod,
            String externalTransactionId) {
        String sql = """
                INSERT INTO transactions (order_id, amount, payment_method, status, currency, external_transaction_id, created_at)
                VALUES (?, ?, ?, 'PENDING', 'VND', ?, CURRENT_TIMESTAMP)
                RETURNING transaction_id
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setDouble(2, amount);
            stmt.setString(3, paymentMethod.toUpperCase());
            stmt.setString(4, externalTransactionId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                return transactionId;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to create pending transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Create a COMPLETED transaction from VietQR callback with all required fields.
     * This stores all the data from the VietQR Transaction Sync API.
     * 
     * @param orderId         The numeric order ID from database
     * @param amount          Amount in VND
     * @param transactionId   VietQR's transaction ID (external_transaction_id)
     * @param bankAccount     Bank account used for payment
     * @param transType       D (debit) or C (credit)
     * @param content         Transfer message content
     * @param transactionTime When payment was made (timestamp in ms)
     * @param referenceNumber Bank reference code
     * @return The generated internal transaction_id, or -1 if failed
     */
    public int createVietQRTransaction(int orderId, double amount, String transactionId,
            String bankAccount, String transType, String content,
            Long transactionTime, String referenceNumber) {
        String sql = """
                INSERT INTO transactions (
                    order_id, amount, payment_method, status, currency,
                    external_transaction_id, bank_account, trans_type, content,
                    transaction_time, reference_number, created_at, completed_at
                )
                VALUES (?, ?, 'VIETQR', 'COMPLETED', 'VND', ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING transaction_id
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setDouble(2, amount);
            stmt.setString(3, transactionId);
            stmt.setString(4, bankAccount);
            stmt.setString(5, transType);
            stmt.setString(6, content);

            // Convert timestamp (ms) to SQL Timestamp
            if (transactionTime != null) {
                stmt.setTimestamp(7, new Timestamp(transactionTime));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setString(8, referenceNumber);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int internalId = rs.getInt("transaction_id");

                // Also update the order's payment_status
                updateOrderPaymentStatus(orderId, internalId, "Completed");

                return internalId;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to create VietQR transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Create a COMPLETED transaction directly (for PayPal or simple cases)
     * This saves space by not storing cancelled/failed attempts.
     * 
     * @param orderId               The numeric order ID from database
     * @param amount                Amount in VND
     * @param paymentMethod         'VIETQR' or 'PAYPAL'
     * @param externalTransactionId PayPal Order ID or VietQR transaction ref (can
     *                              be null)
     * @return The generated transaction_id, or -1 if failed
     */
    public int createCompletedTransaction(int orderId, double amount, String paymentMethod,
            String externalTransactionId) {
        String sql = """
                INSERT INTO transactions (order_id, amount, payment_method, status, currency, external_transaction_id, created_at, completed_at)
                VALUES (?, ?, ?, 'COMPLETED', 'VND', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING transaction_id
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setDouble(2, amount);
            stmt.setString(3, paymentMethod.toUpperCase());
            stmt.setString(4, externalTransactionId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int transactionId = rs.getInt("transaction_id");

                // Also update the order's payment_status
                updateOrderPaymentStatus(orderId, transactionId, "Completed");

                return transactionId;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to create completed transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Update transaction with external ID (PayPal Order ID or VietQR ref)
     * 
     * @param transactionId         Internal transaction ID
     * @param externalTransactionId PayPal Order ID or VietQR transaction reference
     * @return true if successful
     */
    public boolean setExternalTransactionId(int transactionId, String externalTransactionId) {
        String sql = "UPDATE transactions SET external_transaction_id = ? WHERE transaction_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, externalTransactionId);
            stmt.setInt(2, transactionId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to set external transaction ID: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mark transaction as completed
     * 
     * @param transactionId Internal transaction ID
     * @return true if successful
     */
    public boolean markCompleted(int transactionId) {
        String sql = """
                UPDATE transactions
                SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP
                WHERE transaction_id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                // Also update the order's payment_status
                updateOrderPaymentStatus(transactionId, "Completed");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to mark transaction completed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mark transaction as completed using external transaction ID
     * (Used by VietQR webhook when we don't have internal ID)
     * 
     * @param externalTransactionId PayPal Order ID or VietQR ref
     * @return true if successful
     */
    public boolean markCompletedByExternalId(String externalTransactionId) {
        String sql = """
                UPDATE transactions
                SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP
                WHERE external_transaction_id = ?
                RETURNING transaction_id
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, externalTransactionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                updateOrderPaymentStatus(transactionId, "Completed");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to mark transaction completed by external ID: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mark transaction as failed
     * 
     * @param transactionId Internal transaction ID
     * @param reason        Failure reason
     * @return true if successful
     */
    public boolean markFailed(int transactionId, String reason) {
        String sql = """
                UPDATE transactions
                SET status = 'FAILED', failure_reason = ?, completed_at = CURRENT_TIMESTAMP
                WHERE transaction_id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reason);
            stmt.setInt(2, transactionId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                updateOrderPaymentStatus(transactionId, "Failed");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to mark transaction failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mark transaction as cancelled
     * 
     * @param transactionId Internal transaction ID
     * @return true if successful
     */
    public boolean markCancelled(int transactionId) {
        String sql = """
                UPDATE transactions
                SET status = 'CANCELLED', completed_at = CURRENT_TIMESTAMP
                WHERE transaction_id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                updateOrderPaymentStatus(transactionId, "Cancelled");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to mark transaction cancelled: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update the order's payment_status and link to transaction
     * This version looks up the orderId from the transactions table
     */
    private void updateOrderPaymentStatus(int transactionId, String status) {
        // First, get the order_id from the transaction
        String lookupSql = "SELECT order_id FROM transactions WHERE transaction_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement lookupStmt = conn.prepareStatement(lookupSql)) {

            lookupStmt.setInt(1, transactionId);
            ResultSet rs = lookupStmt.executeQuery();

            if (rs.next()) {
                int orderId = rs.getInt("order_id");
                updateOrderPaymentStatus(orderId, transactionId, status);
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to lookup order_id for transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the order's payment_status and link to transaction
     */
    private void updateOrderPaymentStatus(int orderId, int transactionId, String status) {
        String sql = """
                UPDATE orders
                SET payment_status = ?, transaction_id = ?
                WHERE order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, transactionId);
            stmt.setInt(3, orderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to update order payment status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find transaction by order ID
     * 
     * @param orderId The order ID
     * @return Transaction ID or -1 if not found
     */
    public int findByOrderId(int orderId) {
        String sql = "SELECT transaction_id FROM transactions WHERE order_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("transaction_id");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to find transaction by order ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get transaction status
     * 
     * @param transactionId Transaction ID
     * @return Status string or null if not found
     */
    public String getStatus(int transactionId) {
        String sql = "SELECT status FROM transactions WHERE transaction_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get transaction status: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
