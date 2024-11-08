package com.spring.food.ordering.system.payment.service.messaging.mapper;

import com.spring.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.spring.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.spring.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.spring.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.spring.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.spring.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentMessagingDataMapper {

    public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
        return PaymentRequest.builder()
                .id(paymentRequestAvroModel.getId())
                .sagaId(paymentRequestAvroModel.getSagaId())
                .customerId(paymentRequestAvroModel.getCustomerId())
                .orderId(paymentRequestAvroModel.getOrderId())
                .price(paymentRequestAvroModel.getPrice())
                .createdAt(paymentRequestAvroModel.getCreatedAt())
                .paymentOrderStatus(PaymentOrderStatus.valueOf(
                        paymentRequestAvroModel.getPaymentOrderStatus().name()))
                .build();
    }

    public PaymentResponseAvroModel orderEventPayloadToPaymentResponseAvroModel(
            String sagaId, OrderEventPayload orderEventPayload) {
        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setPaymentId(orderEventPayload.getPaymentId())
                .setCustomerId(orderEventPayload.getCustomerId())
                .setOrderId(orderEventPayload.getOrderId())
                .setPrice(orderEventPayload.getPrice())
                .setCreatedAt(orderEventPayload.getCreatedAt().toInstant())
                .setPaymentStatus(PaymentStatus.valueOf(orderEventPayload.getPaymentStatus()))
                .setFailureMessages(orderEventPayload.getFailureMessages())
                .build();
    }
}
