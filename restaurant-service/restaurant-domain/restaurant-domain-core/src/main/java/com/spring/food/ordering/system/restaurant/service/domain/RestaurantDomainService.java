package com.spring.food.ordering.system.restaurant.service.domain;

import com.spring.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.spring.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.spring.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.spring.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.spring.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import java.util.List;

public interface RestaurantDomainService {

    OrderApprovalEvent validateOrder(
            Restaurant restaurant,
            List<String> failureMessages,
            DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher,
            DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher);
}
