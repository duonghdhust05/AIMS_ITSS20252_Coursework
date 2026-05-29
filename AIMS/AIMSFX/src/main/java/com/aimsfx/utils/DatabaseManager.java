package com.aimsfx.utils;

/**
 * Utility class để quản lý lifecycle của database connection pool
 */
public class DatabaseManager {
    
    private static boolean shutdownHookRegistered = false;
    
    /**
     * Đăng ký shutdown hook để đóng connection pool khi ứng dụng tắt
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
     * Khởi tạo database connection pool và đăng ký shutdown hook
     */
    public static void initialize() {
        // Khởi tạo connection pool thông qua getInstance()
        DatabaseConnection.getInstance();
        registerShutdownHook();
        System.out.println("SUCCESS: Database Manager initialized successfully");
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