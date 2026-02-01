package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    private static final Brand VALID_BRAND = new Brand("나이키");
    private static final String VALID_NAME = "에어맥스 90";
    private static final int VALID_PRICE = 159000;
    private static final int VALID_STOCK = 100;

    @DisplayName("상품을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("모든 정보가 올바르면, 정상적으로 생성된다.")
        @Test
        void createsProduct_whenAllFieldsAreValid() {
            // act
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);

            // assert
            assertAll(
                () -> assertThat(product.getName()).isEqualTo(VALID_NAME),
                () -> assertThat(product.getPrice()).isEqualTo(VALID_PRICE),
                () -> assertThat(product.getStock()).isEqualTo(VALID_STOCK),
                () -> assertThat(product.getBrand()).isEqualTo(VALID_BRAND)
            );
        }

        @DisplayName("이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product(null, VALID_PRICE, VALID_STOCK, VALID_BRAND);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product("  ", VALID_PRICE, VALID_STOCK, VALID_BRAND);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("가격이 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenPriceIsZeroOrNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product(VALID_NAME, 0, VALID_STOCK, VALID_BRAND);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("재고가 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenStockIsNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product(VALID_NAME, VALID_PRICE, -1, VALID_BRAND);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("브랜드가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenBrandIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, null);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("좋아요 수를 증가시킬 때, ")
    @Nested
    class IncrementLikeCount {

        @DisplayName("좋아요 수가 1 증가한다.")
        @Test
        void incrementsLikeCountByOne() {
            // arrange
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);

            // act
            product.incrementLikeCount();

            // assert
            assertThat(product.getLikeCount()).isEqualTo(1);
        }
    }

    @DisplayName("좋아요 수를 감소시킬 때, ")
    @Nested
    class DecrementLikeCount {

        @DisplayName("좋아요 수가 1 감소한다.")
        @Test
        void decrementsLikeCountByOne() {
            // arrange
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);
            product.incrementLikeCount();

            // act
            product.decrementLikeCount();

            // assert
            assertThat(product.getLikeCount()).isEqualTo(0);
        }

        @DisplayName("좋아요 수가 0이면, 0을 유지한다.")
        @Test
        void staysAtZero_whenAlreadyZero() {
            // arrange
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);

            // act
            product.decrementLikeCount();

            // assert
            assertThat(product.getLikeCount()).isEqualTo(0);
        }
    }

    @DisplayName("상품을 수정할 때, ")
    @Nested
    class Update {

        @DisplayName("올바른 정보가 주어지면, 이름/가격/재고가 변경된다.")
        @Test
        void updatesFields_whenValidInfoProvided() {
            // arrange
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);

            // act
            product.update("에어포스 1", 129000, 50);

            // assert
            assertAll(
                () -> assertThat(product.getName()).isEqualTo("에어포스 1"),
                () -> assertThat(product.getPrice()).isEqualTo(129000),
                () -> assertThat(product.getStock()).isEqualTo(50)
            );
        }

        @DisplayName("이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsNull() {
            // arrange
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                product.update(null, VALID_PRICE, VALID_STOCK);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("가격이 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenPriceIsZeroOrNegative() {
            // arrange
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                product.update(VALID_NAME, 0, VALID_STOCK);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("재고가 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenStockIsNegative() {
            // arrange
            Product product = new Product(VALID_NAME, VALID_PRICE, VALID_STOCK, VALID_BRAND);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                product.update(VALID_NAME, VALID_PRICE, -1);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
