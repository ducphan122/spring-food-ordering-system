# Debezium Change Data Capture

- Introduce **Change Data Capture (CDC)** for implementing the outbox pattern, replacing the pull-based outbox table approach.
- **Change Data Capture (CDC)**: Reads database transaction logs (e.g., Write Ahead Log in Postgres) to capture changes in near real-time.
  - Uses a **push-based method**, reducing overhead and increasing timeliness. Provides near real-time data by listening the Transaction Logs and send it Kafka immediately.
  - Logs capture every **insert, update, and delete** for recovery and consistency.
  - Replace the **pull-based method** outbox table approach with **push-based** CDC.
- **Debezium** platform:
  - Provides **Kafka connectors** to push data into Kafka.
  - Offers **source connectors** for databases like Postgres.
  - Utilizes **Debezium/connect Docker image** to add CDC functionality to Kafka cluster in `docker_compose`.
- **Process**:
  - Register Debezium’s Postgres source connector to listen to transaction logs.
  - Debezium captures changes and streams data to Kafka in real-time.
- **Outbox pattern enhancement**:
  - Replace Spring Boot schedulers with Debezium Kafka connectors for push-based CDC.
  - Provides a near real-time solution by listening to transaction logs instead of polling.
- Next steps: Update Kafka cluster `docker_compose` file with Debezium connector.

## Installation
mvnrepository.com/artifact/com.google.guava/guava/12.0 
mvnrepository.com/artifact/org.apache.avro/avro/1.11.1
https://packages.confluent.io/maven/io/confluent/
We need to download the required jars and add them to the Debezium connector container as volumes.
- See `infrastructure/docker-compose/kafka_cluster.yml`

## Implementation
- Debezium is run with a Docker image, and the Debezium connector container definition is added to the Kafka cluster compose file.
- The updated compose file includes the Debezium Docker image version 2.2, an environment file with the Debezium version, and uses port 8083.
- Three mandatory Kafka topics are created for the Debezium connector: config, offset, and status.
- The bootstrap service of the Kafka cluster is used to connect to Kafka from the connector, and the debug log level is set.
- The key and value converter schema registry URL is set to the schema registry URL, and the depends_on property is used to start the Kafka cluster before the Debezium connector.
- The networks property allows the connector to communicate with Kafka using a common network, and volumes are defined to map required schema registry JAR files into the running connector container.
- The required JAR files are downloaded from the Confluent Maven Repository, and additional JAR files (Guava, Avro, and Kafka schema converter) are added to run Debezium Connect 2.2 with Kafka version 7.2.5.
- By default, Debezium uses JSON serialization, but it is configured to use Avro, which is a more compact binary format with better performance.
- The schema will have two objects: "before" and "after", representing the data before and after a change, respectively.
- The "after" object can be used to get the newly inserted record with the outbox pattern.
- There is an option to use Single Message Transform (SMT) in Debezium, which can transform the JSON object to a simplified version.
- The Docker compose file is run, and the Debezium connector container is started on port 8083/connectors
- The Postgres source connector is used to connect the 4 outbox tables that are used in this project: payment_outbox and restaurant_approval_outbox in order schema, order_outbox in payment schema and order_outbox in restaurant schema


## Configuring Postgresql for Change Data Capture

1. **Objective: Setting Up Debezium PostgreSQL Source Connectors**
   - The main goal is to set up Debezium PostgreSQL source connectors using Debezium's connector Post API with JSON representations of two connectors.
   - While a pre-configured Debezium PostgreSQL Docker image is available, this setup uses a local PostgreSQL instance to demonstrate required configuration changes for CDC.

2. **PostgreSQL Configuration Updates for CDC**
   - CDC in PostgreSQL requires logical replication, which captures changes in the database transaction log, known as the Write-Ahead Log (WAL).
   - Logical decoding translates changes in the WAL to a format readable by external applications, such as Debezium connectors.

