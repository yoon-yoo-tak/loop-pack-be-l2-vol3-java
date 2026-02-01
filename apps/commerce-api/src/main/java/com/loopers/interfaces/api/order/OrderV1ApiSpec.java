package com.loopers.interfaces.api.order;

import com.loopers.domain.user.User;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Order V1 API", description = "주문 고객 API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문 생성", description = "상품을 주문합니다.")
    ApiResponse<OrderV1Dto.OrderResponse> createOrder(User user, OrderV1Dto.CreateRequest request);

    @Operation(summary = "주문 목록 조회", description = "유저의 주문 목록을 조회합니다.")
    ApiResponse<List<OrderV1Dto.OrderListResponse>> getOrders(User user, LocalDate startAt, LocalDate endAt);

    @Operation(summary = "주문 상세 조회", description = "단일 주문 상세 정보를 조회합니다.")
    ApiResponse<OrderV1Dto.OrderResponse> getOrder(User user, Long orderId);
}
