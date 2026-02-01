package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final ProductService productService;

    @Transactional
    public Like addLike(Long userId, Long productId) {
        Product product = productService.getById(productId);

        if (likeRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 좋아요한 상품입니다.");
        }

        Like like = new Like(userId, productId);
        product.incrementLikeCount();

        return likeRepository.save(like);
    }

    @Transactional
    public void removeLike(Long userId, Long productId) {
        Like like = likeRepository.findByUserIdAndProductId(userId, productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요하지 않은 상품입니다."));

        Product product = productService.getById(productId);
        product.decrementLikeCount();

        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public List<Like> getLikesByUserId(Long userId) {
        return likeRepository.findAllByUserId(userId);
    }
}