3. **Enabling Logical Replication and WAL Configuration**
   - Locate PostgreSQL's configuration file using the query `SHOW config_file;`.
   - Essential configurations include:
      - **WAL Level**: Set to `logical` to enable logical replication.
      - **Max WAL Senders**: Set to `4`, which allows four concurrent processes to send WAL changes.
      - **Max Replication Slots**: Set to `4`, matching the number of connectors and outbox tables.
      - **log_min_error_statement**: Set to `fatal`
      - **listen_addresses**: Set to `*` to allow connections from any host.
      - **max_collections**: Set to `200` because each spring service by default use 10 connections.
   - These settings are essential for PostgreSQL to broadcast changes in the WAL to connected consumers.

4. **Replication Slot Setup**
   - Replication slots manage logical decoding streams and ensure each connector receives all relevant data changes.
   - A replication slot ensures that the WAL keeps unconsumed data changes, preventing data loss but requiring careful storage monitoring.
   - Query available slots with `SELECT * FROM pg_catalog.pg_replication_slots;` and remove unused ones with `pg_drop_replication_slot`.

5. **Choosing an Output Plugin**
   - Debezium previously required WAL decoding plugins (e.g., Wal2Json or decoderbufs) to translate WAL content.
   - With PostgreSQL 10+, Debezium can use the built-in `pgoutput` plugin, eliminating the need for additional plugins.
   - The `pgoutput` plugin converts changes into JSON format and feeds them into the replication slot for Debezium to consume.

6. **Configuring PostgreSQL for Multiple Connections**
   - Increase `max_connections` to 200, exceeding the default 100 connections to support:
      - Multiple application instances.
      - Connection pooling (e.g., with Hikari for Spring applications).
      - Additional connections from Debezium and pgAdmin.
   - Without this adjustment, PostgreSQL may throw “too many clients already” errors.

7. **Authentication Configuration (PG HBA Config)**
   - Modify the `pg_hba.conf` file (access the path with `SHOW hba_file;`), adding entries to permit trusted access for applications.
   - This adjustment streamlines client authentication for database replication purposes.

8. **Restarting PostgreSQL**
   - services.msc -> PostgreSQL -> Restart
   - use `SHOW max_collections` to check if the configuration is successful (it should be 200)
   - All of the postgress changes can be used directly via a docker container postgres_debezium.yml (no need to change locally)

9. **Using a Push Model with Debezium for CDC**
   - Instead of a traditional pull model where applications query PostgreSQL at intervals, CDC with Debezium uses a push model.
   - Postgres notifies Debezium of data changes in real time, optimizing the outbox pattern implementation with low latency.
10. **Handling Duplicates and Failures in Kafka**
   - Debezium’s Kafka integration operates with an “at least once” delivery guarantee, potentially sending duplicate messages.
   - Deduplication is handled on the consumer side, ensuring accuracy and resilience in the Kafka pipeline despite network interruptions or retries.

11. **Monitoring Replication Slots and WAL**
   - Regular monitoring of replication slots is crucial to prevent storage issues if slots are not actively consumed.
   - Unconsumed changes in WAL can cause PostgreSQL storage to fill up if slots are left unmonitored.
   - Use SELECT * FROM pg_replication_slots; to check all slots
   - At first, when we create connector (via POST request), the slot is not yet created if the table which the connectors subscribes to is empty. When we insert data into the table, the slot is then automatically created. Debezium connector will then be able to read from the slot and send data to kafka.

## Debezium PostgreSQL Source Connector Setup

1. **Debezium PostgreSQL Source Connector Setup**:  
   - The instructor explains the process of setting up Debezium PostgreSQL source connectors to capture database changes and replicate them to Kafka.
   - The connector configuration will be done using Postman on `localhost:8083` (Debezium connector port), with a `POST` request to `/connectors`.
   - The JSON configuration is provided in the lecture's resources section, including required Postgres config changes.

2. **Connector Configuration Parameters**:  
   - **Connector Name**: Set as `order-payment-connector`.
   - **Connector Class**: Uses `io.debezium.connector.postgresql.PostgresConnector`.
   - **tasks.max**: Set to `1`, since Debezium source connectors cannot parallelize across tasks due to sequential writes in Postgres' Write-Ahead Log (WAL).
   - **Table Inclusion**: Specifies `table.include.list` as `order.payment_outbox` to monitor only the `payment_outbox` table in the `order` schema.
   - **Host Config**: Uses `host.docker.internal` as the host to connect Docker containers to Postgres on the host machine.

