package com.aimsfx.dto;

/**
 * DatabaseStatusDTO - Data Transfer Object for basic database connection status
 * Purpose: Provides a safe representation of connection health without exposing credentials or URL.
 */
public class DatabaseStatusDTO {
    private String poolName;
    private String status;
    private boolean isConnectionOk;

    public DatabaseStatusDTO() {
    }

    public DatabaseStatusDTO(String poolName, String status, boolean isConnectionOk) {
        this.poolName = poolName;
        this.status = status;
        this.isConnectionOk = isConnectionOk;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isConnectionOk() {
        return isConnectionOk;
    }

    public void setConnectionOk(boolean connectionOk) {
        isConnectionOk = connectionOk;
    }
}
