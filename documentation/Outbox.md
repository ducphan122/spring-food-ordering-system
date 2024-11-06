As seen from Saga Pattern, what if the publishing fails? Or the consumer fails before running the business -> Outbox pattern is used to ensure that the message is published to the topic even if the business logic failed?

# Introduction
- Outbox Pattern, which uses local ACID transactions to create consistent distributed transactions and complete SAGA in a safe and consistent way.
- The SAGA pattern involves a long-running transaction with a local data store transaction and events publishing and consuming operations, but it can leave the system in an inconsistent state if not handled properly.
- The Outbox Pattern resolves this issue by not publishing events directly, instead keeping them in a local database table called the Outbox Table, which belongs to the same database used for local database operations. It use scheduler to read the outbox table and fire the event
- This allows for a single ACID transaction to complete database operations and insert events into the Outbox Table, ensuring that events are created automatically within the local database.
- The Outbox Pattern is completed by reading data from the Outbox Table and publishing events, which can be done using two approaches: 
  - pulling the table data
  - change data capture
- For now we will use the pulling Outbox Table approach and handle possible failure scenarios
- The Outbox Table is used to:
  - **Track SAGA Status**:
    - Maintains the current state of each SAGA operation
    - Records which steps have been completed
    - Stores compensation/rollback information if needed
    - Helps resume operations after system failures
  
  - **Ensure Idempotency**:
    - Stores unique message IDs for each event
    - Prevents duplicate processing of the same event or consume same data
  
  - **Prevent Data Corruption with Optimistic Locking and DB Constraints**:
    - Uses version numbers or timestamps for optimistic locking
    - Handles concurrent operations safely
    - Maintains data integrity across distributed transactions
    - Provides audit trail of all operations

# Implementation

- use ObjectMapper for Json serialization of domain events. With spring-boot-starter-json, a default ObjectMapper is created and it is used for serialization/deserialization, we can however add custom configuration, properties to it (Ex: add FAIL_ON_UNKNOWN_PROPERTIES to false will prevent failing when it encounters unknown JSON properties during serialization)

## Order Service
- the 3 events: OrderCreatedEvent, OrderPaidEvent, OrderApprovedEvent are persisted to the database tables but are segregated into two tables to keep unrelated events separate, one for payment servce and one for restaurant service.
- OrderPaymentOutboxMessage and OrderApprovalOutboxMessage are the outbox tables for payment and restaurant service respectively.

**ports/output/message/publisher**
- we dont need seperate publisher for each event (Ex: OrderCreatedEvent, OrderPaidEvent,OrderCancelledEvent, ) because we use outbox publisher for each service (payment, restaurant). We can differentiate the event type by using the payload object from outbox message (which are the domain events) 
  - In PaymentOutboxMessage there is OrderPaymentEventPayload, which has field paymentOrderStatus, which can be PAID or CREATED or CANCELLED
  - In RestaurantOutboxMessage there is OrderApprovalEventPayload, which has field restaurantOrderStatus, which can be APPROVED

**PaymentOutboxScheduler**
- This scheduler will read the outbox table, and publish the message to the topic. It only process message with outboxStatus = STARTED, and sagaStatus = STARTED or COMPENSATING. We use these 2 sagaStatus because in if we look at OrderSagaHelper, we are actually asking for order PENDING (when first Created) and CANCELLING (when restaurant reject the order) events, which are the 2 types of events that paymentService is subscribed to.
- This Scheduler will use paymentRequestMessagePublisher interface to publish the message, passing the outboxMessage as well as a method to update and persist outbox status to database. Because this method will be used by the publisher, which is a implementation of paymentRequestMessagePublisher interface in the messaging module
- The reason why we process message with outboxStatus = STARTED, because when Scheduler publish the message, it will update the outbox status to either COMPLETED or FAILED, so the next time scheduler process message, it wont process the same message again. In rare cases, because of network delay, the first scheduler publish method is not called until the second run of scheduler, this time the same outboxMessage may be published more than once
  - Option 1: this can be handled by strict lock and await mechanism, but it will make the scheduler slower and not acceptable in distributed applications
  - Option 2: we should be cautious for the consumer side, in this case is the restaurant service, after processing the message, it will update the outbox status to COMPLETED or FAILED, so the next time scheduler process message, it wont process the same message again. This will make sure we have idempotent (distince) messages

**PaymentOutboxCleanerScheduler**
- This scheduler will delete the outbox message with outboxStatus = COMPLETED, and sagaStatus = SUCCEEDED, FAILED, COMPENSATED. We use these 3 sagaStatus because we want to delete the message after it is successfully processed by the consumer. 
- We can also store the message in archive table for audit purpose (not implemented)

