package com.spring.food.ordering.system.payment.service.dataaccess.payment.adapter;

import com.spring.food.ordering.system.domain.valueobject.PaymentStatus;
import com.spring.food.ordering.system.payment.service.dataaccess.payment.mapper.PaymentDataAccessMapper;
import com.spring.food.ordering.system.payment.service.dataaccess.payment.repository.PaymentJpaRepository;
import com.spring.food.ordering.system.payment.service.domain.entity.Payment;
import com.spring.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    public PaymentRepositoryImpl(
            PaymentJpaRepository paymentJpaRepository, PaymentDataAccessMapper paymentDataAccessMapper) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.paymentDataAccessMapper = paymentDataAccessMapper;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentDataAccessMapper.paymentEntityToPayment(
                paymentJpaRepository.save(paymentDataAccessMapper.paymentToPaymentEntity(payment)));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId).map(paymentDataAccessMapper::paymentEntityToPayment);
    }

    @Override
    public Optional<Payment> findByOrderIdAndStatus(UUID orderId, PaymentStatus status) {
        return paymentJpaRepository
                .findByOrderIdAndStatus(orderId, status)
                .map(paymentDataAccessMapper::paymentEntityToPayment);
    }
}
