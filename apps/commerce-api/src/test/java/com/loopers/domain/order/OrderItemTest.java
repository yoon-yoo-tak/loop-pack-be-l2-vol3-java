package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    private static final Long VALID_PRODUCT_ID = 1L;
    private static final int VALID_QUANTITY = 2;
    private static final String VALID_PRODUCT_NAME = "에어맥스 90";
    private static final int VALID_PRODUCT_PRICE = 159000;
    private static final String VALID_BRAND_NAME = "나이키";

    @DisplayName("주문 항목을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("모든 정보가 올바르면, 정상적으로 생성된다.")
        @Test
        void createsOrderItem_whenAllFieldsAreValid() {
            // act
            OrderItem orderItem = new OrderItem(VALID_PRODUCT_ID, VALID_QUANTITY, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_BRAND_NAME);

            // assert
            assertAll(
                () -> assertThat(orderItem.getProductId()).isEqualTo(VALID_PRODUCT_ID),
                () -> assertThat(orderItem.getQuantity()).isEqualTo(VALID_QUANTITY),
                () -> assertThat(orderItem.getProductName()).isEqualTo(VALID_PRODUCT_NAME),
                () -> assertThat(orderItem.getProductPrice()).isEqualTo(VALID_PRODUCT_PRICE),
                () -> assertThat(orderItem.getBrandName()).isEqualTo(VALID_BRAND_NAME)
            );
        }

        @DisplayName("상품 ID가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductIdIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(null, VALID_QUANTITY, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_BRAND_NAME);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 ID가 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductIdIsZeroOrNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(0L, VALID_QUANTITY, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_BRAND_NAME);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("수량이 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenQuantityIsZeroOrNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(VALID_PRODUCT_ID, 0, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_BRAND_NAME);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductNameIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(VALID_PRODUCT_ID, VALID_QUANTITY, null, VALID_PRODUCT_PRICE, VALID_BRAND_NAME);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductNameIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(VALID_PRODUCT_ID, VALID_QUANTITY, "  ", VALID_PRODUCT_PRICE, VALID_BRAND_NAME);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 가격이 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductPriceIsZeroOrNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(VALID_PRODUCT_ID, VALID_QUANTITY, VALID_PRODUCT_NAME, 0, VALID_BRAND_NAME);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("브랜드 이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenBrandNameIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(VALID_PRODUCT_ID, VALID_QUANTITY, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, null);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("브랜드 이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenBrandNameIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new OrderItem(VALID_PRODUCT_ID, VALID_QUANTITY, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, "  ");
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