3. **Connector Properties for Topic Configuration**:  
   - **Topic Prefix**: Set as `debezium`, a mandatory property in Debezium Connect 2.2.
   - **Tombstones.on.delete**: Set to `false` to ignore tombstone events (delete markers) from Kafka topics.
   - **Slot Configuration**: Each connector has a unique `slot.name` (e.g., `order_payments_outbox_slot`) for replicating changes from the transaction log, ensuring unique slots across connectors.
   - **Plugin**: Uses the default Postgres replication plugin `pgoutput`, sufficient for Postgres versions 10 and above.

4. **Multi-Connector Setup**:  
   - Due to four distinct outbox tables, four unique connectors will be created, each configured with a different slot and `table.include.list`.
   - Connectors will be named according to their corresponding outbox tables: `order-payment-connector`, `restaurant-approval-outbox`, `payment-order-outbox`, and `restaurant-order-outbox`.

5. **Connector and Kafka UI Initialization**:  
   - **Kafka Environment Setup**: Starts Zookeeper, Kafka cluster, Postgres and Debezium connector containers using Docker Compose.
   - **Kafka Topics Creation**: Topics for each outbox table (e.g., `debezium.order.payment_outbox`) are auto-created upon data insertion into corresponding tables, with names derived from the schema and table name.

6. **Schema Management with Avro**:  
   - In oder to use avro serialization, we need to get the avro schema from the schema registry and save it as a .avsc file in the `kafka-model/resources/avro` folder, then mvn install to generate the java classes.
   - See [Readme.md] Installation (till step 4). Then open dbeaver and insert using debezium_insert.sql. This will insert data into the outbox tables. As we have started the debezium connector, the data will be captured and sent to kafka. Then the avro schema will be generated and accessible via the Schema Registry API. Use this to copy the schema as save it as .avsc file. If you have done it, skip this step.
   - **Schema Retrieval and Storage**: Retrieves and saves the Avro schemas for each table as `.avsc` files in the `kafka-model avro` folder. Ex: Make GET request to http://localhost:8081/subjects/debezium.order.payment_outbox-value/versions/latest/schema. We need to make 4 requests to get all 4 schemas. Use localhost:9000 to get the topic name 

7. **Manual Topic Creation (Optional)**:  
   - While topics and schemas are auto-created, for production, manual creation of topics and schemas offers better control.
   - Updates `init-kafka` compose file to predefine four topics, each with three partitions, while keeping previous topics for performance comparison between Change Data Capture (CDC) and table-polling implementations.

8. **Maven Project Update**:  
   - Running `mvn clean install` with the Avro Maven plugin compiles Avro schema files into Java classes.
   - Make sure avro-maven-plugin version 1.11.1, if version is higher, it will throw error Can't redefine: io.debezium.connector.postgresql.Source
   - Each schema generates unique `Envelope` and `Value` classes, while `Source` and `Block` classes are shared among schemas.

9. **Kafka Listener Update (Next Steps)**:  
   - The Kafka listeners will be updated to leverage the new Avro classes for consuming outbox topics in the CDC implementation.


## Difference between kafka-debezium-connector and postgres_debezium

1. **kafka-debezium-connector** (in kafka_cluster.yml):
- This is the Debezium Connect service that acts as a connector framework
- It's responsible for:
  - Running the actual CDC (Change Data Capture) operations
  - Converting and streaming changes to Kafka
  - Managing the connection to Schema Registry
  - Handling the Avro conversion of messages
- Think of it as the "worker" that monitors database changes and sends them to Kafka

2. **postgres** (in postgres_debezium.yml):
- This is the actual PostgreSQL database service
- It's configured specifically for Debezium, kafka-debezium-connector will read from WAL
- This is the source database that Debezium will monitor for changes

The relationship between them:
- The PostgreSQL instance is the data source
- The Debezium connector connects to this PostgreSQL instance, reads its transaction logs
- When changes occur in PostgreSQL, the Debezium connector captures these changes and streams them to Kafka

