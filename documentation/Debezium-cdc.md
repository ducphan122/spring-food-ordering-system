# Debezium Change Data Capture

- Introduce **Change Data Capture (CDC)** for implementing the outbox pattern, replacing the pull-based outbox table approach.
- **Change Data Capture (CDC)**: Reads database transaction logs (e.g., Write Ahead Log in Postgres) to capture changes in near real-time.
  - Uses a **push-based method**, reducing overhead and increasing timeliness. Provides near real-time data by listening the Transaction Logs and send it Kafka immediately.
  - Logs capture every **insert, update, and delete** for recovery and consistency.
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

- See `infrastructure/docker-compose/kafka_cluster.yml`

```
avro-1.11.1.jar
common-config-7.2.5.jar
common-utils-7.2.5.jar
guava-12.0.jar
kafka-avro-serializer-7.2.5.jar
kafka-connect-avro-converter-7.2.5.jar
kafka-connect-avro-data-7.2.5.jar
kafka-schema-converter-7.2.5.jar
kafka-schema-registry-7.2.5.jar
kafka-schema-registry-client-7.2.5.jar
kafka-schema-serializer-7.2.5.jar
```

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