package com.aimsfx.dto;

/**
 * ConnectionPoolMetricsDTO - Data Transfer Object for detailed pool statistics
 * Purpose: Used for system monitoring dashboards, providing structured numeric data 
 * instead of raw concatenated strings.
 */
public class ConnectionPoolMetricsDTO {
    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private int maxPoolSize;
    private int minPoolSize;

    public ConnectionPoolMetricsDTO() {
    }

    public ConnectionPoolMetricsDTO(int activeConnections, int idleConnections, int totalConnections, int maxPoolSize, int minPoolSize) {
        this.activeConnections = activeConnections;
        this.idleConnections = idleConnections;
        this.totalConnections = totalConnections;
        this.maxPoolSize = maxPoolSize;
        this.minPoolSize = minPoolSize;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    public int getIdleConnections() {
        return idleConnections;
    }

    public void setIdleConnections(int idleConnections) {
        this.idleConnections = idleConnections;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(int totalConnections) {
        this.totalConnections = totalConnections;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }
}
