
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