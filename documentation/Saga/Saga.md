# Saga Pattern
**Definition**: A pattern for managing distributed transactions across multiple services, where each transaction is broken down into a sequence of local transactions.


**Key Components**:
- Chain of local transactions
- Compensation (rollback) mechanism for failures
- Choreography approach using events (in this case, Kafka)

**Example Flow 1**:
1. The **Order Service** acts as the coordinator for the SAGA flow, initiating the process.
2. It starts by sending an **Order Created** event to the **Payment Service**.
3. The Order Service’s local database marks the order as **Pending**.
4. Upon receiving a **Payment Completed** event from the Payment Service, the Order Service updates the order status to **Paid** in its database.
5. It then sends an **Order Paid** event to the **Restaurant Service** to request order approval.
6. When the **Order Service** receives an **Order Approved** event from the Restaurant Service, it updates the order status to **Approved** in its database.
7. The **Approved** state is then returned to clients when they query the order through the **GET** endpoint.
8. After receiving the approved state, the client can trigger the next steps, such as starting the **Delivery Process**.

**Example Flow 2**:
1. The **Order Service** acts as the coordinator for the SAGA flow, initiating the process.
2. It starts by sending an **Order Created** event to the **Payment Service**.
3. The Order Service’s local database marks the order as **Pending**.
4. Upon receiving a **Payment Completed** event from the Payment Service, the Order Service updates the order status to **Paid** in its database.
5. It then sends an **Order Paid** event to the **Restaurant Service** to request order approval.
6. When the **Order Service** receives a **Order Rejected** event from the Restaurant Service (via order-messaging/listener), it will call reject method in _application-service/ports/input/message/listener/restaurantapproval/_ , this reject method will call rollback defined in OrderApprovalSaga, which is in _domain-application-service_, the returned value from this rollback is a OrderCancelledEvent. This OrderCancelledEvent will be fired as PaymentRequestAvroModel via _order-messaging/publisher/kafka/CreateOrderKafkaMessagePublisher.java_. The order state will be right now "CANCELLING". 
7. The Payment Service will listen to _payment-request_ topic, and it will consume the message (PaymentRequestAvroModel) via PaymentRequestKafkaListener(in messaging module) -> paymentRequestMessageListener(in application-service) -> this paymentRequestMessageListener will get the required Entities (via interact with repository), then it will give all these entities to domain-service to run business logic, then it persist the data to database. Then take the returned event from domain-service and fire it. In this case is a PaymentCancelledEvent.
8. The PaymentCancelledEvent will be consumed by _order-messaging/listener/kafka/OrderResponseKafkaListener_ . It will then call rollback method in OrderPaymentSaga, the returned value from this rollback is a EmptyEvent. Because nothing to do here, the flow is finished, the order state is now "CANCELLED".

**Implementation Requirements**:
- Each SAGA step must implement:
  - `process()` - Execute the local transaction
  - `rollback()` - Compensate/undo if failure occurs later

This pattern is particularly useful in microservices architectures where maintaining data consistency across services is crucial

## Implementation
- Create SagaStep interface in common-domain
- In order-application-service we implement the SagaStep interface because order-service is the coordinator of the saga flow

**PaymentResponseMessageListenerImpl will call OrderPaymentSaga** (Steps 4 of Example Flow)
- In order-messaging we have a kafka listener to listen to the topic payment-response, and based on the payment response, it will call the methods defined in interface ports/input/message/listener/payment/. This interface is then implemented in order-application-service via PaymentResponseMessageListenerImpl
- PaymentResponseMessageListenerImpl will call the methods in OrderPaymentSaga, which implements SagaStep interface. OrderPaymentSaga will interact with domain-service to run business logic (domain service will give back the domain events), then it will use repository to persist the data to database. After that it return the domain event
- PaymentResponseMessageListenerImpl will then use the returned domain event to fire the event that restaurant-service is subscribed to, so that restaurant-service can perform its business logic

**OrderApprovalMessageListenerImpl will call OrderApprovalSaga** (Steps 5 and 6 of Example Flow)