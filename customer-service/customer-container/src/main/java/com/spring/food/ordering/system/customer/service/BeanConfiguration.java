package com.spring.food.ordering.system.customer.service;

import com.spring.food.ordering.system.customer.service.domain.CustomerDomainService;
import com.spring.food.ordering.system.customer.service.domain.CustomerDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CustomerDomainService customerDomainService() {
        return new CustomerDomainServiceImpl();
    }
}
