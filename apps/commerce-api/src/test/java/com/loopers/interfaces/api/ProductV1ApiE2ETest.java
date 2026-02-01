package com.loopers.interfaces.api;

import com.loopers.interfaces.api.brand.BrandAdminV1Dto;
import com.loopers.interfaces.api.product.ProductAdminV1Dto;
import com.loopers.interfaces.api.product.ProductV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductV1ApiE2ETest {

    private static final String ENDPOINT_PRODUCTS = "/api/v1/products";
    private static final String ENDPOINT_PRODUCT_DETAIL = "/api/v1/products/{productId}";
    private static final String ENDPOINT_ADMIN_BRANDS = "/api-admin/v1/brands";
    private static final String ENDPOINT_ADMIN_PRODUCTS = "/api-admin/v1/products";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public ProductV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Loopers-Ldap", "loopers.admin");
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private Long createBrand(String name) {
        BrandAdminV1Dto.CreateRequest request = new BrandAdminV1Dto.CreateRequest(name);
        ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
            testRestTemplate.exchange(ENDPOINT_ADMIN_BRANDS, HttpMethod.POST, new HttpEntity<>(request, adminHeaders()),
                new ParameterizedTypeReference<>() {});
        return response.getBody().data().id();
    }

    private Long createProduct(String name, int price, int stock, Long brandId) {
        ProductAdminV1Dto.CreateRequest request = new ProductAdminV1Dto.CreateRequest(name, price, stock, brandId);
        ResponseEntity<ApiResponse<ProductAdminV1Dto.ProductResponse>> response =
            testRestTemplate.exchange(ENDPOINT_ADMIN_PRODUCTS, HttpMethod.POST, new HttpEntity<>(request, adminHeaders()),
                new ParameterizedTypeReference<>() {});
        return response.getBody().data().id();
    }

    private Long brandId;

    @BeforeEach
    void setUp() {
        brandId = createBrand("나이키");
    }

    @DisplayName("GET /api/v1/products/{productId}")
    @Nested
    class GetProductDetail {

        @DisplayName("존재하는 상품 ID이면, 200 OK와 함께 상품 정보를 반환한다 (재고 미포함).")
        @Test
        void returnsProductInfo_whenProductExists() {
            // arrange
            Long productId = createProduct("에어맥스 90", 159000, 100, brandId);

            // act
            ParameterizedTypeReference<ApiResponse<ProductV1Dto.ProductResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<ProductV1Dto.ProductResponse>> response =
                testRestTemplate.exchange(ENDPOINT_PRODUCT_DETAIL, HttpMethod.GET, null, responseType, productId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().id()).isEqualTo(productId),
                () -> assertThat(response.getBody().data().name()).isEqualTo("에어맥스 90"),
                () -> assertThat(response.getBody().data().price()).isEqualTo(159000),
                () -> assertThat(response.getBody().data().brandId()).isEqualTo(brandId),
                () -> assertThat(response.getBody().data().brandName()).isEqualTo("나이키")
            );
        }

        @DisplayName("존재하지 않는 상품 ID이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenProductDoesNotExist() {
            // act
            ParameterizedTypeReference<ApiResponse<ProductV1Dto.ProductResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<ProductV1Dto.ProductResponse>> response =
                testRestTemplate.exchange(ENDPOINT_PRODUCT_DETAIL, HttpMethod.GET, null, responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @DisplayName("GET /api/v1/products")
    @Nested
    class GetProductList {

        @DisplayName("전체 상품 목록을 페이지네이션하여 반환한다.")
        @Test
        void returnsPaginatedProducts() {
            // arrange
            createProduct("에어맥스 90", 159000, 100, brandId);
            createProduct("에어포스 1", 129000, 200, brandId);
            createProduct("조던 1", 199000, 50, brandId);

            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_PRODUCTS + "?page=0&size=2", HttpMethod.GET, null, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat((List<?>) response.getBody().data().get("content")).hasSize(2),
                () -> assertThat(((Number) response.getBody().data().get("totalElements")).longValue()).isEqualTo(3L)
            );
        }

        @DisplayName("brandId로 필터링하여 반환한다.")
        @Test
        void returnsFilteredProducts_whenBrandIdProvided() {
            // arrange
            Long anotherBrandId = createBrand("아디다스");
            createProduct("에어맥스 90", 159000, 100, brandId);
            createProduct("울트라부스트", 189000, 80, anotherBrandId);

            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_PRODUCTS + "?brandId=" + brandId, HttpMethod.GET, null, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat((List<?>) response.getBody().data().get("content")).hasSize(1)
            );
        }

        @DisplayName("sort=latest이면, 최신순으로 정렬한다.")
        @Test
        void returnsSortedByLatest() {
            // arrange
            createProduct("에어맥스 90", 159000, 100, brandId);
            createProduct("에어포스 1", 129000, 200, brandId);

            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_PRODUCTS + "?sort=latest", HttpMethod.GET, null, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> {
                    List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().data().get("content");
                    assertThat(content.get(0).get("name")).isEqualTo("에어포스 1");
                    assertThat(content.get(1).get("name")).isEqualTo("에어맥스 90");
                }
            );
        }

        @DisplayName("sort=price_asc이면, 가격 낮은순으로 정렬한다.")
        @Test
        void returnsSortedByPriceAsc() {
            // arrange
            createProduct("에어맥스 90", 159000, 100, brandId);
            createProduct("에어포스 1", 129000, 200, brandId);
            createProduct("조던 1", 199000, 50, brandId);

            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_PRODUCTS + "?sort=price_asc", HttpMethod.GET, null, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> {
                    List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().data().get("content");
                    assertThat(((Number) content.get(0).get("price")).intValue()).isEqualTo(129000);
                    assertThat(((Number) content.get(1).get("price")).intValue()).isEqualTo(159000);
                    assertThat(((Number) content.get(2).get("price")).intValue()).isEqualTo(199000);
                }
            );
        }

        @DisplayName("인증 없이도 조회할 수 있다.")
        @Test
        void returnsSuccess_withoutAuth() {
            // arrange
            createProduct("에어맥스 90", 159000, 100, brandId);

            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_PRODUCTS, HttpMethod.GET, null, responseType);

            // assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }
    }
}
