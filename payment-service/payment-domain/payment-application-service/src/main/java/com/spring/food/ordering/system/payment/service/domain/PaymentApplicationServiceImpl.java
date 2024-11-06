package com.spring.food.ordering.system.payment.service.domain;

import com.spring.food.ordering.system.payment.service.domain.dto.TopUpRequest;
import com.spring.food.ordering.system.payment.service.domain.ports.input.service.PaymentApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class PaymentApplicationServiceImpl implements PaymentApplicationService {

    private final PaymentTopUpHelper paymentTopUpHelper;

    public PaymentApplicationServiceImpl(PaymentTopUpHelper paymentTopUpHelper) {
        this.paymentTopUpHelper = paymentTopUpHelper;
    }

    @Override
    public void topUp(TopUpRequest topUpRequest) {
        paymentTopUpHelper.persistTopUp(topUpRequest);
    }
}
