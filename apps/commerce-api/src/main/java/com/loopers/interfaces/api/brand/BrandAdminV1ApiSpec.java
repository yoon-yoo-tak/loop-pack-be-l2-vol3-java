package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Brand Admin V1 API", description = "브랜드 어드민 API 입니다.")
public interface BrandAdminV1ApiSpec {

    @Operation(summary = "브랜드 등록", description = "새로운 브랜드를 등록합니다.")
    ApiResponse<BrandAdminV1Dto.BrandResponse> create(String admin, BrandAdminV1Dto.CreateRequest request);

    @Operation(summary = "브랜드 목록 조회", description = "등록된 브랜드 목록을 페이지네이션하여 조회합니다.")
    ApiResponse<Page<BrandAdminV1Dto.BrandResponse>> getList(String admin, Pageable pageable);

    @Operation(summary = "브랜드 상세 조회", description = "브랜드 상세 정보를 조회합니다.")
    ApiResponse<BrandAdminV1Dto.BrandResponse> getDetail(String admin, Long brandId);

    @Operation(summary = "브랜드 정보 수정", description = "브랜드 정보를 수정합니다.")
    ApiResponse<BrandAdminV1Dto.BrandResponse> update(String admin, Long brandId, BrandAdminV1Dto.UpdateRequest request);

    @Operation(summary = "브랜드 삭제", description = "브랜드를 삭제합니다.")
    ApiResponse<Void> delete(String admin, Long brandId);
}
