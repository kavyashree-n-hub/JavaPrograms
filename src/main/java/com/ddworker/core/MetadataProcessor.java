package com.ddworker.core;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Processor for handling large metadata volumes with chunking to prevent timeouts
 */
public class MetadataProcessor {
    private static final Logger logger = Logger.getLogger(MetadataProcessor.class.getName());
    private final ProcessingConfig config;
    
    public MetadataProcessor(ProcessingConfig config) {
        this.config = config;
    }
    
    /**
     * Processes metadata in chunks to handle large volumes and prevent timeouts
     */
    public void processMetadata(Map<String, Object> metadata, String objectId) {
        if (metadata == null || metadata.isEmpty()) {
            logger.info("No metadata to process for object: " + objectId);
            return;
        }
        
        logger.info("Processing metadata for object: " + objectId + " with " + metadata.size() + " entries");
        
        long startTime = System.currentTimeMillis();
        
        try {
            if (config.isEnableChunking() && metadata.size() > config.getBatchSize()) {
                processMetadataInChunks(metadata, objectId);
            } else {
                processMetadataBatch(metadata, objectId);
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            logger.info("Successfully processed metadata for object: " + objectId 
                + " in " + processingTime + "ms");
                
            // Check if processing time is approaching timeout limit
            if (processingTime > (config.getTimeoutMillis() * 0.8)) {
                logger.warning("Processing time approaching timeout limit for object: " + objectId
                    + " (" + processingTime + "ms out of " + config.getTimeoutMillis() + "ms)");
            }
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.severe("Error processing metadata for object: " + objectId 
                + " after " + processingTime + "ms: " + e.getMessage());
            throw new RuntimeException("Failed to process metadata", e);
        }
    }
    
    /**
     * Processes metadata in chunks to handle large volumes
     */
    private void processMetadataInChunks(Map<String, Object> metadata, String objectId) {
        logger.info("Processing metadata in chunks for object: " + objectId);
        
        int chunkNumber = 0;
        int batchSize = config.getBatchSize();
        Object[] entries = metadata.entrySet().toArray();
        
        for (int i = 0; i < entries.length; i += batchSize) {
            chunkNumber++;
            int endIndex = Math.min(i + batchSize, entries.length);
            
            logger.info("Processing chunk " + chunkNumber + " for object: " + objectId 
                + " (entries " + (i + 1) + "-" + endIndex + " of " + entries.length + ")");
            
            // Process each chunk
            for (int j = i; j < endIndex; j++) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) entries[j];
                processMetadataEntry(entry.getKey(), entry.getValue(), objectId);
            }
            
            // Check timeout between chunks
            if (isTimeoutApproaching()) {
                logger.warning("Timeout approaching during chunk processing for object: " + objectId);
                break;
            }
        }
        
        logger.info("Completed chunked processing for object: " + objectId + " (processed " + chunkNumber + " chunks)");
    }
    
    /**
     * Processes metadata as a single batch
     */
    private void processMetadataBatch(Map<String, Object> metadata, String objectId) {
        logger.info("Processing metadata as single batch for object: " + objectId);
        
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            processMetadataEntry(entry.getKey(), entry.getValue(), objectId);
            
            // Check timeout during batch processing
            if (isTimeoutApproaching()) {
                logger.warning("Timeout approaching during batch processing for object: " + objectId);
                break;
            }
        }
    }
    
    /**
     * Processes a single metadata entry
     */
    private void processMetadataEntry(String key, Object value, String objectId) {
        // Simulate metadata processing - in real implementation this would:
        // - Validate metadata format
        // - Transform data as needed
        // - Store in appropriate data stores
        // - Index for search
        // - Apply business rules
        
        try {
            // Simulate processing time based on metadata size
            if (value != null) {
                String valueStr = value.toString();
                if (valueStr.length() > 1000) {
                    Thread.sleep(10); // Simulate processing of large metadata
                } else {
                    Thread.sleep(1); // Simulate processing of small metadata
                }
            }
            
            logger.fine("Processed metadata entry: " + key + " for object: " + objectId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Metadata processing interrupted", e);
        }
    }
    
    /**
     * Checks if processing time is approaching the timeout limit
     */
    private boolean isTimeoutApproaching() {
        // This would typically check against the function start time
        // For now, we'll use a simple check
        return false; // Simplified for demo
    }
}