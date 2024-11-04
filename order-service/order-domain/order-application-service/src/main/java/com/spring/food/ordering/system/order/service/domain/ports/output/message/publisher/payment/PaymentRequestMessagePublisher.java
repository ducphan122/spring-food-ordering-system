package com.spring.food.ordering.system.order.service.domain.ports.output.message.publisher.payment;

import com.spring.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.spring.food.ordering.system.outbox.OutboxStatus;
import java.util.function.BiConsumer;

public interface PaymentRequestMessagePublisher {

    void publish(
            OrderPaymentOutboxMessage orderPaymentOutboxMessage,
            BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback);
}
