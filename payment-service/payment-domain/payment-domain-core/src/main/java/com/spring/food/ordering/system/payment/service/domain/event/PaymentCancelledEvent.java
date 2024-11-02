package com.spring.food.ordering.system.payment.service.domain.event;

import com.spring.food.ordering.system.payment.service.domain.entity.Payment;
import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCancelledEvent extends PaymentEvent {

    public PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt) {
        super(payment, createdAt, Collections.emptyList());
    }
}
