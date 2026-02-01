package com.loopers.application.product;

import com.loopers.domain.product.Product;

public record ProductInfo(Long id, String name, int price, int stock, int likeCount, Long brandId, String brandName) {
    public static ProductInfo from(Product product) {
        return new ProductInfo(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            product.getLikeCount(),
            product.getBrandId(),
            product.getBrandName()
        );
    }
}
