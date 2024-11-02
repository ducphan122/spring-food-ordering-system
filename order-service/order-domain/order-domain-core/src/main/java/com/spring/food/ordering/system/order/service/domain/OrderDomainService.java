/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain;

import com.spring.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.spring.food.ordering.system.order.service.domain.entity.Order;
import com.spring.food.ordering.system.order.service.domain.entity.Restaurant;
import com.spring.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.spring.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.spring.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import java.util.List;

public interface OrderDomainService {

    OrderCreatedEvent validateAndInitiateOrder(
            Order order,
            Restaurant restaurant,
            DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher);

    OrderPaidEvent payOrder(Order order, DomainEventPublisher<OrderPaidEvent> orderPaidEventDomainEventPublisher);

    void approveOrder(Order order);

    OrderCancelledEvent cancelOrderPayment(
            Order order,
            List<String> failureMessages,
            DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher);

    void cancelOrder(Order order, List<String> failureMessages);
}