## Idempotency
- Idempotency is achieved through careful tracking of SagaStatus and OutboxStatus
- This prevents duplicate processing of the same event even if called multiple times

**Scenarios:**
See OrderPaymentSaga.java
1. **Status Checking**:
- Verifies outbox message status before processing
- Only processes messages in outboxStatus STARTED 
- Early returns if message already processed

2. **State Transitions**:
- Updates SagaStatus as operation progresses
- Moves SagaStatus from STARTED → PROCESSING
- Status changes are atomic within transactions

3. **Outbox Message Lifecycle**:
- Created with outboxStatus STARTED
- Every 10 seconds, PaymentOutboxScheduler will pick up the message with outboxStatus STARTED + SagaStatus STARTED or COMPENSATING (we dont explain this in this scenario) and update the SagaStatus to PROCESSING, then the event is published
- Then in payment-service, ...
- After payment is completed, the payment-service will update the outboxStatus to COMPLETED, then PaymentResponseAvroModel is published
- In order-service, the PaymentResponseKafkaListener will listen for the message and decide based on paymentStatus.
  - If payment is PAID, it will call paymentCompleted method in PaymentResponseMessageListener, which is in ports/input/message/listener/payment
  - If payment is CANCELLED, it will call paymentCancelled method. Here we dont discuss this 
- In paymentCompleted, it will first get the outBoxmessage that has sagaStatus as STARTED (in this stage the outBoxStatus is already set COMPLETED by payment-service), then it will using domain service to set the order to PAID, then update the sagaStatus to PROCESSING. Then it will save this outBoxmessage in both payment-outbox and approval-outbox tables. It saves to approval-outbox to prepare for the next step in saga (restaurant approval). The sagaStatus of payment-outbox will be updated to SUCCEEDED by OrderApprovalSaga.
- 

This approach ensures that even if the same operation is attempted multiple times (due to retries or system issues), it will only be processed once, maintaining data consistency

## Optimistic Locking in Concurrent Saga Processing
**Key Challenges**
- Multiple threads potentially processing the same saga simultaneously
- Risk of double updates to database records
- Need for thread-safe transaction management without performance bottlenecks

**Optimistic Locking Solution**
- Uses a version field in database entities
- Allows concurrent read access
Prevents simultaneous updates through version checking mechanism
**How It Works**
1. Initial version starts at zero
2. When updating, system checks current version
3. If versions match, update proceeds and version increments
4. If versions differ, an optimistic lock exception occurs
5. Conflicting thread's transaction rolls back

**Concurrent Processing Scenarios**
See OrderPaymentSaga.java
1. First thread completes: Second thread finds no processable record
2. Threads arrive simultaneously:
   - One thread successfully updates, other threads wait for the first thread to commit (see Postgres Isolation Level Discussion)
   - Other thread gets rolled back due to version mismatch
3. Additional protection via unique constraints on certain tables

### Postgres Isolation Level Discussion

**Read Committed Behavior (Postgres Default)**
- This is the default isolation level in PostgreSQL
- When first thread updates but hasn't committed:
  1. Second thread will wait for first transaction to commit
  2. Only gets data after the update is committed
  3. Will see updated status and return empty result
  4. Prevents "dirty reads"

**Read Uncommitted Alternative**
- If using read uncommitted:
  1. Second thread wouldn't find the data
  2. Can't see data with "started" status because it's already changed (though uncommitted)
  3. Would return immediately

The read committed isolation level in Postgres actually helps enforce the desired behavior by making concurrent threads wait for committed changes rather than seeing intermediate states.

## Pessimistic vs Optimistic Locking Comparison

**Pessimistic Locking Mentioned**
- Uses "SELECT FOR UPDATE" approach
- Locks the row completely
- Prevents even reading operations from other transactions
- Better for monetary operations specifically

**Why Not Pessimistic Here**
- Performance impact is significant
- Blocks other transactions entirely
- Only allows single thread execution at a time
- Would "affect the performance badly" (author's words)

**Author's Recommendation**
- Use pessimistic locking for monetary operations
- Prefer optimistic locking for most other operations
- Especially good when collision probability is low

**Trade-offs Discussed**
Optimistic:
- Handles rollbacks on collision
- Better performance with few collisions
- Less blocking

Pessimistic:
- Stronger isolation
- More suitable for critical financial transactions
- Higher performance cost due to blocking

