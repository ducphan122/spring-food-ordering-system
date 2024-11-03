package com.spring.food.ordering.system.dataaccess.restaurant.repository;

import com.spring.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.spring.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntityId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, RestaurantEntityId> {

    Optional<List<RestaurantEntity>> findByRestaurantIdAndProductIdIn(UUID restaurantId, List<UUID> productIds);
}
