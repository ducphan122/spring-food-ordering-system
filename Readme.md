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