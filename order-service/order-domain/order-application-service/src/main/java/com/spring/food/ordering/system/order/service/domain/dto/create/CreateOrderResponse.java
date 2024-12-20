/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain.dto.create;

import com.spring.food.ordering.system.domain.valueobject.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateOrderResponse {
    @NotNull
    private final UUID orderTrackingId;

    @NotNull
    private final OrderStatus orderStatus;

    @NotNull
    private final String message;
}
