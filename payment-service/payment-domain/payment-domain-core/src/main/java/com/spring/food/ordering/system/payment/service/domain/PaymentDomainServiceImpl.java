package com.spring.food.ordering.system.payment.service.domain;

import static com.spring.food.ordering.system.domain.DomainConstants.UTC;

import com.spring.food.ordering.system.domain.valueobject.CustomerId;
import com.spring.food.ordering.system.domain.valueobject.Money;
import com.spring.food.ordering.system.domain.valueobject.PaymentStatus;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.spring.food.ordering.system.payment.service.domain.entity.Payment;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.spring.food.ordering.system.payment.service.domain.valueobject.CreditHistoryId;
import com.spring.food.ordering.system.payment.service.domain.valueobject.TransactionType;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public void validatePaymentNotCompleted(Optional<Payment> existingPayment, List<String> failureMessages) {
        if (existingPayment.isPresent() && existingPayment.get().getPaymentStatus() == PaymentStatus.COMPLETED) {
            failureMessages.add("Payment is already completed for order: "
                    + existingPayment.get().getOrderId().getValue());
        }
    }

    @Override
    public PaymentEvent validateAndInitiatePayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.DEBIT);
        validateCreditHistory(creditEntry, creditHistories, failureMessages);

        if (failureMessages.isEmpty()) {
            log.info(
                    "Payment is initiated for order id: {}",
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)));
        } else {
            log.info(
                    "Payment initiation is failed for order id: {}",
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
        }
    }

    @Override
    public PaymentEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.CREDIT);

        if (failureMessages.isEmpty()) {
            log.info(
                    "Payment is cancelled for order id: {}",
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)));
        } else {
            log.info(
                    "Payment cancellation is failed for order id: {}",
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
        }
    }

    @Override
    public void validateAndInitiateTopUp(
            CreditEntry creditEntry,
            Money topUpAmount,
            List<CreditHistory> creditHistories,
            List<String> failureMessages) {

        log.info(
                "Validating top-up amount for customer: {}",
                creditEntry.getCustomerId().getValue());

        if (topUpAmount == null || !topUpAmount.isGreaterThanZero()) {
            log.error(
                    "Top-up amount must be greater than zero for customer: {}",
                    creditEntry.getCustomerId().getValue());
            failureMessages.add("Top-up amount must be greater than zero!");
            return;
        }

        creditEntry.addCreditAmount(topUpAmount);
        updateCreditHistoryForTopUp(creditEntry.getCustomerId(), topUpAmount, creditHistories);
        validateCreditHistory(creditEntry, creditHistories, failureMessages);

        if (failureMessages.isEmpty()) {
            log.info(
                    "Successfully topped up credit for customer: {}",
                    creditEntry.getCustomerId().getValue());
        }
    }

    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        log.info(
                "@@@@@Total credit amount for customer id: {} is {}",
                payment.getCustomerId().getValue(),
                creditEntry.getTotalCreditAmount().getAmount());
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error(
                    "Customer with id: {} doesn't have enough credit for payment!",
                    payment.getCustomerId().getValue());

            failureMessages.add("Customer with id=" + payment.getCustomerId().getValue()
                    + " doesn't have enough credit for payment!");
        }
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void updateCreditHistory(
            Payment payment, List<CreditHistory> creditHistories, TransactionType transactionType) {
        creditHistories.add(CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                .customerId(payment.getCustomerId())
                .amount(payment.getPrice())
                .transactionType(transactionType)
                .build());
    }

    private void validateCreditHistory(
            CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages) {
        Money totalCreditHistory = getTotalHistoryAmount(creditHistories, TransactionType.CREDIT);
        Money totalDebitHistory = getTotalHistoryAmount(creditHistories, TransactionType.DEBIT);

        if (totalDebitHistory.isGreaterThan(totalCreditHistory)) {
            log.error(
                    "Customer with id: {} doesn't have enough credit according to credit history",
                    creditEntry.getCustomerId().getValue());
            failureMessages.add(
                    "Customer with id=" + creditEntry.getCustomerId().getValue()
                            + " doesn't have enough credit according to credit history!");
        }

        if (!creditEntry.getTotalCreditAmount().equals(totalCreditHistory.subtract(totalDebitHistory))) {
            log.error(
                    "Credit history total is not equal to current credit for customer id: {}!",
                    creditEntry.getCustomerId().getValue());
            failureMessages.add("Credit history total is not equal to current credit for customer id: {}"
                    + creditEntry.getCustomerId().getValue() + "!");
        }
    }

    private Money getTotalHistoryAmount(List<CreditHistory> creditHistories, TransactionType transactionType) {
        return creditHistories.stream()
                .filter(creditHistory -> transactionType == creditHistory.getTransactionType())
                .map(CreditHistory::getAmount)
                .reduce(Money.ZERO, Money::add);
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }

    private void updateCreditHistoryForTopUp(CustomerId customerId, Money amount, List<CreditHistory> creditHistories) {
        creditHistories.add(CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                .customerId(customerId)
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .build());
    }
}
