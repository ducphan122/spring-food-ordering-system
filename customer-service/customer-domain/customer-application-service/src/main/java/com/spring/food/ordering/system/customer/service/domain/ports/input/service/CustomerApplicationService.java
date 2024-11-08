package com.spring.food.ordering.system.customer.service.domain.ports.input.service;

import com.spring.food.ordering.system.customer.service.domain.create.CreateCustomerCommand;
import com.spring.food.ordering.system.customer.service.domain.create.CreateCustomerResponse;
import jakarta.validation.Valid;

public interface CustomerApplicationService {

    CreateCustomerResponse createCustomer(@Valid CreateCustomerCommand createCustomerCommand);
}
