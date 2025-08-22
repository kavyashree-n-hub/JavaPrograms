package com.ddworker.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manager for handling subscriptions and tracking processed events
 */
public class SubscriptionManager {
    private static final Logger logger = Logger.getLogger(SubscriptionManager.class.getName());
    
    // In-memory storage for demonstration - in production would use a database
    private final Map<String, List<Subscription>> subscriptionsByType;
    private final Map<String, Map<String, Boolean>> processedEvents; // subscriptionId -> objectId -> processed
    
    public SubscriptionManager() {
        this.subscriptionsByType = new ConcurrentHashMap<>();
        this.processedEvents = new ConcurrentHashMap<>();
        
        // Initialize with some sample subscriptions
        initializeSampleSubscriptions();
    }
    
    /**
     * Gets all active subscriptions for a given object type
     */
    public List<Subscription> getActiveSubscriptions(String objectType) {
        List<Subscription> subscriptions = subscriptionsByType.getOrDefault(objectType, new ArrayList<>());
        List<Subscription> activeSubscriptions = subscriptions.stream()
            .filter(Subscription::isActive)
            .collect(Collectors.toList());
            
        logger.info("Found " + activeSubscriptions.size() + " active subscriptions for object type: " + objectType);
        return activeSubscriptions;
    }
    
    /**
     * Adds a new subscription
     */
    public void addSubscription(Subscription subscription) {
        subscriptionsByType
            .computeIfAbsent(subscription.getObjectType(), k -> new ArrayList<>())
            .add(subscription);
            
        processedEvents.putIfAbsent(subscription.getSubscriptionId(), new ConcurrentHashMap<>());
        
        logger.info("Added subscription: " + subscription.getSubscriptionId() 
            + " for object type: " + subscription.getObjectType());
    }
    
    /**
     * Marks an event as processed for a specific subscription
     */
    public void markEventProcessed(String subscriptionId, String objectId) {
        processedEvents
            .computeIfAbsent(subscriptionId, k -> new ConcurrentHashMap<>())
            .put(objectId, true);
            
        logger.info("Marked event processed: subscription=" + subscriptionId + ", object=" + objectId);
    }
    
    /**
     * Checks if an event has been processed for a specific subscription
     */
    public boolean isEventProcessed(String subscriptionId, String objectId) {
        return processedEvents
            .getOrDefault(subscriptionId, new HashMap<>())
            .getOrDefault(objectId, false);
    }
    
    /**
     * Gets processing statistics for monitoring
     */
    public Map<String, Integer> getProcessingStats() {
        Map<String, Integer> stats = new HashMap<>();
        
        for (Map.Entry<String, Map<String, Boolean>> entry : processedEvents.entrySet()) {
            String subscriptionId = entry.getKey();
            int processedCount = (int) entry.getValue().values().stream()
                .mapToLong(processed -> processed ? 1 : 0)
                .sum();
            stats.put(subscriptionId, processedCount);
        }
        
        return stats;
    }
    
    /**
     * Initialize some sample subscriptions for demonstration
     */
    private void initializeSampleSubscriptions() {
        // Document processing subscription
        addSubscription(new Subscription(
            "doc-processor-1", 
            "document", 
            true, 
            ProcessingConfig.getDefault()
        ));
        
        // Image processing subscription  
        addSubscription(new Subscription(
            "image-processor-1", 
            "image", 
            true, 
            new ProcessingConfig(5, 600000, 50, true, 2097152) // 10 min timeout, 2MB metadata
        ));
        
        // Analytics subscription
        addSubscription(new Subscription(
            "analytics-1", 
            "document", 
            true, 
            ProcessingConfig.getDefault()
        ));
        
        // Video processing subscription (inactive for demo)
        addSubscription(new Subscription(
            "video-processor-1", 
            "video", 
            false, 
            new ProcessingConfig(3, 1800000, 10, true, 5242880) // 30 min timeout, 5MB metadata
        ));
        
        logger.info("Initialized sample subscriptions");
    }
}