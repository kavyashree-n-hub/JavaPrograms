package com.ddworker.core;

/**
 * Configuration for processing subscription events
 * Helps prevent timeouts and handle large metadata volumes
 */
public class ProcessingConfig {
    private final int maxRetries;
    private final long timeoutMillis;
    private final int batchSize;
    private final boolean enableChunking;
    private final int maxMetadataSize;
    
    public ProcessingConfig(int maxRetries, long timeoutMillis, int batchSize, 
                          boolean enableChunking, int maxMetadataSize) {
        this.maxRetries = maxRetries;
        this.timeoutMillis = timeoutMillis;
        this.batchSize = batchSize;
        this.enableChunking = enableChunking;
        this.maxMetadataSize = maxMetadataSize;
    }
    
    // Default configuration optimized for Azure Functions
    public static ProcessingConfig getDefault() {
        return new ProcessingConfig(
            3,           // maxRetries
            300000,      // timeoutMillis (5 minutes - Azure Function default)
            100,         // batchSize for chunked processing
            true,        // enableChunking
            1048576      // maxMetadataSize (1MB)
        );
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public long getTimeoutMillis() {
        return timeoutMillis;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public boolean isEnableChunking() {
        return enableChunking;
    }
    
    public int getMaxMetadataSize() {
        return maxMetadataSize;
    }
    
    @Override
    public String toString() {
        return "ProcessingConfig{" +
                "maxRetries=" + maxRetries +
                ", timeoutMillis=" + timeoutMillis +
                ", batchSize=" + batchSize +
                ", enableChunking=" + enableChunking +
                ", maxMetadataSize=" + maxMetadataSize +
                '}';
    }
}