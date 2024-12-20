package com.spring.food.ordering.system.restaurant.service.dataaccess.restaurant.repository;

import com.spring.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.OrderApprovalEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderApprovalJpaRepository extends JpaRepository<OrderApprovalEntity, UUID> {}
