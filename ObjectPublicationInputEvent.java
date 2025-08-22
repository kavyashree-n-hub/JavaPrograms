/**
 * Input event class for object publication operations
 */
public class ObjectPublicationInputEvent {
    private String invocationId;
    private AzureQueueMetadata azureQueueMetadata;
    
    public String getInvocationId() {
        return invocationId;
    }
    
    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }
    
    public AzureQueueMetadata getAzureQueueMetadata() {
        return azureQueueMetadata;
    }
    
    public void setAzureQueueMetadata(AzureQueueMetadata azureQueueMetadata) {
        this.azureQueueMetadata = azureQueueMetadata;
    }
}