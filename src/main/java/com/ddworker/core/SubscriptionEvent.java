package com.ddworker.core;

import java.util.Map;

/**
 * Represents a subscription event that will be processed by Azure Functions
 * Each event corresponds to a single subscription and prevents timeout issues
 */
public class SubscriptionEvent {
    private final String objectId;
    private final String objectType;
    private final Map<String, Object> metadata;
    private final String subscriptionId;
    private final ProcessingConfig processingConfig;
    private final long timestamp;
    
    public SubscriptionEvent(String objectId, String objectType, Map<String, Object> metadata, 
                           String subscriptionId, ProcessingConfig processingConfig) {
        this.objectId = objectId;
        this.objectType = objectType;
        this.metadata = metadata;
        this.subscriptionId = subscriptionId;
        this.processingConfig = processingConfig;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getObjectId() {
        return objectId;
    }
    
    public String getObjectType() {
        return objectType;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public String getSubscriptionId() {
        return subscriptionId;
    }
    
    public ProcessingConfig getProcessingConfig() {
        return processingConfig;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "SubscriptionEvent{" +
                "objectId='" + objectId + '\'' +
                ", objectType='" + objectType + '\'' +
                ", subscriptionId='" + subscriptionId + '\'' +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                ", timestamp=" + timestamp +
                '}';
    }
}