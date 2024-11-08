package com.spring.food.ordering.system.payment.service.domain;

import com.spring.food.ordering.system.domain.valueobject.Money;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.spring.food.ordering.system.payment.service.domain.entity.Payment;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentEvent;
import java.util.List;
import java.util.Optional;

public interface PaymentDomainService {

    PaymentEvent validateAndInitiatePayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages);

    PaymentEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages);

    void validateAndInitiateTopUp(
            CreditEntry creditEntry,
            Money topUpAmount,
            List<CreditHistory> creditHistories,
            List<String> failureMessages);

    void validatePaymentNotCompleted(Optional<Payment> existingPayment, List<String> failureMessages);
}
