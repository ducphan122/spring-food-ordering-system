package com.spring.food.ordering.system.customer.service.domain;

import com.spring.food.ordering.system.customer.service.domain.entity.Customer;
import com.spring.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;

public interface CustomerDomainService {

    CustomerCreatedEvent validateAndInitiateCustomer(Customer customer);
}
