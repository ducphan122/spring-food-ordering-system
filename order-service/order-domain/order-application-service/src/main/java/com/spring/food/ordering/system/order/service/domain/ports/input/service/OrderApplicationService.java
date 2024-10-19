/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain.ports.input.service;

import com.spring.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.spring.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.spring.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.spring.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import jakarta.validation.Valid;

public interface OrderApplicationService {

    CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);

    TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);
}
