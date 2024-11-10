#!/bin/bash

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