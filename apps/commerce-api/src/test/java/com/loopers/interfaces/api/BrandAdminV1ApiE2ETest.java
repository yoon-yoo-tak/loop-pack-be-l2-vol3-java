package com.loopers.interfaces.api;

import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.interfaces.api.brand.BrandAdminV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
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
class BrandAdminV1ApiE2ETest {

    private static final String ENDPOINT_BRANDS = "/api-admin/v1/brands";
    private static final String ENDPOINT_BRAND_DETAIL = "/api-admin/v1/brands/{brandId}";

    private final TestRestTemplate testRestTemplate;
    private final BrandJpaRepository brandJpaRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public BrandAdminV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        BrandJpaRepository brandJpaRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.brandJpaRepository = brandJpaRepository;
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
        ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
            testRestTemplate.exchange(ENDPOINT_BRANDS, HttpMethod.POST, new HttpEntity<>(request, adminHeaders()), responseType);
        return response.getBody().data().id();
    }

    @DisplayName("POST /api-admin/v1/brands")
    @Nested
    class CreateBrand {

        @DisplayName("올바른 요청이면, 200 OK와 함께 브랜드 정보를 반환한다.")
        @Test
        void returnsBrandInfo_whenValidRequest() {
            // arrange
            BrandAdminV1Dto.CreateRequest request = new BrandAdminV1Dto.CreateRequest("나이키");

            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRANDS, HttpMethod.POST, new HttpEntity<>(request, adminHeaders()), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().id()).isNotNull(),
                () -> assertThat(response.getBody().data().name()).isEqualTo("나이키")
            );
        }

        @DisplayName("이름이 빈 문자열이면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void returnsBadRequest_whenNameIsBlank() {
            // arrange
            BrandAdminV1Dto.CreateRequest request = new BrandAdminV1Dto.CreateRequest("  ");

            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRANDS, HttpMethod.POST, new HttpEntity<>(request, adminHeaders()), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("어드민 인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAdminHeader() {
            // arrange
            BrandAdminV1Dto.CreateRequest request = new BrandAdminV1Dto.CreateRequest("나이키");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRANDS, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("GET /api-admin/v1/brands")
    @Nested
    class GetBrandList {

        @DisplayName("브랜드 목록을 페이지네이션하여 반환한다.")
        @Test
        void returnsPaginatedBrands() {
            // arrange
            createBrand("나이키");
            createBrand("아디다스");
            createBrand("푸마");

            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_BRANDS + "?page=0&size=2", HttpMethod.GET, new HttpEntity<>(adminHeaders()), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat((List<?>) response.getBody().data().get("content")).hasSize(2),
                () -> assertThat(((Number) response.getBody().data().get("totalElements")).longValue()).isEqualTo(3L)
            );
        }
    }

    @DisplayName("GET /api-admin/v1/brands/{brandId}")
    @Nested
    class GetBrandDetail {

        @DisplayName("존재하는 브랜드 ID이면, 200 OK와 함께 브랜드 정보를 반환한다.")
        @Test
        void returnsBrandInfo_whenBrandExists() {
            // arrange
            Long brandId = createBrand("나이키");

            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.GET, new HttpEntity<>(adminHeaders()), responseType, brandId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().id()).isEqualTo(brandId),
                () -> assertThat(response.getBody().data().name()).isEqualTo("나이키")
            );
        }

        @DisplayName("존재하지 않는 브랜드 ID이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenBrandDoesNotExist() {
            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.GET, new HttpEntity<>(adminHeaders()), responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @DisplayName("PUT /api-admin/v1/brands/{brandId}")
    @Nested
    class UpdateBrand {

        @DisplayName("올바른 요청이면, 200 OK와 함께 수정된 브랜드 정보를 반환한다.")
        @Test
        void returnsUpdatedBrand_whenValidRequest() {
            // arrange
            Long brandId = createBrand("나이키");
            BrandAdminV1Dto.UpdateRequest request = new BrandAdminV1Dto.UpdateRequest("아디다스");

            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.PUT, new HttpEntity<>(request, adminHeaders()), responseType, brandId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().name()).isEqualTo("아디다스")
            );
        }

        @DisplayName("존재하지 않는 브랜드 ID이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenBrandDoesNotExist() {
            // arrange
            BrandAdminV1Dto.UpdateRequest request = new BrandAdminV1Dto.UpdateRequest("아디다스");

            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.PUT, new HttpEntity<>(request, adminHeaders()), responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @DisplayName("DELETE /api-admin/v1/brands/{brandId}")
    @Nested
    class DeleteBrand {

        @DisplayName("존재하는 브랜드를 삭제하면, 200 OK를 반환한다.")
        @Test
        void returnsSuccess_whenBrandExists() {
            // arrange
            Long brandId = createBrand("나이키");

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), responseType, brandId);

            // assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }

        @DisplayName("삭제 후 브랜드를 조회하면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_afterDeletion() {
            // arrange
            Long brandId = createBrand("나이키");
            testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.DELETE, new HttpEntity<>(adminHeaders()),
                new ParameterizedTypeReference<ApiResponse<Void>>() {}, brandId);

            // act
            ParameterizedTypeReference<ApiResponse<BrandAdminV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandAdminV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.GET, new HttpEntity<>(adminHeaders()), responseType, brandId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @DisplayName("존재하지 않는 브랜드를 삭제하면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenBrandDoesNotExist() {
            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
