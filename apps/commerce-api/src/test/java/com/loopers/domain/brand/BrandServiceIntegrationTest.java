package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BrandServiceIntegrationTest {

    @Autowired
    private BrandService brandService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("브랜드를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("올바른 이름이 주어지면, 브랜드가 저장되고 반환된다.")
        @Test
        void savesAndReturnsBrand_whenNameIsValid() {
            // act
            Brand result = brandService.create("나이키");

            // assert
            assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getName()).isEqualTo("나이키")
            );
        }
    }

    @DisplayName("브랜드를 조회할 때, ")
    @Nested
    class GetById {

        @DisplayName("존재하는 ID가 주어지면, 브랜드를 반환한다.")
        @Test
        void returnsBrand_whenIdExists() {
            // arrange
            Brand saved = brandService.create("나이키");

            // act
            Brand result = brandService.getById(saved.getId());

            // assert
            assertAll(
                () -> assertThat(result.getId()).isEqualTo(saved.getId()),
                () -> assertThat(result.getName()).isEqualTo("나이키")
            );
        }

        @DisplayName("존재하지 않는 ID가 주어지면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenIdDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                brandService.getById(999L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("브랜드 목록을 조회할 때, ")
    @Nested
    class GetAll {

        @DisplayName("페이지네이션이 적용된 결과를 반환한다.")
        @Test
        void returnsPaginatedBrands() {
            // arrange
            brandService.create("나이키");
            brandService.create("아디다스");
            brandService.create("푸마");

            // act
            Page<Brand> result = brandService.getAll(PageRequest.of(0, 2));

            // assert
            assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(3),
                () -> assertThat(result.getTotalPages()).isEqualTo(2)
            );
        }
    }

    @DisplayName("브랜드를 수정할 때, ")
    @Nested
    class Update {

        @DisplayName("존재하는 브랜드의 이름을 변경한다.")
        @Test
        void updatesName_whenBrandExists() {
            // arrange
            Brand saved = brandService.create("나이키");

            // act
            Brand result = brandService.update(saved.getId(), "아디다스");

            // assert
            assertThat(result.getName()).isEqualTo("아디다스");
        }

        @DisplayName("존재하지 않는 ID가 주어지면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenIdDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                brandService.update(999L, "아디다스");
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("브랜드를 삭제할 때, ")
    @Nested
    class Delete {

        @DisplayName("존재하는 브랜드를 삭제한다.")
        @Test
        void deletesBrand_whenBrandExists() {
            // arrange
            Brand saved = brandService.create("나이키");

            // act
            brandService.delete(saved.getId());

            // assert
            CoreException result = assertThrows(CoreException.class, () -> {
                brandService.getById(saved.getId());
            });
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("존재하지 않는 ID가 주어지면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenIdDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                brandService.delete(999L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
