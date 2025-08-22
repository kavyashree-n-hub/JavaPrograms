package com.ddworker.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Queue Manager for handling subscription events
 * In a real Azure environment, this would integrate with Service Bus or Storage Queues
 */
public class QueueManager {
    private static final Logger logger = Logger.getLogger(QueueManager.class.getName());
    
    // In-memory queues for demonstration - in production would use Azure Service Bus/Storage Queue
    private final BlockingQueue<SubscriptionEvent> mainQueue;
    private final BlockingQueue<SubscriptionEvent> deadLetterQueue;
    private final String queueName;
    
    public QueueManager(String queueName) {
        this.queueName = queueName;
        this.mainQueue = new LinkedBlockingQueue<>();
        this.deadLetterQueue = new LinkedBlockingQueue<>();
    }
    
    /**
     * Sends a subscription event to the queue
     * Each message represents a single subscription processing event
     */
    public void sendMessage(SubscriptionEvent event) {
        try {
            mainQueue.put(event);
            logger.info("Sent subscription event to queue: " + event.getSubscriptionId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send message to queue", e);
        }
    }
    
    /**
     * Receives a subscription event from the queue
     * This would be called by Azure Functions to get the next event to process
     */
    public SubscriptionEvent receiveMessage() {
        try {
            SubscriptionEvent event = mainQueue.take();
            logger.info("Received subscription event from queue: " + event.getSubscriptionId());
            return event;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to receive message from queue", e);
        }
    }
    
    /**
     * Sends failed events to dead letter queue for manual intervention
     */
    public void sendToDeadLetterQueue(SubscriptionEvent event, String errorMessage) {
        try {
            deadLetterQueue.put(event);
            logger.severe("Sent subscription event to dead letter queue: " + event.getSubscriptionId() 
                + " - Error: " + errorMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send message to dead letter queue", e);
        }
    }
    
    /**
     * Gets the current queue size - useful for monitoring
     */
    public int getQueueSize() {
        return mainQueue.size();
    }
    
    /**
     * Gets the dead letter queue size - useful for monitoring
     */
    public int getDeadLetterQueueSize() {
        return deadLetterQueue.size();
    }
    
    /**
     * Checks if the main queue has messages available
     */
    public boolean hasMessages() {
        return !mainQueue.isEmpty();
    }
    
    /**
     * Polls for a message without blocking - useful for Azure Functions trigger
     */
    public SubscriptionEvent pollMessage() {
        SubscriptionEvent event = mainQueue.poll();
        if (event != null) {
            logger.info("Polled subscription event from queue: " + event.getSubscriptionId());
        }
        return event;
    }
}