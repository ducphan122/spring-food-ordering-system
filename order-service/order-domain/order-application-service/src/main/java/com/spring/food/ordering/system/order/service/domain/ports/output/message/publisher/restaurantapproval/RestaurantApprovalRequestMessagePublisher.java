package com.spring.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval;

import com.spring.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.spring.food.ordering.system.outbox.OutboxStatus;
import java.util.function.BiConsumer;

public interface RestaurantApprovalRequestMessagePublisher {

    void publish(
            OrderApprovalOutboxMessage orderApprovalOutboxMessage,
            BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback);
}
