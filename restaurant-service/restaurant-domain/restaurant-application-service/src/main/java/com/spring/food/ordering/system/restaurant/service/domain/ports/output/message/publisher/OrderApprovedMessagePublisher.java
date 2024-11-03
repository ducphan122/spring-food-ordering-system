package com.spring.food.ordering.system.restaurant.service.domain.ports.output.message.publisher;

import com.spring.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.spring.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;

public interface OrderApprovedMessagePublisher extends DomainEventPublisher<OrderApprovedEvent> {}
