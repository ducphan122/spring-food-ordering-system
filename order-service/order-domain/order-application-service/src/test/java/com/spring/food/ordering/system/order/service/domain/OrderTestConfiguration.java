package com.spring.food.ordering.system.order.service.domain;

import com.spring.food.ordering.system.order.service.domain.ports.output.repository.*;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.spring.food.ordering.system")
public class OrderTestConfiguration {

    /**
     * The OrderDomainService is a core domain service, it will not contain any
     * dependencies (like Spring), so we need to inject a bean here to test
     * business logic
     */
    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }

    /**
     * The 3 repositories below are implemented by adapters, so we need to mock them
     * to test business logic
     */
    @Bean
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }

    @Bean
    public RestaurantRepository restaurantRepository() {
        return Mockito.mock(RestaurantRepository.class);
    }

    @Bean
    public OrderRepository orderRepository() {
        return Mockito.mock(OrderRepository.class);
    }

    @Bean
    public PaymentOutboxRepository paymentOutboxRepository() {
        return Mockito.mock(PaymentOutboxRepository.class);
    }

    @Bean
    public ApprovalOutboxRepository approvalOutboxRepository() {
        return Mockito.mock(ApprovalOutboxRepository.class);
    }
}
