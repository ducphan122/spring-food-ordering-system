package com.spring.food.ordering.system.payment.service.domain.outbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderEventPayload {
    @JsonProperty
    private String paymentId;

    @JsonProperty
    private String customerId;

    @JsonProperty
    private String orderId;

    @JsonProperty
    private BigDecimal price;

    @JsonProperty
    private ZonedDateTime createdAt;

    @JsonProperty
    private String paymentStatus;

    @JsonProperty
    private List<String> failureMessages;
}
