package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

public class ProductV1Dto {

    public record ProductResponse(Long id, String name, int price, Long brandId, String brandName) {
        public static ProductResponse from(ProductInfo info) {
            return new ProductResponse(
                info.id(),
                info.name(),
                info.price(),
                info.brandId(),
                info.brandName()
            );
        }
    }
}
