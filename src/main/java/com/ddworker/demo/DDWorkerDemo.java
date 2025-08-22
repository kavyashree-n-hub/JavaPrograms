package com.ddworker.demo;

import com.ddworker.azure.SubscriptionEventFunction;
import com.ddworker.core.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Demo application to showcase DD Worker functionality
 * Demonstrates how object publications are processed as separate Azure function invocations
 */
public class DDWorkerDemo {
    private static final Logger logger = Logger.getLogger(DDWorkerDemo.class.getName());
    
    public static void main(String[] args) {
        logger.info("Starting DD Worker Demo");
        
        try {
            // Initialize components
            QueueManager queueManager = new QueueManager("demo-queue");
            SubscriptionManager subscriptionManager = new SubscriptionManager();
            DDWorker ddWorker = new DDWorker(queueManager, subscriptionManager);
            SubscriptionEventFunction azureFunction = new SubscriptionEventFunction();
            
            // Demo 1: Show subscription registration
            demonstrateSubscriptionRegistration(subscriptionManager);
            
            // Demo 2: Publish an object and show separate event creation
            demonstrateObjectPublication(ddWorker, queueManager);
            
            // Demo 3: Simulate Azure Function processing
            demonstrateAzureFunctionProcessing(azureFunction, queueManager);
            
            // Demo 4: Show large metadata handling
            demonstrateLargeMetadataHandling(ddWorker, queueManager);
            
            // Demo 5: Show monitoring capabilities
            demonstrateMonitoring(subscriptionManager, queueManager);
            
            // Cleanup
            ddWorker.shutdown();
            logger.info("DD Worker Demo completed successfully");
            
        } catch (Exception e) {
            logger.severe("Error in DD Worker Demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateSubscriptionRegistration(SubscriptionManager subscriptionManager) {
        logger.info("\n=== Demo 1: Subscription Registration ===");
        
        // Add a custom subscription
        ProcessingConfig customConfig = new ProcessingConfig(5, 300000, 200, true, 2097152);
        Subscription customSubscription = new Subscription("custom-processor", "document", true, customConfig);
        subscriptionManager.addSubscription(customSubscription);
        
        // Show active subscriptions
        var documentSubs = subscriptionManager.getActiveSubscriptions("document");
        logger.info("Active document subscriptions: " + documentSubs.size());
        for (Subscription sub : documentSubs) {
            logger.info("  - " + sub.getSubscriptionId());
        }
    }
    
    private static void demonstrateObjectPublication(DDWorker ddWorker, QueueManager queueManager) {
        logger.info("\n=== Demo 2: Object Publication ===");
        
        // Create a sample object with metadata
        Map<String, Object> metadata = createSampleMetadata();
        ObjectPublication publication = new ObjectPublication("demo_object_123", "document", metadata);
        
        logger.info("Publishing object: " + publication.getObjectId());
        int initialQueueSize = queueManager.getQueueSize();
        
        // Publish object - this will create separate events for each subscription
        ddWorker.publishObject(publication).join();
        
        int finalQueueSize = queueManager.getQueueSize();
        int eventsCreated = finalQueueSize - initialQueueSize;
        
        logger.info("Events created in queue: " + eventsCreated);
        logger.info("Queue size after publication: " + finalQueueSize);
    }
    
    private static void demonstrateAzureFunctionProcessing(SubscriptionEventFunction azureFunction, 
                                                          QueueManager queueManager) {
        logger.info("\n=== Demo 3: Azure Function Processing ===");
        
        logger.info("Simulating Azure Function triggers for subscription events...");
        
        // Process events as separate Azure Function invocations
        int processedCount = 0;
        while (queueManager.hasMessages() && processedCount < 5) {
            logger.info("Triggering Azure Function for subscription event " + (processedCount + 1));
            
            try {
                // Simulate Azure Function trigger
                azureFunction.processSubscriptionEvent("queue-message-" + processedCount);
                processedCount++;
                
                // Simulate some delay between function invocations
                Thread.sleep(100);
                
            } catch (Exception e) {
                logger.warning("Azure Function processing error: " + e.getMessage());
            }
        }
        
        logger.info("Processed " + processedCount + " subscription events as separate Azure Function invocations");
    }
    
    private static void demonstrateLargeMetadataHandling(DDWorker ddWorker, QueueManager queueManager) {
        logger.info("\n=== Demo 4: Large Metadata Handling ===");
        
        // Create object with large metadata to test chunking
        Map<String, Object> largeMetadata = createLargeMetadata(500); // 500 metadata entries
        ObjectPublication largePublication = new ObjectPublication("large_object_456", "image", largeMetadata);
        
        logger.info("Publishing object with large metadata: " + largeMetadata.size() + " entries");
        
        // Publish and process
        ddWorker.publishObject(largePublication).join();
        
        // Process one of the large metadata events
        if (queueManager.hasMessages()) {
            SubscriptionEvent event = queueManager.receiveMessage();
            logger.info("Processing large metadata event for subscription: " + event.getSubscriptionId());
            ddWorker.processSubscriptionEvent(event).join();
        }
    }
    
    private static void demonstrateMonitoring(SubscriptionManager subscriptionManager, QueueManager queueManager) {
        logger.info("\n=== Demo 5: Monitoring ===");
        
        // Show processing statistics
        Map<String, Integer> stats = subscriptionManager.getProcessingStats();
        logger.info("Processing Statistics:");
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            logger.info("  " + entry.getKey() + ": " + entry.getValue() + " events processed");
        }
        
        // Show queue status
        logger.info("Queue Status:");
        logger.info("  Main Queue: " + queueManager.getQueueSize() + " messages");
        logger.info("  Dead Letter Queue: " + queueManager.getDeadLetterQueueSize() + " messages");
    }
    
    private static Map<String, Object> createSampleMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Demo Document");
        metadata.put("author", "DD Worker Demo");
        metadata.put("created", System.currentTimeMillis());
        metadata.put("size", 2048);
        metadata.put("format", "PDF");
        metadata.put("version", "1.0");
        metadata.put("tags", new String[]{"demo", "document", "test"});
        return metadata;
    }
    
    private static Map<String, Object> createLargeMetadata(int entryCount) {
        Map<String, Object> metadata = new HashMap<>();
        
        for (int i = 0; i < entryCount; i++) {
            metadata.put("field_" + i, "This is a large metadata entry number " + i + 
                " with substantial content to simulate real-world scenarios where metadata can be quite large and complex. " +
                "This helps test the chunking functionality and timeout prevention mechanisms.");
        }
        
        // Add some structured data
        metadata.put("processing_config", Map.of(
            "timeout", 300000,
            "retries", 3,
            "batch_size", 100
        ));
        
        metadata.put("object_info", Map.of(
            "type", "large_object",
            "classification", "high_volume",
            "priority", "normal"
        ));
        
        return metadata;
    }
}