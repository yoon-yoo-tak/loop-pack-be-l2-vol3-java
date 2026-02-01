package com.loopers.interfaces.api;

import com.loopers.interfaces.api.brand.BrandAdminV1Dto;
import com.loopers.interfaces.api.brand.BrandV1Dto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BrandV1ApiE2ETest {

    private static final String ENDPOINT_BRAND_DETAIL = "/api/v1/brands/{brandId}";
    private static final String ENDPOINT_ADMIN_BRANDS = "/api-admin/v1/brands";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public BrandV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
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

    @DisplayName("GET /api/v1/brands/{brandId}")
    @Nested
    class GetBrandDetail {

        @DisplayName("존재하는 브랜드 ID이면, 200 OK와 함께 브랜드 정보를 반환한다.")
        @Test
        void returnsBrandInfo_whenBrandExists() {
            // arrange
            Long brandId = createBrand("나이키");

            // act
            ParameterizedTypeReference<ApiResponse<BrandV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.GET, null, responseType, brandId);

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
            ParameterizedTypeReference<ApiResponse<BrandV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.GET, null, responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @DisplayName("인증 없이도 조회할 수 있다.")
        @Test
        void returnsSuccess_withoutAuth() {
            // arrange
            Long brandId = createBrand("나이키");

            // act
            ParameterizedTypeReference<ApiResponse<BrandV1Dto.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandV1Dto.BrandResponse>> response =
                testRestTemplate.exchange(ENDPOINT_BRAND_DETAIL, HttpMethod.GET, null, responseType, brandId);

            // assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }
    }
}
