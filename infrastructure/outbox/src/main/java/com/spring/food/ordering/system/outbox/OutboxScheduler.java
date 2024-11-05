package com.spring.food.ordering.system.outbox;

public interface OutboxScheduler {
    void processOutboxMessage();
}
