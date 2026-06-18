package com.aimsfx.model;

import java.util.Locale;

/**
 * OrderStatus - canonical order workflow statuses for DB + business logic.
 *
 * Notes:
 * - The current codebase stores order status as a String in the orders table.
 * - This enum provides a safer mapping layer without forcing a DB migration.
 */
public enum OrderStatus {
    PROCESSING("PROCESSING"),
    PENDING_REVIEW("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED"),
    REFUND_REQUEST("REFUND_REQUEST"),
    REFUNDED("REFUNDED");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String toDbValue() {
        return dbValue;
    }

    public static OrderStatus fromDbValue(String value) {
        if (value == null) {
            return PROCESSING;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (OrderStatus status : values()) {
            if (status.dbValue.equals(normalized)) {
                return status;
            }
        }
        // Preserve forward compatibility if DB contains a newer string.
        return PROCESSING;
    }
}