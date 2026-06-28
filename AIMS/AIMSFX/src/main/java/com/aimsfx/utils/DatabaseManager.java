package com.aimsfx.utils;

/**
 * Utility class to manage the lifecycle of the database connection pool
 */
public class DatabaseManager {

    private static boolean shutdownHookRegistered = false;

    /**
     * Register shutdown hook to close connection pool when application shuts down
     */
    public static void registerShutdownHook() {
        if (!shutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Closing database connection pool...");
                DatabaseConnection.getInstance().shutdown();
            }));
            shutdownHookRegistered = true;
            System.out.println("SUCCESS: Registered shutdown hook for database");
        }
    }

    /**
     * Initialize database connection pool and register shutdown hook
     */
    public static void initialize() {
        // Khởi tạo connection pool thông qua getInstance()
        DatabaseConnection.getInstance();
        registerShutdownHook();
        System.out.println("SUCCESS: Database Manager initialized successfully");
    }

    /**
     * Get database status
     */
    public static String getStatus() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        com.aimsfx.dto.DatabaseStatusDTO dbStatus = db.getDatabaseStatusDTO();
        com.aimsfx.dto.ConnectionPoolMetricsDTO metrics = db.getConnectionPoolMetricsDTO();
        
        return "=== DATABASE STATUS ===\n" +
                "Pool Name: " + dbStatus.getPoolName() + "\n" +
                "Status: " + dbStatus.getStatus() + "\n" +
                "Connection OK: " + dbStatus.isConnectionOk() + "\n\n" +
                "=== CONNECTION POOL STATUS ===\n" +
                "Active Connections: " + metrics.getActiveConnections() + "\n" +
                "Idle Connections: " + metrics.getIdleConnections() + "\n" +
                "Total Connections: " + metrics.getTotalConnections() + "\n" +
                "Max Pool Size: " + metrics.getMaxPoolSize();
    }
}