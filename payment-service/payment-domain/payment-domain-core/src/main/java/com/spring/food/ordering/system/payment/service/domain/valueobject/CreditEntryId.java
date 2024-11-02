package com.spring.food.ordering.system.payment.service.domain.valueobject;

import com.spring.food.ordering.system.domain.valueobject.BaseId;
import java.util.UUID;

public class CreditEntryId extends BaseId<UUID> {
    public CreditEntryId(UUID value) {
        super(value);
    }
}
