/* (C)2024 */
package com.spring.food.ordering.system.domain.event;

public interface DomainEvent<T> {
    void fire();
}
