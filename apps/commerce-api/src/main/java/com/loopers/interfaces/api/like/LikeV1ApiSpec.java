package com.loopers.interfaces.api.like;

import com.loopers.domain.user.User;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Like V1 API", description = "좋아요 고객 API 입니다.")
public interface LikeV1ApiSpec {

    @Operation(summary = "상품 좋아요 등록", description = "상품에 좋아요를 등록합니다.")
    ApiResponse<LikeV1Dto.LikeResponse> addLike(User user, Long productId);

    @Operation(summary = "상품 좋아요 취소", description = "상품에 등록한 좋아요를 취소합니다.")
    ApiResponse<Void> removeLike(User user, Long productId);

    @Operation(summary = "내가 좋아요한 상품 목록 조회", description = "유저가 좋아요한 상품 목록을 조회합니다.")
    ApiResponse<List<LikeV1Dto.LikedProductResponse>> getUserLikes(User user, Long userId);
}
