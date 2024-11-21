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

A typical outbox structure is as follows:
- in **application-service/outbox**, we have model and scheduler. 
  - In model, We have OderOutboxMessage. In OderOutboxMessage, we also have a string payload, which is the serialized json of OrderEventPayload
  - In scheduler, we have PaymentOutboxScheduler and PaymentOutboxCleanerScheduler.
- In **application-service/ports/output/repository**, which is the interface to save, find, delete the outbox message. This interface is implemented in dataaccess layer
- In **application-service/ports/output/message/publisher**, we have the outbox publisher interface, which is implemented in messaging module. This interface has a publish method, which takes in an outboxMessage and a callback BiConsumer method to update outbox status 

## Order Service
- the 3 events: OrderCreatedEvent, OrderPaidEvent, OrderApprovedEvent are persisted to the database tables but are segregated into two tables to keep unrelated events separate, one for payment servce and one for restaurant service.
- OrderPaymentOutboxMessage and OrderApprovalOutboxMessage are the outbox tables for payment and restaurant service respectively.


**ports/output/message/publisher**
- we dont need seperate publisher for each event (Ex: OrderCreatedEvent, OrderPaidEvent,OrderCancelledEvent, ) because we use outbox publisher for each service (payment, restaurant). We can differentiate the event type by using the payload object from outbox message (which are the domain events) 
  - In PaymentOutboxMessage there is OrderPaymentEventPayload, which has field paymentOrderStatus, which can be PAID or CREATED or CANCELLED
  - In RestaurantOutboxMessage there is OrderApprovalEventPayload, which has field restaurantOrderStatus, which can be APPROVED

**PaymentOutboxScheduler Notes**
In rare cases, because of network delay, the first scheduler publish method is not called until the second run of scheduler, this time the same OrderPaymentOutboxMessage->PaymentRequestAvroModel may be published more than once
  - Option 1: this can be handled by strict lock and await mechanism, but it will make the scheduler slower and not acceptable in distributed applications
  - Option 2: we should be cautious for the consumer side, in this case is the payment service (in PaymentRequestKafkaListener -> PaymentRequestHelper.persistPayment), we will eliminate duplicate message (not run the business logic). This will make sure we have idempotent (distinct) message. See PaymentRequestHelper.persistPayment comment
- Notes, in both OrderService and PaymentService, we have optimistic locking and unique constraint to prevent saving the final state of OutboxMessage. But in rare cases,if 2 concurrent threads that simultaneously bypass the check (if exist outBoxMessage before), they can both run the business logic, even for the payment service to update the CreditEntry and CreditHistory. However, this is not an issue because at the end, it will need to save the final state of OutboxMessage, and the unique constraint or optimistic locking will raise an exception. Because we annotate the entire operation with @Transactional, any changes made by one of the thread will be rolled back. 

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
Stage1
- An order is created, saved to database, and an outbox message is also created with outboxStatus STARTED, it has the OrderEventPayload as payload string. This outbox Message is saved in payment_outbox table.
- Every 10 seconds, the PaymentOutboxScheduler will read the outbox table and pick up the message that has outboxStatus STARTED + SagaStatus STARTED (translate to OrderPending) + SagaStatus COMPENSATING (we dont explain this in this scenario). It then called the publish method via interface PaymentRequestMessagePublisher, passing in the outboxMessage and a callback method to update the outboxStatus of OrderPaymentOutboxMessage in payment_outbox table.
- OrderPaymentEventKafkaPublisher is implementation of PaymentRequestMessagePublisher, it will get the OrderPaymentEventPayload from outboxMessage, then convert it to PaymentRequestAvroModel, then publish it to the payment-request topic, it will also pass the callback method to update the outboxStatus of OrderPaymentOutboxMessage in payment_outbox table.
- If the message is published successfully, Kafka will call the callback method via KafkaMessageHelper, and use this callback to update the outboxStatus of OrderPaymentOutboxMessage in payment_outbox table to COMPLETED. If the message is not published successfully, the callback will update the outboxStatus to FAILED. Remember that the SagaStatus is STARTED. This will be needed in stage 3

