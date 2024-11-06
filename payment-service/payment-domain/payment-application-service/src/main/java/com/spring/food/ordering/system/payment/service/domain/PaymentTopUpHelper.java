package com.spring.food.ordering.system.payment.service.domain;

import com.spring.food.ordering.system.domain.valueobject.CustomerId;
import com.spring.food.ordering.system.domain.valueobject.Money;
import com.spring.food.ordering.system.payment.service.domain.dto.TopUpRequest;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.spring.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PaymentTopUpHelper {

    private final PaymentDomainService paymentDomainService;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    public PaymentTopUpHelper(
            PaymentDomainService paymentDomainService,
            CreditEntryRepository creditEntryRepository,
            CreditHistoryRepository creditHistoryRepository) {
        this.paymentDomainService = paymentDomainService;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
    }

    @Transactional
    public void persistTopUp(TopUpRequest topUpRequest) {
        CustomerId customerId = new CustomerId(UUID.fromString(topUpRequest.getCustomerId()));
        Money topUpAmount = new Money(topUpRequest.getAmount());

        CreditEntry creditEntry = getCreditEntry(customerId);
        List<CreditHistory> creditHistories = getCreditHistory(customerId);
        List<String> failureMessages = new ArrayList<>();

        paymentDomainService.validateAndInitiateTopUp(creditEntry, topUpAmount, creditHistories, failureMessages);

        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
            log.info("Successfully processed top-up for customer: {}", customerId.getValue());
        } else {
            log.error("Failed to process top-up for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException(String.join(", ", failureMessages));
        }
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
}
