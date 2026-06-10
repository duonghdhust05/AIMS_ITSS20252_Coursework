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
        initializeSchema();
        registerShutdownHook();
        System.out.println("SUCCESS: Database Manager initialized successfully");
    }
    
    private static void initializeSchema() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Checking and updating database schema...");
            try {
                stmt.execute("ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255)");
            } catch (Exception e) {
                System.out.println("Column cancel_reason might already exist or error: " + e.getMessage());
            }

            String createTableSQL = "CREATE TABLE IF NOT EXISTS order_action_logs (" +
                                    "id SERIAL PRIMARY KEY, " +
                                    "order_id INTEGER NOT NULL REFERENCES orders(order_id), " +
                                    "action VARCHAR(50) NOT NULL, " +
                                    "reason TEXT, " +
                                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                    ")";
            stmt.execute(createTableSQL);
            System.out.println("SUCCESS: Schema up to date.");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize database schema: " + e.getMessage());
        }
    }
    
    /**
     * Lấy thông tin trạng thái của database
     */
    public static String getStatus() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        return "=== DATABASE STATUS ===\n" + 
               db.getDatabaseInfo() + "\n\n" +
               "=== CONNECTION POOL STATUS ===\n" + 
               db.getPoolInfo();
    }
}