Stage2
- Then in payment-service, we have PaymentRequestKafkaListener to listen for the message from payment-request topic. Based on the paymentOrderStatus in PaymentRequestAvroModel, it will call paymentRequestMessageListener completePayment or cancelPayment method (in this scenario we will focus on completePayment). If we have multiple implentations of paymentRequestMessageListener, we can use the @Qualifier to specify which one to use.
- paymentRequestMessageListener is the interface defined in ports/input. It has an implementation PaymentRequestMessageListenerImpl. The completePayment method will use PaymentRequestHelper in domain-service to persist the payment. After running business logic, it will use OrderOutboxHelper->OrderOutboxRepository interface->OrderOutboxRepositoryImpl (in dataacess adapter)->OrderOutboxJpaRepository (@Repository) to save this outboxmessage in order-outbox table with outboxStatus STARTED, paymentStatus is COMPLETED
- Then the OrderOutboxScheduler will every 10 seconds pick up OrderOutboxMessage that has outboxStatus STARTED, and publish it to the message bus using paymentResponseMessagePublisher. It pass in the outboxMessage and a callback method to update the outboxStatus of OrderOutboxMessage in order-outbox table.
- the PaymentEventKafkaPublisher (implementation of paymentResponseMessagePublisher) will convert the OrderOutboxMessage to PaymentResponseAvroModel, then publish it to the payment-response topic, pass the outBoxCallback to KafkaCallback
- if the message is published successfully, Kafka will call the callback method via KafkaMessageHelper, and use this callback to update the outboxStatus of OrderOutboxMessage in order-outbox table to COMPLETED. If the message is not published successfully, the callback will update the outboxStatus to FAILED.

Stage3
- Then in order-service, the PaymentResponseKafkaListener will listen for the message and decide based on paymentStatus. if paymentStatus is COMPLETED, it will call paymentCompleted method in PaymentResponseMessageListener, which is in ports/input/message/listener/payment. If paymentStatus is CANCELLED or FAILED, it will call paymentCancelled method.
- In paymentCompleted, it will delegate to orderPaymentSaga to process, passing paymentResponse as parameter. This paymentResponse has sagaId and PaymentStatus
- orderPaymentSaga will first get the OrderPaymentOutboxMessage that has sagaId and sagaStatus as STARTED (in this stage the outBoxStatus is already set COMPLETED in stage1 by Kafkacallback, the outBoxStatus will indicates if the outbux message is waiting to be picked up (STARTED) or is sent successfully by kafka (COMPLETED) or failed (FAILED)). The reason why we need to check sagaStatus is STARTED is because we want to make sure the message is not processed twice. 
- orderPaymentSaga will then run business logic to order and then get the sagaStatus of PROCESSING (translate to OrderPaid)
  - at this point, there is optimistic lock 
  - it then save the updated OrderPaymentOutboxMessage in payment-outbox table with sagaStatus PROCESSING, orderStatus PAID, processedAt as current time. The outBoxStatus is still COMPLETED so no need to update it.
  - it then also saves a OrderApprovalOutboxMessage in restaurant-approval-outbox table. This OrderApprovalOutboxMessage has the same sagaId as the OrderPaymentOutboxMessage. It has sagaStatus as PROCESSING, and outboxStatus as STARTED.


## Concurrency Strategy
- Optimistic locking via JPA version field. 
- Unique constraint to prevent duplicate payments. This is achieved by creating unique index. See init-schema.sql of all outbox tables, there is always a unique index
- In oder-service/OrdePaymentSage, we have both optimistic locking and unique constraint. Optimistic locking is used for concurrent get and update of an existing database record.
- In payment-service, we dont use saga because this is at the end of chain, we only to need process the payment for order Pending or Cancelled. So in payment-service, the main logic happens at PaymentRequestHelper.persistPayment, we only need to check if we have already processed the payment for the same sagaId (in case we assume order-service failed to process the paymentResponse), if yes, we just publish the message again, if no, we process the payment and then save OrderOutboxMessage, we dont actually get the OrderOutboxMessage or update it. So in this case, we dont actually have optimistic locking. Therefore, we only have unique constraint to prevent duplicate payments (via unique index)

