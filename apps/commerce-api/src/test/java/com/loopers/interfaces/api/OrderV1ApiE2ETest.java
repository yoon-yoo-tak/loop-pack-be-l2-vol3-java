package com.loopers.interfaces.api;

import com.loopers.interfaces.api.brand.BrandAdminV1Dto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderV1ApiE2ETest {

    private static final String ENDPOINT_ORDERS = "/api/v1/orders";
    private static final String ENDPOINT_ORDER_DETAIL = "/api/v1/orders/{orderId}";
    private static final String ENDPOINT_SIGNUP = "/api/v1/users";
    private static final String ENDPOINT_ADMIN_BRANDS = "/api-admin/v1/brands";
    private static final String ENDPOINT_ADMIN_PRODUCTS = "/api-admin/v1/products";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public OrderV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
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
    private Long brandId;
    private Long productId;

    @BeforeEach
    void setUp() {
        userId = signupUser("testUser1", "Abcd1234!", "테스터");
        brandId = createBrand("나이키");
        productId = createProduct("에어맥스 90", 159000, 100, brandId);
    }

    @DisplayName("POST /api/v1/orders")
    @Nested
    class CreateOrder {

        @DisplayName("정상적인 주문 요청이면, 200 OK와 함께 주문 정보를 반환한다.")
        @Test
        void returnsOrderInfo_whenValidRequest() {
            // arrange
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(new OrderV1Dto.OrderItemRequest(productId, 2))
            );

            // act
            ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> response =
                testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                    new HttpEntity<>(request, userHeaders("testUser1", "Abcd1234!")), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().orderId()).isNotNull(),
                () -> assertThat(response.getBody().data().totalPrice()).isEqualTo(318000),
                () -> assertThat(response.getBody().data().items()).hasSize(1),
                () -> assertThat(response.getBody().data().items().get(0).productName()).isEqualTo("에어맥스 90"),
                () -> assertThat(response.getBody().data().items().get(0).brandName()).isEqualTo("나이키")
            );
        }

        @DisplayName("여러 상품을 주문하면, 모든 상품이 포함된 주문이 생성된다.")
        @Test
        void returnsOrderWithMultipleItems_whenMultipleProducts() {
            // arrange
            Long product2Id = createProduct("에어포스 1", 129000, 200, brandId);
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(
                    new OrderV1Dto.OrderItemRequest(productId, 2),
                    new OrderV1Dto.OrderItemRequest(product2Id, 1)
                )
            );

            // act
            ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> response =
                testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                    new HttpEntity<>(request, userHeaders("testUser1", "Abcd1234!")), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().totalPrice()).isEqualTo(447000),
                () -> assertThat(response.getBody().data().items()).hasSize(2)
            );
        }

        @DisplayName("존재하지 않는 상품이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenProductDoesNotExist() {
            // arrange
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(new OrderV1Dto.OrderItemRequest(999L, 1))
            );

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                    new HttpEntity<>(request, userHeaders("testUser1", "Abcd1234!")), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @DisplayName("재고가 부족하면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void returnsBadRequest_whenInsufficientStock() {
            // arrange
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(new OrderV1Dto.OrderItemRequest(productId, 101))
            );

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                    new HttpEntity<>(request, userHeaders("testUser1", "Abcd1234!")), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAuthHeaders() {
            // arrange
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(new OrderV1Dto.OrderItemRequest(productId, 1))
            );

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                    new HttpEntity<>(request), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("GET /api/v1/orders")
    @Nested
    class GetOrders {

        @DisplayName("날짜 범위 내의 주문 목록을 반환한다.")
        @Test
        void returnsOrderList_whenValidDateRange() {
            // arrange
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(new OrderV1Dto.OrderItemRequest(productId, 1))
            );
            testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                new HttpEntity<>(request, userHeaders("testUser1", "Abcd1234!")),
                new ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderResponse>>() {});

            String today = LocalDate.now().toString();
            String tomorrow = LocalDate.now().plusDays(1).toString();

            // act
            ParameterizedTypeReference<ApiResponse<List<OrderV1Dto.OrderListResponse>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<OrderV1Dto.OrderListResponse>>> response =
                testRestTemplate.exchange(
                    ENDPOINT_ORDERS + "?startAt=" + today + "&endAt=" + tomorrow,
                    HttpMethod.GET,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data()).hasSize(1)
            );
        }

        @DisplayName("날짜 범위에 해당하는 주문이 없으면, 빈 목록을 반환한다.")
        @Test
        void returnsEmptyList_whenNoOrdersInRange() {
            // arrange
            String futureStart = LocalDate.now().plusDays(10).toString();
            String futureEnd = LocalDate.now().plusDays(11).toString();

            // act
            ParameterizedTypeReference<ApiResponse<List<OrderV1Dto.OrderListResponse>>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<OrderV1Dto.OrderListResponse>>> response =
                testRestTemplate.exchange(
                    ENDPOINT_ORDERS + "?startAt=" + futureStart + "&endAt=" + futureEnd,
                    HttpMethod.GET,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data()).isEmpty()
            );
        }

        @DisplayName("인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAuthHeaders() {
            // arrange
            String today = LocalDate.now().toString();
            String tomorrow = LocalDate.now().plusDays(1).toString();

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(
                    ENDPOINT_ORDERS + "?startAt=" + today + "&endAt=" + tomorrow,
                    HttpMethod.GET, null, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("GET /api/v1/orders/{orderId}")
    @Nested
    class GetOrder {

        @DisplayName("본인의 주문을 조회하면, 200 OK와 함께 주문 상세를 반환한다.")
        @Test
        void returnsOrderDetail_whenValidRequest() {
            // arrange
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(new OrderV1Dto.OrderItemRequest(productId, 2))
            );
            ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> createResponse =
                testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                    new HttpEntity<>(request, userHeaders("testUser1", "Abcd1234!")),
                    new ParameterizedTypeReference<>() {});
            Long orderId = createResponse.getBody().data().orderId();

            // act
            ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> response =
                testRestTemplate.exchange(ENDPOINT_ORDER_DETAIL, HttpMethod.GET,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, orderId);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().orderId()).isEqualTo(orderId),
                () -> assertThat(response.getBody().data().totalPrice()).isEqualTo(318000),
                () -> assertThat(response.getBody().data().items()).hasSize(1)
            );
        }

        @DisplayName("존재하지 않는 주문이면, 404 NOT_FOUND 응답을 받는다.")
        @Test
        void returnsNotFound_whenOrderDoesNotExist() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ORDER_DETAIL, HttpMethod.GET,
                    new HttpEntity<>(userHeaders("testUser1", "Abcd1234!")), responseType, 999L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @DisplayName("다른 유저의 주문을 조회하면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void returnsBadRequest_whenAccessingOtherUsersOrder() {
            // arrange
            OrderV1Dto.CreateRequest request = new OrderV1Dto.CreateRequest(
                List.of(new OrderV1Dto.OrderItemRequest(productId, 1))
            );
            ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> createResponse =
                testRestTemplate.exchange(ENDPOINT_ORDERS, HttpMethod.POST,
                    new HttpEntity<>(request, userHeaders("testUser1", "Abcd1234!")),
                    new ParameterizedTypeReference<>() {});
            Long orderId = createResponse.getBody().data().orderId();

            signupUser("otherUser1", "Abcd1234!", "다른유저");

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ORDER_DETAIL, HttpMethod.GET,
                    new HttpEntity<>(userHeaders("otherUser1", "Abcd1234!")), responseType, orderId);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("인증 헤더가 없으면, 401 UNAUTHORIZED 응답을 받는다.")
        @Test
        void returnsUnauthorized_whenNoAuthHeaders() {
            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                testRestTemplate.exchange(ENDPOINT_ORDER_DETAIL, HttpMethod.GET, null, responseType, 1L);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
