package com.aimsfx.utils;

import java.sql.Connection;
import java.sql.Statement;

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
        return "=== DATABASE STATUS ===\n" +
                db.getDatabaseInfo() + "\n\n" +
                "=== CONNECTION POOL STATUS ===\n" +
                db.getPoolInfo();
    }
}