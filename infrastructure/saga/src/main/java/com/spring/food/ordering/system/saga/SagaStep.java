package com.spring.food.ordering.system.saga;

import com.spring.food.ordering.system.domain.event.DomainEvent;

public interface SagaStep<T, S extends DomainEvent, U extends DomainEvent> {
    S process(T data);

    U rollback(T data);
}
