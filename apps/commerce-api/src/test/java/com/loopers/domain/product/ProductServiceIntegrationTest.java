package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Brand brand;

    @BeforeEach
    void setUp() {
        brand = brandService.create("나이키");
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("올바른 정보가 주어지면, 상품이 저장되고 반환된다.")
        @Test
        void savesAndReturnsProduct_whenValidInfoProvided() {
            // act
            Product result = productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());

            // assert
            assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getName()).isEqualTo("에어맥스 90"),
                () -> assertThat(result.getPrice()).isEqualTo(159000),
                () -> assertThat(result.getStock()).isEqualTo(100),
                () -> assertThat(result.getBrandId()).isEqualTo(brand.getId()),
                () -> assertThat(result.getBrandName()).isEqualTo(brand.getName())
            );
        }
    }

    @DisplayName("상품을 조회할 때, ")
    @Nested
    class GetById {

        @DisplayName("존재하는 ID가 주어지면, 상품을 반환한다.")
        @Test
        void returnsProduct_whenIdExists() {
            // arrange
            Product saved = productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());

            // act
            Product result = productService.getById(saved.getId());

            // assert
            assertAll(
                () -> assertThat(result.getId()).isEqualTo(saved.getId()),
                () -> assertThat(result.getName()).isEqualTo("에어맥스 90")
            );
        }

        @DisplayName("존재하지 않는 ID가 주어지면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenIdDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                productService.getById(999L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("상품 목록을 조회할 때, ")
    @Nested
    class GetAll {

        @DisplayName("전체 목록을 페이지네이션하여 반환한다.")
        @Test
        void returnsPaginatedProducts() {
            // arrange
            productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());
            productService.create("에어포스 1", 129000, 200, brand.getId(), brand.getName());
            productService.create("조던 1", 199000, 50, brand.getId(), brand.getName());

            // act
            Page<Product> result = productService.getAll(null, PageRequest.of(0, 2));

            // assert
            assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(3)
            );
        }

        @DisplayName("brandId로 필터링하여 반환한다.")
        @Test
        void returnsFilteredProducts_whenBrandIdProvided() {
            // arrange
            Brand anotherBrand = brandService.create("아디다스");
            productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());
            productService.create("울트라부스트", 189000, 80, anotherBrand.getId(), anotherBrand.getName());

            // act
            Page<Product> result = productService.getAll(brand.getId(), PageRequest.of(0, 20));

            // assert
            assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getName()).isEqualTo("에어맥스 90")
            );
        }
    }

    @DisplayName("상품을 수정할 때, ")
    @Nested
    class Update {

        @DisplayName("올바른 정보가 주어지면, 이름/가격/재고가 변경된다.")
        @Test
        void updatesProduct_whenValidInfoProvided() {
            // arrange
            Product saved = productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());

            // act
            Product result = productService.update(saved.getId(), "에어포스 1", 129000, 50);

            // assert
            assertAll(
                () -> assertThat(result.getName()).isEqualTo("에어포스 1"),
                () -> assertThat(result.getPrice()).isEqualTo(129000),
                () -> assertThat(result.getStock()).isEqualTo(50)
            );
        }

        @DisplayName("존재하지 않는 ID가 주어지면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenIdDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                productService.update(999L, "에어포스 1", 129000, 50);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("상품을 삭제할 때, ")
    @Nested
    class Delete {

        @DisplayName("존재하는 상품을 삭제한다.")
        @Test
        void deletesProduct_whenProductExists() {
            // arrange
            Product saved = productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());

            // act
            productService.delete(saved.getId());

            // assert
            CoreException result = assertThrows(CoreException.class, () -> {
                productService.getById(saved.getId());
            });
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("존재하지 않는 ID가 주어지면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenIdDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                productService.delete(999L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("브랜드 ID로 상품을 일괄 삭제할 때, ")
    @Nested
    class DeleteByBrandId {

        @DisplayName("해당 브랜드의 모든 상품이 삭제된다.")
        @Test
        void deletesAllProductsOfBrand() {
            // arrange
            productService.create("에어맥스 90", 159000, 100, brand.getId(), brand.getName());
            productService.create("에어포스 1", 129000, 200, brand.getId(), brand.getName());

            // act
            productService.deleteByBrandId(brand.getId());

            // assert
            Page<Product> result = productService.getAll(brand.getId(), PageRequest.of(0, 20));
            assertThat(result.getContent()).isEmpty();
        }
    }
}
