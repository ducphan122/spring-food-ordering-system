package com.spring.food.ordering.system.payment.service.domain.valueobject;

import com.spring.food.ordering.system.domain.valueobject.BaseId;
import java.util.UUID;

public class PaymentId extends BaseId<UUID> {
    public PaymentId(UUID value) {
        super(value);
    }
}
