package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderItemCommand;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private final OrderService orderService;
    private final ProductService productService;

    @Transactional
    public OrderInfo createOrder(User user, List<OrderItemCommand> itemCommands) {
        if (itemCommands == null || itemCommands.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 비어있을 수 없습니다.");
        }

        List<OrderItem> orderItems = itemCommands.stream()
            .map(command -> {
                Product product = productService.getById(command.productId());
                product.decrementStock(command.quantity());
                return new OrderItem(
                    product.getId(),
                    command.quantity(),
                    product.getName(),
                    product.getPrice(),
                    product.getBrandName()
                );
            })
            .toList();

        Order order = orderService.createOrder(user.getId(), orderItems);
        return OrderInfo.from(order);
    }

    public List<OrderInfo> getOrders(User user, LocalDate startAt, LocalDate endAt) {
        ZonedDateTime start = startAt.atStartOfDay(ZONE_SEOUL);
        ZonedDateTime end = endAt.plusDays(1).atStartOfDay(ZONE_SEOUL);

        List<Order> orders = orderService.getOrdersByUser(user.getId(), start, end);
        return orders.stream()
            .map(OrderInfo::from)
            .toList();
    }

    public OrderInfo getOrder(User user, Long orderId) {
        Order order = orderService.getById(orderId);
        if (!order.getUserId().equals(user.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "본인의 주문만 조회할 수 있습니다.");
        }
        return OrderInfo.from(order);
    }

    public Page<OrderInfo> getAllOrders(Pageable pageable) {
        return orderService.getAll(pageable).map(OrderInfo::from);
    }

    public OrderInfo getOrderById(Long orderId) {
        Order order = orderService.getById(orderId);
        return OrderInfo.from(order);
    }
}
