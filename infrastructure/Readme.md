## Docker-compose
For now, we are using docker compose to run the kafka cluster. In the future, we will use kubernetes and cp helm charts.
Command:
- docker-compose -f common.yml -f zookeeper.yml up
  - then open terminal in the docker container and run command to check if zookeeper is running: echo ruok | nc localhost 2181
- docker-compose -f common.yml -f kafka_cluster.yml up
- docker-compose -f common.yml -f init_kafka.yml up

Steps:
- after running all dockers, open localhost:9000
- add cluster with name spring-food-ordering-system, cluster Zookeeper hosts zookeeper:2181

## Kafka
