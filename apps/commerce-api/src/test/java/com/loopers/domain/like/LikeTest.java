package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LikeTest {

    @DisplayName("좋아요를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("유효한 userId와 productId가 주어지면, 정상적으로 생성된다.")
        @Test
        void createsLike_whenValidIdsProvided() {
            // act
            Like like = new Like(1L, 1L);

            // assert
            assertAll(
                () -> assertThat(like.getUserId()).isEqualTo(1L),
                () -> assertThat(like.getProductId()).isEqualTo(1L)
            );
        }

        @DisplayName("userId가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUserIdIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Like(null, 1L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("productId가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductIdIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Like(1L, null);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("userId가 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUserIdIsNotPositive() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Like(0L, 1L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("productId가 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductIdIsNotPositive() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Like(1L, 0L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