### Optimistic Locking in Concurrent Saga Processing
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
See OrderPaymentSaga.java, in this case we dont want to save the 
1. First thread completes: Second thread finds no processable record
2. Threads arrive simultaneously:
   - One thread successfully updates, other threads wait for the first thread to commit (see Postgres Isolation Level Discussion)
   - Other thread gets rolled back due to version mismatch
3. Additional protection via unique constraints on certain tables

#### Postgres Isolation Level Discussion

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



# Outbox Lifecycle

### **Step 2: Initiating the Order Process**

1. **Send Order Request**:
   - Using Postman, send a POST request to `order-service` with a JSON payload for the wallet input.
   - The order is initialized in `OrderDomainServiceImpl` and saved via `CreateHelper`.
   - A `PaymentOutboxMessage` is created and stored in the database.

2. **Return Response to User**:
   - `CreateOrderResponse` with a tracking ID is returned to the user, indicating the order's initial creation.

### **Step 3: Payment Processing via Kafka**

1. **Outbox Scheduler**:
   - The `PaymentOutboxScheduler` identifies the `PaymentOutboxMessage`, then it will send the outbox message to message bus via `OrderPaymentEventKafkaPublisher`.
   - `OrderPaymentEventKafkaPublisher` then sends the eventPayload (extracted from outbox message) to Kafka, and the status is updated to `completed` upon Kafka's acknowledgment.

2. **Payment-Service Receives Event**:
   - `payment-service` listens to the payment request event.
   - The payment is processed, and a corresponding `OrderOutboxMessage` is created in the payments database.
   - Scheduler publishes the outbox message to Kafka via `PaymentEventKafkaPublisher`.
   - Upon Kafka acknowledgement, the outbox message status is updated to `completed`.

### **Step 4: Update Order Status to Paid**

1. **Order-Service Receives Payment Confirmation**:
   - `order-service` listens for the payment response.
   - Once received, it processes the successful payment and updates the order status to `paid`.
   - An outbox message is created for the next step, which is order approval, in order_approval_outbox table.
   - Update the outbox message in payment_outbox table to saga status "PROCESSING"

### **Step 5: Approval by Restaurant-Service**

1. **Send Approval Request**:
   - `RestaurantApprovalOutboxScheduler` in `order-service` identifies the approval message.
   - This message is published to Kafka using `OrderApprovalEventKafkaPublisher`.
   - Upon successful Kafka acknowledgement, the message status is updated to `completed`.

2. **Approval by Restaurant-Service**:
   - `restaurant-service` listens for the approval request and approves the order.
   - An outbox message is generated and stored in order_outbox of restaurant schema.
   - Scheduler picks up the message and send the outbox message to message bus via `RestaurantApprovalEventsKafkaPublisher`
   - `RestaurantApprovalEventsKafkaPublisher` then sends the eventPayload (extracted from outbox message) to Kafka, and the status is updated to `completed` upon Kafka's acknowledgment.

### **Step 6: Final Approval in Order-Service**

1. **Receive Approval Response**:
   - `order-service` listens for the restaurant approval response.
   - Once received, the order is marked as `approved`, and the saga is considered completed.

2. **Update Databases**:
   - The relevant outbox messages in `order-service`, `payment-service`, and `restaurant-service` databases are marked as `completed`.
   - The order database reflects the final `approved` status of the order.

### **Step 7: Archival and Clean-Up**

1. **Outbox Table Clean-Up**:
   - Completed outbox messages are cleaned up by dedicated schedulers to ensure performance.
   - Optionally, in production, these messages may be archived for future analysis.

2. **Final Checks**:
   - Confirm the following in respective tables:
     - `order-service`: Order status is `approved`.
     - `payment-service`: Credit entry reflects customer’s balance, with history showing the transaction.
     - `restaurant-service`: Order approval table shows the order as approved.
   - Kafka topics can be inspected (e.g., using `kafkacat`) to view messages and confirm IDs.

---

This cycle efficiently processes an order from creation through payment, approval, and finalization across distributed services using Kafka for messaging and outbox patterns to ensure reliability and consistency.
