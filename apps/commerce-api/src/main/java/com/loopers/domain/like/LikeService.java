package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public Like addLike(Long userId, Long productId) {
        if (likeRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 좋아요한 상품입니다.");
        }

        Like like = new Like(userId, productId);
        return likeRepository.save(like);
    }

    @Transactional
    public void removeLike(Long userId, Long productId) {
        Like like = likeRepository.findByUserIdAndProductId(userId, productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요하지 않은 상품입니다."));

        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public List<Like> getLikesByUserId(Long userId) {
        return likeRepository.findAllByUserId(userId);
    }
}
