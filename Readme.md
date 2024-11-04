# DDD

## Domain Layer
- OrderItem, Order is implemented with builder pattern that allows for the step-by-step construction of complex objects. Can be created with Lombok @Builder annotation, but because it is in domain layer, we dont want any dependency on any framework or library, so we implement it manually.
### Domain Core
**Entity**
- Represent the core business logic and state of the application.
- State Mutability: Entities can change state through their lifecycle, should be controlled through well-defined methods
- Aggregate Root: Entities that are responsible for managing a collection of associated entities. It is crucial to define clear boundaries for your Aggregates and their Aggregate Roots. Understanding what it can and cannot contain is essential for maintaining a robust and maintainable architecture.

**ValueObject**
- Represent a value in the domain, not an entity, immutable, no identity, no behavior
- Any "modification" should create a new instance

**Event**
- Events that can be used by domain-application-service to publish message to kafka. These events are called mostly by interfaces in domain-application-service/ports/output/message/publisher. These interfaces will be implemented in messaging module.

**Exception**
- Every exception that happens in domain layer should be an instance defined in this folder. SO that the application layer (Rest Controller Advice) can handle it properly.

### Domain Application Service
**Config**
- DomainServiceConfigData -> configuration for domain service, get the configuration from application.yml with prefix @ConfigurationProperties
- Centralizes Kafka topic configuration, Makes topic names configurable without code changes
- Defined in this layer and used in other layers like domain-messaging,... to know which topics to publish/subscribe to when communicating with payment and restaurant services

**Mapper**
- Use mapper to create domain object from input dto. And create output dto from domain object

**ports/input**
- Define how external actors interact with the core, domain business logic (entry points). This represents the use cases or business operations.There are 2 types of input ports:
1. **message/listener**: define the methods that will be called when a message topic is received, this interface will also be implemented by application service. This is for internal communication between microservices. This may call domain service methods to run business logic, then raise domain events to be consumed by other microservices.
2. **service**: this interface will be implemented by application service, used mainly for entrypoint of client (rest-api or postman,...)

**ports/output**
- Define how the core communicates with external systems. 2 types of output ports:
1. **message/publisher**: define the methods that will be called to publish a message to kafka. This interface will be implemented by _messaging module_. These publishers will be used to publish domain events (which are created in domain-service). This may call domain service methods to run business logic, then raise domain events to be consumed by other microservices. 
2. **repository**: define the methods that will interact with database. This interface will be implemented by _dataaccess layer_
  - Uses domain objects (`Order`, `OrderId`)
  - Defines WHAT can be done

## Messaging Layer

- it also has dependency to domain-application-service because it needs to implement the messaging interfaces from the domain layer
- Has mapper, publisher and listener
- Mapper is used to map the domain events to the avro models, and vice versa

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

## Dataaccess layer

- It contains the data access logic for domain-service, mainly for interacting with database
- It will have adapters for the output ports of domain layer. So it will implement the interfaces defined in the domain layer and it should have a dependency to domain-application-service

**dataaccess/repository**
- Uses JPA entities and Spring
- Defines HOW data is store in database

**dataaccess/adapter**
- adapter between JPA and domain layer
- Implements the repository interface from _application-service/ports/output/repository_ and use classes defined from _dataaccess/repository_
- Handles the mapping between domain entities and JPA entities

Flow: `Domain → output port repository interface → adapter → repository → Entity database`

## Container Layer
- It has all dependencies of all modules
- It is used to create single runnable jar files and run as microservice
- It also contains the docker file to build docker image to use it in cloud deployment

### BeanConfiguration for domain-core Bean
- We are registering the DomainService as a Spring bean by defining @Bean-annotated methods in a configuration class that return a new DomainServiceImpl instance. This approach allows the domain core module to remain free of Spring dependencies while enabling the DomainService to be injected into the domain application service module. When the Spring Boot application starts, it recognizes and registers the domain service as a Spring bean, facilitating its use within the application without directly coupling the domain core to Spring.

## Application Layer
- It is the entry point from the clients and it should pass request to domain layer -> It has dependency to domain-application-service
- REST Controller

### Exception handling
- For application layer, we use @ControllerAdvice to handle exceptions that are thrown from domain-core. 
- We also use a global @ControllerAdvice in common module to handle generic exceptions that are unexpected. When handling globally, log the error internally but return a generic error message for security reason (make sense because we dont even know that the error exists).
- We also use global @ControllerAdvice in common module to handle validation errors, that are thrown from application-service layer, mostly from dto or interface (example OrderApplicationService in input ports)

