/**
 * Test class to demonstrate ObjectPublicationFunction functionality
 */
public class ObjectPublicationFunctionTest {
    public static void main(String[] args) {
        System.out.println("Testing ObjectPublicationFunction...");
        
        // Create an instance of the function
        ObjectPublicationFunction function = new ObjectPublicationFunction();
        
        // Create mock parameters
        String eventJson = "{\"message\":\"test event\"}";
        int dequeueCount = 1;
        ExecutionContext context = new ExecutionContext("test-invocation-123");
        
        // Execute the function
        try {
            function.execute(eventJson, dequeueCount, context);
            System.out.println("Function executed successfully!");
        } catch (Exception e) {
            System.err.println("Error executing function: " + e.getMessage());
        }
    }
}