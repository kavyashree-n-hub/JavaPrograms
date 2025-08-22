/**
 * Mock annotation for Azure Queue Trigger
 */
public @interface QueueTrigger {
    String queueName();
    String connection();
    String name();
}