package com.spring.food.ordering.system.payment.service.dataaccess.outbox.repository;

import com.spring.food.ordering.system.domain.valueobject.PaymentStatus;
import com.spring.food.ordering.system.outbox.OutboxStatus;
import com.spring.food.ordering.system.payment.service.dataaccess.outbox.entity.OrderOutboxEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutboxEntity, UUID> {

    Optional<List<OrderOutboxEntity>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    Optional<OrderOutboxEntity> findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(
            String type, UUID sagaId, PaymentStatus paymentStatus, OutboxStatus outboxStatus);

    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
