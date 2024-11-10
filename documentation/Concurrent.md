## Kafka Listener for Debezium Connector
- **Goal**: Modify the Kafka message listener in the payment service to consume messages published by a Debezium connector.
- **Context**: This system receives high volumes of orders, particularly under scenarios where a single customer ID is used by a company for multiple simultaneous orders. This creates a need to ensure the listener handles concurrency effectively and processes messages without errors.

## Load Testing with JMeter
- **Scenario**: The speaker will simulate a load where a single customer ID creates up to 3000 order requests. This will allow testing the system's behavior when multiple users simultaneously submit requests using the same ID.
- **Configuration**: 
  - **Instances**: Use three instances each of the order, payment, and restaurant services.
  - **Kafka Partitions**: Utilize three Kafka partitions to enable parallel processing across the instances. This setup is expected to test the system's concurrency limits, as each partition supports one consumer running concurrently.

## Potential Concurrency Challenges for Kafka Listener
- **Concurrency Issue**: Given multiple orders with the same customer ID, the system risks encountering concurrent update issues.
  - **Problem**: If a batch of requests fails partway through, retrying them could lead to unique constraint exceptions (because of saving the same data multiple times, see Kafka -> The Unique Constraint Exception).
  - **Solution**: avoiding processing items in bulk (lists) and instead handling items individually in the listener to minimize reprocessing overhead.

## Potential Concurrency Inconsistency in Payment Service Business Logic
- **Implementation Concern**: In the `persistPayment` method, concurrency issues may arise when two threads work on the same credit data.
- **Credit Entry and History Check**: There’s a business requirement to validate that credit entry and credit history are consistent.
  - **Inconsistency Example**: One thread might fetch a credit entry, and before it retrieves the credit history, another thread could update the history. This would lead to inconsistencies and failed business validations.

- **Solutions for Concurrency Control**
  - Pessimistic Locking: Ensures that no other threads can access certain data until the lock is released, preventing concurrent data modifications.
    - **Implementation**: Lock on `getCreditEntry` to block other threads until the transaction completes.
    - **Impact**: Prevents concurrency issues but serializes data access, potentially reducing parallel processing in the payment service.
- While optimistic locking allows more parallelism, it’s slower in high-conflict scenarios due to frequent rollbacks.

### Thoughts on payment service kafka listener
- With the implementation of pessimistic locking for credit entry and history check, a thread may experience delays while waiting for the lock to release, potentially leading to a timeout exception. Alternatively, with optimistic locking for credit entry and history check, repeated optimistic lock exceptions could occur, which might cause the Kafka offset commit timeout to expire. This expiration means that the same message could be re-delivered multiple times to the listener, as Kafka will continue attempting to process it until it successfully commits -> In either way, the message will be processed multiple times.
- Solution: In KafkaListener, instead of processing a list of messages, we will process one message at a time, so that kafka can commit the offset after successfully processing each message.
  - Make changes also to change batch configuration properties in payment-service appilication.yml. Becasue we have KafkaConsumerConfig Configuration class, it will pick up the batchListener and concurrencyLevel from the application.yml file of each service to set up the kafka listener container factory.