# DD Worker - Azure Function Object Publication Handler

This implementation provides a solution for handling object publications where each subscription process runs as a separate event/Azure function invocation. This architecture helps handle large metadata volumes and prevents timeout exceptions.

## Architecture Overview

The DD Worker system consists of several key components:

### Core Components

1. **DDWorker** - Main orchestrator that handles object publications and subscription processing
2. **QueueManager** - Manages message queues for separate event processing
3. **SubscriptionManager** - Handles subscription registration and tracking
4. **MetadataProcessor** - Processes large metadata volumes with chunking to prevent timeouts
5. **SubscriptionEventFunction** - Azure Function handlers for separate subscription processing

### Key Features

- **Separate Event Processing**: Each subscription creates a separate queue message, allowing Azure Functions to process them independently
- **Timeout Prevention**: Large metadata is processed in chunks with configurable batch sizes and timeouts
- **Scalable Architecture**: Each Azure Function invocation handles only one subscription, enabling horizontal scaling
- **Dead Letter Queue**: Failed events are moved to a dead letter queue for retry/manual intervention
- **Monitoring**: Built-in statistics and queue monitoring capabilities

## How It Works

### 1. Object Publication Flow

```
Object Publication -> DD Worker -> Creates Separate Events -> Queue Messages -> Azure Functions
```

1. When an object is published, DDWorker identifies all active subscriptions for that object type
2. For each subscription, a separate `SubscriptionEvent` is created and sent to the queue
3. Each queue message triggers a separate Azure Function invocation
4. This ensures subscription processing is isolated and scalable

### 2. Subscription Processing

Each Azure Function invocation:
- Receives a single subscription event from the queue
- Processes the metadata using chunking for large volumes
- Marks the event as processed
- Handles errors by sending failed events to dead letter queue

### 3. Large Metadata Handling

The `MetadataProcessor` provides:
- **Chunking**: Large metadata is processed in configurable batch sizes
- **Timeout Monitoring**: Processing time is monitored to prevent timeouts
- **Incremental Processing**: Metadata entries are processed incrementally with timeout checks

## Configuration

Key configuration options in `ProcessingConfig`:

- `maxRetries`: Maximum retry attempts for failed processing
- `timeoutMillis`: Maximum processing time before timeout (Azure Function default: 5 minutes)
- `batchSize`: Number of metadata entries to process per chunk
- `enableChunking`: Whether to enable chunked processing for large metadata
- `maxMetadataSize`: Maximum allowed metadata size

## Usage Example

```java
// Initialize components
QueueManager queueManager = new QueueManager("subscription-events");
SubscriptionManager subscriptionManager = new SubscriptionManager();
DDWorker ddWorker = new DDWorker(queueManager, subscriptionManager);

// Create object publication with metadata
Map<String, Object> metadata = new HashMap<>();
metadata.put("title", "Sample Document");
metadata.put("size", 1024);
// ... add more metadata

ObjectPublication publication = new ObjectPublication("obj_123", "document", metadata);

// Publish object - creates separate events for each subscription
ddWorker.publishObject(publication);

// Azure Functions will process each subscription event separately
```

## Azure Function Integration

The `SubscriptionEventFunction` class provides Azure Function handlers:

### Service Bus Queue Trigger
```java
@FunctionName("processSubscriptionEvent")
@ServiceBusQueueTrigger(name = "message", queueName = "subscription-events", connection = "ServiceBusConnection")
public void processSubscriptionEvent(String queueMessage)
```

### HTTP Trigger for Object Publication
```java
@FunctionName("publishObject")
@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
public String publishObject(String httpRequestBody)
```

## Benefits

1. **Prevents Timeouts**: Each subscription is processed in a separate function invocation with its own timeout
2. **Handles Large Metadata**: Chunking mechanism processes large metadata volumes without timing out
3. **Scalable**: Azure Functions can scale horizontally to handle multiple subscriptions simultaneously
4. **Fault Tolerant**: Failed events are sent to dead letter queue for retry/manual intervention
5. **Monitoring**: Built-in monitoring for queue depth and processing statistics

## Running the Demo

Compile and run the demo application:

```bash
javac -cp src/main/java src/main/java/com/ddworker/demo/DDWorkerDemo.java
java -cp src/main/java com.ddworker.demo.DDWorkerDemo
```

The demo will show:
1. Subscription registration
2. Object publication creating separate events
3. Azure Function processing simulation
4. Large metadata handling with chunking
5. Monitoring and statistics

## Production Deployment

For production deployment:

1. Replace in-memory queue with Azure Service Bus or Storage Queue
2. Add proper authentication and security
3. Configure logging and monitoring (Application Insights)
4. Set up proper error handling and retry policies
5. Configure auto-scaling for Azure Functions
6. Add health checks and monitoring endpoints

## File Structure

```
src/main/java/com/ddworker/
├── core/
│   ├── DDWorker.java              # Main orchestrator
│   ├── QueueManager.java          # Queue management
│   ├── SubscriptionManager.java   # Subscription handling
│   ├── MetadataProcessor.java     # Large metadata processing
│   ├── ObjectPublication.java     # Object publication model
│   ├── Subscription.java          # Subscription model
│   ├── SubscriptionEvent.java     # Event model
│   └── ProcessingConfig.java      # Configuration model
├── azure/
│   └── SubscriptionEventFunction.java  # Azure Function handlers
└── demo/
    └── DDWorkerDemo.java          # Demo application
```