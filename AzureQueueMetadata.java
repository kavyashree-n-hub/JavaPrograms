/**
 * Metadata for Azure Queue operations
 */
public class AzureQueueMetadata {
    private int dequeueCount;
    
    private AzureQueueMetadata(Builder builder) {
        this.dequeueCount = builder.dequeueCount;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public int getDequeueCount() {
        return dequeueCount;
    }
    
    public static class Builder {
        private int dequeueCount;
        
        public Builder dequeueCount(int dequeueCount) {
            this.dequeueCount = dequeueCount;
            return this;
        }
        
        public AzureQueueMetadata build() {
            return new AzureQueueMetadata(this);
        }
    }
}