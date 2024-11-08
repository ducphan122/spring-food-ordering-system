- (CQRS): Separate read and write operations. Better performance on read part using right technology for reading, and preventing conflicts with update commands. Scale each part separately.
- Eventual Consistency means that the system will eventually reach a consistent state: any updates made on a distributed system will eventually be reflected to all nodes
- Once the write operation is persisted, an event is stored in event-store.
  - Event-store is used to store data as immutable events.
- Events can be replayed multiple times based on requirements to create different type of query store. See [Event-Store and Replayability in CQRS](#benefits-of-multiple-query-stores)

## Move from materialized view cqrs
- Current implementation is using materialized view. For example, in order-service, we are getting customer data via the materialized view customer schema. This cross schema query is not efficient.
- Now, instead of using materialized view, we will use cqrs to store customer data in order-service.

## Example
- In this project, after saving the customer object in customer local database, we will create and send the customer created event into kafka to hold the event
- Then in order-service, we will consume the event from kafka and create cqrs store for the customer data


## Event-Store and Replayability in CQRS

Once events are placed in an **event-store** (a specialized data store for persisting events), the system gains significant flexibility in how these events can be processed and consumed by various services. The event-store essentially functions as a central repository of all events that have occurred in the system, and this history can be accessed or replayed as needed. 
- Replaying events in Kafka means creating a new consumer group. It will read the events from start by default.

### The Power of Replayability

With events stored separately, they can be **replayed multiple times** to generate different **query stores** or views of data. These query stores are optimized for specific use cases or subsystems, which can benefit from different data formats or even entirely different data structures. For instance, you might need:

1. **Real-Time Search Indexes**: You could replay events to populate an index in a search engine like **Elasticsearch**. This allows users to perform full-text search on entities like customer profiles or orders in real time, leveraging Elasticsearch’s search capabilities without affecting the main transactional database.

2. **Analytics and Reporting Databases**: Events can also be replayed into an **analytics database** specifically designed for reporting or data analysis. This could be a data warehouse or OLAP (Online Analytical Processing) database, where you can run complex aggregations and calculations on large datasets without impacting the performance of the primary transactional systems.

3. **Custom Views for Specific Services**: Separate query stores can be created for different services, allowing each service to have a tailored view of the data it needs. For example, an **Order Service** might only need customer data relevant to order processing. By replaying only the necessary customer events into a dedicated query store, the Order Service gains efficient access to the data it needs without querying the primary database.

### Benefits of Multiple Query Stores

By creating multiple query stores from the same set of events, CQRS and event-sourcing offer flexibility in data usage:

- **Scalability**: Since query stores are separate from each other and from the write store, they can be scaled independently, based on the demand of each specific use case. For example, an analytics database can handle heavy read loads without impacting the performance of other systems.

- **Flexibility in Data Models**: Each query store can have its own schema or structure, optimized for a particular read pattern or service. This allows the system to avoid the complexity and inefficiency of a “one-size-fits-all” data model.

- **Adaptability to Changing Requirements**: As business needs evolve, you can create new query stores by replaying events without modifying the event-store or altering the write path. This is especially helpful for implementing new features, analytics, or views without reworking the foundational system.

### Infinite Replay and Event Consumption

The ability to replay and consume events in various ways gives the system “infinite chances” to tailor query stores to emerging requirements. For example:

- **Historical Analysis**: Replay events from the event-store to build historical data for analytics or machine learning models, helping to understand past trends or customer behavior over time.

- **Debugging and Auditing**: Event replay enables a precise reconstruction of the state of the system at any point in time, facilitating debugging and auditing. You can trace exactly what happened during a specific transaction or failure.

- **Real-Time Processing**: With technologies like **Kafka** (as mentioned in the example), you can not only persist events but also stream them in real time to various consumers, triggering immediate updates in query stores or pushing data to real-time dashboards.

### Key Takeaway

By storing events separately and replaying them as needed, CQRS enables highly customized, efficient, and scalable data retrieval. This separation of write and read models allows developers to leverage **different data stores** optimized for specific read or write patterns, enhancing the flexibility and performance of distributed systems. This, in turn, provides a foundation for creating robust, scalable, and adaptable systems with the CQRS pattern.