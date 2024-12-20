package com.spring.food.ordering.system.customer.service.domain;

import com.spring.food.ordering.system.customer.service.domain.entity.Customer;
import com.spring.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerDomainServiceImpl implements CustomerDomainService {

    public CustomerCreatedEvent validateAndInitiateCustomer(Customer customer) {
        // Any Business logic required to run for a customer creation
        log.info("Customer with id: {} is initiated", customer.getId().getValue());
        return new CustomerCreatedEvent(customer, ZonedDateTime.now(ZoneId.of("UTC")));
    }
}
