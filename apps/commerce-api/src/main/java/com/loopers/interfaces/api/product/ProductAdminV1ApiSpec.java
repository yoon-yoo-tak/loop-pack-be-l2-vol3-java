package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Product Admin V1 API", description = "상품 어드민 API 입니다.")
public interface ProductAdminV1ApiSpec {

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다. 브랜드는 이미 등록되어 있어야 합니다.")
    ApiResponse<ProductAdminV1Dto.ProductResponse> create(String admin, ProductAdminV1Dto.CreateRequest request);

    @Operation(summary = "상품 목록 조회", description = "등록된 상품 목록을 페이지네이션하여 조회합니다. brandId로 필터링 가능합니다.")
    ApiResponse<Page<ProductAdminV1Dto.ProductResponse>> getList(String admin, Long brandId, Pageable pageable);

    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다.")
    ApiResponse<ProductAdminV1Dto.ProductResponse> getDetail(String admin, Long productId);

    @Operation(summary = "상품 정보 수정", description = "상품 정보를 수정합니다. 브랜드는 수정할 수 없습니다.")
    ApiResponse<ProductAdminV1Dto.ProductResponse> update(String admin, Long productId, ProductAdminV1Dto.UpdateRequest request);

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    ApiResponse<Void> delete(String admin, Long productId);
}
