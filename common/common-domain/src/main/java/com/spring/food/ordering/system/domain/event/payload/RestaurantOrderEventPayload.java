package com.spring.food.ordering.system.domain.event.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantOrderEventPayload {

    @JsonProperty
    private String orderId;

    @JsonProperty
    private String restaurantId;

    @JsonProperty
    private ZonedDateTime createdAt;

    @JsonProperty
    private String orderApprovalStatus;

    @JsonProperty
    private List<String> failureMessages;
}
