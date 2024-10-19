/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain.exception;

import com.spring.food.ordering.system.domain.exception.DomainException;

public class OrderDomainException extends DomainException {

    public OrderDomainException(String message) {
        super(message);
    }

    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
