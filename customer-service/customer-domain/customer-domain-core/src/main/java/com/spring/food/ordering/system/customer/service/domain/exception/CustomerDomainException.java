package com.spring.food.ordering.system.customer.service.domain.exception;

import com.spring.food.ordering.system.domain.exception.DomainException;

public class CustomerDomainException extends DomainException {

    public CustomerDomainException(String message) {
        super(message);
    }
}
