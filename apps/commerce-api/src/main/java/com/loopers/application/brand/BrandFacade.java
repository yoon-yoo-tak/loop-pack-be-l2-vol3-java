package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;
    private final ProductService productService;

    public BrandInfo create(String name) {
        Brand brand = brandService.create(name);
        return BrandInfo.from(brand);
    }

    public BrandInfo getById(Long id) {
        Brand brand = brandService.getById(id);
        return BrandInfo.from(brand);
    }

    public Page<BrandInfo> getAll(Pageable pageable) {
        return brandService.getAll(pageable).map(BrandInfo::from);
    }

    public BrandInfo update(Long id, String name) {
        Brand brand = brandService.update(id, name);
        return BrandInfo.from(brand);
    }

    public void delete(Long id) {
        productService.deleteByBrandId(id);
        brandService.delete(id);
    }
}
