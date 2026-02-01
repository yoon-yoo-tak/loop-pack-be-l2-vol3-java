package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.auth.AuthAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/brands")
public class BrandAdminV1Controller implements BrandAdminV1ApiSpec {

    private final BrandFacade brandFacade;

    @PostMapping
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> create(@AuthAdmin String admin, @RequestBody BrandAdminV1Dto.CreateRequest request) {
        BrandInfo info = brandFacade.create(request.name());
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(info));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<BrandAdminV1Dto.BrandResponse>> getList(@AuthAdmin String admin, Pageable pageable) {
        Page<BrandInfo> page = brandFacade.getAll(pageable);
        Page<BrandAdminV1Dto.BrandResponse> responsePage = page.map(BrandAdminV1Dto.BrandResponse::from);
        return ApiResponse.success(responsePage);
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> getDetail(@AuthAdmin String admin, @PathVariable Long brandId) {
        BrandInfo info = brandFacade.getById(brandId);
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(info));
    }

    @PutMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> update(@AuthAdmin String admin, @PathVariable Long brandId, @RequestBody BrandAdminV1Dto.UpdateRequest request) {
        BrandInfo info = brandFacade.update(brandId, request.name());
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(info));
    }

    @DeleteMapping("/{brandId}")
    @Override
    public ApiResponse<Void> delete(@AuthAdmin String admin, @PathVariable Long brandId) {
        brandFacade.delete(brandId);
        return ApiResponse.success(null);
    }
}
