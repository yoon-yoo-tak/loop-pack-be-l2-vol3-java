package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.product.Product;

public record LikeInfo(Long likeId, Long productId, String productName, int productPrice, Long brandId, String brandName) {
    public static LikeInfo from(Like like, Product product) {
        return new LikeInfo(
            like.getId(),
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getBrandId(),
            product.getBrandName()
        );
    }
}
