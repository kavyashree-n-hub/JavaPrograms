/**
 * Mock ExecutionContext for Azure Functions
 */
public class ExecutionContext {
    private String invocationId;
    
    public ExecutionContext(String invocationId) {
        this.invocationId = invocationId;
    }
    
    public String getInvocationId() {
        return invocationId;
    }
}