/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain.valueobject;

import com.spring.food.ordering.system.domain.valueobject.BaseId;
import java.util.UUID;

public class TrackingId extends BaseId<UUID> {
    public TrackingId(UUID value) {
        super(value);
    }
}
