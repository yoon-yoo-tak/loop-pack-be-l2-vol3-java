package com.loopers.interfaces.api;

import com.loopers.interfaces.api.brand.BrandAdminV1Dto;
import com.loopers.interfaces.api.order.OrderAdminV1Dto;
import com.loopers.interfaces.api.order.OrderV1Dto;
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
class OrderAdminV1ApiE2ETest {

    private static final String ENDPOINT_ADMIN_ORDERS = "/api-admin/v1/orders";
    private static final String ENDPOINT_ADMIN_ORDER_DETAIL = "/api-admin/v1/orders/{orderId}";
    private static final String ENDPOINT_SIGNUP = "/api/v1/users";
    private static final String ENDPOINT_ADMIN_BRANDS = "/api-admin/v1/brands";
    private static final String ENDPOINT_ADMIN_PRODUCTS = "/api-admin/v1/products";
    private static final String ENDPOINT_CUSTOMER_ORDERS = "/api/v1/orders";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public OrderAdminV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
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

    private Long createOrder(String loginId, String password, Long productId, int quantity) {
        OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
            List.of(new OrderV1Dto.OrderItemRequest(productId, quantity))
        );
        ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> response =
            testRestTemplate.exchange(ENDPOINT_CUSTOMER_ORDERS, HttpMethod.POST,
                new HttpEntity<>(request, userHeaders(loginId, password)),
                new ParameterizedTypeReference<>() {});
        return response.getBody().data().orderId();
    }

    private Long userId;
    private Long brandId;
    private Long productId;

    @BeforeEach
    void setUp() {
        userId = signupUser("testUser1", "Abcd1234!", "테스터");
        brandId = createBrand("나이키");
        productId = createProduct("에어맥스 90", 159000, 100, brandId);
    }

    @DisplayName("GET /api-admin/v1/orders")
    @Nested
    class GetOrderList {

        @DisplayName("전체 주문 목록을 페이지네이션하여 반환한다.")
        @Test
        void returnsPaginatedOrders() {
            // arrange
            createOrder("testUser1", "Abcd1234!", productId, 1);
            createOrder("testUser1", "Abcd1234!", productId, 2);
            createOrder("testUser1", "Abcd1234!", productId, 3);

            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_ADMIN_ORDERS + "?page=0&size=2", HttpMethod.GET,
                    new HttpEntity<>(adminHeaders()), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat((List<?>) response.getBody().data().get("content")).hasSize(2),
                () -> assertThat(((Number) response.getBody().data().get("totalElements")).longValue()).isEqualTo(3L)
            );
        }

        @DisplayName("주문이 없으면, 빈 페이지를 반환한다.")
        @Test
        void returnsEmptyPage_whenNoOrders() {
            // act
            ParameterizedTypeReference<ApiResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                testRestTemplate.exchange(ENDPOINT_ADMIN_ORDERS + "?page=0&size=20", HttpMethod.GET,
                    new HttpEntity<>(adminHeaders()), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat((List<?>) response.getBody().data().get("content")).isEmpty()
            );
        }

        @DisplayName("어드민 인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAdminHeader() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ADMIN_ORDERS + "?page=0&size=20", HttpMethod.GET, null, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("GET /api-admin/v1/orders/{orderId}")
    @Nested
    class GetOrderDetail {

        @DisplayName("존재하는 주문 ID이면, 200 OK와 함께 주문 상세를 반환한다.")
        @Test
        void returnsOrderDetail_whenOrderExists() {
            // arrange
            Long orderId = createOrder("testUser1", "Abcd1234!", productId, 2);

            // act
            ParameterizedTypeReference<ApiResponse<OrderAdminV1Dto.OrderResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<OrderAdminV1Dto.OrderResponse>> response =
                testRestTemplate.exchange(ENDPOINT_ADMIN_ORDER_DETAIL, HttpMethod.GET,
                    new HttpEntity<>(adminHeaders()), responseType, orderId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().orderId()).isEqualTo(orderId),
                () -> assertThat(response.getBody().data().totalPrice()).isEqualTo(318000),
                () -> assertThat(response.getBody().data().items()).hasSize(1),
                () -> assertThat(response.getBody().data().items().get(0).productName()).isEqualTo("에어맥스 90"),
                () -> assertThat(response.getBody().data().items().get(0).brandName()).isEqualTo("나이키")
            );
        }

        @DisplayName("존재하지 않는 주문 ID이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenOrderDoesNotExist() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ADMIN_ORDER_DETAIL, HttpMethod.GET,
                    new HttpEntity<>(adminHeaders()), responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @DisplayName("어드민 인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAdminHeader() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ADMIN_ORDER_DETAIL, HttpMethod.GET, null, responseType, 1L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
