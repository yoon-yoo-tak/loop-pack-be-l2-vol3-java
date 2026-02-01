package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

public class ProductAdminV1Dto {

    public record CreateRequest(String name, int price, int stock, Long brandId) {
    }

    public record UpdateRequest(String name, int price, int stock) {
    }

    public record ProductResponse(Long id, String name, int price, int stock, int likeCount, Long brandId, String brandName) {
        public static ProductResponse from(ProductInfo info) {
            return new ProductResponse(
                info.id(),
                info.name(),
                info.price(),
                info.stock(),
                info.likeCount(),
                info.brandId(),
                info.brandName()
            );
        }
    }
}
