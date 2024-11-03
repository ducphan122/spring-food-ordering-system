package com.spring.food.ordering.system.payment.service.dataaccess.credithistory.adapter;

import com.spring.food.ordering.system.domain.valueobject.CustomerId;
import com.spring.food.ordering.system.payment.service.dataaccess.credithistory.entity.CreditHistoryEntity;
import com.spring.food.ordering.system.payment.service.dataaccess.credithistory.mapper.CreditHistoryDataAccessMapper;
import com.spring.food.ordering.system.payment.service.dataaccess.credithistory.repository.CreditHistoryJpaRepository;
import com.spring.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CreditHistoryRepositoryImpl implements CreditHistoryRepository {

    private final CreditHistoryJpaRepository creditHistoryJpaRepository;
    private final CreditHistoryDataAccessMapper creditHistoryDataAccessMapper;

    public CreditHistoryRepositoryImpl(
            CreditHistoryJpaRepository creditHistoryJpaRepository,
            CreditHistoryDataAccessMapper creditHistoryDataAccessMapper) {
        this.creditHistoryJpaRepository = creditHistoryJpaRepository;
        this.creditHistoryDataAccessMapper = creditHistoryDataAccessMapper;
    }

    @Override
    public CreditHistory save(CreditHistory creditHistory) {
        return creditHistoryDataAccessMapper.creditHistoryEntityToCreditHistory(creditHistoryJpaRepository.save(
                creditHistoryDataAccessMapper.creditHistoryToCreditHistoryEntity(creditHistory)));
    }

    @Override
    public Optional<List<CreditHistory>> findByCustomerId(CustomerId customerId) {
        Optional<List<CreditHistoryEntity>> creditHistory =
                creditHistoryJpaRepository.findByCustomerId(customerId.getValue());
        return creditHistory.map(creditHistoryList -> creditHistoryList.stream()
                .map(creditHistoryDataAccessMapper::creditHistoryEntityToCreditHistory)
                .collect(Collectors.toList()));
    }
}