## Understanding domain events in DDD

- Creating domain events within the domain core—either in entities or domain services—to ensure that events are generated as part of the business logic. However, the actual firing of these events is delegated to the application service. This separation ensures that business operations are first persisted to the database before any events are triggered, preventing the possibility of firing incorrect events if persistence fails. 
- The domain has no knowledge about event publishing or event tracking. It only creates and return the events after running business logic. Application service will decide when and how to raise the events.
- By keeping repository interactions and event publishing within the application service, the domain core remains focused solely on business logic without being burdened by infrastructure concerns. 
- Additionally, while domain services are not mandatory in DDD, I prefer to use them to encapsulate interactions with multiple aggregates or complex logic, allowing the application service to handle event creation through the domain service rather than directly interacting with entities.

# Project Structure

- infrastructure: run docker compose to start kafka cluster, zookeeper and create topics
- common: common classes that can be used across different modules
- microservices: order, customer, restaurant, payment

## How to run the project

- we use mvn clean install to build and install the jar file. You can also build each service separately by running mvn clean install at each service folder.
- use depgraph-maven-plugin to generate the dependency graph: mvn com.github.ferstl:depgraph-maven-plugin:aggregate -DcreateImage=true -DreduceEdges=false -Dscope=compile "-Dincludes=com.spring.food.ordering.system:*"
- For locally purpose. After running all docker compose. Open each service/container folder -> run command: mvn spring-boot:run (comment the build steps in pom.xml for faster build time)

### Installation
- make sure the database is created and running. Database name should be postgres. Use PgAdmin to check for database. Use Dbeaver to connect to database.
- run docker compose to start kafka cluster, zookeeper and create topics (in infrastructure folder)
- run the project with mvn spring-boot:run at order-container (make sure the kafka is running because the order-messaging depends on kafkaProducer Bean to send topic and @KafkaListener for consuming topic), customer-container and restaurant-container and payment-container. This will populate the database with some schema and data every time it starts

### Check the topic, consumer group and consumer lag
After running the project, login to kafka-manager at http://localhost:9000/ and check the topic, consumer group and consumer lag
Use Extension kafka vscode and connect to 1 of of the kafka bootstrap server (19092, 29092, 39092) to see the topic, consumer group and consumer lag

### Basic flow
- make a POST request to http://localhost:8181/orders, with body
{
  "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
  "restaurantId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45",
  "address": {
    "street": "street_1",
    "postalCode": "1000AB",
    "city": "Amsterdam"
  },
  "price": 200.00,
  "items": [
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",
      "quantity": 1,
      "price": 50.00,
      "subTotal": 50.00
    },
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",
      "quantity": 3,
      "price": 50.00,
      "subTotal": 150.00
    }
  ]
}
- Use the command to read message from kafka: docker run -it --network=host edenhill/kcat:1.7.1 -b localhost:19092 -C -t payment-request
- Alternatively, use kafka-ui at http://localhost:8080/.
**Flows**
- Client sends Order JSON via POST to Order Controller.
- Order Service validates and saves order in the database.
- Order Service publishes Payment Request to Kafka (payment-request topic).
- Payment Service consumes Payment Request, processes payment, and publishes Payment Completed to Kafka (payment-response topic).
- Order Service consumes Payment Completed, updates order status to "Paid", and publishes Order Paid to Kafka (restaurant-approval-request topic).
- Restaurant Service consumes Order Paid, approves the order, and publishes Order Approved to Kafka (restaurant-approval-response topic).
- Order Service consumes Order Approved and updates the order status to "Approved".
- Client can query the order status through the Tracking Endpoint.

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

# Outbox Pattern
## Customer, Restaurant Data Architecture: From Shared Schema to Service Isolation 
**This is example of customer only**
Current 
├── customer schema
│   ├── customer table (source)
│   └── order_customer_m_view (materialized view)
└── order schema
    └── (order related tables)

-> Oder will use materialized view from customer schema to get customer data, but this is not scalable and coupled

Future
- Update customer data through events rather than direct database access. Each service will have its own database -> achieve true "database per service"
├── Customer Service
│   └── Customer Database
├── Order Service
│   └── Order Database (including customer data table)
└── Kafka (for event sourcing)

**This is example of restaurant only**
order_restaurant_m_view is a materialized view in restaurant schema, order service will use this to get restaurant data. This materialized view will have restaurantId and productId as composite key, the data structure represents a restaurant-product relationship where:
- One restaurant can have many products
- Each row represents a unique restaurant-product combination




