package com.ddworker.demo;

import com.ddworker.core.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Focused test to demonstrate timeout prevention and separate processing
 */
public class TimeoutPreventionTest {
    private static final Logger logger = Logger.getLogger(TimeoutPreventionTest.class.getName());
    
    public static void main(String[] args) {
        logger.info("=== DD Worker Timeout Prevention Test ===");
        
        // Test scenario: Large metadata that would cause timeout in single processing
        testLargeMetadataChunking();
        
        // Test scenario: Multiple subscriptions processed separately
        testSeparateSubscriptionProcessing();
        
        logger.info("=== Timeout Prevention Test Completed ===");
    }
    
    private static void testLargeMetadataChunking() {
        logger.info("\n--- Test 1: Large Metadata Chunking ---");
        
        // Create very large metadata that would timeout in single batch
        Map<String, Object> largeMetadata = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            largeMetadata.put("large_field_" + i, 
                "This is a very large metadata entry number " + i + " " +
                "that contains substantial content to simulate real-world scenarios " +
                "where metadata can be extremely large and would cause timeouts " +
                "if processed as a single batch. The chunking mechanism should " +
                "break this down into manageable pieces.");
        }
        
        // Test with chunking enabled (should handle large metadata)
        ProcessingConfig chunkingConfig = new ProcessingConfig(3, 5000, 100, true, 1048576);
        MetadataProcessor processorWithChunking = new MetadataProcessor(chunkingConfig);
        
        logger.info("Processing " + largeMetadata.size() + " metadata entries with chunking enabled");
        long startTime = System.currentTimeMillis();
        
        processorWithChunking.processMetadata(largeMetadata, "large_test_object");
        
        long chunkingTime = System.currentTimeMillis() - startTime;
        logger.info("Chunking processing completed in: " + chunkingTime + "ms");
        
        // Test without chunking (would be slower/timeout-prone)
        ProcessingConfig noChunkingConfig = new ProcessingConfig(3, 5000, 100, false, 1048576);
        MetadataProcessor processorNoChunking = new MetadataProcessor(noChunkingConfig);
        
        logger.info("Processing same metadata without chunking (single batch)");
        startTime = System.currentTimeMillis();
        
        processorNoChunking.processMetadata(largeMetadata, "large_test_object_no_chunk");
        
        long batchTime = System.currentTimeMillis() - startTime;
        logger.info("Batch processing completed in: " + batchTime + "ms");
        
        logger.info("Performance comparison - Chunking: " + chunkingTime + "ms vs Batch: " + batchTime + "ms");
    }
    
    private static void testSeparateSubscriptionProcessing() {
        logger.info("\n--- Test 2: Separate Subscription Processing ---");
        
        QueueManager queueManager = new QueueManager("test-queue");
        SubscriptionManager subscriptionManager = new SubscriptionManager();
        DDWorker ddWorker = new DDWorker(queueManager, subscriptionManager);
        
        // Create object publication
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Document");
        metadata.put("size", 2048);
        
        ObjectPublication publication = new ObjectPublication("test_obj_separate", "document", metadata);
        
        logger.info("Publishing object to demonstrate separate subscription processing");
        
        // Record initial queue state
        int initialQueueSize = queueManager.getQueueSize();
        logger.info("Initial queue size: " + initialQueueSize);
        
        // Publish object - this creates separate events for each subscription
        ddWorker.publishObject(publication).join();
        
        int finalQueueSize = queueManager.getQueueSize();
        int eventsCreated = finalQueueSize - initialQueueSize;
        
        logger.info("Final queue size: " + finalQueueSize);
        logger.info("Separate events created: " + eventsCreated);
        
        // Process each event separately to demonstrate isolation
        logger.info("Processing each subscription event separately:");
        
        for (int i = 0; i < eventsCreated && queueManager.hasMessages(); i++) {
            SubscriptionEvent event = queueManager.receiveMessage();
            
            logger.info("Processing event " + (i + 1) + " for subscription: " + event.getSubscriptionId());
            long startTime = System.currentTimeMillis();
            
            // This would be a separate Azure Function invocation in production
            ddWorker.processSubscriptionEvent(event).join();
            
            long processingTime = System.currentTimeMillis() - startTime;
            logger.info("Event " + (i + 1) + " processed in: " + processingTime + "ms (separate invocation)");
        }
        
        // Show processing statistics
        Map<String, Integer> stats = subscriptionManager.getProcessingStats();
        logger.info("Final processing statistics:");
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            logger.info("  " + entry.getKey() + ": " + entry.getValue() + " events processed");
        }
        
        ddWorker.shutdown();
    }
}