Think of it as:
- PostgreSQL = The source of data changes
- Debezium Connector = The service that watches and streams those changes to Kafka

Postgres notifies Debezium of data changes through a process called Write-Ahead Logging (WAL) and logical replication. Here's how it works:

1. **Write-Ahead Log (WAL)**:
```plaintext
Database Write → WAL Entry → Disk Write

[Transaction] → [WAL Buffer] → [WAL Files on Disk]
                                     ↓
                              [Debezium reads]
```
   - Every change (INSERT/UPDATE/DELETE) is first written to the WAL
   - This happens before the actual database changes (hence "Write-Ahead")

2. **Logical Replication**:
   - PostgreSQL's `pgoutput` plugin converts WAL entries into logical changes
   - Each connector gets a dedicated replication slot (`slot.name`)
   - The slot keeps track of what changes have been read

3. **Change Capture**:
   - Debezium continuously reads from its assigned replication slot
   - Changes are immediately pushed to Debezium as they occur
   - Debezium then forwards these changes to Kafka

4. **Real-time Nature**:
   - No polling is needed
   - Changes are pushed to Debezium as soon as they hit the WAL
   - This results in near real-time replication

This is why it's called a "push-based" approach, as opposed to the previous "pull-based" approach where applications had to repeatedly query the database for changes.

## Implementation changes

1. Scheduler and Publisher Cleanup
- **Delete Scheduling Implementations**: We remove all outbox schedulers that are polling outbox tables in each service, as well as the publishers tied to these schedulers in respective messaging modules. 
- We also delete the interface ports/output/message/publisher because producing and publish outbox messages are now handled by the Debezium CDC.

2. Kafka Configuration Changes for Debezium Topics
- Update **Kafka topic names** in `application.yml` of each service to reflect Debezium’s naming pattern: `debezium.<schema>.<table>`.
  - Example:
    - Payment request topic: `debezium.order.payment_outbox`
    - Payment response topic: `debezium.payment.order_outbox`
- Delete the outbox-scheduler configuration in `application.yml` of each service.

3. Listener Updates and Debezium Envelope Class
- **Replace Generic Types**: In `ResponseKafkaListener` of messaging/listerner/kafka, change the type from `ResponseAvroModel` to `Envelope`
- Because we are using DDD, most of the changing should happen only at messaging module
- **Extract Operation and Data**: Modify the listener to:
  - Check if the `before` object is null and the operation (`op`) is equal to `'c'` (for create operations).


### Handling Insert, Update, and Delete Events
- For CDC events, modify code to accept only `'c'` (create) operations to prevent processing `update` or `delete` events.
- Note the benefits of using Debezium: CDC can read from the database transaction logs, enabling you to insert and immediately delete the outbox records since the data is already available to Debezium.

### Mapping Data with Updated Listener Logic
- **Logging**: Update log statements to print the number of create operations.
- **Payload Extraction**: Retrieve the `after` object from `Envelope`, representing the inserted data.
- **OrderEventPayload**: Use `OrderEventPayload` from `payment domain` module to map necessary fields, renaming it to `PaymentOrderEventPayload` to align with the payment-to-order data flow.
- **PaymentResponse Mapping**: Update `OrderMessagingDataMapper` to map data from `PaymentOrderEventPayload` to `PaymentResponse`, including fields like ID, saga ID, and timestamps.

### Final Cleanup
- **Data Mapper Cleanup**: Remove unused methods from `OrderMessagingDataMapper`.
- **Method Renaming**: Rename methods to reflect the removal of publish operations.
- **Testing Cleanup**: Remove publisher mocks from `OrderTestConfiguration` after the publisher code deletion.
  
### Similar Changes for Other Services
- Plan similar CDC and listener updates for **restaurant-service** and **payment-service** in the following lectures.

---

This comprehensive update integrates CDC with Debezium into the existing microservices architecture, removing redundant polling mechanisms and configuring Kafka listeners to efficiently handle database change events directly. The lecture concludes with considerations for handling failure scenarios and ensuring robust message processing.