package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private UserService userService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = userService.signup("testUser1", "Abcd1234!", "테스터", LocalDate.of(1990, 1, 1), "test@email.com");
        Brand brand = brandService.create("나이키");
        product = productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요를 추가할 때, ")
    @Nested
    class AddLike {

        @DisplayName("정상적으로 좋아요가 저장된다.")
        @Test
        void savesLike_whenValid() {
            // act
            Like result = likeService.addLike(user.getId(), product.getId());

            // assert
            assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getUserId()).isEqualTo(user.getId()),
                () -> assertThat(result.getProductId()).isEqualTo(product.getId())
            );
        }

        @DisplayName("이미 좋아요한 상품이면, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflict_whenAlreadyLiked() {
            // arrange
            likeService.addLike(user.getId(), product.getId());

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                likeService.addLike(user.getId(), product.getId());
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    @DisplayName("좋아요를 취소할 때, ")
    @Nested
    class RemoveLike {

        @DisplayName("정상적으로 좋아요가 삭제된다.")
        @Test
        void removesLike_whenValid() {
            // arrange
            likeService.addLike(user.getId(), product.getId());

            // act
            likeService.removeLike(user.getId(), product.getId());

            // assert
            assertThat(likeService.getLikesByUserId(user.getId())).isEmpty();
        }

        @DisplayName("좋아요하지 않은 상품이면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenNotLiked() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                likeService.removeLike(user.getId(), product.getId());
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("유저의 좋아요 목록을 조회할 때, ")
    @Nested
    class GetLikesByUserId {

        @DisplayName("좋아요한 상품 목록을 반환한다.")
        @Test
        void returnsLikeList() {
            // arrange
            Brand anotherBrand = brandService.create("아디다스");
            Product product2 = productService.create("울트라부스트", 189000, 80, anotherBrand.getId(), anotherBrand.getName());
            likeService.addLike(user.getId(), product.getId());
            likeService.addLike(user.getId(), product2.getId());

            // act
            List<Like> result = likeService.getLikesByUserId(user.getId());

            // assert
            assertThat(result).hasSize(2);
        }

        @DisplayName("좋아요한 상품이 없으면, 빈 목록을 반환한다.")
        @Test
        void returnsEmptyList_whenNoLikes() {
            // act
            List<Like> result = likeService.getLikesByUserId(user.getId());

            // assert
            assertThat(result).isEmpty();
        }
    }
}
