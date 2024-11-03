## order-domain 
### order-domain-core
### order-application-service
## order-dataaccess
## order-messaging
## order-application
## order-container
## order-dataaccess
- In JPA, when an entity like OrderItemEntity has multiple columns forming its primary key (composite key), we need a separate class (OrderItemEntityId) annotated with @IdClass to represent this composite key. This class must implement Serializable to allow JPA to convert the primary key object into bytes for caching, session management, and distributed systems. The composite key class needs to mirror the primary key fields of the main entity and must provide proper equals() and hashCode() methods for entity comparison and identification. This approach is particularly useful when one of the primary key fields is also used in relationships (like @ManyToOne).

## FAQ

### Simple Workflow
- POST /orders -> OrderController -> OrderApplicationService -> OrderApplicationServiceImpl -> OrderCreateCommandHandler -> OrderService-> OrderCreatedEvent -> OrderCreatedPaymentRequestMessagePublisher -> CreateOrderKafkaMessagePublisher -> OrderServiceConfigData.paymentRequestTopicName -> Kafka Producer -> PaymentRequestAvroModel -> OrderServiceConfigData.paymentResponseTopicName -> Kafka Consumer -> PaymentResponseMessageListener -> OrderCancelledEvent -> OrderCancelledPaymentRequestMessagePublisher -> OrderServiceConfigData.paymentRequestTopicName -> Kafka Producer -> PaymentRequestAvroModel
### Order State changing status
Order States and Transitions
- Pending
Purpose: Initial state when an order is created. Indicates that the order is awaiting payment.
Transitions:
To Paid: When payment is successful.
To Canceled: If payment fails.

- Paid
Purpose: Indicates that payment has been successfully received for the order. The order is now awaiting restaurant approval.
Transitions:
To Approved: When the restaurant approves the order.
To Canceling: If restaurant approval fails and a payment rollback is required.

- Approved
Purpose: Confirms that the restaurant has approved the order. The order can now proceed to fulfillment.

- Canceling
Purpose: Represents that a cancellation process has been initiated. This state is used when a payment rollback is necessary due to failed approval from restaurant.
Transitions:
To Canceled: After successful rollback of the payment (order service must inform payment service to rollback the payment). Using SAGA pattern for compensation.

- Canceled
Purpose: Final state indicating that the order has been canceled. This can occur either due to payment failure or after a successful cancellation process.
Transitions:
(Terminal State)

### Order Events
- OrderCreatedEvent
- OrderPaidEvent
- OrderCancelledEvent: use to rollback the payment if the restaurant did not approve the order. After setting the order status to CANCELLING, we need to fire the OrderCancelledEvent to rollback the payment.
- We dont need to fire OrderApprovedEvent because the clients will fetch the data using tracking id. If we have a client that has capable of capturing the approved event, we can fire the event that can be consumed by that client to continue delivery process. The client as an event consumer. However, in the current design we dont have any client that is capable of capturing the approved event because we just use simple http client with Postman. Moreover, because this is last step in order processing


### Event Publisher
- OrderApplicationServiceImpl call OrderCreateCommandHandler to create order. After saving order to database, OrderCreateCommandHandler will publish OrderCreatedEvent. In some cases, we can also use spring transactional event listener to automatically publish the event after transaction is committed.

### Event Listener
- PaymentResponseMessageListenerImpl and RestaurantApprovalResponseMessageListenerImpl are the event listeners for payment and restaurant approval responses. They are triggered by the domain events from other bounded contexts (payment and restaurant service). 
- Will be implemented in saga pattern



