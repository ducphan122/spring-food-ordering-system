package com.spring.food.ordering.system.customer.service.domain;

import com.spring.food.ordering.system.customer.service.domain.create.CreateCustomerCommand;
import com.spring.food.ordering.system.customer.service.domain.create.CreateCustomerResponse;
import com.spring.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.spring.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.spring.food.ordering.system.customer.service.domain.ports.input.service.CustomerApplicationService;
import com.spring.food.ordering.system.customer.service.domain.ports.output.message.publisher.CustomerMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
class CustomerApplicationServiceImpl implements CustomerApplicationService {

    private final CustomerCreateCommandHandler customerCreateCommandHandler;

    private final CustomerDataMapper customerDataMapper;

    private final CustomerMessagePublisher customerMessagePublisher;

    public CustomerApplicationServiceImpl(
            CustomerCreateCommandHandler customerCreateCommandHandler,
            CustomerDataMapper customerDataMapper,
            CustomerMessagePublisher customerMessagePublisher) {
        this.customerCreateCommandHandler = customerCreateCommandHandler;
        this.customerDataMapper = customerDataMapper;
        this.customerMessagePublisher = customerMessagePublisher;
    }

    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerCommand createCustomerCommand) {
        CustomerCreatedEvent customerCreatedEvent = customerCreateCommandHandler.createCustomer(createCustomerCommand);
        // we keep it simple here to focus on cqrs. For strong consistency between local database operations and data
        // publishing operation, outbox pattern should be used here. Because we dont know if the message will be
        // published successfully or not.
        customerMessagePublisher.publish(customerCreatedEvent);
        return customerDataMapper.customerToCreateCustomerResponse(
                customerCreatedEvent.getCustomer(), "Customer saved successfully!");
    }
}
