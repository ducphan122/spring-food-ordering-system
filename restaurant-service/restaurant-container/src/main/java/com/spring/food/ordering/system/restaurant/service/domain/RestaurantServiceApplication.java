package com.spring.food.ordering.system.restaurant.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.spring.food.ordering.system.restaurant.service.dataaccess",
        "com.spring.food.ordering.system.dataaccess" })
@EntityScan(basePackages = { "com.spring.food.ordering.system.restaurant.service.dataaccess",
        "com.spring.food.ordering.system.dataaccess" })
@SpringBootApplication(scanBasePackages = "com.spring.food.ordering.system")
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
