## order-service
- OrderItem, Order is implemented with builder pattern that allows for the step-by-step construction of complex objects. Can be created with Lombok @Builder annotation, but because it is in domain layer, we dont want any dependency on any framework or library, so we implement it manually.

### Exception handling
- For application layer, we use @ControllerAdvice to handle exceptions that are thrown from domain-core. 
- We also use a global @ControllerAdvice in common module to handle generic exceptions that are unexpected. When handling globally, log the error internally but return a generic error message for security reason (make sense because we dont even know that the error exists).
- We also use global @ControllerAdvice in common module to handle validation errors, that are thrown from application-service layer, mostly from dto or interface (example OrderApplicationService in input ports)

## Infrastructure
- run docker compose to start kafka cluster, zookeeper and create topics

## Others
- we use mvn clean install to build and install the jar file to the local maven repository
- use depgraph-maven-plugin to generate the dependency graph: mvn com.github.ferstl:depgraph-maven-plugin:aggregate -DcreateImage=true -DreduceEdges=false -Dscope=compile "-Dincludes=com.spring.food.ordering.system:*"

## common
- common-domain will have entity, value object, exception that can be used across different services
- value object is immutable, thats why we use final keyword

## Understanding domain events in DDD

- Creating domain events within the domain core—either in entities or domain services—to ensure that events are generated as part of the business logic. However, the actual firing of these events is delegated to the application service. This separation ensures that business operations are first persisted to the database before any events are triggered, preventing the possibility of firing incorrect events if persistence fails. 
- The domain has no knowledge about event publishing or event tracking. It only creates and return the events after running business logic. Application service will decide when and how to raise the events.
- By keeping repository interactions and event publishing within the application service, the domain core remains focused solely on business logic without being burdened by infrastructure concerns. 
- Additionally, while domain services are not mandatory in DDD, I prefer to use them to encapsulate interactions with multiple aggregates or complex logic, allowing the application service to handle event creation through the domain service rather than directly interacting with entities.

## Customer, Restaurant Data Architecture: From Shared Schema to Service Isolation 
This is example of customer only (restaurant is similar)
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

## How to run the project

### Installation
- make sure the database is created and running. Database name should be postgres. Use PgAdmin to check for database. Use Dbeaver to connect to database.
- run docker compose to start kafka cluster, zookeeper and create topics (in infrastructure folder)
- run the project with mvn spring-boot:run at order-container (make sure the kafka is running because the order-messaging depends on kafkaProducer Bean to send topic and @KafkaListener for consuming topic), customer-container and restaurant-container. This will populate the database with some schema and data every time it starts

### Check the topic, consumer group and consumer lag
After running the project, login to kafka-manager at http://localhost:9000/ and check the topic, consumer group and consumer lag
Use Extension kafka vscode and connect to 1 of of the kafka bootstrap server (19092, 29092, 39092) to see the topic, consumer group and consumer lag

### Read message from kafka
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



