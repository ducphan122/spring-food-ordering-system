/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain;

import com.spring.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.spring.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.spring.food.ordering.system.order.service.domain.entity.Order;
import com.spring.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.spring.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.spring.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.spring.food.ordering.system.order.service.domain.valueobject.TrackingId;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderTrackCommandHandler {
    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    public OrderTrackCommandHandler(OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        Optional<Order> orderResult =
                orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));
        if (orderResult.isEmpty()) {
            log.warn("Could not find order with tracking id: {}", trackOrderQuery.getOrderTrackingId());
            throw new OrderNotFoundException(
                    "Could not find order with tracking id: " + trackOrderQuery.getOrderTrackingId());
        }
        return orderDataMapper.orderToTrackOrderResponse(orderResult.get());
    }
}
