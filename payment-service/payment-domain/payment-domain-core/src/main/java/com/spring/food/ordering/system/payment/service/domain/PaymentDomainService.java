package com.spring.food.ordering.system.payment.service.domain;

import com.spring.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.spring.food.ordering.system.domain.valueobject.Money;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.spring.food.ordering.system.payment.service.domain.entity.Payment;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import java.util.List;

public interface PaymentDomainService {

    PaymentEvent validateAndInitiatePayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages,
            DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher,
            DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher);

    PaymentEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages,
            DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher,
            DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher);

    void validateAndInitiateTopUp(
            CreditEntry creditEntry,
            Money topUpAmount,
            List<CreditHistory> creditHistories,
            List<String> failureMessages);
}
