package com.spring.food.ordering.system.payment.service.domain.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TopUpRequest {
    private String customerId;
    private BigDecimal amount;
}
