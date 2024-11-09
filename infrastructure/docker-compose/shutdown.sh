#!/bin/bash

echo "Delete debezium connectors"
curl --location --request DELETE 'localhost:8083/connectors/order-payment-connector'
curl --location --request DELETE 'localhost:8083/connectors/order-restaurant-connector'
curl --location --request DELETE 'localhost:8083/connectors/payment-order-connector'
curl --location --request DELETE 'localhost:8083/connectors/restaurant-order-connector'

echo "Shutdown zookeeper"
docker-compose -f common.yml -f zookeeper.yml down

sleep 5
echo "Shutdown kafka cluster"
docker-compose -f common.yml -f kafka_cluster.yml down

sleep 5
echo "Shutdown init kafka"
docker-compose -f common.yml -f init_kafka.yml down

sleep 5
echo "Shutdown Debezium Postgres"
docker-compose -f common.yml -f postgres_debezium.yml down

sleep 5
echo "Deleting Kafka and Zookeeper volumes"
yes | rm -r ./volumes/kafka/*
yes | rm -r ./volumes/zookeeper/*
yes | rm -r ./volumes/postgres/*

echo "Shutdown services completed"
read -p "Press any key to exit..."