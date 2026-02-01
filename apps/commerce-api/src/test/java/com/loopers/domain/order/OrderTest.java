package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    private static final Long VALID_USER_ID = 1L;

    private OrderItem createOrderItem(Long productId, int quantity, int price) {
        return new OrderItem(productId, quantity, "상품" + productId, price, "브랜드");
    }

    @DisplayName("주문을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("올바른 정보가 주어지면, 정상적으로 생성되고 총 가격이 계산된다.")
        @Test
        void createsOrder_whenAllFieldsAreValid() {
            // arrange
            List<OrderItem> items = List.of(
                createOrderItem(1L, 2, 10000),
                createOrderItem(2L, 1, 20000)
            );

            // act
            Order order = new Order(VALID_USER_ID, items);

            // assert
            assertAll(
                () -> assertThat(order.getUserId()).isEqualTo(VALID_USER_ID),
                () -> assertThat(order.getItems()).hasSize(2),
                () -> assertThat(order.getTotalPrice()).isEqualTo(40000)
            );
        }

        @DisplayName("유저 ID가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUserIdIsNull() {
            // arrange
            List<OrderItem> items = List.of(createOrderItem(1L, 1, 10000));

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Order(null, items);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("유저 ID가 0 이하이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUserIdIsZeroOrNegative() {
            // arrange
            List<OrderItem> items = List.of(createOrderItem(1L, 1, 10000));

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Order(0L, items);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("주문 항목이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenItemsIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Order(VALID_USER_ID, null);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("주문 항목이 비어있으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenItemsIsEmpty() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Order(VALID_USER_ID, new ArrayList<>());
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
