## Pom.xml

- use dependency management to manage the versions of the dependencies of child modules in the parent pom.xml

## order-domain 
### order-domain-core

- This module contains the core domain logic for the order service
- It contains the domain core (most independent), 

### order-application-service

- It is the application service layer that implements the interfaces defined in the domain layer and it should have a dependency to order-domain-core
- It is not order-application module because it does not contain any main method and it is not an entry point to the clients

#### OrderServiceConfigData
- It is used to get the configuration data for the order service from the application.yml file
- Centralizes Kafka topic configuration
- Makes topic names configurable without code changes
- Enables different topic names for different environments (dev, test, prod)
Used in the order service to know which topics to publish/subscribe to when communicating with payment and restaurant services

#### Ports and Adapters

Input Ports:
- OrderApplicationService is the input port, clients will use it (exp: Postman). -> OrderApplicationServiceImpl 
- PaymentResponseMessageListener and RestaurantApprovalResponseMessageListener are the input ports - message listeners for payment and restaurant approvals. They are triggered by domain events. Payment or Restaurant service will raise the domain events and they will trigger the message listeners input port in the order service

Output Ports:
- OrderCreatedPaymentRequestMessagePublisher, OrderCancelledPaymentRequestMessagePublisher, OrderPaidRestaurantRequestMessagePublisher are the message publisher output ports to publish 3 types of events in order domain logic

#### Repositories examples
**OrderRepository (domain -> application-service -> output port -> repository)**
- Uses domain objects (`Order`, `OrderId`)
- Defines WHAT can be done

**OrderJpaRepository (order-dataaccess -> order -> repository)**
- Uses JPA entities and Spring
- Defines HOW data is store in database

**OrderJpaRepositoryImpl (order-dataaccess -> order -> adapter)**
- It is the adapter between JPA and domain layer
- Implements OrderRepository and use OrderJpaRepository
- Handles the mapping between domain entities and JPA entities

Flow: `Domain → output port (OrderRepository) → adapter (OrderJpaRepositoryImpl) -> repository (OrderJpaRepository) -> Entity database`

## order-dataaccess

- It contains the data access logic for the order service
- It will have adapters for the output ports of domain layer. So it will implement the interfaces defined in the domain layer and it should have a dependency to order-application-service

## order-messaging
- it also has dependency to order-application-service because it needs to implement the messaging interfaces from the domain layer
- Has mapper, publisher and listener
- Mapper is used to map the domain events to the avro models

### Listener
- Listener is used to listen to the events from the Kafka topic
- Each listener will implement KafkaConsumer interface, and injects the message listener implementation (located in order-application-service, it is the implementation of the input port message listener interface)

### Publisher
- Publisher is used to publish the events to the Kafka topic
- Each publisher will implement the interface from the domain-application-service/ports/output/message/publisher/ 
In the publisher:
1. We use a mapper to convert domain events to Avro models
2. We use a Kafka producer to publish these events to Kafka topics
3. When sending messages, we include a callback function that handles the publishing result:
   - The callback is of type `BiConsumer<SendResult<String, T>, Throwable>`
   - If publishing succeeds, the callback receives a `SendResult` object containing metadata about the sent message
   - If publishing fails, the callback receives a `Throwable` containing the error details
   - For now, it is just logging the message, but in future we can use it to manage the saga pattern and outbox pattern

#### Callback's Role in Saga and Outbox Patterns
- **Saga Pattern**: The callback helps manage distributed transactions by:
  - Detecting failures in message publishing
  - Triggering compensation transactions (rollbacks) when needed
  - Example: If publishing an OrderPaidEvent fails, we can rollback the order status to maintain consistency

- **Outbox Pattern**: The callback helps ensure reliable message delivery by:
  - Confirming successful message publication before marking outbox entries as processed
  - Retrying failed messages or marking them for retry
  - Maintaining the outbox table state based on publishing results

## order-application
- It is the entry point from the clients and it should pass request to domain layer -> It has dependency to order-application-service

## order-container
- It has all dependencies of all modules
- It is used to create single runnable jar files and run as microservice
- It also contains the docker file to build docker image to use it in cloud deployment

### BeanConfiguration
- We are registering the OrderDomainService as a Spring bean by defining @Bean-annotated methods in a configuration class that return a new OrderDomainServiceImpl instance. This approach allows the domain core module to remain free of Spring dependencies while enabling the OrderDomainService to be injected into the order application service module. When the Spring Boot application starts, it recognizes and registers the domain service as a Spring bean, facilitating its use within the application without directly coupling the domain core to Spring.

## order-dataaccess
- In JPA, when an entity like OrderItemEntity has multiple columns forming its primary key (composite key), we need a separate class (OrderItemEntityId) annotated with @IdClass to represent this composite key. This class must implement Serializable to allow JPA to convert the primary key object into bytes for caching, session management, and distributed systems. The composite key class needs to mirror the primary key fields of the main entity and must provide proper equals() and hashCode() methods for entity comparison and identification. This approach is particularly useful when one of the primary key fields is also used in relationships (like @ManyToOne).

## FAQ

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



