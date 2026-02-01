package com.loopers.application.order;

import com.loopers.domain.order.Order;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderInfo(Long orderId, Long userId, int totalPrice, ZonedDateTime createdAt, List<OrderItemInfo> items) {
    public static OrderInfo from(Order order) {
        List<OrderItemInfo> items = order.getItems().stream()
            .map(OrderItemInfo::from)
            .toList();
        return new OrderInfo(
            order.getId(),
            order.getUserId(),
            order.getTotalPrice(),
            order.getCreatedAt(),
            items
        );
    }
}
