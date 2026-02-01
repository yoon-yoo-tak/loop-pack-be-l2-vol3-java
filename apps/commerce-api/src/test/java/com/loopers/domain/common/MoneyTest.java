package com.loopers.domain.common;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @DisplayName("Money를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("양수 금액이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsMoney_whenAmountIsPositive() {
            // act
            Money money = new Money(10000);

            // assert
            assertThat(money.getAmount()).isEqualTo(10000);
        }

        @DisplayName("금액이 0이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenAmountIsZero() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Money(0);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("금액이 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenAmountIsNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Money(-1);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("동등성을 비교할 때, ")
    @Nested
    class Equals {

        @DisplayName("같은 금액이면 동등하다.")
        @Test
        void isEqual_whenSameAmount() {
            // arrange
            Money money1 = new Money(10000);
            Money money2 = new Money(10000);

            // assert
            assertThat(money1).isEqualTo(money2);
        }

        @DisplayName("다른 금액이면 동등하지 않다.")
        @Test
        void isNotEqual_whenDifferentAmount() {
            // arrange
            Money money1 = new Money(10000);
            Money money2 = new Money(20000);

            // assert
            assertThat(money1).isNotEqualTo(money2);
        }
    }
}
