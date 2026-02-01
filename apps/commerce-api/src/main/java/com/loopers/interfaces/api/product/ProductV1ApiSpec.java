package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Product V1 API", description = "상품 고객 API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다. brandId로 필터링, sort(latest/price_asc)로 정렬 가능합니다.")
    ApiResponse<Page<ProductV1Dto.ProductResponse>> getList(Long brandId, String sort, int page, int size);

    @Operation(summary = "상품 정보 조회", description = "상품 상세 정보를 조회합니다.")
    ApiResponse<ProductV1Dto.ProductResponse> getDetail(Long productId);
}
