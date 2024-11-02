/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain.event;

import com.spring.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.spring.food.ordering.system.order.service.domain.entity.Order;
import java.time.ZonedDateTime;

public class OrderCancelledEvent extends OrderEvent {

    private final DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher;

    public OrderCancelledEvent(
            Order order,
            ZonedDateTime createdAt,
            DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderCancelledEventDomainEventPublisher = orderCancelledEventDomainEventPublisher;
    }

    @Override
    public void fire() {
        orderCancelledEventDomainEventPublisher.publish(this);
    }
}
