## Pom.xml

- use dependency management to manage the versions of the dependencies of child modules in the parent pom.xml

## order-domain 
### order-domain-core

- This module contains the core domain logic for the order service
- It contains the domain core (most independent), 

### order-application-service

- It is the application service layer that implements the interfaces defined in the domain layer and it should have a dependency to order-domain-core
- It is not order-application module because it does not contain any main method and it is not an entry point to the clients

## order-dataaccess

- It contains the data access logic for the order service
- It will have adapters for the output ports of domain layer. So it will implement the interfaces defined in the domain layer and it should have a dependency to order-application-service

## order-messaging
- it also has dependency to order-application-service because it needs to implement the messaging interfaces from the domain layer

## order-application
- It is the entry point from the clients and it should pass request to domain layer -> It has dependency to order-application-service

## order-container
- It has all dependencies of all modules
- It is used to create single runnable jar files and run as microservice
- It also contains the docker file to build docker image to use it in cloud deployment



