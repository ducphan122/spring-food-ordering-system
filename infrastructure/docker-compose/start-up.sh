#!/bin/bash
echo "Deleting Kafka and Zookeeper volumes"

yes | rm -r ./volumes/kafka/*

yes | rm -r ./volumes/zookeeper/*

echo "Creating required Kafka and Zookeeper volumes folders"
mkdir -p ./volumes/{kafka/broker-{1,2,3},zookeeper/{data,transaction}}
chmod 755 ./volumes/kafka/broker-{1,2,3} ./volumes/zookeeper/{data,transaction} 
echo "Volumes created"

echo "Starting Debezium Postgres"
docker-compose -f common.yml -f postgres_debezium.yml up -d
# check debezium postgres health
postgresCheckResult=$(docker exec postgres pg_isready | grep 'accepting connections')

while [[ ! $postgresCheckResult == "/var/run/postgresql:5432 - accepting connections" ]]; do
  >&2 echo "Debezium Postgres is not running yet!"
  sleep 2
  postgresCheckResult=$(docker exec postgres pg_isready | grep 'accepting connections')
done

echo "Starting Zookeeper"

# start zookeeper
docker-compose -f common.yml -f zookeeper.yml up -d

# check zookeeper health
NCAT="/c/Program Files (x86)/Nmap/ncat.exe"
zookeeperCheckResult=$(echo ruok | "$NCAT" localhost 2181)

while [[ ! $zookeeperCheckResult == "imok" ]]; do
  >&2 echo "Zookeeper is not running yet!"
  sleep 2
  zookeeperCheckResult=$(echo ruok | "$NCAT" localhost 2181)
done

echo "Starting Kafka cluster"

# start kafka
docker-compose -f common.yml -f kafka_cluster.yml up -d

# check kafka health
kafkaCheckResult=$(docker run --network=host edenhill/kcat:1.7.1 -b localhost:19092 -L | grep '3 brokers:')

while [[ ! $kafkaCheckResult == " 3 brokers:" ]]; do
  >&2 echo "Kafka cluster is not running yet!"
  sleep 2
  kafkaCheckResult=$(docker run --network=host edenhill/kcat:1.7.1 -b localhost:19092 -L | grep '3 brokers:')
done

echo "Creating Kafka topics"

# start kafka init
docker-compose -f common.yml -f init_kafka.yml up -d

# check topics in kafka
kafkaTopicCheckResult=$(docker run --network=host edenhill/kcat:1.7.1 -b localhost:19092 -L | grep 'debezium.restaurant.order_outbox')

while [[ $kafkaTopicCheckResult == "" ]]; do
  >&2 echo "Kafka topics are not created yet!"
  sleep 3
  kafkaTopicCheckResult=$(docker run --network=host edenhill/kcat:1.7.1 -b localhost:19092 -L | grep 'debezium.restaurant.order_outbox')
done

# check debezium
servicesCheckResult=$(curl -s -o /dev/null -I -w "%{http_code}" http://localhost:8083)

echo "Result status code:" "$curlResult"

while [[ ! $servicesCheckResult == "200" ]]; do
  >&2 echo "Debezium is not running yet!"
  sleep 2
  servicesCheckResult=$(curl -s -o /dev/null -I -w "%{http_code}" http://localhost:8083)
done

echo "Creating debezium connectors"

curl --location --request POST 'localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data-raw '{
  "name": "order-payment-connector",
  "config": {
      "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
      "tasks.max": "1",
      "database.hostname": "host.docker.internal",
      "database.port": "5433",
      "database.user": "postgres",
      "database.password": "postgres",
      "database.dbname" : "postgres",
      "database.server.name": "PostgreSQL-15",
      "table.include.list": "order.payment_outbox",
      "topic.prefix": "debezium",
      "tombstones.on.delete" : "false",
      "slot.name" : "order_payment_outbox_slot",
      "plugin.name": "pgoutput",
      "auto.create.topics.enable": false,
      "auto.register.schemas": false
      }
 }'

curl --location --request POST 'localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data-raw '{
  "name": "order-restaurant-connector",
  "config": {
      "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
      "tasks.max": "1",
      "database.hostname": "host.docker.internal",
      "database.port": "5433",
      "database.user": "postgres",
      "database.password": "postgres",
      "database.dbname" : "postgres",
      "database.server.name": "PostgreSQL-15",
      "table.include.list": "order.restaurant_approval_outbox",
      "topic.prefix": "debezium",
      "tombstones.on.delete" : "false",
      "slot.name" : "order_restaurant_approval_outbox_slot",
      "plugin.name": "pgoutput",
      "auto.create.topics.enable": false,
      "auto.register.schemas": false
      }
 }'

curl --location --request POST 'localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data-raw '{
  "name": "payment-order-connector",
  "config": {
      "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
      "tasks.max": "1",
      "database.hostname": "host.docker.internal",
      "database.port": "5433",
      "database.user": "postgres",
      "database.password": "postgres",
      "database.dbname" : "postgres",
      "database.server.name": "PostgreSQL-15",
      "table.include.list": "payment.order_outbox",
      "topic.prefix": "debezium",
      "tombstones.on.delete" : "false",
      "slot.name" : "payment_order_outbox_slot",
      "plugin.name": "pgoutput",
      "auto.create.topics.enable": false,
      "auto.register.schemas": false
      }
 }'

curl --location --request POST 'localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data-raw '{
  "name": "restaurant-order-connector",
  "config": {
      "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
      "tasks.max": "1",
      "database.hostname": "host.docker.internal",
      "database.port": "5433",
      "database.user": "postgres",
      "database.password": "postgres",
      "database.dbname" : "postgres",
      "database.server.name": "PostgreSQL-15",
      "table.include.list": "restaurant.order_outbox",
      "topic.prefix": "debezium",
      "tombstones.on.delete" : "false",
      "slot.name" : "restaurant_order_outbox_slot",
      "plugin.name": "pgoutput",
      "auto.create.topics.enable": false,
      "auto.register.schemas": false
      }
 }'

echo "Start-up completed"
read -p "Press any key to exit..."