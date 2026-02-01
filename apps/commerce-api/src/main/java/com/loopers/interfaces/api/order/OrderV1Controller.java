package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.domain.order.OrderItemCommand;
import com.loopers.domain.user.User;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> createOrder(@AuthUser User user, @RequestBody OrderV1Dto.CreateRequest request) {
        List<OrderItemCommand> commands = request.items().stream()
            .map(item -> new OrderItemCommand(item.productId(), item.quantity()))
            .toList();
        OrderInfo info = orderFacade.createOrder(user, commands);
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(info));
    }

    @GetMapping
    @Override
    public ApiResponse<List<OrderV1Dto.OrderListResponse>> getOrders(
        @AuthUser User user,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startAt,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endAt
    ) {
        List<OrderInfo> orders = orderFacade.getOrders(user, startAt, endAt);
        List<OrderV1Dto.OrderListResponse> response = orders.stream()
            .map(OrderV1Dto.OrderListResponse::from)
            .toList();
        return ApiResponse.success(response);
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrder(@AuthUser User user, @PathVariable Long orderId) {
        OrderInfo info = orderFacade.getOrder(user, orderId);
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(info));
    }
}
