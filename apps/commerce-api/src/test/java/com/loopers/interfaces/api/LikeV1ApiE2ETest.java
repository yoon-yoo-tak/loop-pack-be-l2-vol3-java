package com.loopers.interfaces.api;

import com.loopers.interfaces.api.brand.BrandAdminV1Dto;
import com.loopers.interfaces.api.like.LikeV1Dto;
import com.loopers.interfaces.api.product.ProductAdminV1Dto;
import com.loopers.interfaces.api.user.UserV1Dto;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LikeV1ApiE2ETest {

    private static final String ENDPOINT_LIKE = "/api/v1/products/{productId}/likes";
    private static final String ENDPOINT_USER_LIKES = "/api/v1/users/{userId}/likes";
    private static final String ENDPOINT_SIGNUP = "/api/v1/users";
    private static final String ENDPOINT_ADMIN_BRANDS = "/api-admin/v1/brands";
    private static final String ENDPOINT_ADMIN_PRODUCTS = "/api-admin/v1/products";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public LikeV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
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

    private HttpHeaders userHeaders(String loginId, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Loopers-LoginId", loginId);
        headers.set("X-Loopers-LoginPw", password);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private Long signupUser(String loginId, String password, String name) {
        UserV1Dto.SignupRequest request = new UserV1Dto.SignupRequest(
            loginId, password, name, LocalDate.of(1990, 1, 1), loginId + "@email.com"
        );
        ResponseEntity<ApiResponse<UserV1Dto.SignupResponse>> response =
            testRestTemplate.exchange(ENDPOINT_SIGNUP, HttpMethod.POST, new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {});
        return response.getBody().data().id();
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

    private Long userId;
    private Long productId;
    private Long brandId;

    @BeforeEach
    void setUp() {
        userId = signupUser("testUser1", "Abcd1234!", "테스터");
        brandId = createBrand("나이키");
        productId = createProduct("에어맥스 90", 159000, 100, brandId);
    }

    @DisplayName("POST /api/v1/products/{productId}/likes")
    @Nested
    class AddLike {

        @DisplayName("정상적인 요청이면, 200 OK와 함께 좋아요 정보를 반환한다.")
        @Test
        void returnsLikeInfo_whenValidRequest() {
            // act
            ParameterizedTypeReference<ApiResponse<LikeV1Dto.LikeResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<LikeV1Dto.LikeResponse>> response =
                testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, productId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().productId()).isEqualTo(productId),
                () -> assertThat(response.getBody().data().productName()).isEqualTo("에어맥스 90")
            );
        }

        @DisplayName("이미 좋아요한 상품이면, 409 CONFLICT 응답을 받는다.")
        @Test
        void returnsConflict_whenAlreadyLiked() {
            // arrange
            testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST,
                new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")),
                new ParameterizedTypeReference<ApiResponse<LikeV1Dto.LikeResponse>>() {}, productId);

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, productId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @DisplayName("존재하지 않는 상품이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenProductDoesNotExist() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @DisplayName("인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAuthHeaders() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST, null, responseType, productId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("DELETE /api/v1/products/{productId}/likes")
    @Nested
    class RemoveLike {

        @DisplayName("정상적인 요청이면, 200 OK를 반환한다.")
        @Test
        void returnsSuccess_whenValidRequest() {
            // arrange
            testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST,
                new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")),
                new ParameterizedTypeReference<ApiResponse<LikeV1Dto.LikeResponse>>() {}, productId);

            // act
            ParameterizedTypeReference<ApiResponse<Void>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Void>> response =
                testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.DELETE,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, productId);

            // assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }

        @DisplayName("좋아요하지 않은 상품이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenNotLiked() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.DELETE,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, productId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @DisplayName("인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAuthHeaders() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.DELETE, null, responseType, productId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("GET /api/v1/users/{userId}/likes")
    @Nested
    class GetUserLikes {

        @DisplayName("본인의 좋아요 목록을 정상적으로 조회한다.")
        @Test
        void returnsLikedProductList_whenValidRequest() {
            // arrange
            Long product2Id = createProduct("에어포스 1", 129000, 200, brandId);
            testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST,
                new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")),
                new ParameterizedTypeReference<ApiResponse<LikeV1Dto.LikeResponse>>() {}, productId);
            testRestTemplate.exchange(ENDPOINT_LIKE, HttpMethod.POST,
                new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")),
                new ParameterizedTypeReference<ApiResponse<LikeV1Dto.LikeResponse>>() {}, product2Id);

            // act
            ParameterizedTypeReference<ApiResponse<List<LikeV1Dto.LikedProductResponse>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<LikeV1Dto.LikedProductResponse>>> response =
                testRestTemplate.exchange(ENDPOINT_USER_LIKES, HttpMethod.GET,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, userId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data()).hasSize(2)
            );
        }

        @DisplayName("좋아요한 상품이 없으면, 빈 목록을 반환한다.")
        @Test
        void returnsEmptyList_whenNoLikes() {
            // act
            ParameterizedTypeReference<ApiResponse<List<LikeV1Dto.LikedProductResponse>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<LikeV1Dto.LikedProductResponse>>> response =
                testRestTemplate.exchange(ENDPOINT_USER_LIKES, HttpMethod.GET,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, userId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data()).isEmpty()
            );
        }

        @DisplayName("다른 유저의 좋아요 목록을 조회하면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void returnsBadRequest_whenAccessingOtherUserLikes() {
            // arrange
            Long otherUserId = signupUser("otherUser1", "Abcd1234!", "다른유저");

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_USER_LIKES, HttpMethod.GET,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, otherUserId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAuthHeaders() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_USER_LIKES, HttpMethod.GET, null, responseType, userId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
