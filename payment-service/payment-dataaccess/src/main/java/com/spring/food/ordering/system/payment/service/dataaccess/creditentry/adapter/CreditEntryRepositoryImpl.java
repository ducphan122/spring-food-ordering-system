package com.spring.food.ordering.system.payment.service.dataaccess.creditentry.adapter;

import com.spring.food.ordering.system.domain.valueobject.CustomerId;
import com.spring.food.ordering.system.payment.service.dataaccess.creditentry.mapper.CreditEntryDataAccessMapper;
import com.spring.food.ordering.system.payment.service.dataaccess.creditentry.repository.CreditEntryJpaRepository;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CreditEntryRepositoryImpl implements CreditEntryRepository {

    private final CreditEntryJpaRepository creditEntryJpaRepository;
    private final CreditEntryDataAccessMapper creditEntryDataAccessMapper;

    private final EntityManager entityManager;

    public CreditEntryRepositoryImpl(
            CreditEntryJpaRepository creditEntryJpaRepository,
            CreditEntryDataAccessMapper creditEntryDataAccessMapper,
            EntityManager entityManager) {
        this.creditEntryJpaRepository = creditEntryJpaRepository;
        this.creditEntryDataAccessMapper = creditEntryDataAccessMapper;
        this.entityManager = entityManager;
    }

    @Override
    public CreditEntry save(CreditEntry creditEntry) {
        return creditEntryDataAccessMapper.creditEntryEntityToCreditEntry(
                creditEntryJpaRepository.save(creditEntryDataAccessMapper.creditEntryToCreditEntryEntity(creditEntry)));
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {
        return creditEntryJpaRepository
                .findByCustomerId(customerId.getValue())
                .map(creditEntryDataAccessMapper::creditEntryEntityToCreditEntry);
    }

    @Override
    public void detach(CustomerId customerId) {
        entityManager.detach(
                creditEntryJpaRepository.findByCustomerId(customerId.getValue()).orElseThrow());
    }
}
