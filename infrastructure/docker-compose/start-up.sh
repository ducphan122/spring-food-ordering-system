#!/bin/bash
echo "Deleting Kafka and Zookeeper volumes"

yes | rm -r ./volumes/kafka/*
yes | rm -r ./volumes/zookeeper/*
yes | rm -r ./volumes/postgres/*

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
kafkaTopicCheckResult=$(docker run --network=host edenhill/kcat:1.7.1 -b localhost:19092 -L | grep 'customer')

while [[ $kafkaTopicCheckResult == "" ]]; do
  >&2 echo "Kafka topics are not created yet!"
  sleep 3
  kafkaTopicCheckResult=$(docker run --network=host edenhill/kcat:1.7.1 -b localhost:19092 -L | grep 'customer')
done

echo "Delete kcat containers"

docker ps -a | grep 'edenhill/kcat:1.7.1' | awk '{print $1}' | xargs -r docker rm

read -p "Start up completed. Press any key to exit..."