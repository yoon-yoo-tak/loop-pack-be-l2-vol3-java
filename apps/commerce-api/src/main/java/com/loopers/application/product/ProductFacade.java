package com.loopers.application.product;

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

    public ProductInfo create(String name, int price, int stock, Long brandId) {
        Product product = productService.create(name, price, stock, brandId);
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
