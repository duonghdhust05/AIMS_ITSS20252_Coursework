package com.aimsfx.repository;

import com.aimsfx.model.TransactionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionRepository
 * Maps to Phase 3 of Test Plan
 */
class TransactionRepositoryTest {

    private TransactionRepository repository;
    private Connection connection;
    private org.mockito.MockedStatic<com.aimsfx.utils.DatabaseConnection> mockedDbConnection;

    @BeforeEach
    void setUp() throws Exception {
        // Setup an in-memory SQLite database for testing
        Connection realConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        connection = org.mockito.Mockito.spy(realConnection);
        org.mockito.Mockito.doNothing().when(connection).close();
        
        // Setup table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE transactions (" +
                    "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "amount REAL, " +
                    "payment_method TEXT, " +
                    "currency TEXT, " +
                    "external_transaction_id TEXT, " +
                    "status TEXT, " +
                    "bank_account TEXT, " +
                    "trans_type TEXT, " +
                    "content TEXT, " +
                    "transaction_time TIMESTAMP, " +
                    "reference_number TEXT, " +
                    "failure_reason TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "completed_at TIMESTAMP)");
        }
        
        com.aimsfx.utils.DatabaseConnection mockDb = org.mockito.Mockito.mock(com.aimsfx.utils.DatabaseConnection.class);
        org.mockito.Mockito.when(mockDb.getConnection()).thenReturn(connection);
        
        mockedDbConnection = org.mockito.Mockito.mockStatic(com.aimsfx.utils.DatabaseConnection.class);
        mockedDbConnection.when(com.aimsfx.utils.DatabaseConnection::getInstance).thenReturn(mockDb);
        
        repository = TransactionRepository.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockedDbConnection != null) {
            mockedDbConnection.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("[IT-REP-01] Update Txn Status")
    void testUpdateStatus_Success() {
        // Arrange: Insert a pending transaction
        int txnId = repository.createPendingTransaction(101, 50000.0, "PAYPAL", "EXT-123");
        assertTrue(txnId > 0, "Transaction should be created successfully");

        // Act
        repository.updateStatus(txnId, "COMPLETED");

        // Assert
        List<TransactionInfo> pendingTxns = repository.findPendingTransactions();
        // The transaction should no longer be pending
        assertTrue(pendingTxns.isEmpty(), "There should be no pending transactions after update");
    }

    @Test
    @DisplayName("[IT_PAY_007] DB Save Transaction")
    void testCreatePendingTransaction() {
        // Act
        int txnId = repository.createPendingTransaction(102, 100000.0, "PAYPAL", "PAYPAL-456");

        // Assert
        assertTrue(txnId > 0);
        List<TransactionInfo> pendingTxns = repository.findPendingTransactions();
        assertEquals(1, pendingTxns.size());
        assertEquals(String.valueOf(txnId), pendingTxns.get(0).getTransactionId());
        assertEquals("PENDING", pendingTxns.get(0).getStatusString());
    }
}
