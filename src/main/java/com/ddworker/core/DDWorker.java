package com.ddworker.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * DD Worker for handling object publications with separate event processing
 * Each subscription process runs as a separate Azure function invocation
 * to handle large metadata volumes and prevent timeout exceptions
 */
public class DDWorker {
    private static final Logger logger = Logger.getLogger(DDWorker.class.getName());
    private final QueueManager queueManager;
    private final SubscriptionManager subscriptionManager;
    private final ExecutorService executor;
    private final int maxConcurrentProcessing;
    
    public DDWorker(QueueManager queueManager, SubscriptionManager subscriptionManager) {
        this.queueManager = queueManager;
        this.subscriptionManager = subscriptionManager;
        this.maxConcurrentProcessing = Integer.parseInt(
            System.getProperty("dd.worker.max.concurrent", "10")
        );
        this.executor = Executors.newFixedThreadPool(maxConcurrentProcessing);
    }
    
    /**
     * Publishes an object and creates separate queue messages for each subscription
     * This ensures each subscription is processed as a separate Azure function invocation
     */
    public CompletableFuture<Void> publishObject(ObjectPublication publication) {
        logger.info("Publishing object: " + publication.getObjectId());
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Get all active subscriptions for this object type
                List<Subscription> subscriptions = subscriptionManager
                    .getActiveSubscriptions(publication.getObjectType());
                
                logger.info("Found " + subscriptions.size() + " active subscriptions for object type: " 
                    + publication.getObjectType());
                
                // Create separate queue messages for each subscription
                // This ensures each subscription is processed independently
                for (Subscription subscription : subscriptions) {
                    SubscriptionEvent event = new SubscriptionEvent(
                        publication.getObjectId(),
                        publication.getObjectType(),
                        publication.getMetadata(),
                        subscription.getSubscriptionId(),
                        subscription.getProcessingConfig()
                    );
                    
                    // Send each subscription event to the queue separately
                    // This allows Azure Functions to pick up and process each one independently
                    queueManager.sendMessage(event);
                    
                    logger.info("Queued subscription event for subscription: " 
                        + subscription.getSubscriptionId());
                }
                
                logger.info("Successfully published object " + publication.getObjectId() 
                    + " to " + subscriptions.size() + " subscription queues");
                    
            } catch (Exception e) {
                logger.severe("Error publishing object " + publication.getObjectId() + ": " + e.getMessage());
                throw new RuntimeException("Failed to publish object", e);
            }
        }, executor);
    }
    
    /**
     * Processes a subscription event - this method is called by Azure Functions
     * Each invocation handles a single subscription to prevent timeouts
     */
    public CompletableFuture<Void> processSubscriptionEvent(SubscriptionEvent event) {
        logger.info("Processing subscription event for subscription: " + event.getSubscriptionId());
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Process metadata in chunks to handle large volumes
                MetadataProcessor processor = new MetadataProcessor(event.getProcessingConfig());
                processor.processMetadata(event.getMetadata(), event.getObjectId());
                
                // Mark subscription event as processed
                subscriptionManager.markEventProcessed(event.getSubscriptionId(), event.getObjectId());
                
                logger.info("Successfully processed subscription event for subscription: " 
                    + event.getSubscriptionId());
                    
            } catch (Exception e) {
                logger.severe("Error processing subscription event for subscription " 
                    + event.getSubscriptionId() + ": " + e.getMessage());
                
                // Send to dead letter queue for retry/manual intervention
                queueManager.sendToDeadLetterQueue(event, e.getMessage());
                throw new RuntimeException("Failed to process subscription event", e);
            }
        }, executor);
    }
    
    /**
     * Gracefully shutdown the worker
     */
    public void shutdown() {
        logger.info("Shutting down DD Worker");
        executor.shutdown();
    }
}