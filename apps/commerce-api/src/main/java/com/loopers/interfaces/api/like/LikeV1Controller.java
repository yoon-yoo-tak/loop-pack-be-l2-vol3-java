package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.LikeInfo;
import com.loopers.domain.user.User;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class LikeV1Controller implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;

    @PostMapping("/api/v1/products/{productId}/likes")
    @Override
    public ApiResponse<LikeV1Dto.LikeResponse> addLike(@AuthUser User user, @PathVariable Long productId) {
        LikeInfo info = likeFacade.addLike(user, productId);
        return ApiResponse.success(LikeV1Dto.LikeResponse.from(info));
    }

    @DeleteMapping("/api/v1/products/{productId}/likes")
    @Override
    public ApiResponse<Void> removeLike(@AuthUser User user, @PathVariable Long productId) {
        likeFacade.removeLike(user, productId);
        return ApiResponse.success(null);
    }

    @GetMapping("/api/v1/users/{userId}/likes")
    @Override
    public ApiResponse<List<LikeV1Dto.LikedProductResponse>> getUserLikes(@AuthUser User user, @PathVariable Long userId) {
        List<LikeInfo> likes = likeFacade.getUserLikes(user, userId);
        List<LikeV1Dto.LikedProductResponse> response = likes.stream()
            .map(LikeV1Dto.LikedProductResponse::from)
            .toList();
        return ApiResponse.success(response);
    }
}
