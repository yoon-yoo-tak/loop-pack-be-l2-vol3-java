package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.auth.AuthAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/orders")
public class OrderAdminV1Controller implements OrderAdminV1ApiSpec {

    private final OrderFacade orderFacade;

    @GetMapping
    @Override
    public ApiResponse<Page<OrderAdminV1Dto.OrderListResponse>> getList(@AuthAdmin String admin, Pageable pageable) {
        Page<OrderInfo> page = orderFacade.getAllOrders(pageable);
        Page<OrderAdminV1Dto.OrderListResponse> responsePage = page.map(OrderAdminV1Dto.OrderListResponse::from);
        return ApiResponse.success(responsePage);
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderAdminV1Dto.OrderResponse> getDetail(@AuthAdmin String admin, @PathVariable Long orderId) {
        OrderInfo info = orderFacade.getOrderById(orderId);
        return ApiResponse.success(OrderAdminV1Dto.OrderResponse.from(info));
    }
}
