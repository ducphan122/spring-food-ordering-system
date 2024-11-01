package com.spring.food.ordering.system.restaurant.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.spring.food.ordering.system")
public class RestaurantServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(RestaurantServiceApplication.class, args);
  }
}