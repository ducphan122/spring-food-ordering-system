package com.spring.food.ordering.system.order.service.domain.ports.output.repository;

import com.spring.food.ordering.system.domain.valueobject.OrderId;
import com.spring.food.ordering.system.order.service.domain.entity.Order;
import com.spring.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {

  Order save(Order order);

  Optional<Order> findById(OrderId orderId);

  Optional<Order> findByTrackingId(TrackingId trackingId);
}
