/* (C)2024 */
package com.spring.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval;

import com.spring.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;

public interface RestaurantApprovalResponseMessageListener {

    void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);

    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);
}
