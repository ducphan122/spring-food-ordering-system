package com.spring.food.ordering.system.payment.service.domain;

import com.spring.food.ordering.system.domain.valueobject.CustomerId;
import com.spring.food.ordering.system.domain.valueobject.PaymentStatus;
import com.spring.food.ordering.system.outbox.OutboxStatus;
import com.spring.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.spring.food.ordering.system.payment.service.domain.entity.Payment;
import com.spring.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.spring.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.spring.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.spring.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.spring.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.spring.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PaymentRequestHelper {

    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final OrderOutboxHelper orderOutboxHelper;

    public PaymentRequestHelper(
            PaymentDomainService paymentDomainService,
            PaymentDataMapper paymentDataMapper,
            PaymentRepository paymentRepository,
            CreditEntryRepository creditEntryRepository,
            CreditHistoryRepository creditHistoryRepository,
            OrderOutboxHelper orderOutboxHelper) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.orderOutboxHelper = orderOutboxHelper;
    }

    @Transactional
    public boolean persistPayment(PaymentRequest paymentRequest) {
        if (isOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.COMPLETED)) {
            log.info("An outbox message with saga id: {} is already saved to database!", paymentRequest.getSagaId());
            return true;
        }

        log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent =
                paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages);

        return persistIfSucceeded(paymentRequest, payment, creditEntry, creditHistories, failureMessages, paymentEvent);
    }

    @Transactional
    public boolean persistCancelPayment(PaymentRequest paymentRequest) {
        if (isOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELLED)) {
            log.info("An outbox message with saga id: {} is already saved to database!", paymentRequest.getSagaId());
            return true;
        }

        log.info("Received payment rollback event for order id: {}", paymentRequest.getOrderId());
        Optional<Payment> paymentResponse =
                paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Payment with order id: {} could not be found!", paymentRequest.getOrderId());
            throw new PaymentNotFoundException(
                    "Payment with order id: " + paymentRequest.getOrderId() + " could not be found!");
        }
        Payment payment = paymentResponse.get();
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent =
                paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistories, failureMessages);

        return persistIfSucceeded(paymentRequest, payment, creditEntry, creditHistories, failureMessages, paymentEvent);
    }

    private boolean persistIfSucceeded(
            PaymentRequest paymentRequest,
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages,
            PaymentEvent paymentEvent) {
        boolean noOptimisticLockingConflict = true;
        // When validation fails, we need to verify if the failure was due to:
        // 1. Actual business validation failures, or
        // 2. Stale data from concurrent updates
        // the check is to help even in case failureMessages, if it is because of concurrent update, we will not do
        // anything at all.
        if (!failureMessages.isEmpty()) {
            int version = creditEntry.getVersion();
            // We detach and reload the creditEntry to get the latest version from the database,
            // avoiding any cached versions from JPA/Hibernate's first-level cache
            creditEntryRepository.detach(payment.getCustomerId());
            creditEntry = getCreditEntry(payment.getCustomerId());
            noOptimisticLockingConflict = version == creditEntry.getVersion();
        }

        if (noOptimisticLockingConflict) {
            // in persistDbObjects, these is also a check for failureMessages so it will not update the creditEntry and
            // history if there are failure messages. But the payment itself will be saved in payment table and the
            // outbox message will be saved in outbox table to signal other services to complete the saga.
            persistDbObjects(payment, creditEntry, creditHistories, failureMessages);
            orderOutboxHelper.saveOrderOutboxMessage(
                    paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
                    paymentEvent.getPayment().getPaymentStatus(),
                    OutboxStatus.STARTED,
                    UUID.fromString(paymentRequest.getSagaId()));
        }

        return noOptimisticLockingConflict;
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId);
        if (creditEntry.isEmpty()) {
            log.error("Could not find credit entry for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException(
                    "Could not find credit entry for customer: " + customerId.getValue());
        }
        return creditEntry.get();
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        Optional<List<CreditHistory>> creditHistories = creditHistoryRepository.findByCustomerId(customerId);
        if (creditHistories.isEmpty()) {
            log.error("Could not find credit history for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException(
                    "Could not find credit history for customer: " + customerId.getValue());
        }
        return creditHistories.get();
    }

    private void persistDbObjects(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages) {
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
        }
    }

    private boolean isOutboxMessageProcessedForPayment(PaymentRequest paymentRequest, PaymentStatus paymentStatus) {
        Optional<OrderOutboxMessage> orderOutboxMessage =
                orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
                        UUID.fromString(paymentRequest.getSagaId()), paymentStatus);
        if (orderOutboxMessage.isPresent()) {
            return true;
        }
        return false;
    }
}
