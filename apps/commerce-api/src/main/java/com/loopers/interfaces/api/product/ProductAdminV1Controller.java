package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/products")
public class ProductAdminV1Controller implements ProductAdminV1ApiSpec {

    private final ProductFacade productFacade;

    @PostMapping
    @Override
    public ApiResponse<ProductAdminV1Dto.ProductResponse> create(@AuthAdmin String admin, @RequestBody ProductAdminV1Dto.CreateRequest request) {
        ProductInfo info = productFacade.create(request.name(), request.price(), request.stock(), request.brandId());
        return ApiResponse.success(ProductAdminV1Dto.ProductResponse.from(info));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<ProductAdminV1Dto.ProductResponse>> getList(
        @AuthAdmin String admin,
        @RequestParam(required = false) Long brandId,
        Pageable pageable
    ) {
        Page<ProductInfo> page = productFacade.getAll(brandId, pageable);
        Page<ProductAdminV1Dto.ProductResponse> responsePage = page.map(ProductAdminV1Dto.ProductResponse::from);
        return ApiResponse.success(responsePage);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductAdminV1Dto.ProductResponse> getDetail(@AuthAdmin String admin, @PathVariable Long productId) {
        ProductInfo info = productFacade.getById(productId);
        return ApiResponse.success(ProductAdminV1Dto.ProductResponse.from(info));
    }

    @PutMapping("/{productId}")
    @Override
    public ApiResponse<ProductAdminV1Dto.ProductResponse> update(@AuthAdmin String admin, @PathVariable Long productId, @RequestBody ProductAdminV1Dto.UpdateRequest request) {
        ProductInfo info = productFacade.update(productId, request.name(), request.price(), request.stock());
        return ApiResponse.success(ProductAdminV1Dto.ProductResponse.from(info));
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Void> delete(@AuthAdmin String admin, @PathVariable Long productId) {
        productFacade.delete(productId);
        return ApiResponse.success(null);
    }
}
