package com.spring.food.ordering.system.payment.service.domain.ports.input.service;

import com.spring.food.ordering.system.payment.service.domain.dto.TopUpRequest;

public interface PaymentApplicationService {
    void topUp(TopUpRequest topUpRequest);
}
