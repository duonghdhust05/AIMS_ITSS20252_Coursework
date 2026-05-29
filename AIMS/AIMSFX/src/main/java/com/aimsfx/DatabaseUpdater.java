package com.aimsfx;

import com.aimsfx.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseUpdater {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Adding cancel_reason to orders table...");
            try {
                stmt.execute("ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255)");
                System.out.println("Success.");
            } catch (Exception e) {
                System.out.println("Column might already exist or error: " + e.getMessage());
            }

            System.out.println("Creating order_action_logs table...");
            String createTableSQL = "CREATE TABLE IF NOT EXISTS order_action_logs (" +
                                    "id SERIAL PRIMARY KEY, " +
                                    "order_id INTEGER NOT NULL REFERENCES orders(order_id), " +
                                    "action VARCHAR(50) NOT NULL, " +
                                    "reason TEXT, " +
                                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                    ")";
            stmt.execute(createTableSQL);
            System.out.println("Table created successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
