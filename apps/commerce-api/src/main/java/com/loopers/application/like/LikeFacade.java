package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final LikeService likeService;
    private final ProductService productService;

    @Transactional
    public LikeInfo addLike(User user, Long productId) {
        Product product = productService.getById(productId);
        Like like = likeService.addLike(user.getId(), productId);
        product.incrementLikeCount();
        return LikeInfo.from(like, product);
    }

    @Transactional
    public void removeLike(User user, Long productId) {
        Product product = productService.getById(productId);
        likeService.removeLike(user.getId(), productId);
        product.decrementLikeCount();
    }

    public List<LikeInfo> getUserLikes(User user, Long userId) {
        if (!user.getId().equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "본인의 좋아요 목록만 조회할 수 있습니다.");
        }

        List<Like> likes = likeService.getLikesByUserId(userId);
        return likes.stream()
            .map(like -> {
                Product product = productService.getById(like.getProductId());
                return LikeInfo.from(like, product);
            })
            .toList();
    }
}
