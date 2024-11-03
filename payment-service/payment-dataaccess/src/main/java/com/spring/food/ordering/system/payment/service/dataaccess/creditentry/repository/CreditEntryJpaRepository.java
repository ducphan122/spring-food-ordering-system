package com.spring.food.ordering.system.payment.service.dataaccess.creditentry.repository;

import com.spring.food.ordering.system.payment.service.dataaccess.creditentry.entity.CreditEntryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditEntryJpaRepository extends JpaRepository<CreditEntryEntity, UUID> {

    Optional<CreditEntryEntity> findByCustomerId(UUID customerId);
}
