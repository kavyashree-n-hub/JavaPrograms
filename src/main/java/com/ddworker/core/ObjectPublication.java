package com.ddworker.core;

import java.util.Map;

/**
 * Represents an object publication that needs to be processed by subscriptions
 */
public class ObjectPublication {
    private final String objectId;
    private final String objectType;
    private final Map<String, Object> metadata;
    private final long timestamp;
    
    public ObjectPublication(String objectId, String objectType, Map<String, Object> metadata) {
        this.objectId = objectId;
        this.objectType = objectType;
        this.metadata = metadata;
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
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "ObjectPublication{" +
                "objectId='" + objectId + '\'' +
                ", objectType='" + objectType + '\'' +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                ", timestamp=" + timestamp +
                '}';
    }
}