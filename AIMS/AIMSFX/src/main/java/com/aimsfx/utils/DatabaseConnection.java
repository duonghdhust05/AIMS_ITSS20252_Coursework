package com.aimsfx.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Singleton class để quản lý connection pool database PostgreSQL sử dụng
 * HikariCP
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private HikariDataSource dataSource;
    private String url;
    private String username;
    private String password;
    private int maxConnections;
    private int minConnections;
    private long connectionTimeout;

    // Private constructor to implement Singleton pattern
    private DatabaseConnection() {
        loadDatabaseConfig();
        initializeConnectionPool();
    }

    /**
     * Load database config from file properties
     */
    private void loadDatabaseConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                System.out.println("application.properties file not found");
                return;
            }

            props.load(input);
            this.url = props.getProperty("spring.datasource.url");
            this.username = props.getProperty("spring.datasource.username");
            this.password = props.getProperty("spring.datasource.password");

            // Load connection pool settings
            this.maxConnections = Integer.parseInt(props.getProperty("db.max.connections", "20"));
            this.minConnections = Integer.parseInt(props.getProperty("db.min.connections", "5"));
            this.connectionTimeout = Long.parseLong(props.getProperty("db.connection.timeout", "30000"));

            if (this.url == null || this.username == null || this.password == null) {
                System.out.println("Database credentials not found in application.properties, using defaults");
            }

        } catch (IOException ex) {
            System.err.println("Error reading database configuration file: " + ex.getMessage());

        } catch (NumberFormatException ex) {
            System.err.println("Number format error in configuration: " + ex.getMessage());

        }
    }

    /**
     * Default configuration if properties file is missing or has errors
     * 
     * private void setDefaultConfig() {
     * this.url =
     * "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:6543/postgres?user=postgres.pmwbyfvyxtstuindqkkj&password=Aims2025!@&sslmode=require";
     * this.username = "postgres.pmwbyfvyxtstuindqkkj";
     * this.password = "Aims2025!@";
     * this.maxConnections = 20;
     * this.minConnections = 5;
     * this.connectionTimeout = 30000;
     * }
     */

    /**
     * Khởi tạo HikariCP connection pool
     */
    private void initializeConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();

            // Cấu hình database
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            config.addDataSourceProperty("prepareThreshold", "0");
            config.addDataSourceProperty("preparedStatementCacheQueries", "0");

            // Cấu hình connection pool
            config.setMaximumPoolSize(maxConnections);
            config.setMinimumIdle(minConnections);
            config.setConnectionTimeout(connectionTimeout);
            config.setIdleTimeout(600000); // 10 phút
            config.setMaxLifetime(1800000); // 30 phút
            config.setLeakDetectionThreshold(60000); // 1 phút

            // Cấu hình connection validation
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);

            // Pool name
            config.setPoolName("AIMS-Supabase-Pool");

            this.dataSource = new HikariDataSource(config);
            System.out.println("SUCCESS: Connection pool initialized successfully!");
            System.out.println("Pool size: " + minConnections + " - " + maxConnections + " connections");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize connection pool: " + e.getMessage());
            throw new RuntimeException("Cannot initialize connection pool", e);
        }
    }

    /**
     * Lấy instance của DatabaseConnection (Singleton)
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Lấy kết nối từ connection pool
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Test kết nối database
     */
    public boolean testConnection() {
        try (Connection testConn = getConnection()) {
            return testConn != null && !testConn.isClosed() && testConn.isValid(5);
        } catch (SQLException e) {
            System.err.println("ERROR: Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đóng connection pool (chỉ nên gọi khi shutdown ứng dụng)
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("SUCCESS: Connection pool closed");
        }
    }

    /**
     * Lấy thông tin về connection pool
     */
    public String getPoolInfo() {
        if (dataSource == null) {
            return "Connection pool not initialized";
        }

        return "Pool Name: " + dataSource.getPoolName() + "\n" +
                "Active Connections: " + dataSource.getHikariPoolMXBean().getActiveConnections() + "\n" +
                "Idle Connections: " + dataSource.getHikariPoolMXBean().getIdleConnections() + "\n" +
                "Total Connections: " + dataSource.getHikariPoolMXBean().getTotalConnections() + "\n" +
                "Max Pool Size: " + dataSource.getMaximumPoolSize() + "\n" +
                "Min Pool Size: " + dataSource.getMinimumIdle();
    }

    /**
     * Lấy thông tin cấu hình database
     */
    public String getDatabaseInfo() {
        return "Database URL: " + url + "\n" +
                "Username: " + username + "\n" +
                "Connection Pool Status: " + (dataSource != null && !dataSource.isClosed() ? "Active" : "Inactive")
                + "\n" +
                "Test Connection: " + (testConnection() ? "OK" : "Failed");
    }
}