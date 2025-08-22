package com.ddworker.azure;

import com.ddworker.core.*;
import java.util.logging.Logger;

/**
 * Azure Function handler for processing subscription events
 * Each function invocation processes a single subscription event to prevent timeouts
 */
public class SubscriptionEventFunction {
    private static final Logger logger = Logger.getLogger(SubscriptionEventFunction.class.getName());
    
    private static DDWorker ddWorker;
    private static QueueManager queueManager;
    
    static {
        // Initialize DD Worker components
        queueManager = new QueueManager("subscription-events");
        SubscriptionManager subscriptionManager = new SubscriptionManager();
        ddWorker = new DDWorker(queueManager, subscriptionManager);
    }
    
    /**
     * Azure Function entry point for processing subscription events
     * This would be triggered by Service Bus or Storage Queue messages
     * 
     * Example Azure Function binding:
     * @FunctionName("processSubscriptionEvent")
     * @ServiceBusQueueTrigger(name = "message", queueName = "subscription-events", connection = "ServiceBusConnection")
     */
    public void processSubscriptionEvent(String queueMessage) {
        logger.info("Azure Function triggered for subscription event processing");
        
        try {
            // In a real implementation, this would deserialize the queue message
            // For now, we'll poll from our in-memory queue
            SubscriptionEvent event = queueManager.pollMessage();
            
            if (event != null) {
                logger.info("Processing subscription event: " + event.getSubscriptionId() 
                    + " for object: " + event.getObjectId());
                
                // Process the event asynchronously but wait for completion
                // This ensures the Azure Function doesn't complete until processing is done
                ddWorker.processSubscriptionEvent(event).join();
                
                logger.info("Successfully completed processing for subscription: " + event.getSubscriptionId());
            } else {
                logger.info("No subscription events available in queue");
            }
            
        } catch (Exception e) {
            logger.severe("Error in Azure Function processing: " + e.getMessage());
            // In Azure Functions, throwing an exception will trigger retry logic
            throw new RuntimeException("Failed to process subscription event", e);
        }
    }
    
    /**
     * Azure Function for handling object publications
     * This creates separate queue messages for each subscription
     * 
     * Example Azure Function binding:
     * @FunctionName("publishObject")
     * @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
     */
    public String publishObject(String httpRequestBody) {
        logger.info("Azure Function triggered for object publication");
        
        try {
            // In a real implementation, this would parse the HTTP request body
            // For demo, we'll create a sample publication
            ObjectPublication publication = createSamplePublication();
            
            logger.info("Publishing object: " + publication.getObjectId());
            
            // Publish the object asynchronously but wait for completion
            ddWorker.publishObject(publication).join();
            
            String response = "Successfully published object: " + publication.getObjectId();
            logger.info(response);
            return response;
            
        } catch (Exception e) {
            logger.severe("Error in object publication Azure Function: " + e.getMessage());
            throw new RuntimeException("Failed to publish object", e);
        }
    }
    
    /**
     * Azure Function for monitoring queue status
     * Provides insights into queue depth and processing status
     */
    public String getQueueStatus() {
        logger.info("Azure Function triggered for queue status check");
        
        try {
            int queueSize = queueManager.getQueueSize();
            int deadLetterSize = queueManager.getDeadLetterQueueSize();
            
            String status = String.format(
                "Queue Status - Main Queue: %d messages, Dead Letter Queue: %d messages",
                queueSize, deadLetterSize
            );
            
            logger.info(status);
            return status;
            
        } catch (Exception e) {
            logger.severe("Error getting queue status: " + e.getMessage());
            throw new RuntimeException("Failed to get queue status", e);
        }
    }
    
    /**
     * Creates a sample object publication for demonstration
     */
    private ObjectPublication createSamplePublication() {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("title", "Sample Document");
        metadata.put("author", "DD Worker");
        metadata.put("size", 1024);
        metadata.put("format", "PDF");
        metadata.put("created", System.currentTimeMillis());
        
        // Add large metadata to simulate real-world scenarios
        for (int i = 0; i < 50; i++) {
            metadata.put("field_" + i, "Large metadata value " + i + " with lots of content to simulate real world scenarios");
        }
        
        return new ObjectPublication("obj_" + System.currentTimeMillis(), "document", metadata);
    }
}