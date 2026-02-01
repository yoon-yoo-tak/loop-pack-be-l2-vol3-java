package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderItemInfo;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderV1Dto {

    public record CreateRequest(List<OrderItemRequest> items) {}

    public record OrderItemRequest(Long productId, int quantity) {}

    public record OrderResponse(Long orderId, Long userId, int totalPrice, ZonedDateTime createdAt, List<OrderItemResponse> items) {
        public static OrderResponse from(OrderInfo info) {
            List<OrderItemResponse> items = info.items().stream()
                .map(OrderItemResponse::from)
                .toList();
            return new OrderResponse(
                info.orderId(),
                info.userId(),
                info.totalPrice(),
                info.createdAt(),
                items
            );
        }
    }

    public record OrderItemResponse(Long orderItemId, Long productId, int quantity, String productName, int productPrice, String brandName) {
        public static OrderItemResponse from(OrderItemInfo info) {
            return new OrderItemResponse(
                info.orderItemId(),
                info.productId(),
                info.quantity(),
                info.productName(),
                info.productPrice(),
                info.brandName()
            );
        }
    }

    public record OrderListResponse(Long orderId, Long userId, int totalPrice, ZonedDateTime createdAt) {
        public static OrderListResponse from(OrderInfo info) {
            return new OrderListResponse(
                info.orderId(),
                info.userId(),
                info.totalPrice(),
                info.createdAt()
            );
        }
    }
}
