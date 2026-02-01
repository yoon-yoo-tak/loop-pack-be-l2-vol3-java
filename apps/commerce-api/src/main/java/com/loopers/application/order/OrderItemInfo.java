package com.loopers.application.order;

import com.loopers.domain.order.OrderItem;

public record OrderItemInfo(Long orderItemId, Long productId, int quantity, String productName, int productPrice, String brandName) {
    public static OrderItemInfo from(OrderItem orderItem) {
        return new OrderItemInfo(
            orderItem.getId(),
            orderItem.getProductId(),
            orderItem.getQuantity(),
            orderItem.getProductName(),
            orderItem.getProductPrice(),
            orderItem.getBrandName()
        );
    }
}
