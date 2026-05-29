package com.aimsfx.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DatabaseConnection
 * Testing database connection pool functionality using JUnit 5:
 * 1. testConnectionPoolInitialization() - Test connection pool setup
 * 2. testGetConnection() - Test retrieving connections from pool
 * 3. testMultipleConnections() - Test multiple concurrent connections
 * 4. testConnectionMetadata() - Test database metadata access
 * 5. testPoolInfo() - Test connection pool statistics
 */
class DatabaseTest {
    
    private DatabaseConnection dbConnection;
    
    @BeforeEach
    void setUp() {
        // Set timezone and get database connection instance
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        dbConnection = DatabaseConnection.getInstance();
    }
    
    @AfterEach
    void tearDown() {
        // Note: Do not shutdown pool in unit tests as it's a singleton
        // The pool should remain active for other tests and application use
    }
    
    @Test
    void testConnectionPoolInitialization() {
        // Act & Assert: Connection pool should be initialized
        assertNotNull(dbConnection, "DatabaseConnection instance should not be null");
        assertTrue(dbConnection.testConnection(), "Connection test should pass");
        
        String databaseInfo = dbConnection.getDatabaseInfo();
        assertNotNull(databaseInfo, "Database info should not be null");
        assertTrue(databaseInfo.contains("Database URL"), "Database info should contain URL");
        assertTrue(databaseInfo.contains("Username"), "Database info should contain username");
    }
    
    @Test
    void testGetConnection() throws SQLException {
        // Act: Get connection from pool
        Connection connection = dbConnection.getConnection();
        
        // Assert: Connection should be valid
        assertNotNull(connection, "Connection should not be null");
        assertFalse(connection.isClosed(), "Connection should not be closed");
        assertTrue(connection.isValid(5), "Connection should be valid within 5 seconds");
        
        // Clean up: Return connection to pool
        connection.close();
    }
    
    @Test
    void testMultipleConnections() throws SQLException {
        // Act: Get multiple connections from pool
        Connection conn1 = dbConnection.getConnection();
        Connection conn2 = dbConnection.getConnection();
        Connection conn3 = dbConnection.getConnection();
        
        // Assert: All connections should be valid and different instances
        assertNotNull(conn1, "First connection should not be null");
        assertNotNull(conn2, "Second connection should not be null");
        assertNotNull(conn3, "Third connection should not be null");
        
        assertNotEquals(conn1, conn2, "Connections should be different instances");
        assertNotEquals(conn2, conn3, "Connections should be different instances");
        assertNotEquals(conn1, conn3, "Connections should be different instances");
        
        assertTrue(conn1.isValid(5), "First connection should be valid");
        assertTrue(conn2.isValid(5), "Second connection should be valid");
        assertTrue(conn3.isValid(5), "Third connection should be valid");
        
        // Clean up: Return all connections to pool
        conn1.close();
        conn2.close();
        conn3.close();
    }
    
    @Test
    void testConnectionMetadata() throws SQLException {
        // Act: Get connection and retrieve metadata
        try (Connection connection = dbConnection.getConnection()) {
            // Assert: Should be able to access database metadata
            assertNotNull(connection.getMetaData(), "Metadata should not be null");
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName(), 
                        "Database should be PostgreSQL");
            assertNotNull(connection.getMetaData().getDatabaseProductVersion(), 
                         "Database version should not be null");
            assertNotNull(connection.getMetaData().getDriverName(), 
                         "Driver name should not be null");
            assertNotNull(connection.getMetaData().getDriverVersion(), 
                         "Driver version should not be null");
        }
    }
    
    @Test
    void testPoolInfo() {
        // Act: Get pool information
        String poolInfo = dbConnection.getPoolInfo();
        
        // Assert: Pool info should contain expected information
        assertNotNull(poolInfo, "Pool info should not be null");
        assertTrue(poolInfo.contains("Pool Name"), "Pool info should contain pool name");
        assertTrue(poolInfo.contains("Active Connections"), "Pool info should contain active connections");
        assertTrue(poolInfo.contains("Idle Connections"), "Pool info should contain idle connections");
        assertTrue(poolInfo.contains("Total Connections"), "Pool info should contain total connections");
        assertTrue(poolInfo.contains("Max Pool Size"), "Pool info should contain max pool size");
        assertTrue(poolInfo.contains("Min Pool Size"), "Pool info should contain min pool size");
    }
    
    @Test
    void testDatabaseInfo() {
        // Act: Get database information
        String databaseInfo = dbConnection.getDatabaseInfo();
        
        // Assert: Database info should contain connection details
        assertNotNull(databaseInfo, "Database info should not be null");
        assertTrue(databaseInfo.contains("jdbc:postgresql"), "Should contain PostgreSQL URL");
        assertTrue(databaseInfo.contains("postgres"), "Should contain correct username");
        assertTrue(databaseInfo.contains("Connection Pool Status"), "Should contain pool status");
        assertTrue(databaseInfo.contains("Test Connection"), "Should contain connection test result");
    }
    
    @Test
    void testConnectionPoolSingleton() {
        // Act: Get multiple instances
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();
        
        // Assert: Should return same instance (Singleton pattern)
        assertSame(instance1, instance2, "Should return same singleton instance");
        assertSame(dbConnection, instance1, "Should be same as setup instance");
    }
    
    /**
     * Integration test method for comprehensive workflow testing
     * This test verifies the complete connection lifecycle
     */
    @Test
    void testCompleteConnectionWorkflow() throws SQLException {
        // Arrange: Verify initial state
        assertNotNull(dbConnection, "Database connection should be initialized");
        assertTrue(dbConnection.testConnection(), "Initial connection test should pass");
        
        // Act: Test complete workflow with multiple connections
        try (Connection conn1 = dbConnection.getConnection();
             Connection conn2 = dbConnection.getConnection();
             Connection conn3 = dbConnection.getConnection()) {
            
            // Assert: All connections should be valid
            assertNotNull(conn1, "First connection should not be null");
            assertNotNull(conn2, "Second connection should not be null");
            assertNotNull(conn3, "Third connection should not be null");
            
            // Verify metadata access
            assertNotNull(conn1.getMetaData().getDatabaseProductName(), 
                         "Should be able to access database product name");
            assertNotNull(conn1.getMetaData().getDatabaseProductVersion(), 
                         "Should be able to access database version");
            
            // Verify pool information is accessible
            String poolInfo = dbConnection.getPoolInfo();
            assertNotNull(poolInfo, "Pool info should be accessible");
            assertTrue(poolInfo.contains("Active Connections"), 
                      "Pool info should show active connections");
            
        } catch (SQLException e) {
            fail("Connection workflow should not throw SQLException: " + e.getMessage());
        }
        
        // Assert: Connection pool should still be functional after test
        assertTrue(dbConnection.testConnection(), "Connection pool should remain functional");
    }
}