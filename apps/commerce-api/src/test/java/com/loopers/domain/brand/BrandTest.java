package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandTest {

    private static final String VALID_NAME = "나이키";

    @DisplayName("브랜드를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("올바른 이름이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsBrand_whenNameIsValid() {
            // act
            Brand brand = new Brand(VALID_NAME);

            // assert
            assertThat(brand.getName()).isEqualTo(VALID_NAME);
        }

        @DisplayName("이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Brand(null);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Brand("  ");
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("브랜드를 수정할 때, ")
    @Nested
    class Update {

        @DisplayName("올바른 이름이 주어지면, 이름이 변경된다.")
        @Test
        void updatesName_whenNewNameIsValid() {
            // arrange
            Brand brand = new Brand(VALID_NAME);

            // act
            brand.update("아디다스");

            // assert
            assertThat(brand.getName()).isEqualTo("아디다스");
        }

        @DisplayName("이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNewNameIsNull() {
            // arrange
            Brand brand = new Brand(VALID_NAME);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                brand.update(null);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNewNameIsBlank() {
            // arrange
            Brand brand = new Brand(VALID_NAME);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                brand.update("  ");
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
