package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;

    public ProductInfo create(String name, int price, int stock, Long brandId) {
        Brand brand = brandService.getById(brandId);
        Product product = productService.create(name, price, stock, brand.getId(), brand.getName());
        return ProductInfo.from(product);
    }

    public ProductInfo getById(Long id) {
        Product product = productService.getById(id);
        return ProductInfo.from(product);
    }

    public Page<ProductInfo> getAll(Long brandId, Pageable pageable) {
        return productService.getAll(brandId, pageable).map(ProductInfo::from);
    }

    public ProductInfo update(Long id, String name, int price, int stock) {
        Product product = productService.update(id, name, price, stock);
        return ProductInfo.from(product);
    }

    public void delete(Long id) {
        productService.delete(id);
    }
}
