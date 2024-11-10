package com.spring.food.ordering.system.payment.service.dataaccess.creditentry.repository;

import com.spring.food.ordering.system.payment.service.dataaccess.creditentry.entity.CreditEntryEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditEntryJpaRepository extends JpaRepository<CreditEntryEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CreditEntryEntity> findByCustomerId(UUID customerId);
}
