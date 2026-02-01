package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Order Admin V1 API", description = "주문 어드민 API 입니다.")
public interface OrderAdminV1ApiSpec {

    @Operation(summary = "주문 목록 조회", description = "전체 주문 목록을 페이지네이션하여 조회합니다.")
    ApiResponse<Page<OrderAdminV1Dto.OrderListResponse>> getList(String admin, Pageable pageable);

    @Operation(summary = "주문 상세 조회", description = "단일 주문 상세 정보를 조회합니다.")
    ApiResponse<OrderAdminV1Dto.OrderResponse> getDetail(String admin, Long orderId);
}
