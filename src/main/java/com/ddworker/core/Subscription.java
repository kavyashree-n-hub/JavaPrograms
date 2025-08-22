package com.ddworker.core;

/**
 * Represents a subscription to object publications
 */
public class Subscription {
    private final String subscriptionId;
    private final String objectType;
    private final boolean active;
    private final ProcessingConfig processingConfig;
    
    public Subscription(String subscriptionId, String objectType, boolean active, ProcessingConfig processingConfig) {
        this.subscriptionId = subscriptionId;
        this.objectType = objectType;
        this.active = active;
        this.processingConfig = processingConfig;
    }
    
    public String getSubscriptionId() {
        return subscriptionId;
    }
    
    public String getObjectType() {
        return objectType;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public ProcessingConfig getProcessingConfig() {
        return processingConfig;
    }
    
    @Override
    public String toString() {
        return "Subscription{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", objectType='" + objectType + '\'' +
                ", active=" + active +
                ", processingConfig=" + processingConfig +
                '}';
    }
}