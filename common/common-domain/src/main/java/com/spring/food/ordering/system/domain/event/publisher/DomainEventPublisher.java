/* (C)2024 */
package com.spring.food.ordering.system.domain.event.publisher;

import com.spring.food.ordering.system.domain.event.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {

    void publish(T domainEvent);
}
