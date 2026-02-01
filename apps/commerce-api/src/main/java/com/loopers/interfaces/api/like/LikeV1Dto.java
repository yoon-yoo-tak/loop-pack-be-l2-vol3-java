package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeInfo;

public class LikeV1Dto {

    public record LikeResponse(Long likeId, Long productId, String productName, int productPrice, Long brandId, String brandName) {
        public static LikeResponse from(LikeInfo info) {
            return new LikeResponse(
                info.likeId(),
                info.productId(),
                info.productName(),
                info.productPrice(),
                info.brandId(),
                info.brandName()
            );
        }
    }

    public record LikedProductResponse(Long likeId, Long productId, String productName, int productPrice, Long brandId, String brandName) {
        public static LikedProductResponse from(LikeInfo info) {
            return new LikedProductResponse(
                info.likeId(),
                info.productId(),
                info.productName(),
                info.productPrice(),
                info.brandId(),
                info.brandName()
            );
        }
    }
}
