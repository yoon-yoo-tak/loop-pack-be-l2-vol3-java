package com.loopers.domain.order;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private UserService userService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long userId;
    private Long brandId;
    private Long productId;

    @BeforeEach
    void setUp() {
        var user = userService.signup("testuser1", "Abcd1234!", "테스터", LocalDate.of(1990, 1, 1), "test@email.com");
        userId = user.getId();
        var brand = brandService.create("나이키");
        brandId = brand.getId();
        var product = productService.create("에어맥스 90", 159000, 100, brandId);
        productId = product.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문을 생성할 때, ")
    @Nested
    class CreateOrder {

        @DisplayName("올바른 주문 정보가 주어지면, 주문이 저장되고 재고가 차감된다.")
        @Test
        void savesOrderAndDecrementsStock_whenValidRequest() {
            // arrange
            List<OrderService.OrderItemCommand> commands = List.of(
                new OrderService.OrderItemCommand(productId, 2)
            );

            // act
            Order result = orderService.createOrder(userId, commands);

            // assert
            Product updatedProduct = productService.getById(productId);
            assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getUserId()).isEqualTo(userId),
                () -> assertThat(result.getTotalPrice()).isEqualTo(318000),
                () -> assertThat(result.getItems()).hasSize(1),
                () -> assertThat(result.getItems().get(0).getProductName()).isEqualTo("에어맥스 90"),
                () -> assertThat(result.getItems().get(0).getProductPrice()).isEqualTo(159000),
                () -> assertThat(result.getItems().get(0).getBrandName()).isEqualTo("나이키"),
                () -> assertThat(updatedProduct.getStock()).isEqualTo(98)
            );
        }

        @DisplayName("여러 상품을 주문하면, 모든 상품의 재고가 차감되고 총 가격이 올바르게 계산된다.")
        @Test
        void savesOrderWithMultipleItems_whenMultipleProducts() {
            // arrange
            Long product2Id = productService.create("에어포스 1", 129000, 200, brandId).getId();
            List<OrderService.OrderItemCommand> commands = List.of(
                new OrderService.OrderItemCommand(productId, 2),
                new OrderService.OrderItemCommand(product2Id, 1)
            );

            // act
            Order result = orderService.createOrder(userId, commands);

            // assert
            assertAll(
                () -> assertThat(result.getItems()).hasSize(2),
                () -> assertThat(result.getTotalPrice()).isEqualTo(447000)
            );
        }

        @DisplayName("존재하지 않는 상품이면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            List<OrderService.OrderItemCommand> commands = List.of(
                new OrderService.OrderItemCommand(999L, 1)
            );

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                orderService.createOrder(userId, commands);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("재고가 부족하면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenInsufficientStock() {
            // arrange
            List<OrderService.OrderItemCommand> commands = List.of(
                new OrderService.OrderItemCommand(productId, 101)
            );

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                orderService.createOrder(userId, commands);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("주문 항목이 비어있으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenItemsIsEmpty() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                orderService.createOrder(userId, List.of());
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("주문을 조회할 때, ")
    @Nested
    class GetById {

        @DisplayName("존재하는 주문 ID가 주어지면, 주문을 반환한다.")
        @Test
        void returnsOrder_whenIdExists() {
            // arrange
            Order order = orderService.createOrder(userId, List.of(
                new OrderService.OrderItemCommand(productId, 1)
            ));

            // act
            Order result = orderService.getById(order.getId());

            // assert
            assertAll(
                () -> assertThat(result.getId()).isEqualTo(order.getId()),
                () -> assertThat(result.getUserId()).isEqualTo(userId),
                () -> assertThat(result.getItems()).hasSize(1)
            );
        }

        @DisplayName("존재하지 않는 ID가 주어지면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenIdDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                orderService.getById(999L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("유저의 주문 목록을 조회할 때, ")
    @Nested
    class GetOrdersByUser {

        @DisplayName("날짜 범위 내의 주문 목록을 반환한다.")
        @Test
        void returnsOrdersWithinDateRange() {
            // arrange
            orderService.createOrder(userId, List.of(
                new OrderService.OrderItemCommand(productId, 1)
            ));

            ZonedDateTime startAt = LocalDate.now().atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().atZone(ZoneId.of("Asia/Seoul"));
            ZonedDateTime endAt = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().atZone(ZoneId.of("Asia/Seoul"));

            // act
            List<Order> result = orderService.getOrdersByUser(userId, startAt, endAt);

            // assert
            assertThat(result).hasSize(1);
        }

        @DisplayName("날짜 범위에 해당하는 주문이 없으면, 빈 목록을 반환한다.")
        @Test
        void returnsEmptyList_whenNoOrdersInRange() {
            // arrange
            orderService.createOrder(userId, List.of(
                new OrderService.OrderItemCommand(productId, 1)
            ));

            ZonedDateTime startAt = LocalDate.now().plusDays(10).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().atZone(ZoneId.of("Asia/Seoul"));
            ZonedDateTime endAt = LocalDate.now().plusDays(11).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().atZone(ZoneId.of("Asia/Seoul"));

            // act
            List<Order> result = orderService.getOrdersByUser(userId, startAt, endAt);

            // assert
            assertThat(result).isEmpty();
        }
    }
}
