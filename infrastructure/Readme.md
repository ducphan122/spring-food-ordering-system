## Docker-compose
For now, we are using docker compose to run the kafka cluster. In the future, we will use kubernetes and cp helm charts.
Command:
- docker-compose -f common.yml -f zookeeper.yml up
  - then open terminal in the docker container and run command to check if zookeeper is running: echo ruok | nc localhost 2181
- docker-compose -f common.yml -f kafka_cluster.yml up
- docker-compose -f common.yml -f init_kafka.yml up (run only when kafka topics are not created or you want to recreate them)
- docker-compose -f common.yml -f kafka-ui.yml up
Steps:
- after running all dockers, open localhost:9000 ->  add cluster with name spring-food-ordering-system, cluster Zookeeper hosts zookeeper:2181
- open localhost:8080 -> create spring-food-ordering-system cluster -> add bootstrap servers: kafka-broker-1:9092,kafka-broker-2:9092,kafka-broker-3:9092.


Note:
- Create volumes folder: zookeeper/data and zookeeper/transactions, kafka/broker-1, kafka/broker-2, kafka/broker-3 first because if we let docker compose create it, it fails to write into it because of permissions.

## Kafka

### Kafka Model
- we are using avro to serialize and deserialize the messages.
- the avro schema is defined in the kafka-model/src/main/resources/avro/
- the avro schema is used to generate the java classes that will be used to serialize and deserialize the messages.
- the java classes are generated using the maven plugin avro-maven-plugin, with sourceDirectory and outputDirectory. To generate the java classes, run the command: mvn clean install (at kafka-model folder or at root).